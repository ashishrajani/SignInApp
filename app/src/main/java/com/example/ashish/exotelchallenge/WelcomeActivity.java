package com.example.ashish.exotelchallenge;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.parse.ParseUser;


public class WelcomeActivity extends ActionBarActivity {

    private TextView tvFirstName, tvLastName, tvEmail, tvPhoneNumber;
    private Button btLogout;
    private ParseUser parseUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        parseUser = ParseUser.getCurrentUser();
        initComponent();
        populateData();
    }

    private void initComponent(){
        tvFirstName = (TextView) findViewById(R.id.tvFirstName);
        tvLastName = (TextView) findViewById(R.id.tvLastName);
        tvEmail = (TextView) findViewById(R.id.tvEmail);
        tvPhoneNumber = (TextView) findViewById(R.id.tvPhoneNumber);
        btLogout = (Button) findViewById(R.id.btLogout);
        btLogout.setOnClickListener(logout_click_listner);
    }

    private void populateData(){
        tvFirstName.setText(parseUser.get("first_name").toString());
        tvLastName.setText(parseUser.get("last_name").toString());
        tvEmail.setText(parseUser.getEmail());
        tvPhoneNumber.setText(parseUser.get("mobile_number").toString());
    }

    private Button.OnClickListener logout_click_listner = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            ParseUser.logOut();
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        }
    };

}
