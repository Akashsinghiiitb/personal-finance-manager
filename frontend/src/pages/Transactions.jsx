import React, { useState, useEffect } from 'react';
import api from '../services/api';
import { 
  Plus, 
  Search, 
  Trash2, 
  Edit2, 
  X,
  Filter,
  Loader2,
  Calendar
} from 'lucide-react';
import { toast } from 'react-hot-toast';

const Transactions = () => {
  const [transactions, setTransactions] = useState([]);
  const [categories, setCategories] = useState([]);
  const [loading, setLoading] = useState(true);

  // Filters state
  const [filterCategory, setFilterCategory] = useState('');
  const [filterStartDate, setFilterStartDate] = useState('');
  const [filterEndDate, setFilterEndDate] = useState('');

  // Modals state
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [modalMode, setModalMode] = useState('ADD'); // 'ADD' or 'EDIT'
  const [selectedTx, setSelectedTx] = useState(null);

  // Form inputs state
  const [amount, setAmount] = useState('');
  const [date, setDate] = useState('');
  const [categoryName, setCategoryName] = useState('');
  const [description, setDescription] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);

  const fetchTransactions = async () => {
    try {
      setLoading(true);
      let queryParams = [];
      if (filterStartDate) queryParams.push(`startDate=${filterStartDate}`);
      if (filterEndDate) queryParams.push(`endDate=${filterEndDate}`);
      if (filterCategory) {
        // Find category ID based on name or pass name?
        // Wait, the API specifies: "/api/transactions?categoryId=..."
        // So we look up category ID in categories state
        const matched = categories.find(c => c.name === filterCategory);
        if (matched?.id) {
          queryParams.push(`categoryId=${matched.id}`);
        }
      }

      const url = `/transactions${queryParams.length > 0 ? '?' + queryParams.join('&') : ''}`;
      const response = await api.get(url);
      setTransactions(response.data.transactions || []);
    } catch (err) {
      console.error('Error fetching transactions:', err);
      toast.error('Failed to load transactions');
    } finally {
      setLoading(false);
    }
  };

  const fetchCategories = async () => {
    try {
      const response = await api.get('/categories');
      setCategories(response.data.categories || []);
    } catch (err) {
      console.error('Error fetching categories:', err);
    }
  };

  useEffect(() => {
    const initData = async () => {
      await fetchCategories();
    };
    initData();
  }, []);

  // Fetch transactions when filters change
  useEffect(() => {
    fetchTransactions();
  }, [filterStartDate, filterEndDate, filterCategory, categories]);

  const handleOpenAddModal = () => {
    setModalMode('ADD');
    setSelectedTx(null);
    setAmount('');
    setDate(new Date().toISOString().split('T')[0]); // default to today
    setCategoryName(categories[0]?.name || '');
    setDescription('');
    setIsModalOpen(true);
  };

  const handleOpenEditModal = (tx) => {
    setModalMode('EDIT');
    setSelectedTx(tx);
    setAmount(tx.amount);
    setDate(tx.date); // read-only in UI
    setCategoryName(tx.category);
    setDescription(tx.description || '');
    setIsModalOpen(true);
  };

  const handleSaveTransaction = async (e) => {
    e.preventDefault();
    if (!amount || parseFloat(amount) <= 0) {
      toast.error('Amount must be positive');
      return;
    }
    if (!categoryName) {
      toast.error('Category must be selected');
      return;
    }

    setIsSubmitting(true);
    try {
      if (modalMode === 'ADD') {
        await api.post('/transactions', {
          amount: parseFloat(amount),
          date,
          category: categoryName,
          description: description.trim()
        });
        toast.success('Transaction added successfully!');
      } else {
        // Date cannot be updated, so it is locked
        await api.put(`/transactions/${selectedTx.id}`, {
          amount: parseFloat(amount),
          category: categoryName,
          description: description.trim(),
          date // sending original date to satisfy backend validation
        });
        toast.success('Transaction updated successfully!');
      }
      setIsModalOpen(false);
      fetchTransactions();
    } catch (err) {
      const msg = err.response?.data?.message || 'Action failed';
      toast.error(msg);
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleDelete = async (id) => {
    if (!window.confirm('Are you sure you want to delete this transaction?')) {
      return;
    }

    try {
      await api.delete(`/transactions/${id}`);
      toast.success('Transaction deleted!');
      fetchTransactions();
    } catch (err) {
      toast.error(err.response?.data?.message || 'Deletion failed');
    }
  };

  const resetFilters = () => {
    setFilterCategory('');
    setFilterStartDate('');
    setFilterEndDate('');
  };

  const formatCurrency = (val) => {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD'
    }).format(val || 0);
  };

  return (
    <div className="space-y-8 animate-slide-up">
      {/* Header */}
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div>
          <h1 className="text-3xl font-bold text-slate-900 heading-font">Transactions</h1>
          <p className="text-slate-500 mt-1">Review, filter, and log income or expenses.</p>
        </div>
        <button
          onClick={handleOpenAddModal}
          className="flex items-center gap-2 px-4 py-2.5 bg-sky-500 hover:bg-sky-400 active:bg-sky-600 text-white font-medium rounded-xl text-sm transition-all duration-200 shadow-md shadow-sky-500/10 w-fit"
        >
          <Plus className="w-4 h-4" />
          <span>New Transaction</span>
        </button>
      </div>

      {/* Filters Bar */}
      <div className="bg-white border border-slate-100 rounded-3xl p-6 shadow-sm">
        <div className="flex items-center gap-2 mb-4 text-slate-400 text-xs font-semibold uppercase tracking-wider">
          <Filter className="w-4 h-4" />
          <span>Filter Records</span>
        </div>
        
        <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
          {/* Category Filter */}
          <div>
            <select
              value={filterCategory}
              onChange={(e) => setFilterCategory(e.target.value)}
              className="block w-full px-4 py-2 bg-slate-50 border border-slate-200 rounded-xl text-slate-800 focus:outline-none focus:ring-2 focus:ring-sky-500/20 focus:border-sky-500 transition-all text-sm"
            >
              <option value="">All Categories</option>
              {categories.map((c) => (
                <option key={c.name} value={c.name}>{c.name}</option>
              ))}
            </select>
          </div>

          {/* Start Date */}
          <div>
            <input
              type="date"
              value={filterStartDate}
              onChange={(e) => setFilterStartDate(e.target.value)}
              className="block w-full px-4 py-2 bg-slate-50 border border-slate-200 rounded-xl text-slate-800 focus:outline-none focus:ring-2 focus:ring-sky-500/20 focus:border-sky-500 transition-all text-sm"
            />
          </div>

          {/* End Date */}
          <div>
            <input
              type="date"
              value={filterEndDate}
              onChange={(e) => setFilterEndDate(e.target.value)}
              className="block w-full px-4 py-2 bg-slate-50 border border-slate-200 rounded-xl text-slate-800 focus:outline-none focus:ring-2 focus:ring-sky-500/20 focus:border-sky-500 transition-all text-sm"
            />
          </div>

          {/* Reset Filters */}
          <button
            onClick={resetFilters}
            className="flex items-center justify-center px-4 py-2 bg-slate-100 hover:bg-slate-200 active:bg-slate-300 text-slate-600 font-semibold rounded-xl text-sm transition-colors duration-200"
          >
            Clear Filters
          </button>
        </div>
      </div>

      {/* Table grid */}
      <div className="bg-white border border-slate-100 rounded-3xl overflow-hidden shadow-sm">
        {loading ? (
          <div className="flex items-center justify-center py-20">
            <div className="flex flex-col items-center gap-3">
              <Loader2 className="w-10 h-10 text-sky-500 animate-spin" />
              <p className="text-slate-500 text-sm font-medium">Loading transactions...</p>
            </div>
          </div>
        ) : transactions.length > 0 ? (
          <div className="overflow-x-auto">
            <table className="w-full text-left border-collapse">
              <thead>
                <tr className="bg-slate-50/50 border-b border-slate-100">
                  <th className="px-6 py-4 text-xs font-bold text-slate-400 uppercase tracking-wider">Date</th>
                  <th className="px-6 py-4 text-xs font-bold text-slate-400 uppercase tracking-wider">Description</th>
                  <th className="px-6 py-4 text-xs font-bold text-slate-400 uppercase tracking-wider">Category</th>
                  <th className="px-6 py-4 text-xs font-bold text-slate-400 uppercase tracking-wider">Type</th>
                  <th className="px-6 py-4 text-xs font-bold text-slate-400 uppercase tracking-wider text-right">Amount</th>
                  <th className="px-6 py-4 text-xs font-bold text-slate-400 uppercase tracking-wider text-center">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {transactions.map((tx) => (
                  <tr key={tx.id} className="hover:bg-slate-50/30 transition-colors">
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-slate-500 font-medium">
                      <span className="flex items-center gap-1.5">
                        <Calendar className="w-4 h-4 text-slate-400" />
                        {tx.date}
                      </span>
                    </td>
                    <td className="px-6 py-4 text-sm font-semibold text-slate-800">
                      {tx.description || <span className="text-slate-300 italic">No description</span>}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-xs font-bold uppercase tracking-wider text-slate-500">
                      {tx.category}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <span className={`inline-flex px-2 py-0.5 rounded-full text-[10px] font-bold uppercase tracking-wider ${
                        tx.type === 'INCOME' 
                          ? 'bg-emerald-50 text-emerald-600 border border-emerald-500/10' 
                          : 'bg-rose-50 text-rose-600 border border-rose-500/10'
                      }`}>
                        {tx.type}
                      </span>
                    </td>
                    <td className={`px-6 py-4 whitespace-nowrap text-sm font-bold text-right heading-font ${
                      tx.type === 'INCOME' ? 'text-emerald-600' : 'text-slate-800'
                    }`}>
                      {tx.type === 'INCOME' ? '+' : '-'}{formatCurrency(tx.amount)}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-center">
                      <div className="flex items-center justify-center gap-2">
                        <button
                          onClick={() => handleOpenEditModal(tx)}
                          className="p-1.5 text-slate-400 hover:text-sky-500 hover:bg-sky-50 rounded-lg transition-all"
                          title="Edit transaction"
                        >
                          <Edit2 className="w-4 h-4" />
                        </button>
                        <button
                          onClick={() => handleDelete(tx.id)}
                          className="p-1.5 text-slate-400 hover:text-rose-500 hover:bg-rose-50 rounded-lg transition-all"
                          title="Delete transaction"
                        >
                          <Trash2 className="w-4 h-4" />
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        ) : (
          <div className="flex flex-col items-center justify-center py-20 text-slate-400 text-sm">
            No transactions found. Add a new one to get started!
          </div>
        )}
      </div>

      {/* Transaction Modal (Add/Edit) */}
      {isModalOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/40 backdrop-blur-sm">
          <div className="bg-white rounded-3xl w-full max-w-md overflow-hidden shadow-2xl animate-slide-up border border-slate-100">
            {/* Modal Header */}
            <div className="flex items-center justify-between px-6 py-5 border-b border-slate-100 bg-slate-50/50">
              <h3 className="text-lg font-bold text-slate-900 heading-font">
                {modalMode === 'ADD' ? 'Add Transaction' : 'Edit Transaction'}
              </h3>
              <button
                onClick={() => setIsModalOpen(false)}
                className="p-1 text-slate-400 hover:text-slate-600 rounded-lg"
              >
                <X className="w-5 h-5" />
              </button>
            </div>

            {/* Modal Form */}
            <form onSubmit={handleSaveTransaction} className="p-6 space-y-4">
              {/* Amount */}
              <div>
                <label className="block text-xs font-semibold text-slate-400 uppercase tracking-wider mb-2">
                  Amount (USD)
                </label>
                <input
                  type="number"
                  step="0.01"
                  required
                  min="0.01"
                  placeholder="0.00"
                  value={amount}
                  onChange={(e) => setAmount(e.target.value)}
                  className="block w-full px-4 py-2.5 bg-slate-50 border border-slate-200 rounded-xl text-slate-800 placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-sky-500/20 focus:border-sky-500 transition-all text-sm"
                />
              </div>

              {/* Date (Disabled in Edit Mode) */}
              <div>
                <label className="block text-xs font-semibold text-slate-400 uppercase tracking-wider mb-2">
                  Transaction Date
                </label>
                <input
                  type="date"
                  required
                  disabled={modalMode === 'EDIT'}
                  max={new Date().toISOString().split('T')[0]} // prevent future dates
                  value={date}
                  onChange={(e) => setDate(e.target.value)}
                  className="block w-full px-4 py-2.5 bg-slate-50 border border-slate-200 rounded-xl text-slate-800 focus:outline-none focus:ring-2 focus:ring-sky-500/20 focus:border-sky-500 transition-all text-sm disabled:opacity-60 disabled:cursor-not-allowed"
                />
              </div>

              {/* Category */}
              <div>
                <label className="block text-xs font-semibold text-slate-400 uppercase tracking-wider mb-2">
                  Category
                </label>
                <select
                  required
                  value={categoryName}
                  onChange={(e) => setCategoryName(e.target.value)}
                  className="block w-full px-4 py-2.5 bg-slate-50 border border-slate-200 rounded-xl text-slate-800 focus:outline-none focus:ring-2 focus:ring-sky-500/20 focus:border-sky-500 transition-all text-sm"
                >
                  {categories.map((c) => (
                    <option key={c.name} value={c.name}>{c.name} ({c.type})</option>
                  ))}
                </select>
              </div>

              {/* Description */}
              <div>
                <label className="block text-xs font-semibold text-slate-400 uppercase tracking-wider mb-2">
                  Description
                </label>
                <textarea
                  placeholder="Notes about this transaction..."
                  value={description}
                  onChange={(e) => setDescription(e.target.value)}
                  maxLength={400}
                  className="block w-full px-4 py-2.5 bg-slate-50 border border-slate-200 rounded-xl text-slate-800 placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-sky-500/20 focus:border-sky-500 transition-all text-sm h-20 resize-none"
                />
              </div>

              {/* Modal Actions */}
              <div className="flex gap-3 mt-6 pt-4 border-t border-slate-100">
                <button
                  type="button"
                  onClick={() => setIsModalOpen(false)}
                  className="flex-1 py-2.5 border border-slate-200 text-slate-600 font-semibold rounded-xl text-sm hover:bg-slate-50 transition-colors"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={isSubmitting}
                  className="flex-1 py-2.5 bg-sky-500 hover:bg-sky-400 active:bg-sky-600 text-white font-semibold rounded-xl text-sm transition-all duration-200 shadow-md shadow-sky-500/10 disabled:opacity-50 flex items-center justify-center"
                >
                  {isSubmitting ? <Loader2 className="w-5 h-5 animate-spin" /> : 'Save'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

export default Transactions;
