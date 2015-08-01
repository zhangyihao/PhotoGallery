package com.zhangyihao.photogallery.util;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import android.util.Log;

import com.zhangyihao.photogallery.entity.GalleryItem;

public class FlickrFetchr {
	public static final String TAG = "FlickrFetchr";
	
	private static final String ENDPOINT = "http://www.bababian.com/xmlrpc";
	private static final String API_KEY = "AF1EB81DEA9066C2A8B30458EDA9FE99AK";
	private static final String METHOD_GET_RECENT = "bababian.photo.getRecommendPhoto";
	
	
	public byte[] getUrlBytes(String urlSpec) throws Exception {
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
	
	public List<GalleryItem> fetchItems() {
		List<GalleryItem> items = new ArrayList<GalleryItem>();
		String xml = buildXmlRequestString();
		try {
			byte[] xmlbyte = xml.toString().getBytes("UTF-8");
			URL url = new URL(ENDPOINT);
			HttpURLConnection conn = (HttpURLConnection)url.openConnection();
			conn.setConnectTimeout(5000);
            conn.setDoOutput(true);// 允许输出
            conn.setDoInput(true);
            conn.setUseCaches(false);// 不使用缓存
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Connection", "Keep-Alive");// 维持长连接
            conn.setRequestProperty("Charset", "UTF-8");
            conn.setRequestProperty("Content-Length", String.valueOf(xmlbyte.length));
            conn.setRequestProperty("Content-Type", "text/xml; charset=UTF-8");
            
            conn.getOutputStream().write(xmlbyte);
            conn.getOutputStream().flush();
            conn.getOutputStream().close();

            if(conn.getResponseCode()!=HttpURLConnection.HTTP_OK) {
            	throw new RuntimeException("请求url失败...");
            }
            
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			InputStream in = conn.getInputStream();
			
			int bytesRead = 0;
			byte[] buffer = new byte[1024];
			while((bytesRead=in.read(buffer))>0) {
				out.write(buffer, 0, bytesRead);
			}
			out.close();
			String result = new String(out.toByteArray());
			Log.i(TAG, result);
			
			XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
			XmlPullParser parser = factory.newPullParser();
			parser.setInput(new StringReader(result));
			parseItem(items, parser);
		} catch(Exception e) {
		} finally {
		}
		return items;
	}
	
	private void parseItem(List<GalleryItem> items, XmlPullParser parser) throws Exception {
		int eventType = parser.getEventType();
		try {
			GalleryItem item = null;
			String fieldName = "";
			String value = "";
			while(eventType!=XmlPullParser.END_DOCUMENT) {
				String nodeName = parser.getName();
				switch(eventType) {
				case XmlPullParser.START_TAG:
					if("name".equals(nodeName)) {
						fieldName = parser.nextText();
					} else if("string".equals(nodeName)) {
						value = parser.nextText();
						if("did".equals(fieldName)) {
							if(item==null) {
								item = new GalleryItem();
							}
							item.setId(value);
						} else if("src".equals(fieldName)) {
							item.setUrl(value);
						} else if("title".equals(fieldName)) {
							item.setCaption(value);
						}
					}
					break;
				case XmlPullParser.END_TAG:
					if("struct".equals(nodeName)) {
						if(item!=null) {
							items.add(item);
						}
						item = null;
						fieldName = "";
						value = "";
					}
					break;
				default:
					break;
				}
				eventType = parser.next();
			}
		} catch (Exception e) {
			
		}
	}
	
	private String buildXmlRequestString() {
		StringBuilder xml = new StringBuilder();
		xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		xml.append("<methodCall>");
		xml.append("<methodName>").append(METHOD_GET_RECENT).append("</methodName>");
		xml.append("<params>");
		xml.append("	<param>");
		xml.append("		<value><string>").append(API_KEY).append("</string></value>");
		xml.append("	</param>");
		xml.append("	<param>");
		xml.append("		<value><string>1</string></value>");
		xml.append("	</param>");
		xml.append("	<param>");
		xml.append("		<value><string>20</string></value>");
		xml.append("	</param>");
		xml.append("	<param>");
		xml.append("		<value><string>240</string></value>");
		xml.append("	</param>");
		xml.append("</params>");
		xml.append("</methodCall>");
		return xml.toString();
	}
}
