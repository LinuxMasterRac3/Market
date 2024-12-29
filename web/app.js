document.addEventListener("DOMContentLoaded", function () {
  // Aggiungi questa funzione all'inizio
  async function checkSession() {
    try {
      const response = await fetch("/api/checkSession", {
        method: "GET",
        credentials: "same-origin",
      });

      if (response.ok) {
        const data = await response.json();
        if (data.authenticated) {
          document.getElementById("portfolioList").style.display = "block";
          document.getElementById("portfolioContent").style.display = "block";
          document.getElementById("accountSection").style.display = "block";
          showAuthForms(false);
          await fetchPortfolio();
          return;
        }
      }
      showAuthForms(true);
    } catch (error) {
      console.error("Errore nel controllo della sessione:", error);
      showAuthForms(true);
    }
  }

  // Chiama checkSession all'avvio
  checkSession();

  // Crea dinamicamente l'overlay di caricamento (spinner)
  const loadingOverlay = document.createElement("div");
  loadingOverlay.className = "loading-overlay";
  loadingOverlay.innerHTML = '<div class="spinner"></div>';
  document.body.appendChild(loadingOverlay);

  // Helper per mostrare/hide l'overlay
  function showLoadingOverlay(show) {
    loadingOverlay.style.display = show ? "flex" : "none";
  }

  document
    .getElementById("addStockForm")
    .addEventListener("submit", async function (e) {
      e.preventDefault();
      showLoadingOverlay(true); // Mostra l'overlay
      var ticker = document.getElementById("ticker").value;
      var quantity = parseInt(document.getElementById("quantity").value);
      var purchasePrice = parseFloat(
        document.getElementById("purchasePrice").value
      );
      var isBond = document.getElementById("isBond").checked;

      var data = new URLSearchParams();
      data.append("action", "add"); // Add this line
      data.append("ticker", ticker);
      data.append("quantity", quantity);
      data.append("purchasePrice", purchasePrice);
      data.append("isBond", isBond);

      try {
        const response = await fetch("/api/portfolio", {
          // Changed endpoint
          method: "POST",
          headers: {
            "Content-Type": "application/x-www-form-urlencoded",
          },
          body: data,
          credentials: "include", // Important for session handling
        });
        if (response.status === 401) {
          alert("Please login first");
          return;
        }
        const result = await response.json();
        if (result.success) {
          await fetchPortfolio();
        } else {
          alert(result.message || "Error adding stock");
        }
      } catch (error) {
        console.error("Error:", error);
        alert("Error adding stock");
      } finally {
        showLoadingOverlay(false); // Nascondi l’overlay in ogni caso
      }
    });

  document
    .getElementById("loginForm")
    .addEventListener("submit", async function (e) {
      e.preventDefault();
      const username = document.getElementById("loginUsername").value;
      const password = document.getElementById("loginPassword").value;

      const data = new URLSearchParams();
      data.append("username", username);
      data.append("password", password);

      try {
        const response = await fetch("/api/login", {
          method: "POST",
          headers: {
            "Content-Type": "application/x-www-form-urlencoded",
          },
          body: data,
          credentials: "include",
        });

        const result = await response.json();
        console.log("Login response:", result); // Debug log

        if (result.success) {
          console.log("Login successful");
          document.getElementById("portfolioList").style.display = "block";
          document.getElementById("portfolioContent").style.display = "block";
          document.getElementById("accountSection").style.display = "block";
          showAuthForms(false);
          await fetchPortfolio();
        } else {
          console.error("Login failed:", result.message);
          alert(result.message);
        }
      } catch (error) {
        console.error("Login error:", error);
        alert("Error during login. Please try again.");
      }
    });

  document
    .getElementById("registerForm")
    .addEventListener("submit", async function (e) {
      e.preventDefault();
      const username = document.getElementById("registerUsername").value;
      const password = document.getElementById("registerPassword").value;

      const data = new URLSearchParams();
      data.append("username", username);
      data.append("password", password);

      try {
        const response = await fetch("/api/register", {
          method: "POST",
          headers: {
            "Content-Type": "application/x-www-form-urlencoded",
          },
          body: data,
        });
        const result = await response.text();
        if (response.status === 200) {
          alert("Registration successful");
        } else {
          alert(result);
        }
      } catch (error) {
        console.error("Errore:", error);
      }
    });

  // Function to fetch and display the portfolio
  let pieChart; // Variabile globale per il grafico a torta

  // Funzione per recuperare e mostrare lo storico delle transazioni
  async function fetchTransactionHistory() {
    try {
      const response = await fetch("/api/portfolio", {
        method: "GET",
        credentials: "include",
        headers: {
          Accept: "application/json",
        },
      });

      if (response.status === 401) {
        console.warn(
          "Utente non autenticato per recuperare lo storico delle transazioni."
        );
        document.getElementById("transactionHistory").innerHTML =
          "<p>Accesso richiesto per visualizzare lo storico delle transazioni.</p>";
        return;
      }

      const data = await response.json();
      if (data.transactions) {
        displayTransactionHistory(data.transactions);
      }
    } catch (error) {
      console.error(
        "Errore nel recupero dello storico delle transazioni:",
        error
      );
      document.getElementById("transactionHistory").innerHTML =
        "<p>Errore nel caricamento dello storico delle transazioni.</p>";
    }
  }

  function displayTransactionHistory(transactions) {
    const container = document.getElementById("transactionHistory");
    container.innerHTML = "";

    if (transactions.length === 0) {
      container.innerHTML = "<p>Nessuna transazione registrata.</p>";
      return;
    }

    // Mostra solo le ultime 4 transazioni
    const recentTransactions = transactions.slice(-4).reverse();

    const table = document.createElement("table");
    table.className = "table";

    const thead = document.createElement("thead");
    const headerRow = document.createElement("tr");
    ["Tipo", "Ticker", "Quantità", "Prezzo (€)", "Data"].forEach((text) => {
      const th = document.createElement("th");
      th.textContent = text;
      headerRow.appendChild(th);
    });
    thead.appendChild(headerRow);
    table.appendChild(thead);

    const tbody = document.createElement("tbody");
    recentTransactions.forEach((tx) => {
      const row = document.createElement("tr");

      const typeCell = document.createElement("td");
      typeCell.textContent = tx.type;
      row.appendChild(typeCell);

      const tickerCell = document.createElement("td");
      tickerCell.textContent = tx.ticker;
      row.appendChild(tickerCell);

      const quantityCell = document.createElement("td");
      // Show quantity for all transaction types
      quantityCell.textContent = tx.quantity.toString();
      row.appendChild(quantityCell);

      const priceCell = document.createElement("td");
      // Show price for all transaction types
      priceCell.textContent = tx.price ? tx.price.toFixed(2) : "0.00";
      row.appendChild(priceCell);

      const dateCell = document.createElement("td");
      const date = new Date(tx.timestamp);
      dateCell.textContent = date.toLocaleString();
      row.appendChild(dateCell);

      tbody.appendChild(row);
    });
    table.appendChild(tbody);
    container.appendChild(table);
  }

  async function fetchPortfolio() {
    try {
      const response = await fetch("/api/portfolio", {
        method: "GET",
        credentials: "include", // Important for sending cookies
        headers: {
          Accept: "application/json",
        },
      });

      if (!response.ok) {
        if (response.status === 401) {
          showAuthForms(true);
          return;
        }
        throw new Error(`HTTP error! status: ${response.status}`);
      }

      const data = await response.json();
      if (data.error) {
        throw new Error(data.error);
      }

      displayPortfolio(data.stocks);
      updateSummary(data.stocks);
      updatePieChart(data.stocks);
      await fetchTransactionHistory();
    } catch (error) {
      console.error("Portfolio fetch error:", error);
      if (error.message.includes("401")) {
        showAuthForms(true);
      } else {
        alert(`Error loading portfolio: ${error.message}`);
      }
    }
  }

  document
    .getElementById("updatePricesButton")
    .addEventListener("click", async function () {
      try {
        const data = new URLSearchParams();
        data.append("action", "updatePrices");

        const response = await fetch("/api/portfolio", {
          method: "POST",
          headers: {
            "Content-Type": "application/x-www-form-urlencoded",
          },
          body: data,
          credentials: "include",
        });

        const result = await response.json();
        if (result.success) {
          await fetchPortfolio();
        } else {
          alert(result.message || "Error updating prices");
        }
      } catch (error) {
        console.error("Error:", error);
        alert("Error updating prices");
      }
    });

  function displayPortfolio(stocks) {
    var portfolioList = document.getElementById("portfolioList");
    portfolioList.innerHTML = "";
    stocks.forEach(function (stock) {
      // Remove the card structure, create a list-like row
      var item = document.createElement("div");
      item.className = "stock-item d-flex align-items-center";

      var title = document.createElement("span");
      title.textContent = stock.ticker + " | ";
      item.appendChild(title);

      var quantity = document.createElement("span");
      quantity.textContent = "Quantità: " + stock.quantity + " | ";
      item.appendChild(quantity);

      var purchasePrice = document.createElement("span");
      purchasePrice.textContent =
        "Acquisto: €" + stock.purchasePrice.toFixed(2) + " | ";
      item.appendChild(purchasePrice);

      var currentPrice = document.createElement("span");
      currentPrice.textContent =
        "Attuale: €" + stock.currentPrice.toFixed(2) + " | ";
      item.appendChild(currentPrice);

      var variationValue =
        (stock.currentPrice - stock.purchasePrice) * stock.quantity;
      var variation = document.createElement("span");
      variation.textContent =
        "Variazione: €" + variationValue.toFixed(2) + " | ";
      item.appendChild(variation);

      var taxRate = stock.isBond ? 0.125 : 0.26;
      var taxValue = variationValue * taxRate;
      var taxes = document.createElement("span");
      taxes.textContent = "Tasse: €" + taxValue.toFixed(2) + " | ";
      item.appendChild(taxes);

      // Create actions container
      var actionsDiv = document.createElement("div");
      actionsDiv.className = "stock-actions";

      // Create and append Modify button
      var modifyBtn = document.createElement("button");
      modifyBtn.className = "btn btn-warning mr-2";
      modifyBtn.textContent = "Modifica";
      modifyBtn.addEventListener("click", function () {
        modifyStock(stock);
      });
      actionsDiv.appendChild(modifyBtn);

      // Create and append Delete button
      var deleteBtn = document.createElement("button");
      deleteBtn.className = "btn btn-danger";
      deleteBtn.textContent = "Cancella";
      deleteBtn.addEventListener("click", function () {
        deleteStock(stock.ticker);
      });
      actionsDiv.appendChild(deleteBtn);

      // Append actionsDiv to item
      item.appendChild(actionsDiv);

      portfolioList.appendChild(item);
    });
  }

  function updateSummary(stocks) {
    let totalValue = 0;
    let totalVariation = 0;
    let totalTaxes = 0;

    stocks.forEach((stock) => {
      totalValue += stock.currentPrice * stock.quantity;
      const variation =
        (stock.currentPrice - stock.purchasePrice) * stock.quantity;
      totalVariation += variation;
      const taxRate = stock.isBond ? 0.125 : 0.26;
      totalTaxes += variation * taxRate;
    });

    document.getElementById("totalValue").textContent =
      "Valore Totale: €" + totalValue.toFixed(2);
    document.getElementById("totalVariation").textContent =
      "Variazione Totale: €" + totalVariation.toFixed(2);
    document.getElementById("totalTaxes").textContent =
      "Tasse Totali: €" + totalTaxes.toFixed(2);
  }

  function modifyStock(stock) {
    // Prompt per ottenere nuovi valori
    const newQuantity = prompt("Inserisci la nuova quantità:", stock.quantity);
    const newPurchasePrice = prompt(
      "Inserisci il nuovo prezzo di acquisto:",
      stock.purchasePrice
    );
    const newIsBond = confirm("È un Bond? Premi OK per Sì, Annulla per No.");

    if (newQuantity !== null && newPurchasePrice !== null) {
      fetch("/api/modifyStock", {
        method: "POST",
        headers: {
          "Content-Type": "application/x-www-form-urlencoded",
        },
        body: new URLSearchParams({
          ticker: stock.ticker,
          quantity: newQuantity,
          purchasePrice: newPurchasePrice,
          isBond: newIsBond ? "on" : "off",
        }),
      })
        .then((response) => response.text())
        .then((result) => {
          console.log(result);
          fetchPortfolio();
        })
        .catch((error) => {
          console.error("Errore:", error);
        });
    }
  }

  function deleteStock(ticker) {
    if (confirm("Sei sicuro di voler cancellare " + ticker + "?")) {
      const data = new URLSearchParams();
      data.append("action", "remove");
      data.append("ticker", ticker);

      fetch("/api/portfolio", {
        method: "POST",
        headers: {
          "Content-Type": "application/x-www-form-urlencoded",
        },
        body: data,
        credentials: "include",
      })
        .then((response) => response.json())
        .then((result) => {
          if (result.success) {
            fetchPortfolio();
          } else {
            alert(result.message || "Error removing stock");
          }
        })
        .catch((error) => {
          console.error("Error:", error);
          alert("Error removing stock");
        });
    }
  }

  function updatePieChart(stocks) {
    var ctx = document.getElementById("portfolioPieChart").getContext("2d");

    // Calcola il valore totale del portafoglio
    var total = stocks.reduce(
      (sum, stock) => sum + stock.currentPrice * stock.quantity,
      0
    );

    if (total === 0) {
      console.warn(
        "Il totale del portafoglio è 0. Grafico a torta non creato."
      );
      return;
    }

    // Prepara i dati per il grafico a torta
    var labels = stocks.map((stock) => stock.ticker);
    var data = stocks.map((stock) =>
      (((stock.currentPrice * stock.quantity) / total) * 100).toFixed(2)
    );
    var nominalData = stocks.map((stock) =>
      (stock.currentPrice * stock.quantity).toFixed(2)
    );

    // Genera colori casuali per le slice
    var backgroundColors = stocks.map(
      () =>
        "#" +
        Math.floor(Math.random() * 16777215)
          .toString(16)
          .padStart(6, "0")
    );

    if (pieChart) {
      pieChart.destroy();
    }

    pieChart = new Chart(ctx, {
      type: "pie",
      data: {
        labels: labels,
        datasets: [
          {
            data: data,
            backgroundColor: backgroundColors,
          },
        ],
      },
      options: {
        responsive: true,
        plugins: {
          tooltip: {
            callbacks: {
              label: function (context) {
                var index = context.dataIndex;
                var label = context.label || "";
                var value = data[index];
                var nominal = nominalData[index];
                return label + ": " + value + "% (€" + nominal + ")";
              },
            },
          },
        },
      },
    });
  }

  function showAuthForms(show) {
    document.getElementById("authContainer").style.display = show
      ? "block"
      : "none";
  }

  function logoutUser() {
    // Semplice logout lato client
    showAuthForms(true);
    document.getElementById("portfolioContent").style.display = "none";
    document.getElementById("accountSection").style.display = "none";
    console.log("Logout effettuato");
  }

  // Fix the logout functionality
  document
    .getElementById("logoutButton")
    .addEventListener("click", async function () {
      try {
        const response = await fetch("api/logout", {
          method: "GET", // Cambiato da POST a GET
          credentials: "same-origin",
        });

        console.log("Logout response status:", response.status); // Debug log

        // Gestisci il logout indipendentemente dalla risposta
        showAuthForms(true);
        document.getElementById("portfolioContent").style.display = "none";
        document.getElementById("accountSection").style.display = "none";
        document.getElementById("loginUsername").value = "";
        document.getElementById("loginPassword").value = "";

        const text = await response.text();
        console.log("Logout response:", text); // Debug log

        if (response.ok) {
          console.log("Logout effettuato con successo");
        } else {
          console.warn("Logout completato con warning:", text);
        }
      } catch (error) {
        console.error("Errore durante il logout:", error);
        // Esegui comunque il logout lato client
        showAuthForms(true);
        document.getElementById("portfolioContent").style.display = "none";
        document.getElementById("accountSection").style.display = "none";
      }
    });

  /* Theme Switcher */
  const themeToggleButton = document.getElementById("themeToggleButton");

  if (themeToggleButton) {
    // Load theme preference from localStorage
    if (localStorage.getItem("theme") === "dark") {
      document.body.classList.add("dark-mode");
      themeToggleButton.textContent = "Light Mode";
      console.log("Dark mode enabled from localStorage.");
    }

    // Toggle theme on button click
    themeToggleButton.addEventListener("click", function () {
      document.body.classList.toggle("dark-mode");
      if (document.body.classList.contains("dark-mode")) {
        themeToggleButton.textContent = "Light Mode";
        localStorage.setItem("theme", "dark");
        console.log("Dark mode enabled.");
      } else {
        themeToggleButton.textContent = "Dark Mode";
        localStorage.setItem("theme", "light");
        console.log("Light mode enabled.");
      }
    });
  } else {
    console.warn("themeToggleButton non trovato nell'HTML.");
  }

  // 1. Optimize Event Listeners for Mobile
  // Use touch events if necessary
  const buttons = document.querySelectorAll(".btn, button");
  buttons.forEach((button) => {
    button.addEventListener("touchstart", () => {
      button.classList.add("active");
    });
    button.addEventListener("touchend", () => {
      button.classList.remove("active");
    });
  });

  // 2. Debounce Resize Events
  window.addEventListener(
    "resize",
    debounce(function () {
      // Handle resize events if necessary
      console.log("Window resized");
    }, 250)
  );

  // Debounce function to limit the rate at which a function can fire.
  function debounce(func, wait) {
    let timeout;
    return function () {
      const context = this,
        args = arguments;
      clearTimeout(timeout);
      timeout = setTimeout(() => func.apply(context, args), wait);
    };
  }

  // 3. Lazy Load Images and Content
  const lazyImages = document.querySelectorAll("img.lazy");
  const observer = new IntersectionObserver((entries, observer) => {
    entries.forEach((entry) => {
      if (entry.isIntersecting) {
        const img = entry.target;
        img.src = img.dataset.src;
        img.classList.remove("lazy");
        observer.unobserve(img);
      }
    });
  });

  lazyImages.forEach((img) => {
    observer.observe(img);
  });

  // Inizializza la lista del portafoglio all'avvio
  fetchPortfolio();
});
