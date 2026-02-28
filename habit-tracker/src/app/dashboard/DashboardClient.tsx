"use client";

import { useState, useEffect, useCallback } from "react";
import { Header } from "@/components/Header";
import { HabitGrid } from "@/components/HabitGrid";
import { HabitModal } from "@/components/HabitModal";
import { ViewSwitcher, ViewType } from "@/components/ViewSwitcher";

interface Habit {
  id: string;
  name: string;
  description?: string | null;
  color: string;
}

export function DashboardClient() {
  const [habits, setHabits] = useState<Habit[]>([]);
  const [view, setView] = useState<ViewType>("weekly");
  const [showModal, setShowModal] = useState(false);
  const [editingHabit, setEditingHabit] = useState<Habit | null>(null);
  const [loading, setLoading] = useState(true);

  const fetchHabits = useCallback(async () => {
    setLoading(true);
    try {
      const res = await fetch("/api/habits");
      if (res.ok) {
        const data = await res.json();
        setHabits(data);
      }
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchHabits();
  }, [fetchHabits]);

  const handleSave = (habit: Habit) => {
    setHabits((prev) => {
      const idx = prev.findIndex((h) => h.id === habit.id);
      if (idx >= 0) {
        const next = [...prev];
        next[idx] = habit;
        return next;
      }
      return [...prev, habit];
    });
    setShowModal(false);
    setEditingHabit(null);
  };

  const handleDelete = (id: string) => {
    setHabits((prev) => prev.filter((h) => h.id !== id));
  };

  const openAdd = () => {
    setEditingHabit(null);
    setShowModal(true);
  };

  const openEdit = (habit: Habit) => {
    setEditingHabit(habit);
    setShowModal(true);
  };

  return (
    <div className="min-h-screen bg-gray-50">
      <Header />
      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 mb-8">
          <div>
            <h1 className="text-2xl font-bold text-gray-900">My Habits</h1>
            <p className="text-gray-500 text-sm mt-1">
              Track your daily progress
            </p>
          </div>
          <div className="flex items-center gap-3">
            <ViewSwitcher view={view} onChange={setView} />
            <button
              onClick={openAdd}
              className="flex items-center gap-2 bg-indigo-600 hover:bg-indigo-700 text-white font-semibold px-4 py-2 rounded-xl transition-colors shadow-sm"
            >
              <span className="text-lg">+</span>
              <span>Add Habit</span>
            </button>
          </div>
        </div>

        <div className="bg-white rounded-2xl shadow-sm border border-gray-200 p-4 sm:p-6">
          {loading ? (
            <div className="text-center py-20 text-gray-400">
              <div className="text-4xl mb-4 animate-pulse">⏳</div>
              <p>Loading habits...</p>
            </div>
          ) : (
            <HabitGrid
              habits={habits}
              view={view}
              onEditHabit={openEdit}
              onDeleteHabit={handleDelete}
            />
          )}
        </div>
      </main>

      {showModal && (
        <HabitModal
          habit={editingHabit}
          onClose={() => {
            setShowModal(false);
            setEditingHabit(null);
          }}
          onSave={handleSave}
        />
      )}
    </div>
  );
}
