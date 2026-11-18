import {useEffect, useState} from 'react';
import {NavLink, Routes, Route, Navigate} from 'react-router-dom';
import {
    LayoutDashboard,
    FileText,
    Users,
    Wallet,
    BarChart3,
    RefreshCw,
    ShieldCheck,
    Plus,
    CheckCircle2,
    Activity
} from 'lucide-react';
import {getJson, postJson} from './api';

type Role = 'SUPPLIER' | 'BUYER' | 'FINANCE' | 'ADMIN';
type Invoice = {
    invoiceId: string;
    supplierId: string;
    buyerId: string;
    invoiceNumber: string;
    currency: string;
    amount: number;
    status: string;
    dueDate: string
};
type Summary = {
    eventCount: number;
    invoiceCreated: number;
    invoiceAccepted: number;
    riskCompleted: number;
    financingApproved: number;
    paymentsDisbursed: number;
    reconciliationsCompleted: number;
    recentEvents: { eventId: string; eventType: string; aggregateId: string; occurredAt: string }[]
};
const money = (v: number, c = 'INR') => new Intl.NumberFormat('en-IN', {
    style: 'currency',
    currency: c,
    maximumFractionDigits: 0
}).format(v || 0);

function App() {
    const [role, setRole] = useState<Role>('SUPPLIER');
    const [refresh, setRefresh] = useState(0);
    const [notice, setNotice] = useState('');
    const [loading, setLoading] = useState(false);
    const [summary, setSummary] = useState<Summary | null>(null);
    const [invoices, setInvoices] = useState<Invoice[]>([]);
    const [parties, setParties] = useState<any[]>([]);
    const reload = () => {
        setRefresh(x => x + 1);
        setNotice('Refreshing live service data…')
    };
    useEffect(() => {
        getJson<Invoice[]>('/invoices').then(setInvoices).catch(() => {
        });
        getJson<Summary>('/dashboard/summary').then(setSummary).catch(() => {
        });
        getJson<any[]>('/parties').then(setParties).catch(() => {
        });
    }, [refresh]);
    const act = async (fn: () => Promise<any>, ok: string) => {
        setLoading(true);
        setNotice('');
        try {
            await fn();
            setNotice(ok);
            setRefresh(x => x + 1);
        } catch (e) {
            setNotice(e instanceof Error ? e.message : 'Action failed');
        } finally {
            setLoading(false);
        }
    };
    const nav = [{
        to: '/',
        label: 'Overview',
        icon: LayoutDashboard,
        roles: ['SUPPLIER', 'BUYER', 'FINANCE', 'ADMIN']
    }, {
        to: '/invoices',
        label: 'Invoices',
        icon: FileText,
        roles: ['SUPPLIER', 'BUYER', 'FINANCE', 'ADMIN']
    }, {to: '/parties', label: 'Parties', icon: Users, roles: ['ADMIN', 'FINANCE']}, {
        to: '/financing',
        label: 'Financing',
        icon: Wallet,
        roles: ['SUPPLIER', 'FINANCE', 'ADMIN']
    }, {to: '/analytics', label: 'Event analytics', icon: BarChart3, roles: ['FINANCE', 'ADMIN']}];
    const Sidebar = () => <aside>
        <div className="brand">
            <div className="brand-mark">IF</div>
            <div><b>InvoiceFlow</b><small>Receivables finance</small></div>
        </div>
        <div className="role"><label>DEMO WORKSPACE</label><select value={role}
                                                                   onChange={e => setRole(e.target.value as Role)}>
            <option>SUPPLIER</option>
            <option>BUYER</option>
            <option>FINANCE</option>
            <option>ADMIN</option>
        </select></div>
        <nav>{nav.filter(n => n.roles.includes(role)).map(n => <NavLink key={n.to} to={n.to} end={n.to === '/'}>
            <n.icon size={17}/>
            {n.label}</NavLink>)}</nav>
        <div className="side-foot"><ShieldCheck size={16}/> Demo-only access · not production auth</div>
    </aside>;
    const Header = () => <header><span>Invoice discounting platform</span>
        <div className="header-right">
            <button className="icon-btn" onClick={reload} title="Refresh"><RefreshCw size={16}/></button>
            <span className="user"><span className="avatar">{role[0]}</span>{role} USER</span></div>
    </header>;
    const Page = ({title, children}: { title: string; children: React.ReactNode }) => <section>
        <div className="page-head">
            <div><p className="eyebrow">INVOICEFLOW WORKSPACE</p><h1>{title}</h1></div>
            <span className="status">POC environment</span></div>
        {notice && <div className="notice">{notice}</div>}{children}</section>;
    const Dashboard = () => <Page title="Overview">
        <div className="metrics">
            <article><small>Invoices submitted</small><strong>{invoices.length}</strong><em>Live from Invoice
                Service</em></article>
            <article><small>Accepted invoices</small><strong>{summary?.invoiceAccepted ?? '—'}</strong><em>Buyer
                approval events</em></article>
            <article><small>Financing approvals</small><strong>{summary?.financingApproved ?? '—'}</strong><em>Risk-eligible
                invoices</em></article>
            <article><small>Simulated disbursements</small><strong>{summary?.paymentsDisbursed ?? '—'}</strong><em>No
                real money movement</em></article>
        </div>
        <div className="two-col">
            <div className="panel">
                <div className="panel-title"><h2>Invoice pipeline</h2><Activity size={18}/></div>
                <div
                    className="pipeline">{['Invoice created', 'Buyer accepted', 'Risk evaluated', 'Financing approved', 'Payment simulated', 'Reconciled'].map((s, i) =>
                    <div className="pipeline-step" key={s}><span>{i + 1}</span>{s}</div>)}</div>
            </div>
            <div className="panel"><h2>Event processing</h2>
                <div className="big-number">{summary?.eventCount ?? '—'}</div>
                <p className="muted">Events consumed by the read-model service from RabbitMQ.</p>
                <div className="event-stats">
                    <span>Risk checks <b>{summary?.riskCompleted ?? 0}</b></span><span>Reconciliations <b>{summary?.reconciliationsCompleted ?? 0}</b></span>
                </div>
            </div>
        </div>
        <div className="panel">
            <div className="panel-title"><h2>Recent invoices</h2><span className="muted">Latest service records</span>
            </div>
            <InvoiceTable rows={invoices.slice(0, 6)} onAccept={accept}/></div>
    </Page>;
    const accept = (id: string) => act(() => postJson(`/invoices/${id}/accept`), `Buyer acceptance submitted for ${id}. Downstream events process asynchronously.`);
    const InvoiceTable = ({rows, onAccept}: { rows: Invoice[]; onAccept: (id: string) => void }) => <div
        className="table-wrap">
        <table>
            <thead>
            <tr>
                <th>Invoice</th>
                <th>Supplier</th>
                <th>Buyer</th>
                <th>Amount</th>
                <th>Due date</th>
                <th>Status</th>
                <th>Action</th>
            </tr>
            </thead>
            <tbody>{rows.map(r => <tr key={r.invoiceId}>
                <td><b>{r.invoiceNumber}</b><small className="subid">{r.invoiceId}</small></td>
                <td>{r.supplierId}</td>
                <td>{r.buyerId}</td>
                <td>{money(Number(r.amount), r.currency)}</td>
                <td>{String(r.dueDate).slice(0, 10)}</td>
                <td><span className={`pill ${r.status.toLowerCase()}`}>{r.status}</span></td>
                <td>{r.status === 'SUBMITTED' && <button className="small-btn" disabled={loading || role === 'SUPPLIER'}
                                                         onClick={() => onAccept(r.invoiceId)}>Accept</button>}</td>
            </tr>)}</tbody>
        </table>
        {!rows.length && <p className="muted pad">No invoices yet. Submit one to begin the workflow.</p>}</div>;
    const InvoicePage = () => {
        const [supplier, setSupplier] = useState('SUP-001'), [buyer, setBuyer] = useState('BUY-001'), [num, setNum] = useState('INV-DEMO-'), [amount, setAmount] = useState('125000'), [due, setDue] = useState('2026-12-15');
        return <Page title="Invoice management">
            <div className="panel form-panel"><h2>Submit a demo invoice</h2>
                <div className="form-grid"><label>Supplier<select value={supplier}
                                                                  onChange={e => setSupplier(e.target.value)}>{parties.filter(p => p.partyType === 'SUPPLIER').map(p =>
                    <option key={p.partyId}
                            value={p.partyId}>{p.partyId} · {p.legalName}</option>)}</select></label><label>Buyer<select
                    value={buyer}
                    onChange={e => setBuyer(e.target.value)}>{parties.filter(p => p.partyType === 'BUYER').map(p =>
                    <option key={p.partyId}
                            value={p.partyId}>{p.partyId} · {p.legalName}</option>)}</select></label><label>Invoice
                    number<input value={num} onChange={e => setNum(e.target.value)}/></label><label>Amount (INR)<input
                    type="number" min="1" value={amount} onChange={e => setAmount(e.target.value)}/></label><label>Issue
                    date<input type="date" defaultValue="2026-11-01" id="issue-date"/></label><label>Due date<input
                    type="date" value={due} onChange={e => setDue(e.target.value)}/></label></div>
                <button disabled={loading} onClick={() => act(() => postJson('/invoices', {
                    supplierId: supplier,
                    buyerId: buyer,
                    invoiceNumber: num,
                    currency: 'INR',
                    amount: Number(amount),
                    issueDate: (document.getElementById('issue-date') as HTMLInputElement).value,
                    dueDate: due
                }), 'Invoice submitted. Its risk/financing/payment chain begins after buyer acceptance.')}><Plus
                    size={16}/> Submit invoice
                </button>
            </div>
            <div className="panel">
                <div className="panel-title"><h2>Invoice register</h2><span
                    className="muted">{invoices.length} records</span></div>
                <InvoiceTable rows={invoices} onAccept={accept}/></div>
        </Page>
    };
    const Parties = () => <Page title="Supplier & buyer directory">
        <div className="panel">
            <table>
                <thead>
                <tr>
                    <th>Party ID</th>
                    <th>Type</th>
                    <th>Legal name</th>
                    <th>Email</th>
                    <th>Country</th>
                    <th>Status</th>
                </tr>
                </thead>
                <tbody>{parties.map(p => <tr key={p.partyId}>
                    <td>{p.partyId}</td>
                    <td>{p.partyType}</td>
                    <td>{p.legalName}</td>
                    <td>{p.email}</td>
                    <td>{p.countryCode}</td>
                    <td><span className="pill accepted">{p.status}</span></td>
                </tr>)}</tbody>
            </table>
        </div>
    </Page>;
    const Financing = () => <Page title="Financing workflow">
        <div className="panel"><h2>Approval process</h2><p className="muted">Buyer acceptance triggers rule-based risk
            evaluation. Eligible invoices receive a POC financing approval equal to 90% of invoice value. The discount
            rate is a demo parameter, not a commercial offer.</p>
            <div
                className="pipeline">{['Accepted invoice', 'Risk rules (≤ ₹50 lakh; INR)', '90% advance calculation', 'Simulated payment'].map((s, i) =>
                <div className="pipeline-step" key={s}><span>{i + 1}</span>{s}</div>)}</div>
            <p className="muted">Approvals: {summary?.financingApproved ?? 0} · Simulated
                payments: {summary?.paymentsDisbursed ?? 0}</p></div>
    </Page>;
    const Analytics = () => <Page title="Event analytics">
        <div className="metrics six">
            <article><small>Total events</small><strong>{summary?.eventCount ?? 0}</strong></article>
            <article><small>Created</small><strong>{summary?.invoiceCreated ?? 0}</strong></article>
            <article><small>Accepted</small><strong>{summary?.invoiceAccepted ?? 0}</strong></article>
            <article><small>Risk checks</small><strong>{summary?.riskCompleted ?? 0}</strong></article>
            <article><small>Approvals</small><strong>{summary?.financingApproved ?? 0}</strong></article>
            <article><small>Reconciled</small><strong>{summary?.reconciliationsCompleted ?? 0}</strong></article>
        </div>
        <div className="panel">
            <div className="panel-title"><h2>Recent domain events</h2><span
                className="muted">RabbitMQ → Read Model</span></div>
            <table>
                <thead>
                <tr>
                    <th>Event</th>
                    <th>Aggregate</th>
                    <th>Received</th>
                </tr>
                </thead>
                <tbody>{(summary?.recentEvents ?? []).map(e => <tr key={e.eventId}>
                    <td><span className="event-type">{e.eventType}</span></td>
                    <td>{e.aggregateId}</td>
                    <td>{new Date(e.occurredAt).toLocaleString()}</td>
                </tr>)}</tbody>
            </table>
        </div>
    </Page>;
    return <div className="shell"><Sidebar/>
        <main><Header/><Routes><Route path="/" element={<Dashboard/>}/><Route path="/invoices"
                                                                              element={<InvoicePage/>}/><Route
            path="/parties" element={<Parties/>}/><Route path="/financing" element={<Financing/>}/><Route
            path="/analytics" element={<Analytics/>}/><Route path="*" element={<Navigate to="/" replace/>}/></Routes>
            <footer>InvoiceFlow POC · Synthetic data · Simulated payments only</footer>
        </main>
    </div>;
}

export default App;
