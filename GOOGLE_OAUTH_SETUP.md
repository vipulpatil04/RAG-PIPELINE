# How to Generate Google OAuth Credentials

This guide walks you through creating a **Google Client ID** and **Client Secret** for the Habit Tracker app (or any app that uses Google Sign-In).

---

## Prerequisites

- A Google account
- Access to [Google Cloud Console](https://console.cloud.google.com/)

---

## Step-by-Step Instructions

### 1. Go to Google Cloud Console

Open your browser and navigate to:
👉 **https://console.cloud.google.com/**

Sign in with your Google account if prompted.

---

### 2. Create a New Project

1. Click the **project dropdown** at the top-left of the page (next to the Google Cloud logo).
2. Click **"New Project"**.
3. Enter a **Project name** (e.g., `habit-tracker`).
4. Click **"Create"**.
5. Wait a few seconds, then make sure the new project is selected in the dropdown.

---

### 3. Enable the Google+ API (OAuth prerequisite)

1. In the left sidebar, go to **APIs & Services → Library**.
2. Search for **"Google+ API"** or **"Google Identity"**.
3. Click on **Google+ API** and press **"Enable"**.

> ℹ️ If you don't see it, you can skip this step — the OAuth consent screen setup in the next step will trigger enablement automatically.

---

### 4. Configure the OAuth Consent Screen

1. In the left sidebar, go to **APIs & Services → OAuth consent screen**.
2. Choose **"External"** (so any Google account can sign in) and click **"Create"**.
3. Fill in the required fields:
   - **App name** — e.g., `Habit Tracker`
   - **User support email** — your Gmail address
   - **Developer contact information** — your Gmail address
4. Click **"Save and Continue"** through the **Scopes** and **Test users** steps (you can leave defaults).
5. On the **Summary** page, click **"Back to Dashboard"**.

---

### 5. Create OAuth 2.0 Credentials

1. In the left sidebar, go to **APIs & Services → Credentials**.
2. Click **"+ Create Credentials"** at the top.
3. Select **"OAuth 2.0 Client IDs"**.
4. Under **Application type**, choose **"Web application"**.
5. Give it a name (e.g., `Habit Tracker Web Client`).
6. Under **Authorized redirect URIs**, click **"+ Add URI"** and enter:
   ```
   http://localhost:3000/api/auth/callback/google
   ```
   > For production deployment, also add your live URL, e.g.:
   > `https://your-app.vercel.app/api/auth/callback/google`
7. Click **"Create"**.

---

### 6. Copy Your Credentials

A popup will appear showing:
- **Your Client ID** — looks like `123456789-abc...apps.googleusercontent.com`
- **Your Client Secret** — looks like `GOCSPX-...`

Copy both values and add them to your **`.env.local`** file:

```env
AUTH_GOOGLE_ID="paste-your-client-id-here"
AUTH_GOOGLE_SECRET="paste-your-client-secret-here"
```

> 🔒 **Never commit `.env.local` to Git.** Make sure it is listed in your `.gitignore`.

---

## Complete `.env.local` Template

```env
DATABASE_URL="file:./dev.db"
NEXTAUTH_SECRET="run: openssl rand -base64 32"
NEXTAUTH_URL="http://localhost:3000"
AUTH_GOOGLE_ID="your-google-client-id"
AUTH_GOOGLE_SECRET="your-google-client-secret"
```

Generate a strong `NEXTAUTH_SECRET` by running:
```bash
openssl rand -base64 32
```

---

## Troubleshooting

| Problem | Fix |
|---|---|
| `redirect_uri_mismatch` error | Make sure the redirect URI in Google Console **exactly** matches `http://localhost:3000/api/auth/callback/google` (no trailing slash) |
| `Access blocked: app not verified` | In OAuth consent screen, add your Google account as a **Test user** |
| Credentials not showing after creation | Go to **APIs & Services → Credentials** and click the pencil icon next to your client to view them again |
| `invalid_client` error | Double-check that you copied the Client ID and Secret without extra spaces |

---

## Resources

- [Google Cloud Console](https://console.cloud.google.com/)
- [Google OAuth 2.0 Documentation](https://developers.google.com/identity/protocols/oauth2)
- [NextAuth.js Google Provider Docs](https://next-auth.js.org/providers/google)
