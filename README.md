# SMS Gateway

A robust Android application that turns your device into an SMS gateway, orchestrated remotely via GitHub. This app allows you to schedule and send SMS messages using a configuration hosted on a private GitHub repository.

## Features

- **Remote Orchestration**: Fetch SMS commands from a JSON file hosted on GitHub.
- **Exact Scheduling**: Uses Android's `AlarmManager` to ensure reliable background execution, even on devices with strict battery optimizations.
- **Configurable Interval**: Set the check frequency (minimum 15 minutes) to balance timeliness and battery life.
- **Secure Configuration**: Store your GitHub Personal Access Token (PAT) and file URL securely in the app settings.
- **Live Logs**: View real-time application logs directly within the app for debugging and monitoring.

## Tech Stack

- **Language**: Java
- **Architecture**: MVVM (Model-View-ViewModel)
- **Background Processing**:
    - `AlarmManager` for exact scheduling.
    - `WorkManager` for guaranteed execution of background tasks.
- **Networking**: Retrofit & OkHttp for GitHub API integration.
- **UI**: Material Design components.

## Setup & Configuration

### Prerequisites

1. **GitHub Repository**: Create a private repository to host your command file.
2. **Command File**: Create a JSON file (e.g., `commands.json`) in your repository.
   ```json
   {
     "iphone": [
       {
         "message": "Hello from Gateway",
         "phone": "+1234567890"
       }
     ]
   }
   ```
3. **Personal Access Token (PAT)**: Generate a GitHub PAT with `repo` scope to allow the app to read the private file.

### App Configuration

1. Launch the **SMS Gateway** app.
2. Tap the **Settings** icon (gear).
3. Enter the following details:
    - **GitHub File URL**: The raw URL or API URL to your JSON file.
    - **GitHub Token**: Your Personal Access Token (starts with `ghp_`).
    - **Check Interval**: Frequency in minutes (default: 15).
4. Tap **Save**.
5. On the main screen, tap **Start Service**.

## Permissions

The app requires the following permissions to function:

- `SEND_SMS`: To send text messages.
- `INTERNET`: To fetch commands from GitHub.
- `SCHEDULE_EXACT_ALARM`: To run background checks precisely at the set interval.

## Troubleshooting

- **Service not starting?**
    - Ensure you have granted all requested permissions.
    - Check if "Battery Saver" mode is enabling; it may restrict background activity.
- **Logs show "Error fetching commands"?**
    - Verify your GitHub Token is valid and has the correct scopes.
    - Check your internet connection.

## License

Free for use—your feedback helps us improve.
