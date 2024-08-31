# User Management Android App

This Android application allows users to be added, edited, deleted, and displayed. It uses a local SQLite database (Room) to store user data and fetches user information from an API. The app showcases the use of Android components such as RecyclerView, Retrofit, Material Design, and more.

## Features

- **Add User**: Create a new user with details like first name, last name, email, and avatar URL.
- **Edit User**: Modify the details of an existing user.
- **Delete User**: Remove a user from the list.
- **Display Users**: View a list of users from both the local database and remote API.
- **API Integration**: Fetch user data from a remote server using Retrofit.
- **Local Database**: Utilizes Room for local data storage and management.
- **Error Handling**: Manages API errors and displays appropriate messages to users.
- **Loading Indicator**: Shows a loader while data is being fetched from the API.

## Technologies Used

- **Java**: Core programming language for the app.
- **Room Database**: Provides local data storage capabilities.
- **Retrofit**: HTTP client for making API calls.
- **RecyclerView**: Displays the user list.
- **Material Design Components**: Ensures a modern UI experience.
- **ExecutorService**: Manages background operations to keep the UI responsive.

## Prerequisites

- **Android Studio**: Version 4.1 or later.
- **Gradle**: Version 7.0 or later.
- **Java Development Kit (JDK)**: Version 8 or later.

## Setup Instructions

1. **Clone the Repository**:
    ```bash
    git clone https://github.com/roeebenezra/myAndroidApp.git
    ```

2. **Open the Project in Android Studio**:
    - Launch Android Studio and open the cloned project directory.

3. **Build the Project**:
    - Allow Android Studio to build the project and download necessary dependencies.

4. **Run the App**:
    - Connect your Android device or start an emulator.
    - Click on the "Run" button in Android Studio to install and launch the app.

## Usage

- **Add a User**: Click the Floating Action Button (FAB) to open the "Add User" form.
- **Edit a User**: Tap on any user in the list to modify their details.
- **Delete a User**: Long press on a user item to delete them.
- **Fetch Users from API**: Users will be fetched and displayed from the API upon app startup.
- **Clear Database**: The local database is cleared on every app start to ensure a fresh dataset.

## API Configuration

The app uses a remote API to fetch user data. Configure the base URL in the `ApiClient` class:

```java
private static final String BASE_URL = "https://your-api-url.com/";
```
Replace `"https://your-api-url.com/"` with your actual API endpoint.

## Error Handling

- Proper error messages are shown to users for network failures or API errors (e.g., 404, 500).

## Future Improvements

- Add pagination for handling large datasets.
- Implement search functionality to filter users by name or email.
- Enhance the UI/UX for a more polished user experience.
- Optimize performance for managing large datasets.

## Contributing

Contributions are welcome! Feel free to fork this repository and submit a pull request.
