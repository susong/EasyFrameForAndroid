package com.dream.photoselector.ui;
/**
 * 
 * @author Aizaz AZ
 *
 */

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.dream.photoselector.R;
import com.dream.photoselector.model.PhotoModel;
import com.dream.photoselector.polites.GestureImageView;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

public class PhotoPreview extends LinearLayout implements OnClickListener {

	private ProgressBar pbLoading;
	private GestureImageView ivContent;
	private OnClickListener l;

	public PhotoPreview(Context context) {
		super(context);
		LayoutInflater.from(context).inflate(R.layout.ps_view_photo_preview, this, true);

		pbLoading = (ProgressBar) findViewById(R.id.pb_loading_ps);
		ivContent = (GestureImageView) findViewById(R.id.iv_content_ps);
		ivContent.setOnClickListener(this);
	}

	public PhotoPreview(Context context, AttributeSet attrs, int defStyle) {
		this(context);
	}

	public PhotoPreview(Context context, AttributeSet attrs) {
		this(context);
	}

	public void loadImage(PhotoModel photoModel) {
		loadImage("file://" + photoModel.getOriginalPath());
	}

	private void loadImage(String path) {
		ImageLoader.getInstance().loadImage(path, new SimpleImageLoadingListener() {
			@Override
			public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
				ivContent.setImageBitmap(loadedImage);
				pbLoading.setVisibility(View.GONE);
			}

			@Override
			public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
				ivContent.setImageDrawable(getResources().getDrawable(R.drawable.ps_picture_loadfailed));
				pbLoading.setVisibility(View.GONE);
			}
		});
	}

	@Override
	public void setOnClickListener(OnClickListener l) {
		this.l = l;
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.iv_content_ps && l != null)
			l.onClick(ivContent);
	};

}
