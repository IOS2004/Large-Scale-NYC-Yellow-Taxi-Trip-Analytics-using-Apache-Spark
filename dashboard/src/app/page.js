'use client';

import { useState, useEffect } from 'react';
import { AreaChart, Area, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, BarChart, Bar, Legend } from 'recharts';
import { Car, CreditCard, Activity, ArrowUpRight, ArrowDownRight } from 'lucide-react';

// Mock data based on our Spark aggregated output
const rollingAvgData = [
  { date: '2025-10-01', Manhattan: 45000, Brooklyn: 15000, Queens: 12000 },
  { date: '2025-10-02', Manhattan: 46200, Brooklyn: 15300, Queens: 12100 },
  { date: '2025-10-03', Manhattan: 47500, Brooklyn: 15800, Queens: 12500 },
  { date: '2025-10-04', Manhattan: 49000, Brooklyn: 16200, Queens: 13000 },
  { date: '2025-10-05', Manhattan: 52000, Brooklyn: 17000, Queens: 14000 },
  { date: '2025-10-06', Manhattan: 51500, Brooklyn: 16800, Queens: 13800 },
  { date: '2025-10-07', Manhattan: 48000, Brooklyn: 15500, Queens: 12200 },
];

const topZonesData = [
  { hour: '08:00', 'JFK Airport': 1200, 'Midtown': 2500, 'Financial Dist': 1800 },
  { hour: '12:00', 'JFK Airport': 1500, 'Midtown': 3100, 'Financial Dist': 1400 },
  { hour: '18:00', 'JFK Airport': 2200, 'Midtown': 4200, 'Financial Dist': 2100 },
  { hour: '22:00', 'JFK Airport': 1800, 'Midtown': 3500, 'Financial Dist': 1200 },
];

const CustomTooltip = ({ active, payload, label }) => {
  if (active && payload && payload.length) {
    return (
      <div className="custom-tooltip">
        <p className="tooltip-label">{label}</p>
        {payload.map((entry, index) => (
          <p key={index} className="tooltip-value" style={{ color: entry.color }}>
            {entry.name}: {entry.value.toLocaleString()} trips
          </p>
        ))}
      </div>
    );
  }
  return null;
};

export default function Dashboard() {
  const [mounted, setMounted] = useState(false);

  useEffect(() => {
    setMounted(true);
  }, []);

  if (!mounted) return null;

  return (
    <div className="dashboard-container">
      <header className="header">
        <div className="header-title">
          <h1>NYC Taxi Intelligence</h1>
          <p>Live Distributed Analytics & Machine Learning Insights</p>
        </div>
      </header>

      <div className="kpi-grid">
        <div className="glass-panel kpi-card">
          <div className="kpi-icon"><Activity size={28} /></div>
          <div className="kpi-content">
            <h3>Total Processed Trips</h3>
            <div className="value">396.1M</div>
            <div className="trend positive"><ArrowUpRight size={16} /> 12% vs last year</div>
          </div>
        </div>

        <div className="glass-panel kpi-card">
          <div className="kpi-icon"><Car size={28} /></div>
          <div className="kpi-content">
            <h3>Avg Daily Demand (Manhattan)</h3>
            <div className="value">48.5K</div>
            <div className="trend positive"><ArrowUpRight size={16} /> 5.2% vs last week</div>
          </div>
        </div>

        <div className="glass-panel kpi-card">
          <div className="kpi-icon"><CreditCard size={28} /></div>
          <div className="kpi-content">
            <h3>ML Tip Predictor (ROC)</h3>
            <div className="value">0.892</div>
            <div className="trend negative"><ArrowDownRight size={16} /> -0.01 vs v1.0</div>
          </div>
        </div>
      </div>

      <div className="charts-grid">
        <div className="glass-panel">
          <div className="chart-header">
            <h2>Rolling 7-Day Average Demand</h2>
            <p>Calculated via Spark SQL Window Functions</p>
          </div>
          <div className="chart-container">
            <ResponsiveContainer width="100%" height="100%">
              <AreaChart data={rollingAvgData}>
                <defs>
                  <linearGradient id="colorManhattan" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="5%" stopColor="#3b82f6" stopOpacity={0.8}/>
                    <stop offset="95%" stopColor="#3b82f6" stopOpacity={0}/>
                  </linearGradient>
                  <linearGradient id="colorBrooklyn" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="5%" stopColor="#8b5cf6" stopOpacity={0.8}/>
                    <stop offset="95%" stopColor="#8b5cf6" stopOpacity={0}/>
                  </linearGradient>
                </defs>
                <CartesianGrid strokeDasharray="3 3" stroke="rgba(255,255,255,0.1)" vertical={false} />
                <XAxis dataKey="date" stroke="#94a3b8" tick={{fill: '#94a3b8'}} axisLine={false} tickLine={false} />
                <YAxis stroke="#94a3b8" tick={{fill: '#94a3b8'}} axisLine={false} tickLine={false} tickFormatter={(value) => `${value/1000}k`} />
                <Tooltip content={<CustomTooltip />} />
                <Area type="monotone" dataKey="Manhattan" stroke="#3b82f6" strokeWidth={3} fillOpacity={1} fill="url(#colorManhattan)" />
                <Area type="monotone" dataKey="Brooklyn" stroke="#8b5cf6" strokeWidth={3} fillOpacity={1} fill="url(#colorBrooklyn)" />
              </AreaChart>
            </ResponsiveContainer>
          </div>
        </div>

        <div className="glass-panel">
          <div className="chart-header">
            <h2>Busiest Zones by Hour (Top 3)</h2>
            <p>Ranked via Spark Dense Rank Partitions</p>
          </div>
          <div className="chart-container">
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={topZonesData} margin={{ top: 20, right: 30, left: 20, bottom: 5 }}>
                <CartesianGrid strokeDasharray="3 3" stroke="rgba(255,255,255,0.1)" vertical={false} />
                <XAxis dataKey="hour" stroke="#94a3b8" tick={{fill: '#94a3b8'}} axisLine={false} tickLine={false} />
                <YAxis stroke="#94a3b8" tick={{fill: '#94a3b8'}} axisLine={false} tickLine={false} />
                <Tooltip content={<CustomTooltip />} />
                <Legend wrapperStyle={{ paddingTop: '20px' }} />
                <Bar dataKey="Midtown" fill="#10b981" radius={[4, 4, 0, 0]} />
                <Bar dataKey="JFK Airport" fill="#f59e0b" radius={[4, 4, 0, 0]} />
                <Bar dataKey="Financial Dist" fill="#ec4899" radius={[4, 4, 0, 0]} />
              </BarChart>
            </ResponsiveContainer>
          </div>
        </div>
      </div>
    </div>
  );
}
