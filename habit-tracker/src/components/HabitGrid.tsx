"use client";

import { useState, useEffect, useCallback } from "react";
import type { ViewType } from "./ViewSwitcher";

interface Habit {
  id: string;
  name: string;
  description?: string | null;
  color: string;
}

interface HabitLog {
  id: string;
  habitId: string;
  date: string;
  completed: boolean;
}

interface Props {
  habits: Habit[];
  view: ViewType;
  onEditHabit: (habit: Habit) => void;
  onDeleteHabit: (id: string) => void;
}

function formatDate(date: Date): string {
  return date.toISOString().split("T")[0];
}

function getDates(view: ViewType): string[] {
  const today = new Date();
  today.setHours(0, 0, 0, 0);
  const dates: string[] = [];

  if (view === "weekly") {
    for (let i = 6; i >= 0; i--) {
      const d = new Date(today);
      d.setDate(d.getDate() - i);
      dates.push(formatDate(d));
    }
  } else if (view === "monthly") {
    for (let i = 29; i >= 0; i--) {
      const d = new Date(today);
      d.setDate(d.getDate() - i);
      dates.push(formatDate(d));
    }
  } else {
    for (let i = 364; i >= 0; i--) {
      const d = new Date(today);
      d.setDate(d.getDate() - i);
      dates.push(formatDate(d));
    }
  }
  return dates;
}

function getDayLabel(dateStr: string, view: ViewType): string {
  const date = new Date(dateStr + "T00:00:00");
  if (view === "weekly") {
    return date.toLocaleDateString("en-US", { weekday: "short", month: "short", day: "numeric" });
  }
  return date.toLocaleDateString("en-US", { month: "short", day: "numeric" });
}

function groupByMonth(dates: string[]): Record<string, string[]> {
  const groups: Record<string, string[]> = {};
  for (const d of dates) {
    const key = d.slice(0, 7);
    if (!groups[key]) groups[key] = [];
    groups[key].push(d);
  }
  return groups;
}

export function HabitGrid({ habits, view, onEditHabit, onDeleteHabit }: Props) {
  const [logs, setLogs] = useState<HabitLog[]>([]);
  const [toggling, setToggling] = useState<string>("");
  const [loading, setLoading] = useState(true);

  const dates = getDates(view);
  const startDate = dates[0];
  const endDate = dates[dates.length - 1];

  const fetchLogs = useCallback(async () => {
    setLoading(true);
    try {
      const res = await fetch(`/api/logs?startDate=${startDate}&endDate=${endDate}`);
      if (res.ok) {
        const data = await res.json();
        setLogs(data);
      }
    } finally {
      setLoading(false);
    }
  }, [startDate, endDate]);

  useEffect(() => {
    fetchLogs();
  }, [fetchLogs]);

  const isCompleted = (habitId: string, date: string) =>
    logs.some((l) => l.habitId === habitId && l.date === date && l.completed);

  const toggleLog = async (habitId: string, date: string) => {
    const key = `${habitId}-${date}`;
    if (toggling) return;
    setToggling(key);
    try {
      const res = await fetch("/api/logs", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ habitId, date }),
      });
      if (res.ok) {
        const updated = await res.json();
        setLogs((prev) => {
          const filtered = prev.filter(
            (l) => !(l.habitId === habitId && l.date === date)
          );
          return [...filtered, updated];
        });
      }
    } finally {
      setToggling("");
    }
  };

  const handleDelete = async (id: string) => {
    if (!confirm("Delete this habit and all its logs?")) return;
    const res = await fetch(`/api/habits/${id}`, { method: "DELETE" });
    if (res.ok) onDeleteHabit(id);
  };

  if (habits.length === 0) {
    return (
      <div className="text-center py-20 text-gray-400">
        <div className="text-6xl mb-4">📋</div>
        <p className="text-xl font-medium">No habits yet</p>
        <p className="text-sm mt-1">Click &quot;Add Habit&quot; to get started</p>
      </div>
    );
  }

  if (loading) {
    return (
      <div className="text-center py-20 text-gray-400">
        <div className="animate-spin text-4xl mb-4">⏳</div>
        <p>Loading...</p>
      </div>
    );
  }

  // Yearly view = heatmap
  if (view === "yearly") {
    const monthGroups = groupByMonth(dates);
    return (
      <div className="overflow-x-auto">
        <div className="space-y-8 min-w-max">
          {habits.map((habit) => {
            const total = dates.filter((d) => isCompleted(habit.id, d)).length;
            const pct = Math.round((total / dates.length) * 100);
            return (
              <div key={habit.id} className="space-y-2">
                <div className="flex items-center gap-3">
                  <div
                    className="w-3 h-3 rounded-full flex-shrink-0"
                    style={{ backgroundColor: habit.color }}
                  />
                  <span className="font-semibold text-gray-800">{habit.name}</span>
                  <span className="text-sm text-gray-400">
                    {total}/{dates.length} days ({pct}%)
                  </span>
                  <div className="ml-auto flex gap-2">
                    <button
                      onClick={() => onEditHabit(habit)}
                      className="text-xs text-gray-400 hover:text-indigo-600 px-2 py-1 rounded hover:bg-indigo-50"
                    >
                      Edit
                    </button>
                    <button
                      onClick={() => handleDelete(habit.id)}
                      className="text-xs text-gray-400 hover:text-red-600 px-2 py-1 rounded hover:bg-red-50"
                    >
                      Delete
                    </button>
                  </div>
                </div>
                <div className="flex gap-3 flex-wrap">
                  {Object.entries(monthGroups).map(([month, mDates]) => {
                    const monthDate = new Date(month + "-01T00:00:00");
                    const monthLabel = monthDate.toLocaleDateString("en-US", { month: "short", year: "2-digit" });
                    return (
                      <div key={month} className="space-y-1">
                        <div className="text-xs text-gray-400 text-center">{monthLabel}</div>
                        <div className="grid grid-cols-7 gap-0.5">
                          {mDates.map((d) => {
                            const done = isCompleted(habit.id, d);
                            return (
                              <button
                                key={d}
                                title={d}
                                onClick={() => toggleLog(habit.id, d)}
                                className="w-3.5 h-3.5 rounded-sm transition-colors hover:opacity-80"
                                style={{
                                  backgroundColor: done ? habit.color : "#e5e7eb",
                                }}
                              />
                            );
                          })}
                        </div>
                      </div>
                    );
                  })}
                </div>
              </div>
            );
          })}
        </div>
      </div>
    );
  }

  // Weekly / Monthly view = table (rows = dates, cols = habits)
  return (
    <div className="overflow-x-auto">
      <table className="w-full border-collapse">
        <thead>
          <tr className="border-b-2 border-gray-200">
            <th className="text-left py-3 px-4 text-sm font-semibold text-gray-500 w-32">
              Date
            </th>
            {habits.map((habit) => (
              <th key={habit.id} className="py-3 px-2 min-w-[100px]">
                <div className="flex flex-col items-center gap-1">
                  <div
                    className="w-3 h-3 rounded-full"
                    style={{ backgroundColor: habit.color }}
                  />
                  <span className="text-xs font-semibold text-gray-700 truncate max-w-[90px]">
                    {habit.name}
                  </span>
                  <div className="flex gap-1">
                    <button
                      onClick={() => onEditHabit(habit)}
                      className="text-xs text-gray-300 hover:text-indigo-500"
                      title="Edit"
                    >
                      ✏️
                    </button>
                    <button
                      onClick={() => handleDelete(habit.id)}
                      className="text-xs text-gray-300 hover:text-red-500"
                      title="Delete"
                    >
                      🗑️
                    </button>
                  </div>
                </div>
              </th>
            ))}
          </tr>
        </thead>
        <tbody>
          {dates.map((date, i) => {
            const isToday = date === formatDate(new Date());
            return (
              <tr
                key={date}
                className={`border-b border-gray-100 hover:bg-gray-50 transition-colors ${
                  isToday ? "bg-indigo-50" : i % 2 === 0 ? "bg-white" : "bg-gray-50/30"
                }`}
              >
                <td className="py-2.5 px-4 text-sm text-gray-600 font-medium whitespace-nowrap">
                  {isToday && (
                    <span className="text-xs bg-indigo-500 text-white rounded-full px-2 py-0.5 mr-1">
                      Today
                    </span>
                  )}
                  {getDayLabel(date, view)}
                </td>
                {habits.map((habit) => {
                  const done = isCompleted(habit.id, date);
                  const key = `${habit.id}-${date}`;
                  return (
                    <td key={habit.id} className="py-2.5 px-2 text-center">
                      <button
                        onClick={() => toggleLog(habit.id, date)}
                        disabled={toggling === key}
                        className={`w-7 h-7 rounded-full border-2 transition-all hover:scale-110 disabled:cursor-wait ${
                          done
                            ? "border-transparent"
                            : "border-gray-300 hover:border-gray-400 bg-white"
                        }`}
                        style={done ? { backgroundColor: habit.color } : {}}
                        title={done ? "Mark incomplete" : "Mark complete"}
                      >
                        {done && (
                          <svg className="w-3 h-3 mx-auto text-white" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={3}>
                            <path strokeLinecap="round" strokeLinejoin="round" d="M5 13l4 4L19 7" />
                          </svg>
                        )}
                      </button>
                    </td>
                  );
                })}
              </tr>
            );
          })}
        </tbody>
      </table>
    </div>
  );
}
