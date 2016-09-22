package com.autocounting.autocounting.views.widgets;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.autocounting.autocounting.MainActivity;
import com.autocounting.autocounting.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;

public class LoginButton extends Button implements View.OnClickListener {

    private EditText mailField;
    private EditText passwordField;
    private ProgressDialog progressDialog;

    private Activity contextActivity;
    private FirebaseUser fbUser;
    private FirebaseAuth auth;

    public LoginButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setup(EditText mailField, EditText passwordField,
                      FirebaseAuth auth, Activity contextActivity) {
        this.mailField = mailField;
        this.passwordField = passwordField;
        this.auth = auth;
        this.contextActivity = contextActivity;

        setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        attemptSignIn(mailField.getText().toString(), passwordField.getText().toString());
    }

    private void attemptSignIn(String email, String password) {
        progressDialog = new ProgressDialog(contextActivity);
        progressDialog.setMessage("Logger inn …");
        progressDialog.show();
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(contextActivity, new OnCompleteListener<AuthResult>() {

                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            fbUser = FirebaseAuth.getInstance().getCurrentUser();
                            fbUser.getToken(true).addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {

                                @Override
                                public void onComplete(@NonNull Task<GetTokenResult> task) {
                                    if (task.isSuccessful()) {
                                        try {
                                            User user = new User(contextActivity, task.getResult().getToken(), fbUser.getUid());
                                            user.save();
                                            Intent toMainMenu = new Intent(getContext(), MainActivity.class);
                                            contextActivity.startActivity(toMainMenu);
                                        } catch (NullPointerException e) {
                                            onFailure();
                                        }
                                    } else {
                                        onFailure();
                                    }
                                }
                            });
                        } else {
                            progressDialog.hide();
                            Toast.makeText(contextActivity, "Not even close to the right password", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void onFailure() {
        // Fail gracefully
    }
}
