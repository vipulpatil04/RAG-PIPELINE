import NextAuth from "next-auth";
import Google from "next-auth/providers/google";
import { PrismaAdapter } from "@auth/prisma-adapter";
import { prisma } from "@/lib/prisma";

export const { handlers, auth, signIn, signOut } = NextAuth({
  adapter: PrismaAdapter(prisma),
  providers: [
    Google({
      clientId: process.env.AUTH_GOOGLE_ID ?? (() => { throw new Error("AUTH_GOOGLE_ID is not set") })(),
      clientSecret: process.env.AUTH_GOOGLE_SECRET ?? (() => { throw new Error("AUTH_GOOGLE_SECRET is not set") })(),
    }),
  ],
  callbacks: {
    async session({ session, user }) {
      if (session.user) {
        session.user.id = user.id;
        session.user.role = (user as { role?: string }).role ?? "user";
      }
      return session;
    },
  },
  pages: {
    signIn: "/auth/signin",
  },
});
