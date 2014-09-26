package com.wowfly.wowyun.wowyun_mobile;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import android.app.Activity;
import android.app.ActionBar;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.support.v13.app.FragmentPagerAdapter;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.ActionMode;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.wowfly.android.core.BuddyListFragment;
import com.wowfly.android.core.ImageViewFragment;
import com.wowfly.android.core.MediaInfo;
import com.wowfly.android.core.VideoViewFragment;
import com.wowfly.android.core.WowYunApp;
import com.wowfly.android.core.XMPPService;

import org.apache.http.Header;
import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManagerListener;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.RosterListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;

public class MainActivity extends Activity implements ActionBar.TabListener, ChatManagerListener, MessageListener, AbsListView.MultiChoiceModeListener {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v13.app.FragmentStatePagerAdapter}.
     */
    SectionsPagerAdapter mSectionsPagerAdapter;
    private Chat newChat;
    private static final String TAG = "Wowyun";
    private TextView mUploadInfoText;
    private WowYunApp mAPP;
    private XMPPService mXMPP;
    private Map<Integer, String> mImageUploadList = new HashMap<Integer, String>();
    private Map<Integer, String> mVideoUploadList = new HashMap<Integer, String>();
    private boolean isImage = true;
    private ImageViewFragment mImageViewFragment;
    private VideoViewFragment mVideoViewFragment;
    private BuddyListFragment mBuddyListFragment;
    private static final AsyncHttpClient mUploadClient = new AsyncHttpClient();
    private ArrayList<String> mUploadList;
    private Handler mHandler;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;


    private RosterListener mRosterListener = new RosterListener() {
        @Override
        public void entriesAdded(Collection<String> strings) {
            for(String item: strings) {
                Log.i(TAG, " buddy added " + item);
            }
        }

        @Override
        public void entriesUpdated(Collection<String> strings) {
            for(String item: strings) {
                Log.i(TAG, " buddy entriesUpdated " + item);
            }
        }

        @Override
        public void entriesDeleted(Collection<String> strings) {
            for(String item: strings) {
                Log.i(TAG, " buddy entriesDeleted " + item);
            }
        }

        @Override
        public void presenceChanged(Presence presence) {
            if(mBuddyListFragment != null) {
                Handler handler = mBuddyListFragment.getHandler();
                Log.i(TAG, " presenceChanged " + presence.getType().toString() + " jid = " + presence.getFrom());
                if (presence.getType() == Presence.Type.unavailable) {
                    android.os.Message msg = new android.os.Message();
                    msg.what = WowYunApp.BUDDY_LIST_UPDATE;
                    handler.sendMessage(msg);
                    Log.i(TAG, " buddy " + presence.getFrom() + " status change to unavailable ");
                } else if (presence.getType() == Presence.Type.available) {
                    android.os.Message msg = new android.os.Message();
                    msg.what = WowYunApp.BUDDY_LIST_UPDATE;
                    handler.sendMessage(msg);
                    Log.i(TAG, " buddy " + presence.getFrom() + " status change to available ");
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mUploadList = new ArrayList<String>(128);


/*        MediaInfo mi = new MediaInfo(MainActivity.this.getContentResolver());
        mi.getVideosInfo();*/

        mHandler = new Handler() {
            public void handleMessage(android.os.Message msg) {
                switch(msg.what) {
                    case WowYunApp.BUDDY_ADD_SUCCESS:
                        Toast.makeText(getApplicationContext(), R.string.action_buddy_add_success, Toast.LENGTH_SHORT).show();
                        mXMPP.getBuddyList();
                        break;

                    case WowYunApp.BUDDY_ADD_FAILURE:
                        Toast.makeText(getApplicationContext(), R.string.action_buddy_add_failure, Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        };

        ActivityManager mgr = (ActivityManager)this.getSystemService(Context.ACTIVITY_SERVICE);
        Log.i(TAG, "MemoryClass = " + mgr.getMemoryClass());
        // Set up the action bar.
        final ActionBar actionBar = getActionBar();

        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        mAPP = (WowYunApp) getApplication();
        mXMPP = mAPP.getXMPP();
        mXMPP.setChatManagerListener(MainActivity.this);
        mXMPP.addRosterListener(mRosterListener);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        // When swiping between different sections, select the corresponding
        // tab. We can also use ActionBar.Tab#select() to do this if we have
        // a reference to the Tab.
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
            }
        });

        // For each of the sections in the app, add a tab to the action bar.
        for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
            // Create a tab with text corresponding to the page title defined by
            // the adapter. Also specify this Activity object, which implements
            // the TabListener interface, as the callback (listener) for when
            // this tab is selected.
            actionBar.addTab(
                    actionBar.newTab()
                            .setText(mSectionsPagerAdapter.getPageTitle(i))
                            .setTabListener(this));
        }
        //new XMPPService().execute("login:admin:admin");
        //mXMPP.execute("login:admin:admin");
/*        Runnable runnable = new Runnable() {
            @Override
            public void run() {

                boolean ret = mXMPP.doLogin("admin", "admin", MainActivity.this);
                Log.i(TAG, "Login.RET = " + ret);
            }
        };
        new Thread(runnable).start();*/
/*        mHandler = new Handler() {
            public void handleMessage(android.os.Message msg) {
                switch(msg.what) {
                    case WowYunApp.BUDDY_ADD_SUCCESS:
                        Toast.makeText(getApplicationContext(), R.string.action_buddy_add_success, Toast.LENGTH_SHORT).show();
                        mXMPP.getBuddyList();
                        break;

                    case WowYunApp.BUDDY_ADD_FAILURE:
                        Toast.makeText(getApplicationContext(), R.string.action_buddy_add_failure, Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        };*/

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                mXMPP.getBuddyList();
                Log.i(TAG, "finish get buddy list, try to update Buddy list fragment");
                android.os.Message msg = new android.os.Message();
                msg.what = WowYunApp.BUDDY_LIST_UPDATE;

                if(mBuddyListFragment!= null)
                    mBuddyListFragment.getHandler().sendMessage(msg);
            }
        };
        new Thread(runnable).start();

    }


    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        View v = LayoutInflater.from(this).inflate(R.layout.action_longpress_overlay, null);
        String sInfo = getResources().getString(R.string.upload_select_info);
        sInfo = String.format(sInfo, 0);

        if(mImageUploadList.size() > 0)
            mImageUploadList.clear();

        if(mVideoUploadList.size() > 0)
            mVideoUploadList.clear();

        mUploadInfoText = (TextView)v.findViewById(R.id.info_selection_status);
        //mUploadInfoText.setText("This is a demo");

        mode.setCustomView(v);
        getMenuInflater().inflate(R.menu.upload_mode, menu);

        return true;

    }

    public void onDestroyActionMode(ActionMode mode) {

    }

    public boolean isInUploadList(int pos, boolean isImage) {
        if(isImage) {
            return mImageUploadList.containsKey(Integer.valueOf(pos));
        } else {
            return mVideoUploadList.containsKey(Integer.valueOf(pos));
        }
    }

    public void removeFromUploadList(int pos, boolean isImage) {
        if(isImage) {
            mImageUploadList.remove(Integer.valueOf(pos));
        } else {
            mVideoUploadList.remove(Integer.valueOf(pos));
        }
    }

    private void doUploadFile(String from_jid, String to_jid) {
        int sep = to_jid.indexOf('@');
        to_jid = to_jid.substring(0, sep);

        Iterator iter = mImageUploadList.keySet().iterator();
        while(iter.hasNext()) {
            Object key = iter.next();
            Object val = mImageUploadList.get(key);
            final String path = (String) val;
            Integer pos = (Integer) key;
            //ProgressBar progressBar = mImageViewFragment.getProgressBarFromPosition(pos.intValue());

            RequestParams params = new RequestParams();
            params.add("from_jid", from_jid);
            params.add("to_jid", to_jid);

            try {
                File file = new File(path);
                params.put("upload", file, "image/jpeg");
            } catch (FileNotFoundException e) {
                Log.i(TAG, "upload file " + path + "not found");
                continue;
            }
            sep = path.lastIndexOf('/');
            String name = path.substring(sep+1);
            Log.i(TAG, "upload file " + path);

            mUploadClient.post("http://101.69.230.238:8080/upload", params, new UploadHttpResponseHandler((WowYunApp) getApplication(), to_jid,  true, name, mImageViewFragment.getGridView(), pos.intValue()));
        }
        mImageUploadList.clear();

        iter = mVideoUploadList.keySet().iterator();
        while(iter.hasNext()) {
            Object key = iter.next();
            Object val = mVideoUploadList.get(key);
            final String path = (String) val;
            Integer pos = (Integer) key;
            //ProgressBar progressBar = mImageViewFragment.getProgressBarFromPosition(pos.intValue());

            RequestParams params = new RequestParams();
            params.add("from_jid", from_jid);
            params.add("to_jid", to_jid);

            try {
                File file = new File(path);
                params.put("upload", file, "image/jpeg");
            } catch (FileNotFoundException e) {
                Log.i(TAG, "upload file " + path + "not found");
                continue;
            }
            sep = path.lastIndexOf('/');
            String name = path.substring(sep+1);
            Log.i(TAG, "upload file " + path);

            mUploadClient.post("http://101.69.230.238:8080/upload", params, new UploadHttpResponseHandler((WowYunApp)getApplication(), to_jid, false, name, mVideoViewFragment.getGridView(), pos.intValue()));
        }
        mVideoUploadList.clear();

    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK) {
            moveTaskToBack(false);
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    private void showBuddyListDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.title_media_share);

        LayoutInflater inflater = getLayoutInflater();
        final View layout = inflater.inflate(R.layout.dialog_media_share, null);
        builder.setView(layout);

        final AlertDialog dlg = builder.create();
        dlg.show();

        final ListView _buddyList = (ListView) layout.findViewById(R.id.dialog_buddylist);
        SimpleAdapter sa = new SimpleAdapter(this, mXMPP.getBuddyData(), R.layout.item_list_buddy,
                new String[] {"icon", "name"},
                new int[] {R.id.buddyicon, R.id.buddyname});
        _buddyList.setAdapter(sa);
        _buddyList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Map<String, Object> item = mXMPP.getBuddyData().get(i);
                String jid = (String)item.get("jid");
                Log.i(TAG, "select buddy name = " + jid);
                doUploadFile(mXMPP.getMyJid(),jid);
                dlg.cancel();
            }
        });
    }

    private void showAddBuddyDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.title_add_buddy);

        LayoutInflater inflater = getLayoutInflater();
        final View layout = inflater.inflate((R.layout.dialog_buddy_add), null);
        builder.setView(layout);

        TextView jid = (TextView)layout.findViewById(R.id.jidtext);
        jid.setText(R.string.jidtext);

        final AlertDialog dlg = builder.create();
        dlg.show();

        Button btn = (Button) layout.findViewById(R.id.button_cancel);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dlg.cancel();
            }
        });

        btn = (Button) layout.findViewById(R.id.button_addbuddy);
        btn.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mXMPP != null) {
                    EditText edit = (EditText) layout.findViewById(R.id.jidedit);
                    final String jid = edit.getText().toString();
                    Runnable runnable = new Runnable() {
                        @Override
                        public void run() {
                            if(mXMPP != null) {
                                boolean ret = mXMPP.addBuddybyJid(jid);
                                if(ret == true) {
                                    android.os.Message msg = new android.os.Message();
                                    //Message msg = new Message();
                                    msg.what = WowYunApp.BUDDY_ADD_SUCCESS;
                                    if(mBuddyListFragment != null)
                                        mBuddyListFragment.getHandler().sendMessage(msg);
                                    else
                                        mHandler.sendMessage(msg);
                                    //MainActivity.this.mHandler.sendMessage(msg);
                                    //BuddyListFragment.
                                } else {
                                    android.os.Message msg = new android.os.Message();
                                    msg.what = WowYunApp.BUDDY_ADD_FAILURE;
                                    if(mBuddyListFragment != null)
                                        mBuddyListFragment.getHandler().sendMessage(msg);
                                    else
                                        mHandler.sendMessage(msg);
                                    //MainActivity.this.mHandler.sendMessage(msg);
                                }
                            }
                        }
                    };
                    new Thread(runnable).start();
                    //mXMPP.addBuddybyJid(jid);
                    dlg.cancel();
                }
            }
        });



    }

/*    DialogInterface.OnClickListener onBuddySelect = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialogInterface, int i) {
            Log.i(TAG, "Buddy select " + i);
            switch(i) {

            }
        }
    }*/

    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        Log.i(TAG, "ActionMode Item Clicked id = " + item.getItemId());
        switch (item.getItemId()) {
            case R.id.action_upload:
/*                Iterator iter = mImageUploadList.keySet().iterator();
                while(iter.hasNext()) {
                    Object key = iter.next();
                    Object val = mImageUploadList.get(key);
                    mUploadList.add((String)val);
                }
                iter = mVideoUploadList.keySet().iterator();
                while (iter.hasNext()) {
                    Object key = iter.next();
                    Object val = mVideoUploadList.get(key);
                    mUploadList.add((String)val);
                }*/
                showBuddyListDialog();
                //doUploadFile();
                break;
        }
        mode.finish();
        return true;
    }

    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return true;
    }


    public void onItemCheckedStateChanged(ActionMode mode, int pos, long id, boolean checked) {
        String sInfo = getResources().getString(R.string.upload_select_info);

        if(isImage){
            if(checked) {
                mImageUploadList.put(Integer.valueOf(pos), mImageViewFragment.getPathFromPosition(pos));
            } else {
                Log.i(TAG, "onItemCheckedStateChanged pos = " + pos + " path = " + mImageViewFragment.getPathFromPosition(pos));
                mImageUploadList.remove(Integer.valueOf(pos));
            }
        } else {
            if(checked) {
                mVideoUploadList.put(Integer.valueOf(pos), mVideoViewFragment.getPathFromPosition(pos));
            } else {
                Log.i(TAG, "onItemCheckedStateChanged pos = " + pos + " path = " + mVideoViewFragment.getPathFromPosition(pos));
                mVideoUploadList.remove(Integer.valueOf(pos));
            }
        }

        sInfo = String.format(sInfo, mImageUploadList.size()+mVideoUploadList.size());
        mUploadInfoText.setText(sInfo);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        //menu.add(0, 0, 0, R.string.action_upload).setIcon(R.drawable.ic_action_upload).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        //menu.add(0, 1, 0, R.string.action_add_buddy).setIcon(R.drawable.ic_action_add_buddy).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id) {
            case R.id.action_settings:
                Intent intent = new Intent(this, OptionActivity.class);
                startActivity(intent);
                break;
            case R.id.action_quit:
                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which){
                            case DialogInterface.BUTTON_POSITIVE:
                                //Yes button clicked
                                MainActivity.this.finish();
                                //System.exit(0);
                                break;

                            case DialogInterface.BUTTON_NEGATIVE:
                                //No button clicked
                                break;
                        }
                    }
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("是否退出?")
                        .setTitle("提醒")
                        .setPositiveButton("确认", dialogClickListener)
                        .setNegativeButton("取消", dialogClickListener).show();
                break;

            case R.id.action_add_buddy:
                showAddBuddyDialog();
                break;
        }

        Log.i(TAG, "Options Item Selected");
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        // When the given tab is selected, switch to the corresponding page in
        // the ViewPager.
        int idx = tab.getPosition();
        Log.i(TAG, "Tab " + idx + " selected");

        if(idx == 0) {
            isImage = true;
        } else {
            isImage =false;
        }
/*        if(mXMPP != null) {
            mXMPP.doSend("u001@wuruxu.com", "Tab " + idx + " was selected");
        }*/

        mViewPager.setCurrentItem(tab.getPosition());

    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            switch(position) {
                case 0:
                    mImageViewFragment = ImageViewFragment.newInstance(0);
                    return mImageViewFragment;
                case 1:
                    mVideoViewFragment = VideoViewFragment.newInstance(1);
                    return mVideoViewFragment;
                case 2:
                    mBuddyListFragment = BuddyListFragment.newInstance(mXMPP, 2);
                    return mBuddyListFragment;
                default:
                    return PlaceholderFragment.newInstance(position + 1);
            }
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return getString(R.string.title_section1);
                case 1:
                    return getString(R.string.title_section2);
                case 2:
                    return getString(R.string.title_section3);
            }
            return null;
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            return rootView;
        }
    }

    public void chatCreated(Chat chat, boolean createdLocally) {
        newChat = chat;
        newChat.addMessageListener(this);
    }
    public Chat getNewChat() {
        return newChat;
    }

    public void processMessage(Chat chat, Message msg) {
        Log.i(TAG, "new Message " + msg);
    }
}
