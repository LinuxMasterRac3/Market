document.addEventListener("DOMContentLoaded", function () {
  // Remove createAuthForms and related functions

  async function checkSession() {
    try {
      const response = await fetch("/checkSession", {
        method: "GET",
        credentials: "include",
      });

      if (!response.ok || !(await response.json()).authenticated) {
        window.location.replace("/registration.html");
        return;
      }

      // If authenticated, show portfolio
      document.getElementById("portfolioContent").style.display = "block";
      await fetchPortfolio();
    } catch (error) {
      console.error("Session check error:", error);
      window.location.replace("/registration.html");
    }
  }

  // Handle logout
  document
    .getElementById("logoutButton")
    .addEventListener("click", async function () {
      try {
        const response = await fetch("/logout", {
          method: "POST",
          credentials: "include",
        });

        if (response.ok) {
          window.location.replace("/registration.html");
        } else {
          console.error("Logout failed");
        }
      } catch (error) {
        console.error("Logout error:", error);
      }
    });

  // Initialize portfolio
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
        const response = await fetch("/portfolio", {
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

  let pieChart; // Variabile globale per il grafico a torta

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
      console.log("Fetching portfolio...");
      const response = await fetch("/portfolio", {
        method: "GET",
        credentials: "include", // Important for sending cookies
        headers: {
          Accept: "application/json",
        },
      });

      console.log("Portfolio response status:", response.status);

      if (!response.ok) {
        if (response.status === 401) {
          console.log("User not authenticated, showing login form");
          showAuthForms(true);
          return;
        }
        throw new Error(`HTTP error! status: ${response.status}`);
      }

      const text = await response.text();
      console.log("Raw portfolio response:", text);

      const data = JSON.parse(text);
      console.log("Parsed portfolio data:", data);

      if (data.error) {
        throw new Error(data.error);
      }

      document.getElementById("portfolioContent").style.display = "block";

      if (data.stocks && Array.isArray(data.stocks)) {
        displayPortfolio(data.stocks);
        updateSummary(data.stocks);
        updatePieChart(data.stocks);
      } else {
        console.warn("No stocks data found in response");
        document.getElementById("portfolioList").innerHTML =
          "<p>No stocks in portfolio</p>";
      }

      if (data.transactions && Array.isArray(data.transactions)) {
        displayTransactionHistory(data.transactions);
      }
    } catch (error) {
      console.error("Portfolio fetch error:", error);
      document.getElementById("portfolioContent").innerHTML = `
            <div class="alert alert-danger">
                Error loading portfolio: ${error.message}
                <button onclick="fetchPortfolio()" class="btn btn-link">Retry</button>
            </div>
        `;
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
      fetch("/portfolio", {
        method: "POST",
        headers: {
          "Content-Type": "application/x-www-form-urlencoded",
        },
        body: new URLSearchParams({
          action: "modify",
          ticker: stock.ticker,
          quantity: newQuantity,
          purchasePrice: newPurchasePrice,
          isBond: newIsBond,
        }),
        credentials: "include",
      })
        .then((response) => response.json())
        .then((result) => {
          if (result.success) {
            fetchPortfolio();
          } else {
            throw new Error(result.message || "Failed to modify stock");
          }
        })
        .catch((error) => {
          console.error("Errore:", error);
          alert("Error modifying stock: " + error.message);
        });
    }
  }

  function deleteStock(ticker) {
    if (confirm("Sei sicuro di voler cancellare " + ticker + "?")) {
      fetch("/portfolio", {
        method: "POST",
        headers: {
          "Content-Type": "application/x-www-form-urlencoded",
        },
        body: new URLSearchParams({
          action: "remove",
          ticker: ticker,
        }),
        credentials: "include",
      })
        .then((response) => response.json())
        .then((result) => {
          if (result.success) {
            fetchPortfolio();
          } else {
            throw new Error(result.message || "Failed to delete stock");
          }
        })
        .catch((error) => {
          console.error("Error:", error);
          alert("Error deleting stock: " + error.message);
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
        const response = await fetch("/logout", {
          method: "POST",
          credentials: "include",
        });
        const result = await response.json();
        if (result.success) {
          showAuthForms(true);
          document.getElementById("portfolioContent").style.display = "none";
          document.getElementById("accountSection").style.display = "none";
        } else {
          alert("Logout error: " + result.message);
        }
      } catch (error) {
        console.error("Logout error:", error);
        alert("Logout error");
      }
    });
  document.addEventListener("DOMContentLoaded", function () {
    // Theme handling and logout handler
    const themeToggleButton = document.getElementById("themeToggleButton");
    if (localStorage.getItem("theme") === "dark") {
      document.body.classList.add("dark-mode");
      themeToggleButton.textContent = "Light Mode";
    }
    themeToggleButton.addEventListener("click", function () {
      document.body.classList.toggle("dark-mode");
      const isDark = document.body.classList.contains("dark-mode");
      localStorage.setItem("theme", isDark ? "dark" : "light");
      themeToggleButton.textContent = isDark ? "Light Mode" : "Dark Mode";
    });

    // Modified logout endpoint from /api/logout to /logout
  });
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
