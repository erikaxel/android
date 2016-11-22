package com.autocounting.autocounting;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;
import java.util.Arrays;
import java.util.List;

import com.autocounting.autocounting.models.User;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GetTokenResult;

public class LoginActivity extends Activity {

    private static final int RC_SIGN_IN = 100;
    private FirebaseAuth auth;
    private final List PROVIDERS = Arrays.asList(
            new AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build(),
            new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build(),
            new AuthUI.IdpConfig.Builder(AuthUI.FACEBOOK_PROVIDER).build()
    );

    private final String TAG = "Login";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        auth = FirebaseAuth.getInstance();
    }

    protected void onResume(){
        super.onResume();

        if (auth.getCurrentUser() != null) {
            Log.i(TAG, "User is " + auth.getCurrentUser().getDisplayName() + ". Logging in ...");
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
        } else {
            Log.i(TAG, "User is null. Starting login");
            startActivityForResult(
                    AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            .setProviders(
                                    PROVIDERS)
                            .setTheme(R.style.AppTheme)
                            .build(), RC_SIGN_IN);
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.i(TAG, "onActivityResult with resultCode" + resultCode);
        if (requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_OK) {
                auth.getCurrentUser().getToken(false).addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
                    @Override
                    public void onComplete(@NonNull Task<GetTokenResult> task) {
                        new User(LoginActivity.this,
                                task.getResult().getToken(),
                                auth.getCurrentUser().getUid(),
                                auth.getCurrentUser().getEmail()).save();
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        finish();
                    }
                });
            } else {
                Toast.makeText(this, "Login failed", Toast.LENGTH_SHORT).show();
                Log.i(TAG, "Login failed (RESULT_CANCELLED)");
            }
        }
    }
}
