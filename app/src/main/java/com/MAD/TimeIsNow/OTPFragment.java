package com.MAD.TimeIsNow;

import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.MAD.TimeIsNow.databinding.FragmentOTPBinding;
import com.MAD.TimeIsNow.utilities.ActivityInterface;
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
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.auth.UserProfileChangeRequest;
import java.util.concurrent.TimeUnit;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;


public class OTPFragment extends Fragment implements View.OnClickListener{

    private NavController navController;
    private FragmentOTPBinding binding;
    private static String phone = "", email="", password="", fullname="";
    private ActivityInterface listener;

    private static int state;
    // 3 from reset
    // 0 from signup
    private static final String TAG = "PhoneAuthActivity";

    private static final String KEY_VERIFY_IN_PROGRESS = "key_verify_in_progress";
    private static final int STATE_CODE_SENT = 2;
    private static final int STATE_VERIFY_FAILED = 3;
    private static final int STATE_VERIFY_SUCCESS = 4;
    private static final int STATE_CODE_NOT_SENT = 5;
    private static final int STATE_SIGNIN_SUCCESS = 6;
    private static final int STATE_LINK_SUCCESS = 7;

    private FirebaseAuth mAuth;

    private boolean mVerificationInProgress = false;
    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;


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


    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentOTPBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);

        if (savedInstanceState != null) {
            mVerificationInProgress = savedInstanceState.getBoolean(KEY_VERIFY_IN_PROGRESS);
        }

        try{
            listener = (ActivityInterface) getContext();
        }
        catch(ClassCastException ignored) {}

        Bundle extras = getArguments();
        phone = extras.getString("phone");
        state = extras.getInt("state");
        if(state == 0) {
            email = extras.getString("email");
            password = extras.getString("password");
            fullname = extras.getString("fullname");
        }

        // Assign click listeners
        binding.buttonVerifyPhoneNumber.setOnClickListener(this);
        binding.buttonResendCODE.setOnClickListener(this);
        binding.imageViewVerifyBack.setOnClickListener(this);

        mAuth = FirebaseAuth.getInstance();

        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            @Override
            public void onVerificationCompleted(PhoneAuthCredential credential) {
                // This callback will be invoked in two situations:
                // 1 - Instant verification. In some cases the phone number can be instantly
                //     verified without needing to send or enter a verification code.
                // 2 - Auto-retrieval. On some devices Google Play services can automatically
                //     detect the incoming verification SMS and perform verification without
                //     user action.
                Log.d(TAG, "onVerificationCompleted:" + credential);
                // [START_EXCLUDE silent]
                mVerificationInProgress = false;
                // [END_EXCLUDE]

                // [START_EXCLUDE silent]
                // Update the UI and attempt sign in with the phone credential
                updateUI(STATE_VERIFY_SUCCESS, credential);
                // [END_EXCLUDE]
                signInWithPhoneAuthCredential(credential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                // This callback is invoked in an invalid request for verification is made,
                // for instance if the the phone number format is not valid.
                Log.w(TAG, "onVerificationFailed", e);
                // [START_EXCLUDE silent]
                mVerificationInProgress = false;
                // [END_EXCLUDE]

                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    updateUI(STATE_VERIFY_FAILED);
                } else updateUI(STATE_CODE_NOT_SENT);

            }

            @Override
            public void onCodeSent(@NonNull String verificationId,
                                   @NonNull PhoneAuthProvider.ForceResendingToken token) {
                // The SMS verification code has been sent to the provided phone number, we
                // now need to ask the user to enter the code and then construct a credential
                // by combining the code with a verification ID.
                Log.d(TAG, "onCodeSent:" + verificationId);

                // Save verification ID and resending token so we can use them later
                mVerificationId = verificationId;
                mResendToken = token;
                // [START_EXCLUDE]
                // Update UI
                updateUI(STATE_CODE_SENT);
                // [END_EXCLUDE]
            }
        };

        startPhoneNumberVerification(phone);

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(KEY_VERIFY_IN_PROGRESS, mVerificationInProgress);
    }

    private void startPhoneNumberVerification(String phoneNumber) {

        // [START start_phone_auth]
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,        // Phone number to verify
                5,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                getActivity(),               // Activity (for callback binding)
                mCallbacks);        // OnVerificationStateChangedCallbacks
        // [END start_phone_auth]

        mVerificationInProgress = true;
    }

    private void verifyPhoneNumberWithCode (String verificationId, String code) {
        // [START verify_with_code]
        showProgress(binding.buttonVerifyPhoneNumber);
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
        // [END verify_with_code]
        if (state==3) signInWithPhoneAuthCredential(credential);
        else linkCredentials(credential);
    }

    private void resendVerificationCode(String phoneNumber,
                                        PhoneAuthProvider.ForceResendingToken token) {
        binding.buttonResendCODE.setText("Please wait 30 seconds");
        binding.buttonResendCODE.setEnabled(false);
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,        // Phone number to verify
                30,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                getActivity(),               // Activity (for callback binding)
                mCallbacks,         // OnVerificationStateChangedCallbacks
                token);             // ForceResendingToken from callbacks
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                hideProgress(binding.buttonResendCODE, "Resend Code");
            }
        }, 30000); // 30 seconds
    }

    // Action methods
    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            // [START_EXCLUDE]
                            updateUI(STATE_SIGNIN_SUCCESS);
                            // [END_EXCLUDE]
                        } else {
                            // Sign in failed, display a message and update the UI
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            String errorCode = ((FirebaseAuthException) task.getException()).getErrorCode();
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                binding.firstPinView.setError("Invalid code.");
                            }
                            else switch (errorCode) {

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
                        }
                    }
                });
    }
    private void linkCredentials(PhoneAuthCredential credential) {
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
                                    .setDisplayName(fullname)
                                    .build();

                            user.updateProfile(profileUpdates)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful())
                                            {
                                                Log.d(TAG, "User profile updated.");
                                                mAuth.getCurrentUser().linkWithCredential(credential)
                                                        .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<AuthResult> task) {
                                                                if (task.isSuccessful()) {
                                                                    Log.d(TAG, "linkWithCredential:success");
                                                                    updateUI(STATE_LINK_SUCCESS);
                                                                }
                                                                else {
                                                                    Log.w(TAG, "linkWithCredential:failure", task.getException());
                                                                    if (task.getException() instanceof FirebaseAuthInvalidCredentialsException)
                                                                        binding.firstPinView.setError("Invalid code.");
                                                                }
                                                                hideProgress(binding.buttonVerifyPhoneNumber, "VERIFY");
                                                            }
                                                        });
                                            } else updateUI(STATE_CODE_NOT_SENT);
                                            hideProgress(binding.buttonVerifyPhoneNumber, "VERIFY");
                                        }
                                    });

                        }
                        else updateUI(STATE_CODE_NOT_SENT);
                        hideProgress(binding.buttonVerifyPhoneNumber, "VERIFY");
                    }
                });
    }


    private void updateUI(int uiState) {updateUI(uiState, null);}
    private void updateUI(int uiState, PhoneAuthCredential cred) {
        hideProgress(binding.buttonVerifyPhoneNumber, "VERIFY");
        switch (uiState) {
            case STATE_CODE_NOT_SENT:
                Snackbar.make(getView(), "Something went wrong...", Snackbar.LENGTH_SHORT).show();
                break;
            case STATE_CODE_SENT:
                Snackbar.make(getView(), "Code sent", Snackbar.LENGTH_SHORT).show();
                break;
            case STATE_VERIFY_FAILED:
                Snackbar.make(getView(), "Verification failed", Snackbar.LENGTH_SHORT).show();
                break;
            case STATE_VERIFY_SUCCESS:
                if (cred != null) {
                    if (cred.getSmsCode() != null) {
                        binding.firstPinView.setText(cred.getSmsCode());
                    }
                }
            case STATE_SIGNIN_SUCCESS:
                if(state==3) listener.ToSetPassword();
                break;
            case STATE_LINK_SUCCESS:
                listener.ToHome();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button_VerifyPhoneNumber:
                String code = binding.firstPinView.getText().toString();
                if (TextUtils.isEmpty(code)) {
                    binding.firstPinView.setError("Cannot be empty.");
                    return;
                }

                verifyPhoneNumberWithCode(mVerificationId, code);
                break;
            case R.id.button_resendCODE:
                resendVerificationCode(phone, mResendToken);
                break;
            case R.id.imageView_verifyBack:
                getActivity().onBackPressed();
                break;
        }
    }

}