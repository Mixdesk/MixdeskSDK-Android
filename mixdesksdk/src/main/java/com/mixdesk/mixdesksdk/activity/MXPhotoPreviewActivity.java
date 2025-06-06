package com.mixdesk.mixdesksdk.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.viewpager.widget.PagerAdapter;
import androidx.core.view.ViewCompat;
import androidx.viewpager.widget.ViewPager;
import androidx.core.view.ViewPropertyAnimatorListenerAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mixdesk.mixdesksdk.R;
import com.mixdesk.mixdesksdk.imageloader.MXImage;
import com.mixdesk.mixdesksdk.imageloader.MXImageLoader;
import com.mixdesk.mixdesksdk.third.photoview.PhotoViewAttacher;
import com.mixdesk.mixdesksdk.util.MXAsyncTask;
import com.mixdesk.mixdesksdk.util.MXBrowserPhotoViewAttacher;
import com.mixdesk.mixdesksdk.util.MXSavePhotoTask;
import com.mixdesk.mixdesksdk.util.MXUtils;
import com.mixdesk.mixdesksdk.widget.MXHackyViewPager;
import com.mixdesk.mixdesksdk.widget.MXImageView;

import java.io.File;
import java.util.ArrayList;


public class MXPhotoPreviewActivity extends Activity implements PhotoViewAttacher.OnViewTapListener, View.OnClickListener, MXAsyncTask.Callback<Void> {
    private static final String EXTRA_SAVE_IMG_DIR = "EXTRA_SAVE_IMG_DIR";
    private static final String EXTRA_PREVIEW_IMAGES = "EXTRA_PREVIEW_IMAGES";
    private static final String EXTRA_CURRENT_POSITION = "EXTRA_CURRENT_POSITION";
    private static final String EXTRA_IS_SINGLE_PREVIEW = "EXTRA_IS_SINGLE_PREVIEW";
    private static final String EXTRA_PHOTO_PATH = "EXTRA_PHOTO_PATH";


    private RelativeLayout mTitleRl;
    private TextView mTitleTv;
    private ImageView mDownloadIv;
    private MXHackyViewPager mContentHvp;

    private ArrayList<String> mPreviewImages;
    private boolean mIsSinglePreview;

    private File mSaveImgDir;

    private boolean mIsHidden = false;
    private MXSavePhotoTask mSavePhotoTask;

    /**
     * 上一次标题栏显示或隐藏的时间戳
     */
    private long mLastShowHiddenTime;

    /**
     * 获取查看图片的intent
     *
     * @param context
     * @param saveImgDir      保存图片的目录，如果传null，则没有保存图片功能
     * @param previewImages   当前预览的图片目录里的图片路径集合
     * @param currentPosition 当前预览图片的位置
     * @return
     */
    public static Intent newIntent(Context context, File saveImgDir, ArrayList<String> previewImages, int currentPosition) {
        Intent intent = new Intent(context, MXPhotoPreviewActivity.class);
        intent.putExtra(EXTRA_SAVE_IMG_DIR, saveImgDir);
        intent.putStringArrayListExtra(EXTRA_PREVIEW_IMAGES, previewImages);
        intent.putExtra(EXTRA_CURRENT_POSITION, currentPosition);
        intent.putExtra(EXTRA_IS_SINGLE_PREVIEW, false);
        return intent;
    }

    /**
     * 获取查看图片的intent
     *
     * @param context
     * @param saveImgDir 保存图片的目录，如果传null，则没有保存图片功能
     * @param photoPath  图片路径
     * @return
     */
    public static Intent newIntent(Context context, File saveImgDir, String photoPath) {
        Intent intent = new Intent(context, MXPhotoPreviewActivity.class);
        intent.putExtra(EXTRA_SAVE_IMG_DIR, saveImgDir);
        intent.putExtra(EXTRA_PHOTO_PATH, photoPath);
        intent.putExtra(EXTRA_CURRENT_POSITION, 0);
        intent.putExtra(EXTRA_IS_SINGLE_PREVIEW, true);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MXUtils.updateLanguage(this);
        initView();
        initListener();
        processLogic(savedInstanceState);
    }

    private void initView() {
        setContentView(R.layout.mx_activity_photo_preview);
        mTitleRl = findViewById(R.id.title_rl);
        mTitleTv = findViewById(R.id.title_tv);
        mDownloadIv = findViewById(R.id.download_iv);
        mContentHvp = findViewById(R.id.content_hvp);
    }

    private void initListener() {
        findViewById(R.id.back_iv).setOnClickListener(this);
        mDownloadIv.setOnClickListener(this);

        mContentHvp.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                renderTitleTv();
            }
        });
    }

    private void processLogic(Bundle savedInstanceState) {
        // 被回收，就直接退出
        if (getIntent() == null) {
            finish();
        }

        mSaveImgDir = (File) getIntent().getSerializableExtra(EXTRA_SAVE_IMG_DIR);
        if (mSaveImgDir == null) {
            mDownloadIv.setVisibility(View.INVISIBLE);
        }

        mPreviewImages = getIntent().getStringArrayListExtra(EXTRA_PREVIEW_IMAGES);
        if (mPreviewImages == null) {
            mPreviewImages = new ArrayList<>();
        }

        mIsSinglePreview = getIntent().getBooleanExtra(EXTRA_IS_SINGLE_PREVIEW, false);
        if (mIsSinglePreview) {
            mPreviewImages = new ArrayList<>();
            mPreviewImages.add(getIntent().getStringExtra(EXTRA_PHOTO_PATH));
        }

        int currentPosition = getIntent().getIntExtra(EXTRA_CURRENT_POSITION, 0);
        mContentHvp.setAdapter(new ImagePageAdapter());
        mContentHvp.setCurrentItem(currentPosition);


        // 处理第一次进来时指示数字
        renderTitleTv();

        // 过2秒隐藏标题栏
        mTitleRl.postDelayed(new Runnable() {
            @Override
            public void run() {
                hiddenTitlebar();
            }
        }, 2000);
    }

    private void renderTitleTv() {
        if (mIsSinglePreview) {
            mTitleTv.setText(R.string.mx_view_photo);
        } else {
            mTitleTv.setText((mContentHvp.getCurrentItem() + 1) + "/" + mPreviewImages.size());
        }
    }

    @Override
    public void onViewTap(View view, float x, float y) {
        if (System.currentTimeMillis() - mLastShowHiddenTime > 500) {
            mLastShowHiddenTime = System.currentTimeMillis();
            if (mIsHidden) {
                showTitlebar();
            } else {
                hiddenTitlebar();
            }
        }
    }

    private void showTitlebar() {
        ViewCompat.animate(mTitleRl).translationY(0).setInterpolator(new DecelerateInterpolator(2)).setListener(new ViewPropertyAnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(View view) {
                mIsHidden = false;
            }
        }).start();
    }

    private void hiddenTitlebar() {
        ViewCompat.animate(mTitleRl).translationY(-mTitleRl.getHeight()).setInterpolator(new DecelerateInterpolator(2)).setListener(new ViewPropertyAnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(View view) {
                mIsHidden = true;
            }
        }).start();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.back_iv) {
            onBackPressed();
        } else if (v.getId() == R.id.download_iv) {
            if (mSavePhotoTask == null) {
                savePic();
            }
        }
    }

    private synchronized void savePic() {
        if (mSavePhotoTask != null) {
            return;
        }

        final String url = mPreviewImages.get(mContentHvp.getCurrentItem());
        File file;
        if (url.startsWith("file")) {
            file = new File(url.replace("file://", ""));
            if (file.exists()) {
                MXUtils.showSafe(MXPhotoPreviewActivity.this, getString(R.string.mx_save_img_success_folder, file.getParentFile().getAbsolutePath()));
                return;
            }
        }

        // 通过MD5加密url生成文件名，避免多次保存同一张图片
        final String fileName = MXUtils.stringToMD5(url) + ".png";
        file = new File(mSaveImgDir, fileName);
        if (file.exists()) {
            MXUtils.showSafe(this, getString(R.string.mx_save_img_success_folder, mSaveImgDir.getAbsolutePath()));
            return;
        }

        mSavePhotoTask = new MXSavePhotoTask(this, this, file);
        MXImage.downloadImage(this, url, new MXImageLoader.MQDownloadImageListener() {
            @Override
            public void onSuccess(String url, Bitmap bitmap) {
                if (mSavePhotoTask != null) {
                    mSavePhotoTask.setBitmapAndPerform(bitmap);
                }
            }

            @Override
            public void onFailed(String url) {
                mSavePhotoTask = null;
                MXUtils.showSafe(MXPhotoPreviewActivity.this, R.string.mx_save_img_failure);
            }
        });
    }

    @Override
    public void onPostExecute(Void aVoid) {
        mSavePhotoTask = null;
    }

    @Override
    public void onTaskCancelled() {
        mSavePhotoTask = null;
    }

    @Override
    protected void onDestroy() {
        if (mSavePhotoTask != null) {
            mSavePhotoTask.cancelTask();
            mSavePhotoTask = null;
        }
        super.onDestroy();
    }

    private class ImagePageAdapter extends PagerAdapter {

        @Override
        public int getCount() {
            return mPreviewImages.size();
        }

        @Override
        public View instantiateItem(ViewGroup container, int position) {
            final MXImageView imageView = new MXImageView(container.getContext());
            container.addView(imageView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            final MXBrowserPhotoViewAttacher photoViewAttacher = new MXBrowserPhotoViewAttacher(imageView);
            photoViewAttacher.setOnViewTapListener(MXPhotoPreviewActivity.this);

            imageView.setDrawableChangedCallback(new MXImageView.OnDrawableChangedCallback() {
                @Override
                public void onDrawableChanged(Drawable drawable) {
                    if (drawable != null && drawable.getIntrinsicHeight() > drawable.getIntrinsicWidth() && drawable.getIntrinsicHeight() > MXUtils.getScreenHeight(imageView.getContext())) {
                        photoViewAttacher.setIsSetTopCrop(true);
                        photoViewAttacher.setUpdateBaseMatrix();
                    } else {
                        photoViewAttacher.update();
                    }
                }
            });

            MXImage.displayImage(MXPhotoPreviewActivity.this, imageView, mPreviewImages.get(position), R.drawable.mx_ic_holder_dark, R.drawable.mx_ic_holder_dark, MXUtils.getScreenWidth(MXPhotoPreviewActivity.this), MXUtils.getScreenHeight(MXPhotoPreviewActivity.this), null);

            return imageView;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }
    }
}
