package com.MAD.TimeIsNow;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.ActionCodeUrl;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.PendingDynamicLinkData;

public class MainActivity extends AppCompatActivity {
    private NavController navController;
    private FirebaseDynamicLinks dynamicLinks = FirebaseDynamicLinks.getInstance();
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.navController = Navigation.findNavController(this, R.id.nav_host_fragment);


        if (mAuth.getCurrentUser() != null) {
            NoResetNavigation();
        } else {
            dynamicLinks.getDynamicLink(getIntent()).addOnSuccessListener(MainActivity.this, new OnSuccessListener<PendingDynamicLinkData>() {
                @Override
                public void onSuccess(PendingDynamicLinkData pendingDynamicLinkData) {
                    // Get deep link from result (may be null if no link is found)
                    Uri deepLink = null;
                    if (pendingDynamicLinkData != null) {
                        deepLink = pendingDynamicLinkData.getLink();
                    }

                    if (deepLink != null) {
                        ActionCodeUrl url = ActionCodeUrl.parseLink(deepLink.toString());

                        int operation = url.getOperation();
                        String code = url.getCode();

                        if (operation == 0) {
                            mAuth.verifyPasswordResetCode(code)
                                    .addOnCompleteListener(new OnCompleteListener<String>() {
                                        @Override
                                        public void onComplete(@NonNull Task<String> task) {
                                            if (task.isSuccessful())
                                                setNewPassword(code, task.getResult());
                                            else {
                                                Snackbar.make(findViewById(android.R.id.content), "Invalid link", Snackbar.LENGTH_SHORT).show();
                                                NoResetNavigation();
                                            }
                                        }
                                    });
                        } else
                            NoResetNavigation();
                    } else {
                        Log.d("Dynamic Link Hook", "Received Deep Link Data: " + null);
                        NoResetNavigation();
                    }
                }
            })
                    .addOnFailureListener(MainActivity.this, new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w("Dynamic Link Hook", "getDynamicLink:onFailure", e);
                            NoResetNavigation();
                        }
                    });
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        return navController.navigateUp() || super.onSupportNavigateUp();
    }

    public void setNewPassword(String code, String email) {
        navController.navigate(SplashFragmentDirections.SplashToReset(code, email, 0));
    }


    public void NoResetNavigation() {
        if(mAuth.getCurrentUser() != null) navController.navigate(SplashFragmentDirections.SplashToHome());
        else navController.navigate(SplashFragmentDirections.SplashToAuth());
    }

}