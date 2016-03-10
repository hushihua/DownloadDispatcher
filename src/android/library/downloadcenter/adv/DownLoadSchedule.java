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

import java.io.Serializable;

import android.library.downloadcenter.adv.DownloadCenterDispatcher.CachePolicyType;

/**
 * @author hushihua
 *
 */
public class DownLoadSchedule implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public enum RunType{
		WAITING, LOADING, FINISH, ERROR, PAUSE, DELETE, INSTALL, NOME, REMOVE
	}
	
	public int totalSize;
	public int completeSize;
	public RunType state; //emun
	public int property;//优先级
	public String path;  //要有一个创建机制
	public String downLoadUrl;
	public RunType common; //emun 命令
	
	public CachePolicyType policy;
	
}
