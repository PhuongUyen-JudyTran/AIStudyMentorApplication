# Implementation Plan - Complete SignIn and SignUp with Firebase

This plan details the steps to implement a complete and simple authentication flow using Firebase Authentication.

## Proposed Changes

### [Authentication Flow]

The app will follow this logical flow:
1. **MainActivity**: Acts as a splash/entry point. Checks if the user is logged in.
   - If YES -> Redirect to `HomeActivity`.
   - If NO -> Redirect to `SignInActivity`.
2. **SignInActivity**: Allows users to log in.
   - Success -> Redirect to `HomeActivity`.
   - No account -> Redirect to `SignUpActivity`.
3. **SignUpActivity**: Allows users to register.
   - Success -> Redirect to `SignInActivity` (or `HomeActivity`).
4. **HomeActivity**: The main content screen after login.
   - Includes a "Sign Out" button.

---

### [Component: :app]

#### [MODIFY] [MainActivity.java](file:///D:/BTEC/7436_Application_Development/AIStudyMentorApplication/app/src/main/java/com/example/aistudymentorapplication/MainActivity.java)
- Remove the hardcoded user creation logic.
- Implement a check for `mAuth.getCurrentUser()`.
- Redirect to either `HomeActivity` or `SignInActivity`.

#### [MODIFY] [SignInActivity.java](file:///D:/BTEC/7436_Application_Development/AIStudyMentorApplication/app/src/main/java/com/example/aistudymentorapplication/SignInActivity.java)
- Initialize `FirebaseAuth`.
- Implement `mAuth.signInWithEmailAndPassword()` in `btnSignIn`'s click listener.
- Navigate to `HomeActivity` on success.

#### [MODIFY] [SignUpActivity.java](file:///D:/BTEC/7436_Application_Development/AIStudyMentorApplication/app/src/main/java/com/example/aistudymentorapplication/SignUpActivity.java)
- Initialize `FirebaseAuth`.
- Implement `mAuth.createUserWithEmailAndPassword()` in `btnSignUp`'s click listener (after validation).
- Navigate back to `SignInActivity` on success.

#### [NEW] [HomeActivity.java](file:///D:/BTEC/7436_Application_Development/AIStudyMentorApplication/app/src/main/java/com/example/aistudymentorapplication/HomeActivity.java)
- Create this activity to display `activity_home.xml`.
- Implement `btnSignOut` to log out the user and return to `SignInActivity`.

#### [MODIFY] [AndroidManifest.xml](file:///D:/BTEC/7436_Application_Development/AIStudyMentorApplication/app/src/main/AndroidManifest.xml)
- Register `HomeActivity`.

## Verification Plan

### Automated Tests
- Run `gradle build` to ensure no syntax errors.

### Manual Verification
- Deploy to an emulator/device.
- Test Sign Up with a new email.
- Test Sign In with the created email.
- Test Sign Out.
- Verify that restarting the app while logged in bypasses the login screen.
