package io.github.takusan23.twitteroauth;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.net.Uri;
import android.os.AsyncTask;

import androidx.appcompat.app.AppCompatActivity;
import androidx.browser.customtabs.CustomTabsIntent;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import twitter4j.conf.ConfigurationBuilder;

public class MainActivity extends AppCompatActivity {

    private EditText consumer_key_EditText;
    private EditText consumer_secret_EditText;
    private EditText pin_EditText;
    private Button login_Button;
    private Button access_token_Button;
    private Button access_token_copy_Button;
    private Button access_token_secret_copy_Button;
    private Switch logcat_Switch;
    private TextView response_TextView;
    private TwitterFactory twitterFactory;
    private RequestToken request_token;
    private Twitter twitter;

    public static String consumerKey = "";
    public static String consumerSecret = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        consumer_key_EditText = findViewById(R.id.consumer_key_EditText);
        consumer_secret_EditText = findViewById(R.id.consumer_secret_EditText);
        login_Button = findViewById(R.id.login_button);
        pin_EditText = findViewById(R.id.pin_EditText);
        access_token_copy_Button = findViewById(R.id.access_token_copy);
        access_token_secret_copy_Button = findViewById(R.id.access_token_secret_copy);
        response_TextView = findViewById(R.id.response_TextView);
        access_token_Button = findViewById(R.id.access_token_button);
        logcat_Switch = findViewById(R.id.logcat_switch);

        //飛ばす
        getLoginScreen();
        //認証コードからアクセストークン取得
        getAccessToken();
    }

    /*ログイン画面*/
    private void getLoginScreen() {
        login_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                consumerKey = consumer_key_EditText.getText().toString();
                consumerSecret = consumer_secret_EditText.getText().toString();

                ConfigurationBuilder cb = new ConfigurationBuilder();
                cb.setDebugEnabled(true)
                        .setOAuthConsumerKey(consumerKey)
                        .setOAuthConsumerSecret(consumerSecret)
                        .setOAuthAccessToken(null)
                        .setOAuthAccessTokenSecret(null);

                twitterFactory = new TwitterFactory(cb.build());
                twitter = twitterFactory.getInstance();

                new AsyncTask<Void, Void, Void>() {
                    @Override
                    protected Void doInBackground(Void... aVoid) {
                        try {
                            request_token = twitter.getOAuthRequestToken();
                            String url = request_token.getAuthenticationURL();
                            //ChromeCustomTab起動
                            CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
                            builder.setShowTitle(true);
                            CustomTabsIntent customTabsIntent = builder.build();
                            customTabsIntent.launchUrl(MainActivity.this, Uri.parse(url));
                        } catch (TwitterException e) {
                            e.printStackTrace();
                        }
                        return null;
                    }
                }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

            }
        });
    }

    /*アクセストークン取得*/
    private void getAccessToken() {
        access_token_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AsyncTask<Void, Void, Void>() {
                    @Override
                    protected Void doInBackground(Void... aVoid) {
                        AccessToken accessToken = null;
                        try {
                            final String pin = pin_EditText.getText().toString();
                            if (pin.length() > 0) {
                                accessToken = twitter.getOAuthAccessToken(pin);
                            } else {
                                accessToken = twitter.getOAuthAccessToken();
                            }
                            //できた！
                            final AccessToken finalAccessToken = accessToken;
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    final String token = finalAccessToken.getToken();
                                    final String secret = finalAccessToken.getTokenSecret();
                                    response_TextView.setText(getString(R.string.token) + " : " + token);
                                    response_TextView.append(getString(R.string.token_secret) + " : " + secret);
                                    //クリップボード
                                    final ClipboardManager clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                                    access_token_copy_Button.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            clipboardManager.setPrimaryClip(ClipData.newPlainText("", token));
                                            Toast.makeText(MainActivity.this, "コピー : " + token, Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                    access_token_secret_copy_Button.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            clipboardManager.setPrimaryClip(ClipData.newPlainText("", secret));
                                            Toast.makeText(MainActivity.this, "コピー : " + secret, Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                    //Logcat出力
                                    if (logcat_Switch.isChecked()) {
                                        System.out.println("コンシューマーキー : " + consumerKey);
                                        System.out.println("コンシューマーシークレット : " + consumerSecret);
                                        System.out.println("アクセストークン : " + token);
                                        System.out.println("アクセストークンシークレット : " + secret);
                                    }
                                }
                            });
                        } catch (TwitterException e) {
                            e.printStackTrace();
                        }

                        return null;
                    }
                }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        });
    }

}
