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
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import net.duckling.dchat.utils.DChatConstants;
import net.duckling.dchat.utils.LogoUtils;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.rooyeetone.rtp.sdk.IEvent;

/**
 * @author Administrator
 *
 */
public class DynamicButton extends LinkButton {
	
	private static final Logger LOG = Logger.getLogger(DynamicButton.class);
	
	public String getAppid() {
		return appid;
	}
	
	/**
	 * @param appClientURL
	 * @description 桌面客户端访问URL
	 */
	private String appClientURL; 
	
	/**
	 * @param appMobileURL
	 * @description 移动客户端访问URL
	 */
	private String appMobileURL;
	
	private String clientImage;
	
	private String androidImage;
	
	@SuppressWarnings("unused")
	private String iosImage;
	
	
	/**
	 * @return 	点击按钮时触发的连接地址。推在需要免登录的链接后面加上参数"?rtplink=WEBID" 
	 */
	public String getAccessURL(String contextPath) {
		try {
			String fullPath = DChatConstants.getDomain() + contextPath;
			if (Device.WEB.toString().equalsIgnoreCase(deviceos)) {
				checkAndDownloadLogoFile(clientImage);
				return fullPath + "/v1/apps?id=" + URLEncoder.encode(appid,"UTF-8") + "&rtplink=" + DChatConstants.getWebid() + "&device="
						+ this.deviceos + "&redirect=" + URLEncoder.encode(this.appClientURL, "UTF-8");
			}else{
				checkAndDownloadLogoFile(androidImage);
				if(StringUtils.isNotEmpty(appMobileURL)){
					return fullPath + "/v1/apps?id=" + URLEncoder.encode(appid,"UTF-8") + "&device=" + this.deviceos + "&redirect="
							+ URLEncoder.encode(this.appMobileURL, "UTF-8");
				}else{
					return fullPath + "/v1/apps?id=" + URLEncoder.encode(appid,"UTF-8") + "&device=" + this.deviceos + "&redirect="
							+ URLEncoder.encode(this.appClientURL, "UTF-8");
				}
			}
		} catch (UnsupportedEncodingException e) {
			LOG.error(e.getMessage(),e);
		}
		return null;
	}

	private void checkAndDownloadLogoFile(String clientImage) {
		String imageName = "btn_" + this.deviceos + "_" + this.imageKey + ".png";
		String fullPath = DChatConstants.getWebRootDir() + "rtp" + File.separator + "images" + File.separator
				+ imageName;
		File f = new File(fullPath);
		String logoName = this.deviceos + "_" + this.appid;
		if (f.exists()) {
			LOG.info("The logo named [" + logoName  + "] already exist");
		} else {
			LogoUtils.fetchRemoteLogoFile(clientImage, fullPath);
			LOG.info("Download dynamic button logo named [" + logoName + "] from vmt");
		}
	}

	
	public Object getButtonObject(IEvent event,String contextPath) {
		try {
			if(refreshURL == null){
				return event.getRtpSvc().exec("CreateButton", deviceos +"_"+ imageKey, 
						tooltip,getAccessURL(contextPath), linkType, features,
						buttonPlace);
			} else {
				return event.getRtpSvc().exec("CreateButton",deviceos +"_"+ imageKey, 
						tooltip,getAccessURL(contextPath), linkType, features,
						buttonPlace, refreshURL);
			}
		} catch (Exception e) {
			LOG.error(e.getMessage(),e);
		}
		return null;
	}
	
	private DynamicButton(){}
	
	public static DynamicButton buildInstance(String appid, String clientImage, String androidImage, String iosImage, String clientURL,
			String mobileURL, String device, String tooltip) {
		DynamicButton bp = new DynamicButton();
		bp.appid = appid;
		if("web".equals(device)){			
			bp.imageKey = DigestUtils.md5Hex(clientImage);
		} else if("android".equals(device)) {
			bp.imageKey = DigestUtils.md5Hex(androidImage);
		} else {
			bp.imageKey = DigestUtils.md5Hex(iosImage);
		}
		bp.clientImage = clientImage;
		bp.androidImage = androidImage;
		bp.appClientURL = clientURL;
		bp.appMobileURL = mobileURL;
		bp.deviceos = device;
		bp.tooltip = tooltip;
		bp.buttonPlace = ButtonPlace.BOTTOMTOOLBAR.toString();
		bp.linkType = LinkType.BROWSER.toString();
		bp.refreshURL = null;
		bp.features = "";
		return bp;
	}
	
	public DynamicButton fillRefreshURL(String refreshURL){
		this.refreshURL = refreshURL;
		return this;
	}
	
	public DynamicButton fillButtonType(ButtonPlace buttonPlace){
		this.buttonPlace = buttonPlace.toString();
		return this;
	}
	
	public DynamicButton fillAppClientURL(String clientURL){
		this.appClientURL = clientURL;
		return this;
	}
	
	public DynamicButton fillAppMobileURL(String mobileURL){
		this.appMobileURL = mobileURL;
		return this;
	}
	
	public static void main(String[] args) {
		//[B@c01e99
		//[B@c01e99
		String abc = "http://vmttest.escience.cn/logo/145?size=small  ";
		System.out.println(Base64.encodeBase64(abc.getBytes()).toString());
	}
}
