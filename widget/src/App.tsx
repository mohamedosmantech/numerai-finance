import { useState, useCallback } from 'react';
import { createRoot } from 'react-dom/client';
import './styles.css';

// Types
interface LoanInput {
  principal: number;
  annualRate: number;
  years: number;
}

interface LoanResult {
  monthlyPayment: number;
  totalPayment: number;
  totalInterest: number;
}

interface CompoundInput {
  principal: number;
  annualRate: number;
  years: number;
  compoundingFrequency: number;
  monthlyContribution: number;
}

interface CompoundResult {
  futureValue: number;
  totalContributions: number;
  totalInterestEarned: number;
  effectiveAnnualRate: number;
}

interface TaxInput {
  grossIncome: number;
  filingStatus: string;
  deductions: number;
  state: string;
}

interface TaxResult {
  federalTax: number;
  stateTax: number;
  totalTax: number;
  effectiveRate: number;
  takeHomePay: number;
  taxableIncome: number;
}

// API helpers
const API_BASE = 'http://localhost:8002';

async function callTool<T>(sessionId: string, toolName: string, args: object): Promise<T> {
  const response = await fetch(`${API_BASE}/mcp/messages?sessionId=${sessionId}`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      jsonrpc: '2.0',
      id: Date.now(),
      method: 'tools/call',
      params: { name: toolName, arguments: args }
    })
  });

  const data = await response.json();
  if (data.error) throw new Error(data.error.message);
  return data.result.structuredContent as T;
}

async function createSession(): Promise<string> {
  return new Promise((resolve, reject) => {
    const eventSource = new EventSource(`${API_BASE}/mcp`);
    eventSource.addEventListener('endpoint', (event) => {
      const data = event.data;
      const sessionId = new URLSearchParams(data.split('?')[1]).get('sessionId');
      if (sessionId) {
        resolve(sessionId);
      }
    });
    eventSource.onerror = () => reject(new Error('Failed to connect'));
    setTimeout(() => reject(new Error('Connection timeout')), 5000);
  });
}

// Format helpers
const formatCurrency = (value: number) =>
  new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD' }).format(value);

// Components
function LoanCalculatorWidget() {
  const [input, setInput] = useState<LoanInput>({ principal: 300000, annualRate: 6.5, years: 30 });
  const [result, setResult] = useState<LoanResult | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const calculate = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const sessionId = await createSession();
      const data = await callTool<{ input: LoanInput; result: LoanResult }>(
        sessionId, 'calculate_loan_payment', input
      );
      setResult(data.result);
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Calculation failed');
    } finally {
      setLoading(false);
    }
  }, [input]);

  const principalPercent = result ? (input.principal / result.totalPayment) * 100 : 50;
  const interestPercent = result ? (result.totalInterest / result.totalPayment) * 100 : 50;

  return (
    <div className="fincalc-widget loan-calculator">
      <div className="header">
        <div className="icon">🏠</div>
        <h2>Loan Payment Calculator</h2>
      </div>

      <div className="input-form">
        <div className="form-group">
          <label>Loan Amount ($)</label>
          <input
            type="number"
            value={input.principal}
            onChange={(e) => setInput({ ...input, principal: Number(e.target.value) })}
            min={1000}
            max={10000000}
          />
        </div>
        <div className="form-group">
          <label>Interest Rate (%)</label>
          <input
            type="number"
            value={input.annualRate}
            onChange={(e) => setInput({ ...input, annualRate: Number(e.target.value) })}
            min={0}
            max={30}
            step={0.125}
          />
        </div>
        <div className="form-group">
          <label>Term (years)</label>
          <input
            type="number"
            value={input.years}
            onChange={(e) => setInput({ ...input, years: Number(e.target.value) })}
            min={1}
            max={50}
          />
        </div>
        <button onClick={calculate} disabled={loading} className="calculate-btn">
          {loading ? 'Calculating...' : 'Calculate'}
        </button>
      </div>

      {error && <div className="error-message">{error}</div>}

      {result && (
        <>
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
        </>
      )}
    </div>
  );
}

function CompoundInterestWidget() {
  const [input, setInput] = useState<CompoundInput>({
    principal: 10000,
    annualRate: 7,
    years: 20,
    compoundingFrequency: 12,
    monthlyContribution: 500
  });
  const [result, setResult] = useState<CompoundResult | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const calculate = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const sessionId = await createSession();
      const data = await callTool<{ input: CompoundInput; result: CompoundResult }>(
        sessionId, 'calculate_compound_interest', input
      );
      setResult(data.result);
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Calculation failed');
    } finally {
      setLoading(false);
    }
  }, [input]);

  const contributionPercent = result ? (result.totalContributions / result.futureValue) * 100 : 50;
  const interestPercent = result ? (result.totalInterestEarned / result.futureValue) * 100 : 50;

  return (
    <div className="fincalc-widget compound-interest">
      <div className="header">
        <div className="icon">📈</div>
        <h2>Investment Growth Calculator</h2>
      </div>

      <div className="input-form">
        <div className="form-group">
          <label>Initial Investment ($)</label>
          <input
            type="number"
            value={input.principal}
            onChange={(e) => setInput({ ...input, principal: Number(e.target.value) })}
            min={0}
          />
        </div>
        <div className="form-group">
          <label>Annual Return (%)</label>
          <input
            type="number"
            value={input.annualRate}
            onChange={(e) => setInput({ ...input, annualRate: Number(e.target.value) })}
            min={0}
            max={50}
            step={0.5}
          />
        </div>
        <div className="form-group">
          <label>Years</label>
          <input
            type="number"
            value={input.years}
            onChange={(e) => setInput({ ...input, years: Number(e.target.value) })}
            min={1}
            max={100}
          />
        </div>
        <div className="form-group">
          <label>Monthly Contribution ($)</label>
          <input
            type="number"
            value={input.monthlyContribution}
            onChange={(e) => setInput({ ...input, monthlyContribution: Number(e.target.value) })}
            min={0}
          />
        </div>
        <button onClick={calculate} disabled={loading} className="calculate-btn">
          {loading ? 'Calculating...' : 'Calculate'}
        </button>
      </div>

      {error && <div className="error-message">{error}</div>}

      {result && (
        <>
          <div className="result-highlight">
            <div className="label">Future Value</div>
            <div className="amount">{formatCurrency(result.futureValue)}</div>
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
        </>
      )}
    </div>
  );
}

function TaxEstimatorWidget() {
  const [input, setInput] = useState<TaxInput>({
    grossIncome: 100000,
    filingStatus: 'single',
    deductions: 0,
    state: 'CA'
  });
  const [result, setResult] = useState<TaxResult | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const calculate = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const sessionId = await createSession();
      const data = await callTool<{ input: TaxInput; result: TaxResult }>(
        sessionId, 'estimate_taxes', input
      );
      setResult(data.result);
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Calculation failed');
    } finally {
      setLoading(false);
    }
  }, [input]);

  const takeHomePercent = result ? (result.takeHomePay / input.grossIncome) * 100 : 75;
  const taxPercent = result ? (result.totalTax / input.grossIncome) * 100 : 25;

  return (
    <div className="fincalc-widget tax-estimator">
      <div className="header">
        <div className="icon">📊</div>
        <h2>Tax Estimator (2024)</h2>
      </div>

      <div className="input-form">
        <div className="form-group">
          <label>Gross Income ($)</label>
          <input
            type="number"
            value={input.grossIncome}
            onChange={(e) => setInput({ ...input, grossIncome: Number(e.target.value) })}
            min={0}
          />
        </div>
        <div className="form-group">
          <label>Filing Status</label>
          <select
            value={input.filingStatus}
            onChange={(e) => setInput({ ...input, filingStatus: e.target.value })}
          >
            <option value="single">Single</option>
            <option value="married_joint">Married Filing Jointly</option>
            <option value="married_separate">Married Filing Separately</option>
            <option value="head_of_household">Head of Household</option>
          </select>
        </div>
        <div className="form-group">
          <label>State</label>
          <select
            value={input.state}
            onChange={(e) => setInput({ ...input, state: e.target.value })}
          >
            <option value="CA">California</option>
            <option value="NY">New York</option>
            <option value="TX">Texas (No State Tax)</option>
            <option value="FL">Florida (No State Tax)</option>
            <option value="WA">Washington (No State Tax)</option>
            <option value="IL">Illinois</option>
            <option value="PA">Pennsylvania</option>
            <option value="GA">Georgia</option>
          </select>
        </div>
        <button onClick={calculate} disabled={loading} className="calculate-btn">
          {loading ? 'Calculating...' : 'Calculate'}
        </button>
      </div>

      {error && <div className="error-message">{error}</div>}

      {result && (
        <>
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
            <div className="total-item">
              <span className="label">State Tax</span>
              <span className="value">{formatCurrency(result.stateTax)}</span>
            </div>
            <div className="total-item">
              <span className="label">Effective Tax Rate</span>
              <span className="value">{result.effectiveRate}%</span>
            </div>
          </div>
        </>
      )}
    </div>
  );
}

// Main App
function App() {
  const [activeTab, setActiveTab] = useState<'loan' | 'compound' | 'tax'>('loan');

  return (
    <div className="app-container">
      <header className="app-header">
        <h1>💰 FinCalc Pro</h1>
        <p>Financial Calculator for ChatGPT</p>
      </header>

      <nav className="tab-nav">
        <button
          className={activeTab === 'loan' ? 'active' : ''}
          onClick={() => setActiveTab('loan')}
        >
          🏠 Loan
        </button>
        <button
          className={activeTab === 'compound' ? 'active' : ''}
          onClick={() => setActiveTab('compound')}
        >
          📈 Investment
        </button>
        <button
          className={activeTab === 'tax' ? 'active' : ''}
          onClick={() => setActiveTab('tax')}
        >
          📊 Taxes
        </button>
      </nav>

      <main>
        {activeTab === 'loan' && <LoanCalculatorWidget />}
        {activeTab === 'compound' && <CompoundInterestWidget />}
        {activeTab === 'tax' && <TaxEstimatorWidget />}
      </main>

      <footer className="app-footer">
        <p>MCP Server: <code>http://localhost:8002/mcp</code></p>
      </footer>
    </div>
  );
}

// Mount
const container = document.getElementById('root');
if (container) {
  createRoot(container).render(<App />);
}

export default App;
