package com.cuisec.mshield.push;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.cuisec.mshield.MyApplication;
import com.cuisec.mshield.R;
import com.cuisec.mshield.SeachActivity;
import com.cuisec.mshield.activity.home.HomeActivity;
import com.cuisec.mshield.bean.PushBean;
import com.cuisec.mshield.design.NetTrustManager;
import com.cuisec.mshield.utils.L;
import com.cuisec.mshield.utils.SPUtils;
import com.google.gson.Gson;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class PushService extends Service {

	 static Timer timer = null;
	private Date mParse;
	private volatile boolean mIsDestroy = false;
	private int countNum = 0;
	private int mChaZhi;
	private String mNextUpdateDate;
	Thread mThread = null;
	private Runnable mRunnable;

	//清除通知
	public static void cleanAllNotification() {
		NotificationManager mn= (NotificationManager) HomeActivity.getContext().getSystemService(NOTIFICATION_SERVICE);
		mn.cancelAll();	
		if (timer != null) {
			timer.cancel();
			timer = null;
		}
	}

	//添加通知
	public static void addNotification(int delayTime,String tickerText,String contentTitle,String contentText)
	{			
		Intent intent = new Intent(HomeActivity.getContext(), PushService.class);
		intent.putExtra("delayTime", delayTime);
		intent.putExtra("tickerText", tickerText);
		intent.putExtra("contentTitle", contentTitle);
		intent.putExtra("contentText", contentText);
		HomeActivity.getContext().startService(intent);
	}
	
    public void onCreate() {
		initData3();
    	Log.e("addNotification", "===========create=======");
    }
    
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public int onStartCommand(final Intent intent, int flags, int startId) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				//需要在子线程中处理的逻辑
				while(true){
					try {
						Thread.sleep(10000);
						//线程休眠十分钟检查一次更新
						SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
						Date curdate =new Date();
						try {
							mParse = dateFormat.parse(mPushBean.getData().getNextUpdateDate());
						} catch (ParseException e) {
							e.printStackTrace();
						}
						L.i("当前时间"+curdate);
						L.i("获取时间"+mParse);
						if (mParse != null && curdate.getTime() < mParse.getTime()){
							L.i("获取时间"+(curdate.getTime() < mParse.getTime()));
							continue;
						}
						countNum = (int) SPUtils.get(getApplicationContext(),"num",countNum);
						initData3();
						int count1 = mPushBean.getData().getCount();
						mNextUpdateDate = mPushBean.getData().getNextUpdateDate();
						SPUtils.put(getApplicationContext(),"num",count1);
						L.i("count1 "+countNum + "    "+"mNextUpdateDate"+mNextUpdateDate);
						Thread.sleep(10000);
						if (countNum >= count1 || countNum == 0){
							continue;
						}
						mChaZhi = count1 - countNum;
						L.i(mChaZhi+" 差值");
						//setNotification();
						mHandler.sendMessage(mHandler.obtainMessage());
						//sleep2秒，可根据需求更换为响应的时间
						//SystemClock.sleep(10000);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}).start();
	/*	long period = 10000; //24小时一个周期
		int delay=intent.getIntExtra("delayTime",0);
		if (null == timer ) {
			timer = new Timer();
		}*/
		return super.onStartCommand(intent, flags, startId);
	}
	
    @Override
    public void onDestroy(){
    	Log.e("addNotification", "===========destroy=======");
       super.onDestroy();
    }
	//调用定时刷新函数
	private Handler mHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			setNotification();
			/*timer.schedule(new TimerTask() {

				@Override
				public void run() {
					while(true){
						try {
							Thread.sleep(10000);
							//线程休眠十分钟检查一次更新
							SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
							Date curdate =new Date();
							try {
								mParse = dateFormat.parse(mPushBean.getData().getNextUpdateDate());
							} catch (ParseException e) {
								e.printStackTrace();
							}
							L.i("当前时间"+curdate);
							L.i("获取时间"+mParse);
							if (mParse != null && curdate.getTime() < mParse.getTime()){
								L.i("获取时间"+(curdate.getTime() < mParse.getTime()));
								continue;
							}
							countNum = (int) SPUtils.get(getApplicationContext(),"num",countNum);
							initData3();
							int count1 = mPushBean.getData().getCount();
							mNextUpdateDate = mPushBean.getData().getNextUpdateDate();
							SPUtils.put(getApplicationContext(),"num",count1);
							L.i("count1 "+countNum + "    "+"mNextUpdateDate"+mNextUpdateDate);
							if (countNum >= count1 || countNum == 0){
								continue;
							}
							mChaZhi = count1 - countNum;
							L.i(mChaZhi+" 差值");
							//setNotification();
							mHandler.sendMessage(mHandler.obtainMessage());
							//sleep2秒，可根据需求更换为响应的时间
							//SystemClock.sleep(10000);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			*//*},delay, period);*/
		}
	};
	/**
	 * 创建通知栏
	 */
	private void setNotification() {
		if (mNotificationManager == null )
			mNotificationManager = (NotificationManager)  PushService.this.getSystemService(Context.NOTIFICATION_SERVICE);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
			NotificationChannel channel = new NotificationChannel(
					PushService.this.getPackageName(),
					"通知",
					NotificationManager.IMPORTANCE_DEFAULT
			);
			mNotificationManager.createNotificationChannel(channel);
		}
		mBuilder = new NotificationCompat.Builder(this,CHANNEL);
		mBuilder
				.setSmallIcon(R.mipmap.ic_launcher_round)
				.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
				.setOngoing(true)
				.setAutoCancel(true)
				.setChannelId( PushService.this.getPackageName())
				.setPriority(NotificationCompat.PRIORITY_HIGH)
				.setTicker("您有一条新消息！")
				.setContentTitle("您有"+mChaZhi+"条新的招标讯息")
				//.setContentText(mPushBean.getData().getNextUpdateDate().substring(0, 11)+"点击查看更多详情信息")
				.setAutoCancel(true)
				.setWhen(System.currentTimeMillis());
		updateIntent = new Intent( PushService.this, SeachActivity.class);
		//updateIntent.putExtra("title",mCitrsBean.getDomain_list().get(0).getTitle());
		updatePendingIntent = PendingIntent.getActivity( PushService.this,0, updateIntent,PendingIntent.FLAG_UPDATE_CURRENT);
		mBuilder.setContentIntent(updatePendingIntent);
		mNotificationManager.notify(1, mBuilder.build());
	}
	private NotificationManager mNotificationManager;
	private NotificationCompat.Builder mBuilder;
	private static final String CHANNEL = "update";
	private Intent updateIntent;
	private PendingIntent updatePendingIntent;
	private PushBean mPushBean;
	private void initData3() {
		OkHttpClient clientWithCache = getClientWithCache();
		RequestBody requestBody = FormBody.create(null
				, "");
		final Request request = new Request.Builder()
				.post(requestBody)
				//.url(Config.https_base_service_url+"bidinfo/checkcount")
				.url("https://111.203.206.158:38888/"+"bidinfo/checkcount")
				.build();
		clientWithCache.newCall(request).enqueue(new Callback() {
			@Override
			public void onFailure(Call call, IOException e) {

			}
			@Override
			public void onResponse(Call call, Response response) throws IOException {
				String string = response.body().string();
				L.i(string);
				Gson gson = new Gson();
				mPushBean = gson.fromJson(string, PushBean.class);
			}
		});
	}
	public OkHttpClient getClientWithCache() {
		return new OkHttpClient.Builder()
				.sslSocketFactory(NetTrustManager.getNetTrustManager().createSSLSocketFactory()).hostnameVerifier(new NetTrustManager.TrustAllHostnameVerifier())
				.build();
	}
}
