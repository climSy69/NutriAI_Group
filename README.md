# 🥗 NutriAI (Group Project Version)

> An Android nutrition tracking application with AI-powered insights and cloud backend.

NutriAI is a modern Android application designed to help users track their daily nutrition, monitor macronutrients, and receive AI-driven health guidance.

This version represents the enhanced **group project iteration**, building on the original individual implementation with improved UI, backend logic, and new intelligent features.

> 🎓 Developed as part of the **Advanced Topics in Computer Science (CN6008_1)** course.

---

## 📱 Features

### 🔐 User Authentication
- Secure registration & login via **Supabase Auth**
- JWT-based session handling

### 🍽️ Meal Tracking
- Add meals with calories, protein, carbs, fats
- Input validation and structured data entry

### 📊 Daily Summary
- Real-time calculation of total daily macros & calories

### 📅 Weekly Diet View
- Organized meal tracking per day

### 🤖 AI Assistant
- Integrated chat using **Google Gemini**
- Ask for meal suggestions, health advice, and nutrition explanations

### 👤 User Profile System
- Stores username, email, and personal data
- Synced with backend database

### ☁️ Cloud Integration
- All data stored in Supabase **PostgreSQL** database

### 💾 Session Persistence
- Users remain logged in across app restarts

---

## 🛠️ Tech Stack

### Frontend
- **Java** (Android SDK)
- **XML Layouts**
- **Material Design 3**
- Custom vector assets & modern UI styling

### Backend — Supabase
- Authentication (JWT)
- PostgreSQL database
- REST API
- Row Level Security (RLS)

### Networking
- **Retrofit 2**
- **OkHttp**
- **Gson**
- Custom interceptor for auth headers

### Storage
- **SharedPreferences**
- Session & user data handling

---

## 🏗️ Architecture

The app follows a **three-tier architecture**:

```
┌─────────────────────────────────────────┐
│  Presentation Layer (Android UI)        │
│  Activities + XML Layouts               │
└─────────────────┬───────────────────────┘
                  │
┌─────────────────▼───────────────────────┐
│  Logic Layer                            │
│  SupabaseClient, UserSession            │
└─────────────────┬───────────────────────┘
                  │
┌─────────────────▼───────────────────────┐
│  Data Layer (Supabase Cloud)            │
│  Auth + REST API + PostgreSQL           │
└─────────────────────────────────────────┘
```

---

## 📂 Project Structure

```
NutriAI/
├── app/
│   ├── java/com/example/.../
│   │   ├── LoginActivity.java
│   │   ├── RegisterActivity.java
│   │   ├── MainActivity.java
│   │   ├── AddMealActivity.java
│   │   ├── DailySummaryActivity.java
│   │   ├── ProfileActivity.java
│   │   ├── SupabaseClient.java
│   │   └── UserSession.java
│   │
│   ├── res/
│   │   ├── layout/
│   │   ├── drawable/
│   │   └── values/
│   │
│   └── AndroidManifest.xml
```

---

## 🗄️ Database Schema

### `profiles`

| Column      | Type   | Description                          |
|-------------|--------|--------------------------------------|
| `id`        | UUID   | Primary key (linked to `auth.users`) |
| `full_name` | TEXT   | User's full name                     |
| `email`     | TEXT   | User's email                         |
| `username`  | TEXT   | Display username                     |

### `meals`

| Column      | Type   | Description                |
|-------------|--------|----------------------------|
| `id`        | INT8   | Primary key                |
| `user_id`   | UUID   | Foreign key → `profiles.id`|
| `meal_name` | TEXT   | Meal name                  |
| `calories`  | INT4   | Calories                   |
| `protein`   | FLOAT8 | Protein                    |
| `carbs`     | FLOAT8 | Carbs                      |
| `fats`      | FLOAT8 | Fats                       |
| `day`       | TEXT   | Day                        |
| `meal_type` | TEXT   | Meal category              |

---

## 🔐 Authentication Flow

1. User registers → **Supabase Auth**
2. On success → profile row created in database
3. JWT token stored locally
4. All future requests authenticated via **Bearer token**

---

## 🎨 UI & Visual Improvements

This group version includes major UI upgrades:

- 🎨 Modern color theme (**NutriAI Green**)
- 🧊 Card-based layouts with elevation
- 🔘 Larger, more accessible buttons
- 🧾 Improved form structure (vertical alignment)
- 🎯 Clean spacing and mobile-friendly design
- 🧩 Custom vector icons (replaced private Android drawables)
- 🌈 Soft gradient backgrounds for premium feel

---

## 🐞 Important Fixes

- ✅ Fixed Supabase query issues (`user_id` vs `id` mismatch)
- ✅ Fixed profile creation bug (RLS + token issues)
- ✅ Fixed app freezes from blocking network calls
- ✅ Fixed private drawable crash (`@android:drawable/...`)
- ✅ Fixed theme/resource linking errors (`values-night`)
- ✅ Improved Retrofit query handling
- ✅ Added safe handling for empty API responses

---

## 🚀 Getting Started

### Prerequisites
- **Android Studio**
- **Android SDK 24+**
- **Supabase account**

### Installation

1. Clone the repository:
   ```bash
   git clone https://github.com/climSy69/NutriAI.git
   ```

2. Open the project in **Android Studio**

3. Add your Supabase credentials:
   ```java
   SUPABASE_URL = "your-url";
   SUPABASE_API_KEY = "your-key";
   ```

4. **Build & Run** 🎉

---

## 🧪 Testing

- ✔ Registration / Login
- ✔ Profile creation
- ✔ Meal logging
- ✔ Daily summary calculations
- ✔ Session persistence
- ✔ UI responsiveness

---

## 🔮 Future Improvements

- 📊 Charts & analytics
- ✏️ Edit/Delete meals
- 🎯 Calorie goals
- 🔔 Notifications
- 📷 Barcode scanner
- 🌙 Dark mode support
- 📦 Play Store deployment

---

## 👨‍💻 Team (Group Project)

This version was developed collaboratively by:

- **Alex Sacara**
- **Zaxarias Mosxofidis**
- **Odysseas Aligewrgas**

### Contributions include:
- Backend fixes & Supabase integration
- UI/UX improvements
- Debugging and testing

---

## 📄 License

This project was developed for **academic purposes** as part of university coursework.

---

## 💡 Final Note

This version represents a **fully functional, production-ready prototype** with:

- ✅ Real backend
- ✅ Authentication
- ✅ Polished UI
