package com.example.ashish.exotelchallenge;

import android.app.Application;

import com.parse.Parse;
import com.parse.ParseACL;
import com.parse.ParseUser;

/**
 * Created by ashish on 31-05-2015.
 */
public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Parse.initialize(this, "Lp6AstZXRSRAalBeBRFLGnu1IWwRhppNCSxlpBBy", "25aPtVn3JjGmTmHpivG683JF8f4rUyu2GUxJtMo5");
        ParseUser.enableAutomaticUser();
        ParseACL defaultACL = new ParseACL();
        defaultACL.setPublicReadAccess(true);
        ParseACL.setDefaultACL(defaultACL, true);
    }
}
