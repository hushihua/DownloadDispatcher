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

/**
 * @author hushihua
 *
 */
public interface ICacheHandler {
	
	/**
	 * add schedule collection
	 * @param schedules
	 */
	public void pushSchedules(List<DownLoadSchedule> schedules);
	
	/**
	 * load all download schedule
	 * @return
	 */
	public List<DownLoadSchedule> loadAllDownloadSchedule();
	
	/**
	 * add schedule
	 * @param schedules
	 */
	public void pushSchedule(DownLoadSchedule schedule);
	
	/**
	 * get schedule by inentily
	 * @param sIdentily
	 * @return
	 */
	public DownLoadSchedule getSchedule(String sIdentily);
	
	/**
	 * remove schedule by schedule object
	 * @param schedules
	 */
	public void popSchedule(DownLoadSchedule schedule);
	
	/**
	 * remove schedule by inentily 
	 * @param sIdentily
	 */
	public void popSchedule(String sIdentily);
	
	/**
	 * clean all cache data
	 */
	public void clean();
}
