Real Estate Mobile Application - Final Year Project 
Summary

This repository contains the complete documentation and implementation for a Real Estate Mobile Application developed as a Final Year Project. Below is a consolidated overview of the project, combining information from both the Software Requirements Specification (SRS) and Software Design Document (SDD).


Project Overview
The Real Estate Mobile Application is an Android-based platform designed to simplify property discovery and management. It enables users to:
- Browse property listings with detailed information
- Contact property owners directly
- Save favorite properties for quick access
- Add and manage their own property listings

The app is built using Android Studio with Firebase backend services, following modern Android development practices.


2.Key Features
Functional Requirements:
1. User Authentication – Secure sign-up, login, and logout using Firebase Authentication.
2. Property Browsing – View available properties with images, price, location, and descriptions.
3. Category Filtering – Filter properties by type (Villa, Home, Warehouse, Farmhouse, etc.).
4. Favorites System – Save and remove favorite properties.
5. Contact Owners – Direct communication with property owners.
6. Add Listings – Users can list their own properties for sale/rent.

3.Non-Functional Requirements:
- Performance: Load listings in under 3 seconds.
- Security: Secure data handling via Firebase.
- Usability: Intuitive, Material Design-based interface.
- Reliability: Stable performance with error handling.

---

4.System Architecture
Architectural Pattern: MVVM (Model-View-ViewModel)
- View: Activities and Fragments (UI Layer)
- ViewModel: Handles business logic and data presentation
- Model: Data entities (User, Property, Favorite) and Firebase integration

5.Backend Services (Firebase):
- Firebase Authentication – User management
- Firebase Firestore – NoSQL database for properties, users, and favorites
- Firebase Storage – Image storage for property listings

6.Key Components:
- Activities: Login, Register, Home, Property Details, Add Property, Favorites, Profile
- ViewModels: AuthViewModel, PropertyViewModel, FavoriteViewModel, UserProfileViewModel
- Models: User, Property, Favorite


7.User Interface Design
- Follows Material Design Guidelines
- Clean, minimalistic layout with responsive design
- Screens include:
  - Login/Register
  - Home (Listings with search/filter)
  - Property Details
  - Add Property
  - Favorites
  - User Profile


8.Technologies Used
- Frontend: Android Studio (Java), XML
- Backend: Firebase (Auth, Firestore, Storage)
- Libraries: Glide/Picasso (image loading), Firebase SDKs
- Tools: GitHub (version control)

---

9.Project Scope
Included:
- User authentication and profile management
- Property listing and browsing
- Favorites system
- Category-based filtering
- Owner contact feature

Out of Scope:
- Payment/booking system
- Map navigation/GPS integration
- Admin dashboard (optional future enhancement)


10.Documentation Structure

Final-Year-Project/
├── Proposal/           Project proposal document
├── SRS/              Software Requirements Specification
├── SDD/               Software Design Document
├── Source-Code/       Android application source code
└── README.md          Project summary (this file)


Developer Information
- Name: Tanveer Hussain  
- Roll No: F22BINFT1E02085  
- Degree: BS (Information Technology)  
- Supervisor: Sir Abdul Karim Nawaz  
- Institution: The Islamia University of Bahawalpur, Faculty of Computing, Department of Information Technology



References
- [Android Studio Documentation](https://developer.android.com/studio)
- [Firebase Documentation](https://firebase.google.com/docs)
- [Material Design Guidelines](https://material.io/design)
- [Stack Overflow](https://stackoverflow.com)



This project demonstrates a complete mobile application development lifecycle—from requirements gathering and system design to implementation using modern Android and cloud technologies.

