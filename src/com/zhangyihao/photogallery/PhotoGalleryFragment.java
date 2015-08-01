package com.zhangyihao.photogallery;

import java.util.List;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.zhangyihao.photogallery.entity.GalleryItem;
import com.zhangyihao.photogallery.util.FlickrFetchr;

public class PhotoGalleryFragment extends Fragment {

	private GridView mGridView;
	private List<GalleryItem> mItems;
	private ThumbnailDownloader<ImageView> mThumbnailDownloader;
	
	private static final String TAG = "PhotoGalleryFragment";
	

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
		new FetchItemsTask().execute();
		
		mThumbnailDownloader = new ThumbnailDownloader<ImageView>(new Handler());
		mThumbnailDownloader.setListener(new ThumbnailDownloader.Listener<ImageView>() {

			@Override
			public void onThumbnailDownloaded(ImageView token, Bitmap thumbnail) {
				if(isVisible()) {
					token.setImageBitmap(thumbnail);
				}
			}
			
		});
		mThumbnailDownloader.start();
		mThumbnailDownloader.getLooper();
		Log.i(TAG, "Backagroun thread started");
	}

	@Override
	@Nullable
	public View onCreateView(LayoutInflater inflater,
			@Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_photo_gallery, container, false);
		mGridView = (GridView)view.findViewById(R.id.gridView);
		setupAdapter();
		return view;
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		mThumbnailDownloader.quit();
		Log.i(TAG, "Backagroun thread destroyed");
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		mThumbnailDownloader.clearQueue();
	}

	private void setupAdapter() {
		if(getActivity()==null || mGridView==null) {
			return;
		}
		if(mItems!=null) {
			mGridView.setAdapter(new GalleryItemAdapter(mItems));
		} else {
			mGridView.setAdapter(null);
		}
	}
	
	private class FetchItemsTask extends AsyncTask<Void, Void, List<GalleryItem>> {

		@Override
		protected List<GalleryItem> doInBackground(Void... params) {
			return new FlickrFetchr().fetchItems();
		}

		@Override
		protected void onPostExecute(List<GalleryItem> result) {
			mItems = result;
			setupAdapter();
		}
	}
	
	private class GalleryItemAdapter extends ArrayAdapter<GalleryItem> {

		public GalleryItemAdapter(List<GalleryItem> items) {
			super(getActivity(), 0, items);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if(convertView==null) {
				convertView = getActivity().getLayoutInflater().inflate(R.layout.photo_gallery_item, parent, false);
			}
			ImageView imageView = (ImageView)convertView.findViewById(R.id.gallery_item_imageView);
			TextView textView = (TextView)convertView.findViewById(R.id.gallery_item_title);
//			imageView.setImageResource(R.drawable.brian_up_close);
			
			GalleryItem item = getItem(position);
			textView.setText(item.getCaption());
			mThumbnailDownloader.queueThumbnail(imageView, item.getUrl());
			return convertView;
		}
	}
	
}
