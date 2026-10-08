import { useState } from 'react'
import './App.css'

const API = 'http://localhost:8090'

const SAMPLES = [
  'INC-10101 Order ORD-68D80E09 stuck in PAYMENT_PENDING although payment was recorded.',
  'INC-10102 eSIM provisioning PRV-D7AC40C1 FAILED for order 2.',
  'INC-10106 Order ORD-8495EEAF FAILED at validation: customer CUS-DEMO003 SUSPENDED.',
  'INC-10103 reservation RSV-22B3887F stuck ACTIVE for order 9999; MSISDN RES-B2979633 blocked RESERVED.',
]

const TICKETS = [
  { id: 'INC-10101', title: 'Stuck payment', text: SAMPLES[0] },
  { id: 'INC-10102', title: 'eSIM failure', text: SAMPLES[1] },
  { id: 'INC-10103', title: 'Leaked hold', text: SAMPLES[3] },
  { id: 'INC-10104', title: 'SMS never delivered', text: 'INC-10104 ORDER_COMPLETED SMS for order 2 never delivered although the order completed.' },
  { id: 'INC-10105', title: 'Duplicate SMS', text: 'INC-10105 Duplicate ORDER_COMPLETED SMS for order 2 within a minute.' },
  { id: 'INC-10106', title: 'Suspended customer', text: SAMPLES[2] },
  { id: 'INC-10107', title: 'Stuck in RETRYING', text: 'INC-10107 Order ORD-475DC248 stuck in RETRYING after a failed payment, never revalidated.' },
  { id: 'INC-10108', title: 'N+1 slowness', text: 'INC-10108 GET /api/orders is slow: N+1 selects on order_item, one per order in the page.' },
  { id: 'INC-10109', title: 'Promo vanished', text: 'INC-10109 Promo FESTIVE10 vanished from order ORD-038A154B after modify; payable jumped.' },
  { id: 'INC-10110', title: 'Expired hold leak', text: 'INC-10110 reservation RSV-C5396D47 can never confirm after TTL; SIM stuck RESERVED.' },
  { id: 'INC-10111', title: 'Bulk partial failure', text: 'INC-10111 Bulk corporate order partially failed: 2 created, 1 item rejected (customerId null).' },
  { id: 'INC-10112', title: 'Fiber unlinked', text: 'INC-10112 Broadband activation FAILED (MISSING_RESOURCE) although fiber port is reserved for order 7777.' },
  { id: 'INC-10113', title: 'Number already taken', text: 'INC-10113 MSISDN 919000007771 refused for customer 2: already in use by customer 1.' },
  { id: 'INC-10114', title: 'Expired promo', text: 'INC-10114 Promo EXPIRED5 rejected for order 76: outside validity window, flyer still circulating.' },
]

function Pill({ tone, children }) {
  return <span className={`pill ${tone}`}>{children}</span>
}

function toneFor(status) {
  const s = String(status || '').toUpperCase()
  if (['ACTIVE', 'COMPLETED', 'SENT', 'SUCCESS', 'VALIDATED', 'CONFIRMED'].includes(s)) return 'green'
  if (s.includes('FAIL') || ['BLOCKED', 'SUSPENDED'].includes(s)) return 'red'
  if (['PENDING', 'VALIDATING', 'IN_PROGRESS', 'RETRYING', 'RESERVED'].includes(s)) return 'amber'
  return 'navy'
}

export default function App() {
  const [text, setText] = useState(SAMPLES[0])
  const [result, setResult] = useState(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [useLlm, setUseLlm] = useState(() => localStorage.getItem('ai-mode') !== 'rules')

  async function investigate() {
    setLoading(true)
    setError('')
    setResult(null)
    localStorage.setItem('ai-mode', useLlm ? 'llm' : 'rules')
    try {
      const res = await fetch(`${API}/api/investigate`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ incident_text: text, mode: useLlm ? 'llm' : 'rules' }),
      })
      if (!res.ok) throw new Error(`backend ${res.status}`)
      setResult(await res.json())
    } catch (e) {
      setError(`Investigator backend unreachable — is it running on :8090? (${e.message})`)
    } finally {
      setLoading(false)
    }
  }

  return (
    <main className="page">
      <header className="hero">
        <p className="eyebrow">AI Production Incident Investigator · Phase 14</p>
        <h1>Paste the ticket. Get the trace.</h1>
        <p>Pastes a Jira-style incident, extracts every ID, searches the five service logs, correlates the request, checks the databases (read-only) and names the suspect service.</p>
      </header>

      <section className="card">
        <h2>Incident</h2>
        <textarea value={text} onChange={(e) => setText(e.target.value)} rows={3} placeholder="INC-10101 Order ORD-… stuck…" />
        <div className="btn-row mode-row">
          <button className={`mode-toggle ${useLlm ? 'on' : 'off'}`} onClick={() => setUseLlm(!useLlm)} title="ON = LLM reasoning, OFF = rule-based only">
            <span className="knob" />
            <span className="mode-label">{useLlm ? 'LLM ON' : 'LLM OFF · rules'}</span>
          </button>
          <button className="btn" onClick={investigate} disabled={loading || !text.trim()}>
            {loading ? 'Investigating…' : 'Investigate'}
          </button>
        </div>
        <div className="btn-row">
          {SAMPLES.map((s, i) => (
            <button key={i} className="btn small ghost" onClick={() => setText(s)}>Sample {i + 1}</button>
          ))}
        </div>
        {error && <div className="alert error">{error}</div>}
      </section>

      <section className="card">
        <h2>Incident library — all {TICKETS.length} tickets (click to load)</h2>
        <div className="ticket-grid">
          {TICKETS.map((t) => (
            <button key={t.id} className="ticket" onClick={() => { setText(t.text); setResult(null); window.scrollTo({ top: 0, behavior: 'smooth' }) }} title={t.text}>
              <strong>{t.id}</strong><span>{t.title}</span>
            </button>
          ))}
        </div>
      </section>

      {result && (
        <>
          <section className="card verdict">
            <h2>Verdict</h2>
            <div className="verdict-row">
              <div>
                <div className="muted">Fix type</div>
                <div className="fix-badge">{result.llm?.enabled ? result.llm.analysis.fix_type : result.fix_hint}</div>
              </div>
              <div>
                <div className="muted">Suspect service</div>
                <div className="fix-badge dim">{result.suspect.service || 'unknown'}</div>
              </div>
              <div>
                <div className="muted">Confidence</div>
                <div className="fix-badge dim">{result.llm?.enabled ? result.llm.analysis.confidence : result.suspect.confidence}</div>
              </div>
            </div>
          </section>

          <section className="card">
            <h2>Hypothesis</h2>
            <p><strong>{result.hypothesis}</strong></p>
            <p className="muted">
              Suspect: <Pill tone={toneFor(result.suspect.confidence === 'high' ? 'FAILED' : 'PENDING')}>{result.suspect.service || 'unknown'}</Pill>
              {' '}· Signal: <code>{result.suspect.signal}</code>
              {' '}· Confidence: {result.suspect.confidence}
              {' '}· {result.log_stats.lines} log lines, {result.log_stats.direct_hits} direct hits
              {' '}· Correlations: {result.correlations.join(', ') || 'none'}
            </p>
            <p className="muted">{result.safety}</p>
            <h2>Where the logs were read from</h2>
            <p className="muted">Local rolling files on this machine (Phase 12) — no SSH, no remote pull:</p>
            <ul className="logsrc">
              {(result.log_sources?.files || []).map((f) => <li key={f}><code>{f}</code></li>)}
            </ul>
          </section>

          <section className="card">
            <h2>LLM analysis {result.llm?.enabled ? <span className="pill green">{result.llm.model}</span> : <span className="pill">rule-based mode</span>}</h2>
            {!result.llm?.enabled && <p className="muted">{result.llm?.reason} Add LLM_API_KEY to ai-investigator/backend/.env to enable.</p>}
            {result.llm?.enabled && result.llm.analysis && (
              <>
                <p><strong>{result.llm.analysis.summary}</strong></p>
                <p>Root cause: <code>{result.llm.analysis.root_cause}</code> · Confidence: {result.llm.analysis.confidence} · Fix type: {result.llm.analysis.fix_type}</p>
                <h3>Evidence chain</h3>
                <ul>{(result.llm.analysis.evidence_refs || []).map((e, i) => <li key={i}>{e}</li>)}</ul>
                {result.llm.analysis.data_fix_sql && (
                  <>
                    <h3>Proposed data fix (needs human approval)</h3>
                    <pre>{result.llm.analysis.data_fix_sql}</pre>
                    <p className="muted">Validate: <code>{result.llm.analysis.data_fix_validation}</code><br />Rollback: {result.llm.analysis.data_fix_rollback}</p>
                  </>
                )}
                {result.llm.analysis.code_fix && (
                  <>
                    <h3>Proposed code fix — {result.llm.analysis.code_fix.service} / {result.llm.analysis.code_fix.area}</h3>
                    <p className="muted">Before:</p>
                    <pre>{result.llm.analysis.code_fix.before}</pre>
                    <p className="muted">After:</p>
                    <pre>{result.llm.analysis.code_fix.after}</pre>
                  </>
                )}
                <h3>Next steps</h3>
                <ul>{(result.llm.analysis.next_steps || []).map((s, i) => <li key={i}>{s}</li>)}</ul>
              </>
            )}
          </section>

          <div className="grid two">
            <section className="card">
              <h2>Extracted entities</h2>
              {Object.entries(result.entities).map(([k, v]) => (
                <p key={k}><code>{k}</code>: {v.join(', ')}</p>
              ))}
              <h2>Database evidence (SELECT only)</h2>
              {Object.entries(result.db_evidence).map(([k, rows]) => (
                <div key={k}>
                  <h3>{k}</h3>
                  <pre>{JSON.stringify(rows, null, 1).slice(0, 1200)}</pre>
                </div>
              ))}
            </section>
            <section className="card">
              <h2>Service journey</h2>
              <ul className="timeline">
                {result.journey.map((j, i) => (
                  <li key={i}><strong>{j.service}</strong> · {j.event} <Pill tone={toneFor(j.status)}>{j.status}</Pill><br /><span className="t">{j.time}</span></li>
                ))}
              </ul>
            </section>
          </div>

          <section className="card">
            <h2>Relevant log lines ({result.log_lines.length})</h2>
            <pre className="logs">{result.log_lines.map((l) => `[${l.service}] ${l.line}`).join('\n')}</pre>
          </section>
        </>
      )}
    </main>
  )
}
