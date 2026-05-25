import React, { useState, useEffect } from 'react';
import api from '../services/api';
import { 
  Plus, 
  Trash2, 
  Edit2, 
  X,
  Target,
  Calendar,
  DollarSign,
  Loader2,
  AlertCircle
} from 'lucide-react';
import { toast } from 'react-hot-toast';

const SavingsGoals = () => {
  const [goals, setGoals] = useState([]);
  const [loading, setLoading] = useState(true);

  // Modal control
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [modalMode, setModalMode] = useState('ADD'); // 'ADD' or 'EDIT'
  const [selectedGoal, setSelectedGoal] = useState(null);

  // Form input state
  const [goalName, setGoalName] = useState('');
  const [targetAmount, setTargetAmount] = useState('');
  const [targetDate, setTargetDate] = useState('');
  const [startDate, setStartDate] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);

  const fetchGoals = async () => {
    try {
      setLoading(true);
      const response = await api.get('/goals');
      setGoals(response.data.goals || []);
    } catch (err) {
      console.error('Error fetching savings goals:', err);
      toast.error('Failed to load savings goals');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchGoals();
  }, []);

  const handleOpenAddModal = () => {
    setModalMode('ADD');
    setSelectedGoal(null);
    setGoalName('');
    setTargetAmount('');
    
    // Default dates: start is today, target is next year
    const todayStr = new Date().toISOString().split('T')[0];
    const nextYear = new Date();
    nextYear.setFullYear(nextYear.getFullYear() + 1);
    const nextYearStr = nextYear.toISOString().split('T')[0];

    setStartDate(todayStr);
    setTargetDate(nextYearStr);
    setIsModalOpen(true);
  };

  const handleOpenEditModal = (goal) => {
    setModalMode('EDIT');
    setSelectedGoal(goal);
    setGoalName(goal.goalName);
    setTargetAmount(goal.targetAmount);
    setTargetDate(goal.targetDate);
    setStartDate(goal.startDate);
    setIsModalOpen(true);
  };

  const handleSaveGoal = async (e) => {
    e.preventDefault();
    if (!goalName.trim()) {
      toast.error('Goal name is required');
      return;
    }
    if (!targetAmount || parseFloat(targetAmount) <= 0) {
      toast.error('Target amount must be positive');
      return;
    }
    if (!targetDate || new Date(targetDate) < new Date(new Date().setHours(0,0,0,0))) {
      toast.error('Target date must be today or in the future');
      return;
    }
    if (!startDate) {
      toast.error('Start date is required');
      return;
    }

    setIsSubmitting(true);
    try {
      if (modalMode === 'ADD') {
        await api.post('/goals', {
          goalName: goalName.trim(),
          targetAmount: parseFloat(targetAmount),
          targetDate,
          startDate
        });
        toast.success('Savings goal set up successfully!');
      } else {
        await api.put(`/goals/${selectedGoal.id}`, {
          goalName: goalName.trim(),
          targetAmount: parseFloat(targetAmount),
          targetDate,
          startDate
        });
        toast.success('Savings goal updated!');
      }
      setIsModalOpen(false);
      fetchGoals();
    } catch (err) {
      toast.error(err.response?.data?.message || 'Action failed');
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleDelete = async (id) => {
    if (!window.confirm('Are you sure you want to delete this savings goal?')) {
      return;
    }

    try {
      await api.delete(`/goals/${id}`);
      toast.success('Savings goal deleted successfully!');
      fetchGoals();
    } catch (err) {
      toast.error(err.response?.data?.message || 'Deletion failed');
    }
  };

  const formatCurrency = (val) => {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD'
    }).format(val || 0);
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-[60vh]">
        <div className="flex flex-col items-center gap-3">
          <Loader2 className="w-10 h-10 text-sky-500 animate-spin" />
          <p className="text-slate-500 text-sm font-medium">Fetching savings goals...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="space-y-8 animate-slide-up">
      {/* Header */}
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div>
          <h1 className="text-3xl font-bold text-slate-900 heading-font">Savings Goals</h1>
          <p className="text-slate-500 mt-1">Track and build targets using your monthly cash flow.</p>
        </div>
        <button
          onClick={handleOpenAddModal}
          className="flex items-center gap-2 px-4 py-2.5 bg-sky-500 hover:bg-sky-400 active:bg-sky-600 text-white font-medium rounded-xl text-sm transition-all duration-200 shadow-md shadow-sky-500/10 w-fit"
        >
          <Plus className="w-4 h-4" />
          <span>New Savings Goal</span>
        </button>
      </div>

      {/* Info card */}
      <div className="bg-sky-50/40 border border-sky-100 rounded-3xl p-5 flex items-start gap-4">
        <AlertCircle className="w-6 h-6 text-sky-600 flex-shrink-0 mt-0.5" />
        <div>
          <h4 className="font-semibold text-sm text-sky-900 leading-tight">How is progress calculated?</h4>
          <p className="text-xs text-sky-700/80 mt-1">
            Progress is automatically computed based on your actual net savings (Total Income - Total Expenses) since each goal's start date.
          </p>
        </div>
      </div>

      {/* Grid of goals */}
      {goals.length > 0 ? (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {goals.map((goal) => {
            const pct = Math.min(Math.max(parseFloat(goal.progressPercentage), 0), 100);
            return (
              <div 
                key={goal.id} 
                className="bg-white border border-slate-100 rounded-3xl p-6 shadow-sm flex flex-col justify-between"
              >
                <div>
                  {/* Top Bar */}
                  <div className="flex items-start justify-between mb-4">
                    <div className="flex items-center justify-center w-10 h-10 rounded-xl bg-sky-50 text-sky-600 flex-shrink-0">
                      <Target className="w-5 h-5" />
                    </div>
                    <div className="flex items-center gap-1">
                      <button
                        onClick={() => handleOpenEditModal(goal)}
                        className="p-1.5 text-slate-400 hover:text-sky-500 hover:bg-sky-50 rounded-lg transition-all"
                        title="Edit goal"
                      >
                        <Edit2 className="w-4 h-4" />
                      </button>
                      <button
                        onClick={() => handleDelete(goal.id)}
                        className="p-1.5 text-slate-400 hover:text-rose-500 hover:bg-rose-50 rounded-lg transition-all"
                        title="Delete goal"
                      >
                        <Trash2 className="w-4 h-4" />
                      </button>
                    </div>
                  </div>

                  {/* Name */}
                  <h3 className="text-base font-bold text-slate-800 truncate mb-1 heading-font">{goal.goalName}</h3>
                  <div className="flex items-center gap-3 text-xs text-slate-400 font-medium mb-5">
                    <span className="flex items-center gap-1">
                      <Calendar className="w-3.5 h-3.5" />
                      {goal.startDate}
                    </span>
                    <span>to</span>
                    <span className="flex items-center gap-1">
                      <Calendar className="w-3.5 h-3.5" />
                      {goal.targetDate}
                    </span>
                  </div>

                  {/* Progress bar */}
                  <div className="space-y-1.5">
                    <div className="flex justify-between items-center text-sm font-semibold text-slate-700">
                      <span>Progress</span>
                      <span className="font-bold text-slate-900 heading-font">{pct.toFixed(0)}%</span>
                    </div>
                    <div className="w-full h-2.5 bg-slate-100 rounded-full overflow-hidden">
                      <div 
                        className="h-full bg-sky-500 rounded-full transition-all duration-500" 
                        style={{ width: `${pct}%` }} 
                      />
                    </div>
                  </div>
                </div>

                {/* Progress Details */}
                <div className="grid grid-cols-2 gap-4 mt-6 pt-5 border-t border-slate-100 text-xs font-semibold">
                  <div>
                    <span className="text-slate-400 block font-normal">Saved</span>
                    <span className="text-slate-800 text-sm font-bold mt-0.5 block heading-font">{formatCurrency(goal.currentProgress)}</span>
                  </div>
                  <div className="text-right">
                    <span className="text-slate-400 block font-normal">Remaining</span>
                    <span className="text-slate-800 text-sm font-bold mt-0.5 block heading-font">
                      {parseFloat(goal.remainingAmount) <= 0 ? 'Goal Met!' : formatCurrency(goal.remainingAmount)}
                    </span>
                  </div>
                </div>
              </div>
            );
          })}
        </div>
      ) : (
        <div className="bg-white border border-slate-100 rounded-3xl py-20 text-center text-slate-400 text-sm shadow-sm">
          You haven't configured any savings goals yet.
        </div>
      )}

      {/* Savings Goal Modal */}
      {isModalOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/40 backdrop-blur-sm">
          <div className="bg-white rounded-3xl w-full max-w-md overflow-hidden shadow-2xl animate-slide-up border border-slate-100">
            {/* Modal Header */}
            <div className="flex items-center justify-between px-6 py-5 border-b border-slate-100 bg-slate-50/50">
              <h3 className="text-lg font-bold text-slate-900 heading-font">
                {modalMode === 'ADD' ? 'Create Savings Goal' : 'Edit Savings Goal'}
              </h3>
              <button
                onClick={() => setIsModalOpen(false)}
                className="p-1 text-slate-400 hover:text-slate-600 rounded-lg"
              >
                <X className="w-5 h-5" />
              </button>
            </div>

            {/* Form */}
            <form onSubmit={handleSaveGoal} className="p-6 space-y-4">
              {/* Goal Name */}
              <div>
                <label className="block text-xs font-semibold text-slate-400 uppercase tracking-wider mb-2">
                  Goal Name
                </label>
                <input
                  type="text"
                  required
                  placeholder="e.g. Vacation Fund"
                  value={goalName}
                  onChange={(e) => setGoalName(e.target.value)}
                  className="block w-full px-4 py-2.5 bg-slate-50 border border-slate-200 rounded-xl text-slate-800 placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-sky-500/20 focus:border-sky-500 transition-all text-sm"
                />
              </div>

              {/* Target Amount */}
              <div>
                <label className="block text-xs font-semibold text-slate-400 uppercase tracking-wider mb-2">
                  Target Amount (USD)
                </label>
                <input
                  type="number"
                  step="0.01"
                  min="0.01"
                  required
                  placeholder="0.00"
                  value={targetAmount}
                  onChange={(e) => setTargetAmount(e.target.value)}
                  className="block w-full px-4 py-2.5 bg-slate-50 border border-slate-200 rounded-xl text-slate-800 placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-sky-500/20 focus:border-sky-500 transition-all text-sm"
                />
              </div>

              {/* Start Date */}
              <div>
                <label className="block text-xs font-semibold text-slate-400 uppercase tracking-wider mb-2">
                  Start Date
                </label>
                <input
                  type="date"
                  required
                  value={startDate}
                  onChange={(e) => setStartDate(e.target.value)}
                  className="block w-full px-4 py-2.5 bg-slate-50 border border-slate-200 rounded-xl text-slate-800 focus:outline-none focus:ring-2 focus:ring-sky-500/20 focus:border-sky-500 transition-all text-sm"
                />
              </div>

              {/* Target Date */}
              <div>
                <label className="block text-xs font-semibold text-slate-400 uppercase tracking-wider mb-2">
                  Target Date
                </label>
                <input
                  type="date"
                  required
                  min={new Date().toISOString().split('T')[0]} // today or future
                  value={targetDate}
                  onChange={(e) => setTargetDate(e.target.value)}
                  className="block w-full px-4 py-2.5 bg-slate-50 border border-slate-200 rounded-xl text-slate-800 focus:outline-none focus:ring-2 focus:ring-sky-500/20 focus:border-sky-500 transition-all text-sm"
                />
              </div>

              {/* Form Buttons */}
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

export default SavingsGoals;
