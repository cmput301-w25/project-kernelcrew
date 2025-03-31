# CMPUT 301 W25 - Team Kernal Crew

When seeting up locally, follow the [Getting Started Instructions](./docs/getting-started.md).

For details about CI, see the [CI Docs](./docs/ci.md).

## Team Members

| **Full Name**       | **CCID**  | **@username**      |
|---------------------|-----------|--------------------|
| Aditya Patel        | aditydi   | adityadipakpatel   |
| Taha Kamil          | tkamil    | TahaKamil          |
| Anton Roupassov-Ruiz| roupasso  | antonrou           |
| Aidan Olsen         | arolsen   | PossiblyAShrub     |
| Siddhant Goel       | sgoel3    | siddhantgoel7      |
| James Mckinnon      | jam10     | jamesMckinnonSchool|

## Project Description

### 📱 Mood App

This project was developed as part of the CMPUT 301 Mobile Application Development course at the University of Alberta in Winter 2025. Our goal was to create a polished and user-friendly Mood Tracking App that allows users to log and reflect on their emotional states.

## ✨ Wow Factor Features

We went above and beyond the project requirements by adding two major "wow" features:

#### 🗺️ Interactive Mini Map
- When creating or editing a mood, a **mini Google Map** appears.
- Users can **manually select a location** by tapping and moving a pin.
- In **Mood Details** and the **Global Mood Map**, a pin appears on the map, styled to match the emotion (e.g., 😊 for happy).
- The experience ties emotional states to real-world places — clean, immersive, and intuitive.

---

#### 🔔 Real-Time Notifications
- Users receive notifications when someone **requests to follow them**.
- This fosters interaction and adds a social touch that encourages engagement.

## 🛠️ Key Features

We implemented all the core features required by the user stories.

#### 🔐 Authentication
- Sign up using **email and password** and choose a **username** others will see you by via Firebase Authentication.
- Sign in using email and password
- Ensures secure access and session persistence across devices.

---

#### 📝 Create Mood
- Select from preset emotions (e.g., Happy, Sad, Angry).
- Add a reason.
- Choose a social situation (Alone, One Person, Group).
- Attach an optional photo.
- Pick a location on the map.
- Set visibility: **Public** or **Private**.

---

#### ✏️ Edit Mood
- Edit emotion, reason, photo, location, or visibility.

---

#### 🏠 Home Feed
- View your moods and those from users you follow.
- Displayed emotions can be filtered by **date posted** (today, this week, this month, all time), by **emotion** (anger, confusion, disgust, etc.), and by **location** (within 5km or 10km).
- Cards show emotion, time and date, and username.

---

#### 🗺️ Map View
- Explore public moods on a real-time Google map.
- Mood pins use emotion emojis.
- Tap a pin to view full mood details.
- Filter by emotion, keyword, or date.

---

#### 🔍 Filtering & Search
- Filter moods by:
  - Emotion
  - Keyword in reason
  - Date or date range

---

#### 💬 Comments
- Leave comments on public moods.
- See threaded comment discussions.
- Private moods have commenting disabled.

---

#### 👥 Following System
- Search users and send follow requests.
- Accept or decline incoming requests.
- Private moods are visible only to accepted followers.
- Notifications appear when someone requests to follow you.

---

#### 📶 Offline Mode
- Full offline support.
- Create/edit/delete moods offline.
- Automatic syncing with Firebase when back online.

## Setup Instructions

Follow the [Getting Started Instructions](./docs/getting-started.md).

## Documentation

- [Wiki Link](https://github.com/cmput301-w25/project-kernelcrew/wiki)
- [Scrum Board](https://github.com/orgs/cmput301-w25/projects/65/views/1)
- [UI Mockups](https://github.com/cmput301-w25/project-kernelcrew/wiki/UI-Mockups-Project-Part-4)
- [JavaDocs](https://cmput301-w25.github.io/project-kernelcrew/)
- [UML](https://github.com/cmput301-w25/project-kernelcrew/wiki/UML-for-Project-Part-4)

 ## 🔑 Tech Stack

- Java & Android SDK
- Firebase Firestore & Authentication
- Google Maps SDK for Android
- Fragment Architecture
- Espresso UI Testing
- JUnit & Mockito

### 👨‍💻 Made With ❤️ By

CMPUT 301 Winter 2025 Team #34
