/* ========================================================================
 *
 * Copyright (C) 2015 hushihua
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =========================================================================
 */
package android.library.downloadcenter;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.library.downloadcenter.DownLoadTask.DownLoadEventListener;
import android.library.downloadcenter.adv.DownLoadBoradcastSender;
import android.library.downloadcenter.adv.DownLoadSchedule;
import android.library.downloadcenter.adv.DownLoadSchedule.RunType;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

/**
 * @author hushihua
 *
 *���⣺
 *1.��������׼ȷ�Կ��ǣ��Ƿ���Ӧ���˳��ǣ���ͣ������������
 *
 */
public class DownLoadService extends Service implements DownLoadEventListener{

	private ExecutorService exeService;
	private int POOL_SIZE = 5;
	private ArrayList<DownLoadSchedule> cacheData;

	@Override
	public void onCreate() {
		super.onCreate();
		exeService = Executors.newFixedThreadPool(POOL_SIZE);
		cacheData = new ArrayList<DownLoadSchedule>();
		DownLoadDBManager.getInstance().initDB(this);
		//fill data
		ArrayList<AppDownLoadInfo> oldData = DownLoadDBManager.getInstance().loadAllSchedule();
		for (AppDownLoadInfo appDownLoadInfo : oldData) {
			if (appDownLoadInfo.totalSize == appDownLoadInfo.completeSize) {
				appDownLoadInfo.state = RunType.FINISH;
			}else{
				appDownLoadInfo.state = RunType.PAUSE;//��ʱ���ó� pause
			}
		}
		cacheData.addAll(oldData);
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (intent != null) {
			Object extra = intent.getSerializableExtra("SCHEDULE");
			if (extra != null && extra instanceof AppDownLoadInfo ) {
				AppDownLoadInfo schedule = (AppDownLoadInfo)extra;
				pushSchedule(this, schedule);
			}
		}
		return super.onStartCommand(intent, flags, startId);
	}
	
	public void pushSchedule(Context context, DownLoadSchedule schedule){
		for (int i = 0; i < cacheData.size(); i++) {
			if (cacheData.get(i).downLoadUrl.equals(schedule.downLoadUrl)) {
				return;
			}
		}
		schedule.state = RunType.WAITING;
		cacheData.add(schedule);
		DownLoadDBManager.getInstance().insertSchedule((AppDownLoadInfo)schedule);
		submitRequest(schedule);
	}
	
	public void submitRequest(DownLoadSchedule schedule) {
		DownLoadTask runnable = new DownLoadTask(schedule, this);
		exeService.submit(runnable);
	}
	
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		Log.i("download", "-------- DownLoadService onDestroy --------");
		exeService.shutdown();
		super.onDestroy();
	}
	
	@Override
	public IBinder onBind(Intent arg0) {
		return binder;
	}
	
	private ServiceBinder binder = new ServiceBinder();
	
	public class ServiceBinder extends Binder {
		
        public DownLoadService getService() {
            return DownLoadService.this;
        }
        
        /**
    	 * �Ƿ��Ѿ��ύ��������
    	 * @param url
    	 * @return
    	 */
    	public boolean isLoading(String url){
    		for (int i = 0; i < cacheData.size(); i++) {
    			if (cacheData.get(i).downLoadUrl.equals(url)) {
    				return true;
    			}
    		}
    		return false;
    	}
    	
    	public ArrayList<DownLoadSchedule> getCacheState(){
    		return cacheData;
    	}
    	
    	/**
    	 * ��ӡ�������������
    	 * @param context
    	 * @param schedule
    	 */
    	public void pushSchedule(DownLoadSchedule schedule){
    		for (int i = 0; i < cacheData.size(); i++) { 
    			if (cacheData.get(i).downLoadUrl.equals(schedule.downLoadUrl)) {
    				return;
    			}
    		}
    		schedule.state = RunType.WAITING;
    		cacheData.add(schedule);
    		DownLoadDBManager.getInstance().insertSchedule((AppDownLoadInfo)schedule);
    		submitRequest(schedule);
    	}
    	
    	/**
    	 * �ָ��������񣬼�������
    	 * @param context
    	 * @param schedule
    	 */
    	public void resumeScehdule(DownLoadSchedule schedule){
    		boolean runable = false;
    		for (int i = 0; i < cacheData.size(); i++) {
    			if (cacheData.get(i).downLoadUrl.equals(schedule.downLoadUrl)) {
    				runable = true;
    				break;
    			}
    		}
    		if (runable) {
    			schedule.state = RunType.WAITING;
    			submitRequest(schedule);
    		}
    	}
    	
    	/**
    	 * ��ȡָ������������
    	 * @param url
    	 * @return
    	 */
    	public DownLoadSchedule getDownLoadSchedule(String url){
    		for (int i = 0; i < cacheData.size(); i++) {
    			if (cacheData.get(i).downLoadUrl.equals(url)) {
    				return cacheData.get(i);
    			}
    		}
    		return null;
    	}
    	
    	/**
    	 * ɾ����������
    	 * @param schedule
    	 */
    	public void popSchedule(DownLoadSchedule schedule){
    		for (int i = 0; i < cacheData.size(); i++) {
    			if (cacheData.get(i).downLoadUrl.equals(schedule.downLoadUrl)) {
    				cacheData.remove(i);
    				DownLoadDBManager.getInstance().deleteSchedule((AppDownLoadInfo)schedule);
    				DownLoadBoradcastSender.sendRefreshDownloadCacheBroadcast(DownLoadService.this);
    				return;
    			}
    		}
    	}
    	
    	/**
    	 * ɾ����������
    	 * @param schedule
    	 */
    	public void popAllSchedule(){
    		for (int i = 0; i < cacheData.size(); i++) {
    			DownLoadSchedule info = cacheData.get(i);
				if (info.state == RunType.LOADING || info.state == RunType.WAITING) {
					info.common = RunType.DELETE;//���߳�ͣ����������������ʱ��������ô��ͣ��������ν��
				}
				DownLoadDBManager.getInstance().deleteSchedule((AppDownLoadInfo)info);
    		}
    		cacheData.clear();
			DownLoadBoradcastSender.sendRefreshDownloadCacheBroadcast(DownLoadService.this);
    	}
    }

	@Override
	public void onEventHandler(DownLoadSchedule schedule) {
		// TODO Auto-generated method stub
		if (schedule.state == RunType.DELETE) {
			for (int i = 0; i < cacheData.size(); i++) {
    			if (cacheData.get(i).downLoadUrl.equals(schedule.downLoadUrl)) {
    				cacheData.remove(i);
    				DownLoadDBManager.getInstance().deleteSchedule((AppDownLoadInfo)schedule);
    				DownLoadBoradcastSender.sendRefreshDownloadCacheBroadcast(DownLoadService.this);
    				return;
    			}
    		}
			return;
		}
		
		if (schedule.state == RunType.FINISH) {//�������
			DownLoadBoradcastSender.sendItemFinishBroadcast(this, schedule);
		}
	}
}
