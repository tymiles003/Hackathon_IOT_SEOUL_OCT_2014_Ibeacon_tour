package com.junit.bline;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.URI;
import java.net.URLEncoder;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class httpCon extends Thread implements Runnable
{
	String UUID;
	MainActivity requsetActivity;
	
	ResultHandler mHandler = new ResultHandler(requsetActivity);//핸들러 사용을 위한 선언
	
	public httpCon(String uuid, MainActivity mainActivity)
	{
		UUID = uuid;
		requsetActivity = mainActivity;
	}

	public void run()
	{
		String StringA = UUID;
		
		Log.i("StringA의 값 : ", StringA);
		
		DefaultHttpClient client = new DefaultHttpClient();
		
		try {

			String En_StringA = URLEncoder.encode(StringA, "utf-8");
			//TODO
			String address = "http://bline.azurewebsites.net" + "/dbtest.jsp?UUID=" + En_StringA;
			
			Log.i("address의 값 : ", address);
			HttpGet HG = new HttpGet();
			
			HG.setURI(new URI(address));
			Log.i("수행완료 : ", address);
			
			HttpResponse response = client.execute(HG);///실행문제
			
			HttpParams params = client.getParams();
			HttpConnectionParams.setConnectionTimeout(params, 3000);
			HttpConnectionParams.setSoTimeout(params, 3000);
			response.setParams(params);
			Log.i("레알수행완료 : ", address);

			BufferedReader bufreader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "utf-8"));

			String line = null;
			String result = "";

			while ((line = bufreader.readLine()) != null)
			{
				result += line; // 버퍼로 읽어들인 JSON형식의 String을 그대로 result에 넣어준다.
				Message msg = Message.obtain();
				
				msg.obj = result;//line;
				mHandler.sendMessage(msg);
				Log.i("응답 값" + " : ", result);
			}

		} catch (Exception e) {
			e.printStackTrace();
			client.getConnectionManager().shutdown(); // 연결 지연 종료

			String line = "연결 실패";
			Message msg = Message.obtain();
			msg.obj = line;

			mHandler.sendMessage(msg);
		}
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			
		}
	}
	
	static class ResultHandler extends Handler {
	    private final WeakReference<MainActivity> mActivity;
	         
	    ResultHandler(MainActivity activity) {
	        mActivity = new WeakReference<MainActivity>(activity);
	    }

		@Override
		public void handleMessage(Message msg) {
			MainActivity activity = mActivity.get();
	        if(activity != null) {
				activity.handleMessage(msg);
			}
		}
	}
}