package com.autocounting.autocounting.activities.firebase;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.autocounting.autocounting.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;

public class FirebaseActivity extends AppCompatActivity {

    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseAuth auth;
    private static final String AUTH_TAG = "Authentication";

    @Override
    protected void onStart() {
        super.onStart();

        if (authStateListener == null)
            setAuthStateListener();

        auth = FirebaseAuth.getInstance();
        auth.addAuthStateListener(authStateListener);
    }

    @Override
    protected void onStop(){
        super.onStop();
        if(authStateListener != null)
            auth.removeAuthStateListener(authStateListener);
    }

    private void setAuthStateListener(){
        authStateListener = new FirebaseAuth.AuthStateListener() {

            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth auth) {
                FirebaseUser user = auth.getCurrentUser();
                if (user != null) {
                    Log.i(AUTH_TAG, "User is not null");
                    auth.getCurrentUser().getToken(false).addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {

                        @Override
                        public void onComplete(@NonNull Task<GetTokenResult> task) {
                            Log.i(AUTH_TAG, "Setting new token");
                            User.setToken(FirebaseActivity.this, task.getResult().getToken());
                        }
                    });

                } else {
                    Log.i(AUTH_TAG, "Setting token to null");
                    User.setToken(FirebaseActivity.this, null);
                }
            }
        };
    }
}
