package com.wowfly.android.core;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Checkable;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.wowfly.wowyun.wowyun_mobile.R;

import java.text.AttributedCharacterIterator;

/**
 * Created by user on 8/20/14.
 */
public class GridItem extends RelativeLayout implements Checkable {
    private Context mContext;
    private boolean mChecked;
    private ImageView mImageView = null, mSelectView = null;

    public  GridItem(Context context) {
        this(context, null, 0);
    }

    public GridItem(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GridItem(Context context, AttributeSet attrs, int style) {
        super(context, attrs, style);

        mContext = context;
/*        LayoutInflater.from(context).inflate(R.layout.item_grid_image, this);
        mImageView = (ImageView) findViewById(R.id.imagethumb);
        mSelectView = (ImageView) findViewById(R.id.upload_tag);*/
    }

    public void setLayout(int layout) {
        LayoutInflater.from(mContext).inflate(layout, this);
        mSelectView = (ImageView) findViewById(R.id.upload_tag);
/*        mImageView = (ImageView) findViewById(R.id.imagethumb);
        mSelectView = (ImageView) findViewById(R.id.upload_tag);*/
    }
    public void setChecked(boolean checked) {
        mChecked = checked;
        mSelectView.setVisibility(checked ? View.VISIBLE : View.GONE);
    }

    public boolean isChecked() {
        return mChecked;
    }

    public void toggle() {
        setChecked(!mChecked);
    }

    public void setImageResId(int resid) {
/*        if(mImageView)
            mImageView.setBackground(resid);*/
    }
}
