package com.example.ashish.exotelchallenge;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.squareup.okhttp.Cache;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;


public class OtpVerificationActivity extends ActionBarActivity {

    private String MOBILE, KEYMATCH, OTP;
    private EditText etOtpStart, etOtpEnd;
    private TextView tvTimer;
    private Button btVerifyOtp;
    private ProgressBar progressBar;
    private ProgressDialog progress;
    private CountDownTimer timer;
    private String countDownTimerText;
    private long countDownTill = 180000;
    private String BASEURL = "https://www.cognalys.com/api/v1/otp/?";
    private String APPID = "762d2882367c47a5b15808c";
    private String ACCESSTOKEN = "6e4687bd681c408297ff84599454bbfe0f98db08";
    private OtpData otpData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp_verification);

        Intent intent = getIntent();
        MOBILE = intent.getStringExtra("mobile");

        initComponent();
        manageProgressBar("Requesting OTP ...");
        new RequestOTPAsyncTask().execute();

    }

    private void initComponent(){
        etOtpStart = (EditText) findViewById(R.id.etOtpStart);
        etOtpStart.setEnabled(false);
        etOtpEnd = (EditText) findViewById(R.id.etOtpEnd);
        btVerifyOtp = (Button) findViewById(R.id.btVerifyOtp);
        btVerifyOtp.setOnClickListener(verify_otp_click_listner);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        tvTimer = (TextView) findViewById(R.id.tvTimer);
    }

    public void countDownManager(long diff){
        if(timer != null){
            timer.cancel();
        }
        tvTimer.setTextColor(Color.parseColor("#ff159186"));
        timer = new CountDownTimer(diff, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long ms = millisUntilFinished;
                long sec = TimeUnit.MILLISECONDS.toSeconds(ms) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(ms));
                long min = TimeUnit.MILLISECONDS.toMinutes(ms) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(ms));
                String text = String.format("%02d : %02d", min, sec);
                countDownTimerText = text;
                tvTimer.setText(text);
            }

            @Override
            public void onFinish() {
                try {
                    timer.cancel();
                    Toast.makeText(getApplicationContext(),"Time Out. Try Again", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(intent);
                    finish();
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }
            }
        };

        timer.start();
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

    private class RequestOTPAsyncTask extends AsyncTask<String, Integer, Double> {
        String responseText = null;
        OkHttpClient client;

        @Override
        protected Double doInBackground(String... params) {
            postData();
            return null;
        }

        @Override
        protected void onPostExecute(Double result) {
            if (responseText != null) {
                JsonParser parser = new JsonParser();
                JsonObject object = (JsonObject) parser.parse(responseText);
                Gson gson = new Gson();
                otpData = gson.fromJson(object, new TypeToken<OtpData>(){}.getType());
                if(otpData.getStatus().equals("success")){
                    etOtpStart.setText(otpData.getOtp_start());
                }else{
                    Toast.makeText(getApplicationContext(), "Error, Try Again !",Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(intent);
                    finish();
                }
                progress.dismiss();
            }
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
        }

        public void postData() {
            String sURL = BASEURL + "app_id=" + APPID + "&access_token=" + ACCESSTOKEN + "&mobile=" + MOBILE;

            client = new OkHttpClient();
            try {
                responseText = run(sURL);
                countDownManager(countDownTill);
                progressBar.setVisibility(View.VISIBLE);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public String run(String sURL) throws IOException {
            Request request = new Request.Builder()
                    .url(sURL)
                    .build();

            Response response = client.newCall(request).execute();
            return response.body().string();
        }
    }

    private Button.OnClickListener verify_otp_click_listner = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if(etOtpEnd.getText().length() < 5){
                etOtpEnd.setError("Enter last 5 digits");
            }else{
                manageProgressBar("Verifying OTP");
                OTP = otpData.getOtp_start()+etOtpEnd.getText();
                KEYMATCH = otpData.getKeymatch();
                new VerifyOTPAsyncTask().execute();
            }
        }
    };

    private class VerifyOTPAsyncTask extends AsyncTask<String, Integer, Double> {
        String responseText = null;
        OkHttpClient client;

        @Override
        protected Double doInBackground(String... params) {
            postData();
            return null;
        }

        protected void onPostExecute(Double result) {
            if (responseText != null) {
                JsonParser parser = new JsonParser();
                JsonObject object = (JsonObject) parser.parse(responseText);
                Gson gson = new Gson();
                OtpData otpData = gson.fromJson(object, new TypeToken<OtpData>(){}.getType());
                if(otpData.getStatus().equals("success")){
                    Intent intent = new Intent(getApplicationContext(), UserDetailSignUpActivity.class);
                    UserData userData = new UserData();
                    userData.setMobileNumber(OTP);
                    intent.putExtra("userData", userData);
                    startActivity(intent);
                }else{
                    Toast.makeText(getApplicationContext(), "Wrong OTP, Try Again !",Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(intent);
                    finish();
                }
                progress.dismiss();
            }
        }

        protected void onProgressUpdate(Integer... progress) {
        }

        public void postData() {
            String sURL = BASEURL + "app_id=" + APPID + "&access_token=" + ACCESSTOKEN + "&keymatch=" + KEYMATCH + "&otp=" + OTP;

            client = new OkHttpClient();
            try {
                responseText = run(sURL);
                timer.cancel();
                progressBar.setVisibility(View.INVISIBLE);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public String run(String sURL) throws IOException {
            Request request = new Request.Builder()
                    .url(sURL)
                    .build();

            Response response = client.newCall(request).execute();
            return response.body().string();
        }
    }

}
