import React, { useState, useEffect } from 'react';
import api from '../services/api';
import { 
  BarChart3, 
  Calendar, 
  TrendingUp, 
  TrendingDown, 
  Wallet,
  ArrowUpRight,
  ArrowDownRight,
  Loader2
} from 'lucide-react';
import { 
  ResponsiveContainer, 
  BarChart, 
  Bar, 
  XAxis, 
  YAxis, 
  Tooltip, 
  Legend 
} from 'recharts';
import { toast } from 'react-hot-toast';

const Reports = () => {
  const [reportType, setReportType] = useState('MONTHLY'); // 'MONTHLY' or 'YEARLY'
  const [loading, setLoading] = useState(false);
  const [monthlyData, setMonthlyData] = useState(null);
  const [yearlyData, setYearlyData] = useState(null);

  // Selector inputs state
  const today = new Date();
  const [selectedYear, setSelectedYear] = useState(today.getFullYear().toString());
  const [selectedMonth, setSelectedMonth] = useState((today.getMonth() + 1).toString());

  const yearsList = Array.from({ length: 5 }, (_, i) => (today.getFullYear() - i).toString());
  const monthsList = [
    { value: '1', name: 'January' },
    { value: '2', name: 'February' },
    { value: '3', name: 'March' },
    { value: '4', name: 'April' },
    { value: '5', name: 'May' },
    { value: '6', name: 'June' },
    { value: '7', name: 'July' },
    { value: '8', name: 'August' },
    { value: '9', name: 'September' },
    { value: '10', name: 'October' },
    { value: '11', name: 'November' },
    { value: '12', name: 'December' }
  ];

  const fetchReport = async () => {
    setLoading(true);
    try {
      if (reportType === 'MONTHLY') {
        const response = await api.get(`/reports/monthly/${selectedYear}/${selectedMonth}`);
        setMonthlyData(response.data);
      } else {
        const response = await api.get(`/reports/yearly/${selectedYear}`);
        setYearlyData(response.data);
      }
    } catch (err) {
      console.error('Error fetching report details:', err);
      toast.error('Failed to generate report');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchReport();
  }, [reportType, selectedYear, selectedMonth]);

  const formatCurrency = (val) => {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD'
    }).format(val || 0);
  };

  const getSum = (mapData) => {
    if (!mapData) return 0;
    return Object.values(mapData).reduce((a, b) => a + parseFloat(b), 0);
  };

  // Recharts trend config
  const trendData = Object.entries(yearlyData?.totalExpenses || {}).map(([name, val]) => ({
    name,
    Amount: parseFloat(val)
  }));

  const monthlyIncomeList = Object.entries(monthlyData?.totalIncome || {});
  const monthlyExpenseList = Object.entries(monthlyData?.totalExpenses || {});

  const totalIncome = getSum(monthlyData?.totalIncome);
  const totalExpenses = getSum(monthlyData?.totalExpenses);

  const yearlyIncome = getSum(yearlyData?.totalIncome);
  const yearlyExpenses = getSum(yearlyData?.totalExpenses);

  return (
    <div className="space-y-8 animate-slide-up">
      {/* Header */}
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div>
          <h1 className="text-3xl font-bold text-slate-900 heading-font">Reports</h1>
          <p className="text-slate-500 mt-1">Compile comprehensive financial summaries.</p>
        </div>
        
        {/* Toggle switches */}
        <div className="bg-slate-100 p-1.5 rounded-2xl flex items-center w-fit border border-slate-200">
          <button
            onClick={() => setReportType('MONTHLY')}
            className={`px-4 py-2 rounded-xl text-sm font-semibold transition-all ${
              reportType === 'MONTHLY' 
                ? 'bg-white text-slate-800 shadow-sm' 
                : 'text-slate-500 hover:text-slate-700'
            }`}
          >
            Monthly
          </button>
          <button
            onClick={() => setReportType('YEARLY')}
            className={`px-4 py-2 rounded-xl text-sm font-semibold transition-all ${
              reportType === 'YEARLY' 
                ? 'bg-white text-slate-800 shadow-sm' 
                : 'text-slate-500 hover:text-slate-700'
            }`}
          >
            Yearly
          </button>
        </div>
      </div>

      {/* Selectors Bar */}
      <div className="bg-white border border-slate-100 rounded-3xl p-6 shadow-sm flex flex-col md:flex-row gap-4 items-center">
        <div className="flex items-center gap-2 text-slate-400 text-sm mr-2 flex-shrink-0">
          <Calendar className="w-5 h-5 text-sky-500" />
          <span className="font-semibold text-xs uppercase tracking-wider">Select Range</span>
        </div>

        <div className="flex gap-4 w-full md:w-auto">
          {/* Year selector */}
          <select
            value={selectedYear}
            onChange={(e) => setSelectedYear(e.target.value)}
            className="flex-1 md:w-36 px-4 py-2 bg-slate-50 border border-slate-200 rounded-xl text-slate-800 focus:outline-none focus:ring-2 focus:ring-sky-500/20 focus:border-sky-500 transition-all text-sm"
          >
            {yearsList.map(y => (
              <option key={y} value={y}>{y}</option>
            ))}
          </select>

          {/* Month selector (Monthly only) */}
          {reportType === 'MONTHLY' && (
            <select
              value={selectedMonth}
              onChange={(e) => setSelectedMonth(e.target.value)}
              className="flex-1 md:w-44 px-4 py-2 bg-slate-50 border border-slate-200 rounded-xl text-slate-800 focus:outline-none focus:ring-2 focus:ring-sky-500/20 focus:border-sky-500 transition-all text-sm"
            >
              {monthsList.map(m => (
                <option key={m.value} value={m.value}>{m.name}</option>
              ))}
            </select>
          )}
        </div>
      </div>

      {loading ? (
        <div className="flex items-center justify-center py-20">
          <div className="flex flex-col items-center gap-3">
            <Loader2 className="w-10 h-10 text-sky-500 animate-spin" />
            <p className="text-slate-500 text-sm font-medium">Generating financial statement...</p>
          </div>
        </div>
      ) : reportType === 'MONTHLY' ? (
        /* Monthly report view */
        monthlyData && (
          <div className="space-y-8">
            {/* Monthly KPIs */}
            <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
              <div className="bg-white border border-slate-100 rounded-3xl p-6 shadow-sm flex items-center gap-5">
                <div className="flex items-center justify-center w-12 h-12 rounded-2xl bg-emerald-50 text-emerald-600">
                  <TrendingUp className="w-6 h-6" />
                </div>
                <div>
                  <span className="text-xs font-semibold text-slate-400 uppercase tracking-wider">Total Income</span>
                  <h3 className="text-2xl font-bold text-slate-900 heading-font mt-1">{formatCurrency(totalIncome)}</h3>
                </div>
              </div>

              <div className="bg-white border border-slate-100 rounded-3xl p-6 shadow-sm flex items-center gap-5">
                <div className="flex items-center justify-center w-12 h-12 rounded-2xl bg-rose-50 text-rose-600">
                  <TrendingDown className="w-6 h-6" />
                </div>
                <div>
                  <span className="text-xs font-semibold text-slate-400 uppercase tracking-wider">Total Expenses</span>
                  <h3 className="text-2xl font-bold text-slate-900 heading-font mt-1">{formatCurrency(totalExpenses)}</h3>
                </div>
              </div>

              <div className="bg-white border border-slate-100 rounded-3xl p-6 shadow-sm flex items-center gap-5">
                <div className="flex items-center justify-center w-12 h-12 rounded-2xl bg-sky-50 text-sky-600">
                  <Wallet className="w-6 h-6" />
                </div>
                <div>
                  <span className="text-xs font-semibold text-slate-400 uppercase tracking-wider">Net Savings</span>
                  <h3 className={`text-2xl font-bold heading-font mt-1 ${monthlyData.netSavings >= 0 ? 'text-slate-900' : 'text-rose-600'}`}>
                    {formatCurrency(monthlyData.netSavings)}
                  </h3>
                </div>
              </div>
            </div>

            {/* Income & Expense Breakdown tables */}
            <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
              
              {/* Income by Category */}
              <div className="bg-white border border-slate-100 rounded-3xl p-6 shadow-sm">
                <div className="flex items-center gap-2 mb-6">
                  <ArrowUpRight className="w-5 h-5 text-emerald-500" />
                  <h2 className="text-lg font-bold text-slate-900 heading-font">Income by Category</h2>
                </div>

                {monthlyIncomeList.length > 0 ? (
                  <div className="space-y-4">
                    {monthlyIncomeList.map(([name, amount]) => (
                      <div key={name} className="flex justify-between items-center py-2 border-b border-slate-50 last:border-0">
                        <span className="font-semibold text-sm text-slate-700">{name}</span>
                        <span className="font-bold text-sm text-slate-900 heading-font">{formatCurrency(amount)}</span>
                      </div>
                    ))}
                  </div>
                ) : (
                  <div className="text-slate-400 text-sm py-10 text-center">No income records matching filters</div>
                )}
              </div>

              {/* Expense by Category */}
              <div className="bg-white border border-slate-100 rounded-3xl p-6 shadow-sm">
                <div className="flex items-center gap-2 mb-6">
                  <ArrowDownRight className="w-5 h-5 text-rose-500" />
                  <h2 className="text-lg font-bold text-slate-900 heading-font">Expenses by Category</h2>
                </div>

                {monthlyExpenseList.length > 0 ? (
                  <div className="space-y-4">
                    {monthlyExpenseList.map(([name, amount]) => (
                      <div key={name} className="flex justify-between items-center py-2 border-b border-slate-50 last:border-0">
                        <span className="font-semibold text-sm text-slate-700">{name}</span>
                        <span className="font-bold text-sm text-slate-900 heading-font">{formatCurrency(amount)}</span>
                      </div>
                    ))}
                  </div>
                ) : (
                  <div className="text-slate-400 text-sm py-10 text-center">No expense records matching filters</div>
                )}
              </div>

            </div>
          </div>
        )
      ) : (
        /* Yearly report view */
        yearlyData && (
          <div className="space-y-8">
            {/* Yearly KPIs */}
            <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
              <div className="bg-white border border-slate-100 rounded-3xl p-6 shadow-sm flex items-center gap-5">
                <div className="flex items-center justify-center w-12 h-12 rounded-2xl bg-emerald-50 text-emerald-600">
                  <TrendingUp className="w-6 h-6" />
                </div>
                <div>
                  <span className="text-xs font-semibold text-slate-400 uppercase tracking-wider">Total Income</span>
                  <h3 className="text-2xl font-bold text-slate-900 heading-font mt-1">{formatCurrency(yearlyIncome)}</h3>
                </div>
              </div>

              <div className="bg-white border border-slate-100 rounded-3xl p-6 shadow-sm flex items-center gap-5">
                <div className="flex items-center justify-center w-12 h-12 rounded-2xl bg-rose-50 text-rose-600">
                  <TrendingDown className="w-6 h-6" />
                </div>
                <div>
                  <span className="text-xs font-semibold text-slate-400 uppercase tracking-wider">Total Expenses</span>
                  <h3 className="text-2xl font-bold text-slate-900 heading-font mt-1">{formatCurrency(yearlyExpenses)}</h3>
                </div>
              </div>

              <div className="bg-white border border-slate-100 rounded-3xl p-6 shadow-sm flex items-center gap-5">
                <div className="flex items-center justify-center w-12 h-12 rounded-2xl bg-sky-50 text-sky-600">
                  <Wallet className="w-6 h-6" />
                </div>
                <div>
                  <span className="text-xs font-semibold text-slate-400 uppercase tracking-wider">Net Savings</span>
                  <h3 className={`text-2xl font-bold heading-font mt-1 ${yearlyData.netSavings >= 0 ? 'text-slate-900' : 'text-rose-600'}`}>
                    {formatCurrency(yearlyData.netSavings)}
                  </h3>
                </div>
              </div>
            </div>

            {/* Yearly Expense Bar Chart */}
            <div className="bg-white border border-slate-100 rounded-3xl p-6 shadow-sm">
              <h2 className="text-lg font-bold text-slate-900 heading-font mb-6">Yearly Expense Trend</h2>
              <div className="w-full h-80">
                {trendData.length > 0 ? (
                  <ResponsiveContainer width="100%" height="100%">
                    <BarChart data={trendData}>
                      <XAxis dataKey="name" stroke="#94a3b8" fontSize={12} tickLine={false} axisLine={false} />
                      <YAxis stroke="#94a3b8" fontSize={12} tickLine={false} axisLine={false} />
                      <Tooltip cursor={{ fill: '#f8fafc' }} />
                      <Legend verticalAlign="top" height={36} iconType="circle" />
                      <Bar dataKey="Amount" fill="#f43f5e" radius={[4, 4, 0, 0]} />
                    </BarChart>
                  </ResponsiveContainer>
                ) : (
                  <div className="flex items-center justify-center h-full text-slate-400">No trend data compiled</div>
                )}
              </div>
            </div>

            {/* Income & Expense Breakdown tables */}
            <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
              
              {/* Income by Category */}
              <div className="bg-white border border-slate-100 rounded-3xl p-6 shadow-sm">
                <div className="flex items-center gap-2 mb-6">
                  <ArrowUpRight className="w-5 h-5 text-emerald-500" />
                  <h2 className="text-lg font-bold text-slate-900 heading-font">Yearly Income by Category</h2>
                </div>

                {Object.entries(yearlyData.totalIncome || {}).length > 0 ? (
                  <div className="space-y-4">
                    {Object.entries(yearlyData.totalIncome || {}).map(([name, amount]) => (
                      <div key={name} className="flex justify-between items-center py-2 border-b border-slate-50 last:border-0">
                        <span className="font-semibold text-sm text-slate-700">{name}</span>
                        <span className="font-bold text-sm text-slate-900 heading-font">{formatCurrency(amount)}</span>
                      </div>
                    ))}
                  </div>
                ) : (
                  <div className="text-slate-400 text-sm py-10 text-center">No income records matching filters</div>
                )}
              </div>

              {/* Expense by Category */}
              <div className="bg-white border border-slate-100 rounded-3xl p-6 shadow-sm">
                <div className="flex items-center gap-2 mb-6">
                  <ArrowDownRight className="w-5 h-5 text-rose-500" />
                  <h2 className="text-lg font-bold text-slate-900 heading-font">Yearly Expenses by Category</h2>
                </div>

                {Object.entries(yearlyData.totalExpenses || {}).length > 0 ? (
                  <div className="space-y-4">
                    {Object.entries(yearlyData.totalExpenses || {}).map(([name, amount]) => (
                      <div key={name} className="flex justify-between items-center py-2 border-b border-slate-50 last:border-0">
                        <span className="font-semibold text-sm text-slate-700">{name}</span>
                        <span className="font-bold text-sm text-slate-900 heading-font">{formatCurrency(amount)}</span>
                      </div>
                    ))}
                  </div>
                ) : (
                  <div className="text-slate-400 text-sm py-10 text-center">No expense records matching filters</div>
                )}
              </div>

            </div>

          </div>
        )
      )}
    </div>
  );
};

export default Reports;
