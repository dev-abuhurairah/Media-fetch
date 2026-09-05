# Compiling MediaFetch with GitHub Actions (No Android Studio Required)

GitHub Actions provides pre-configured Ubuntu virtual environments with **JDK 17**, **Android SDK API 35**, and **Build Tools** pre-installed. You do not need Android Studio or a local JDK to build the ready-to-install APK.

---

## Step 1: Initialize Git Repository and Push to GitHub

From the `MediaFetch` directory in PowerShell or your terminal:

```powershell
# Navigate into the project folder
cd C:\Users\abuhu\.gemini\antigravity\scratch\MediaFetch

# Initialize git
git init
git add .
git commit -m "feat: initial commit of MediaFetch Android app & backend"

# Connect to your GitHub repository (create a new empty repo on github.com first)
git remote add origin https://github.com/YOUR_USERNAME/MediaFetch.git
git branch -M main
git push -u origin main
```

---

## Step 2: GitHub Actions Automated Build

Once you push to GitHub, the workflow [`.github/workflows/build.yml`](../.github/workflows/build.yml) triggers automatically:

1. **Environment Setup**: Provisions Ubuntu with Temurin JDK 17, Android SDK 35, and Gradle 8.9.
2. **Automated Testing**:
   - Executes all Android unit tests (`gradle test`).
   - Runs backend API integration tests (`npm test`).
3. **Compilation**:
   - Compiles **Debug APK** (`gradle assembleDebug`).
   - Compiles **Release APK** (`gradle assembleRelease`).
4. **Artifact Upload**:
   - The compiled `.apk` files are uploaded as downloadable artifacts on GitHub.

---

## Step 3: Download the Compiled APK

1. Go to your repository on [GitHub](https://github.com).
2. Click on the **Actions** tab at the top.
3. Click on the latest workflow run (e.g. *"Build MediaFetch (Android & Backend)"*).
4. Scroll down to the **Artifacts** section at the bottom of the page:
   - **`MediaFetch-Debug-APK`**: Ready to install directly on your Android device (no signature required).
   - **`MediaFetch-Release-APK`**: Optimized release APK with R8 code shrinking and obfuscation enabled.
5. Click to download the zip file, unzip it, and transfer `app-debug.apk` to your phone!

---

## Manual Trigger (Workflow Dispatch)

You can trigger a build at any time without making commits:
1. In your GitHub repository, open the **Actions** tab.
2. Select **"Build MediaFetch (Android & Backend)"** from the left sidebar.
3. Click the **"Run workflow"** dropdown button on the right and click **Run workflow**.