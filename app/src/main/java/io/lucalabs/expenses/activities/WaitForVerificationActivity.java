package io.lucalabs.expenses.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import io.lucalabs.expenses.R;

public class WaitForVerificationActivity extends AppCompatActivity {

    private FirebaseUser mUser;
    private boolean isVerifying;
    private static final String TAG = WaitForVerificationActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wait_for_verification);

        mUser = FirebaseAuth.getInstance().getCurrentUser();

        ((TextView) findViewById(R.id.verify_email_text)).setText(getString(R.string.email_needs_verification_notice,
                mUser.getEmail()));

        findViewById(R.id.resend_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                view.setEnabled(false);

                mUser.sendEmailVerification()
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful())
                                    Toast.makeText(WaitForVerificationActivity.this, "Email resent", Toast.LENGTH_SHORT).show();
                                else
                                    Toast.makeText(WaitForVerificationActivity.this, "Couldn't send email", Toast.LENGTH_SHORT).show();

                                view.setEnabled(true);
                            }
                        });
            }
        });

        findViewById(R.id.refresh_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view.setEnabled(false);
                Toast.makeText(WaitForVerificationActivity.this, getString(R.string.refreshing), Toast.LENGTH_SHORT).show();
                mUser.reload();

                if (mUser.isEmailVerified()) {
                    onVerify();
                }

                view.setEnabled(true);
            }
        });

        findViewById(R.id.cancel_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(WaitForVerificationActivity.this, LoginActivity.class));
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        isVerifying = true;

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (isVerifying) {
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    mUser.reload();

                    if (mUser.isEmailVerified())
                        runOnUiThread(new Thread(new Runnable() {
                            @Override
                            public void run() {
                                onVerify();
                            }
                        }));
                }
            }
        }).start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        isVerifying = false;
    }

    private void onVerify() {
        Toast.makeText(WaitForVerificationActivity.this, getString(R.string.email_verified), Toast.LENGTH_SHORT).show();
        startActivity(new Intent(WaitForVerificationActivity.this, MainActivity.class));
    }
}
