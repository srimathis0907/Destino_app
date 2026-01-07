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

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.regex.Pattern;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    private static final String DEBUG_TAG = "SESSION_DEBUG";
    private TextInputEditText emailEditText, passwordEditText;
    private TextInputLayout emailLayout;
    private Button loginButton;
    private ProgressBar progressBar;
    private SessionManager sessionManager;
    private CredentialManager credentialManager;

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[a-zA-Z0-9._%+-]+@(gmail\\.com|mail\\.com)$");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        sessionManager = new SessionManager(getApplicationContext());
        credentialManager = CredentialManager.create(this);

        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        emailLayout = findViewById(R.id.emailLayout);
        loginButton = findViewById(R.id.loginButton);
        progressBar = findViewById(R.id.progressBar);
        TextView forgotPasswordText = findViewById(R.id.forgotPasswordText);
        TextView signUpText = findViewById(R.id.signUpText);
        LinearLayout googleSignInButton = findViewById(R.id.googleSignInButton);

        loginButton.setOnClickListener(v -> loginUserWithPhp());

        if (googleSignInButton != null) {
            googleSignInButton.setOnClickListener(v -> {
                signInWithGoogle();
            });
        }

        forgotPasswordText.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class));
        });

        signUpText.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, SignupActivity.class));
        });
    }

    private void loginUserWithPhp() {
        emailLayout.setError(null);
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!EMAIL_PATTERN.matcher(email).matches()) {
            emailLayout.setError("Please use a valid gmail.com or mail.com address");
            return;
        }

        showLoading(true);

        ApiService apiService = RetrofitClient.getClient(this).create(ApiService.class);
        Call<LoginResponse> call = apiService.loginUser(email, password);

        call.enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                handleLoginResponse(response);
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                showLoading(false);
                Log.e(TAG, "Email Login API Call Failure: " + t.getMessage());
                Toast.makeText(LoginActivity.this, "Network Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    // --- START: CORRECTED GOOGLE SIGN-IN METHOD ---
    private void signInWithGoogle() {
        showLoading(true);
        String webClientId = getString(R.string.default_web_client_id);

        GetGoogleIdOption googleIdOption = new GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(true) // Set to true for login
                .setServerClientId(webClientId)
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
                handleLoginResponse(response);
            }
            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                showLoading(false);
                Log.e(TAG, "Google Auth API Call Failure: " + t.getMessage());
                Toast.makeText(LoginActivity.this, "Network Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void handleLoginResponse(Response<LoginResponse> response) {
        showLoading(false);
        if (response.isSuccessful() && response.body() != null) {
            LoginResponse loginResponse = response.body();
            Toast.makeText(LoginActivity.this, loginResponse.getMessage(), Toast.LENGTH_SHORT).show();
            if (loginResponse.isStatus() && loginResponse.getUser() != null) {
                int userId = loginResponse.getUser().getId();
                String email = loginResponse.getUser().getEmail();
                String fullname = loginResponse.getUser().getFullname();
                String token = loginResponse.getToken();
                Log.d(DEBUG_TAG, "Login successful. User ID from server is: " + userId);
                sessionManager.createLoginSession(userId, email, fullname, token);
                navigateToCorrectHome(email);
            }
        } else {
            Toast.makeText(LoginActivity.this, "Login failed. Invalid credentials.", Toast.LENGTH_LONG).show();
        }
    }

    private void navigateToCorrectHome(String email) {
        if ("admin123@gmail.com".equals(email)) {
            startActivity(new Intent(LoginActivity.this, AdminhomeActivity.class));
        } else {
            startActivity(new Intent(LoginActivity.this, HomeActivity.class));
        }
        finishAffinity();
    }

    private void showLoading(boolean isLoading) {
        if (isLoading) {
            progressBar.setVisibility(View.VISIBLE);
            loginButton.setEnabled(false);
        } else {
            progressBar.setVisibility(View.GONE);
            loginButton.setEnabled(true);
        }
    }
}