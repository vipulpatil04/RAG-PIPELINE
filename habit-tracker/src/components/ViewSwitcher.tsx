"use client";

export type ViewType = "weekly" | "monthly" | "yearly";

interface Props {
  view: ViewType;
  onChange: (v: ViewType) => void;
}

export function ViewSwitcher({ view, onChange }: Props) {
  const views: { key: ViewType; label: string }[] = [
    { key: "weekly", label: "Week" },
    { key: "monthly", label: "Month" },
    { key: "yearly", label: "Year" },
  ];

  return (
    <div className="flex bg-gray-100 rounded-xl p-1 gap-1">
      {views.map(({ key, label }) => (
        <button
          key={key}
          onClick={() => onChange(key)}
          className={`px-4 py-2 rounded-lg text-sm font-medium transition-all ${
            view === key
              ? "bg-white text-indigo-600 shadow-sm"
              : "text-gray-500 hover:text-gray-700"
          }`}
        >
          {label}
        </button>
      ))}
    </div>
  );
}
