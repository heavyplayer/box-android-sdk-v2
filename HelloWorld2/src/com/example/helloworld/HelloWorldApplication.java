package com.example.helloworld;

import android.app.Application;

import com.box.boxandroidlibv2.BoxAndroidClient;

public class HelloWorldApplication extends Application {

    public static final String CLIENT_ID = "";
    public static final String CLIENT_SECRET = "";
    public static final String REDIRECT_URL = "";

    private BoxAndroidClient mClient;

    public void setClient(BoxAndroidClient client) {
        this.mClient = client;
    }

    /**
     * Gets the BoxAndroidClient for this app.
     * 
     * @return a singleton instance of BoxAndroidClient.
     */
    public BoxAndroidClient getClient() {
        return mClient;
    }
}
