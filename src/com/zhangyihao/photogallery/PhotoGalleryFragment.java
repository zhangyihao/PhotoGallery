package com.zhangyihao.photogallery;

import java.io.IOException;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import com.zhangyihao.photogallery.util.FilckrFetchr;

public class PhotoGalleryFragment extends Fragment {

	private GridView mGridView;
	
	private final String TAG = "PhotoGalleryFragment";

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
		new FetchItemsTask().execute();
	}

	@Override
	@Nullable
	public View onCreateView(LayoutInflater inflater,
			@Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_photo_gallery, container, false);
		mGridView = (GridView)view.findViewById(R.id.gridView);
		return view;
	}
	
	private class FetchItemsTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			String result;
			try {
				result = new FilckrFetchr().getUrl("http://www.baidu.com");
				Log.i(TAG, "Fetched contents of URL:"+result);
			} catch (Exception e) {
				Log.e(TAG, "Failed to fetched contents of URL", e);
			}
			return null;
		}
		
	}
	
}
