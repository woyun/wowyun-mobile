package com.wowfly.android.core;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.wowfly.wowyun.wowyun_mobile.MainActivity;
import com.wowfly.wowyun.wowyun_mobile.R;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by user on 8/18/14.
 */
public class VideoViewFragment extends Fragment {
    private static final String TAG = "VideoViewFragment";

    private MediaLoader mLoader = null;
    private AbsListView listView;
    private TextView mInfoHeader;
    private SharedPreferences mPref;

    public static VideoViewFragment newInstance(int idx) {
        VideoViewFragment fragment = new VideoViewFragment();
        return fragment;
    }
    public VideoViewFragment() {

    }

    public GridView getGridView() {
        GridView view = (GridView) listView;

        return view;
    }

    private void showFolderListDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.title_folder_selection);
        final List<Map<String, Object>> bucketList = mLoader.getMediaInfo().getBucketList(false, mPref);

        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View layout = inflater.inflate(R.layout.dialog_folder_list, null);
        builder.setView(layout);

        final AlertDialog dlg = builder.create();
        dlg.show();
        dlg.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
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
                editor.putStringSet("video.bucket.filter", folders);
                editor.commit();

                //mLoader.reset();
                mLoader.getMediaInfo().getVideosInfo(mPref);
                BaseAdapter adapter = (BaseAdapter)listView.getAdapter();
                adapter.getCount();
                adapter.notifyDataSetInvalidated();
                String sInfo = getResources().getString(R.string.videos_header_status);
                sInfo = String.format(sInfo, mLoader.getMediaInfo().getVideoCount());
                mInfoHeader.setText(sInfo);

            }
        });

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

        String sInfo = getResources().getString(R.string.videos_header_status);

        mPref = getActivity().getSharedPreferences("wowyun-mobile", Context.MODE_PRIVATE);
        mLoader.getMediaInfo().getVideosInfo(mPref);
        sInfo = String.format(sInfo, mLoader.getMediaInfo().getVideoCount());
        Log.i(TAG, "sInfo = " + sInfo);

        Button btn = (Button) getActivity().findViewById(R.id.video_phone_button);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //mLoader.getMediaInfo().getBucketList(true);

                showFolderListDialog();
            }
        });

        listView = (GridView) getActivity().findViewById(R.id.videogridview);
        listView.setChoiceMode(GridView.CHOICE_MODE_MULTIPLE_MODAL);
        listView.setMultiChoiceModeListener((MainActivity)getActivity());
        mInfoHeader = (TextView) getActivity().findViewById(R.id.videoview_infoheader);
        mInfoHeader.setText(sInfo);

        ((GridView) listView).setAdapter(new ImageAdapter());
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Log.i(TAG, "Item Click " + i + " play video " + mLoader.getMediaInfo().getVideoPath(i));
                String path = mLoader.getMediaInfo().getVideoUri(i);
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(path));
                intent.setDataAndType(Uri.parse(path), "video/*");

                startActivity(intent);
            }
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                Log.i(TAG, "OnItemLongClick position = " + i );
                //getActivity().getActionBar().setCustomView(R.layout.action_longpress_overlay);
                return true;
            }
        });

    }

    public String getPathFromPosition(int pos) {
        if(mLoader != null) {
            return mLoader.getMediaInfo().getVideoPath(pos);
        }
        return "";
    }

    public void onCreate(Bundle saved) {
        super.onCreate(saved);
        mLoader = new MediaLoader(getActivity());
        mLoader.setup();
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.videofragment, container, false);
    }

    public class ImageAdapter extends BaseAdapter {
        WowYunApp.UploadDataItem[] udItem = null;
        int udSize = 0;

        public int getCount() {
            int n =  mLoader.getMediaInfo().getVideoCount();

            if(udItem == null) {
                udItem = new WowYunApp.UploadDataItem[n];
                for (int idx = 0; idx < n; idx++) {
                    udItem[idx] = new WowYunApp.UploadDataItem();
                    udItem[idx].uploading = false;
                    udItem[idx].progress = 0;
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
            return n;
        }

        public Object getItem(int position) {
            Object obj = udItem[position];
            //Log.i(TAG, "get Item position = " + position + " obj = " + obj);
            return udItem[position];
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(int pos, View view, ViewGroup parent) {
            final ViewHolder holder;
            GridItem myview = (GridItem)view;
            MainActivity activity = (MainActivity)getActivity();

            WowYunApp.UploadDataItem item = null;
            if(udItem != null) {
                if(udItem.length > 0)
                    item = udItem[pos];
            }

            if(myview == null) {
                myview = new GridItem(activity.getApplicationContext());//getActivity().getLayoutInflater().inflate(R.layout.item_grid_video, parent, false);
                myview.setLayout(R.layout.item_grid_video);
                holder = new ViewHolder();

                holder.imageView = (ImageView ) myview.findViewById(R.id.videothumb);
                holder.progressBar = (ProgressBar) myview.findViewById(R.id.videoprogress);
                holder.progressBar.setVisibility(View.GONE);

                myview.setTag(holder);
            } else {
                holder = (ViewHolder) view.getTag();
            }

            //holder.imageView.setImageBitmap(mLoader.getMediaInfo().getVideoBitmap(pos));
/*            TextView text = (TextView) myview.findViewById(R.id.text);
            text.setText(mLoader.getMediaInfo().getImageName(pos));*/
            //myview.setChecked(activity.isInUploadList(pos, false));
            if(item != null) {
                if (item.uploading) {
                    //if(item.progress == 0)
                    holder.progressBar.setVisibility(View.VISIBLE);
                    //Log.i(TAG, "pos = " + pos + " progressbar.vis = "+ holder.progressBar.getVisibility() + " progress = " + item.progress);
                    holder.progressBar.setProgress(item.progress);
                } else {
                    //Log.i(TAG, "progressbar.vis = "+ holder.progressBar.getVisibility() + " progress = " + item.progress);
                    holder.progressBar.setVisibility(View.GONE);
                }
            }

            mLoader.displayVideo(pos, holder.imageView, new SimpleImageLoadingListener() {
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
