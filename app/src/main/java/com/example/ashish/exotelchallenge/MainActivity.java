package com.example.ashish.exotelchallenge;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.matesnetwork.Cognalys.VerifyMobile;
import com.parse.FindCallback;
import com.parse.LogInCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.brickred.socialauth.Profile;
import org.brickred.socialauth.android.DialogListener;
import org.brickred.socialauth.android.SocialAuthAdapter;
import org.brickred.socialauth.android.SocialAuthError;
import org.brickred.socialauth.android.SocialAuthListener;

import java.util.List;


public class MainActivity extends ActionBarActivity {

    private SocialAuthAdapter adapter;
    private Button btFacebookLogin, btGoogleLogin, btNumberLogin;
    private EditText etCountryCode, etMobileNumber;
    private ProgressDialog progress;
    private String mobile;
    private UserData userData;
    private ParseUser parseUser;
    private boolean verifyNumber = false;
    private boolean numberLoginButtonClicked = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initComponent();
    }

    private void initComponent(){
        btNumberLogin = (Button) findViewById(R.id.btNumberLogin);
        btNumberLogin.setOnClickListener(number_login_listner);
        etCountryCode = (EditText) findViewById(R.id.etCountryCode);
        etCountryCode.setText(VerifyMobile.getCountryCode(getApplicationContext()));
        etCountryCode.setEnabled(false);
        etMobileNumber = (EditText) findViewById(R.id.etMobileNumber);

        adapter = new SocialAuthAdapter(new ResponseListener());
        btFacebookLogin = (Button) findViewById(R.id.btFacebookLogin);
        btFacebookLogin.setOnClickListener(facebook_login_listner);

        btGoogleLogin = (Button) findViewById(R.id.btGoogleLogin);
        btGoogleLogin.setOnClickListener(google_login_listner);

        userData = new UserData();
    }

    private Button.OnClickListener number_login_listner = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            numberLoginButtonClicked = true;
            if(etMobileNumber.getText().length() < 10){
                etMobileNumber.setError("Enter valid mobile number");
            }else {
                if(CheckNetworkConnection.isConnectionAvailable(getApplicationContext())) {
                    mobile = etCountryCode.getText().toString() + etMobileNumber.getText().toString();
                    ParseQuery<ParseUser> query = ParseUser.getQuery();
                    query.whereEqualTo("mobile_number", mobile);
                    query.findInBackground(user_found_callback);
                    manageProgressBar("Checking pre-existing Users");
                }else {
                    Toast.makeText(getApplicationContext(),"no internet connection", Toast.LENGTH_SHORT).show();
                }
/*
                Intent in = new Intent(MainActivity.this, VerifyMobile.class);
                in.putExtra("app_id", "9195ab713ac24a5098c1317");
                in.putExtra("access_token", "5cbf3792e8def00a9775a2673f11c35d3f39fb5d");
                in.putExtra("mobile", mobile);
                if (CheckNetworkConnection.isConnectionAvailable(getApplicationContext())) {
                    startActivityForResult(in, VerifyMobile.REQUEST_CODE);
                } else {
                    Toast.makeText(getApplicationContext(),"no internet connection", Toast.LENGTH_SHORT).show();
                }
*/
            }
        }
    };

    private FindCallback<ParseUser> user_found_callback = new FindCallback<ParseUser>() {
        @Override
        public void done(List<ParseUser> list, ParseException e) {
            progress.dismiss();
            if(list == null){
                Toast.makeText(getApplicationContext(), "Something went wrong", Toast.LENGTH_LONG).show();
            } else if (list.size() == 1){
                parseUser = list.get(0);
                if (numberLoginButtonClicked){
                    verifyNumber = true;
                }
                if(verifyNumber){
                    initiateNumberVerification();
                } else {
                    loginUser();
                    manageProgressBar("Logging in ...");
                }
            } else {
                displayDialogToCreateNewUser();
            }
        }
    };

    private void displayDialogToCreateNewUser(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Sign Up");
        builder.setMessage("Create account using these credentials ?");
        builder.setCancelable(false);

        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                if(numberLoginButtonClicked) {
                    initiateOTPProcedure();
                }else{
                    Intent intent = new Intent(getApplicationContext(), UserDetailSignUpActivity.class);
                    intent.putExtra("userData", userData);
                    startActivity(intent);
                }
            }
        });

        builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Do nothing
                dialog.dismiss();
            }
        });

        AlertDialog alert = builder.create();
        alert.show();
    }

    private void manageProgressBar(String text){
        if(progress != null){
            progress.dismiss();
        }
        progress = new ProgressDialog(this);
        progress.setMessage(text);
        progress.setCancelable(false);
        progress.show();
    }

    private void initiateOTPProcedure(){
        Intent in = new Intent(MainActivity.this, VerifyMobile.class);
        in.putExtra("app_id", "9195ab713ac24a5098c1317");
        in.putExtra("access_token", "5cbf3792e8def00a9775a2673f11c35d3f39fb5d");
        in.putExtra("mobile", mobile);
        if (CheckNetworkConnection.isConnectionAvailable(getApplicationContext())) {
            startActivityForResult(in, VerifyMobile.REQUEST_CODE);
        } else {
            Toast.makeText(getApplicationContext(),"no internet connection", Toast.LENGTH_SHORT).show();
        }

/*        Intent intent  = new Intent(getApplicationContext(), OtpVerificationActivity.class);
        intent.putExtra("mobile",mobile);
        startActivity(intent);
        finish();*/
    }

    @Override
    protected void onActivityResult(int arg0, int arg1, Intent arg2) {
        // TODO Auto-generated method stub
        super.onActivityResult(arg0, arg1, arg2);

        if (arg0 == VerifyMobile.REQUEST_CODE) {
            String message = arg2.getStringExtra("message");
            int result = arg2.getIntExtra("result", 0);
            if(message.equals("VERIFICATION SUCCESS")){
                if(verifyNumber){
                    loginUser();
                    verifyNumber = false;
                } else {
                    userData.setMobileNumber(mobile);
                    Intent intent = new Intent(getApplicationContext(), UserDetailSignUpActivity.class);
                    intent.putExtra("userData", userData);
                    startActivity(intent);
                }
            }
            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
            Toast.makeText(getApplicationContext(), "" + result,Toast.LENGTH_SHORT).show();
        }
    }

    private Button.OnClickListener facebook_login_listner = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            manageProgressBar("Please wait ...");
            adapter.authorize(MainActivity.this, SocialAuthAdapter.Provider.FACEBOOK);
        }
    };

    private Button.OnClickListener google_login_listner = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            manageProgressBar("Please wait ...");
            adapter.addCallBack(SocialAuthAdapter.Provider.GOOGLEPLUS, "http://localhost/authorize/"); //https://www.example.com/oauth2callback
            adapter.authorize(MainActivity.this, SocialAuthAdapter.Provider.GOOGLEPLUS);
        }
    };

    private final class ResponseListener implements DialogListener{
        public void onComplete(Bundle values) {
            adapter.getUserProfileAsync(new ProfileDataListener());
        }

        @Override
        public void onError(SocialAuthError socialAuthError) {

        }

        @Override
        public void onCancel() {

        }

        @Override
        public void onBack() {

        }
    }

    private final class ProfileDataListener implements SocialAuthListener{

        @Override
        public void onExecute(String s, Object o) {
            Log.d("Custom-UI", "Receiving Data");
            Profile profileMap = (Profile) o;
            userData.setFirstName(profileMap.getFirstName());
            userData.setLastName(profileMap.getLastName());
            userData.setEmail(profileMap.getEmail());
            checkUserAlreadyExist();
        }

        @Override
        public void onError(SocialAuthError socialAuthError) {

        }

    }

    private void loginUser(){
        ParseUser.logInInBackground(parseUser.getEmail().toString(), "", login_callback);
        manageProgressBar("Logging in ...");
    }

    private LogInCallback login_callback = new LogInCallback() {
        @Override
        public void done(ParseUser parseUser, ParseException e) {
            progress.dismiss();
            if (parseUser != null) {
                // If user exist and authenticated, send user to Welcome.class
                Intent intent = new Intent(getApplicationContext(), WelcomeActivity.class);
                startActivity(intent);
                Toast.makeText(getApplicationContext(),"Successfully Logged in", Toast.LENGTH_LONG).show();
                finish();
            } else {
                Toast.makeText( getApplicationContext(), "No such user exist, please signup", Toast.LENGTH_LONG).show();
            }
        }
    };

    private void checkUserAlreadyExist(){
        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.whereEqualTo("email", userData.getEmail());
        query.findInBackground(user_found_callback);
        manageProgressBar("Checking pre-existing Users");
    }

    private boolean initiateNumberVerification(){
        boolean status = true;
        initiateOTPProcedure();
        return status;
    }

}
