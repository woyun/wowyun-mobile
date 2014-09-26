package com.wowfly.android.core;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.wowfly.wowyun.wowyun_mobile.MainActivity;
import com.wowfly.wowyun.wowyun_mobile.R;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Created by user on 8/15/14.
 */
public class ImageViewFragment extends Fragment {
    private static final String TAG = "ImageViewFragment";
    private MediaLoader mLoader = null;
    private AbsListView listView;
    private TextView mInfoHeader;
    private boolean mSelectMode = false;
    private SharedPreferences mPref;

    public static ImageViewFragment newInstance(int idx) {
        ImageViewFragment fragment = new ImageViewFragment();
        return fragment;
    }

    public ImageViewFragment() {
    }

    public String getPathFromPosition(int pos) {
        if(mLoader != null) {
            return mLoader.getMediaInfo().getImagePath(pos);
        }
        return "";
    }

    public GridView getGridView() {
        GridView view = (GridView) listView;

        return view;
    }

    private void showFolderListDialog() {
        final List<Map<String, Object>> bucketList = mLoader.getMediaInfo().getBucketList(true, mPref);
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        Log.i(TAG, " Apply button clicked");
                        dialog.cancel();

                        int n = bucketList.size();
                        Set<String> folders = new HashSet<String>();
                        for(int idx=0; idx<n; idx++) {
                            Map<String, Object> item = (Map<String, Object>) bucketList.get(idx);
                            Boolean bChecked = (Boolean) item.get(new String("checked"));
                            Log.i(TAG, " bucket = " + item.get(new String("bucket")) + " checked = " + bChecked);
                            if(bChecked == true) {
                                String bucket = (String) item.get(new String("bucket"));
                                folders.add(bucket);
                            }
                        }

                        Log.i(TAG, " folders.filter.sets = " + folders.toString());
                        SharedPreferences.Editor editor = mPref.edit();
                        editor.putStringSet("images.bucket.filter", folders);
                        editor.commit();

                        mLoader.getMediaInfo().getImagesInfo(mPref);
                        BaseAdapter adapter = (BaseAdapter)listView.getAdapter();
                        adapter.getCount();
                        adapter.notifyDataSetInvalidated();
                        String sInfo = getResources().getString(R.string.images_header_status);
                        sInfo = String.format(sInfo, mLoader.getMediaInfo().getImageCount());
                        mInfoHeader.setText(sInfo);
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        //No button clicked
                        dialog.cancel();
                        break;
                }
            }
        };

        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.title_folder_selection);
        builder.setPositiveButton("应用", dialogClickListener)
               .setNegativeButton("取消", dialogClickListener);


        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View layout = inflater.inflate(R.layout.dialog_folder_list, null);
        builder.setView(layout);
        //Button cancelBtn = (Button)layout.findViewById(R.id.folderlist_cancel);

        final AlertDialog dlg = builder.create();
        dlg.show();
/*        Button applyBtn = (Button) layout.findViewById(R.id.folderlist_apply);
        applyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, " Apply button clicked");
                dlg.cancel();

                int n = bucketList.size();
                Set<String> folders = new HashSet<String>();
                for(int idx=0; idx<n; idx++) {
                    Map<String, Object> item = (Map<String, Object>) bucketList.get(idx);
                    Boolean bChecked = (Boolean) item.get(new String("checked"));
                    Log.i(TAG, " bucket = " + item.get(new String("bucket")) + " checked = " + bChecked);
                    if(bChecked == true) {
                        String bucket = (String) item.get(new String("bucket"));
                        folders.add(bucket);
                    }
                }

                Log.i(TAG, " folders.filter.sets = " + folders.toString());
                SharedPreferences.Editor editor = mPref.edit();
                editor.putStringSet("images.bucket.filter", folders);
                editor.commit();

                //listView.
                //mLoader.reset();
                mLoader.getMediaInfo().getImagesInfo(mPref);
                BaseAdapter adapter = (BaseAdapter)listView.getAdapter();
                adapter.getCount();
                adapter.notifyDataSetInvalidated();
                String sInfo = getResources().getString(R.string.images_header_status);
                sInfo = String.format(sInfo, mLoader.getMediaInfo().getImageCount());
                mInfoHeader.setText(sInfo);
            }
        });*/

        final ListView _buddyList = (ListView) layout.findViewById(R.id.dialog_folderlist);
        SimpleAdapter sa = new SimpleAdapter((MainActivity)getActivity(), bucketList, R.layout.item_list_folder,
                new String[] {"icon", "bucket", "checked"},
                new int[] {R.id.foldericon, R.id.foldername, R.id.folderstatus});/* {
                public View getView(final int pos, View convertView, ViewGroup parent) {
                    if(convertView == null) {
                        convertView = View.inflate(getActivity(), R.layout.item_list_folder, null);
                        CheckBox chk = (CheckBox) convertView.findViewById(R.id.folderstatus);
                        //chk.setChecked(true);
                        chk.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Log.i(TAG, " item clicked = " + ((CheckBox)view).isChecked());
                            }
                        });
                    }

                    return super.getView(pos, convertView, parent);
                }
        };*/
        _buddyList.setAdapter(sa);
        _buddyList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                CheckBox chk = (CheckBox) view.findViewById(R.id.folderstatus);
                Map<String, Object> item = (Map<String, Object>)adapterView.getAdapter().getItem(i);
                Log.i(TAG, "folder list item clicked " + i + " checked = " + item.get(new String("checked")));
                chk.setChecked(!chk.isChecked());
                item.put("checked", chk.isChecked());
                //dlg.cancel();
            }
        });
    }

    public void onActivityCreated(Bundle saved) {
        super.onActivityCreated(saved);
        String sInfo = getResources().getString(R.string.images_header_status);

        Log.i(TAG, "Activity = " + getActivity());
        mPref = getActivity().getSharedPreferences("wowyun-mobile", Context.MODE_PRIVATE);
        mLoader.getMediaInfo().getImagesInfo(mPref);

        sInfo = String.format(sInfo, mLoader.getMediaInfo().getImageCount());

        Button btn = (Button) getActivity().findViewById(R.id.image_phone_button);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //mLoader.getMediaInfo().getBucketList(true);
                showFolderListDialog();
            }
        });

        listView = (GridView) getActivity().findViewById(R.id.imagegridview);
        listView.setChoiceMode(GridView.CHOICE_MODE_MULTIPLE_MODAL);
        listView.setMultiChoiceModeListener((MainActivity)getActivity());
        mInfoHeader = (TextView) getActivity().findViewById(R.id.imageview_infoheader);
        mInfoHeader.setText(sInfo);

        ((GridView) listView).setAdapter(new ImageAdapter());
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Log.i(TAG, "Item Click " + i);
                if(mSelectMode == false) {
                    String path = mLoader.getMediaInfo().getImageUri(i);
                    Intent intent = new Intent(getActivity(), ImageViewer.class);
                    intent.putExtra("position", i);
                    startActivity(intent);
                } else {
                    //ImageView tag = (ImageView)view.findViewById(R.id.upload_tag);
                    //tag.setVisibility(View.VISIBLE);
                    view.setSelected(true);

                }
            }
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                Log.i(TAG, "OnItemLongClick position = " + i );
                //getActivity().getActionBar().setCustomView(R.layout.action_longpress_overlay);
                //createActionDialog();
                mSelectMode = true;
                view.setSelected(true);
                view.setBackground(getResources().getDrawable(R.drawable.ic_selection_tag));
                return true;
            }
        });

    }

    public void onCreate(Bundle saved) {
        WowYunApp _app = (WowYunApp) getActivity().getApplication();
        super.onCreate(saved);

        mLoader = new MediaLoader(getActivity());
        _app.setMediaInfo(mLoader.getMediaInfo());
        mLoader.setup();
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
       return inflater.inflate(R.layout.imagefragment, container, false);
    }

    public class ImageAdapter extends BaseAdapter {
        WowYunApp.UploadDataItem[] udItem = null;
        int udSize = 0;

        public int getCount() {
            int n =  mLoader.getMediaInfo().getImageCount();

            //Log.i(TAG, "getCount = " + n);
            if(udItem == null) {
                udItem = new WowYunApp.UploadDataItem[n];
                for (int idx = 0; idx < n; idx++) {
                    udItem[idx] = new WowYunApp.UploadDataItem();
                }
                udSize = n;
            } else {
                if(udSize < n) {
                    for(int idx=0; idx<udSize; idx++)
                        udItem[idx] = null;
                    udItem = null;

                    udItem = new WowYunApp.UploadDataItem[n];
                    for (int idx = 0; idx < n; idx++) {
                        udItem[idx] = new WowYunApp.UploadDataItem();
                    }
                    udSize = n;
                }
            }

            //Log.i(TAG, "obj[4] = " + udItem[4]);
            return n;
        }

        public Object getItem(int position) {
            Object obj = udItem[position];
            //Log.i(TAG, "get Item position = " + position + " obj = " + obj);
            return obj;
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(int pos, View view, ViewGroup parent) {
            GridItem myview;

            WowYunApp.UploadDataItem item = null;
            if(udItem != null) {
                item = udItem[pos];
            }

            MainActivity activity = (MainActivity)getActivity();

            //Log.i(TAG, "pos = " + pos + " view = " + view);
            final ViewHolder holder;
            myview = (GridItem) view;

            if(myview == null) {
                myview = new GridItem(getActivity().getApplicationContext());
                myview.setLayout(R.layout.item_grid_image);

                holder = new ViewHolder();

                holder.imageView = (ImageView ) myview.findViewById(R.id.imagethumb);
                holder.progressBar = (ProgressBar) myview.findViewById(R.id.imageprogress);

                myview.setTag(holder);
            } else {
                holder = (ViewHolder) view.getTag();
                //Log.i(TAG, "holder.progressbar = " + holder.progressBar + " pos = " + pos);
            }

            if(item != null) {
                if (item.uploading) {
                    //Log.i(TAG, " uploading true, item progress " + item.progress);
                    holder.progressBar.setVisibility(View.VISIBLE);
                    holder.progressBar.setProgress(item.progress);
                } else {
                    holder.progressBar.setVisibility(View.GONE);
                }
            }

            mLoader.displayImage(pos, holder.imageView, new SimpleImageLoadingListener() {
                public void onLoadingStarted(String uri, View view) {
                    ViewHolder holder = (ViewHolder) view.getTag();
                }

                public void onLoadingFailed(String uri, View view, FailReason ret) {
                }

                public void onLoadingComplete(String uri, View view, Bitmap loaded) {

                }
            });
            return myview;
        }

    }
}