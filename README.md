# Chatzz Setup Instructions

Follow these steps to get Chatzz up and running.

## 1. Supabase Backend Setup
1. Create a new project at [supabase.com](https://supabase.com).
2. Go to the **SQL Editor** and run the contents of [schema.sql](file:///c:/Users/sanky/Downloads/chatzzy/supabase/schema.sql).
3. Enable **Realtime** for `messages`, `chats`, and `users` tables in the Supabase Dashboard (Database -> Replication -> supabase_realtime).
4. Go to **Authentication -> Providers** and enable **Email OTP**.

## 2. Firebase Cloud Messaging (FCM) Setup
1. Create a project in the [Firebase Console](https://console.firebase.google.com/).
2. Add an Android App with package name `com.chatzz`.
3. Download `google-services.json` and place it in the `app/` directory of this project.
4. Enable **Cloud Messaging** API in the Google Cloud Console if needed.

## 3. Configure Android App
1. Open [SupabaseInstance.kt](file:///c:/Users/sanky/Downloads/chatzzy/app/src/main/java/com/chatzz/data/SupabaseInstance.kt).
2. Replace `YOUR_SUPABASE_URL` and `YOUR_SUPABASE_ANON_KEY` with your project credentials from **Project Settings -> API**.

## 4. Build and Run
1. Open the project in **Android Studio**.
2. Wait for Gradle sync to complete.
3. Run the app on an emulator or physical device.

## Key Features Implemented:
- **Auth**: Email OTP via Supabase Gotrue.
- **Messaging**: Real-time 1:1 chat using Supabase Realtime.
- **Storage**: Infrastructure for avatars (ready to use).
- **Push**: Integrated with Firebase Messaging.
- **Theme**: WhatsApp-style UI with Jetpack Compose.
- **Delete**: Option to delete chats included.
