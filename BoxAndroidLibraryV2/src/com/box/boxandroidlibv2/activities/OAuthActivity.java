package com.box.boxandroidlibv2.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.http.SslError;
import android.os.Bundle;
import android.view.View;
import android.webkit.SslErrorHandler;
import com.box.boxandroidlibv2.BoxAndroidClient;
import com.box.boxandroidlibv2.R;
import com.box.boxandroidlibv2.dao.BoxAndroidOAuthData;
import com.box.boxandroidlibv2.viewlisteners.OAuthWebViewListener;
import com.box.boxandroidlibv2.views.OAuthWebView;
import com.box.boxjavalibv2.events.OAuthEvent;
import com.box.boxjavalibv2.interfaces.IAuthEvent;
import com.box.boxjavalibv2.interfaces.IAuthFlowMessage;
import org.apache.commons.lang.StringUtils;

/**
 * Activity for OAuth. Use this activity by using the intent from createOAuthActivityIntent method. On completion, this activity will put the parcelable
 * BoxAndroidClient into activity result. In case of failing, the activity result will be {@link Activity#RESULT_CANCELED} together will a error message in
 * intent extra.
 */
public class OAuthActivity extends Activity {

    public static final String ERROR_MESSAGE = "exception";
    public static final String BOX_CLIENT_OAUTH = "boxAndroidClient_oauth";
    public static final String BOX_DEVICE_ID = "boxdeviceid";
    public static final String BOX_DEVICE_NAME = "boxdevicename";

    protected static final String CLIENT_ID = "clientId";
    protected static final String CLIENT_SECRET = "clientSecret";
    protected static final String REDIRECT_URL = "redirecturl";
    protected static final String ALLOW_LOAD_REDIRECT_PAGE = "allowloadredirectpage";

    private OAuthWebView oauthView;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getContentView());

        Intent intent = getIntent();
        String clientId = intent.getStringExtra(CLIENT_ID);
        String clientSecret = intent.getStringExtra(CLIENT_SECRET);
        String deviceId = intent.getStringExtra(BOX_DEVICE_ID);
        String deviceName = intent.getStringExtra(BOX_DEVICE_NAME);
        String redirectUrl = intent.getStringExtra(REDIRECT_URL);
        boolean allowShowRedirect = getIntent().getBooleanExtra(ALLOW_LOAD_REDIRECT_PAGE, true);
        startOAuth(clientId, clientSecret, redirectUrl, allowShowRedirect, deviceId, deviceName);
    }

    protected int getContentView() {
        return R.layout.boxandroidlibv2_activity_oauth;
    }

    /**
     * Start oauth flow.
     * 
     * @param clientId
     * @param clientSecret
     * @param redirectUrl
     * @param allowShowRedirect
     * @param deviceName
     * @param deviceId
     */
    protected void startOAuth(final String clientId, final String clientSecret, String redirectUrl, boolean allowShowRedirect, String deviceId,
        String deviceName) {
        BoxAndroidClient boxClient = new BoxAndroidClient(clientId, clientSecret, null, null);
        oauthView = (OAuthWebView) findViewById(R.id.oauthview);
        oauthView.setAllowShowingRedirectPage(allowShowRedirect);
        oauthView.initializeAuthFlow(this, clientId, clientSecret, redirectUrl);
        if (StringUtils.isNotEmpty(deviceId) && StringUtils.isNotEmpty(deviceName)) {
            oauthView.setDevice(deviceId, deviceName);
        }

        boxClient.authenticate(oauthView, false, getOAuthFlowListener());
    }

    /**
     * Create a listener to listen to OAuth flow events.
     * 
     * @param boxClient
     * 
     * @return OAuthWebViewListener
     */
    private OAuthWebViewListener getOAuthFlowListener() {
        return new OAuthWebViewListener() {

            @Override
            public void onAuthFlowException(final Exception e) {
                Intent intent = new Intent();
                intent.putExtra(ERROR_MESSAGE, e.getMessage());
                OAuthActivity.this.setResult(RESULT_CANCELED, intent);
                finish();
            }

            @Override
            public void onAuthFlowEvent(final IAuthEvent event, final IAuthFlowMessage message) {
                if (event == OAuthEvent.OAUTH_CREATED) {
                    Intent intent = new Intent();
                    intent.putExtra(BOX_CLIENT_OAUTH, (BoxAndroidOAuthData) message.getData());
                    OAuthActivity.this.setResult(RESULT_OK, intent);
                    finish();
                }
	            else if (event == OAuthEvent.PAGE_FINISHED) {
	                final View progress = findViewById(android.R.id.progress);
	                if(progress != null)
		                progress.setVisibility(View.GONE);
                }
            }

            @Override
            public void onSslError(final SslErrorHandler handler, final SslError error) {
                handler.proceed();
            }

            @Override
            public void onError(final int errorCode, final String description, final String failingUrl) {
                Intent intent = new Intent();
                intent.putExtra(ERROR_MESSAGE, description);
                OAuthActivity.this.setResult(RESULT_CANCELED, intent);
                finish();
            }

            @Override
            public void onAuthFlowMessage(IAuthFlowMessage message) {
            }

        };
    }

    /**
     * Create intent to launch OAuthActivity,use this method only if you already set redirect url in <a href="https://cloud.app.box.com/developers/services">box
     * dev console</a> and you want to show the redirect page at the end of OAuth flow.
     * 
     * @param context
     *            context
     * @param clientId
     *            your box client id
     * @param clientSecret
     *            your box client secret
     * @return
     */
    public static Intent createOAuthActivityIntent(final Context context, final String clientId, final String clientSecret) {
        Intent intent = new Intent(context, OAuthActivity.class);
        intent.putExtra(CLIENT_ID, clientId);
        intent.putExtra(CLIENT_SECRET, clientSecret);
        return intent;
    }

    /**
     * Create intent to launch OAuthActivity, use this method only if you already set redirect url in <a
     * href="https://cloud.app.box.com/developers/services">box dev console</a>.
     * 
     * @param context
     *            context
     * @param clientId
     *            your box client id
     * @param clientSecret
     *            your box client secret
     * @param allowShowRedirectPage
     *            Whether you want to load/show redirected page after OAuth flow is done.
     * @return
     */
    public static Intent createOAuthActivityIntent(final Context context, final String clientId, final String clientSecret, final boolean allowShowRedirectPage) {
        return createOAuthActivityIntent(context, clientId, clientSecret, allowShowRedirectPage, null);
    }

    /**
     * Create intent to launch OAuthActivity. Notes about redirect url parameter: If you already set redirect url in <a
     * href="https://cloud.app.box.com/developers/services">box dev console</a>, you should pass in the same redirect url or use null for redirect url. If you
     * didn't set it in box dev console, you should pass in a url. In case you don't have a redirect server you can simply use "http://localhost".
     * 
     * @param context
     *            context
     * @param clientId
     *            your box client id
     * @param clientSecret
     *            your box client secret
     * @param allowShowRedirectPage
     *            Whether you want to load/show redirected page after OAuth flow is done.
     * @param redirectUrl
     *            redirect url, if you already set redirect url in <a href="https://cloud.app.box.com/developers/services">box dev console</a>, leave this null
     *            or use the same url, otherwise this field is required. You can use "http://localhost" if you don't have a redirect server.
     * @return
     */
    public static Intent createOAuthActivityIntent(final Context context, final String clientId, final String clientSecret,
        final boolean allowShowRedirectPage, String redirectUrl) {
        Intent intent = new Intent(context, OAuthActivity.class);
        intent.putExtra(CLIENT_ID, clientId);
        intent.putExtra(CLIENT_SECRET, clientSecret);
        intent.putExtra(ALLOW_LOAD_REDIRECT_PAGE, allowShowRedirectPage);
        if (StringUtils.isNotEmpty(redirectUrl)) {
            intent.putExtra(REDIRECT_URL, redirectUrl);
        }
        return intent;
    }
}
