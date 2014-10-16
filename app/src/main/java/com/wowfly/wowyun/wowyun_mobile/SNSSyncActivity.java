package com.wowfly.wowyun.wowyun_mobile;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;

import com.wowfly.android.core.WowYunApp;
import com.wowfly.android.core.XMPPService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by user on 9/13/14.
 */
public class SNSSyncActivity extends Activity {
    private static final String TAG = "SNSSyncActivity";
    List<Map<String, Object>> optionList = new ArrayList<Map<String, Object>>();
    private SharedPreferences mPref;
    private String uid;
    private String jid;
    private EditText usernameET;
    private EditText passwordET;
    private Handler mHandler;
    private AlertDialog mBindDlg;
    private int nItemIdx = 0;
    private ListView listView;
    private QzoneScrapy qzoneScrapy;
    private SinaWeiBoScrapy sinaWeiBoScrapy;

    public final static int BIND_SUCCESS = 0x2000;
    public final static int BIND_FAILURE = 0x2001;
    public final static int LOGIN_VC = 0x2002;

    protected void onCreate(Bundle saved) {
        //ImageButton sinaweibo, qzone, qqweibo, renren;
        super.onCreate(saved);

        Intent i = getIntent();
        Bundle extras = i.getExtras();
        jid = extras.getString("jid");
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                WowYunApp _app = (WowYunApp)getApplication();
                XMPPService xmppService = null;
                do {
                    xmppService = _app.getXMPP();
                    if(xmppService == null) {
                        try {
                            Thread.sleep(300);
                            Log.i(TAG, " sleep 300ms, waiting for xmpp connected");
                        } catch (InterruptedException e) {
                        }
                    }
                } while(xmppService == null);
                uid = xmppService.getMyJid();
            }
        };
        new Thread(runnable).start();

        setContentView(R.layout.activity_snssync);

        mPref = getSharedPreferences("wowyun-mobile", Context.MODE_PRIVATE);
        doInit();

        mHandler = new Handler() {
            public void handleMessage(android.os.Message msg) {
                switch (msg.what) {
                    case LOGIN_VC:
                        showVCDialog();
                        break;

                    case BIND_FAILURE:
                        Log.i(TAG, " bind action failure");
                        ProgressBar bar = (ProgressBar) mBindDlg.findViewById(R.id.login_progressbar);
                        bar.setVisibility(View.INVISIBLE);
                        usernameET.setEnabled(true);
                        passwordET.setEnabled(true);
                        mBindDlg.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                        break;
                    case BIND_SUCCESS:
                        Log.i(TAG, " bind action success");
                        mBindDlg.cancel();
                        Bundle data = msg.getData();
                        String nickname = data.getString("nickname");
                        String username = data.getString("username");
                        String password = data.getString("password");
                        String snstype = data.getString("snstype");
                        Log.i(TAG, "bind " + snstype + " success " + " nickname " + nickname);
                        BaseAdapter adapter = (BaseAdapter) listView.getAdapter();
                        HashMap<String, Object> item = (HashMap<String, Object>)adapter.getItem(nItemIdx);
                        item.put("nickname", nickname);
                        String key = "sns." + uid + "." + jid;
                        SharedPreferences.Editor editor = mPref.edit();
                        editor.putString(key+"."+snstype+".username", username);
                        editor.putString(key+"."+snstype+".password", password);
                        editor.putString(key+"."+snstype+".nickname", nickname);
                        editor.commit();
                        adapter.notifyDataSetInvalidated();
                        WowYunApp _app = (WowYunApp) getApplication();
                        XMPPService _xmpp = _app.getXMPP();
                        if(_xmpp != null) {
                            int sep = jid.indexOf("@");
                            String _jid = jid.substring(0, sep);
                            Log.i(TAG, " send snsbind message to " + _jid);
                            _xmpp.doSend(_jid, "snsbind/"+snstype+"/"+username+"/"+password);
                        }
                        break;
                }
            }
        };
        this.setTitle(R.string.dialog_buddy_option4);
        listView = (ListView) findViewById(R.id.snsaccount_list);
        SimpleAdapter sa = new SimpleAdapter(this, optionList, R.layout.item_list_sns,
                new String[] {"icon", "name", "nickname"},
                new int[] {R.id.snslogo, R.id.snsname, R.id.nickname});
        listView.setAdapter(sa);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String key = "sns." + uid + "." + jid;
                nItemIdx = i;
                switch (i) {
                    case 0:
                        String username = mPref.getString(key+".weibo.username", "");
                        if(username.equals("")) {
                            /**display bind dialog*/
                            //showSNSWeiboUnbindDialog();
                            showSNSWeiboBindDialog();
                        } else {
                            /**display unbind dialog*/
                            //showSNSWeiboUnbindDialog();
                            Log.i(TAG, "unbind nickname " + mPref.getString(key+".weibo.nickname", ""));
                            showSNSWeiboUnbindDialog();
                        }
                        break;
                    case 1:
                        username = mPref.getString(key+".qzone.username", "");
                        Log.i(TAG, " qzone username " + username);
                        if(username.equals("")) {
                            /**display bind dialog*/
                            //showSNSWeiboUnbindDialog();
                            showSNSQzoneBindDialog();
                        } else {
                            /**display unbind dialog*/
                            //showSNSWeiboUnbindDialog();
                            Log.i(TAG, "unbind nickname " + mPref.getString(key+".qzone.nickname", ""));
                            showSNSQzoneUnbindDialog();
                        }
                        break;
                }
            }
        });
    }

    private void showVCDialog() {
        BitmapFactory.Options opt = new BitmapFactory.Options();
        opt.inPreferredConfig = Bitmap.Config.ARGB_8888;
        //opt.inSampleSize = 2;
        Bitmap bm = BitmapFactory.decodeFile("/sdcard/qzone-vc.jpg", opt);
        LayoutInflater _inflater = getLayoutInflater();
        View _layout = _inflater.inflate(R.layout.dialog_vc_input, null);
        final EditText _input = (EditText) _layout.findViewById(R.id.dialog_vc_input_edit);
        ImageView imageView = (ImageView) _layout.findViewById(R.id.dialog_vc_bitmap);
        imageView.setImageBitmap(bm);

        final AlertDialog.Builder pwdBuilder = new AlertDialog.Builder(this);
        pwdBuilder.setTitle("验证码输入")
                .setView(_layout)
                .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Editable text = _input.getText();
                        Log.i(TAG, " vcode " + text.toString());
                        //person.setVC(snstype, text.toString());
                        qzoneScrapy.doBind(text.toString().toUpperCase());
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                }).show();

    }

    private void showSNSQzoneBindDialog() {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        //doBingWeibo();
                        AlertDialog dlg = (AlertDialog) dialog;
                        usernameET = (EditText) mBindDlg.findViewById(R.id.login_edit_username);
                        passwordET = (EditText) mBindDlg.findViewById(R.id.login_edit_password);
                        qzoneScrapy = new QzoneScrapy(getApplicationContext(), usernameET.getText().toString(), passwordET.getText().toString(), mHandler);
                        qzoneScrapy.doBind(null);
                        usernameET.setEnabled(false);
                        passwordET.setEnabled(false);
                        dlg.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                        //dlg.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                        View layout = (View) mBindDlg.findViewById(R.id.login_status_layout);
                        layout.setVisibility(View.VISIBLE);

                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        //No button clicked
                        dialog.cancel();
                        break;
                }
            }
        };
        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.dialog_bing_weibo, (ViewGroup) findViewById(R.id.weibo_bind_dialog));
        mBindDlg = new AlertDialog.Builder(this).setTitle("绑定QQ空间帐号")
                .setView(layout)
                .setPositiveButton("确定", dialogClickListener)
                .setNegativeButton("取消", dialogClickListener)
                .show();
    }

    private void showSNSWeiboBindDialog() {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        //doBingWeibo();
                        AlertDialog dlg = (AlertDialog) dialog;
                        usernameET = (EditText) mBindDlg.findViewById(R.id.login_edit_username);
                        passwordET = (EditText) mBindDlg.findViewById(R.id.login_edit_password);
                        sinaWeiBoScrapy = new SinaWeiBoScrapy(getApplicationContext(), usernameET.getText().toString(), passwordET.getText().toString(), mHandler);
                        sinaWeiBoScrapy.doBind();
                        usernameET.setEnabled(false);
                        passwordET.setEnabled(false);
                        dlg.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                        //dlg.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                        View layout = (View) mBindDlg.findViewById(R.id.login_status_layout);
                        layout.setVisibility(View.VISIBLE);

                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        //No button clicked
                        dialog.cancel();
                        break;
                }
            }
        };
        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.dialog_bing_weibo, (ViewGroup) findViewById(R.id.weibo_bind_dialog));
        mBindDlg = new AlertDialog.Builder(this).setTitle("绑定微博帐号")
                .setView(layout)
                .setPositiveButton("确定", dialogClickListener)
                .setNegativeButton("取消", dialogClickListener)
                .show();
    }

    private void showSNSQzoneUnbindDialog() {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String snstype="";

                switch (nItemIdx) {
                    case 1: snstype = "qzone";break;
                    default: dialog.cancel(); return;
                }
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        //Yes button clicked
                        //doBingWeibo();
                        BaseAdapter adapter = (BaseAdapter) listView.getAdapter();
                        HashMap<String, Object> item = (HashMap<String, Object>)adapter.getItem(nItemIdx);
                        item.put("nickname", "");
                        adapter.notifyDataSetInvalidated();

                        String key = "sns." + uid + "." + jid;
                        String username = mPref.getString(key+"."+snstype+".username", "");
                        String password = mPref.getString(key+"."+snstype+".username", "");
                        SharedPreferences.Editor editor = mPref.edit();
                        editor.putString(key+"."+snstype+".username", "");
                        editor.putString(key+"."+snstype+".password", "");
                        editor.putString(key+"."+snstype+".nickname", "");
                        editor.commit();
                        dialog.cancel();

                        WowYunApp _app = (WowYunApp) getApplication();
                        XMPPService _xmpp = _app.getXMPP();
                        if(_xmpp != null) {
                            int sep = jid.indexOf("@");
                            String _jid = jid.substring(0, sep);
                            Log.i(TAG, " send snsunbind message to " + _jid);
                            _xmpp.doSend(_jid, "snsunbind/"+snstype+"/"+username+"/"+password);
                        }

                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        //No button clicked
                        dialog.cancel();
                        break;
                }
            }
        };

        new AlertDialog.Builder(this).setTitle("解除绑定QQ空间帐号")
                .setMessage("是否取消绑定？")
                .setPositiveButton("确定", dialogClickListener)
                .setNegativeButton("取消", dialogClickListener)
                .show();
    }

    private void showSNSWeiboUnbindDialog() {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String snstype="";

                switch (nItemIdx) {
                    case 0: snstype = "weibo";break;
                    default: dialog.cancel(); return;
                }
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        //Yes button clicked
                        //doBingWeibo();
                        BaseAdapter adapter = (BaseAdapter) listView.getAdapter();
                        HashMap<String, Object> item = (HashMap<String, Object>)adapter.getItem(nItemIdx);
                        item.put("nickname", "");
                        adapter.notifyDataSetInvalidated();

                        String key = "sns." + uid + "." + jid;
                        String username = mPref.getString(key+"."+snstype+".username", "");
                        String password = mPref.getString(key+"."+snstype+".username", "");
                        SharedPreferences.Editor editor = mPref.edit();
                        editor.putString(key+"."+snstype+".username", "");
                        editor.putString(key+"."+snstype+".password", "");
                        editor.putString(key+"."+snstype+".nickname", "");
                        editor.commit();
                        dialog.cancel();

                        WowYunApp _app = (WowYunApp) getApplication();
                        XMPPService _xmpp = _app.getXMPP();
                        if(_xmpp != null) {
                            int sep = jid.indexOf("@");
                            String _jid = jid.substring(0, sep);
                            Log.i(TAG, " send snsunbind message to " + _jid);
                            _xmpp.doSend(_jid, "snsunbind/"+snstype+"/"+username+"/"+password);
                        }

                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        //No button clicked
                        dialog.cancel();
                        break;
                }
            }
        };

        new AlertDialog.Builder(this).setTitle("解除绑定微博帐号")
                .setMessage("是否取消绑定？")
                .setPositiveButton("确定", dialogClickListener)
                .setNegativeButton("取消", dialogClickListener)
                .show();
    }

    private void doInit() {
        String key = "sns." + uid + "." + jid;
        Map<String, Object> item = new HashMap<String, Object>();

        item.put("icon", R.drawable.sns_logo_sinaweibo);
        item.put("name", getResources().getString(R.string.sns_sinaweibo));
        item.put("username", mPref.getString(key+".weibo.username", ""));
        item.put("password", mPref.getString(key+".weibo.password", ""));
        item.put("nickname", mPref.getString(key+".weibo.nickname", ""));
        optionList.add(item);

        item = new HashMap<String, Object>();
        item.put("icon", R.drawable.sns_logo_qzone);
        item.put("name", getResources().getString(R.string.sns_qzone));
        item.put("username", mPref.getString(key+".qzone.username", ""));
        item.put("password", mPref.getString(key+".qzone.password", ""));
        item.put("nickname", mPref.getString(key+".qzone.nickname", ""));
        optionList.add(item);

        item = new HashMap<String, Object>();
        item.put("icon", R.drawable.sns_logo_qqweibo);
        item.put("name", getResources().getString(R.string.sns_qqweibo));
        optionList.add(item);

        item = new HashMap<String, Object>();
        item.put("icon", R.drawable.sns_logo_renren);
        item.put("name", getResources().getString(R.string.sns_renren));
        optionList.add(item);
    }
}
