# ONCE AGAIN - Android Mobile Application

## Description
**ONCE AGAIN** is an Android mobile application designed to give a **second life to unused items**.  
It connects people who want to get rid of items in good condition with those who need them, helping reduce waste and promoting a supportive community.

---


---

## Project Objectives
- **Facilitate sharing and reuse of items**: Allow users to give away or find items easily, free or at low cost.  
- **Reduce waste**: Encourage reuse and recycling by connecting donors and receivers.  
- **Promote a supportive community**: Create a platform where users can interact and help each other.

---

## Main Features
- **Post Listings**: Users can post items they want to give away.  
- **Search Items**: Search bar and filters to find specific items.  
- **User Management**: Authentication and role assignment (admin and user).  
- **GPS Location**: Items can be located on a map to facilitate pickup.  
- **Messaging**: Direct communication between users.

---

## Activity Flow and Concepts Used

| Page / Module | Description |
|---------------|-------------|
| **Splash Screen** | Displays the app logo for 3 seconds before redirecting to the login page. |
| **Login** | User authentication via Firebase Auth, with credential verification and link to the registration page. |
| **Registration** | Secure account creation via Firebase Authentication with fields for username, email, and password. |
| **Home** | Main interface with search bar, filters, and item display using RecyclerView. |
| **Item Details** | Detailed view with images (ViewPager2), description, condition, location, and contact info for the donor. |
| **Create Listing** | Form to add a new item, including category, title, description, photos, availability, and location. |
| **Messaging** | Displays user conversations and allows sending messages, integrated with Firebase. |
| **Profile** | Shows user personal info with profile picture, name, email, date of birth, and buttons to edit info or logout. |
| **Edit Profile** | Form to update personal info, validated and stored in Firebase.

---

## Tools and Technologies Used
- **Language**: Kotlin / Java  
- **IDE**: Android Studio  
- **Backend / Database**: Firebase (Authentication, Firestore, Storage)  
- **Location**: Google Maps API  
- **UI Components**: RecyclerView, ViewPager2, Material Components  

---




