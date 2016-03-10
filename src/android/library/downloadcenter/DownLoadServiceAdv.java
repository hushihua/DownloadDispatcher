/**
 * 
 */
package android.library.downloadcenter;

import java.util.ArrayList;

import android.app.Service;
import android.content.Intent;
import android.library.downloadcenter.adv.DownLoadSchedule;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

/**
 * @author hushihua
 *
 */
public class DownLoadServiceAdv extends Service {
	
	private StateCacheCenter _cacheCenter = StateCacheCenter.getInstance();
	
	@Override
	public void onCreate() {
		super.onCreate();
		_cacheCenter.loadCacheDateFromSQlite(this);
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (intent != null) {
			Object extra = intent.getSerializableExtra("SCHEDULE");
			if (extra != null && extra instanceof AppDownLoadInfo ) {
				AppDownLoadInfo schedule = (AppDownLoadInfo)extra;
				_cacheCenter.pushSchedule(this, schedule);
			}
		}
		return super.onStartCommand(intent, flags, startId);
	}

	/**
	 * 问题：
     * 1.出于数据准确性考虑，是否在应用退出是，暂停所有下载任务
	 */
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		Log.i("download", "-------- DownLoadService onDestroy --------");
		DownLoadExecutors.getInstance().shutdown();
		super.onDestroy();
	}
	
	@Override
	public IBinder onBind(Intent arg0) {
		return binder;
	}
	
	private ServiceBinder binder = new ServiceBinder();
	
	public class ServiceBinder extends Binder {
		
        public DownLoadServiceAdv getService() {
            return DownLoadServiceAdv.this;
        }
        
        /**
    	 * 是否已经提交下载任务
    	 * @param url
    	 * @return
    	 */
    	public boolean isLoading(String url){
    		return _cacheCenter.isLoading(url);
    	}
    	
    	/**
    	 * 
    	 * @return
    	 */
    	public ArrayList<DownLoadSchedule> getCacheState(){
    		return _cacheCenter.getCacheState();
    	}
    	
    	/**
    	 * 添加、新增下载任务
    	 * @param context
    	 * @param schedule
    	 */
    	public void pushSchedule(DownLoadSchedule schedule){
    		_cacheCenter.pushSchedule(DownLoadServiceAdv.this, schedule);
    	}
    	
    	/**
    	 * 恢复下载任务，继续下载
    	 * @param context
    	 * @param schedule
    	 */
    	public void resumeScehdule(DownLoadSchedule schedule){
    		_cacheCenter.resumeScehdule(DownLoadServiceAdv.this, schedule);
    	}
    	
    	/**
    	 * 获取指定的下载任务
    	 * @param url
    	 * @return
    	 */
    	public DownLoadSchedule getDownLoadSchedule(String url){
    		return _cacheCenter.getDownLoadSchedule(url);
    	}
    	
    	/**
    	 * 删除下载任务
    	 * @param schedule
    	 */
    	public void popSchedule(DownLoadSchedule schedule){
    		_cacheCenter.popSchedule(schedule);
    	}
    	
    	/**
    	 * 删除下载任务
    	 * @param schedule
    	 */
    	public void popAllSchedule(){
    		_cacheCenter.popAllSchedule();
    	}
    }
}
