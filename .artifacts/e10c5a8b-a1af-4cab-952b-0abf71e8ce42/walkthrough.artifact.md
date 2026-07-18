# Walkthrough - Profile Page Implementation

I have designed and implemented the Profile page, allowing users to view and update their personal information.

## Changes Made

### 1. UI Design ([activity_profile.xml](file:///D:/BTEC/7436_Application_Development/AIStudyMentorApplication/app/src/main/res/layout/activity_profile.xml))
- Created a modern, clean interface using **Material Design 3**.
- Added a header with a back button and the title "Profile".
- Included a user avatar placeholder.
- Implemented input fields for:
    - **Full Name**
    - **Email**
    - **Grade**
    - **School**
- Added a stylish "Update Profile" button.

### 2. Functional Logic ([ProfileActivity.java](file:///D:/BTEC/7436_Application_Development/AIStudyMentorApplication/app/src/main/java/com/example/aistudymentorapplication/ProfileActivity.java))
- Initialized all UI components.
- Added a back button listener to return to the previous screen.
- Implemented a basic "Update Profile" function that validates the name and shows a success toast.
- Pre-filled the fields with dummy data for immediate preview.

### 3. Navigation ([HomeActivity.java](file:///D:/BTEC/7436_Application_Development/AIStudyMentorApplication/app/src/main/java/com/example/aistudymentorapplication/HomeActivity.java))
- Enabled the navigation link in the bottom navigation bar to open the Profile page.

## Verification Plan

### Manual Verification
1. Launch the app and go to the **Profile** tab.
2. Verify that all information fields are displayed correctly.
3. Edit the fields and click **Update Profile**.
4. Confirm that the success toast appears.
5. Click the back button (top left) to return to the Home screen.

> [!TIP]
> The layout uses `TextInputLayout` which provides nice animations and floating labels when you interact with the fields.
