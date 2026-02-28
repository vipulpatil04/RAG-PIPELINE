import { NextResponse } from "next/server";
import { auth } from "@/auth";
import { prisma } from "@/lib/prisma";

export async function PUT(
  request: Request,
  { params }: { params: Promise<{ id: string }> }
) {
  const session = await auth();
  if (!session?.user?.id) {
    return NextResponse.json({ error: "Unauthorized" }, { status: 401 });
  }

  const { id } = await params;
  const habit = await prisma.habit.findUnique({ where: { id } });
  if (!habit) {
    return NextResponse.json({ error: "Not found" }, { status: 404 });
  }
  if (habit.userId !== session.user.id && session.user.role !== "admin") {
    return NextResponse.json({ error: "Forbidden" }, { status: 403 });
  }

  const body = await request.json();
  const { name, description, color } = body;

  const updated = await prisma.habit.update({
    where: { id },
    data: {
      name: name?.trim() || habit.name,
      description: description?.trim() ?? habit.description,
      color: color || habit.color,
    },
  });

  return NextResponse.json(updated);
}

export async function DELETE(
  request: Request,
  { params }: { params: Promise<{ id: string }> }
) {
  const session = await auth();
  if (!session?.user?.id) {
    return NextResponse.json({ error: "Unauthorized" }, { status: 401 });
  }

  const { id } = await params;
  const habit = await prisma.habit.findUnique({ where: { id } });
  if (!habit) {
    return NextResponse.json({ error: "Not found" }, { status: 404 });
  }
  if (habit.userId !== session.user.id && session.user.role !== "admin") {
    return NextResponse.json({ error: "Forbidden" }, { status: 403 });
  }

  await prisma.habit.delete({ where: { id } });
  return NextResponse.json({ success: true });
}
