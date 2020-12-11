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

import com.MAD.TimeIsNow.databinding.FragmentSignInBinding;
import com.github.razir.progressbutton.DrawableButton;
import com.github.razir.progressbutton.DrawableButtonExtensionsKt;
import com.github.razir.progressbutton.ProgressParams;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;

public class SignInFragment extends Fragment implements View.OnClickListener {

    private FragmentSignInBinding binding;
    private NavController navController;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();

    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSignInBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.buttonNewUser.setOnClickListener(this);
        binding.buttonForgotPassword.setOnClickListener(this);
        binding.buttonLogin.setOnClickListener(this);
        navController = Navigation.findNavController(view);
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

    public void openForgetPassword() {
        navController.navigate(SignInFragmentDirections.actionSignInFragmentToForgetPasswordFragment());
    }

    // Validation
    private Boolean validateForm() {
        boolean valid = true;

        String email = binding.email.getEditText().getText().toString();

        if (email.isEmpty()) {
            binding.email.setError("Missing Email");
            valid =  false;
        }
        else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.email.setError("Not a valid email");
            valid = false;
        }
        else {
            binding.email.setError(null);
            binding.email.setErrorEnabled(false);
        }

        String password = binding.password.getEditText().getText().toString();

        if (password.isEmpty()) {
            binding.password.setError("Missing Password");
            valid = false;
        }
        else {
            binding.password.setError(null);
            binding.password.setErrorEnabled(false);
        }
        return valid;
    }

    // Logic
    public void loginUser () {

        //Setting Up Progress Bar
        showProgress(binding.buttonLogin);


        //Validate Login Info
        if (!validateForm()) {
            hideProgress(binding.buttonLogin, "GO");
            return;
        }

        String email = binding.email.getEditText().getText().toString().trim();
        String password = binding.password.getEditText().getText().toString().trim();


        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            hideProgress(binding.buttonLogin, "GO");
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("sign-in", "signInWithEmail:success");

                            openHome();
                        } else {
                            // If sign in fails, display a message to the user.
                            hideProgress(binding.buttonLogin, "GO");

                            // Error Handling
                            Log.w("sign-in", "signInWithEmail:failure", task.getException());
                            String errorCode = ((FirebaseAuthException) task.getException()).getErrorCode();
                            switch (errorCode) {

                                case "ERROR_INVALID_CUSTOM_TOKEN":

                                case "ERROR_INVALID_CREDENTIAL":

                                case "ERROR_CUSTOM_TOKEN_MISMATCH":
                                    Snackbar.make(getView(), "Something went wrong...", Snackbar.LENGTH_SHORT).show();
                                    break;

                                case "ERROR_WRONG_PASSWORD":
                                    Snackbar.make(getView(), "Wrong password", Snackbar.LENGTH_SHORT).show();
                                    binding.password.setError("Wrong Password");
                                    binding.password.requestFocus();
                                    binding.password.getEditText().setText("");
                                    break;

                                case "ERROR_USER_MISMATCH":

                                case "ERROR_USER_NOT_FOUND":

                                case "ERROR_INVALID_EMAIL":

                                case "ERROR_INVALID_USER_TOKEN":
                                    Snackbar.make(getView(), "Email or password is incorrect", Snackbar.LENGTH_SHORT).show();
                                    break;

                                case "ERROR_USER_DISABLED":
                                    Snackbar.make(getView(), "Account disabled", Snackbar.LENGTH_SHORT).show();
                                    break;
                            }
                        }
                    }
                });
    }

    public void openHome() {
        navController.navigate(SignInFragmentDirections.actionSignInFragmentToHomeFragment());
    }

    public void openSignUp() {
        navController.navigate(SignInFragmentDirections.actionSignInFragmentToSignUpFragment());
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if(id == R.id.button_forgotPassword) openForgetPassword();
        if(id == R.id.button_login) loginUser();
        if(id == R.id.button_newUser) openSignUp();
    }
}