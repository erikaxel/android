package com.autocounting.autocounting;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.autocounting.autocounting.models.User;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GetTokenResult;

public class LoginActivity extends Activity {

    private static final int RC_SIGN_IN = 100;
    private FirebaseAuth auth;

    private final String TAG = "Login";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        auth = FirebaseAuth.getInstance();
    }

    protected void onResume(){
        super.onResume();

        if (auth.getCurrentUser() != null) {
            Log.d(TAG, "User is " + auth.getCurrentUser().getDisplayName() + ". Logging in ...");
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
        } else {
            Log.d(TAG, "User is null. Starting login");
            startActivityForResult(
                    AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            .setProviders(
                                    AuthUI.EMAIL_PROVIDER,
                                    AuthUI.GOOGLE_PROVIDER)
                            .build(), RC_SIGN_IN);
            overridePendingTransition(0, 0);
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult with resultCode" + resultCode);
        if (requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_OK) {
                auth.getCurrentUser().getToken(false).addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
                    @Override
                    public void onComplete(@NonNull Task<GetTokenResult> task) {
                        new User(LoginActivity.this, task.getResult().getToken(), auth.getCurrentUser().getUid()).save();
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        finish();
                    }
                });
            } else {
                Toast.makeText(this, "Failed to login", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "Sign in failed (RESULT_CANCELLED)");
            }
        }
    }
}
