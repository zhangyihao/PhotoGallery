package com.zhangyihao.photogallery;

import java.util.List;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;

import com.zhangyihao.photogallery.entity.GalleryItem;
import com.zhangyihao.photogallery.util.FlickrFetchr;

public class PollService extends IntentService {
	
	private static final String TAG = "PollService";
	private static final int POLL_INTERVAL = 1000*60*5;

	public PollService() {
		super(TAG);
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
		//旧版本中getBackgroundDataSetting()返回false，表示不允许使用后台数据
		//Android4.0（IceCream Sandwich）中，后台数据设置会禁用网络，须检查网络连接
		boolean isNetWorkAvailable = cm.getBackgroundDataSetting() && cm.getActiveNetworkInfo()!=null;
		if(!isNetWorkAvailable) {
			return;
		}
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		String query = prefs.getString(FlickrFetchr.PREF_SEARCH_QUERY, null);
		String lastResultId = prefs.getString(FlickrFetchr.PREF_SEARCH_RESULT_ID, null);
		
		List<GalleryItem> items;
		if(query!=null) {
			items = new FlickrFetchr().queryItems(query);
		} else {
			items = new FlickrFetchr().fetchItems();
		}
		
		if(items.size()==0) {
			return;
		}
		String resultId = items.get(0).getId();
		if(!resultId.equals(lastResultId)) {
			Resources r = getResources();
			PendingIntent pi = PendingIntent.getActivity(this, 0,
					new Intent(this, PhotoGalleryActivity.class),  0);
			Notification notification = new NotificationCompat.Builder(this)
				.setTicker(r.getString(R.string.new_pictures_title))
				.setSmallIcon(android.R.drawable.ic_menu_report_image)
				.setContentTitle(r.getString(R.string.new_pictures_title))
				.setContentText(r.getString(R.string.new_pictures_text))
				.setContentIntent(pi)
				.setAutoCancel(true).build();
			
			NotificationManager notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
			notificationManager.notify(0, notification);
		} else {
			
		}
		
		prefs.edit().putString(FlickrFetchr.PREF_SEARCH_RESULT_ID, resultId).commit();
	}

	public static void setServiceAlarm(Context context, boolean isOn) {
		Intent i = new Intent(context, PollService.class);
		//创建一个用来启动PollService的PendingIntent
		PendingIntent pi = PendingIntent.getService(context, 0, i, 0);
		
		AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
		if(isOn) {
			alarmManager.setRepeating(AlarmManager.RTC, System.currentTimeMillis(), POLL_INTERVAL, pi);
		} else {
			alarmManager.cancel(pi);
			pi.cancel();
		}
	}
	
	public static boolean isServiceAlarmOn(Context context) {
		Intent i = new Intent(context, PollService.class);
		PendingIntent pi = PendingIntent.getService(context, 0, i, PendingIntent.FLAG_NO_CREATE);
		return pi!=null;
	}
	
}
