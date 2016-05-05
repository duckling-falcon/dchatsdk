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

import net.duckling.dchat.utils.DChatConstants;

import org.apache.log4j.Logger;

import com.rooyeetone.rtp.sdk.IEvent;

/**
 * @author clivelee
 *
 */
public class StaticButton extends LinkButton {
	
	private static final Logger LOG = Logger.getLogger(StaticButton.class);
	
	/**
	 * @return 		点击按钮时触发的连接地址。推在需要免登录的链接后面加上参数"?rtplink=WEBID" 
	 */
	public String getAccessURL(String contextPath) {
		String fullPath = DChatConstants.getDomain() + contextPath;
		if(Device.WEB.toString().equalsIgnoreCase(deviceos)){			
			return fullPath + "/v1/notify/access/" + appid + "?rtplink=" + DChatConstants.getWebid()+"&device=" + this.deviceos;
		}
		return fullPath + "/v1/notify/access/" + appid + "?device=" + this.deviceos;
	}

	public Object getButtonObject(IEvent event,String contextPath) {
		try {
			if(refreshURL == null){
				return event.getRtpSvc().exec("CreateButton",deviceos +"_"+ imageKey, 
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
	
	private StaticButton(){}
	
	public static StaticButton buildInstance(String appid,String imageKey,StaticButton.Device device,String tooltip){
		StaticButton bp = new StaticButton();
		bp.appid = appid;
		bp.imageKey = imageKey;
		bp.deviceos = device.toString();
		bp.tooltip = tooltip;
		bp.buttonPlace = ButtonPlace.TOOLBAR.toString();
		bp.linkType = LinkType.BROWSER.toString();
		bp.refreshURL = null;
		bp.features = "";
		return bp;
	}
	
	public StaticButton fillRefreshURL(String refreshURL){
		this.refreshURL = refreshURL;
		return this;
	}
	
	public StaticButton fillButtonType(ButtonPlace buttonPlace){
		this.buttonPlace = buttonPlace.toString();
		return this;
	}
}
