// React not needed with new JSX transform
import { createRoot } from 'react-dom/client';
import './styles.css';

interface TaxData {
  input: {
    grossIncome: number;
    filingStatus: string;
    deductions: number;
    state: string;
  };
  result: {
    federalTax: number;
    stateTax: number;
    totalTax: number;
    effectiveRate: number;
    takeHomePay: number;
    taxableIncome: number;
  };
}

function TaxEstimator({ data }: { data: TaxData }) {
  const { input, result } = data;

  const formatCurrency = (value: number) =>
    new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD' }).format(value);

  const filingStatusDisplay: Record<string, string> = {
    single: 'Single',
    married_joint: 'Married Filing Jointly',
    married_separate: 'Married Filing Separately',
    head_of_household: 'Head of Household'
  };

  const taxPercent = (result.totalTax / input.grossIncome) * 100;
  const takeHomePercent = (result.takeHomePay / input.grossIncome) * 100;

  return (
    <div className="fincalc-widget tax-estimator">
      <div className="header">
        <div className="icon">📊</div>
        <h2>Tax Estimator (2024)</h2>
      </div>

      <div className="input-summary">
        <div className="input-item">
          <span className="label">Gross Income</span>
          <span className="value">{formatCurrency(input.grossIncome)}</span>
        </div>
        <div className="input-item">
          <span className="label">Filing Status</span>
          <span className="value">{filingStatusDisplay[input.filingStatus] || input.filingStatus}</span>
        </div>
        <div className="input-item">
          <span className="label">Deductions</span>
          <span className="value">{formatCurrency(input.deductions)}</span>
        </div>
        {input.state && (
          <div className="input-item">
            <span className="label">State</span>
            <span className="value">{input.state.toUpperCase()}</span>
          </div>
        )}
      </div>

      <div className="result-highlight take-home">
        <div className="label">Take-Home Pay</div>
        <div className="amount">{formatCurrency(result.takeHomePay)}</div>
      </div>

      <div className="breakdown">
        <h3>Income Breakdown</h3>
        <div className="bar-chart">
          <div className="bar contributions" style={{ width: `${takeHomePercent}%` }} />
          <div className="bar earnings" style={{ width: `${taxPercent}%` }} />
        </div>
        <div className="legend">
          <div className="legend-item">
            <span className="dot contributions"></span>
            <span>Take Home: {formatCurrency(result.takeHomePay)}</span>
          </div>
          <div className="legend-item">
            <span className="dot earnings"></span>
            <span>Total Tax: {formatCurrency(result.totalTax)}</span>
          </div>
        </div>
      </div>

      <div className="totals">
        <div className="total-item">
          <span className="label">Taxable Income</span>
          <span className="value">{formatCurrency(result.taxableIncome)}</span>
        </div>
        <div className="total-item">
          <span className="label">Federal Tax</span>
          <span className="value">{formatCurrency(result.federalTax)}</span>
        </div>
        {result.stateTax > 0 && (
          <div className="total-item">
            <span className="label">State Tax ({input.state?.toUpperCase()})</span>
            <span className="value">{formatCurrency(result.stateTax)}</span>
          </div>
        )}
        <div className="total-item">
          <span className="label">Effective Tax Rate</span>
          <span className="value">{result.effectiveRate}%</span>
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
      input: { grossIncome: 150000, filingStatus: 'single', deductions: 14600, state: 'CA' },
      result: { federalTax: 26798, stateTax: 12587.70, totalTax: 39385.70, effectiveRate: 26.26, takeHomePay: 110614.30, taxableIncome: 135400 }
    };
    createRoot(container).render(<TaxEstimator data={data} />);
  }
}

if (document.readyState === 'loading') {
  document.addEventListener('DOMContentLoaded', mount);
} else {
  mount();
}

export default TaxEstimator;
