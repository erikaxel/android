package io.lucalabs.expenses.activities.firebase;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.CookieManager;
import android.widget.Toast;

import com.firebase.client.FirebaseException;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;

import io.lucalabs.expenses.R;
import io.lucalabs.expenses.activities.LoginActivity;
import io.lucalabs.expenses.activities.SettingsActivity;
import io.lucalabs.expenses.models.User;

public abstract class FirebaseActivity extends AppCompatActivity {

    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private FirebaseAuth mAuth;
    private static final String TAG = "FirebaseActivity";
    private boolean displayDeleteIcon;

    @Override
    protected void onStart() {
        super.onStart();

        if (mAuthStateListener == null)
            setAuthStateListener();

        mAuth = FirebaseAuth.getInstance();
        mAuth.addAuthStateListener(mAuthStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mAuthStateListener != null)
            mAuth.removeAuthStateListener(mAuthStateListener);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if(displayDeleteIcon)
            getMenuInflater().inflate(R.menu.main_menu_with_delete, menu);
        else
            getMenuInflater().inflate(R.menu.main_menu, menu);


        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.delete_option:
                onDeleteAction();
                break;
            case R.id.logout_option:
                AuthUI.getInstance()
                        .signOut(this)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            public void onComplete(@NonNull Task<Void> task) {
                                // user is now signed out
                                // Deprecated, but available for API 19
                                CookieManager.getInstance().removeAllCookie();
                                FirebaseAuth.getInstance().signOut();
                                finish();
                                startActivity(new Intent(FirebaseActivity.this, LoginActivity.class));
                            }
                        });
                break;
            case R.id.settings_option:
                startActivity(new Intent(FirebaseActivity.this, SettingsActivity.class));
                break;
            default:
                Toast.makeText(this, "An unknown option was selected", Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }


    // ActionBar item actions
    protected void onDeleteAction(){
        // Do nothing unless overridden
    };

    protected void displayDeleteIcon(){
        displayDeleteIcon = true;
    }

    private void setAuthStateListener() {
        try {
            mAuthStateListener = new FirebaseAuth.AuthStateListener() {

                @Override
                public void onAuthStateChanged(@NonNull FirebaseAuth auth) {
                    FirebaseUser user = auth.getCurrentUser();
                    if (user != null) {
                        Log.i(TAG, "User is not null");
                        auth.getCurrentUser().getToken(false).addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {

                            @Override
                            public void onComplete(@NonNull Task<GetTokenResult> task) {
                                Log.i(TAG, "Setting token " + task.getResult().getToken());
                                User.setToken(FirebaseActivity.this, task.getResult().getToken());
                            }
                        });

                    } else {
                        Log.i(TAG, "Setting token to null");
                        User.setToken(FirebaseActivity.this, null);
                    }
                }
            };
        } catch (FirebaseException e) {
            Toast.makeText(getBaseContext(), "Failed to authenticate user. Please login", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }
}
