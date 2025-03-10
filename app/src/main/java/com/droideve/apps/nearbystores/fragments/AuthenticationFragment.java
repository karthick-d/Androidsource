package com.droideve.apps.nearbystores.fragments;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Html;
import android.util.Base64;

import com.droideve.apps.nearbystores.activities.LoginV2Activity;
import com.droideve.apps.nearbystores.activities.OtpActivity;
import com.droideve.apps.nearbystores.appconfig.AppConfig;
import com.droideve.apps.nearbystores.classes.Setting;
import com.droideve.apps.nearbystores.controllers.SettingsController;
import com.droideve.apps.nearbystores.databinding.V2FragmentProfileLoginBinding;
import com.droideve.apps.nearbystores.firebase_auth.FireAuthResult;
import com.droideve.apps.nearbystores.firebase_auth.FirebaseAuthPresenter;
import com.droideve.apps.nearbystores.firebase_auth.FirebaseAuthPresenterListeners;
import com.droideve.apps.nearbystores.network.api_request.ApiRequest;
import com.droideve.apps.nearbystores.network.api_request.ApiRequestListeners;
import com.droideve.apps.nearbystores.parser.Parser;
import com.droideve.apps.nearbystores.utils.NSLog;

import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.databinding.DataBindingUtil;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.balysv.materialripple.MaterialRippleLayout;
import com.bumptech.glide.Glide;
import com.droideve.apps.nearbystores.AppController;
import com.droideve.apps.nearbystores.location.GPStracker;
import com.droideve.apps.nearbystores.R;
import com.droideve.apps.nearbystores.activities.MainActivity;
import com.droideve.apps.nearbystores.activities.SplashActivity;
import com.droideve.apps.nearbystores.appconfig.AppContext;
import com.droideve.apps.nearbystores.appconfig.Constances;
import com.droideve.apps.nearbystores.classes.User;
import com.droideve.apps.nearbystores.controllers.sessions.GuestController;
import com.droideve.apps.nearbystores.controllers.sessions.SessionsController;
import com.droideve.apps.nearbystores.helper.CommunFunctions;
import com.droideve.apps.nearbystores.network.ServiceHandler;
import com.droideve.apps.nearbystores.network.VolleySingleton;
import com.droideve.apps.nearbystores.network.api_request.SimpleRequest;
import com.droideve.apps.nearbystores.parser.api_parser.UserParser;
import com.droideve.apps.nearbystores.parser.tags.Tags;
import com.droideve.apps.nearbystores.utils.ImageUtils;
import com.droideve.apps.nearbystores.utils.MessageDialog;
import com.droideve.apps.nearbystores.utils.NSProgressDialog;
import com.droideve.apps.nearbystores.utils.NSToast;
import com.droideve.apps.nearbystores.utils.Translator;
import com.droideve.apps.nearbystores.utils.Utils;
import com.droideve.apps.nearbystores.views.CustomDialog;
import com.github.siyamed.shapeimageview.CircularImageView;
import com.google.android.material.textfield.TextInputEditText;
import com.hbb20.CountryCodePicker;
//import com.wuadam.awesomewebview.AwesomeWebView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.droideve.apps.nearbystores.appconfig.AppConfig.APP_DEBUG;

public class AuthenticationFragment extends Fragment implements ImageUtils.PrepareImagesData.OnCompressListner, AuthenticationFragmentListeners {


    @BindView(R.id.loginLayout)
    LinearLayout loginLayout;

    @BindView(R.id.signupLayout)
    LinearLayout signupLayout;

    @BindView(R.id.changePasswordLayout)
    LinearLayout changePasswordLayout;

    @BindView(R.id.createAccountBtn)
    TextView createAccountBtn;

    @BindView(R.id.loginOTP)
    TextView loginOTP;
    @BindView(R.id.loginOTPContainer)
    LinearLayout loginOTPContainer;

    @BindView(R.id.signupPhoneContainer)
    LinearLayout signupPhoneContainer;

    @BindView(R.id.haveAccountBtn)
    TextView haveAccountBtn;

    @BindView(R.id.forget_password)
    TextView forget_password;

    //Login layout
    @BindView(R.id.login)
    TextInputEditText loginTxt;

    @BindView(R.id.password)
    TextInputEditText paswordTxt;

    @BindView(R.id.phone_signup)
    TextInputEditText phoneSignupTxt;

    @BindView(R.id.phoneDialCode)
    CountryCodePicker phoneDialCode;

    @BindView(R.id.connect)
    MaterialRippleLayout loginBtn;

    //Signup Layout
    @BindView(R.id.login_signup)
    TextInputEditText loginSignupTxt;

    @BindView(R.id.email_signup)
    TextInputEditText emailSignupTxt;

    @BindView(R.id.full_name_signup)
    TextInputEditText fullNameSignupTxt;

    @BindView(R.id.password_signup)
    TextInputEditText passwordSignupTxt;

    @BindView(R.id.btn_signup)
    MaterialRippleLayout btnSignUp;

    @BindView(R.id.userimage)
    CircularImageView userimage;

    @BindView(R.id.takePicture)
    ImageView takePicture;

    @BindView(R.id.firebaseAuthContainer)
    LinearLayout firebaseAuthContainer;

    private ProgressDialog mPdialog;
    private GPStracker gps;
    private RequestQueue queue;
    private CustomDialog mDialogError;
    private int GALLERY_REQUEST = 103;
    private Uri imageToUpload = null;


    @BindView(R.id.term_of_uses_login)
    TextView term_of_uses_login;

    //declare auth presenter
    private FirebaseAuthPresenter mFirebaseAuthPresenter;

    // newInstance constructor for creating fragment with arguments
    public static AuthenticationFragment newInstance(int page, String title) {
        AuthenticationFragment fragmentFirst = new AuthenticationFragment();
        Bundle args = new Bundle();
        args.putInt("id", page);
        args.putString("title", title);
        fragmentFirst.setArguments(args);
        return fragmentFirst;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        gps = new GPStracker(this.getActivity());
        queue = VolleySingleton.getInstance(getActivity()).getRequestQueue();

    }

    @OnClick(R.id.facebookLoginBtn)
    public void onTapFacebookLoginBtn() {
        if (mFirebaseAuthPresenter != null) {
            mFirebaseAuthPresenter.signInWithFacebook();
        }
    }

    @OnClick(R.id.googleLoginBtn)
    public void onTapGoogleLoginBtn() {
        if (mFirebaseAuthPresenter != null) {
            mFirebaseAuthPresenter.signInWithGoogle();
        }
    }

    public void requestPermissionForReadExtertalStorage() {

        try {
            ActivityCompat.requestPermissions((Activity) getContext(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    1);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

    }


    public boolean checkPermissionForReadExtertalStorage() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int result = getContext().checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE);
            return result == PackageManager.PERMISSION_GRANTED;
        }
        return false;
    }

    ActivityResultLauncher<Intent> mStartForResult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent intent = result.getData();

                        try {
                            String verifiedPhone = intent.getExtras().getString("phoneVerified");
                            if (!verifiedPhone.equals("")){
                                askOtpSignup++;
                                doSignup();
                            }
                        }catch (Exception e){
                            e.printStackTrace();
                        }

                        // Handle the Intent
                    }
                }
            });

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Setting otpEnabled = SettingsController.findSettingFiled("OTP_ENABLED");
        if(otpEnabled != null
                && otpEnabled.getValue().equals("1")){
            loginOTP.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(getActivity(), OtpActivity.class);
                    startActivity(i);
                }
            });
            loginOTPContainer.setVisibility(View.VISIBLE);
            signupPhoneContainer.setVisibility(View.VISIBLE);
        }else{
            loginOTPContainer.setVisibility(View.GONE);
            signupPhoneContainer.setVisibility(View.GONE);
        }

        createAccountBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginLayout.setVisibility(View.GONE);
                signupLayout.setVisibility(View.VISIBLE);
                changePasswordLayout.setVisibility(View.GONE);
            }
        });

        haveAccountBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginLayout.setVisibility(View.VISIBLE);
                signupLayout.setVisibility(View.GONE);
                changePasswordLayout.setVisibility(View.GONE);
            }
        });

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doLogin();
            }
        });

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doSignup();
            }
        });

        takePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFromGallery();
            }
        });

        forget_password.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                forgetPassword();
            }
        });
    }

    public AuthenticationFragmentListeners listeners;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

      /*  V2FragmentProfileLoginBinding binding = DataBindingUtil.inflate(
                inflater, R.layout.v2_fragment_profile_login, container, false);
*/
        View view = inflater.inflate(R.layout.v2_fragment_profile_login, container, false);
        ButterKnife.bind(this, view);
        // Inflate the layout for this fragment
        initToolbar(view);

        setupViews();

        return view;
    }

    private void setupViews() {


        term_of_uses_login.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        term_of_uses_login.setText(
                Html.fromHtml(String.format(getString(R.string.term_of_uses_login_text), getString(R.string.app_name)))
        );

        term_of_uses_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                new AwesomeWebView.Builder(getActivity())
//                        .showMenuOpenWith(false)
//                        .statusBarColorRes(R.color.colorPrimary)
//                        .theme(R.style.FinestWebViewAppTheme)
//                        .titleColor(
//                                ResourcesCompat.getColor(getResources(), R.color.white, null)
//                        ).urlColor(
//                                ResourcesCompat.getColor(getResources(), R.color.white, null)
//                        ).show(Constances.TERMS_OF_USE_URL);
            }
        });

        try {
            boolean isSignup = getArguments().getBoolean("signUp",false);
            if(isSignup){
                signupLayout.setVisibility(View.VISIBLE);
                loginLayout.setVisibility(View.GONE);
            }else{
                signupLayout.setVisibility(View.GONE);
                loginLayout.setVisibility(View.VISIBLE);
            }
        }catch (Exception e){
            signupLayout.setVisibility(View.GONE);
            loginLayout.setVisibility(View.VISIBLE);
        }

        setupFirebaseAuth();

    }

    private void setupFirebaseAuth() {

        if (!AppConfig.ENABLE_SOCIAL_MEDIA_AUTH)
            firebaseAuthContainer.setVisibility(View.GONE);


        mFirebaseAuthPresenter = new FirebaseAuthPresenter(getActivity());
        mFirebaseAuthPresenter.setListeners(new FirebaseAuthPresenterListeners() {
            @Override
            public void onSuccess(FireAuthResult result) {
                checkUserAuthApi(result);
            }

            @Override
            public void onError(String error) {
                NSToast.show(error);
                NSLog.e("mFirebaseAuthPresenter", error);
            }
        });

    }

    private void checkUserAuthApi(FireAuthResult result){

        int guest_id = 0;
        if (GuestController.isStored())
            guest_id = GuestController.getGuest().getId();


        //show proress
        NSProgressDialog.newInstance(getActivity()).show(getString(R.string.loading));

        ApiRequest.newPostInstance(Constances.API.API_EXTERNAL_AUTH, new ApiRequestListeners() {
            @Override
            public void onSuccess(Parser parser) {

                if(NSProgressDialog.getInstance() != null)
                    NSProgressDialog.getInstance().dismiss();

                parse_user_auth(parser);

            }

            @Override
            public void onFail(Map<String, String> errors) {
                //show errors
                MessageDialog.showMessage(getActivity(),errors);
            }}, Map.of(
                        "name", String.valueOf(result.getName()),
                        "email", String.valueOf(result.getEmail()),
                        "guest_id", String.valueOf(guest_id),
                        "auth_type", String.valueOf(result.getSource()),
                        "auth_id", String.valueOf(result.getUniqueid()),
                        "avatar_url", String.valueOf(result.getAvatar()))
        );


    }

    private void parse_user_auth(Parser parser) {

        UserParser mUserParser = new UserParser(parser);

        if (mUserParser.getSuccess() == 0) {
            //show errors
            MessageDialog.showMessage(getActivity(),mUserParser.getErrors());
            return;
        }

        List<User> users = mUserParser.getUser();

        if (users.size() == 0) {
            //show errors
            MessageDialog.showMessage(getActivity(), Map.of("err", getString(R.string.auth_failed)));
            return;
        }

        NSLog.e("ssss",users.get(0).getToken());
        NSLog.e("ssss",users.get(0).getToken());
        //save session
        SessionsController.createSession(users.get(0), users.get(0).getToken());

        //show message successful message
        NSToast.show(getString(R.string.auth_sccuccful));

        //restart the app
        ActivityCompat.finishAffinity(getActivity());
        startActivity(new Intent(getActivity(), SplashActivity.class));

    }

    private void initToolbar(View view) {

        LinearLayout mToolbar = view.findViewById(R.id.toolbar);

        if (AppController.isRTL()) {
            Drawable arrowIcon = getResources().getDrawable(R.drawable.ic_arrow_forward_white_18dp);
            ((ImageView) mToolbar.findViewById(R.id.btnBack)).setImageDrawable(arrowIcon);
        }
        mToolbar.findViewById(R.id.btnBack).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backToHomePage();
            }
        });
    }


    private void backToHomePage() {

        //enable nav drawer when fragment is closed
        if (NavigationDrawerFragment.getInstance() != null)
            NavigationDrawerFragment.getInstance().setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);


        if (getFragmentManager().getBackStackEntryCount() > 0) {
            getFragmentManager().popBackStack();
        } else {
            getActivity().onBackPressed();
        }


        V2MainFragment mf = (V2MainFragment) getFragmentManager().findFragmentByTag(V2MainFragment.TAG);
        if (mf != null) {
            mf.setCurrentFragment(0);
        }
    }

    private void handleBackPressedEvent() {

        getView().setFocusableInTouchMode(true);
        getView().requestFocus();
        getView().setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    backToHomePage();
                    getView().setFocusableInTouchMode(false);
                    return true;
                }
                return false;
            }
        });


    }


    @Override
    public void onResume() {
        super.onResume();

        if (mFirebaseAuthPresenter != null)
            mFirebaseAuthPresenter.onFireAuthResume();

        handleBackPressedEvent();
        askOtpSignup = 0;

    }

    @Override
    public void onPause() {
        super.onPause();

    }

    private void doLogin() {

        loginBtn.setEnabled(false);

        FragmentManager manager = getChildFragmentManager();

        mPdialog = new ProgressDialog(this.getActivity());
        mPdialog.setMessage(getString(R.string.loading));
        mPdialog.show();

        final double lat = gps.getLatitude();
        final double lng = gps.getLongitude();


        int guest_id = 0;

        if (GuestController.isStored())
            guest_id = GuestController.getGuest().getId();


        final int finalGuest_id = guest_id;
        SimpleRequest request = new SimpleRequest(Request.Method.POST,
                Constances.API.API_USER_LOGIN, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                loginBtn.setEnabled(true);

                if (!getActivity().isFinishing() && mPdialog != null)
                    mPdialog.dismiss();

                try {

                    if (APP_DEBUG) {
                        NSLog.i("responseLogin", response);
                    }

                    JSONObject js = new JSONObject(response);
                    UserParser mUserParser = new UserParser(js);

                    int success = Integer.parseInt(mUserParser.getStringAttr(Tags.SUCCESS));

                    if (success == 1) {

                        List<User> list = mUserParser.getUser();


                        if (list.size() > 0) {

                            SessionsController.createSession(list.get(0), list.get(0).getToken());

                               /* //hide Keyboard
                                InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Activity.INPUT_METHOD_SERVICE);
                                imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);*/

                            //Go back to the Home Fragment
                            ActivityCompat.finishAffinity(getActivity());
                            startActivity(new Intent(getActivity(), SplashActivity.class));
                            //getActivity().finish();

                        }


                    } else {


                        Map<String, String> errors = mUserParser.getErrors();

                        MessageDialog.newDialog(getActivity()).onCancelClick(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                MessageDialog.getInstance().hide();
                            }
                        }).onOkClick(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                MessageDialog.getInstance().hide();
                            }
                        }).setContent(Translator.print(CommunFunctions.convertMessages(errors), "Message showError")).show();

                    }


                } catch (JSONException e) {
                    e.printStackTrace();

                    MessageDialog.newDialog(getActivity()).onCancelClick(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            MessageDialog.getInstance().hide();
                        }
                    }).onOkClick(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            MessageDialog.getInstance().hide();
                        }
                    }).setContent(Translator.print(getString(R.string.authentification_error_msg), "Message showError (Parser)")).show();


                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (APP_DEBUG) {
                    NSLog.e("ERROR", error.toString());
                }

                loginBtn.setEnabled(true);

                mPdialog.dismiss();

                Map<String, String> errors = new HashMap<String, String>();
                errors.put("NetworkException:", getString(R.string.check_network));
                mDialogError = CommunFunctions.showErrors(errors, getContext());
                mDialogError.setTitle(getString(R.string.network_error));

            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();

                params.put("token", Utils.getToken(getContext()));
                params.put("mac_adr", ServiceHandler.getMacAddr());
                params.put("password", paswordTxt.getText().toString());
                params.put("login", loginTxt.getText().toString());
                params.put("lat", String.valueOf(lat));
                params.put("lng", String.valueOf(lng));
                params.put("guest_id", String.valueOf(finalGuest_id));

                return params;
            }

        };

        request.setRetryPolicy(new DefaultRetryPolicy(SimpleRequest.TIME_OUT,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        queue.add(request);

    }

    int askOtpSignup = 0;

    private void doSignup() {

        Setting otpEnabled = SettingsController.findSettingFiled("OTP_ENABLED");
        if( otpEnabled!=null &&  Integer.parseInt(otpEnabled.getValue()) == 1){
            if(askOtpSignup==0){
                MessageDialog.showMessageRegisterConfirmUser(getActivity(), Map.of("err",getString(R.string.verify_phone_number_otp)), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        MessageDialog.getInstance().hide();
                        askOtpSignup++;

                        //start otp
                        Intent i = new Intent(getActivity(), OtpActivity.class);
                        i.putExtra("dialCode","+"+phoneDialCode.getSelectedCountryCode().toString().trim());
                        i.putExtra("countryCode",phoneDialCode.getSelectedCountryNameCode().toString().trim());
                        i.putExtra("phoneNumber",phoneSignupTxt.getText().toString().trim());
                        i.putExtra("checkPhoneValidityReq",true);
                        mStartForResult.launch(i);
                    }
                });

                return;
            }
        }

        //Before confirming the sign-up, please verify your phone number.

        loginBtn.setEnabled(false);

        FragmentManager manager = getChildFragmentManager();

        mPdialog = new ProgressDialog(this.getActivity());
        mPdialog.setMessage("Loading ...");
        mPdialog.show();

        final double lat = gps.getLatitude();
        final double lng = gps.getLongitude();


        int guest_id = 0;

        if (GuestController.isStored())
            guest_id = GuestController.getGuest().getId();


        final int finalGuest_id = guest_id;

        Map<String, String> params = new HashMap<String, String>();

        params.put("username", loginSignupTxt.getText().toString().trim());
        params.put("name", fullNameSignupTxt.getText().toString().trim());
        params.put("phone", "+"+phoneDialCode.getSelectedCountryCode().toString()+phoneSignupTxt.getText().toString().trim());
        params.put("email", emailSignupTxt.getText().toString().trim());
        params.put("token", Utils.getToken(getContext()));
        params.put("mac_adr", ServiceHandler.getMacAddr());
        params.put("password", passwordSignupTxt.getText().toString());
        params.put("lat", String.valueOf(lat));
        params.put("lng", String.valueOf(lng));
        params.put("auth_type", "mobile");
        params.put("guest_id", String.valueOf(finalGuest_id));

        if (APP_DEBUG) {
            NSLog.e("Authentication", " params :" + params.toString());
        }


        ApiRequest.newPostInstance(Constances.API.API_USER_SIGNUP, new ApiRequestListeners() {
            @Override
            public void onSuccess(Parser parser) {
                UserParser mUserParser = new UserParser(parser);

                if (mUserParser.getSuccess() == 1) {
                    List<User> list = mUserParser.getUser();
                    if (list.size() > 0) {
                        if (imageToUpload != null)
                            uploadImage(list.get(0).getId());

                        SessionsController.createSession(list.get(0), list.get(0).getToken());
                        NSToast.show(getString(R.string.registerSuccessful));
                        //Go back to the Home Fragment
                        startActivity(new Intent(getActivity(), MainActivity.class));
                        getActivity().finish();
                    }
                } else {
                    Map<String, String> errors = mUserParser.getErrors();
                    MessageDialog.newDialog(getActivity()).onCancelClick(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            MessageDialog.getInstance().hide();
                        }
                    }).onOkClick(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            MessageDialog.getInstance().hide();
                        }
                    }).setContent(Translator.print(CommunFunctions.convertMessages(errors), "Message showError")).show();
                }
            }

            @Override
            public void onFail(Map<String, String> errors) {

                MessageDialog.newDialog(getActivity()).onCancelClick(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        MessageDialog.getInstance().hide();
                    }
                }).onOkClick(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        MessageDialog.getInstance().hide();
                    }
                }).setContent(Translator.print(getString(R.string.authentification_error_msg), "Message showError (Parser)")).show();

            }
        }, params);


    }

    private void forgetPassword() {
//        new AwesomeWebView.Builder(getActivity())
//                .statusBarColorRes(R.color.colorPrimary)
//                .theme(R.style.FinestWebViewAppTheme)
//                .show(Constances.FORGET_PASSWORD);
    }


    private void getFromGallery() {

        if (checkPermissionForReadExtertalStorage()) {
            Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(i, GALLERY_REQUEST);
        } else {
            requestPermissionForReadExtertalStorage();
        }

    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK && requestCode == GALLERY_REQUEST) {

            try {

                Uri selectedImage = data.getData();
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), selectedImage);
                String[] filePathColumn = {MediaStore.Images.Media.DATA};
                Cursor cursor = getActivity().getContentResolver().query(selectedImage, filePathColumn, null, null, null);
                cursor.moveToFirst();
                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                String picturePath = cursor.getString(columnIndex);
                cursor.close();

                try {

                    String createNewFileDest = CommunFunctions.createImageFile(getActivity());

                    new ImageUtils.PrepareImagesData(
                            getActivity(),
                            picturePath,
                            bitmap,
                            createNewFileDest,
                            this).execute();

                    userimage.setImageBitmap(bitmap);


                } catch (IOException e) {

                    if (AppContext.DEBUG)
                        e.printStackTrace();

                }

            } catch (Exception e) {
                if (AppContext.DEBUG)
                    e.printStackTrace();
            }


        }
    }


    private void uploadImage(final int uid) {

        Toast.makeText(getContext(), getString(R.string.fileUploading), Toast.LENGTH_LONG).show();

        SimpleRequest request = new SimpleRequest(Request.Method.POST,
                Constances.API.API_USER_UPLOAD64, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                // pdialog.dismiss();
                try {

                    if (APP_DEBUG)
                        NSLog.i("SignUpUload", response);
                    JSONObject js = new JSONObject(response);

                    UserParser mUserParser = new UserParser(js);
                    int success = Integer.parseInt(mUserParser.getStringAttr(Tags.SUCCESS));
                    if (success == 1) {

                        final List<User> list = mUserParser.getUser();
                        if (list.size() > 0) {
                            SessionsController.updateSession(list.get(0));
                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (APP_DEBUG) {
                    NSLog.e("ERROR", error.toString());
                    Toast.makeText(getContext(), error.toString(), Toast.LENGTH_LONG).show();
                }
                //pdialog.dismiss();

            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();

                Bitmap bm = BitmapFactory.decodeFile(imageToUpload.getPath());
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bm.compress(Bitmap.CompressFormat.JPEG, 100, baos); //bm is the bitmap object
                byte[] b = baos.toByteArray();
                String encodedImage = Base64.encodeToString(b, Base64.DEFAULT);
                params.put("image", encodedImage);

                params.put("int_id", String.valueOf(uid));
                params.put("type", "user");

                //do else
                params.put("module_id", String.valueOf(uid));
                params.put("module", "user");


                return params;
            }

        };


        request.setRetryPolicy(new DefaultRetryPolicy(SimpleRequest.TIME_OUT,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        queue.add(request);

    }

    @Override
    public void onCompressed(String newPath, String oldPath) {
        if (APP_DEBUG)
            Toast.makeText(getContext(), "PATH:" + newPath, Toast.LENGTH_SHORT).show();

        File mFile = new File(newPath);

        Glide.with(this).load(mFile).centerCrop()
                .placeholder(R.drawable.profile_placeholder).into(userimage);

        imageToUpload = Uri.parse(newPath);
    }


    @Override
    public void onResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (mFirebaseAuthPresenter != null)
            mFirebaseAuthPresenter.onFireAuthResult(requestCode, resultCode, data);
    }
}

interface AuthenticationFragmentListeners {
    void onResult(int requestCode, int resultCode, @Nullable Intent data);
}
