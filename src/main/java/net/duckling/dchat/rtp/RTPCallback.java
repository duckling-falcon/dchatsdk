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

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import net.duckling.dchat.umt.UmtClient;
import net.duckling.dchat.utils.DChatConstants;
import net.duckling.dchat.utils.RTPCharUtils;
import net.duckling.dchat.utils.RtpServerUtils;
import net.duckling.dchat.utils.UserUtils;
import net.duckling.dchat.vmt.LdapReader;
import net.duckling.dchat.vmt.RTPVmtUser;
import net.duckling.dchat.vmt.VmtClient;
import net.duckling.vmt.api.IRestUserService;
import net.duckling.vmt.api.domain.VmtApiApp;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.rooyeetone.rtp.sdk.ICallback;
import com.rooyeetone.rtp.sdk.IEvent;
import com.rooyeetone.rtp.sdk.ISyncItem;
/**
 * RTP SDK Server必须实现的接口，提供用户源同步、认证以及客户端功能增加等
 * 注意RTPCallback中不能有任何private变量做数据共享，
 * 因为RTPCallback每次调用都是重新new的一个新对象
 * @author Yangxp
 * @since 2012-06-14
 */
public class RTPCallback implements ICallback {
	
	private static final Logger LOG = Logger.getLogger(RTPCallback.class);
	private static final String USER_HEAD_STR = "User ";
	private UmtClient umt = UmtClient.getInstance(); //umtclient是单例的，可以持有
	private VmtClient vmt = VmtClient.getInstance();
	
	@Override
	public void embedChatfocus(IEvent event, String talkwith, String params) throws Exception {
		String username = (String)event.getHttpRequest().getSession().getAttribute(DChatConstants.CURRENT_USER);
		LOG.info("User ["+ username +"] want to talk with ["+ talkwith +"]");
		String basePath = event.getHttpRequest().getContextPath();
		String encodeParams = "";
		if(StringUtils.isNotEmpty(params)){
			StringBuilder sb = new StringBuilder();
			String[] kvpairs = params.split("&");
			for(String kvp:kvpairs){
				int index = kvp.indexOf('=');
				String val = URLEncoder.encode(kvp.substring(index+1), "UTF-8");
				String key = kvp.substring(0, index);
				sb.append(key+"="+val+"&");
			}
			if(sb.length()>1){				
				encodeParams = sb.substring(0,sb.length()-1);
			}
		}
		event.getHttpResponse().sendRedirect(basePath+"/v1/chat/page?talk_with="+talkwith+"&"+encodeParams);
	}

	@Override
	public void embedConsole(IEvent event, String tabid) throws Exception {
		String username = (String)event.getHttpRequest().getSession().getAttribute(DChatConstants.CURRENT_USER);
		if(StringUtils.isNotBlank(tabid) && tabid.equals("email")){
			String url = "http://mail.cstnet.cn";
			LOG.info("User ["+ username +"] access tab[邮箱服务]");
			event.getHttpResponse().sendRedirect(url+"?tabid="+tabid);
		}
		if(StringUtils.isNotBlank(tabid) ){
			if(tabid.equals("prefs")){
				LOG.info("User ["+ username +"] access tab[偏好设置]");
				event.getHttpResponse().sendRedirect(DChatConstants.getDomain()+"/dchat/v1/preferences");
			}else if(tabid.equals("cs")){
				LOG.info("User ["+ username +"] access tab[公共服务]");
				event.getHttpResponse().sendRedirect(DChatConstants.getDomain()+"/dchat/v1/chat/display");
			}else if(tabid.equals("pubs")){
				LOG.info("User ["+ username +"] access tab[公共帐号]");
				event.getHttpResponse().sendRedirect(DChatConstants.getDomain()+"/dchat/v1/pub/display");
			}
		}
	}

	@Override
	public void initParams(IEvent event, @SuppressWarnings("rawtypes") HashMap arg1) throws Exception {
		LOG.info("initParams");
	}
	/**
	 * 实现第三方认证功能
	 */
	@Override
	public boolean login(IEvent event, String username, String password, boolean authOnly)
			throws Exception {
		if(password.startsWith("utoken_")){
			String embedToken = password.replace("utoken_", "");
			if(umt.consumeEmbedToken(username, embedToken)){
				event.getHttpRequest().getSession().setAttribute(DChatConstants.CURRENT_USER, username );
				return true;
			}else{
				LOG.info(USER_HEAD_STR + username +" login failed, because of invalid embed token!");
				return false;
			}
		}
		boolean flag = true;
		String un = UserUtils.unescapeCstnetId(username);
		flag = umt.login(un, password);
		if(flag){
			LOG.info(USER_HEAD_STR+username+" login, authenticate successfully from UMT!");		        
			event.getHttpRequest().getSession().setAttribute(DChatConstants.CURRENT_USER, username );
			LOG.info("sessionid:"+event.getHttpRequest().getSession().getId()+",currentUser:"+username+",ip:"+event.getHttpRequest().getRemoteAddr());
		}else{
			LOG.info(USER_HEAD_STR+username+" login failed!");
		}
		return flag;
	}

	@SuppressWarnings("deprecation")
	@Override
	public void logout(IEvent event) throws Exception {
		String username = event.getRtpSvc().currentUser();
		username = UserUtils.unescapeCstnetId(username);
		if(null != username){
			umt.removeLoginCache(username);
		}
		LOG.info(USER_HEAD_STR+username+" logout");
	}

	/**
	 * RTP用户属性信息更新后，会调用该方法回写到第三方系统
	 */
	@Override
	public void onUserData(IEvent event, String username) throws Exception {
	}

	
	/**
	 * 在RTP客户端增加标签页、快捷方式等
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void onUserProps(IEvent event, String username, @SuppressWarnings("rawtypes") HashMap props) throws Exception {
		HttpServletRequest req = event.getHttpRequest();
		String contextPath = req.getContextPath();
		String refreshEmailNoticeURL = DChatConstants.getDomain() + req.getContextPath() +"/v1/notify/email?user="+username;
		String refreshDDLNoticeURL = DChatConstants.getDomain() + req.getContextPath() +"/v1/notify/ddlnotice?user="+username;
		List<Object> btns = new ArrayList<Object>();
		String device = event.getHttpRequest().getParameter("deviceos"); //获取终端类型
		StaticButton.Device currDevice = StaticButton.Device.WEB;
		if("Android".equalsIgnoreCase(device)) {
			currDevice = StaticButton.Device.ANDROID;
		}else if("IPad".equalsIgnoreCase(device) || "IPhone".equalsIgnoreCase(device)){
			currDevice = StaticButton.Device.IOS;
		}else{
			currDevice = StaticButton.Device.WEB;
		}
		btns.add(StaticButton.buildInstance("vmt","vmt_addgroup", currDevice,"创建群")
				.getButtonObject(event, contextPath));
		if(umt.isCoreMailUser(username)){			
			btns.add(StaticButton.buildInstance("coremail","cstnet_mail", currDevice,"中科院邮箱")
					.fillRefreshURL(refreshEmailNoticeURL+"&device="+currDevice.toString())
					.getButtonObject(event, contextPath));
		}
		btns.add(StaticButton.buildInstance("ddl","ddl", currDevice,"团队文档库")
				.fillRefreshURL(refreshDDLNoticeURL)
				.getButtonObject(event, contextPath));
		btns.add(StaticButton.buildInstance("dhome","dhome", currDevice,"科研主页")
				.getButtonObject(event, contextPath));
		btns.add(StaticButton.buildInstance("cos","service", currDevice,"用户服务中心")
				.getButtonObject(event, contextPath));
		
		IRestUserService us = vmt.getVmtService().getVmtUserService();
		List<VmtApiApp> result = us.getAppsByUmtId(umt.getUmtId(username));
		for(VmtApiApp app:result){
			String pcLogo = app.getLogoCustomUrl() != null ? (app.getLogoCustomUrl()):(app.getLogo100Url());
			String androidLogo = app.getLogo64Url();
			String iosLogo = app.getLogo32Url();
			btns.add(DynamicButton.buildInstance(
					app.getAppName(), pcLogo.trim(), androidLogo.trim(), iosLogo.trim(),
					app.getAppClientUrl(), app.getAppMobileUrl(),
					currDevice.toString(), app.getAppName()).getButtonObject(event, contextPath));
		}
		
		props.put("buttons", btns);
		ArrayList<Object> tabs = new ArrayList<Object>();
		tabs.add(event.getRtpSvc().exec("createTab", "cs", "公共服务", "perclick"));
		if(umt.isCoreMailUser(username)){
			if(username.endsWith("cstnet.cn")){ //内测阶段只对cstnet.cn的人开放
				tabs.add(event.getRtpSvc().exec("createTab", "pubs", "公共账号", "perclick"));
			}
			tabs.add(event.getRtpSvc().exec("createTab", "prefs", "偏好设置", "perclick"));
		}
		props.put("tabs", tabs);// 设置客户端可显示的嵌入标签页列表
	}

    /**
	 * 从第三方系统同步用户信息到RTP，也包含组织信息
	 */
	@Override
	public void syncUsers(IEvent event) throws Exception {
		LOG.info("Synchronize user data from VMT to RTP Server, Start .........");
		long start = System.currentTimeMillis();
		List<RTPVmtUser> users = LdapReader.getInstance().getRootOrgUsers();
		long middle = System.currentTimeMillis();
		LOG.info("Read all users from vmt takes "+(middle-start)+" ms, total "+users.size());
		String username = null;
		int i = 0, size = users.size();
		Set<String> unique = new HashSet<String>();
		for(RTPVmtUser user : users){
			++i;
			username = user.getCstnetId();
			if(!unique.contains(username)){
				ISyncItem item = event.getRtpSvc().exec("SyncData",username, "USER", "SYNC");
				item.setProp(RTPVmtUser.RTP_NICKNAME, user.getName());
				item.setProp(RTPVmtUser.RTP_FULLNAME, user.getName());
				item.setProp(RTPVmtUser.RTP_EMAIL, username);
				item.setProp(RTPVmtUser.RTP_DEPT, UserUtils.formatDept(user.getCurrentDisplay()));
				int weight = RtpServerUtils.getRTPSortWeights(user.getSortWeights());
				item.setProp(RTPVmtUser.RTP_SORTWEIGHTS, String.valueOf(weight));
				item.commit();
				unique.add(username);
			}
			if(i % 1000 == 0){
				LOG.info("("+i+"/"+size+")");
				Thread.sleep(5*1000);
			}
		}
		long end = System.currentTimeMillis();
		LOG.info("Write "+unique.size()+" users to rtp takes "+(end-middle)+" ms");
		LOG.info("Synchronize user data from VMT to RTP Server, Finished !");
	}

}
