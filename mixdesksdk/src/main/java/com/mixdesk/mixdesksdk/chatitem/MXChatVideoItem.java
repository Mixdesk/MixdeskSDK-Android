package com.mixdesk.mixdesksdk.chatitem;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.mixdesk.mixdesksdk.R;
import com.mixdesk.mixdesksdk.imageloader.MXImage;
import com.mixdesk.mixdesksdk.model.VideoMessage;
import com.mixdesk.mixdesksdk.util.MXUtils;
import com.mixdesk.mixdesksdk.widget.MXBaseCustomCompositeView;

public class MXChatVideoItem extends MXBaseCustomCompositeView {

    private ImageView picIv;
    protected int mImageWidth;
    protected int mImageHeight;

    public MXChatVideoItem(Context context) {
        super(context);
    }

    public MXChatVideoItem(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MXChatVideoItem(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.mx_item_video_layout;
    }

    @Override
    protected void initView() {
        picIv = findViewById(R.id.content_pic);
    }

    @Override
    protected void setListener() {

    }

    @Override
    protected void processLogic() {
        int screenWidth = MXUtils.getScreenWidth(getContext());

        mImageWidth = screenWidth / 3;
        mImageHeight = mImageWidth;
    }

    public void setVideoMessage(final VideoMessage videoMessage) {
        MXImage.displayImage((Activity) getContext(), picIv, videoMessage.getThumbUrl(), R.drawable.mx_ic_holder_white, R.drawable.mx_ic_holder_white, mImageWidth, mImageHeight, null);
        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                try {
                    Uri uri = Uri.parse(videoMessage.getUrl());
                    //调用系统自带的播放器
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setDataAndType(uri, "video/mp4");
                    getContext().startActivity(intent);
                } catch (Exception e) {
                    Toast.makeText(getContext(), R.string.mx_title_unknown_error, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

}
