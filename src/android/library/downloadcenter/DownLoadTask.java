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
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;

import android.library.downloadcenter.adv.DownLoadSchedule;
import android.library.downloadcenter.adv.DownLoadSchedule.RunType;
import android.util.Log;


public class DownLoadTask implements Runnable{
	
	private DownLoadSchedule schedule;
	private DownLoadDBManager dbManager;
	private DownLoadEventListener listener;
	
	public interface DownLoadEventListener{
		public void onEventHandler(DownLoadSchedule schedule);
	}
	
	public DownLoadTask(DownLoadSchedule info, DownLoadEventListener listener) {
		this.schedule = info;
		this.listener = listener;
		dbManager = DownLoadDBManager.getInstance();
	}
	
	@Override
	public void run() {
		if (schedule.common == RunType.DELETE) {
			return;
		}
		schedule.state = RunType.LOADING;
		try {
			URL downUrl = new URL(schedule.downLoadUrl);
			HttpURLConnection connection = (HttpURLConnection) downUrl.openConnection();
			connection.setRequestMethod("GET");
			connection.setConnectTimeout(1000);//注意调整好时间,时间短的话，有利于暂停操作快，是否考虑用其它方式实现外部的暂停？
			if (schedule.completeSize > 0) {
				connection.setRequestProperty("Range", "bytes="+ schedule.completeSize + "-" + schedule.totalSize);//已经下载过的文件，实现续传，新的下载任务，不设置该值
			}
			if (connection.getResponseCode() == 200 || connection.getResponseCode() == 206) {
				if (schedule.completeSize <= 0) {
					Log.i("DownLoad", "<-- 开始下载文件: " + schedule.downLoadUrl + " -->");
					schedule.totalSize = connection.getContentLength(); //以新任务下载时，设置该值
				}
				Log.i("DownLoad", "<-- load file: " + schedule.downLoadUrl + " -->");
				
				//可以考虑添加过滤机制，确保两个文件的大小一样
				InputStream inputStream = connection.getInputStream();
				
				File file = new File(schedule.path);
				if (!file.exists()) {
					file.createNewFile();
				}
				RandomAccessFile outPutStream = new RandomAccessFile(file, "rwd");
				outPutStream.seek(schedule.completeSize);
//				FileOutputStream outPutStream = new FileOutputStream(file);
				
				byte[] buffer = new byte[512];//1k
				int len = 0;
				while ((len = inputStream.read(buffer)) != -1) {
					outPutStream.write(buffer, 0, len);
					schedule.completeSize += len; // add cache-length
					dbManager.updateSchedule(schedule);
//					Log.i("DownLoad", "<-- 下载片段大小: " + len + "， 当前已下载: " + schedule.completeSize + " -->");
					//加入暂停判断 
					if (schedule.common == RunType.PAUSE) {
						schedule.state = RunType.PAUSE;//改写INFO的状态为暂停状态
						outPutStream.close();
						inputStream.close();
						Log.i("DownLoad", "<-- 暂停: " + schedule.downLoadUrl + " -->");
						return;
					}
					//删除状态
					if (schedule.common == RunType.DELETE) {
						schedule.state = RunType.DELETE;
						outPutStream.close();
						inputStream.close();
						file.delete();//删除文件
						listener.onEventHandler(schedule);
//						StateCacheCenter.getInstance().popSchedule(schedule);//删除下载记录，发送广播，更新列表
						Log.i("DownLoad", "<-- 删除: " + schedule.downLoadUrl + " -->");
						return;
					}
					
					//特别用于一键清空
					if (schedule.common == RunType.REMOVE) {
						outPutStream.close();
						inputStream.close();
						file.delete();//删除文件
						Log.i("DownLoad", "<-- REMOVE: " + schedule.downLoadUrl + " -->");
						return;
					}
					
				}
				outPutStream.close();
				inputStream.close();
				schedule.state = RunType.FINISH;
				listener.onEventHandler(schedule);
				//完成是，SQLITE也要记录一下, 除非加逻辑判断
				Log.i("DownLoad", "<-- 下载完成: " + schedule.downLoadUrl + " -->");
				
			}else{
				schedule.state = RunType.ERROR;
				Log.i("DownLoad", "<-- 连接出错，错误码: " + connection.getResponseCode() + " -->");
			}
		} catch (Exception e) {
			e.printStackTrace();
			schedule.state = RunType.ERROR;
			Log.i("DownLoad", "<-- 下载出错，错误信息: " + e.getMessage() + " -->");
		}
	}
	
}
