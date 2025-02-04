class SiteInterface {
  static async delay(ms) {
    return new Promise((resolve) => setTimeout(resolve, ms));
  }

  static setupTheme() {
    const themeToggleButton = document.getElementById("themeToggleButton");
    if (themeToggleButton) {
      if (localStorage.getItem("theme") === "dark") {
        document.body.classList.add("dark-mode");
        themeToggleButton.textContent = "Light Mode";
      }
      themeToggleButton.addEventListener("click", () => {
        document.body.classList.toggle("dark-mode");
        const isDark = document.body.classList.contains("dark-mode");
        localStorage.setItem("theme", isDark ? "dark" : "light");
        themeToggleButton.textContent = isDark ? "Light Mode" : "Dark Mode";
      });
    }
  }

  static async handleLogout() {
    try {
      const response = await fetch("/logout", {
        method: "POST",
        credentials: "include",
      });
      const result = await response.json();
      if (result.success) {
        window.location.href = "/registration.html";
      } else {
        throw new Error(result.message || "Logout failed");
      }
    } catch (error) {
      console.error("Logout error:", error);
      alert("Errore durante il logout: " + error.message);
    }
  }

  static setupLogoutButton() {
    const logoutButton = document.getElementById("logoutButton");
    if (logoutButton) {
      logoutButton.addEventListener("click", this.handleLogout);
    }
  }
}

class AuthenticationManager {
  static async checkSession() {
    try {
      await SiteInterface.delay(500);

      const response = await fetch("/checkSession", {
        method: "GET",
        credentials: "include",
      });

      if (!response.ok) {
        throw new Error("Session check failed");
      }

      const data = await response.json();
      console.log("Session check response:", data);

      if (data.authenticated) {
        const urlParams = new URLSearchParams(window.location.search);
        const destination = urlParams.get("redirect") || "/portfolio.html";

        if (destination !== window.location.pathname) {
          window.location.replace(destination);
          return;
        }
      }

      document.getElementById("authContainer").style.display = "block";
    } catch (error) {
      console.error("Session check error:", error);
      document.getElementById("authContainer").style.display = "block";
    }
  }

  static async handleLogin(e) {
    e.preventDefault();
    const username = document.getElementById("loginUsername").value;
    const password = document.getElementById("loginPassword").value;

    try {
      const response = await fetch("/login", {
        method: "POST",
        headers: {
          "Content-Type": "application/x-www-form-urlencoded",
          Accept: "application/json",
        },
        body: new URLSearchParams({ username, password }).toString(),
        credentials: "include",
      });

      const result = await response.json();
      if (result.success) {
        const urlParams = new URLSearchParams(window.location.search);
        const redirectUrl = urlParams.get("redirect") || "/portfolio.html";
        window.location.href = redirectUrl;
      } else {
        alert(result.message || "Login failed");
      }
    } catch (error) {
      console.error("Login error:", error);
      alert("Login failed: " + error.message);
    }
  }

  static async handleRegister(e) {
    e.preventDefault();
    const username = document.getElementById("registerUsername").value;
    const password = document.getElementById("registerPassword").value;

    try {
      const response = await fetch("/register", {
        method: "POST",
        headers: {
          "Content-Type": "application/x-www-form-urlencoded",
          Accept: "application/json",
        },
        body: new URLSearchParams({ username, password }),
        credentials: "include",
      });

      const text = await response.text();
      try {
        const result = JSON.parse(text);
        if (result.success) {
          alert("Registration successful!");
          document.getElementById("registerForm").reset();
        } else {
          alert(result.message || "Registration failed");
        }
      } catch (parseError) {
        console.error("Parse error:", parseError);
        alert("Server response error: " + text);
      }
    } catch (error) {
      console.error("Registration error:", error);
      alert("Registration failed: " + error.message);
    }
  }

  static setupForms() {
    const loginForm = document.getElementById("loginForm");
    const registerForm = document.getElementById("registerForm");

    if (loginForm) loginForm.addEventListener("submit", this.handleLogin);
    if (registerForm)
      registerForm.addEventListener("submit", this.handleRegister);
  }
}

// Initialize everything when the document is ready
document.addEventListener("DOMContentLoaded", () => {
  // Hide auth container initially
  const authContainer = document.getElementById("authContainer");
  if (authContainer) authContainer.style.display = "none";

  // Setup site interface
  SiteInterface.setupTheme();
  SiteInterface.setupLogoutButton();

  // Setup authentication
  AuthenticationManager.setupForms();
  AuthenticationManager.checkSession();
});
