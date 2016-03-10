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

import java.util.ArrayList;
import java.util.List;

import android.library.downloadcenter.adv.DownLoadSchedule.RunType;

/**
 * @author hushihua
 *
 */
class MemoryCacheHandler implements ICacheHandler{

	
	private static MemoryCacheHandler instance;
	protected ArrayList<DownLoadSchedule> cacheData;
	
	public static ICacheHandler getInstance(){
		if (instance == null) {
			instance = new MemoryCacheHandler();
		}
		return instance;
	}
	
	@Override
	public void pushSchedules(List<DownLoadSchedule> schedules) {
		for (DownLoadSchedule downLoadSchedule : schedules) {
			pushSchedule(downLoadSchedule);
		}
	}

	/**
	 * 不重复添加同一下载计划
	 */
	@Override
	public void pushSchedule(DownLoadSchedule schedule) {
		for (int i = 0; i < cacheData.size(); i++) {
			if (cacheData.get(i).downLoadUrl.equals(schedule.downLoadUrl)) {
				return;
			}
		}
		schedule.state = RunType.WAITING;
		cacheData.add(schedule);
	}

	@Override
	public DownLoadSchedule getSchedule(String sIdentily) {
		for (int i = 0; i < cacheData.size(); i++) {
			DownLoadSchedule schedule = cacheData.get(i);
			if (schedule.downLoadUrl.equals(sIdentily)) {
				return cacheData.get(i);
			}
		}
		return null;
	}

	@Override
	public void popSchedule(DownLoadSchedule schedules) {
		popSchedule(schedules.downLoadUrl);
	}

	@Override
	public void popSchedule(String sIdentily) {
		for (int i = 0; i < cacheData.size(); i++) {
			DownLoadSchedule schedule = cacheData.get(i);
			if (schedule.downLoadUrl.equals(sIdentily)) {
				cacheData.remove(i);
			}
		}
	}

	@Override
	public void clean() {
		for (int i = 0; i < cacheData.size(); i++) {
			DownLoadSchedule info = cacheData.get(i);
			if (info.state == RunType.LOADING || info.state == RunType.WAITING) {
				info.common = RunType.DELETE;//让线程停下来，但遇到阻塞时，不会这么快停下来，如何解决
			}
		}
		cacheData.clear();
	}

	@Override
	public List<DownLoadSchedule> loadAllDownloadSchedule() {
		return cacheData;
	}

}
