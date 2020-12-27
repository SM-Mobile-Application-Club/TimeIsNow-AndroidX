package com.MAD.TimeIsNow;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.MAD.TimeIsNow.databinding.FragmentPasswordSuccessBinding;
import com.MAD.TimeIsNow.utilities.ActivityInterface;

public class PasswordSuccessFragment extends Fragment implements View.OnClickListener{

    private NavController navController;
    private FragmentPasswordSuccessBinding binding;
    private ActivityInterface listener;

    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentPasswordSuccessBinding.inflate(inflater, container, false);
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

        binding.buttonForgetPasswordLoginScreen.setOnClickListener(this);

        try{
            listener = (ActivityInterface) getContext();
        }
        catch(ClassCastException ignored) {}
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.button_forgetPasswordLoginScreen) {
            listener.ToHome();
        }
    }
}