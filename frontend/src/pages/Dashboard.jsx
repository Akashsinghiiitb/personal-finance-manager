import React, { useState, useEffect } from 'react';
import api from '../services/api';
import { 
  TrendingUp, 
  TrendingDown, 
  Wallet, 
  PlusCircle, 
  Calendar,
  ChevronRight,
  Loader2
} from 'lucide-react';
import { Link } from 'react-router-dom';
import { 
  ResponsiveContainer, 
  PieChart, 
  Pie, 
  Cell, 
  BarChart, 
  Bar, 
  XAxis, 
  YAxis, 
  Tooltip, 
  Legend 
} from 'recharts';

const COLORS = ['#0ea5e9', '#38bdf8', '#818cf8', '#a78bfa', '#f472b6', '#fb7185', '#34d399'];

const Dashboard = () => {
  const [loading, setLoading] = useState(true);
  const [monthlyData, setMonthlyData] = useState(null);
  const [yearlyData, setYearlyData] = useState(null);
  const [goals, setGoals] = useState([]);
  const [recentTransactions, setRecentTransactions] = useState([]);

  const today = new Date();
  const year = today.getFullYear();
  const month = today.getMonth() + 1;

  const fetchDashboardData = async () => {
    try {
      setLoading(true);
      const [monthlyRes, yearlyRes, goalsRes, txRes] = await Promise.all([
        api.get(`/reports/monthly/${year}/${month}`),
        api.get(`/reports/yearly/${year}`),
        api.get('/goals'),
        api.get('/transactions')
      ]);

      setMonthlyData(monthlyRes.data);
      setYearlyData(yearlyRes.data);
      setGoals(goalsRes.data.goals || []);
      setRecentTransactions(txRes.data.transactions?.slice(0, 5) || []);
    } catch (err) {
      console.error('Error fetching dashboard statistics:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchDashboardData();
  }, []);

  const formatCurrency = (value) => {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD'
    }).format(value || 0);
  };

  const getSum = (mapData) => {
    if (!mapData) return 0;
    return Object.values(mapData).reduce((a, b) => a + parseFloat(b), 0);
  };

  // Prepping charts data
  const pieData = Object.entries(monthlyData?.totalExpenses || {}).map(([name, val]) => ({
    name,
    value: parseFloat(val)
  }));

  const trendData = Object.entries(yearlyData?.totalExpenses || {}).map(([name, val]) => ({
    name,
    Amount: parseFloat(val)
  }));

  const totalIncome = getSum(monthlyData?.totalIncome);
  const totalExpenses = getSum(monthlyData?.totalExpenses);
  const netSavings = monthlyData?.netSavings || 0;

  return (
    <div className="space-y-8 animate-slide-up">
      {/* Welcome banner */}
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div>
          <h1 className="text-3xl font-bold text-slate-900 heading-font">Overview</h1>
          <p className="text-slate-500 mt-1">Here is your financial status for this month.</p>
        </div>
        <div className="flex items-center gap-3">
          <Link
            to="/transactions"
            className="flex items-center gap-2 px-4 py-2.5 bg-sky-500 hover:bg-sky-400 active:bg-sky-600 text-white font-medium rounded-xl text-sm transition-all duration-200 shadow-md shadow-sky-500/10"
          >
            <PlusCircle className="w-4 h-4" />
            <span>Add Transaction</span>
          </Link>
        </div>
      </div>

      {/* KPI Cards */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        {/* Income Card */}
        <div className="bg-white border border-slate-100 rounded-3xl p-6 shadow-sm flex items-center gap-5">
          <div className="flex items-center justify-center w-12 h-12 rounded-2xl bg-emerald-50 text-emerald-600">
            <TrendingUp className="w-6 h-6" />
          </div>
          <div>
            <span className="text-xs font-semibold text-slate-400 uppercase tracking-wider">Total Income</span>
            <h3 className="text-2xl font-bold text-slate-900 heading-font mt-1">
              {formatCurrency(totalIncome)}
            </h3>
          </div>
        </div>

        {/* Expenses Card */}
        <div className="bg-white border border-slate-100 rounded-3xl p-6 shadow-sm flex items-center gap-5">
          <div className="flex items-center justify-center w-12 h-12 rounded-2xl bg-rose-50 text-rose-600">
            <TrendingDown className="w-6 h-6" />
          </div>
          <div>
            <span className="text-xs font-semibold text-slate-400 uppercase tracking-wider">Total Expenses</span>
            <h3 className="text-2xl font-bold text-slate-900 heading-font mt-1">
              {formatCurrency(totalExpenses)}
            </h3>
          </div>
        </div>

        {/* Net Savings Card */}
        <div className="bg-white border border-slate-100 rounded-3xl p-6 shadow-sm flex items-center gap-5">
          <div className="flex items-center justify-center w-12 h-12 rounded-2xl bg-sky-50 text-sky-600">
            <Wallet className="w-6 h-6" />
          </div>
          <div>
            <span className="text-xs font-semibold text-slate-400 uppercase tracking-wider">Net Savings</span>
            <h3 className={`text-2xl font-bold heading-font mt-1 ${netSavings >= 0 ? 'text-slate-900' : 'text-rose-600'}`}>
              {formatCurrency(netSavings)}
            </h3>
          </div>
        </div>
      </div>

      {/* Main dashboard grids */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
        
        {/* Yearly Expense Chart */}
        <div className="lg:col-span-2 bg-white border border-slate-100 rounded-3xl p-6 shadow-sm flex flex-col">
          <h2 className="text-lg font-bold text-slate-900 heading-font mb-6">Yearly Expense Breakdown ({year})</h2>
          <div className="w-full h-80">
            {trendData.length > 0 ? (
              <ResponsiveContainer width="100%" height="100%">
                <BarChart data={trendData} margin={{ top: 10, right: 10, left: -10, bottom: 0 }}>
                  <XAxis dataKey="name" stroke="#94a3b8" fontSize={12} tickLine={false} axisLine={false} />
                  <YAxis stroke="#94a3b8" fontSize={12} tickLine={false} axisLine={false} />
                  <Tooltip cursor={{ fill: '#f8fafc' }} />
                  <Legend verticalAlign="top" height={36} iconType="circle" />
                  <Bar dataKey="Amount" fill="#f43f5e" radius={[4, 4, 0, 0]} />
                </BarChart>
              </ResponsiveContainer>
            ) : (
              <div className="flex items-center justify-center h-full text-slate-400 text-sm">
                No expense trend data available for this year
              </div>
            )}
          </div>
        </div>

        {/* Expense Category Breakdown Chart */}
        <div className="bg-white border border-slate-100 rounded-3xl p-6 shadow-sm flex flex-col">
          <h2 className="text-lg font-bold text-slate-900 heading-font mb-6">Expense Categories</h2>
          <div className="w-full h-64 flex-1 relative flex items-center justify-center">
            {pieData.length > 0 ? (
              <ResponsiveContainer width="100%" height="100%">
                <PieChart>
                  <Pie
                    data={pieData}
                    cx="50%"
                    cy="50%"
                    innerRadius={60}
                    outerRadius={80}
                    paddingAngle={3}
                    dataKey="value"
                  >
                    {pieData.map((entry, index) => (
                      <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                    ))}
                  </Pie>
                  <Tooltip formatter={(value) => formatCurrency(value)} />
                </PieChart>
              </ResponsiveContainer>
            ) : (
              <div className="text-slate-400 text-sm">No expenses recorded this month</div>
            )}
          </div>
          {pieData.length > 0 && (
            <div className="grid grid-cols-2 gap-2 mt-4 text-xs font-medium text-slate-600">
              {pieData.slice(0, 4).map((entry, index) => (
                <div key={entry.name} className="flex items-center gap-1.5 truncate">
                  <span className="w-2.5 h-2.5 rounded-full flex-shrink-0" style={{ backgroundColor: COLORS[index % COLORS.length] }} />
                  <span className="truncate">{entry.name}</span>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
        {/* Recent Transactions list */}
        <div className="lg:col-span-2 bg-white border border-slate-100 rounded-3xl p-6 shadow-sm">
          <div className="flex items-center justify-between mb-6">
            <h2 className="text-lg font-bold text-slate-900 heading-font">Recent Transactions</h2>
            <Link to="/transactions" className="text-sky-500 hover:text-sky-400 text-xs font-semibold flex items-center gap-1">
              <span>View All</span>
              <ChevronRight className="w-4 h-4" />
            </Link>
          </div>

          {recentTransactions.length > 0 ? (
            <div className="divide-y divide-slate-100">
              {recentTransactions.map((tx) => (
                <div key={tx.id} className="flex items-center justify-between py-4 first:pt-0 last:pb-0">
                  <div className="flex items-center gap-4 min-w-0">
                    <div className={`flex items-center justify-center w-10 h-10 rounded-xl flex-shrink-0 ${
                      tx.type === 'INCOME' ? 'bg-emerald-50 text-emerald-600' : 'bg-rose-50 text-rose-600'
                    }`}>
                      <Calendar className="w-5 h-5" />
                    </div>
                    <div className="min-w-0">
                      <h4 className="text-sm font-semibold text-slate-800 truncate">{tx.description || 'No description'}</h4>
                      <div className="flex items-center gap-2 mt-0.5">
                        <span className="text-[11px] font-semibold text-slate-400 uppercase">{tx.category}</span>
                        <span className="text-[10px] text-slate-400">•</span>
                        <span className="text-xs text-slate-400">{tx.date}</span>
                      </div>
                    </div>
                  </div>
                  <span className={`text-sm font-bold heading-font ${
                    tx.type === 'INCOME' ? 'text-emerald-600' : 'text-slate-800'
                  }`}>
                    {tx.type === 'INCOME' ? '+' : '-'}{formatCurrency(tx.amount)}
                  </span>
                </div>
              ))}
            </div>
          ) : (
            <div className="flex flex-col items-center justify-center py-10 text-slate-400 text-sm">
              No transactions recorded yet.
            </div>
          )}
        </div>

        {/* Savings Goal progress */}
        <div className="bg-white border border-slate-100 rounded-3xl p-6 shadow-sm flex flex-col">
          <div className="flex items-center justify-between mb-6">
            <h2 className="text-lg font-bold text-slate-900 heading-font">Savings Goals</h2>
            <Link to="/goals" className="text-sky-500 hover:text-sky-400 text-xs font-semibold flex items-center gap-1">
              <span>Manage</span>
              <ChevronRight className="w-4 h-4" />
            </Link>
          </div>

          {goals.length > 0 ? (
            <div className="space-y-6 flex-1 overflow-y-auto max-h-80">
              {goals.slice(0, 3).map((goal) => {
                const pct = Math.min(Math.max(parseFloat(goal.progressPercentage), 0), 100);
                return (
                  <div key={goal.id} className="space-y-2">
                    <div className="flex items-center justify-between text-sm">
                      <span className="font-semibold text-slate-800 truncate">{goal.goalName}</span>
                      <span className="font-bold text-slate-900 heading-font">{pct.toFixed(0)}%</span>
                    </div>
                    <div className="w-full h-2 bg-slate-100 rounded-full overflow-hidden">
                      <div 
                        className="h-full bg-sky-500 rounded-full transition-all duration-500" 
                        style={{ width: `${pct}%` }} 
                      />
                    </div>
                    <div className="flex justify-between text-xs text-slate-400">
                      <span>{formatCurrency(goal.currentProgress)} saved</span>
                      <span>Target: {formatCurrency(goal.targetAmount)}</span>
                    </div>
                  </div>
                );
              })}
            </div>
          ) : (
            <div className="flex flex-col items-center justify-center py-10 text-slate-400 text-sm flex-1">
              No active savings goals found.
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default Dashboard;
