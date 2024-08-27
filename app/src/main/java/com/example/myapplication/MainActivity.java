package com.example.myapplication;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.text.TextUtils;
import android.util.Patterns;
import androidx.recyclerview.widget.LinearLayoutManager;
import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import android.app.Dialog;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.material.snackbar.Snackbar;
import org.jetbrains.annotations.NotNull;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private UserAdapter userAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setSupportActionBar(findViewById(R.id.toolbar));

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        userAdapter = new UserAdapter(this, new ArrayList<>(),
                this::showEditUserDialog,               // Open dialog to edit user
                this::showDeleteConfirmationDialog);    // Confirm before deleting user

        recyclerView.setAdapter(userAdapter);

        clearDatabaseAndFetchUsers();  // Clear the database and fetch users on startup

        findViewById(R.id.fab).setOnClickListener(view -> showAddUserDialog());
    }

    @SuppressLint("StaticFieldLeak")
    private void clearDatabaseAndFetchUsers() {
        AppDatabase db = AppDatabase.getDatabase(this);
        UserDao userDao = db.userDao();

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                userDao.deleteAllUsers();  // Clear all rows in the users table
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                fetchUsersFromApi();  // Fetch users after clearing the database
            }
        }.execute();
    }

    private void fetchUsersFromApi() {
        UserApi userApi = ApiClient.getRetrofitInstance().create(UserApi.class);
        Call<UserResponse> call = userApi.getUsers();

        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NotNull Call<UserResponse> call, @NotNull Response<UserResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<User> userList = response.body().getData();
                    saveUsersToDatabase(userList);
                }
            }

            @Override
            public void onFailure(@NotNull Call<UserResponse> call, @NotNull Throwable t) {
                Log.e("API Error", "Failed to fetch users", t);
            }
        });
    }

    @SuppressLint("StaticFieldLeak")
    public void saveUsersToDatabase(List<User> userList) {
        AppDatabase db = AppDatabase.getDatabase(this);
        UserDao userDao = db.userDao();

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                userDao.insertAll(userList);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                loadUsersFromDatabase();  // Load users into RecyclerView after saving
            }
        }.execute();
    }

    private void showAddUserDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_add_user);

        EditText etEmail = dialog.findViewById(R.id.etEmail);
        EditText etFirstName = dialog.findViewById(R.id.etFirstName);
        EditText etLastName = dialog.findViewById(R.id.etLastName);
        EditText etAvatarUrl = dialog.findViewById(R.id.etAvatarUrl);
        Button btnAddUser = dialog.findViewById(R.id.btnAddUser);

        btnAddUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = etEmail.getText().toString();
                String firstName = etFirstName.getText().toString();
                String lastName = etLastName.getText().toString();
                String avatarUrl = etAvatarUrl.getText().toString();

                if (isValidEmail(email)) {
                    etEmail.setError("Invalid email address");
                    return;
                }

                if (firstName.isEmpty()) {
                    etFirstName.setError("First name is required");
                    return;
                }

                if (lastName.isEmpty()) {
                    etLastName.setError("Last name is required");
                    return;
                }


                if (isValidUrl(avatarUrl)) {
                    etAvatarUrl.setError("Invalid URL");
                    return;
                }

                User newUser = new User(0, email, firstName, lastName, avatarUrl);
                addUserToDatabase(newUser);
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    // Show dialog to edit user details
    @SuppressLint("SetTextI18n")
    private void showEditUserDialog(User user) {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_add_user);

        EditText etFirstName = dialog.findViewById(R.id.etFirstName);
        EditText etLastName = dialog.findViewById(R.id.etLastName);
        EditText etAvatarUrl = dialog.findViewById(R.id.etAvatarUrl);
        EditText etEmail = dialog.findViewById(R.id.etEmail);
        Button btnAddUser = dialog.findViewById(R.id.btnAddUser);

        // Pre-fill fields with existing user data
        etFirstName.setText(user.getFirst_name());
        etLastName.setText(user.getLast_name());
        etAvatarUrl.setText(user.getAvatar());
        etEmail.setText(user.getEmail());
        btnAddUser.setText("Update User");

        btnAddUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String firstName = etFirstName.getText().toString();
                String lastName = etLastName.getText().toString();
                String avatarUrl = etAvatarUrl.getText().toString();
                String email = etEmail.getText().toString();

                if (firstName.isEmpty()) {
                    etFirstName.setError("First name is required");
                    return;
                }

                if (lastName.isEmpty()) {
                    etLastName.setError("Last name is required");
                    return;
                }

                if (isValidEmail(email)) {
                    etEmail.setError("Invalid email address");
                    return;
                }

                if (isValidUrl(avatarUrl)) {
                    etAvatarUrl.setError("Invalid URL");
                    return;
                }

                user.setFirstName(firstName);
                user.setLastName(lastName);
                user.setAvatar(avatarUrl);
                user.setEmail(email);

                updateUserInDatabase(user);  // Update user details in the database
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    @SuppressLint("StaticFieldLeak")
    private void updateUserInDatabase(User user) {
        AppDatabase db = AppDatabase.getDatabase(this);
        UserDao userDao = db.userDao();

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                userDao.update(user);  // Update user details in the database
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                loadUsersFromDatabase();  // Refresh the list after updating a user
            }
        }.execute();
    }

    // Show a confirmation dialog before deleting a user
    private void showDeleteConfirmationDialog(User user) {
        new AlertDialog.Builder(this)
                .setTitle("Delete User")
                .setMessage("Are you sure you want to delete this user?")
                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteUserFromDatabase(user);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @SuppressLint("StaticFieldLeak")
    private void deleteUserFromDatabase(User user) {
        AppDatabase db = AppDatabase.getDatabase(this);
        UserDao userDao = db.userDao();

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                userDao.delete(user);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                loadUsersFromDatabase();

                // Show a Snackbar with undo option
                Snackbar.make(findViewById(R.id.recyclerView), "User deleted", Snackbar.LENGTH_LONG)
                        .setAction("UNDO", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                addUserToDatabase(user); // Re-add the user on undo
                            }
                        })
                        .show();
            }
        }.execute();
    }


    private boolean isValidEmail(CharSequence email) {
        return TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private boolean isValidUrl(String url) {
        return TextUtils.isEmpty(url) || !Patterns.WEB_URL.matcher(url).matches();
    }

    @SuppressLint("StaticFieldLeak")
    private void addUserToDatabase(User user) {
        AppDatabase db = AppDatabase.getDatabase(this);
        UserDao userDao = db.userDao();

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                userDao.insert(user);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                // Successfully added the user, now print details
                Log.d("NewUserInfo", "User added with ID: " + user.getId());
                Log.d("NewUserInfo", "First Name: " + user.getFirst_name());
                Log.d("NewUserInfo", "Last Name: " + user.getLast_name());
                Log.d("NewUserInfo", "Email: " + user.getEmail());
                Log.d("NewUserInfo", "Avatar URL: " + user.getAvatar());

                loadUsersFromDatabase();  // Reload all users, including the new one
            }
        }.execute();
    }


    @SuppressLint("StaticFieldLeak")
    private void loadUsersFromDatabase() {
        AppDatabase db = AppDatabase.getDatabase(this);
        UserDao userDao = db.userDao();

        new AsyncTask<Void, Void, List<User>>() {
            @Override
            protected List<User> doInBackground(Void... voids) {
                return userDao.getAllUsers();  // Fetch all users from the database
            }

            @Override
            protected void onPostExecute(List<User> userList) {
                userAdapter.setUserList(userList);  // Pass the full list to the adapter
            }
        }.execute();
    }
}
