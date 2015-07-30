package com.zhangyihao.photogallery;

import java.util.List;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;

import com.zhangyihao.photogallery.entity.GalleryItem;
import com.zhangyihao.photogallery.util.FilckrFetchr;

public class PhotoGalleryFragment extends Fragment {

	private GridView mGridView;
	private List<GalleryItem> mItems;
	
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
		setupAdapter();
		return view;
	}
	
	private void setupAdapter() {
		if(getActivity()==null || mGridView==null) {
			return;
		}
		if(mItems!=null) {
			mGridView.setAdapter(new ArrayAdapter<>(getActivity(), android.R.layout.simple_gallery_item, mItems));
		} else {
			mGridView.setAdapter(null);
		}
	}
	
	private class FetchItemsTask extends AsyncTask<Void, Void, List<GalleryItem>> {

		@Override
		protected List<GalleryItem> doInBackground(Void... params) {
			return new FilckrFetchr().fetchItems();
		}

		@Override
		protected void onPostExecute(List<GalleryItem> result) {
			mItems = result;
			setupAdapter();
		}
		
	}
	
}
