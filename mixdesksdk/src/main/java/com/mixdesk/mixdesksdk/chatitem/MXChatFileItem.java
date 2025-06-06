package com.mixdesk.mixdesksdk.chatitem;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.StrictMode;
import androidx.core.content.FileProvider;
import android.text.format.Formatter;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.mixdesk.mixdesksdk.R;
import com.mixdesk.mixdesksdk.callback.OnDownloadFileCallback;
import com.mixdesk.mixdesksdk.model.FileMessage;
import com.mixdesk.mixdesksdk.util.ErrorCode;
import com.mixdesk.mixdesksdk.util.MXConfig;
import com.mixdesk.mixdesksdk.util.MXTimeUtils;
import com.mixdesk.mixdesksdk.util.MXUtils;
import com.mixdesk.mixdesksdk.widget.CircularProgressBar;
import com.mixdesk.mixdesksdk.widget.MXBaseCustomCompositeView;

import org.json.JSONObject;

import java.io.File;

/**
 * OnePiece
 * Created by xukq on 3/30/16.
 */
public class MXChatFileItem extends MXBaseCustomCompositeView implements View.OnTouchListener {

    private CircularProgressBar mProgressBar;
    private TextView mTitleTv;
    private TextView mSubTitleTv;
    private View mRightIv;
    private View root;
    private FileMessage mFileMessage;
    private Callback mCallback;

    private boolean isCancel;

    public MXChatFileItem(Context context) {
        super(context);
    }

    public MXChatFileItem(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MXChatFileItem(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.mx_item_file_layout;
    }

    @Override
    protected void initView() {
        root = findViewById(R.id.root);
        mProgressBar = findViewById(R.id.progressbar);
        mTitleTv = findViewById(R.id.mx_file_title_tv);
        mSubTitleTv = findViewById(R.id.mx_file_sub_title_tv);
        mRightIv = findViewById(R.id.mx_right_iv);
    }

    @Override
    protected void setListener() {
        root.setOnClickListener(this);
        mRightIv.setOnClickListener(this);
        mProgressBar.setOnTouchListener(this);
    }

    @Override
    protected void processLogic() {
    }

    @Override
    public void onClick(View view) {
        if (mFileMessage == null) {
            return;
        }

        int id = view.getId();
        if (id == R.id.mx_right_iv) {
            root.performClick();
        } else if (id == R.id.progressbar) {
            cancelDownloading();
        } else if (id == R.id.root) {
            switch (mFileMessage.getFileState()) {
                case FileMessage.FILE_STATE_NOT_EXIST:
                    isCancel = false;
                    mFileMessage.setFileState(FileMessage.FILE_STATE_DOWNLOADING);
                    downloadingState();
                    MXConfig.getController(getContext()).downloadFile(mFileMessage, new OnDownloadFileCallback() {
                        @Override
                        public void onSuccess(File file) {
                            // 取消请求到真正取消请求有一个延迟
                            if (isCancel) {
                                return;
                            }
                            mFileMessage.setFileState(FileMessage.FILE_STATE_FINISH);
                            mCallback.notifyDataSetChanged();
                        }

                        @Override
                        public void onProgress(int progress) {
                            mFileMessage.setProgress(progress);
                            mCallback.notifyDataSetChanged();
                        }

                        @Override
                        public void onFailure(int code, String message) {
                            if (code == ErrorCode.DOWNLOAD_IS_CANCEL) {
                                // 取消下载 do nothing
                                return;
                            }

                            mFileMessage.setFileState(FileMessage.FILE_STATE_FAILED);
                            downloadFailedState();
                            // 下载失败，删除文件
                            cancelDownloading();
                            mCallback.onFileMessageDownloadFailure(mFileMessage, code, message);
                        }
                    });
                    break;
                case FileMessage.FILE_STATE_FINISH:
                    openFile();
                    break;
                case FileMessage.FILE_STATE_FAILED:
                    mFileMessage.setFileState(FileMessage.FILE_STATE_NOT_EXIST);
                    root.performClick();
                    break;
                case FileMessage.FILE_STATE_EXPIRED:
                    mCallback.onFileMessageExpired(mFileMessage);
                    break;
            }
        }
    }

    public void initFileItem(Callback callback, FileMessage fileMessage) {
        mCallback = callback;
        mFileMessage = fileMessage;
        downloadInitState();
    }

    private void openFile() {
        // 置入一个不设防的 VmPolicy
        Uri uri;
        File file = new File(MXUtils.getFileMessageFilePath(mFileMessage));
        if (Build.VERSION.SDK_INT >= 24) {
            StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
            StrictMode.setVmPolicy(builder.build());
        }
        // Android Q 使用 FileProvider 的方式
        if (Build.VERSION.SDK_INT >= 29) {
            uri = FileProvider.getUriForFile(getContext(), getContext().getPackageName() + ".fileProvider", file);
        } else {
            uri = Uri.fromFile(new File(MXUtils.getFileMessageFilePath(mFileMessage)));
        }
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, MXUtils.getMIMEType(file));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            getContext().startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(getContext(), R.string.mx_no_app_open_file, Toast.LENGTH_SHORT).show();
        }
    }

    private void cancelDownloading() {
        isCancel = true;
        mFileMessage.setFileState(FileMessage.FILE_STATE_NOT_EXIST);
        MXConfig.getController(getContext()).cancelDownload(mFileMessage.getUrl());
        String filePath = MXUtils.getFileMessageFilePath(mFileMessage);
        MXUtils.delFile(filePath);
        mCallback.notifyDataSetChanged();
    }

    public void setProgress(int progress) {
        mProgressBar.setProgress(progress);
    }

    public void downloadInitState() {
        mProgressBar.setProgress(0);
        mProgressBar.setVisibility(GONE);
        displayFileInfo();
    }

    public void downloadSuccessState() {
        displayFileInfo();
        mProgressBar.setVisibility(GONE);
        setProgress(100);
        mRightIv.setVisibility(GONE);
    }

    public void downloadFailedState() {
        mProgressBar.setVisibility(GONE);
    }

    public void downloadingState() {
        mSubTitleTv.setText(String.format("%s%s", getSubTitlePrefix(), getResources().getString(R.string.mx_downloading)));
        mRightIv.setVisibility(GONE);
        mProgressBar.setVisibility(VISIBLE);
    }

    private void displayFileInfo() {
        mTitleTv.setText(getExtraStringValue("filename"));
        String endStr;
        String filePath = MXUtils.getFileMessageFilePath(mFileMessage);
        boolean isFileExist = MXUtils.isFileExist(filePath);
        if (isFileExist) {
            endStr = getResources().getString(R.string.mx_download_complete);
            mRightIv.setVisibility(GONE);
        } else {
            String expire_at = getExtraStringValue("expire_at");
            long expireTimeLong = MXTimeUtils.parseTimeToLong(expire_at);
            long diffTime = expireTimeLong - System.currentTimeMillis();
            if (diffTime <= 0) {
                endStr = getResources().getString(R.string.mx_expired);
                mRightIv.setVisibility(GONE);
                mFileMessage.setFileState(FileMessage.FILE_STATE_EXPIRED);
            } else {
                float leaveHours = diffTime / 3600000f;
                String leaveHoursStr = new java.text.DecimalFormat("#.0").format(leaveHours);
                endStr = getContext().getString(R.string.mx_expire_after, leaveHoursStr);
                mRightIv.setVisibility(VISIBLE);
            }
        }
        String subTitle = getSubTitlePrefix() + endStr;
        mSubTitleTv.setText(subTitle);
        mProgressBar.setVisibility(GONE);
    }

    private String getExtraStringValue(String key) {
        String value = "";
        try {
            JSONObject extraJsonObj = new JSONObject(mFileMessage.getExtra());
            value = extraJsonObj.optString(key);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return value;
    }

    private String getSubTitlePrefix() {
        String size = Formatter.formatShortFileSize(getContext(), getExtraLongValue("size"));
        return size + " · ";
    }

    private long getExtraLongValue(String key) {
        long value = 0;
        try {
            JSONObject extraJsonObj = new JSONObject(mFileMessage.getExtra());
            value = extraJsonObj.optLong(key);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return value;
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        if (MotionEvent.ACTION_DOWN == motionEvent.getAction()) {
            cancelDownloading();
        }
        return false;
    }

    public interface Callback {
        void notifyDataSetChanged();

        void onFileMessageDownloadFailure(FileMessage fileMessage, int code, String message);

        void onFileMessageExpired(FileMessage fileMessage);
    }
}
