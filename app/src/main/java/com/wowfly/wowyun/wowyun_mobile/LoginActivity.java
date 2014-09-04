package com.wowfly.wowyun.wowyun_mobile;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.wowfly.android.core.WowYunApp;
import com.wowfly.android.core.XMPPService;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManagerListener;
import org.w3c.dom.Text;

import java.security.MessageDigest;
import android.os.Handler;
import android.widget.Toast;

import java.util.logging.LogRecord;

/**
 * Created by user on 8/23/14.
 */
public class LoginActivity extends Activity implements ChatManagerListener{
    private static final String TAG = "LoginActivity";
    private EditText username = null;
    private EditText password = null;
    private WowYunApp mAPP;
    private XMPPService mXMPP;
    private TextView mStatusText;
    private Handler mHandler;
    private ProgressBar mProgressBar;

    protected void onCreate(Bundle saved) {
        ImageView imageView;

        super.onCreate(saved);
        setContentView(R.layout.activity_login);

        mAPP = (WowYunApp) getApplication();

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                mXMPP = mAPP.getXMPP();
                if(mXMPP != null) {
                    mXMPP.doInit();
                }
            }
        };
        new Thread(runnable).start();

        Intent i = getIntent();
        String defaultname = i.getStringExtra("username");


        mHandler = new Handler() {

            public void handleMessage(Message msg) {
                //ImageView statusIcon = (ImageView)findViewById(R.id.login_status_icon);
                switch (msg.what) {
                    case WowYunApp.LOGIN_SUCCESS:
                        mStatusText.setText(R.string.action_login_success);
                        //mProgressBar.setEnabled(false);
                        //mProgressBar.setProgress(100);
                        //statusIcon.setVisibility(View.VISIBLE);
                        //statusIcon.setImageResource(android.R.drawable.ic_dialog_alert);
                        //mProgressBar.setProgress(100);
                        //mProgressBar.setVisibility(View.GONE);
                        break;
                    case WowYunApp.LOGIN_FAILURE:
                        //mStatusText.setText(R.string.action_login_failure);
                        username.setEnabled(true);
                        password.setEnabled(true);
                        View layout = findViewById(R.id.login_status_layout);
                        //layout.setVisibility(View.GONE);
                        //layout.setEnabled(false);
                        Toast.makeText(getApplicationContext(), R.string.action_login_failure, Toast.LENGTH_SHORT).show();
                        layout.setVisibility(View.GONE);
                        //mProgressBar.setProgress(0);
                        //statusIcon.setVisibility(View.VISIBLE);
                        //statusIcon.setImageResource(android.R.drawable.ic_dialog_alert);
                        //mProgressBar.setProgress(0);

                        //mProgressBar.setIndeterminate(false);
                        //mProgressBar.setVisibility(View.GONE);
                        break;
                }
                super.handleMessage(msg);
            }
        };

        Button btn = (Button)findViewById(R.id.login_button);
        username = (EditText) findViewById(R.id.login_edit_username);
        password = (EditText) findViewById(R.id.login_edit_password);
        TextView regBtn = (TextView) findViewById(R.id.regjid);
        mStatusText = (TextView) findViewById(R.id.login_status_text);
        mProgressBar = (ProgressBar) findViewById(R.id.login_progressbar);
        if(defaultname != null) {
            username.setText(defaultname);
            password.setFocusable(true);
        }

        regBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "Register new jid");
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
                LoginActivity.this.finish();
            }
        });

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String jid = username.getText().toString();
                final String passwd = password.getText().toString();

                if(jid.length()>0 && passwd.length()>0) {
                    username.setEnabled(false);
                    password.setEnabled(false);
                    Log.i(TAG, "Login jid " + jid + " password = " + passwd);
                    Runnable runnable = new Runnable() {
                        @Override
                        public void run() {
                            boolean ret = false;

                            do {
                                mXMPP = mAPP.getXMPP();
                                if(mXMPP == null) {
                                    try {
                                        Thread.sleep(200);
                                    } catch (InterruptedException e) {
                                        Log.i(TAG, "sleep Exception " + e.getMessage());
                                        continue;
                                    }
                                    Log.i(TAG, "Thread goto sleep, for XMPP service initialize");
                                }
                            } while(mXMPP == null);

                            ret = mXMPP.doLogin(jid, passwd, LoginActivity.this);

                            if(ret == true) {
                                Log.i(TAG, "Login to jabber server success");
                                Message msg = new Message();
                                msg.what = WowYunApp.LOGIN_SUCCESS;
                                LoginActivity.this.mHandler.sendMessage(msg);
                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                startActivity(intent);
                                LoginActivity.this.finish();
                            } else {
                                Message msg = new Message();
                                msg.what = WowYunApp.LOGIN_FAILURE;
                                LoginActivity.this.mHandler.sendMessage(msg);
                                Log.i(TAG, "Login to jabber server failure");
                            }
                        }
                    };
                    new Thread(runnable).start();
                    View layout = (View)findViewById(R.id.login_status_layout);
                    layout.setVisibility(View.VISIBLE);
                    mStatusText.setText(R.string.action_login_start);
                    //view.post(runnable);
                }
            }
        });
    }

    public void chatCreated(Chat chat, boolean create) {

    }
}
