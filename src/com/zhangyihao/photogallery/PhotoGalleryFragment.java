package com.zhangyihao.photogallery;

import java.util.ArrayList;
import java.util.List;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.SearchView;
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
		setHasOptionsMenu(true);
		updatItems();
		
//		Intent i = new Intent(getActivity(), PollService.class);
//		getActivity().startService(i);
//		PollService.setServiceAlarm(getActivity(), true);
		
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

	public void updatItems() {
		new FetchItemsTask().execute();
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
	
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.fragment_photo_gallery, menu);
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			//首先找到SearchView
			MenuItem searchItem = menu.findItem(R.id.menu_item_search);
			SearchView searchView = (SearchView)searchItem.getActionView(); //获取到操作视图
			//通过SearchManager获取搜索信息，并显示搜索界面
			SearchManager searchManage = (SearchManager)getActivity().getSystemService(Context.SEARCH_SERVICE);
			ComponentName name = getActivity().getComponentName();
			SearchableInfo searchInfo = searchManage.getSearchableInfo(name);
			//将搜索相关信息通知SearchView
			searchView.setSearchableInfo(searchInfo);
		}
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_item_search:
			getActivity().onSearchRequested();
			return true;
		case R.id.menu_item_clear:
			PreferenceManager.getDefaultSharedPreferences(getActivity())
				.edit()
				.putString(FlickrFetchr.PREF_SEARCH_QUERY, null)
				.commit();
			updatItems();
			return true;
		case R.id.menu_item_toggle_polling:
			boolean shouldStartAlarm = !PollService.isServiceAlarmOn(getActivity());
			PollService.setServiceAlarm(getActivity(), shouldStartAlarm);
			if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.HONEYCOMB) {
				getActivity().invalidateOptionsMenu();
			}
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
		MenuItem toggleItem = menu.findItem(R.id.menu_item_toggle_polling);
		if(PollService.isServiceAlarmOn(getActivity())) {
			toggleItem.setTitle(R.string.stop_polling);
		} else {
			toggleItem.setTitle(R.string.start_polling);
		}
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
			Activity activity = getActivity();
			if(null == activity ){
				return new ArrayList<GalleryItem>();
			}
			String query = PreferenceManager.getDefaultSharedPreferences(activity)
					.getString(FlickrFetchr.PREF_SEARCH_QUERY, null);
			if(null != query && !"".equals(query)) {
				return new FlickrFetchr().queryItems(query);
			} else {
				return new FlickrFetchr().fetchItems();
			}
			
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
