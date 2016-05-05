/*
 * Copyright (c) 2008-2016 Computer Network Information Center (CNIC), Chinese Academy of Sciences.
 * 
 * This file is part of Duckling project.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 *
 */
package net.duckling.dchat.rtp;

import java.io.File;
import java.nio.charset.Charset;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.log4j.Logger;


public class ViRestClient extends BaseRestClient {

	private static final String LOGO_FOR_WINDOWS = "logoForWindows";
	private static final String LOGO_FOR_ANDROID = "logoForAndroid";
	private static final Logger LOG = Logger.getLogger(ViRestClient.class);
	
	public ViRestClient(String webId, String server, int port) {
		super(webId, server, port);
	}

	private String getSetViUrl(String group) {
		String url = getUrl("vi_setgroupvi", "group=" + group);
		return url;
	}

	/**
	 * 设置用户所在组织的形象设置
	 * 
	 * @param title
	 *            软件标题
	 * @param windowsLogoFile
	 *            windows客户端显示的Logo，为null时代表清除Logo。
	 * @param androidLogoFile
	 *            android客户端显示的Logo，为null时代表清除Logo。
	 * @throws Exception
	 */
	public void setVi(String title, File windowsLogoFile, File androidLogoFile, String group)
			throws Exception {
		LOG.info("Update vi group of "+ group +" using title:"+title 
				+",windowLogoPath:" + windowsLogoFile +",androidLogoPath:" + androidLogoFile);
		MultipartEntity entity = new MultipartEntity(
				HttpMultipartMode.BROWSER_COMPATIBLE, null,
				Charset.forName("UTF-8"));
		if (title == null) {
			title = "";
		}
		entity.addPart("clientTitle",new StringBody(title, Charset.forName("UTF-8")));
		if (windowsLogoFile != null) {
			entity.addPart(LOGO_FOR_WINDOWS, new FileBody(windowsLogoFile));
		} else {
			entity.addPart(LOGO_FOR_WINDOWS, new StringBody(""));
		}
		if (androidLogoFile != null) {
			entity.addPart(LOGO_FOR_ANDROID, new FileBody(androidLogoFile));
		} else {
			entity.addPart(LOGO_FOR_ANDROID, new StringBody(""));
		}
		String url = getSetViUrl(group);
		postMultipartEntity(entity,url);
	}

	/**
	 * 设置用户所在组织所用的Android客户端软件的Logo
	 * 
	 * @param androidLogoFile
	 *            为null时代表清除Logo。
	 * @throws Exception
	 */
	public void setViAndroidLogo(File androidLogoFile,String group) throws Exception {
		MultipartEntity entity = new MultipartEntity(
				HttpMultipartMode.BROWSER_COMPATIBLE, null,
				Charset.forName("UTF-8"));
		if (androidLogoFile != null) {
			entity.addPart(LOGO_FOR_ANDROID, new FileBody(androidLogoFile));
		} else {
			entity.addPart(LOGO_FOR_ANDROID, new StringBody(""));
		}
		String url = getSetViUrl(group);
		postMultipartEntity(entity,url);
	}

	/**
	 * 设置用户所在组织所用的Windows客户端软件的Logo
	 * 
	 * @param windowsLogoFile
	 *            为null时代表清除Logo。
	 * @throws Exception
	 */
	public void setViWindowsLogo(File windowsLogoFile,String group) throws Exception {
		MultipartEntity entity = new MultipartEntity(
				HttpMultipartMode.BROWSER_COMPATIBLE, null,
				Charset.forName("UTF-8"));
		if (windowsLogoFile != null) {
			entity.addPart(LOGO_FOR_WINDOWS, new FileBody(windowsLogoFile));
		} else {
			entity.addPart(LOGO_FOR_WINDOWS, new StringBody(""));
		}
		String url = getSetViUrl(group);
		postMultipartEntity(entity,url);
	}

	/**
	 * 设置用户所在组织所用的客户端软件的标题
	 * 
	 * @param title
	 * @throws Exception
	 */
	public void setViTitle(String title,String group) throws Exception {
		MultipartEntity entity = new MultipartEntity(
				HttpMultipartMode.BROWSER_COMPATIBLE, null,
				Charset.forName("UTF-8"));
		if (title == null) {
			title = "";
		}
		entity.addPart("clientTitle",
				new StringBody(title, Charset.forName("UTF-8")));
		String url = getSetViUrl(group);
		postMultipartEntity(entity,url);
	}

	private void postMultipartEntity(MultipartEntity entity,String url) throws Exception {
		HttpClient httpClient = new DefaultHttpClient();
		HttpContext localContext = new BasicHttpContext();
		System.out.println(url);
		HttpPost httpPost = new HttpPost(url);
		httpPost.setEntity(entity);
		HttpResponse response = httpClient.execute(httpPost, localContext);
		if (response.getStatusLine().getStatusCode() == 200) {
			 System.out.println("OK!");
		} else {
			throw new Exception(String.valueOf(response.getStatusLine()
					.getStatusCode()));
		}
	}

	/**
	 * 获取组织形象设置
	 * 
	 * @return json字符串
	 * @throws Exception
	 */
	public String getVi(String group) throws Exception {
		String url = getUrl("vi_getusergroupvi", "group=" + group);
		return send(url, null);
	}
	
	public String getGroupList(String user) throws Exception {
		String url = getUrl("GetUserGroupList","user="+ user);
		return send(url, null);
	}
	
	public String deleteVi(String group) throws Exception {
		String url = getUrl("vi_deletegroupvi", "group=" + group);
		return send(url, null);
	}
	
	

}
