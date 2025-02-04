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
    document.getElementById(
      "cashBalance"
    ).textContent = `€${data.cashBalance.toFixed(2)}`;
    document.getElementById(
      "investedCapital"
    ).textContent = `€${data.investedCapital.toFixed(2)}`;
    document.getElementById(
      "totalCapital"
    ).textContent = `€${data.totalCapital.toFixed(2)}`;
    document.getElementById(
      "lastUpdate"
    ).textContent = `Ultimo aggiornamento: ${new Date(
      data.lastUpdate
    ).toLocaleString()}`;
  } catch (error) {
    console.error("Error loading financial data:", error);
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

// Inizializza la pagina quando il documento è caricato
document.addEventListener("DOMContentLoaded", () => {
  document
    .getElementById("addTransactionForm")
    .addEventListener("submit", async (e) => {
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

        // Ricarica i dati dopo l'aggiunta
        await loadFinancialData();
        await loadTransactionHistory();

        // Reset form
        e.target.reset();
      } catch (error) {
        console.error("Error adding transaction:", error);
        alert("Errore nell'aggiunta della transazione");
      }
    });
});
