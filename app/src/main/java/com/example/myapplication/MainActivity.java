package com.example.myapplication;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.os.Handler;
import android.os.Looper;
import android.util.Patterns;
import android.view.View;
import android.widget.*;
import androidx.recyclerview.widget.LinearLayoutManager;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import org.jetbrains.annotations.NotNull;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private UserAdapter userAdapter;
    private List<User> userList = new ArrayList<>();
    private ProgressBar progressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Clear database on app start (if needed)
//        clearDatabase();

        setSupportActionBar(findViewById(R.id.toolbar));
        progressBar = findViewById(R.id.progressBar);

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        findViewById(R.id.fab).setOnClickListener(view -> showAddUserDialog());

        userAdapter = new UserAdapter(this, new ArrayList<>(),
                this::showEditUserDialog,               // Open dialog to edit user
                this::showDeleteConfirmationDialog);    // Confirm before deleting user

        recyclerView.setAdapter(userAdapter);

        loadUsersFromApi();  // fetch users on startup
    }

    private void loadUsersFromApi() {
        progressBar.setVisibility(View.VISIBLE);
        UserApi userApi = ApiClient.getRetrofitInstance().create(UserApi.class);
        int firstPage = 1;
        int secondPage = 2;

        // Fetch the first page of users
        fetchUsersFromPage(userApi, firstPage, users -> {
            userList = users; // Save the initial users

            // Fetch the second page of users
            fetchUsersFromPage(userApi, secondPage, moreUsers -> {
                userList.addAll(moreUsers); // Combine both pages
                saveUsersToDatabase(userList); // Save combined users to the database and display them
            });
        });
    }

    private void fetchUsersFromPage(UserApi userApi, int page, OnUsersLoadedListener listener) {
        userApi.getUsers(page).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NotNull Call<UserResponse> call, @NotNull Response<UserResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<User> users = response.body().getData();
                    listener.onUsersLoaded(users);
                } else {
                    handleApiError(response.message());
                }
                handleResponseOrFailure(); // Handle response or failure
            }

            @Override
            public void onFailure(@NotNull Call<UserResponse> call, @NotNull Throwable t) {
                handleResponseOrFailure(); // Handle response or failure
                handleApiFailure(t);
            }
        });
    }

    private void handleApiError(String message) {
        String errorMessage = "An error occurred: " + message;
        Toast.makeText(MainActivity.this, errorMessage, Toast.LENGTH_LONG).show();
    }

    private void handleApiFailure(Throwable t) {
        String errorMessage = "Failed to load users: " + t.getMessage();
        Toast.makeText(MainActivity.this, errorMessage, Toast.LENGTH_LONG).show();
    }

    // Listener interface to handle user list loading
    private interface OnUsersLoadedListener {
        void onUsersLoaded(List<User> users);
    }

    private void handleResponseOrFailure() {
        // Create a delay to keep the loader visible for at least 0.5 seconds
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            ProgressBar progressBar = findViewById(R.id.progressBar);
            progressBar.setVisibility(View.GONE); // Hide the loader after 0.5 seconds
        }, 500); // Delay of 500 milliseconds
    }

    public void saveUsersToDatabase(List<User> userList) {
        AppDatabase db = AppDatabase.getDatabase(this);
        UserDao userDao = db.userDao();

        executorService.execute(() -> {
            // Perform the database insertion operation on a background thread
            userDao.insertAll(userList);

            // Update the UI on the main thread after the insertion is complete
            // Load users into RecyclerView after saving
            runOnUiThread(this::loadUsersFromDatabase);
        });
    }

    @SuppressLint("SetTextI18n")
    private void showUserDialog(User user, boolean isEditMode) {
        // Initialize the dialog builder
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // Inflate the dialog layout
        View view = getLayoutInflater().inflate(R.layout.dialog_add_user, null);
        builder.setView(view);

        // Get references to the dialog's input fields
        TextView title = view.findViewById(R.id.title);
        EditText etFirstName = view.findViewById(R.id.etFirstName);
        EditText etLastName = view.findViewById(R.id.etLastName);
        EditText etEmail = view.findViewById(R.id.etEmail);
        EditText etAvatarUrl = view.findViewById(R.id.etAvatarUrl);
        Button btnAddUser = view.findViewById(R.id.btnAddUser);

        // If editing, pre-fill the fields with the user's current information
        if (isEditMode && user != null) {
            title.setText("Update user info");
            etFirstName.setText(user.getFirst_name());
            etLastName.setText(user.getLast_name());
            etEmail.setText(user.getEmail());
            etAvatarUrl.setText(user.getAvatar());
            btnAddUser.setText("Update User");
        }

        // Create the AlertDialog
        AlertDialog dialog = builder.create();

        // Set the button action
        btnAddUser.setOnClickListener(v -> {
            // Gather input from the fields
            String firstName = etFirstName.getText().toString().trim();
            String lastName = etLastName.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String avatarUrl = etAvatarUrl.getText().toString().trim();

            // Validate user input fields
            if (!validateInputs(firstName, lastName, email, avatarUrl))
                return;

            if (isEditMode && user != null) {
                // Update existing user
                user.setFirstName(firstName);
                user.setLastName(lastName);
                user.setEmail(email);
                user.setAvatar(avatarUrl);
                updateUserInDatabase(user);
            } else {
                // Add new user
                User newUser = new User(0, firstName, lastName, email, avatarUrl);
                addUserToDatabase(newUser);
            }

            // Dismiss dialog after action
            dialog.dismiss();
        });

        // Show the dialog
        dialog.show();
    }

    private boolean validateInputs(String firstName, String lastName, String email, String avatarUrl) {
        // Validate inputs
        if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || avatarUrl.isEmpty()) {
            Toast.makeText(MainActivity.this, "All fields are required", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Validate email and avatar URL
        if (!isValidEmail(email)) {
            Toast.makeText(MainActivity.this, "Invalid email address", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!isValidUrl(avatarUrl)) {
            Toast.makeText(MainActivity.this, "Invalid URL for avatar", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private boolean isValidEmail(CharSequence email) {
        // Return true if the email matches the email pattern
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private boolean isValidUrl(String url) {
        // Return true if the URL matches the URL pattern
        return Patterns.WEB_URL.matcher(url).matches();
    }

    private void showAddUserDialog() {
        showUserDialog(null, false);
    }

    private void showEditUserDialog(User user) {
        showUserDialog(user, true);
    }

    private void updateUserInDatabase(User user) {
        AppDatabase db = AppDatabase.getDatabase(this);
        UserDao userDao = db.userDao();

        executorService.execute(() -> {
            // Perform the database update operation on a background thread
            userDao.update(user);

            // Update the UI on the main thread after the update is complete
            // Refresh the list after updating a user
            runOnUiThread(this::loadUsersFromDatabase);
        });
    }

    // Show a confirmation dialog before deleting a user
    private void showDeleteConfirmationDialog(User user) {
        new AlertDialog.Builder(this)
                .setTitle("Delete User")
                .setMessage("Are you sure you want to delete this user?")
                .setPositiveButton("Delete", (dialog, which) -> deleteUserFromDatabase(user))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteUserFromDatabase(User user) {
        AppDatabase db = AppDatabase.getDatabase(this);
        UserDao userDao = db.userDao();

        executorService.execute(() -> {
            // Perform the database deletion on a background thread
            userDao.delete(user);

            // Update the UI on the main thread after deletion is complete
            runOnUiThread(() -> {
                loadUsersFromDatabase();  // Reload all users after deletion

                // Show a Snackbar with an undo option
                Snackbar.make(findViewById(R.id.recyclerView), "User deleted", Snackbar.LENGTH_LONG)
                        .setAction("UNDO", v -> {
                            addUserToDatabase(user); // Re-add the user on undo
                        })
                        .show();
            });
        });
    }

    private void addUserToDatabase(User user) {
        AppDatabase db = AppDatabase.getDatabase(this);
        UserDao userDao = db.userDao();

        executorService.execute(() -> {
            // Perform the database insertion on a background thread
            userDao.insert(user);

            // Update the UI on the main thread after insertion is complete
            // Reload all users, including the new one
            runOnUiThread(this::loadUsersFromDatabase);
        });
    }

    private void loadUsersFromDatabase() {
        AppDatabase db = AppDatabase.getDatabase(this);
        UserDao userDao = db.userDao();

        executorService.execute(() -> {
            // Perform the database query on a background thread
            List<User> userListFromDb = userDao.getAllUsers();

            // Update the UI on the main thread
            runOnUiThread(() -> {
                // Pass the full list to the adapter
                userAdapter.setUserList(userListFromDb);
            });
        });
    }

    private void clearDatabase() {
        AppDatabase db = AppDatabase.getDatabase(this);
        UserDao userDao = db.userDao();

        executorService.execute(() -> {
            // Perform the delete operation on a background thread
            userDao.deleteAllUsers();

            // Optionally, update the UI on the main thread
            runOnUiThread(() -> {
                // Clear the adapter's list and notify it of changes
                userAdapter.setUserList(new ArrayList<>());
                Toast.makeText(this, "Database cleared successfully", Toast.LENGTH_SHORT).show();
            });
        });
    }
}
