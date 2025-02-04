class SessionManager {
  static async checkAuthentication() {
    try {
      const response = await fetch("/checkSession", {
        method: "GET",
        credentials: "include",
      });

      const data = await response.json();
      return {
        isAuthenticated: data.authenticated,
        status: response.status,
      };
    } catch (error) {
      console.error("Errore nel controllo dell'autenticazione:", error);
      return {
        isAuthenticated: false,
        status: 500,
      };
    }
  }

  static async logout() {
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

  static async initializePage(
    contentId = "mainContent",
    redirectPath = "/registration.html"
  ) {
    const { isAuthenticated, status } = await this.checkAuthentication();

    if (isAuthenticated) {
      document.getElementById(contentId).style.display = "block";
      return true;
    } else {
      if (status === 401) {
        window.location.href = `${redirectPath}?redirect=${window.location.pathname}`;
      }
      return false;
    }
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
      logoutButton.addEventListener("click", () => this.logout());
    }
  }
}
