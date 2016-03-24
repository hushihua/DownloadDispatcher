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

import android.content.Context;
import android.content.Intent;

/**
 * @author hushihua
 *
 */
public class DownLoadBoradcastSender {

	public static String REFRESH_DOWNLOAD_LIST_ACTION = "android.library.framework.download:CACHE_LIST_CHANGE";
	public static String DOWNLOAD_ITEM_FINISH_ACTION = "android.library.framework.download:ITEM_FINISH";

	public static void sendRefreshDownloadCacheBroadcast(Context context) {
		Intent intent = new Intent(REFRESH_DOWNLOAD_LIST_ACTION);
		context.sendBroadcast(intent);
		// LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
	}

	public static void sendItemFinishBroadcast(Context context, DownLoadSchedule schedule) {
		Intent intent = new Intent(DOWNLOAD_ITEM_FINISH_ACTION);
		intent.putExtra("DATA", schedule);
		context.sendBroadcast(intent);
		// LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
	}
}
