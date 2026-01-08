# StudentHub

StudentHub is an Android app I built to help students manage their daily academic life — classes, assignments, and focus time — with smart reminders and offline support.

The app adapts based on location and schedule, so students get the right information at the right time without being overwhelmed.

---

## Screenshots

### Home Dashboard
<p>
  <img src="./screenshots/Dashboard_empty.png" width="240" alt="Dashboard Empty" />
  <img src="./screenshots/Dashboard_filled.png" width="240" alt="Dashboard Filled" />
</p>

### Schedule
<p>
  <img src="./screenshots/schedule_week.png" width="240" alt="Weekly Timetable View" />
  <img src="./screenshots/add_class.png" width="240" alt="Add / Edit Class" />
</p>

### Assignments
<p>
  <img src="./screenshots/assignments_list.png" width="240" alt="Assignment List" />
  <img src="./screenshots/assignment_completed.png" width="240" alt="Assignment Completed" />
</p>

### Focus Mode
<p>
  <img src="./screenshots/focus_running.png" width="240" alt="Pomodoro Timer Running" />
  <img src="./screenshots/focus_completed.png" width="240" alt="Focus Session Completed" />
</p>

### Settings
<p>
  <img src="./screenshots/settings_notifications.png" width="240" alt="Notification Preferences" />
  <img src="./screenshots/settings_location.png" width="240" alt="Location / Quiet Hours Settings" />
</p>


---

## ✨ Features

### Authentication
- Email/password sign up and login
- User-specific data (each user only sees their own content)

### Dashboard
- Shows today’s classes and upcoming assignments
- Automatically adapts based on context (home vs campus)
- Works offline with automatic sync when online

### Schedule Management
- Add classes with days, time, and location
- Today and weekly views
- Countdown to next class
- Class reminders before start time

### Assignment Tracking
- Add assignments with due dates and priority
- Automatically grouped (Today, This Week, Later)
- Mark assignments complete
- Due date notifications

### Focus Mode (Pomodoro)
- Customizable focus/break timer
- Runs in the background
- Tracks completed focus sessions
- Completion notifications

### Smart Notifications
- Class reminders
- Assignment deadline alerts
- Quiet hours support
- Prevents notification spam

### Offline Support & Sync
- App works without internet
- Data stored locally using Room
- Syncs with Firebase Firestore when online

---

##  Tech Stack
- **Language:** Java
- **UI:** Android Views + XML (Material Design 3)
- **Architecture:** MVVM + Repository pattern
- **Local Storage:** Room (SQLite)
- **Authentication:** Firebase Auth
- **Database:** Firebase Firestore
- **Location:** Geofencing (Google Play Services)
- **Background Tasks:** Foreground Service, WorkManager
