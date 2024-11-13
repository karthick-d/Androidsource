package com.droideve.apps.nearbystores.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.res.ResourcesCompat;

import com.balysv.materialripple.MaterialRippleLayout;
import com.droideve.apps.nearbystores.R;
import com.droideve.apps.nearbystores.appconfig.Constances;
import com.droideve.apps.nearbystores.classes.Setting;
import com.droideve.apps.nearbystores.classes.User;
import com.droideve.apps.nearbystores.controllers.SettingsController;
import com.droideve.apps.nearbystores.controllers.sessions.SessionsController;
import com.droideve.apps.nearbystores.network.api_request.ApiRequest;
import com.droideve.apps.nearbystores.network.api_request.ApiRequestListeners;
import com.droideve.apps.nearbystores.parser.Parser;
import com.droideve.apps.nearbystores.parser.api_parser.UserParser;
import com.droideve.apps.nearbystores.utils.MessageDialog;
import com.droideve.apps.nearbystores.utils.NSLog;
import com.droideve.apps.nearbystores.utils.NSProgressDialog;
import com.droideve.apps.nearbystores.utils.NSToast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthSettings;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.hbb20.CountryCodePicker;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;

public class OtpActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "OtpActivity";
    @BindView(R.id.toolbar_title)
    TextView APP_TITLE_VIEW;
    @BindView(R.id.toolbar_subtitle)
    TextView APP_DESC_VIEW;
    @BindView(R.id.app_bar)
    Toolbar toolbar;

    @BindView(R.id.otpPhoneField)
    TextInputEditText otpPhoneField;

    @BindView(R.id.phoneDialCode)
    CountryCodePicker phoneDialCode;

    @BindView(R.id.smsMessage)
    TextView smsMessage;

    @BindView(R.id.otpCodeField)
    TextInputEditText otpCodeField;

    @BindView(R.id.resendCodeQuickBtn)
    TextView resendCodeQuickBtn;

    @BindView(R.id.sendCodeBtn)
    MaterialRippleLayout sendCodeBtn;

    @BindView(R.id.verifyCodeBtn)
    MaterialRippleLayout verifyCodeBtn;

    @BindView(R.id.verifyCodeContainer)
    CardView verifyCodeContainer;

    @BindView(R.id.sendCodeContainer)
    CardView sendCodeContainer;


    private boolean checkPhoneValidityReq = false;
    private boolean otpTest = false;

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp);
        ButterKnife.bind(this);
        initToolbar();

        //start firebase
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        APP_TITLE_VIEW.setText(getResources().getString(R.string.OTP));
        APP_TITLE_VIEW.setVisibility(View.VISIBLE);
        APP_DESC_VIEW.setVisibility(View.GONE);

        setup();


    }


    void setup(){

        try {
            checkPhoneValidityReq = getIntent().getExtras().getBoolean("checkPhoneValidityReq",false);
        }catch (Exception e){
            e.printStackTrace();
        }

        try {
            String cc = getIntent().getExtras().getString("countryCode");
            String cp = getIntent().getExtras().getString("dialCode");
            otpPhoneField.setText(getIntent().getExtras().getString("phoneNumber"));
            phoneDialCode.setDefaultCountryUsingNameCode(cc);
            phoneDialCode.resetToDefaultCountry();
        }catch (Exception e){
            e.printStackTrace();
        }

        if(otpPhoneField.getText().toString().equals("")){
            sendCodeContainer.setVisibility(View.VISIBLE);
            verifyCodeContainer.setVisibility(View.GONE);
        }else{
            sendCodeContainer.setVisibility(View.GONE);
            verifyCodeContainer.setVisibility(View.VISIBLE);
        }

        sendCodeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                delegateSMSVerification();
            }
        });

        verifyCodeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(otpTest && checkPhoneValidityReq){
                    Intent intent = new Intent();
                    intent.putExtra("phoneVerified", String.valueOf(phoneDialCode.getSelectedCountryCode().toString()+otpPhoneField.getText().toString()));
                    setResult(Activity.RESULT_OK, intent);
                    finish();
                    return;
                }


                Setting otpMethod = SettingsController.findSettingFiled("OTP_METHOD");
                if( otpMethod!=null &&  otpMethod.getValue().equals("firebase")){
                   //verify firebase SMS code
                    verifyWithFirebaseAuth();
                    return;
                }

                //prepare request params
                Map<String, String> params = new HashMap<String, String>();
                params.put("telephone", String.valueOf("+"+phoneDialCode.getSelectedCountryCode().toString()+otpPhoneField.getText().toString()));
                params.put("otpCode", String.valueOf(otpCodeField.getText().toString()));

                if(SessionsController.isLogged()){
                    params.put("user_id", String.valueOf(SessionsController.getSession().getUser().getId()));
                }

                //show proress
                NSProgressDialog.newInstance(OtpActivity.this).show(getString(R.string.loading));
                ApiRequest.newPostInstance(  checkPhoneValidityReq?Constances.API.API_OTP_VERIFFY_CODE0:Constances.API.API_OTP_VERIFFY_CODE , new ApiRequestListeners() {
                    @Override
                    public void onSuccess(Parser parser) {

                        if(NSProgressDialog.getInstance() != null)
                            NSProgressDialog.getInstance().dismiss();

                        if(parser.getSuccess() == 1){

                            if(checkPhoneValidityReq){
                                Intent intent = new Intent();
                                intent.putExtra("phoneVerified", String.valueOf(phoneDialCode.getSelectedCountryCode().toString()+otpPhoneField.getText().toString()));
                                setResult(Activity.RESULT_OK, intent);
                                finish();
                                return;
                            }

                            UserParser mUserParser = new UserParser(parser);
                            List<User> users = mUserParser.getUser();

                            if (users.size() == 0) {
                                //show errors
                                MessageDialog.showMessage(OtpActivity.this, Map.of("err", getString(R.string.auth_failed)));
                                return;
                            }

                            //save session
                            SessionsController.createSession(users.get(0), users.get(0).getToken());

                            //show message successful message
                            NSToast.show(getString(R.string.auth_sccuccful));

                            //restart the app
                            ActivityCompat.finishAffinity(OtpActivity.this);
                            startActivity(new Intent(OtpActivity.this, SplashActivity.class));

                        }else{
                            MessageDialog.showMessage(OtpActivity.this,parser.getErrors());
                        }
                    }

                    @Override
                    public void onFail(Map<String, String> errors) {
                        MessageDialog.showMessage(OtpActivity.this,errors);
                    }
                },params);

            }
        });


        resendCodeQuickBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                delegateSMSVerification();
            }
        });

        //send code to phone number
        if(!otpPhoneField.getText().toString().equals("")){
            delegateSMSVerification();
        }


    }

    private int seconds = 30;

    void sendAndstartCountDown(){

        smsMessage.setText(    String.format(   getString(R.string.smsSentOtp),  phoneDialCode.getSelectedCountryCode().toString()+otpPhoneField.getText().toString(), seconds+"s"  )  );

        new CountDownTimer(seconds * 1000L, 1000) {
            public void onTick(long millisUntilFinished) {
                smsMessage.setText(    String.format(   getString(R.string.smsSentOtp),  phoneDialCode.getSelectedCountryCode().toString()+otpPhoneField.getText().toString(), String.valueOf( (int)millisUntilFinished/1000 )+"s"  )  );
            }
            public void onFinish() {
                resendCodeQuickBtn.setEnabled(true);
                resendCodeQuickBtn.setTextColor(ResourcesCompat.getColor(getResources(),R.color.blue,null));
                smsMessage.setText(    String.format(   getString(R.string.smsSentOtp),  phoneDialCode.getSelectedCountryCode().toString()+otpPhoneField.getText().toString(), "0s"  )  );

            }
        }.start();
    }

    void delegateSMSVerification(){

        Setting otpMethod = SettingsController.findSettingFiled("OTP_METHOD");
        if( otpMethod!=null &&  otpMethod.getValue().equals("firebase")){
            sendCodeUsingFirebase();
        }else{
            sendCodeExAPICall();
        }

    }

    void sendCodeExAPICall(){

        if(otpPhoneField.getText().toString().equals("")){
            MessageDialog.showMessage(OtpActivity.this,Map.of("err",getString(R.string.pleaseEnterValidPhoneNumber)));
            return;
        }

        //prepare request params
        Map<String, String> params = new HashMap<String, String>();
        params.put("telephone", String.valueOf(phoneDialCode.getSelectedCountryCode().toString()+otpPhoneField.getText().toString()));

        if(SessionsController.isLogged()){
            params.put("user_id", String.valueOf(SessionsController.getSession().getUser().getId()));
        }
        //show proress
        NSProgressDialog.newInstance(OtpActivity.this).show(getString(R.string.loading));

        ApiRequest.newPostInstance( checkPhoneValidityReq?Constances.API.API_OTP_SEND_CODE0:Constances.API.API_OTP_SEND_CODE , new ApiRequestListeners() {
            @Override
            public void onSuccess(Parser parser) {

                if(NSProgressDialog.getInstance() != null)
                    NSProgressDialog.getInstance().dismiss();

                if(parser.getSuccess() == 1){

                    sendCodeContainer.setVisibility(View.GONE);
                    verifyCodeContainer.setVisibility(View.VISIBLE);

                    otpPhoneField.setEnabled(false);
                    sendCodeBtn.setEnabled(false);
                    resendCodeQuickBtn.setEnabled(false);
                    resendCodeQuickBtn.setTextColor(ResourcesCompat.getColor(getResources(),R.color.gray,null));

                    sendAndstartCountDown();

                }else if(parser.getSuccess() == -1){

                    MessageDialog.showMessageRegisterUser(OtpActivity.this, parser.getErrors(), new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            MessageDialog.getInstance().hide();
                            //start sign up page
                            Intent intent = new Intent(OtpActivity.this, LoginV2Activity.class);
                            intent.putExtra("signUp",true);
                            startActivity(intent);

                            //finish otp
                            finish();
                        }
                    });

                }else{
                    MessageDialog.showMessage0(OtpActivity.this, parser.getErrors(), new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            MessageDialog.getInstance().hide();
                            //finish otp
                            finish();
                        }
                    });
                }
            }

            @Override
            public void onFail(Map<String, String> errors) {

                if(NSProgressDialog.getInstance() != null)
                    NSProgressDialog.getInstance().dismiss();

                MessageDialog.showMessage(OtpActivity.this,errors);
            }
        },params);


    }


    void verifyWithFirebaseAuth(){

         PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mFireVerificationId, otpCodeField.getText().toString());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            NSLog.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = task.getResult().getUser();

                            if(checkPhoneValidityReq){
                                Intent intent = new Intent();
                                intent.putExtra("phoneVerified", String.valueOf(phoneDialCode.getSelectedCountryCode().toString()+otpPhoneField.getText().toString()));
                                setResult(Activity.RESULT_OK, intent);
                                finish();
                                return;
                            }


                            FirebaseUser user1 = FirebaseAuth.getInstance().getCurrentUser(); // Assuming you have a valid user
                            if (user != null) {
                                user.getIdToken(true)
                                        .addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
                                            @Override
                                            public void onComplete(@NonNull Task<GetTokenResult> tokenTask) {
                                                if (tokenTask.isSuccessful()) {
                                                    String idToken = tokenTask.getResult().getToken();
                                                    // You have the ID token, you can use it for various purposes.

                                                    //login using api server
                                                    firebaseAuthApi(idToken);

                                                } else {
                                                    // Handle the error.
                                                    Exception e = tokenTask.getException();
                                                    // e.getMessage() will contain the error message.
                                                }
                                            }
                                        });
                            }


                        } else {
                            // Sign in failed, display a message and update the UI
                            NSLog.w(TAG, "signInWithCredential:failure", task.getException());
                            MessageDialog.showMessage(OtpActivity.this, Map.of("err",task.getException().getMessage()));

                            sendAndstartCountDown();
                        }
                    }
                });
    }

    void firebaseAuthApi(String token){
        //prepare request params
        Map<String, String> params = new HashMap<String, String>();
        params.put("telephone", String.valueOf("+"+phoneDialCode.getSelectedCountryCode().toString()+otpPhoneField.getText().toString()));
        params.put("otpCode", String.valueOf(otpCodeField.getText().toString()));
        params.put("idToken", token);

        if(SessionsController.isLogged()){
            params.put("user_id", String.valueOf(SessionsController.getSession().getUser().getId()));
        }

        //show proress
        NSProgressDialog.newInstance(OtpActivity.this).show(getString(R.string.loading));

        //Auth with server side
        ApiRequest.newPostInstance(Constances.API.API_OTP_VERIFFY_CODE, new ApiRequestListeners() {
            @Override
            public void onSuccess(Parser parser) {

                if(NSProgressDialog.getInstance() != null)
                    NSProgressDialog.getInstance().dismiss();

                if(parser.getSuccess() == 1){

                    UserParser mUserParser = new UserParser(parser);
                    List<User> users = mUserParser.getUser();

                    if (users.size() == 0) {
                        //show errors
                        String msgErr = String.format(getString(R.string.user_not_found_otp),  ("+"+phoneDialCode.getSelectedCountryCode().toString()+otpPhoneField.getText().toString() ));
                        MessageDialog.showMessage0(OtpActivity.this, Map.of("err", msgErr), new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                MessageDialog.getInstance().hide();
                                //start sign up page
                                Intent intent = new Intent(OtpActivity.this, LoginV2Activity.class);
                                intent.putExtra("signUp",true);
                                startActivity(intent);

                                //finish otp
                                finish();
                            }
                        });

                        ( (Button) MessageDialog.getInstance().getDialog().findViewById(R.id.ok)).setText(getString(R.string.register));
                        MessageDialog.getInstance().getDialog().findViewById(R.id.ok).setVisibility(View.VISIBLE);
                        MessageDialog.getInstance().getDialog().findViewById(R.id.cancel).setVisibility(View.VISIBLE);
                        return;
                    }

                    //save session
                    SessionsController.createSession(users.get(0), users.get(0).getToken());

                    //show message successful message
                    NSToast.show(getString(R.string.auth_sccuccful));

                    //restart the app
                    ActivityCompat.finishAffinity(OtpActivity.this);
                    startActivity(new Intent(OtpActivity.this, SplashActivity.class));

                }else{
                    MessageDialog.showMessage(OtpActivity.this,parser.getErrors());
                }
            }

            @Override
            public void onFail(Map<String, String> errors) {

                if(NSProgressDialog.getInstance() != null)
                    NSProgressDialog.getInstance().dismiss();

                MessageDialog.showMessage(OtpActivity.this,errors);
            }
        },params);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            //restart the app
            if(SessionsController.isLogged()){
                ActivityCompat.finishAffinity(this);
                startActivity(new Intent(this, SplashActivity.class));
                return super.onOptionsItemSelected(item);
            }
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    String mFireVerificationId;
    PhoneAuthProvider.ForceResendingToken mFireResendToken;
    void sendCodeUsingFirebase(){

        PhoneAuthProvider.OnVerificationStateChangedCallbacks
                mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
                NSLog.d(TAG, "onVerificationCompleted:" + credential);
                signInWithPhoneAuthCredential(credential);
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                // This callback is invoked in an invalid request for verification is made,
                // for instance if the the phone number format is not valid.
                NSLog.w(TAG, "onVerificationFailed", e);

                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    // Invalid request
                    MessageDialog.showMessage(OtpActivity.this,Map.of("err",e.getMessage()));
                } else if (e instanceof FirebaseTooManyRequestsException) {
                    // The SMS quota for the project has been exceeded
                    MessageDialog.showMessage(OtpActivity.this,Map.of("err",e.getMessage()));
                }

                // Show a message and update the UI
            }

            @Override
            public void onCodeSent(@NonNull String verificationId,
                                   @NonNull PhoneAuthProvider.ForceResendingToken token) {
                // The SMS verification code has been sent to the provided phone number, we
                // now need to ask the user to enter the code and then construct a credential
                // by combining the code with a verification ID.
                NSLog.d(TAG, "onCodeSent:" + verificationId);

                // Save verification ID and resending token so we can use them later
                mFireVerificationId = verificationId;
                mFireResendToken = token;


                //+++Enable Resend Code////
                sendCodeContainer.setVisibility(View.GONE);
                verifyCodeContainer.setVisibility(View.VISIBLE);

                otpPhoneField.setEnabled(false);
                sendCodeBtn.setEnabled(false);
                resendCodeQuickBtn.setEnabled(false);
                resendCodeQuickBtn.setTextColor(ResourcesCompat.getColor(getResources(),R.color.gray,null));

                //Count down
                sendAndstartCountDown();
                //++++++++++++//

            }
        };


        NSLog.d(TAG, "PhoneNumber:" +  String.valueOf("+"+phoneDialCode.getSelectedCountryCode().toString()+otpPhoneField.getText().toString()));

        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber(  String.valueOf("+"+phoneDialCode.getSelectedCountryCode().toString()+otpPhoneField.getText().toString())  )       // Phone number to verify
                        .setTimeout(Long.valueOf(seconds), TimeUnit.SECONDS) // Timeout and unit
                        .setActivity(this)                 // (optional) Activity for callback binding
                        // If no activity is passed, reCAPTCHA verification can not be used.
                        .setCallbacks(mCallbacks)
                        // OnVerificationStateChangedCallbacks
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);

    }
    private FirebaseAuth mAuth;
private String token ="";
    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {

        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            NSLog.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = task.getResult().getUser();
                            // Update UI
                        } else {
                            // Sign in failed, display a message and update the UI
                            NSLog.w(TAG, "signInWithCredential:failure", task.getException());
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                // The verification code entered was invalid
                            }
                        }
                    }
                });
    }



    @Override
    public void onBackPressed() {
        super.onBackPressed();

        //restart the app
        if(SessionsController.isLogged()){
            ActivityCompat.finishAffinity(this);
            startActivity(new Intent(this, SplashActivity.class));
        }

    }

    public void initToolbar() {


        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setDisplayUseLogoEnabled(false);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        APP_DESC_VIEW.setVisibility(View.GONE);
        APP_TITLE_VIEW.setText(R.string.otp);
        APP_DESC_VIEW.setVisibility(View.GONE);

    }



    @Override
    public void onClick(View view) {




    }
}




