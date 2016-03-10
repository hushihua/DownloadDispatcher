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
package android.library.downloadcenter.adv;

import java.util.List;

import android.content.Context;
import android.library.downloadcenter.AppDownLoadInfo;
import android.library.downloadcenter.DownLoadDBManager;
import android.library.downloadcenter.DownLoadExecutors;
import android.library.downloadcenter.DownLoadTask.DownLoadEventListener;
import android.library.downloadcenter.adv.DownLoadSchedule.RunType;

/**
 * 数据下载中心
 * 
 * 支持数据断点下 载
 * 
 * @author hushihua
 *
 */
public class DownloadCenterDispatcher implements DownLoadEventListener{
	
	private static DownloadCenterDispatcher instance;
	private ICacheHandler memoryCachePolicy;
	private Context context;
	
	/**
	 * 下载中心，数据缓存策略
	 */
	public enum CachePolicyType{
		MEMORY, //内存缓存
		MEMORY_AND_FILE //内存及文件中缓存
	}
	
	public static DownloadCenterDispatcher getInstance(){
		if (instance == null) {
			instance = new DownloadCenterDispatcher();
		}
		return instance;
	}
	
	private DownloadCenterDispatcher(){
		
	}
	
	/**
	 * 添加、新增下载任务
	 * 
	 * 不能重复提交同一URL的下载任务/br
	 * 
	 * @param context
	 * @param schedule
	 */
	public void pushSchedule(Context context, DownLoadSchedule schedule){
		this.context = context;
		DownLoadSchedule cache = memoryCachePolicy.getSchedule(schedule.downLoadUrl);
		if (cache != null) {
			return;
		}
		schedule.state = RunType.WAITING;
		memoryCachePolicy.pushSchedule(schedule);
		DownLoadExecutors.getInstance().submitRequest(schedule, this);
	}
	
	/**
	 * 恢复下载任务，继续下载
	 * @param context
	 * @param schedule
	 */
	public void resumeScehdule(Context context, DownLoadSchedule schedule){
		DownLoadSchedule cache = memoryCachePolicy.getSchedule(schedule.downLoadUrl);
		if (cache != null && (schedule.state != RunType.WAITING || schedule.state != RunType.LOADING)) {
			schedule.state = RunType.WAITING;
			DownLoadExecutors.getInstance().submitRequest(schedule, this);
		}
	}
	
	/**
	 * 删除下载任务
	 * @param schedule
	 */
	public void popSchedule(DownLoadSchedule schedule){
		memoryCachePolicy.popSchedule(schedule);
		DownLoadBoradcastSender.sendRefreshDownloadCacheBroadcast(context);
	}
	
	/**
	 * 删除下载任务
	 * @param schedule
	 */
	public void popAllSchedule(){
		memoryCachePolicy.clean();
		DownLoadBoradcastSender.sendRefreshDownloadCacheBroadcast(context);
	}
	
	/**
	 * 获取所有下载任务
	 * @return
	 */
	public List<DownLoadSchedule> loadAllDownLoadSchedule(){
		return memoryCachePolicy.loadAllDownloadSchedule();
	}
	
	/**
	 * 获取指定的下载任务
	 * @param url
	 * @return
	 */
	public DownLoadSchedule getDownLoadSchedule(String url){
		return memoryCachePolicy.getSchedule(url);
	}
	
	/**
	 * 暂停所有下载任务
	 */
	public void paushAllSchedule(){
		List<DownLoadSchedule> localSchedules =  memoryCachePolicy.loadAllDownloadSchedule();
		for (int i = 0; i < localSchedules.size(); i++) {
			DownLoadSchedule schedule = localSchedules.get(i);
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
