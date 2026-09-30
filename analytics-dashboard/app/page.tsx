'use client'

import { useEffect, useMemo, useState } from 'react'

type View = 'Overview' | 'Market demand' | 'Salary insights' | 'Profiles' | 'Data sources' | 'Settings'
type TrendPoint = { label: string; value: number }

type Metric = { technology: string; averageSalary: number; jobCount: number }
type PeriodSnapshot = { periodType: string; periodStart: string; salaryByTechnology: Metric[]; topPaidTechnologies: Metric[]; technologyDemand: Metric[]; profileDemand: { backend: number; frontend: number; other: number } }
type DashboardSnapshot = { timestamp: string; periods: PeriodSnapshot[] }

const colors = ['#8b5cf6', '#38bdf8', '#f59e0b', '#34d399', '#f472b6']
function selectPeriod(snapshot: DashboardSnapshot | null, type: string) { return snapshot?.periods.find((item) => item.periodType === type) ?? snapshot?.periods[0] }
function totalOffers(snapshot: PeriodSnapshot | undefined) { return snapshot ? snapshot.profileDemand.backend + snapshot.profileDemand.frontend + snapshot.profileDemand.other : 0 }
function toDemand(snapshot: PeriodSnapshot | undefined) { return (snapshot?.technologyDemand ?? []).slice(0, 5).map((item, index) => ({ name: item.technology, value: item.jobCount, color: colors[index % colors.length] })) }
function toSalaries(snapshot: PeriodSnapshot | undefined) { return (snapshot?.topPaidTechnologies ?? []).map((item) => ({ name: item.technology, value: item.averageSalary })) }

function Icon({ name }: { name: string }) {
  const paths: Record<string, string> = { grid: 'M4 4h6v6H4zM14 4h6v6h-6zM4 14h6v6H4zM14 14h6v6h-6z', chart: 'M4 19V5m0 14h16M8 15l3-4 3 2 5-7', briefcase: 'M8 7V5a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2M4 7h16v13H4zM4 12h16', users: 'M16 20v-2a4 4 0 0 0-4-4H7a4 4 0 0 0-4 4v2M9.5 10a4 4 0 1 0 0-8 4 4 0 0 0 0 8ZM21 20v-2a4 4 0 0 0-3-3.87M16 2.13a4 4 0 0 1 0 7.75', dollar: 'M12 2v20M17 5.5A4 4 0 0 0 13 4h-2a4 4 0 0 0 0 8h2a4 4 0 0 1 0 8h-2a4 4 0 0 1-4-1.5' }
  return <svg viewBox="0 0 24 24" aria-hidden="true"><path d={paths[name]} /></svg>
}
function Sparkline() { return <svg className="sparkline" viewBox="0 0 110 34" preserveAspectRatio="none" aria-hidden="true"><path d="M0 28 L14 24 L27 26 L40 17 L54 20 L67 12 L81 15 L95 5 L110 8" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" /></svg> }
function EmptyState({ title, text }: { title: string; text: string }) { return <section className="panel empty-state"><span className="empty-icon"><Icon name="chart" /></span><h2>{title}</h2><p>{text}</p><button className="insight-button">Add data source <span>→</span></button></section> }

function Overview({ setView, snapshot }: { setView: (view: View) => void; snapshot: DashboardSnapshot | null }) {
  const [period, setPeriod] = useState('Weekly')
  const snapshots = snapshot?.periods ?? []
  const points: TrendPoint[] = snapshots.map((item) => ({ label: item.periodType, value: totalOffers(item) }))
  const selected = selectPeriod(snapshot, period.toUpperCase()) ?? snapshots[0]
  const demand = toDemand(selected)
  const salaries = toSalaries(selected)
  const fallbackPoints = points.length ? points : [{ label: 'Waiting', value: 0 }]
  const chartPoints = fallbackPoints
  const max = Math.max(...chartPoints.map((point) => point.value), 1)
  const polyline = chartPoints.map((point, index) => `${(index / Math.max(chartPoints.length - 1, 1)) * 100},${100 - (point.value / max) * 75 - 10}`).join(' ')
  const profile = selected?.profileDemand ?? { backend: 0, frontend: 0, other: 0 }
  const offerCount = totalOffers(selected)
  const averageSalary = salaries.length ? salaries.reduce((sum, item) => sum + item.value, 0) / salaries.length : 0
  return <div className="dashboard-content">
    <div className="section-heading"><div><h2>Market overview</h2><p>Latest snapshot from your analytics pipeline</p></div><div className="updated"><span className="pulse" /> {snapshot ? `Updated ${new Date(snapshot.timestamp).toLocaleTimeString()}` : 'Waiting for snapshot'}</div></div>
    <div className="metric-grid">{[['Total job offers', offerCount.toLocaleString(), 'live', 'briefcase', 'purple'], ['Average salary', averageSalary ? `$${Math.round(averageSalary).toLocaleString()}` : '—', 'live', 'dollar', 'blue'], ['Technologies tracked', (selected?.technologyDemand.length ?? 0).toString(), 'live', 'chart', 'orange'], ['Active profiles', offerCount.toLocaleString(), 'live', 'users', 'green']].map(([label, value, change, icon, color]) => <article className="metric-card" key={label}><div className="metric-top"><span>{label}</span><span className={`metric-icon ${color}`}><Icon name={icon} /></span></div><strong>{value}</strong><div className="metric-footer positive">● {change} <span>from analytics</span><Sparkline /></div></article>)}</div>
    <div className="section-heading chart-heading"><div><h2>Offer volume</h2><p>Job postings represented by the latest periods</p></div><div className="period-tabs">{['Daily', 'Weekly', 'Monthly'].map((item) => <button key={item} onClick={() => setPeriod(item)} className={period === item ? 'selected' : ''}>{item}</button>)}</div></div>
    <article className="panel volume-panel"><div className="chart-summary"><div><span className="chart-label">TOTAL OFFERS</span><strong>{(selected ? offerCount : 0).toLocaleString()}</strong><span className="positive trend-label">Live</span></div><span className="legend"><i /> Job offers</span></div><div className="line-chart"><div className="y-axis"><span>{Math.round(max / 1000)}k</span><span>{Math.round(max * .75 / 1000)}k</span><span>{Math.round(max * .5 / 1000)}k</span><span>0</span></div><div className="plot"><div className="grid-lines"><i /><i /><i /><i /><i /></div><svg viewBox="0 0 100 100" preserveAspectRatio="none"><polygon points={`0,100 ${polyline} 100,100`} fill="url(#fill)" /><polyline points={polyline} fill="none" stroke="#a78bfa" strokeWidth="2" vectorEffect="non-scaling-stroke" /></svg><div className="x-axis">{chartPoints.map((point) => <span key={point.label}>{point.label}</span>)}</div></div></div></article>
    <div className="two-col"><ListPanel title="Most demanded technologies" subtitle="Based on job offer volume" items={demand.map((item) => ({ ...item, value: item.value.toLocaleString() }))} onView={() => setView('Market demand')} /><ListPanel title="Salary by technology" subtitle="Average salary from available offers" items={salaries.map((item) => ({ name: item.name, value: `$${Math.round(item.value).toLocaleString()}` }))} onView={() => setView('Salary insights')} /></div>
    <div className="bottom-grid"><article className="panel profile-panel"><div className="panel-header"><div><h2>Profile distribution</h2><p>By role classification</p></div></div><div className="donut-wrap"><div className="donut"><div><strong>{offerCount.toLocaleString()}</strong><span>offers</span></div></div><div className="profile-legend"><span><i className="legend-backend" />Backend <b>{offerCount ? Math.round(profile.backend / offerCount * 100) : 0}%</b></span><span><i className="legend-frontend" />Frontend <b>{offerCount ? Math.round(profile.frontend / offerCount * 100) : 0}%</b></span><span><i className="legend-other" />Other <b>{offerCount ? Math.round(profile.other / offerCount * 100) : 0}%</b></span></div></div></article><article className="panel insight-panel"><div className="insight-kicker"><span className="spark">✦</span> PIPELINE INSIGHT</div><h2>{snapshot ? 'Analytics connected' : 'Waiting for analytics'}</h2><p>{snapshot ? 'This view is powered by the latest snapshot received from data-analysis.' : 'Start the pipeline to receive the first dashboard snapshot.'}</p><button className="insight-button" onClick={() => setView('Market demand')}>Explore market demand <span>→</span></button></article></div>
  </div>
}
function ListPanel({ title, subtitle, items, onView }: { title: string; subtitle: string; items: { name: string; value: string; color?: string }[]; onView: () => void }) { return <article className="panel"><div className="panel-header"><div><h2>{title}</h2><p>{subtitle}</p></div><button className="text-button" onClick={onView}>View all&nbsp; →</button></div><div className="bar-list">{items.map((item, index) => <div className="bar-row" key={item.name}><span className="rank">0{index + 1}</span><span className="bar-name">{item.name}</span><div className="bar-track"><span style={{ width: `${100 - index * 12}%`, background: item.color || '#8b5cf6' }} /></div><strong>{item.value}</strong></div>)}</div></article> }

function ProfilesScreen({ setNotice }: { setNotice: (message: string) => void }) {
  const [query, setQuery] = useState('')
  const profiles = [{ name: 'Maya Chen', role: 'Senior Frontend Engineer', skills: 'React · TypeScript', level: 'Senior' }, { name: 'Alex Morgan', role: 'Backend Engineer', skills: 'Python · Go', level: 'Mid' }, { name: 'Sam Rivera', role: 'Product Engineer', skills: 'Node.js · React', level: 'Senior' }, { name: 'Taylor Kim', role: 'Data Engineer', skills: 'Python · Scala', level: 'Mid' }]
  const filtered = profiles.filter((profile) => `${profile.name} ${profile.role} ${profile.skills}`.toLowerCase().includes(query.toLowerCase()))
  return <><div className="section-heading"><div><h2>Profiles</h2><p>Search and segment the talent pool represented in your pipeline.</p></div><button className="insight-button" onClick={() => setNotice('Profile import started')} >Import profiles <span>↑</span></button></div><section className="panel"><div className="panel-header"><div><h2>Talent directory</h2><p>{filtered.length} of {profiles.length} profiles shown</p></div><input className="search-input" aria-label="Search profiles" placeholder="Search profiles..." value={query} onChange={(event) => setQuery(event.target.value)} /></div><div className="profile-table">{filtered.map((profile) => <div className="profile-row" key={profile.name}><span className="avatar small">{profile.name.split(' ').map((part) => part[0]).join('')}</span><span><strong>{profile.name}</strong><small>{profile.role}</small></span><span className="profile-skills">{profile.skills}</span><span className="profile-level">{profile.level}</span><button className="text-button" onClick={() => setNotice(`${profile.name} profile selected`)}>View</button></div>)}{filtered.length === 0 && <p className="empty-copy">No profiles match your search.</p>}</div></section></>
}

function DataSourcesScreen({ setNotice }: { setNotice: (message: string) => void }) { const [sources, setSources] = useState(['Jobs API', 'Talent profiles CSV']); return <><div className="section-heading"><div><h2>Data sources</h2><p>Manage the feeds connected to your analytics pipeline.</p></div><button className="insight-button" onClick={() => { setSources((current) => [...current, `New source ${current.length + 1}`]); setNotice('New data source connected') }}>Add source <span>+</span></button></div><section className="panel source-list">{sources.map((source) => <div key={source}><span className="status-dot" /><strong>{source}</strong><small>Last synced 8 min ago</small><b>CONNECTED</b><button className="text-button" onClick={() => setNotice(`${source} sync started`)}>Sync now</button></div>)}</section></> }

function SettingsScreen({ setNotice }: { setNotice: (message: string) => void }) { const [alerts, setAlerts] = useState(true); const [weekly, setWeekly] = useState(false); const [workspace, setWorkspace] = useState('Analytics team'); const [timezone, setTimezone] = useState('UTC-5'); return <><div className="section-heading"><div><h2>Settings</h2><p>Configure your workspace preferences and notifications.</p></div><button className="insight-button" onClick={() => setNotice('Settings saved')}>Save changes <span>→</span></button></div><div className="settings-grid"><section className="panel settings-list"><div className="settings-section-title"><span className="settings-icon">⌘</span><div><h2>Workspace</h2><p>Keep your team details consistent across reports.</p></div></div><label><span><strong>Workspace name</strong><small>Shown in shared dashboards and exports.</small></span><input className="settings-input" value={workspace} onChange={(event) => setWorkspace(event.target.value)} /></label><label><span><strong>Report timezone</strong><small>Used for daily and weekly reporting windows.</small></span><select className="settings-select" value={timezone} onChange={(event) => setTimezone(event.target.value)}><option>UTC-5</option><option>UTC</option><option>UTC+1</option></select></label></section><section className="panel settings-list"><div className="settings-section-title"><span className="settings-icon">◌</span><div><h2>Notifications</h2><p>Choose when MarketScope should reach out.</p></div></div><label><span><strong>Market alerts</strong><small>Notify me when demand changes significantly.</small></span><input type="checkbox" checked={alerts} onChange={() => setAlerts(!alerts)} /></label><label><span><strong>Weekly report</strong><small>Receive a Monday summary by email.</small></span><input type="checkbox" checked={weekly} onChange={() => setWeekly(!weekly)} /></label></section></div></> }

export default function Page() {
  const [active, setActive] = useState<View>('Overview')
  const [notice, setNotice] = useState('')
  const [snapshot, setSnapshot] = useState<DashboardSnapshot | null>(null)
  const [apiError, setApiError] = useState('')
  const workspaceName = 'Analytics team'

  useEffect(() => {
    const apiUrl = process.env.NEXT_PUBLIC_DASHBOARD_API_URL || 'http://localhost:8082'
    fetch(`${apiUrl}/api/v1/dashboard`)
      .then((response) => {
        if (!response.ok) throw new Error(`Dashboard API returned ${response.status}`)
        return response.status === 204 ? null : response.json() as Promise<DashboardSnapshot>
      })
      .then((data) => setSnapshot(data))
      .catch(() => setApiError('Dashboard API is unavailable'))
  }, [])

  useEffect(() => {
    if (!notice) return
    const timeout = window.setTimeout(() => setNotice(''), 3600)
    return () => window.clearTimeout(timeout)
  }, [notice])

  const nav: { label: View; icon: string }[] = [{ label: 'Overview', icon: 'grid' }, { label: 'Market demand', icon: 'chart' }, { label: 'Salary insights', icon: 'dollar' }, { label: 'Profiles', icon: 'users' }]
  const setView = (view: View) => { setActive(view); setNotice('') }
  const title = active === 'Overview' ? 'Good morning, Jordan Velfort' : active
  const currentPeriod = selectPeriod(snapshot, 'DAILY')
  const demand = toDemand(currentPeriod)
  const salaries = toSalaries(currentPeriod)
  const content = useMemo(() => active === 'Overview' ? <Overview setView={setView} snapshot={snapshot} /> : active === 'Market demand' ? <><div className="section-heading"><div><h2>Market demand</h2><p>Explore the skills employers are looking for right now.</p></div></div><ListPanel title="Technology demand ranking" subtitle="Latest analytics snapshot" items={demand.map((item) => ({ ...item, value: item.value.toLocaleString() }))} onView={() => setView('Overview')} /></> : active === 'Salary insights' ? <><div className="section-heading"><div><h2>Salary insights</h2><p>Compare annual compensation across technology roles.</p></div></div><ListPanel title="Average salary by technology" subtitle="Based on available salary data" items={salaries.map((item) => ({ name: item.name, value: `$${Math.round(item.value).toLocaleString()}` }))} onView={() => setView('Overview')} /></> : active === 'Profiles' ? <ProfilesScreen setNotice={setNotice} /> : active === 'Data sources' ? <DataSourcesScreen setNotice={setNotice} /> : active === 'Settings' ? <SettingsScreen setNotice={setNotice} /> : <><div className="section-heading"><div><h2>Settings</h2><p>Configure your workspace preferences.</p></div></div><section className="panel settings-list"><label>Workspace name<input defaultValue="Analytics team" /></label><label>Report timezone<select defaultValue="UTC-5"><option>UTC-5</option><option>UTC</option><option>UTC+1</option></select></label><button className="insight-button" onClick={() => setNotice('Settings saved successfully')}>Save changes <span>→</span></button></section></>, [active, snapshot])
  return <main className="dashboard-shell"><aside className="sidebar"><div className="brand"><span className="brand-mark">◆</span><span>market<span className="brand-accent">scope</span></span></div><div className="workspace-label">WORKSPACE</div><div className="workspace-picker"><div className="workspace" aria-label="Current workspace"><span className="workspace-dot">A</span><span>{workspaceName}</span></div></div><nav aria-label="Main navigation">{nav.map((item) => <button key={item.label} className={`nav-item ${active === item.label ? 'active' : ''}`} onClick={() => setView(item.label)}><Icon name={item.icon} />{item.label}</button>)}</nav><div className="sidebar-bottom"><button className={`nav-item ${active === 'Data sources' ? 'active' : ''}`} onClick={() => setView('Data sources')}><span className="status-dot" />Data sources <span className="live-label">LIVE</span></button><button className={`nav-item ${active === 'Settings' ? 'active' : ''}`} onClick={() => setView('Settings')}>⌘&nbsp; Settings</button><div className="user-card" aria-label="Current analyst"><span className="avatar">JD</span><span><strong>Jordan Davis</strong><small>Workspace analyst</small></span></div></div></aside><nav className="mobile-nav" aria-label="Mobile navigation">{nav.map((item) => <button key={item.label} className={active === item.label ? 'active' : ''} onClick={() => setView(item.label)}><Icon name={item.icon} /><span>{item.label === 'Market demand' ? 'Demand' : item.label === 'Salary insights' ? 'Salary' : item.label}</span></button>)}<button className={active === 'Settings' ? 'active' : ''} onClick={() => setView('Settings')}><span className="mobile-nav-symbol">⌘</span><span>Settings</span></button></nav><section className="content"><header className="topbar"><div><p className="eyebrow">MONDAY, SEPTEMBER 28, 2026</p><h1>{title}</h1><p className="subtitle">Here&apos;s what&apos;s happening in the labor market.</p></div><div className="header-actions"><button className="icon-button" aria-label="Notifications" onClick={() => setNotice('You are all caught up')}>♢<span className="notification-dot" /></button><button className="export-button" onClick={() => setNotice('Report export prepared')}>Export report <span>↓</span></button></div></header>{notice && <div className="toast" role="status">{notice}<button onClick={() => setNotice('')}>×</button></div>}<div className="page-stage">{content}</div></section></main>
}
