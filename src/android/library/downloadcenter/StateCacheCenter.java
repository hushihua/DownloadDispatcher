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
	 * 从SQLite恢复下载列表数据，在后台下载服务开启时调用
	 * @param context
	 */
	public void loadCacheDateFromSQlite(Context context){
		this.context = context;
		DownLoadDBManager.getInstance().initDB(context);
		ArrayList<AppDownLoadInfo> oldData = DownLoadDBManager.getInstance().loadAllSchedule();
		for (AppDownLoadInfo appDownLoadInfo : oldData) {
			//检查文件是否已经删除，删除文件的任务，在数据库中删除
			File file = new File(appDownLoadInfo.path);
			if (!file.exists()) {
				DownLoadDBManager.getInstance().deleteSchedule(appDownLoadInfo);
				continue;
			}
			//强制设置初始状态
			if (appDownLoadInfo.totalSize == appDownLoadInfo.completeSize) {
				appDownLoadInfo.state = RunType.FINISH;
			}else{
				appDownLoadInfo.state = RunType.PAUSE;//暂时设置成 pause
			}
			cacheData.add(appDownLoadInfo);
		}
//		cacheData.addAll(oldData);
	}
	
	/**
	 * 添加、新增下载任务
	 * @param context
	 * @param schedule
	 */
	public void pushSchedule(Context context, DownLoadSchedule schedule){
		this.context = context;
		for (int i = 0; i < cacheData.size(); i++) { //不重复添加同一下载计划
			if (cacheData.get(i).downLoadUrl.equals(schedule.downLoadUrl)) {
				return;
			}
		}
		schedule.state = RunType.WAITING;
		cacheData.add(schedule);
		//插入一条新的下载记录
		DownLoadDBManager.getInstance().insertSchedule((AppDownLoadInfo)schedule);
		//有可能运行的优先级不是按加入顺序运行
		DownLoadExecutors.getInstance().submitRequest(schedule, this);
	}
	
	/**
	 * 恢复下载任务，继续下载
	 * @param context
	 * @param schedule
	 */
	public void resumeScehdule(Context context, DownLoadSchedule schedule){
		this.context = context;
		boolean runable = false;
		for (int i = 0; i < cacheData.size(); i++) { //不重复添加同一下载计划
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
	 * 删除下载任务
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
	 * 删除下载任务
	 * @param schedule
	 */
	public void popAllSchedule(){
		for (int i = 0; i < cacheData.size(); i++) {
			DownLoadSchedule info = cacheData.get(i);
			if (info.state == RunType.LOADING || info.state == RunType.WAITING) {
				info.common = RunType.DELETE;//让线程停下来，但遇到阻塞时，不会这么快停下来，如何解决
			}
			DownLoadDBManager.getInstance().deleteSchedule((AppDownLoadInfo)info);
		}
		cacheData.clear();
		DownLoadBoradcastSender.sendRefreshDownloadCacheBroadcast(context);
	}
	
	/**
	 * 获取所有下载任务
	 * @return
	 */
	public ArrayList<DownLoadSchedule> getCacheState(){
		return cacheData;
	}
	
	/**
	 * 是否已经提交下载任务
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
	 * 获取指定的下载任务
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
	 * 暂停所有下载任务
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
			popSchedule(schedule);//删除下载记录，发送广播，更新列表
			return;
		}
		
		if (schedule.state == RunType.FINISH) {//下载完成
			DownLoadBoradcastSender.sendItemFinishBroadcast(context, schedule);
		}
	}

}
