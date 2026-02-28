"use client";

import { useSession, signOut } from "next-auth/react";
import Link from "next/link";
import Image from "next/image";

export function Header() {
  const { data: session } = useSession();

  return (
    <header className="bg-white border-b border-gray-200 sticky top-0 z-50 shadow-sm">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex items-center justify-between h-16">
          <Link href="/dashboard" className="flex items-center gap-2 font-bold text-xl text-indigo-600">
            <span className="text-2xl">✅</span>
            <span className="hidden sm:block">Habit Tracker</span>
          </Link>
          <nav className="flex items-center gap-4">
            {session?.user?.role === "admin" && (
              <Link
                href="/admin"
                className="text-sm font-medium text-purple-600 hover:text-purple-800 bg-purple-50 px-3 py-1.5 rounded-lg"
              >
                Admin Panel
              </Link>
            )}
            <div className="flex items-center gap-3">
              {session?.user?.image && (
                <Image
                  src={session.user.image}
                  alt={session.user.name || "User"}
                  width={32}
                  height={32}
                  className="rounded-full"
                />
              )}
              <span className="text-sm text-gray-600 hidden sm:block">
                {session?.user?.name}
              </span>
              <button
                onClick={() => signOut({ callbackUrl: "/auth/signin" })}
                className="text-sm font-medium text-gray-500 hover:text-red-600 transition-colors px-3 py-1.5 rounded-lg hover:bg-red-50"
              >
                Sign out
              </button>
            </div>
          </nav>
        </div>
      </div>
    </header>
  );
}
