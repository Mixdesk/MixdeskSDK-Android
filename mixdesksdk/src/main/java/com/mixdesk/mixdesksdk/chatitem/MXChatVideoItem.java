package com.mixdesk.mixdesksdk.chatitem;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import com.mixdesk.mixdesksdk.R;
import com.mixdesk.mixdesksdk.imageloader.MXImage;
import com.mixdesk.mixdesksdk.model.VideoMessage;
import com.mixdesk.mixdesksdk.util.MXUtils;
import com.mixdesk.mixdesksdk.widget.MXBaseCustomCompositeView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;

public class MXChatVideoItem extends MXBaseCustomCompositeView {

    private ImageView picIv;
    private com.mixdesk.mixdesksdk.widget.CircularProgressBar progressBar;
    private boolean isDownloading = false;
    protected int mImageWidth;
    protected int mImageHeight;
    private boolean isLocalPath = false;

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
        progressBar = findViewById(R.id.progress_bar);
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
        String url = videoMessage.getUrl();
        String localPathUrl = videoMessage.getLocalPath();
        if(localPathUrl != null){
            isLocalPath = true;
        }
        if (url == null && localPathUrl == null) {
            return;
        }
        if (!isLocalPath) {
            File cacheFile = getCacheFile(url);
            if (cacheFile.exists()) {
                progressBar.setVisibility(GONE);
            } else {
                progressBar.setVisibility(VISIBLE);
                progressBar.setProgress(0);
            }
        } else {
            progressBar.setVisibility(GONE);
        }

        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if (isLocalPath) {
                    String url = videoMessage.getLocalPath();
                    File file = new File(url);
                    playVideo(file);
                    return;
                }
                String url = videoMessage.getUrl();
                File cacheFile = getCacheFile(url);
                if (cacheFile.exists()) {
                    playVideo(cacheFile);
                } else if (!isDownloading) {
                    isDownloading = true;
                    if (progressBar != null) {
                        progressBar.setVisibility(VISIBLE);
                        progressBar.setProgress(3); // 立即显示一点进度
                    }
                    new DownloadVideoTask(url, cacheFile).execute();
                }
            }
        });
    }

    private File getCacheFile(String url) {
        String fileName = md5(url) + ".mp4";
        File cacheDir = getContext().getCacheDir();
        return new File(cacheDir, fileName);
    }

    private void playVideo(File file) {
        try {
            Uri uri;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                uri = FileProvider.getUriForFile(getContext(), getContext().getPackageName() + ".fileProvider", file);
            } else {
                uri = Uri.fromFile(file);
            }
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, "video/mp4");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            getContext().startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(getContext(), R.string.mx_title_unknown_error, Toast.LENGTH_SHORT).show();
        }
    }

    private static String md5(String s) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            byte[] bytes = digest.digest(s.getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            return String.valueOf(s.hashCode());
        }
    }

    private class DownloadVideoTask extends AsyncTask<Void, Integer, Boolean> {
        private String url;
        private File file;

        DownloadVideoTask(String url, File file) {
            this.url = url;
            this.file = file;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                URL u = new URL(url);
                HttpURLConnection conn = (HttpURLConnection) u.openConnection();
                conn.setConnectTimeout(20000);
                conn.setReadTimeout(600000);
                conn.connect();
                if (conn.getResponseCode() != 200) return false;
                int total = conn.getContentLength();
                InputStream is = conn.getInputStream();
                // 临时文件
                File tempFile = new File(file.getAbsolutePath() + ".downloading");
                if (tempFile.exists()) tempFile.delete();
                FileOutputStream fos = new FileOutputStream(tempFile);
                byte[] buffer = new byte[4096];
                int len;
                int downloaded = 0;
                int lastProgress = 0;
                while ((len = is.read(buffer)) != -1) {
                    fos.write(buffer, 0, len);
                    downloaded += len;
                    if (total > 0) {
                        int progress = (int) (downloaded * 100f / total);
                        if (progress != lastProgress) {
                            android.util.Log.d("MXChatVideoItem", "downloaded=" + downloaded + ", total=" + total + ", progress=" + progress);
                            publishProgress(progress);
                            lastProgress = progress;
                        }
                    }
                }
                fos.close();
                is.close();
                // 下载完成后重命名
                if (!tempFile.renameTo(file)) {
                    tempFile.delete();
                    return false;
                }
                if (total > 0) {
                    publishProgress(100);
                }
                return true;
            } catch (Exception e) {
                // 出错时清理临时文件
                File tempFile = new File(file.getAbsolutePath() + ".downloading");
                if (tempFile.exists()) tempFile.delete();
                if (file.exists()) file.delete();
                android.util.Log.e("MXChatVideoItem", "download error", e);
                return false;
            }
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            int progress = values[0];
            if (progress < 3) progress = 3;
            if (progressBar != null) {
                progressBar.setProgress(progress);
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            isDownloading = false;
            if (progressBar != null) {
                progressBar.setVisibility(GONE);
            }
            if (success) {
                playVideo(file);
            } else {
                Toast.makeText(getContext(), R.string.mx_download_error, Toast.LENGTH_SHORT).show();
            }
        }
    }
}

