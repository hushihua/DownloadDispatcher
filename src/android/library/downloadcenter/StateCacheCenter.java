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

import java.io.File;
import java.util.ArrayList;

import android.content.Context;
import android.library.downloadcenter.DownLoadTask.DownLoadEventListener;
import android.library.downloadcenter.adv.DownLoadBoradcastSender;
import android.library.downloadcenter.adv.DownLoadSchedule;
import android.library.downloadcenter.adv.DownLoadSchedule.RunType;

/**
 * @author hushihua
 * 
 */
public class StateCacheCenter implements DownLoadEventListener{

	private static StateCacheCenter instance;
	private ArrayList<DownLoadSchedule> cacheData;
	private Context context;
	
	public static StateCacheCenter getInstance(){
		if (instance == null) {
			instance = new StateCacheCenter();
		}
		return instance;
	}
	
	private StateCacheCenter(){
		cacheData = new ArrayList<DownLoadSchedule>();
	}
	
	/**
	 * ��SQLite�ָ������б����ݣ��ں�̨���ط�����ʱ����
	 * @param context
	 */
	public void loadCacheDateFromSQlite(Context context){
		this.context = context;
		DownLoadDBManager.getInstance().initDB(context);
		ArrayList<AppDownLoadInfo> oldData = DownLoadDBManager.getInstance().loadAllSchedule();
		for (AppDownLoadInfo appDownLoadInfo : oldData) {
			//����ļ��Ƿ��Ѿ�ɾ����ɾ���ļ������������ݿ���ɾ��
			File file = new File(appDownLoadInfo.path);
			if (!file.exists()) {
				DownLoadDBManager.getInstance().deleteSchedule(appDownLoadInfo);
				continue;
			}
			//ǿ�����ó�ʼ״̬
			if (appDownLoadInfo.totalSize == appDownLoadInfo.completeSize) {
				appDownLoadInfo.state = RunType.FINISH;
			}else{
				appDownLoadInfo.state = RunType.PAUSE;//��ʱ���ó� pause
			}
			cacheData.add(appDownLoadInfo);
		}
//		cacheData.addAll(oldData);
	}
	
	/**
	 * ��ӡ�������������
	 * @param context
	 * @param schedule
	 */
	public void pushSchedule(Context context, DownLoadSchedule schedule){
		this.context = context;
		for (int i = 0; i < cacheData.size(); i++) { //���ظ����ͬһ���ؼƻ�
			if (cacheData.get(i).downLoadUrl.equals(schedule.downLoadUrl)) {
				return;
			}
		}
		schedule.state = RunType.WAITING;
		cacheData.add(schedule);
		//����һ���µ����ؼ�¼
		DownLoadDBManager.getInstance().insertSchedule((AppDownLoadInfo)schedule);
		//�п������е����ȼ����ǰ�����˳������
		DownLoadExecutors.getInstance().submitRequest(schedule, this);
	}
	
	/**
	 * �ָ��������񣬼�������
	 * @param context
	 * @param schedule
	 */
	public void resumeScehdule(Context context, DownLoadSchedule schedule){
		this.context = context;
		boolean runable = false;
		for (int i = 0; i < cacheData.size(); i++) { //���ظ����ͬһ���ؼƻ�
			if (cacheData.get(i).downLoadUrl.equals(schedule.downLoadUrl)) {
				runable = true;
				break;
			}
		}
		if (runable) {
			schedule.state = RunType.WAITING;
			DownLoadExecutors.getInstance().submitRequest(schedule, this);
		}
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
				DownLoadBoradcastSender.sendRefreshDownloadCacheBroadcast(context);
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
		DownLoadBoradcastSender.sendRefreshDownloadCacheBroadcast(context);
	}
	
	/**
	 * ��ȡ������������
	 * @return
	 */
	public ArrayList<DownLoadSchedule> getCacheState(){
		return cacheData;
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
	 * ��ͣ������������
	 */
	public void paushAllSchedule(){
		for (int i = 0; i < cacheData.size(); i++) {
			DownLoadSchedule schedule = cacheData.get(i);
			schedule.common = RunType.PAUSE;
		}
	}

	@Override
	public void onEventHandler(DownLoadSchedule schedule) {
		// TODO Auto-generated method stub
		if (schedule.state == RunType.DELETE) {
			popSchedule(schedule);//ɾ�����ؼ�¼�����͹㲥�������б�
			return;
		}
		
		if (schedule.state == RunType.FINISH) {//�������
			DownLoadBoradcastSender.sendItemFinishBroadcast(context, schedule);
		}
	}

}
