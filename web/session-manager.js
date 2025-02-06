class SessionManager {
  static async checkSession() {
    try {
      const response = await fetch("/checkSession", {
        method: "GET",
        credentials: "include",
      });

      if (!response.ok) {
        return false;
      }

      const data = await response.json();
      return Boolean(data.authenticated);
    } catch (error) {
      console.error("Session check error:", error);
      return false;
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
      alert("Error during logout: " + error.message);
    }
  }

  static async initializePage(contentId) {
    const isAuthenticated = await this.checkSession();
    if (!isAuthenticated) {
      window.location.replace("/registration.html");
      return false;
    }
    document.getElementById(contentId).style.display = "block";
    return true;
  }

  static setupEventListeners() {
    // Theme handling
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

    // Logout handling
    const logoutButton = document.getElementById("logoutButton");
    if (logoutButton) {
      logoutButton.addEventListener("click", this.handleLogout);
    }
  }
}
