package com.MAD.TimeIsNow;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.MAD.TimeIsNow.utilities.ActivityInterface;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;

public class ResetActivity extends AppCompatActivity implements ActivityInterface {

    private String code, email;
    private int flow;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private PhoneAuthCredential cred;
    private static final String TAG = "SetNewPassword";
    private boolean tr = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset);

        SetPasswordFragment frag = new SetPasswordFragment();
        frag.setArguments(getIntent().getExtras());
    }

    @Override
    public void ToHome() {
        Intent i = new Intent(this, HomeActivity.class);
        startActivity(i);
    }
}