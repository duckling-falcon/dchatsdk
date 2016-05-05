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
package net.duckling.dchat.rest.user;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import net.duckling.dchat.exception.RTPServerException;
import net.duckling.dchat.utils.Config;
import net.duckling.dchat.utils.JSONResult;
import net.duckling.dchat.utils.RTPCharUtils;
import net.duckling.dchat.utils.RtpServerUtils;
import net.duckling.dchat.utils.UserUtils;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.rooyeetone.rtp.sdk.IDataItem;
import com.rooyeetone.rtp.sdk.IPagedResult;
import com.rooyeetone.rtp.sdk.IRtpSvc;
import com.rooyeetone.rtp.sdk.RtpSvc;

/**
 * 第三方推送消息到RTP Server的Rest接口
 * @author Yangxp
 * @since 2012-06-13
 */
@Controller
@RequestMapping("/v1/message/{appname}")
public class PubSubMessageController {
	
	private static final Logger LOG = Logger.getLogger(PubSubMessageController.class);
	
	private static Set<String> allowedApps = new HashSet<String>();
	static{
		String apps = Config.getProperty("rtp.message.appname");
		for(String app : apps.split(",")){
			allowedApps.add(app);
		}
	}
	/**
	 * 推送消息到单个用户
	 * @param request
	 * @param appname 推送消息的应用名
	 * @param fromUser 消息来源用户
	 * @param toUser 消息接收者
	 * @param subject 主题
	 * @param body 消息内容 （可选）
	 * @param htmlBody 消息内容的html格式  （可选）
	 * @return 成功与否的JSON信息
	 */
	@RequestMapping(value = "/user", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject push2User(HttpServletRequest request, @PathVariable String appname, 
			@RequestParam("from") String fromUser, @RequestParam("to") String toUser,
			@RequestParam("subject") String subject, 
			@RequestParam(value = "body", required = false) String body,
			@RequestParam(value = "htmlBody", required = false) String htmlBody){
		JSONObject result = checkAppAuth(appname);
		if(null == result){
			try {
				IRtpSvc rtp = RtpSvc.getInstance(request);
				String from = UserUtils.escapeCstnetId(fromUser)+"@cstnet.cn";
				
				String to = UserUtils.escapeCstnetId(toUser)+"@cstnet.cn";
				List<String> toList = new ArrayList<String>();
				toList.add(to);
				
				pushMessage(rtp, from, toList, subject, body, getHtmlBody2());
			} catch (Exception e) {
				LOG.error(e.getMessage(), e);
				return JSONResult.fail(e.getMessage(), null);
			}
			result = JSONResult.success("push message success", null);
		}
		return result;
	}
	/**
	 * 推送消息到单个群组
	 * @param request
	 * @param appname 推送消息的应用名
	 * @param fromUser 消息来源用户
	 * @param toGroup 消息接收的群组
	 * @param subject 主题
	 * @param body 消息内容 （可选）
	 * @param htmlBody 消息内容的html格式  （可选）
	 * @return 成功与否的JSON信息
	 */
	@RequestMapping(value = "/group", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject push2Group(HttpServletRequest request, @PathVariable String appname,
			@RequestParam("from") String fromUser, @RequestParam("to") String toGroup,
			@RequestParam("subject") String subject, 
			@RequestParam(value = "body", required = false) String body,
			@RequestParam(value = "htmlBody", required = false) String htmlBody){
		JSONObject result = checkAppAuth(appname);
		if(null == result){
			try {
				IRtpSvc rtp = RtpSvc.getInstance(request);
				String from = UserUtils.escapeCstnetId(fromUser);
				
				List<String> toList = getGroupMembers(rtp, toGroup, from);
				
				pushMessage(rtp, from, toList, subject, body, getHtmlBody2());
			} catch (Exception e) {
				LOG.error(e.getMessage(), e);
				return JSONResult.fail(e.getMessage(), null);
			}
			result = JSONResult.success("push message success", null);
		}
		return result;
	}
	/**
	 * 推送消息到单个组织, 暂不支持
	 * @param request
	 * @param appname 推送消息的应用名
	 * @param fromUser 消息来源用户
	 * @param toOrg 消息接收的组织路径名
	 * @param subject 主题
	 * @param body 消息内容 （可选）
	 * @param htmlBody 消息内容的html格式  （可选）
	 * @return 成功与否的JSON信息
	 */
	@RequestMapping(value = "/org", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject push2Orgnization(HttpServletRequest request, @PathVariable String appname,
			@RequestParam("from") String fromUser, @RequestParam("to") String toOrg,
			@RequestParam("subject") String subject, 
			@RequestParam(value = "body", required = false) String body,
			@RequestParam(value = "htmlBody", required = false) String htmlBody){
		JSONObject result = checkAppAuth(appname);
		if(null == result){
			try {
				IRtpSvc rtp = RtpSvc.getInstance(request);
				String from = UserUtils.escapeCstnetId(fromUser)+"@"+RtpServerUtils.RTP_SERVER_DOMAIN;
				String to = (toOrg.endsWith("/"))?toOrg:(toOrg+"/");
				rtp.exec("IMSend", RTPCharUtils.escapeNode(from), RTPCharUtils.escapeNode(to), subject, body, htmlBody, "normal");
			} catch (Exception e) {
				LOG.error(e.getMessage(), e);
				return JSONResult.fail(e.getMessage(), null);
			}
			result = JSONResult.success("push message success", null);
		}
		return result;
	}
	/**
	 * 检查应用是否被允许推送消息
	 * @param appname
	 * @return
	 */
	private JSONObject checkAppAuth(String appname){
		JSONObject result = null;
		boolean isBlank = StringUtils.isBlank(appname);
		if(isBlank || (!isBlank && !allowedApps.contains(appname))){
			String message = "Application is not allowed to push message to RTP Server";
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("appname", appname);
			result = JSONResult.fail(message, params);
		}
		return result;
	}
	
	private void pushMessage(IRtpSvc rtp, String from, List<String> toList,
			String subject, String body, String htmlBody) throws RTPServerException{
		for(String to : toList){
			try{
				rtp.exec("IMSend", RTPCharUtils.escapeNode(from), RTPCharUtils.escapeNode(to), subject, body, htmlBody, "normal");
			}catch(Exception e){
				throw new RTPServerException(e);
			}
		}
	}
	
	private List<String> getGroupMembers(IRtpSvc rtp, String groupID, String from) 
			throws RTPServerException{
		List<String> members = new ArrayList<String>();
		try{
			if(null != rtp && StringUtils.isNotBlank(groupID) 
					&& StringUtils.isNotBlank(from)){
				String groupJID = RtpServerUtils.getGroupJIDFromID(groupID);
				IPagedResult result = rtp.exec("GroupChat.GetMembers", groupJID);
				List<IDataItem> items = result.getItems();
				for(IDataItem item : items){
					String userJID = (String)item.getProp("jid");
					if(!from.equals(userJID)){
						members.add(userJID);
					}
				}
			}
		}catch(Exception e){
			throw new RTPServerException(e);
		}
		return members;
	}
	
	private String getHtmlBody2(){
		return "<body>" + "<img src=\"http://static.oschina.net/uploads/user/582/1165356_50.jpg?t=1370571557000\"/><p>hahahahahaha</p>" +
				"</body>";
	}
}
