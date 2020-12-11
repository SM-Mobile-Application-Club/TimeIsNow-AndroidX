package com.MAD.TimeIsNow;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.MAD.TimeIsNow.databinding.FragmentSignUpBinding;
import com.MAD.TimeIsNow.helper.PhoneNumberTextWatcher;
import com.MAD.TimeIsNow.helper.shortcuts;
import com.github.razir.progressbutton.DrawableButton;
import com.github.razir.progressbutton.DrawableButtonExtensionsKt;
import com.github.razir.progressbutton.ProgressParams;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.i18n.phonenumbers.AsYouTypeFormatter;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

import java.util.concurrent.Executor;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;

public class SignUpFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = "SIGN-UP";
    private FragmentSignUpBinding binding;
    private NavController navController;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();

    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSignUpBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.buttonBackToSignIn.setOnClickListener(this);
        binding.buttonCreateNewUser.setOnClickListener(this);
        navController = Navigation.findNavController(view);


        binding.newPhoneNumber.getEditText().addTextChangedListener(new PhoneNumberTextWatcher(binding.newPhoneNumber));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    // Progress Methods
    private void showProgress(final Button button) {

        DrawableButtonExtensionsKt.showProgress(button, new Function1<ProgressParams, Unit>() {
            @Override
            public Unit invoke(ProgressParams progressParams) {
                progressParams.setProgressColor(Color.WHITE);
                progressParams.setGravity(DrawableButton.GRAVITY_CENTER);
                return Unit.INSTANCE;
            }
        });

        button.setEnabled(false);
    }
    private void hideProgress(final Button button, String response) {
        button.setEnabled(true);
        DrawableButtonExtensionsKt.hideProgress(button, response);
    }

    // Validation
    private Boolean validateForm() {
        boolean valid = true;

        String email = shortcuts.getText(binding.newEmail);

        if (email.isEmpty()) {
            binding.newEmail.setError("Missing Email");
            valid =  false;
        }
        else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.newEmail.setError("Not a valid email");
            valid = false;
        }
        else {
            binding.newEmail.setError(null);
            binding.newEmail.setErrorEnabled(false);
        }

        String password = shortcuts.getText(binding.newPassword);

        if (password.isEmpty()) {
            binding.newPassword.setError("Missing Password");
            valid = false;
        }
        else {
            binding.newPassword.setError(null);
            binding.newPassword.setErrorEnabled(false);
        }

        String name = shortcuts.getText(binding.newFullName);

        if(name.isEmpty()) {
            binding.newFullName.setError("Missing Name");
            valid = false;
        }
        else {
            binding.newPassword.setError(null);
            binding.newPassword.setErrorEnabled(false);
        }

        String phone = shortcuts.getText(binding.newPhoneNumber);

        if (!phone.isEmpty()) {
            phone = phone.replace("-", " ");
            PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
            try {
                Phonenumber.PhoneNumber NumberProto = phoneUtil.parse(phone, "US");
                if(!phoneUtil.isValidNumber(NumberProto)) {
                    binding.newPhoneNumber.setErrorEnabled(true);
                    binding.newPhoneNumber.setError("Not a valid phone number");
                    valid = false;
                }
            } catch (NumberParseException e) {
                Log.d(TAG, "phoneParser:failure", e);
            }
        }
        else {
            binding.newPhoneNumber.setError(null);
            binding.newPhoneNumber.setErrorEnabled(false);
        }

        return valid;
    }

    // Logic
    public void createUser () {

        //Setting Up Progress Bar
        showProgress(binding.buttonCreateNewUser);


        //Validate Login Info
        if (!validateForm()) {
            hideProgress(binding.buttonCreateNewUser, "GO");
            return;
        }

        String email = shortcuts.getText(binding.newEmail);
        String password = shortcuts.getText(binding.newPassword);
        String phone = shortcuts.getText(binding.newPhoneNumber);
        String name = shortcuts.getText(binding.newFullName);

        if(!phone.isEmpty()) {
            hideProgress(binding.buttonCreateNewUser, "GO");
            openVerifyOTP(phone, email, password, name);
        }
        else {
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                Log.d(TAG, "createUserWithEmail:success");
                                FirebaseUser user = mAuth.getCurrentUser();
                                // updating profile info
                                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                        .setDisplayName(name)
                                        .build();

                                user.updateProfile(profileUpdates)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    Log.d(TAG, "User profile updated.");
                                                    openHome();
                                                }
                                            }
                                        });

                            } else {
                                hideProgress(binding.buttonCreateNewUser, "GO");
                                String errorCode = ((FirebaseAuthException) task.getException()).getErrorCode();
                                switch (errorCode) {
                                    case "ERROR_ACCOUNT_EXISTS_WITH_DIFFERENT_CREDENTIAL":
                                    case "ERROR_EMAIL_ALREADY_IN_USE":
                                    case "ERROR_CREDENTIAL_ALREADY_IN_USE":
                                        Snackbar.make(getView(), "Account already exists", Snackbar.LENGTH_SHORT).show();
                                        binding.newEmail.setError("Account already exists");
                                        binding.newEmail.requestFocus();
                                        break;
                                    case "ERROR_WEAK_PASSWORD":
                                        Snackbar.make(getView(), "Password must be at least 6 characters", Snackbar.LENGTH_SHORT).show();
                                        binding.newPassword.setError("Weak password");
                                        binding.newPassword.requestFocus();
                                        binding.newPassword.getEditText().setText("");
                                        break;
                                }
                                Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            }
                        }
                    });
        }
    }

    public void openHome() {
        navController.navigate(SignUpFragmentDirections.actionSignUpFragmentToHomeFragment());
    }

    public void openSignIn() {
        navController.navigate(SignUpFragmentDirections.actionSignUpFragmentToSignInFragment());
    }

    public void openVerifyOTP(String phone, String email, String password, String name) {
        // Phone Parser
        PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
        phone = phone.replace("-", " ");
        try {
            Phonenumber.PhoneNumber NumberProto = phoneUtil.parse(phone, "US");
            phone = phoneUtil.format(NumberProto, PhoneNumberUtil.PhoneNumberFormat.E164);
        } catch (NumberParseException e) {
            Log.d(TAG, "phoneParser:failure", e);
            return;
        }

        navController.navigate(SignUpFragmentDirections.actionSignUpFragmentToOTPFragment(phone, name, email, password, true));

    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if(id == R.id.button_createNewUser) createUser();
        if(id == R.id.button_backToSignIn) openSignIn();
    }
}