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

import android.library.downloadcenter.AppDownLoadInfo;
import android.library.downloadcenter.DownLoadDBManager;
import android.library.downloadcenter.adv.DownLoadSchedule.RunType;

/**
 * @author hushihua
 *
 */
class DoubleCacheHandler extends MemoryCacheHandler{

	@Override
	public void pushSchedule(DownLoadSchedule schedule) {
		// TODO Auto-generated method stub
		super.pushSchedule(schedule);
		DownLoadDBManager.getInstance().insertSchedule((AppDownLoadInfo)schedule);
	}

	@Override
	public void popSchedule(DownLoadSchedule schedule) {
		// TODO Auto-generated method stub
		super.popSchedule(schedule);
		DownLoadDBManager.getInstance().deleteSchedule((AppDownLoadInfo)schedule);
	}

	@Override
	public void popSchedule(String sIdentily) {
		// TODO Auto-generated method stub
		for (int i = 0; i < cacheData.size(); i++) {
			DownLoadSchedule schedule = cacheData.get(i);
			if (schedule.downLoadUrl.equals(sIdentily)) {
				cacheData.remove(i);
				DownLoadDBManager.getInstance().deleteSchedule((AppDownLoadInfo)schedule);
			}
		}
	}

	@Override
	public void clean() {
		// TODO Auto-generated method stub
		for (int i = 0; i < cacheData.size(); i++) {
			DownLoadSchedule info = cacheData.get(i);
			if (info.state == RunType.LOADING || info.state == RunType.WAITING) {
				info.common = RunType.DELETE;//让线程停下来，但遇到阻塞时，不会这么快停下来，如何解决
			}
			DownLoadDBManager.getInstance().deleteSchedule((AppDownLoadInfo)info);
		}
		cacheData.clear();
	}

}
