async function loadFinancialData() {
  try {
    const response = await fetch("/cashflow/summary", {
      method: "GET",
      credentials: "include",
    });

    if (!response.ok) {
      throw new Error("Failed to load financial data");
    }

    const data = await response.json();

    // Format numbers with 2 decimal places and thousands separator
    const formatCurrency = (value) =>
      new Intl.NumberFormat("it-IT", {
        style: "currency",
        currency: "EUR",
      }).format(value);

    document.getElementById("cashBalance").textContent = formatCurrency(
      data.cashBalance
    );
    document.getElementById("investedCapital").textContent = formatCurrency(
      data.investedCapital
    );
    document.getElementById("totalCapital").textContent = formatCurrency(
      data.totalCapital
    );
    document.getElementById(
      "lastUpdate"
    ).textContent = `Ultimo aggiornamento: ${new Date(
      data.lastUpdate
    ).toLocaleString("it-IT")}`;
  } catch (error) {
    console.error("Error loading financial data:", error);
    document.getElementById("cashBalance").textContent = "Errore";
    document.getElementById("investedCapital").textContent = "Errore";
    document.getElementById("totalCapital").textContent = "Errore";
  }
}

async function loadTransactionHistory() {
  try {
    const response = await fetch("/cashflow/transactions", {
      method: "GET",
      credentials: "include",
    });

    if (!response.ok) {
      throw new Error("Failed to load transactions");
    }

    const data = await response.json();
    displayTransactionHistory(data.transactions);
    updateMonthlyChart(data.transactions);
  } catch (error) {
    console.error("Error loading transactions:", error);
  }
}

function displayTransactionHistory(transactions) {
  const container = document.getElementById("transactionHistory");
  container.innerHTML = `
        <table class="table">
            <thead>
                <tr>
                    <th>Data</th>
                    <th>Tipo</th>
                    <th>Importo</th>
                    <th>Descrizione</th>
                </tr>
            </thead>
            <tbody>
                ${transactions
                  .map(
                    (tx) => `
                    <tr>
                        <td>${new Date(tx.date).toLocaleDateString()}</td>
                        <td>${tx.type === "INCOME" ? "Entrata" : "Spesa"}</td>
                        <td class="${
                          tx.type === "INCOME" ? "text-success" : "text-danger"
                        }">
                            €${tx.amount.toFixed(2)}
                        </td>
                        <td>${tx.description}</td>
                    </tr>
                `
                  )
                  .join("")}
            </tbody>
        </table>
    `;
}

function updateMonthlyChart(transactions) {
  const ctx = document.getElementById("monthlyChart").getContext("2d");

  // Raggruppa le transazioni per mese
  const monthlyData = transactions.reduce((acc, tx) => {
    const date = new Date(tx.date);
    const monthYear = `${date.getMonth() + 1}/${date.getFullYear()}`;

    if (!acc[monthYear]) {
      acc[monthYear] = { income: 0, expense: 0 };
    }

    if (tx.type === "INCOME") {
      acc[monthYear].income += tx.amount;
    } else {
      acc[monthYear].expense += tx.amount;
    }

    return acc;
  }, {});

  new Chart(ctx, {
    type: "bar",
    data: {
      labels: Object.keys(monthlyData),
      datasets: [
        {
          label: "Entrate",
          data: Object.values(monthlyData).map((d) => d.income),
          backgroundColor: "rgba(75, 192, 192, 0.2)",
          borderColor: "rgba(75, 192, 192, 1)",
          borderWidth: 1,
        },
        {
          label: "Spese",
          data: Object.values(monthlyData).map((d) => d.expense),
          backgroundColor: "rgba(255, 99, 132, 0.2)",
          borderColor: "rgba(255, 99, 132, 1)",
          borderWidth: 1,
        },
      ],
    },
    options: {
      responsive: true,
      scales: {
        y: {
          beginAtZero: true,
        },
      },
    },
  });
}

async function checkAuthentication() {
  try {
    const response = await fetch("/checkSession", {
      method: "GET",
      credentials: "include",
    });
    // Instead of throwing error, check if unauthorized
    if (response.status === 401) {
      return false;
    }
    // Assume 200 OK
    const data = await response.json();
    console.log("Auth check response:", data);
    return Boolean(data.authenticated);
  } catch (error) {
    console.error("Auth check error:", error);
    return false;
  }
}

async function initializePage() {
  const isAuthenticated = await checkAuthentication();
  if (!isAuthenticated) {
    window.location.replace("/registration.html");
    return;
  }
  document.getElementById("mainContent").style.display = "block";
  await loadFinancialData();
  await loadTransactionHistory();
}

// Document ready handler
document.addEventListener("DOMContentLoaded", async () => {
  if (await SessionManager.initializePage("mainContent")) {
    await loadFinancialData();
    await loadTransactionHistory();
  }
  SessionManager.setupEventListeners();
});

function setupEventListeners() {
  // Theme handling
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

  // Transaction form handling
  document
    .getElementById("addTransactionForm")
    .addEventListener("submit", handleTransactionSubmit);
}

async function handleTransactionSubmit(e) {
  e.preventDefault();

  try {
    const response = await fetch("/cashflow/add", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify({
        type: document.getElementById("transactionType").value,
        amount: parseFloat(document.getElementById("amount").value),
        description: document.getElementById("description").value,
      }),
      credentials: "include",
    });

    if (!response.ok) {
      throw new Error("Failed to add transaction");
    }

    await loadFinancialData();
    await loadTransactionHistory();
    e.target.reset();
  } catch (error) {
    console.error("Error adding transaction:", error);
    alert("Errore nell'aggiunta della transazione");
  }
}
