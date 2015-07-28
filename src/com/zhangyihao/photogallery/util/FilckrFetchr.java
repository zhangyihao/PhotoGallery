package com.zhangyihao.photogallery.util;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class FilckrFetchr {
	byte[] getUrlBytes(String urlSpec) throws Exception {
		URL url = new URL(urlSpec);
		HttpURLConnection connection = (HttpURLConnection)url.openConnection();
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			InputStream in = connection.getInputStream();
			
			if(connection.getResponseCode()!=HttpURLConnection.HTTP_OK) {
				return null;
			}
			int bytesRead = 0;
			byte[] buffer = new byte[1024];
			while((bytesRead=in.read(buffer))>0) {
				out.write(buffer, 0, bytesRead);
			}
			out.close();
			return out.toByteArray();
			
		} finally {
			
		}
	}
	
	public String getUrl(String urlSpec) throws Exception {
		return new String(getUrlBytes(urlSpec));
	}
}
