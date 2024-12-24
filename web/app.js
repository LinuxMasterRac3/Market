document.addEventListener("DOMContentLoaded", function () {
  document
    .getElementById("addStockForm")
    .addEventListener("submit", async function (e) {
      e.preventDefault();
      var ticker = document.getElementById("ticker").value;
      var quantity = parseInt(document.getElementById("quantity").value);
      var purchasePrice = parseFloat(
        document.getElementById("purchasePrice").value
      );
      var isBond = document.getElementById("isBond").checked;

      var data = new URLSearchParams();
      data.append("ticker", ticker);
      data.append("quantity", quantity);
      data.append("purchasePrice", purchasePrice);
      if (isBond) {
        data.append("isBond", "on");
      }

      try {
        const response = await fetch("/addStock", {
          method: "POST",
          headers: {
            "Content-Type": "application/x-www-form-urlencoded",
          },
          body: data,
        });
        if (response.status === 401) {
          alert("Please login first");
          return;
        }
        const result = await response.text();
        console.log(result);
        await fetchPortfolio(); // Wait for the portfolio to update
      } catch (error) {
        console.error("Errore:", error);
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
        const response = await fetch("/login", {
          // Ensure the endpoint is correct
          method: "POST",
          headers: {
            "Content-Type": "application/x-www-form-urlencoded",
          },
          body: data,
        });

        if (!response.ok) {
          // Log the response status and text for debugging
          const errorText = await response.text();
          console.error(`Login Error: ${response.status} - ${errorText}`);
          alert(`Errore durante il login: ${errorText}`);
          return;
        }

        const result = await response.text();
        if (response.status === 200) {
          alert("Login successful");
          // Mostra il contenuto del portfolio
          document.getElementById("portfolioList").style.display = "block";
          // Rendi visibile il portfolio
          document.getElementById("portfolioContent").style.display = "block";
          document.getElementById("accountSection").style.display = "block";
          showAuthForms(false);
          // Carica il portafoglio
          await fetchPortfolio();
        } else {
          alert(result);
        }
      } catch (error) {
        console.error("Errore:", error);
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
        const response = await fetch("/register", {
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
      const response = await fetch("/transactionHistory", {
        method: "GET",
        headers: {
          "Content-Type": "application/json",
        },
        credentials: "same-origin",
      });

      if (response.status === 401) {
        console.warn(
          "Utente non autenticato per recuperare lo storico delle transazioni."
        );
        document.getElementById("transactionHistory").innerHTML =
          "<p>Accesso richiesto per visualizzare lo storico delle transazioni.</p>";
        return;
      }

      const transactions = await response.json();
      displayTransactionHistory(transactions);
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
    table.className = "table table-striped";

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
      quantityCell.textContent = tx.quantity || "-";
      row.appendChild(quantityCell);

      const priceCell = document.createElement("td");
      priceCell.textContent = tx.price ? tx.price.toFixed(2) : "-";
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
      const response = await fetch("/portfolio");
      if (!response.ok) {
        // Log the response status and text for debugging
        const errorText = await response.text();
        console.error(
          `Fetch Portfolio Error: ${response.status} - ${errorText}`
        );
        alert(
          "Errore nel caricamento del portafoglio. Controlla la console per maggiori dettagli."
        );
        return;
      }

      // Check if response is JSON
      const contentType = response.headers.get("Content-Type");
      if (!contentType || !contentType.includes("application/json")) {
        console.error(`Unexpected Content-Type: ${contentType}`);
        console.error("Response Body:", await response.text());
        alert(
          "Risposta non in formato JSON. Controlla la console per maggiori dettagli."
        );
        return;
      }

      const data = await response.json();
      displayPortfolio(data.stocks);
      updateSummary(data.stocks); // Update the summary after fetching the portfolio
      updatePieChart(data.stocks); // Aggiorna il grafico a torta
      await fetchTransactionHistory(); // Recupera lo storico delle transazioni
    } catch (error) {
      console.error("Errore durante il fetch del portafoglio:", error);
      alert(
        "Si è verificato un errore durante il caricamento del portafoglio."
      );
    }
  }

  document
    .getElementById("updatePricesButton")
    .addEventListener("click", async function () {
      try {
        console.log("Inizio aggiornamento prezzi"); // Log aggiunto
        const response = await fetch("/updatePrices");
        const result = await response.text();
        console.log("Risultato aggiornamento prezzi:", result); // Log aggiunto
        await fetchPortfolio(); // Aggiorna il portafoglio dopo l'aggiornamento dei prezzi
      } catch (error) {
        console.error("Errore durante l'aggiornamento dei prezzi:", error);
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

      // Pulsanti azione
      var modifyBtn = document.createElement("button");
      modifyBtn.className = "btn btn-warning mr-2";
      modifyBtn.textContent = "Modifica";
      modifyBtn.addEventListener("click", function () {
        modifyStock(stock);
      });
      item.appendChild(modifyBtn);

      var deleteBtn = document.createElement("button");
      deleteBtn.className = "btn btn-danger";
      deleteBtn.textContent = "Cancella";
      deleteBtn.addEventListener("click", function () {
        deleteStock(stock.ticker);
      });
      item.appendChild(deleteBtn);

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
      fetch("/modifyStock", {
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
      fetch("/removeStock", {
        method: "POST",
        headers: {
          "Content-Type": "application/x-www-form-urlencoded",
        },
        body: new URLSearchParams({ ticker: ticker }),
      })
        .then((response) => {
          if (response.status === 200) {
            return response.text();
          } else if (response.status === 404) {
            throw new Error("Stock non trovata.");
          } else {
            throw new Error("Errore nella rimozione della stock.");
          }
        })
        .then((result) => {
          console.log(result);
          alert(result);
          fetchPortfolio(); // Aggiorna il portafoglio dopo l'eliminazione
        })
        .catch((error) => {
          console.error("Errore:", error);
          alert(error.message);
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
        const response = await fetch("logout", {
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

  // Inizializza la lista del portafoglio all'avvio
  fetchPortfolio();
});
