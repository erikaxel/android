package com.autocounting.autocounting;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.autocounting.autocounting.models.User;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GetTokenResult;

import io.ably.lib.util.Log;

public class WelcomeActivity extends Activity {

    private static final int RC_SIGN_IN = 100;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        auth = FirebaseAuth.getInstance();

        if (auth.getCurrentUser() != null) {
            startActivity(new Intent(WelcomeActivity.this, MainActivity.class));
        } else {
            startActivityForResult(
                    AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            .setProviders(
                                    AuthUI.EMAIL_PROVIDER,
                                    AuthUI.GOOGLE_PROVIDER)
                            .build(), RC_SIGN_IN);
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        System.out.println("Get this");
        System.out.println(data);
        if (requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_OK) {
                auth.getCurrentUser().getToken(true).addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
                    @Override
                    public void onComplete(@NonNull Task<GetTokenResult> task) {
                        new User(WelcomeActivity.this, task.getResult().getToken(), auth.getCurrentUser().getUid()).save();
                        startActivity(new Intent(WelcomeActivity.this, MainActivity.class));
                        finish();
                    }
                });
            } else {
                Log.d("Callback", "Something wrong happened");
            }
        }
    }
}
