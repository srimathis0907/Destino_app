package com.simats.weekend;

import android.animation.Animator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log; // Import Log
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.airbnb.lottie.LottieAnimationView;

public class MainActivity extends AppCompatActivity {

    // --- UPDATED FRAME NUMBERS ---
    private static final int SEGMENT_1_START_FRAME = 0;
    private static final int SEGMENT_1_END_FRAME = 68;   // End of initial pin animation (Your Value)
    private static final int SEGMENT_2_START_FRAME = 69;   // Start of line drawing (Frame after Seg 1 end)
    private static final int SEGMENT_2_END_FRAME = 152;  // End of line drawing (Your Value)
    private static final int SEGMENT_3_START_FRAME = 153;  // Start of final pin arrival (Frame after Seg 2 end)
    private static final int SEGMENT_3_END_FRAME = 211;  // Absolute end frame of the animation
    // --- END FRAME NUMBER UPDATE ---

    private static final String TAG = "MainActivityAnim"; // Tag for logging
    private static final int NAV_POST_TEXT_DELAY = 1000;
    private static final int TEXT_ANIM_DURATION = 800;

    private LottieAnimationView animationView;
    private TextView appTitle;
    private TextView appSlogan;
    private Animation slideUp;
    private Animation fadeIn;
    private boolean navigationScheduled = false;
    private int currentSegment = 0; // Track which segment is playing

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        animationView = findViewById(R.id.lottieAnimationView);
        appTitle = findViewById(R.id.appTitle);
        appSlogan = findViewById(R.id.appSlogan);

        slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up);
        fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);

        animationView.addAnimatorListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(@NonNull Animator animation) {
                Log.d(TAG, "Segment " + currentSegment + " started.");
            }

            @Override
            public void onAnimationEnd(@NonNull Animator animation) {
                Log.d(TAG, "Segment " + currentSegment + " ended. MaxFrame was: " + animationView.getMaxFrame());

                // Logic based on which segment just finished
                if (currentSegment == 1) {
                    Log.d(TAG, "End of Segment 1 reached, starting Segment 2.");
                    playSegment(SEGMENT_2_START_FRAME, SEGMENT_2_END_FRAME, 2);
                } else if (currentSegment == 2) {
                    Log.d(TAG, "End of Segment 2 reached, starting Segment 3.");
                    playSegment(SEGMENT_3_START_FRAME, SEGMENT_3_END_FRAME, 3);

                } else if (currentSegment == 3) {
                    Log.d(TAG, "End of Segment 3 reached, showing text and scheduling navigation.");
                    showTextAndNavigate();
                } else {
                    Log.w(TAG, "Animation ended, but currentSegment value is unexpected: " + currentSegment);
                    if (!navigationScheduled) showTextAndNavigate();
                }
            }

            @Override
            public void onAnimationCancel(@NonNull Animator animation) {
                Log.d(TAG, "Animation cancelled.");
                if (!navigationScheduled) navigateToLogin();
            }

            @Override
            public void onAnimationRepeat(@NonNull Animator animation) {}
        });

        // Start the first segment
        Log.d(TAG, "Starting Segment 1.");
        playSegment(SEGMENT_1_START_FRAME, SEGMENT_1_END_FRAME, 1);

        // Fallback navigation
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (!navigationScheduled) {
                Log.w(TAG, "Fallback timeout reached, forcing navigation.");
                navigateToLogin();
            }
        }, 12000); // 12 second fallback
    }

    private void playSegment(int startFrame, int endFrame, int segmentNumber) {
        currentSegment = segmentNumber;
        animationView.setMinAndMaxFrame(startFrame, endFrame);
        animationView.setFrame(startFrame);
        animationView.playAnimation();
        Log.d(TAG, "Playing segment " + segmentNumber + " (Frames " + startFrame + "-" + endFrame + ")");
    }

    private void showTextAndNavigate() {
        if (navigationScheduled) {
            Log.d(TAG, "showTextAndNavigate called but navigation already scheduled.");
            return;
        }
        Log.d(TAG, "Showing text and scheduling navigation.");

        appTitle.setVisibility(View.VISIBLE);
        appSlogan.setVisibility(View.VISIBLE);
        appTitle.startAnimation(slideUp);
        appSlogan.startAnimation(fadeIn);

        navigationScheduled = true;
        new Handler(Looper.getMainLooper()).postDelayed(this::navigateToLogin,
                TEXT_ANIM_DURATION + NAV_POST_TEXT_DELAY);
    }

    private void navigateToLogin() {
        if (!isFinishing() && !isDestroyed()) {
            Log.d(TAG, "Navigating to LoginActivity.");
            Intent intent = new Intent(MainActivity.this, SubscriptionActivity.class);
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();
        } else {
            Log.w(TAG, "Navigation skipped because Activity is finishing or destroyed.");
        }
    }
}