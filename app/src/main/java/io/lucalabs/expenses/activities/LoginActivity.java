package io.lucalabs.expenses.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;

import java.util.Arrays;
import java.util.List;

import io.lucalabs.expenses.R;
import io.lucalabs.expenses.models.User;
import io.lucalabs.expenses.network.webapi.BackendServer;

public class LoginActivity extends FragmentActivity {

    private static final int RC_SIGN_IN = 100;
    private FirebaseAuth auth;
    private final List PROVIDERS = Arrays.asList(
            new AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build(),
            new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build(),
            new AuthUI.IdpConfig.Builder(AuthUI.FACEBOOK_PROVIDER).build()
    );

    private final String TAG = LoginActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        auth = FirebaseAuth.getInstance();
    }

    protected void onResume() {
        super.onResume();

        FirebaseUser user = auth.getCurrentUser();

        if (user == null)
            startSignIn();
        else if (!user.isEmailVerified())
            verifyUserEmail();
        else
            startActivity(new Intent(this, MainActivity.class));
    }

    private void verifyUserEmail() {
        startActivity(new Intent(this, WaitForVerificationActivity.class));
    }

    private void startSignIn() {
        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setProviders(
                                PROVIDERS)
                        .setLogo(R.mipmap.login_logo)
                        .setTheme(R.style.AppTheme)
                        .build(), RC_SIGN_IN);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.i(TAG, "onActivityResult with resultCode" + resultCode);
        if (requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_OK) {
                FirebaseUser user = auth.getCurrentUser();

                if (user == null) {
                    Log.w(TAG, "on login result: User is null");
                    return;
                }

                if (!user.isEmailVerified()) {
                    user.sendEmailVerification()
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (!task.isSuccessful()) {
                                        Toast.makeText(LoginActivity.this, "Couldn't send verification email", Toast.LENGTH_SHORT).show();
                                        return;
                                    }

                                    startActivity(new Intent(LoginActivity.this, WaitForVerificationActivity.class));
                                }
                            });
                } else {

                    user.getToken(false).addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
                                                                   @Override
                                                                   public void onComplete(@NonNull Task<GetTokenResult> task) {
                                                                       User.setFirebaseToken(LoginActivity.this, task.getResult().getToken());
                                                                       new Thread(new Runnable() {
                                                                           @Override
                                                                           public void run() {
                                                                               BackendServer.exchangeTokens(LoginActivity.this);
                                                                               BackendServer.exchangeData(LoginActivity.this);
                                                                           }
                                                                       }).start();
                                                                       startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                                                       finish();
                                                                   }
                                                               }
                    );
                }
            } else {
                Toast.makeText(this, R.string.login_please_connect_prompt, Toast.LENGTH_SHORT).show();
                Log.i(TAG, "Login failed (RESULT_CANCELLED)");
            }
        }
    }
}
