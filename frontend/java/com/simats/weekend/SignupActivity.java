package com.simats.weekend;

import android.content.Intent;
import android.os.Bundle; // Keep this
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

// --- START: NEW/UPDATED IMPORTS ---
import androidx.core.content.ContextCompat; // For getMainExecutor
import androidx.credentials.CredentialManager;
import androidx.credentials.CredentialManagerCallback; // The fix
import androidx.credentials.GetCredentialRequest;
import androidx.credentials.GetCredentialResponse;
import androidx.credentials.exceptions.GetCredentialCancellationException;
import androidx.credentials.exceptions.GetCredentialException;
import com.google.android.libraries.identity.googleid.GetGoogleIdOption;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;
// --- END: NEW/UPDATED IMPORTS ---

import com.simats.weekend.models.LoginResponse;
import com.simats.weekend.models.SignUpResponse;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.regex.Pattern;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SignupActivity extends AppCompatActivity {

    private static final String TAG = "SignupActivity";
    private TextInputEditText fullNameEditText, usernameEditText, emailEditText, passwordEditText, confirmPasswordEditText;
    private TextInputLayout fullNameLayout, usernameLayout, emailLayout, passwordLayout, confirmPasswordLayout;
    private ProgressBar progressBar;
    private Button btnSignup;
    private SessionManager sessionManager;
    private CredentialManager credentialManager;

    private static final Pattern NAME_PATTERN = Pattern.compile("^[a-zA-Z\\s]+$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[a-zA-Z0-9._%+-]+@(gmail\\.com|mail\\.com)$");
    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^" +
                    "(?=.*[0-9])" +         //at least 1 digit
                    "(?=.*[a-z])" +         //at least 1 lower case letter
                    "(?=.*[A-Z])" +         //at least 1 upper case letter
                    "(?=.*[@#$%^&+=!*?])" + //at least 1 special character
                    ".{6,8}" +              //between 6 and 8 characters
                    "$");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        sessionManager = new SessionManager(this);
        credentialManager = CredentialManager.create(this);

        fullNameEditText = findViewById(R.id.fullNameEditText);
        usernameEditText = findViewById(R.id.usernameEditText);
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText);

        fullNameLayout = findViewById(R.id.fullNameLayout);
        usernameLayout = findViewById(R.id.usernameLayout);
        emailLayout = findViewById(R.id.emailLayout);
        passwordLayout = findViewById(R.id.passwordLayout);
        confirmPasswordLayout = findViewById(R.id.confirmPasswordLayout);

        progressBar = findViewById(R.id.progressBar);
        btnSignup = findViewById(R.id.btn_signup);
        TextView tvLogin = findViewById(R.id.tv_login);
        LinearLayout googleSignInButton = findViewById(R.id.googleSignInButton);

        btnSignup.setOnClickListener(v -> {
            if (validateInput()) {
                registerUserWithPhp();
            }
        });

        tvLogin.setOnClickListener(v -> {
            startActivity(new Intent(SignupActivity.this, LoginActivity.class));
        });

        googleSignInButton.setOnClickListener(v -> {
            signInWithGoogle();
        });
    }

    private boolean validateInput() {
        // Clear previous errors
        fullNameLayout.setError(null);
        usernameLayout.setError(null);
        emailLayout.setError(null);
        passwordLayout.setError(null);
        confirmPasswordLayout.setError(null);

        String fullName = fullNameEditText.getText().toString().trim();
        String username = usernameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String confirmPassword = confirmPasswordEditText.getText().toString().trim();

        if (fullName.isEmpty() || username.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!NAME_PATTERN.matcher(fullName).matches()) {
            fullNameLayout.setError("Name must contain only letters and spaces.");
            return false;
        }

        if (!username.matches("[a-zA-Z0-9]+")) {
            usernameLayout.setError("Username can only contain letters and numbers");
            return false;
        }

        if (!EMAIL_PATTERN.matcher(email).matches()) {
            emailLayout.setError("Please use a valid gmail.com or mail.com address");
            return false;
        }

        if (!PASSWORD_PATTERN.matcher(password).matches()) {
            passwordLayout.setError("Password must be 6-8 chars with uppercase, lowercase, number & special char");
            return false;
        }

        if (!password.equals(confirmPassword)) {
            confirmPasswordLayout.setError("Passwords do not match");
            return false;
        }

        return true;
    }

    // --- START: CORRECTED GOOGLE SIGN-IN METHOD ---
    private void signInWithGoogle() {
        showLoading(true);
        String webClientId = getString(R.string.default_web_client_id);

        GetGoogleIdOption googleIdOption = new GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false) // Set to false for signup
                .setServerClientId(webClientId)
                .setAutoSelectEnabled(true)
                .build();

        GetCredentialRequest request = new GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build();

        // This is the corrected call
        credentialManager.getCredentialAsync(
                this, // Context
                request, // GetCredentialRequest
                null, // CancellationSignal (optional)
                ContextCompat.getMainExecutor(this), // Executor
                new CredentialManagerCallback<GetCredentialResponse, GetCredentialException>() {
                    @Override
                    public void onResult(GetCredentialResponse result) {
                        // Success
                        Log.d(TAG, "getCredentialAsync success");
                        handleGoogleSignInSuccess(result);
                    }

                    @Override
                    public void onError(GetCredentialException e) {
                        // Failure
                        Log.e(TAG, "getCredentialAsync error", e);
                        handleGoogleSignInFailure(e);
                    }
                }
        );
    }
    // --- END: CORRECTED GOOGLE SIGN-IN METHOD ---

    private void handleGoogleSignInSuccess(GetCredentialResponse result) {
        try {
            GoogleIdTokenCredential credential = GoogleIdTokenCredential.createFrom(result.getCredential().getData());
            String idToken = credential.getIdToken();
            if (idToken != null) {
                authenticateWithBackendUsingGoogleToken(idToken);
            } else {
                showLoading(false);
                Toast.makeText(this, "Google Sign-in failed: No ID Token found.", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            showLoading(false);
            Log.e(TAG, "Google Sign-In Success Handler Failed: ", e);
            Toast.makeText(this, "Google Sign-in failed. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleGoogleSignInFailure(GetCredentialException e) {
        showLoading(false);
        if (e instanceof GetCredentialCancellationException) {
            Toast.makeText(this, "Sign-in cancelled.", Toast.LENGTH_SHORT).show();
        } else {
            Log.w(TAG, "Google sign in failed", e);
            Toast.makeText(this, "Google Sign-in failed. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }

    private void authenticateWithBackendUsingGoogleToken(String idToken) {
        showLoading(true);
        ApiService apiService = RetrofitClient.getClient(this).create(ApiService.class);
        Call<LoginResponse> call = apiService.authWithGoogle(idToken);
        call.enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse loginResponse = response.body();
                    Toast.makeText(SignupActivity.this, loginResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    if (loginResponse.isStatus() && loginResponse.getUser() != null) {
                        int userId = loginResponse.getUser().getId();
                        String email = loginResponse.getUser().getEmail();
                        String fullname = loginResponse.getUser().getFullname();
                        String token = loginResponse.getToken();
                        sessionManager.createLoginSession(userId, email, fullname, token);
                        navigateToCorrectHome(email);
                    }
                } else {
                    Toast.makeText(SignupActivity.this, "Backend authentication failed.", Toast.LENGTH_LONG).show();
                }
            }
            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                showLoading(false);
                Log.e(TAG, "Google Auth API Call Failure: " + t.getMessage());
                Toast.makeText(SignupActivity.this, "Network Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void navigateToCorrectHome(String email) {
        if ("admin1s23@gmail.com".equals(email)) {
            startActivity(new Intent(SignupActivity.this, AdminhomeActivity.class));
        } else {
            startActivity(new Intent(SignupActivity.this, HomeActivity.class));
        }
        finishAffinity();
    }

    private void registerUserWithPhp() {
        showLoading(true);
        String fullName = fullNameEditText.getText().toString().trim();
        String username = usernameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        ApiService apiService = RetrofitClient.getClient(this).create(ApiService.class);
        Call<SignUpResponse> call = apiService.signupUser(fullName, username, email, password);
        call.enqueue(new Callback<SignUpResponse>() {
            @Override
            public void onResponse(Call<SignUpResponse> call, Response<SignUpResponse> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    SignUpResponse signUpResponse = response.body();
                    Toast.makeText(SignupActivity.this, signUpResponse.getMessage(), Toast.LENGTH_LONG).show();
                    if (signUpResponse.isStatus()) {
                        startActivity(new Intent(SignupActivity.this, LoginActivity.class));
                        finish();
                    }
                } else {
                    Toast.makeText(SignupActivity.this, "Registration failed. Please try again.", Toast.LENGTH_LONG).show();
                }
            }
            @Override
            public void onFailure(Call<SignUpResponse> call, Throwable t) {
                showLoading(false);
                Log.e(TAG, "API Call Failure: " + t.getMessage());
                Toast.makeText(SignupActivity.this, "Network Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showLoading(boolean isLoading) {
        if (isLoading) {
            progressBar.setVisibility(View.VISIBLE);
            btnSignup.setEnabled(false);
        } else {
            progressBar.setVisibility(View.GONE);
            btnSignup.setEnabled(true);
        }
    }
}