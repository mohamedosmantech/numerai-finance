// React not needed with new JSX transform
import { createRoot } from 'react-dom/client';
import './styles.css';

interface LoanData {
  input: {
    principal: number;
    annualRate: number;
    years: number;
  };
  result: {
    monthlyPayment: number;
    totalPayment: number;
    totalInterest: number;
  };
}

function LoanCalculator({ data }: { data: LoanData }) {
  const { input, result } = data;

  const formatCurrency = (value: number) =>
    new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD' }).format(value);

  const principalPercent = (input.principal / result.totalPayment) * 100;
  const interestPercent = (result.totalInterest / result.totalPayment) * 100;

  return (
    <div className="fincalc-widget loan-calculator">
      <div className="header">
        <div className="icon">🏠</div>
        <h2>Loan Payment Calculator</h2>
      </div>

      <div className="input-summary">
        <div className="input-item">
          <span className="label">Loan Amount</span>
          <span className="value">{formatCurrency(input.principal)}</span>
        </div>
        <div className="input-item">
          <span className="label">Interest Rate</span>
          <span className="value">{input.annualRate}% APR</span>
        </div>
        <div className="input-item">
          <span className="label">Loan Term</span>
          <span className="value">{input.years} years</span>
        </div>
      </div>

      <div className="result-highlight">
        <div className="label">Monthly Payment</div>
        <div className="amount">{formatCurrency(result.monthlyPayment)}</div>
      </div>

      <div className="breakdown">
        <h3>Payment Breakdown</h3>
        <div className="bar-chart">
          <div className="bar principal" style={{ width: `${principalPercent}%` }} />
          <div className="bar interest" style={{ width: `${interestPercent}%` }} />
        </div>
        <div className="legend">
          <div className="legend-item">
            <span className="dot principal"></span>
            <span>Principal: {formatCurrency(input.principal)}</span>
          </div>
          <div className="legend-item">
            <span className="dot interest"></span>
            <span>Interest: {formatCurrency(result.totalInterest)}</span>
          </div>
        </div>
      </div>

      <div className="totals">
        <div className="total-item">
          <span className="label">Total of {input.years * 12} Payments</span>
          <span className="value">{formatCurrency(result.totalPayment)}</span>
        </div>
      </div>
    </div>
  );
}

// Mount the widget when DOM is ready
function mount() {
  const container = document.getElementById('root');
  if (container) {
    const data = (window as any).__WIDGET_DATA__ || {
      input: { principal: 300000, annualRate: 6.5, years: 30 },
      result: { monthlyPayment: 1896.20, totalPayment: 682632, totalInterest: 382632 }
    };
    createRoot(container).render(<LoanCalculator data={data} />);
  }
}

if (document.readyState === 'loading') {
  document.addEventListener('DOMContentLoaded', mount);
} else {
  mount();
}

export default LoanCalculator;
