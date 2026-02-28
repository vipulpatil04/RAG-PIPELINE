import { NextResponse } from "next/server";
import { auth } from "@/auth";
import { prisma } from "@/lib/prisma";

export async function GET(request: Request) {
  const session = await auth();
  if (!session?.user?.id) {
    return NextResponse.json({ error: "Unauthorized" }, { status: 401 });
  }

  const { searchParams } = new URL(request.url);
  const startDate = searchParams.get("startDate");
  const endDate = searchParams.get("endDate");

  const where: {
    userId: string;
    date?: { gte: string; lte: string };
  } = { userId: session.user.id };

  if (startDate && endDate) {
    where.date = { gte: startDate, lte: endDate };
  }

  const logs = await prisma.habitLog.findMany({ where });
  return NextResponse.json(logs);
}

export async function POST(request: Request) {
  const session = await auth();
  if (!session?.user?.id) {
    return NextResponse.json({ error: "Unauthorized" }, { status: 401 });
  }

  const body = await request.json();
  const { habitId, date } = body;

  if (!habitId || !date) {
    return NextResponse.json({ error: "habitId and date are required" }, { status: 400 });
  }

  const habit = await prisma.habit.findUnique({ where: { id: habitId } });
  if (!habit || habit.userId !== session.user.id) {
    return NextResponse.json({ error: "Forbidden" }, { status: 403 });
  }

  const existing = await prisma.habitLog.findUnique({
    where: { habitId_date: { habitId, date } },
  });

  if (existing) {
    const updated = await prisma.habitLog.update({
      where: { id: existing.id },
      data: { completed: !existing.completed },
    });
    return NextResponse.json(updated);
  } else {
    const log = await prisma.habitLog.create({
      data: { habitId, date, userId: session.user.id, completed: true },
    });
    return NextResponse.json(log, { status: 201 });
  }
}
