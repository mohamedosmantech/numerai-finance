// React not needed with new JSX transform
import { createRoot } from 'react-dom/client';
import './styles.css';

interface CompoundData {
  input: {
    principal: number;
    annualRate: number;
    years: number;
    compoundingFrequency: number;
    monthlyContribution: number;
  };
  result: {
    futureValue: number;
    totalContributions: number;
    totalInterestEarned: number;
    effectiveAnnualRate: number;
  };
}

function CompoundInterest({ data }: { data: CompoundData }) {
  const { input, result } = data;

  const formatCurrency = (value: number) =>
    new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD' }).format(value);

  const contributionPercent = (result.totalContributions / result.futureValue) * 100;
  const interestPercent = (result.totalInterestEarned / result.futureValue) * 100;

  // Generate growth data
  const growthData = [];
  for (let year = 0; year <= input.years; year++) {
    const rate = input.annualRate / 100;
    const n = input.compoundingFrequency;
    const principalFV = input.principal * Math.pow(1 + rate / n, n * year);

    let contributionFV = 0;
    if (input.monthlyContribution > 0 && year > 0) {
      const monthlyRate = rate / 12;
      const totalMonths = year * 12;
      contributionFV = input.monthlyContribution * ((Math.pow(1 + monthlyRate, totalMonths) - 1) / monthlyRate);
    }

    growthData.push({ year, value: principalFV + contributionFV });
  }

  const maxValue = Math.max(...growthData.map(d => d.value));

  return (
    <div className="fincalc-widget compound-interest">
      <div className="header">
        <div className="icon">📈</div>
        <h2>Investment Growth Calculator</h2>
      </div>

      <div className="input-summary">
        <div className="input-item">
          <span className="label">Initial Investment</span>
          <span className="value">{formatCurrency(input.principal)}</span>
        </div>
        <div className="input-item">
          <span className="label">Annual Return</span>
          <span className="value">{input.annualRate}%</span>
        </div>
        <div className="input-item">
          <span className="label">Time Period</span>
          <span className="value">{input.years} years</span>
        </div>
        {input.monthlyContribution > 0 && (
          <div className="input-item">
            <span className="label">Monthly Contribution</span>
            <span className="value">{formatCurrency(input.monthlyContribution)}</span>
          </div>
        )}
      </div>

      <div className="result-highlight">
        <div className="label">Future Value</div>
        <div className="amount">{formatCurrency(result.futureValue)}</div>
      </div>

      <div className="growth-chart">
        <h3>Growth Over Time</h3>
        <div className="chart">
          {growthData.map((point, index) => (
            <div
              key={point.year}
              className="chart-bar"
              style={{ height: `${(point.value / maxValue) * 100}%` }}
              title={`Year ${point.year}: ${formatCurrency(point.value)}`}
            >
              {index === growthData.length - 1 && (
                <span className="chart-label">{formatCurrency(point.value)}</span>
              )}
            </div>
          ))}
        </div>
        <div className="chart-axis">
          <span>Year 0</span>
          <span>Year {input.years}</span>
        </div>
      </div>

      <div className="breakdown">
        <h3>Value Breakdown</h3>
        <div className="bar-chart">
          <div className="bar contributions" style={{ width: `${contributionPercent}%` }} />
          <div className="bar earnings" style={{ width: `${interestPercent}%` }} />
        </div>
        <div className="legend">
          <div className="legend-item">
            <span className="dot contributions"></span>
            <span>Contributions: {formatCurrency(result.totalContributions)}</span>
          </div>
          <div className="legend-item">
            <span className="dot earnings"></span>
            <span>Interest Earned: {formatCurrency(result.totalInterestEarned)}</span>
          </div>
        </div>
      </div>

      <div className="totals">
        <div className="total-item">
          <span className="label">Effective Annual Rate</span>
          <span className="value">{result.effectiveAnnualRate}%</span>
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
      input: { principal: 10000, annualRate: 7, years: 20, compoundingFrequency: 12, monthlyContribution: 500 },
      result: { futureValue: 282195.85, totalContributions: 130000, totalInterestEarned: 152195.85, effectiveAnnualRate: 7.23 }
    };
    createRoot(container).render(<CompoundInterest data={data} />);
  }
}

if (document.readyState === 'loading') {
  document.addEventListener('DOMContentLoaded', mount);
} else {
  mount();
}

export default CompoundInterest;
