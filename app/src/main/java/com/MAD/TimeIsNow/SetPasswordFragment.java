package com.MAD.TimeIsNow;

import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.MAD.TimeIsNow.databinding.FragmentSetPasswordBinding;
import com.github.razir.progressbutton.DrawableButton;
import com.github.razir.progressbutton.DrawableButtonExtensionsKt;
import com.github.razir.progressbutton.ProgressParams;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.PhoneAuthCredential;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;


public class SetPasswordFragment extends Fragment implements View.OnClickListener{

    private NavController navController;
    private FragmentSetPasswordBinding binding;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private String code, email;
    private int flow;
    private PhoneAuthCredential cred;
    private static final String TAG = "SetNewPassword";


    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSetPasswordBinding.inflate(inflater, container, false);
        View v = binding.getRoot();
        v.setFocusableInTouchMode(true);
        v.requestFocus();
        v.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (keyCode == KeyEvent.KEYCODE_BACK) {
                        return true;
                    }
                }
                return false;
            }
        });
        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);


        flow = SetPasswordFragmentArgs.fromBundle(getArguments()).getFlow();
        if (flow == 0 && mAuth.getCurrentUser() == null) {
            code = SetPasswordFragmentArgs.fromBundle(getArguments()).getCode();
            email = SetPasswordFragmentArgs.fromBundle(getArguments()).getEmail();
        }
        binding.buttonForgetPasswordNewPassword.setOnClickListener(this);


    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    public void resetPasswordEmail() {
        if(!validateform()) return;
        String password = binding.editTextForgetPasswordNewPassword.getText().toString();

        mAuth.confirmPasswordReset(code, password)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()) {
                            login();
                        }
                        else {
                            String errorCode = ((FirebaseAuthException) task.getException()).getErrorCode();
                            Snackbar.make(getView(), "Something went wrong...", Snackbar.LENGTH_SHORT).show();
                            hideProgress(binding.buttonForgetPasswordNewPassword, "OK");
                        }
                    }
                });
    }

    public void resetPasswordPhone() {
        if(!validateform()) return;
        String password = binding.editTextForgetPasswordNewPassword.getText().toString();

        mAuth.getCurrentUser().updatePassword(password)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            openPasswordSuccess();
                        }
                        else {
                            hideProgress(binding.buttonForgetPasswordNewPassword, "OK");
                            String errorCode = ((FirebaseAuthException) task.getException()).getErrorCode();
                            if(errorCode.equals("ERROR_USER_DISABLED")) Snackbar.make(getView(), "Account disabled", Snackbar.LENGTH_SHORT).show();
                            else Snackbar.make(getView(), "Something went wrong...", Snackbar.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private Boolean validateform() {
        String password = binding.editTextForgetPasswordNewPassword.getText().toString(),
                confirmedpassword = binding.editTextForgetPasswordConfirmPassword.getText().toString();
        if(password.isEmpty()) {
            binding.editTextForgetPasswordNewPassword.setError("Cannot be empty");
            return false;
        }
        else if(confirmedpassword.isEmpty()) {
            binding.editTextForgetPasswordConfirmPassword.setError("Cannot be empty");
            return false;
        }
        else if (!password.equals(confirmedpassword)) {
            binding.editTextForgetPasswordConfirmPassword.setError("Passwords do not match");
            return false;
        }
        else if (password.length() < 6) {
            binding.editTextForgetPasswordNewPassword.setError("Passwords must be at least 6 characters");
            return false;
        }

        showProgress(binding.buttonForgetPasswordNewPassword);
        return true;
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

    // Login Helper
    private void login() {
        mAuth.signInWithEmailAndPassword(email, binding.editTextForgetPasswordNewPassword.getText().toString())
                .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            openPasswordSuccess();
                        } else {
                            Log.w(TAG, "signInWithEmailfailure", task.getException());
                            String errorCode = ((FirebaseAuthException) task.getException()).getErrorCode();

                            // Error Handling
                            switch (errorCode) {
                                case "ERROR_INVALID_CUSTOM_TOKEN":
                                case "ERROR_INVALID_CREDENTIAL":
                                case "ERROR_CUSTOM_TOKEN_MISMATCH":
                                case "ERROR_WRONG_PASSWORD":
                                case "ERROR_USER_MISMATCH":
                                case "ERROR_USER_NOT_FOUND":
                                case "ERROR_INVALID_EMAIL":
                                case "ERROR_INVALID_USER_TOKEN":
                                    Snackbar.make(getView(), "Something went wrong...", Snackbar.LENGTH_SHORT).show();
                                    break;

                                case "ERROR_USER_DISABLED":
                                    Snackbar.make(getView(), "Account disabled", Snackbar.LENGTH_SHORT).show();
                                    break;
                            }
                            hideProgress(binding.buttonForgetPasswordNewPassword, "OK");
                        }
                    }
                });
    }

    void openPasswordSuccess() {
        navController.navigate(SetPasswordFragmentDirections.actionSetPasswordFragmentToPasswordSuccessFragment());
    }


    @Override
    public void onClick(View view) {
        int id = view.getId();
        if(id == R.id.button_forgetPasswordNewPassword) {
           if(flow == 0)resetPasswordEmail();
           else resetPasswordPhone();
        }
    }
}