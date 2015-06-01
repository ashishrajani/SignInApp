package com.example.ashish.exotelchallenge;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.matesnetwork.Cognalys.VerifyMobile;
import com.parse.FindCallback;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

import java.util.List;


public class UserDetailSignUpActivity extends ActionBarActivity {

    private EditText etFirstName, etLastName, etEmail, etPhoneNumber;
    private Button btSaveInfo;
    private UserData userData;
    private ProgressDialog progress;
    private boolean mobileVerifiedStatus = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_detail_sign_up);

        Intent intent = getIntent();
        userData = (UserData) intent.getSerializableExtra("userData");
        if(userData.getMobileNumber() == null){
            mobileVerifiedStatus = false;
        }
        initComponent();
        populateData();
    }

    private void initComponent(){
        etFirstName = (EditText) findViewById(R.id.etFirstName);
        etLastName = (EditText) findViewById(R.id.etLastName);
        etEmail = (EditText) findViewById(R.id.etEmail);
        etPhoneNumber = (EditText) findViewById(R.id.etPhoneNumber);
        btSaveInfo = (Button) findViewById(R.id.btSaveInfo);
        btSaveInfo.setOnClickListener(save_info_click_listner);
    }

    private void populateData(){
        if(userData.getFirstName() != null){
            etFirstName.setText(userData.getFirstName());
        }
        if(userData.getLastName() != null){
            etLastName.setText(userData.getLastName());
        }
        if(userData.getEmail() != null){
            etEmail.setText(userData.getEmail());
        }
        if(userData.getMobileNumber() != null){
            etPhoneNumber.setText(userData.getMobileNumber());
        }
    }

    private boolean validateData(){
        boolean status = true;
        if(etFirstName.getText().length() == 0){
            etFirstName.setError("Enter First Name");
            status = status && false;
        }
        if(etLastName.getText().length() == 0){
            etLastName.setError("Enter Last Name");
            status = status && false;
        }
        if(etEmail.getText().length() == 0 ){
            etFirstName.setError("Enter First Name");
            status = status && false;
        }
        if(etPhoneNumber.getText().length() == 0 ){
            etPhoneNumber.setError("Enter number +9132146987");
            status = status && false;
        }
        return status;
    }

    private Button.OnClickListener save_info_click_listner = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            boolean status = validateData();
            if(status){
                if(mobileVerifiedStatus){
                    saveInfo();
                }else{
                    verifyMobile();
                }
            }
        }
    };

    private void saveInfo(){
        ParseUser parseUser = new ParseUser();
        parseUser.setEmail(etEmail.getText().toString());
        parseUser.setUsername(etEmail.getText().toString());
        parseUser.setPassword("");
        parseUser.put("first_name", etFirstName.getText().toString());
        parseUser.put("last_name", etLastName.getText().toString());
        parseUser.put("mobile_number", etPhoneNumber.getText().toString());
        manageProgressBar("Please wait. Saving Info ..");
        parseUser.signUpInBackground(signup_callback);
    }

    private void verifyMobile(){
        checkNumberAlreadyRegistered();
    }

    private boolean checkNumberAlreadyRegistered(){
        boolean status = false;
        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.whereEqualTo("mobile_number", etPhoneNumber.getText().toString());
        query.findInBackground(number_found_callback);
        manageProgressBar("Checking pre-existing Users");
        return status;
    }

    private FindCallback<ParseUser> number_found_callback = new FindCallback<ParseUser>() {
        @Override
        public void done(List<ParseUser> list, ParseException e) {
            progress.dismiss();
            if(list == null){
                Toast.makeText(getApplicationContext(), "Something went wrong", Toast.LENGTH_LONG).show();
            } else if (list.size() == 1){
                Toast.makeText(getApplicationContext(), "Number already taken", Toast.LENGTH_LONG).show();
            } else {
                Intent in = new Intent(UserDetailSignUpActivity.this, VerifyMobile.class);
                in.putExtra("app_id", "9195ab713ac24a5098c1317");
                in.putExtra("access_token", "5cbf3792e8def00a9775a2673f11c35d3f39fb5d");
                in.putExtra("mobile", etPhoneNumber.getText().toString());
                if (CheckNetworkConnection.isConnectionAvailable(getApplicationContext())) {
                    startActivityForResult(in, VerifyMobile.REQUEST_CODE);
                } else {
                    Toast.makeText(getApplicationContext(),"no internet connection", Toast.LENGTH_SHORT).show();
                }

            }
        }
    };


    @Override
    protected void onActivityResult(int arg0, int arg1, Intent arg2) {
        // TODO Auto-generated method stub
        super.onActivityResult(arg0, arg1, arg2);

        if (arg0 == VerifyMobile.REQUEST_CODE) {
            String message = arg2.getStringExtra("message");
            int result = arg2.getIntExtra("result", 0);
            if(message.equals("VERIFICATION SUCCESS")){
                saveInfo();
            }
            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
            Toast.makeText(getApplicationContext(), "" + result,Toast.LENGTH_SHORT).show();
        }
    }

    private SignUpCallback signup_callback = new SignUpCallback() {
        @Override
        public void done(ParseException e) {
            progress.dismiss();
            if(e == null){
                Toast.makeText(getApplicationContext(), "Successfully Signed up", Toast.LENGTH_LONG).show();
                executeLoginProcedure();
            }else{
                Toast.makeText(getApplicationContext(),e.toString().split(":")[1], Toast.LENGTH_LONG).show();
            }
        }
    };

    private void manageProgressBar(String text){
        if(progress != null){
            progress.dismiss();
        }
        progress = new ProgressDialog(this);
        progress.setMessage(text);
        progress.setCancelable(false);
        progress.show();
    }

    private void executeLoginProcedure(){
        ParseUser.logInInBackground(etEmail.getText().toString(), "", login_callback);
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

}
