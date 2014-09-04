package com.wowfly.wowyun.wowyun_mobile;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.wowfly.android.core.WowYunApp;
import com.wowfly.android.core.XMPPService;

/**
 * Created by user on 8/24/14.
 */
public class RegisterActivity extends Activity {
    private static final String TAG = "RegisterActivity";
    private XMPPService mXMPP;
    private WowYunApp mAPP;
    private Handler mHandler;
    private EditText username;
    private EditText password;
    private EditText checkd_password;
    private TextView statusText;

    protected void onCreate(Bundle saved) {
        final Button btn;
        super.onCreate(saved);

        setContentView(R.layout.activity_register);
        mAPP = (WowYunApp) getApplication();
        btn = (Button) findViewById(R.id.register_ok_button);
        username = (EditText) findViewById(R.id.register_edit_username);
        password = (EditText) findViewById(R.id.register_edit_password);
        checkd_password = (EditText) findViewById(R.id.register_checked_password);
        statusText = (TextView) findViewById(R.id.register_status_text);

        mHandler = new Handler() {

            public void handleMessage(Message msg) {
                View layout;
                //ImageView statusIcon = (ImageView)findViewById(R.id.login_status_icon);
                switch (msg.what) {
                    case WowYunApp.REGISTER_SUCCESS:
                        statusText.setText(R.string.action_register_success);
                        layout = findViewById(R.id.register_status_layout);
                        //layout.setVisibility(View.GONE);
                        layout.setEnabled(false);
                        btn.setText(R.string.register_backto_login);
                        Toast.makeText(getApplicationContext(), R.string.action_register_success, Toast.LENGTH_SHORT).show();
                        btn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                                intent.putExtra("username", username.getText().toString());
                                startActivity(intent);
                                RegisterActivity.this.finish();
                            }
                        });
                        break;
                    case WowYunApp.REGISTER_FAILURE:
                        statusText.setText(R.string.action_register_failure);
                        username.setEnabled(true);
                        password.setEnabled(true);
                        checkd_password.setEnabled(true);
                        layout = findViewById(R.id.register_status_layout);
                        layout.setVisibility(View.GONE);
                        Toast.makeText(getApplicationContext(), R.string.action_register_failure, Toast.LENGTH_SHORT).show();
                        break;
                }
                super.handleMessage(msg);
            }
        };

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String jid = username.getText().toString();
                final String passwd = password.getText().toString();
                final String checkd_passwd = checkd_password.getText().toString();

                if(checkd_passwd.equals(passwd)==false) {
                    //statusText.setText(R.string.register_invalid_password);
                    password.setText("");
                    checkd_password.setText("");
                    Toast.makeText(getApplicationContext(), R.string.register_invalid_password, Toast.LENGTH_SHORT).show();
                    View layout = findViewById(R.id.register_status_layout);
                    layout.setVisibility(View.GONE);
                } else {
                    if(jid.length() > 0 && passwd.length() > 0) {
                        username.setEnabled(false);
                        password.setEnabled(false);
                        checkd_password.setEnabled(false);
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

                                ret = mXMPP.doRegister(jid, passwd);
                                Log.i(TAG, "XMPP register status = " + ret);
                                if(ret == true) {
                                    Message msg = new Message();
                                    msg.what = WowYunApp.REGISTER_SUCCESS;
                                    RegisterActivity.this.mHandler.sendMessage(msg);
                                } else {
                                    Message msg = new Message();
                                    msg.what = WowYunApp.REGISTER_FAILURE;
                                    RegisterActivity.this.mHandler.sendMessage(msg);
                                }
                            }
                        };
                        new Thread(runnable).start();
                    }
                }
            }
        });
        //mXMPP = mAPP.getXMPP();
    }
}
