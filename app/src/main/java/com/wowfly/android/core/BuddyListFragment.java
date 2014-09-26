package com.wowfly.android.core;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.wowfly.wowyun.wowyun_mobile.MainActivity;
import com.wowfly.wowyun.wowyun_mobile.R;
import com.wowfly.wowyun.wowyun_mobile.SNSSyncActivity;
import com.wowfly.wowyun.wowyun_mobile.WebAlbumActivity;

import org.jivesoftware.smack.RosterEntry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by user on 8/19/14.
 */
public class BuddyListFragment extends Fragment {
    private static final String TAG = "BuddyListFragment";
    private AbsListView buddylist;
    private XMPPService mXMPP;
    private WowYunApp mAPP;
    private Handler mHandler;

    public static BuddyListFragment newInstance(XMPPService xmppService, int idx) {
        Log.i(TAG, "xmppService = " + xmppService);

        BuddyListFragment fragment = new BuddyListFragment();
        //BuddyListFragment fragment = new BuddyListFragment();
        //Bundle bundle = new Bundle();
        //bundle.put
        return fragment;
    }

    public BuddyListFragment() {
        mHandler = new Handler() {
            public void handleMessage(android.os.Message msg) {
                switch(msg.what) {
                    case WowYunApp.BUDDY_LIST_BUDDY_REMOVE:
                        mXMPP.getBuddyList();
                        BaseAdapter adapter = (BaseAdapter) buddylist.getAdapter();
                        //adapter.notifyDataSetInvalidated();
                        adapter.notifyDataSetChanged();
                        //ListView listView = (ListView) buddylist;
                        //listView.removeViewAt(msg.arg1);
                        break;
                    case WowYunApp.BUDDY_LIST_UPDATE:
                        mXMPP.getBuddyList();
                        adapter = (BaseAdapter) buddylist.getAdapter();
                        //adapter.notifyDataSetInvalidated();
                        adapter.notifyDataSetChanged();
                        break;
                    case WowYunApp.BUDDY_ADD_SUCCESS:
                        Toast.makeText(getActivity().getApplicationContext(), R.string.action_buddy_add_success, Toast.LENGTH_SHORT).show();
                        mXMPP.getBuddyList();
                        adapter = (BaseAdapter) buddylist.getAdapter();
                        adapter.notifyDataSetChanged();
                        break;

                    case WowYunApp.BUDDY_ADD_FAILURE:
                        Toast.makeText(getActivity().getApplicationContext(), R.string.action_buddy_add_failure, Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        };
    }

    public Handler getHandler() {
        return mHandler;
    }

    public void setArguments(Bundle saved) {
        Log.i(TAG, "This implement is not available");
    }

    public void onActivityCreated(Bundle saved) {
        super.onActivityCreated(saved);

        //Log.i(TAG, "onActivityCreated ");
        buddylist = (ListView) getActivity().findViewById(R.id.buddylist);
        ((ListView) buddylist).setAdapter(new BuddyAdapter());
        buddylist.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                BaseAdapter adapter = (BaseAdapter )adapterView.getAdapter();
                XMPPService.BuddyInfo info = (XMPPService.BuddyInfo)adapter.getItem(i);
                Log.i(TAG, "Item Click " + i + " " + info.name + " " + info.jid);
                showBuddyOptionDialog(info, i);
                return true;
            }
        });

        buddylist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

            }
        });
    }

    private void showBuddyRemoveDialog(final String jid,final int idx) {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        Log.i(TAG, " remove buddy " + jid);
                        Runnable runnable = new Runnable() {
                            @Override
                            public void run() {
                                mXMPP.removeBuddy(jid);
                                Message msg = new Message();
                                msg.what = WowYunApp.BUDDY_LIST_BUDDY_REMOVE;
                                msg.arg1 = idx;
                                mHandler.sendMessage(msg);
                            }
                        };
                        //mXMPP.removeBuddy(jid);
                        new Thread(runnable).start();
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        //No button clicked
                        dialog.cancel();
                        break;
                }
            }
        };

        new AlertDialog.Builder(getActivity()).setTitle("删除设备")
                .setMessage("是否确定删除该设备？")
                .setPositiveButton("确定", dialogClickListener)
                .setNegativeButton("取消", dialogClickListener)
                .show();
    }

    private void showBuddyOptionDialog(final XMPPService.BuddyInfo info, int idx) {
        Log.i(TAG, "show option dialog for " + info.jid);
        List<Map<String, Object>> optionList = new ArrayList<Map<String, Object>>();
        Map<String, Object> item = new HashMap<String, Object>();
/*        item.put("icon", R.drawable.ic_option_icon_message);
        item.put("name", getResources().getString(R.string.dialog_buddy_option1));
        item.put("details", getResources().getString(R.string.dialog_buddy_option1_details));
        optionList.add(item);

        item = new HashMap<String, Object>();*/
        //item.put("icon", R.drawable.ic_option_icon_album);
        item.put("name", "删除该设备");
        item.put("details", "");
        optionList.add(item);

        item = new HashMap<String, Object>();
        item.put("name", getResources().getString(R.string.dialog_buddy_option2));
        item.put("details", getResources().getString(R.string.dialog_buddy_option2_details));
        optionList.add(item);

/*        item = new HashMap<String, Object>();
        //item.put("icon", R.drawable.ic_option_icon_setting);
        item.put("name", getResources().getString(R.string.dialog_buddy_option3));
        item.put("details", getResources().getString(R.string.dialog_buddy_option3_details));
        optionList.add(item);*/

        item = new HashMap<String, Object>();
        //item.put("icon", R.drawable.ic_option_icon_setting);
        item.put("name", getResources().getString(R.string.dialog_buddy_option4));
        item.put("details", getResources().getString(R.string.dialog_buddy_option4_details));
        optionList.add(item);

        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(info.name);

        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View layout = inflater.inflate(R.layout.dialog_buddy_option, null);
        builder.setView(layout);

        final AlertDialog dlg = builder.create();
        dlg.show();

        final ListView _optionList = (ListView) layout.findViewById(R.id.dialog_buddyoption);
        SimpleAdapter sa = new SimpleAdapter((MainActivity)getActivity(), optionList, R.layout.item_list_option,
                new String[] {"name", "details"},
                new int[] {R.id.optionname, R.id.optiondetails});

        _optionList.setAdapter(sa);
        _optionList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                switch(i) {
                    case 0:
                        showBuddyRemoveDialog(info.jid, i);
                        break;
                    case 1:
                        Log.i(TAG, " start remote image activity");
                        Intent intent = new Intent(getActivity(), WebAlbumActivity.class);
                        intent.putExtra("jid", info.jid);
                        startActivity(intent);
                        break;
                    case 2:
                        intent = new Intent(getActivity(), SNSSyncActivity.class);
                        intent.putExtra("jid", info.jid);
                        startActivity(intent);
                        break;
                }
                dlg.cancel();
            }
        });

    }

    public void onCreate(Bundle saved) {
        super.onCreate(saved);
        Log.i(TAG, "Activity = " + getActivity());
        mAPP = (WowYunApp)getActivity().getApplication();
        mXMPP = mAPP.getXMPP();
/*        mXMPP = mAPP.getXMPP();
        if(mXMPP != null) {
            mXMPP.getBuddyList();
        }*/
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.buddylistfragment, container, false);
    }

    static class ViewHolder {
        ImageView  buddyicon;
        TextView   buddyname;
    }

    public class BuddyAdapter extends BaseAdapter {
        public int getCount() {
            Log.i(TAG, " getCount");
            if(mXMPP != null) {
                int n = mXMPP.getBuddyCount();
                Log.i(TAG, "get buddy Count = " + n);
                return n;
            }

            return 0;
        }

        public Object getItem(int position) {
            return mXMPP.getBuddyInfo(position);
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(int pos, View view, ViewGroup parent) {
            View myview = view;
            final ViewHolder holder;

            if(myview == null) {
                myview = getActivity().getLayoutInflater().inflate(R.layout.item_list_buddy, parent, false);
                holder = new ViewHolder();

                holder.buddyicon = (ImageView) myview.findViewById(R.id.buddyicon);
                holder.buddyname = (TextView) myview.findViewById(R.id.buddyname);
                XMPPService.BuddyInfo info = mXMPP.getBuddyInfo(pos);
                if(info == null) {
                    Log.i(TAG, "getView pos = " + pos);
                    return null;
                }
                if(info.name != null) {
                    Log.i(TAG, "*name = " + info.name);
                    holder.buddyname.setText(info.name);
                } else
                    holder.buddyname.setText(info.jid);

                if(info.name != null && info.name.charAt(0) == 'D')
                    holder.buddyicon.setImageResource(R.drawable.ic_digital_device);
                else {
                    if (info.isAvailable) {
                        holder.buddyicon.setImageResource(R.drawable.ic_buddy_online);
                    } else {
                        holder.buddyicon.setImageResource(R.drawable.ic_buddy_offline);
                    }
                }

                myview.setTag(holder);
            } else {
                holder = (ViewHolder) view.getTag();
            }

            return myview;
        }

    }
}
