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

@Controller
@RequestMapping("/v1/pub")
public class PublicAccountController {
	private static final Logger LOG = Logger.getLogger(PublicAccountController.class);
	
	@RequestMapping(value = "/display", method = RequestMethod.GET)
	public ModelAndView display(){
		ModelAndView mv = new ModelAndView("public-account");
		mv.addObject("profile", DChatConstants.getProfile());
		return mv;
	}
	
	@RequestMapping(value="/slow", method = RequestMethod.GET)
	public ModelAndView slow(){
		ModelAndView mv = new ModelAndView("public-account");
		try {
			Thread.sleep(120000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return mv;
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
	
	
	@ResponseBody
	@RequestMapping(value="/open")
	public JSONObject addPublicAccount(HttpServletRequest request, HttpServletResponse response){
		try {
			String username = (String) request.getSession().getAttribute(DChatConstants.CURRENT_USER);
			String publicAccount = request.getParameter("publicAccount");
			IRtpSvc rtpsvc = RtpSvc.getInstance(request);
			String btnContent = rtpsvc.exec("Roster.AddFriend", RTPCharUtils.escapeNode(username), RTPCharUtils.escapeNode(publicAccount));
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
