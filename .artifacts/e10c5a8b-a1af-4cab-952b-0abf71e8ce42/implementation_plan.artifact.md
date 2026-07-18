# Implementation Plan - Profile Page Design

This plan outlines the steps to design and implement the Profile page in the AI Study Mentor Application.

## User Review Required

> [!NOTE]
> I will use `Material Design 3` components to ensure the UI is consistent with the rest of the application. The email field will be editable by default, but typically in apps, it might be read-only if it's the primary login identifier.

## Proposed Changes

### [Component] UI Resources

#### [MODIFY] [strings.xml](file:///D:/BTEC/7436_Application_Development/AIStudyMentorApplication/app/src/main/res/values/strings.xml)
- Add strings for profile labels: "Profile", "Full Name", "Grade", "School", "Update Profile", etc.

### [Component] Profile Feature

#### [MODIFY] [activity_profile.xml](file:///D:/BTEC/7436_Application_Development/AIStudyMentorApplication/app/src/main/res/layout/activity_profile.xml)
- Implement a scrollable layout with:
    - Top toolbar or header with a back button.
    - User avatar placeholder.
    - Input fields (using `TextInputLayout` and `TextInputEditText`) for Full Name, Email, Grade, and School.
    - A prominent "Update Profile" button at the bottom.

#### [NEW] [ProfileActivity.java](file:///D:/BTEC/7436_Application_Development/AIStudyMentorApplication/app/src/main/java/com/example/aistudymentorapplication/ProfileActivity.java)
- Initialize UI components.
- Implement the back button functionality.
- Add a click listener for the "Update Profile" button (showing a success toast for now).

### [Component] Configuration & Navigation

#### [MODIFY] [AndroidManifest.xml](file:///D:/BTEC/7436_Application_Development/AIStudyMentorApplication/app/src/main/AndroidManifest.xml)
- Register `ProfileActivity`.

#### [MODIFY] [HomeActivity.java](file:///D:/BTEC/7436_Application_Development/AIStudyMentorApplication/app/src/main/java/com/example/aistudymentorapplication/HomeActivity.java)
- Fix the `BottomNavigationView` listener to navigate to `ProfileActivity`.

## Verification Plan

### Manual Verification
1. Launch the application.
2. Navigate to the **Profile** tab via the bottom navigation bar.
3. Check that all fields (Full Name, Email, Grade, School) are visible and properly labeled.
4. Enter some data and click **Update Profile**.
5. Verify a toast message or feedback is shown.
6. Click the back button to return to the Home screen.
