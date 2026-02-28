import { auth } from "@/auth";
import { redirect } from "next/navigation";
import { prisma } from "@/lib/prisma";
import { Header } from "@/components/Header";
import Link from "next/link";

export default async function AdminPage() {
  const session = await auth();
  if (!session) redirect("/auth/signin");
  if (session.user.role !== "admin") redirect("/dashboard");

  const users = await prisma.user.findMany({
    include: {
      habits: {
        include: {
          logs: true,
        },
      },
    },
    orderBy: { createdAt: "asc" },
  });

  return (
    <div className="min-h-screen bg-gray-50">
      <Header />
      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div className="mb-8 flex items-center justify-between">
          <div>
            <h1 className="text-2xl font-bold text-gray-900">Admin Panel</h1>
            <p className="text-gray-500 text-sm mt-1">Manage all users and habits</p>
          </div>
          <Link
            href="/dashboard"
            className="text-sm text-indigo-600 hover:underline"
          >
            ← Back to Dashboard
          </Link>
        </div>

        <div className="bg-white rounded-2xl shadow-sm border border-gray-200 overflow-hidden">
          <div className="px-6 py-4 border-b border-gray-200 bg-gray-50">
            <h2 className="font-semibold text-gray-700">
              Users ({users.length})
            </h2>
          </div>
          <div className="divide-y divide-gray-100">
            {users.map((user) => {
              const totalLogs = user.habits.reduce(
                (sum, h) => sum + h.logs.filter((l) => l.completed).length,
                0
              );
              return (
                <div key={user.id} className="px-6 py-4">
                  <div className="flex items-start justify-between gap-4">
                    <div className="flex items-center gap-3">
                      {user.image && (
                        // eslint-disable-next-line @next/next/no-img-element
                        <img
                          src={user.image}
                          alt={user.name || "User"}
                          className="w-10 h-10 rounded-full"
                        />
                      )}
                      <div>
                        <div className="flex items-center gap-2">
                          <span className="font-semibold text-gray-900">
                            {user.name}
                          </span>
                          <span
                            className={`text-xs px-2 py-0.5 rounded-full font-medium ${
                              user.role === "admin"
                                ? "bg-purple-100 text-purple-700"
                                : "bg-gray-100 text-gray-600"
                            }`}
                          >
                            {user.role}
                          </span>
                        </div>
                        <div className="text-sm text-gray-500">{user.email}</div>
                      </div>
                    </div>
                    <div className="text-right text-sm text-gray-500">
                      <div>{user.habits.length} habits</div>
                      <div>{totalLogs} completions</div>
                    </div>
                  </div>
                  {user.habits.length > 0 && (
                    <div className="mt-3 flex flex-wrap gap-2 ml-[3.25rem]">
                      {user.habits.map((habit) => (
                        <span
                          key={habit.id}
                          className="inline-flex items-center gap-1.5 text-xs px-2.5 py-1 rounded-full text-white font-medium"
                          style={{ backgroundColor: habit.color }}
                        >
                          {habit.name}
                          <span className="opacity-75">
                            ({habit.logs.filter((l) => l.completed).length})
                          </span>
                        </span>
                      ))}
                    </div>
                  )}
                </div>
              );
            })}
          </div>
        </div>
      </main>
    </div>
  );
}
