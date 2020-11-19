package com.MAD.TimeIsNow;

import android.app.Activity;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.NavHostController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.MAD.TimeIsNow.databinding.FragmentSignInBinding;
import com.MAD.TimeIsNow.databinding.FragmentSplashBinding;
import com.google.android.gms.common.SignInButton;

public class SignInFragment extends Fragment implements View.OnClickListener {

    private FragmentSignInBinding binding;
    private NavController navController;

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
    private void hideProgress(final ProgressBar progressBar, final RelativeLayout progressBarLayout) {
        progressBarLayout.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);
    }
    private void showProgress(final ProgressBar progressBar, final RelativeLayout progressBarLayout) {
        progressBarLayout.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.VISIBLE);
    }

    public void openForgetPassword() {
        navController.navigate(SignInFragmentDirections.actionSignInFragmentToForgetPasswordFragment());
    }

    public void loginUser() {

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