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

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.duckling.dchat.utils.DChatConstants;
import net.duckling.dchat.utils.RTPCharUtils;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpStatus;
import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.rooyeetone.rtp.sdk.IRtpSvc;
import com.rooyeetone.rtp.sdk.RtpSvc;

/**
 * 第三方聊天时引用的JS，聊天页面嵌入等
 * @author Yangxp
 * @since 2012-06-25
 */
@Controller
@RequestMapping("/v1/chat")
public class ThirdPartyChatController {
	private static final Logger LOG = Logger.getLogger(ThirdPartyChatController.class);
	private static final String KEY_PAGE_URL = "pageurl";
	
	@RequestMapping(value = "/display", method = RequestMethod.GET)
	public ModelAndView display(){
		ModelAndView mv = new ModelAndView("chat");
		mv.addObject("profile", DChatConstants.getProfile());
		return mv;
	}
	/**
	 * 获取RTP的js和css标签
	 * @param request
	 * @param response
	 */
	@RequestMapping(value = "/js", method = RequestMethod.GET)
	public void getRefJs(HttpServletRequest request, HttpServletResponse response){
		try {
			IRtpSvc rtpsvc = RtpSvc.getInstance(request);
			String jsContent = rtpsvc.exec("RefJS", "");
			jsContent = transferURL(jsContent, request);
			response.getWriter().write(jsContent);
		} catch (Exception e) {
			LOG.error(e);
			response.setStatus(HttpStatus.SC_NOT_FOUND);
		}
	}
	/**
	 * 获取RTP的web在线状态按钮
	 * @param request
	 * @param response
	 * @param params
	 */
	@ResponseBody
	@RequestMapping(value = "/talkbtn", method = RequestMethod.GET)
	public JSONObject getTalkButton(HttpServletRequest request, HttpServletResponse response,
			@RequestParam("userTo")String userTo, @RequestParam("userFrom")String userFrom){
		
		try {
			IRtpSvc rtpsvc = RtpSvc.getInstance(request);
			String pageParams = request.getParameter("pageParams");
			String btnContent = rtpsvc.exec("GetTalkButton", userTo, pageParams, "", userFrom);
			btnContent = transferURL(btnContent, request);
			JSONObject json = new JSONObject();
			json.put("result", btnContent);
			return json;
		} catch (Exception e) {
			LOG.error(e);
			response.setStatus(HttpStatus.SC_NOT_FOUND);
		}
		return null;
	}
	
	/**
	 * 聊天嵌入页面
	 * @param request
	 * @param response
	 * @param params
	 * @throws IOException
	 */
	@RequestMapping(value="/page", method = RequestMethod.GET)
	public ModelAndView sendChatPage(HttpServletRequest request, HttpServletResponse response,
			@RequestParam(required = true) Map<String, String> params) throws IOException{
		String talkWith = request.getParameter("talk_with");
		Set<String> excludeParams = new HashSet<String>();
		excludeParams.add("talk_with");
		excludeParams.add(KEY_PAGE_URL);
		if(StringUtils.isBlank(talkWith)){
			LOG.error("Embed_chatfocus error! talk_with = "+talkWith);
		}
		ModelAndView mv = new ModelAndView("show");
		mv.addObject("stype",params.get("stype"));
		mv.addObject("indexURL",params.get("indexURL"));
		mv.addObject("telephone",params.get("telephone"));
		mv.addObject("content", request.getParameter("content"));
		mv.addObject("name",params.get("name"));
		return mv;
	}
	
	private String transferURL(String content, HttpServletRequest request){
		String path = request.getContextPath();
		if(StringUtils.isBlank(path) || path.equals("/")){
			path = "/rtp";
		}
		String baseURL = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+path;
		String result = content;
		result = result.replace("JavaScript", "\"javascript\"");
		return result.replace(path, baseURL);
	}
	
	/*
	 * Roster.AddFriend 
		为指定帐户添加私有好友。当both参数为false或未指定时，添加操作完成后，UserTo指定的对方会收到一个确认请求；否则会尝试自动将双方互相加为好友。具体参考both参数的说明。 
		参数 
		string user必填，指定当前帐户 
		string to 必填，指定添加的私有好友，值可以是客户应用的用户名或JID 
		string nickname 可选, 指定好友的显示昵称 
		string groups 可选,指定好友的分组 
		bool both 可选, 为true，并且to为RTP帐户(而不是其他如gtalk帐户)时，会尝试自动将双方加为好友，而不需要接收方主动确认。为false或未指定时，添加操作完成后，to指定的对方会收到一个确认请求 
		string toNickName 可选,both为true时，用来指定当前帐户在目标帐户好友列表中的昵称 
		string toGroups 可选, both为true时，用来指定当前帐户在目标帐户好友列表中的分组 
		返回值 无 
		错误码:
		30001: user参数所指定的用户不存在
		30006: to参数所指定的用户已经存在于好友列表中
	 */
	
	@ResponseBody
	@RequestMapping(value="/open")
	public JSONObject addPublicAccount(HttpServletRequest request, HttpServletResponse response){
		try {
			String username = (String) request.getSession().getAttribute(DChatConstants.CURRENT_USER);
			String publicAccount = request.getParameter("publicAccount");
			IRtpSvc rtpsvc = RtpSvc.getInstance(request);
			String btnContent = rtpsvc.exec("Roster.AddFriend",RTPCharUtils.escapeNode(username), RTPCharUtils.escapeNode(publicAccount));
			JSONObject json = new JSONObject();
			json.put("result", btnContent);
			return json;
		} catch (Exception e) {
			LOG.error(e);
			response.setStatus(HttpStatus.SC_NOT_FOUND);
		}
		return null;
	}
}
