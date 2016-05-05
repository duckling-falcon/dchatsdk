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

import net.duckling.dchat.exception.RTPServerException;
import net.duckling.dchat.rtp.service.interf.IRTPUserService;
import net.duckling.dchat.umt.UmtClient;
import net.duckling.dchat.utils.DChatConstants;
import net.duckling.dchat.vmt.RTPVmtUser;

import org.apache.commons.lang.StringUtils;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

@Controller
@RequestMapping("/v1/register")
public class RegisterController {
	
	@Autowired
	private IRTPUserService rtpUserService;

	@RequestMapping
	public ModelAndView display(HttpServletRequest request) {
		ModelAndView mv =  new ModelAndView("register");
		mv.addObject("umtRegistURL", DChatConstants.getPassportURL()+"/regist.jsp");
		return mv;
	}
	
	/**
	 * 
	 * @param request
	 * @param response
	 */
	@RequestMapping(params="func=validate")
	@ResponseBody
	public JSONObject validate(HttpServletRequest request){
		JSONObject re = new JSONObject();
		String username = request.getParameter("username");
		String password = request.getParameter("password");
		String checkCode = request.getParameter("checkcode");
		String type = request.getParameter("type");
		boolean result = checkCode(request, checkCode, type, false);
		if(!result){
			re.put("status", "checkCodeError"); //验证码错误
			return re;
		}
		UmtClient umt = UmtClient.getInstance();
		boolean flag = umt.login(username, password);
		if(!flag){
			re.put("status", "passwordError"); //密码错误
			return re;
		}
		re.put("status", "success");
		return re;
	}
	
	/**
	 * 验证request中保存的验证码是否正确，如果验证错误移除验证码，重新生成
	 * @param request
	 * @param code
	 * @param type
	 * @param reflesh true 去掉type类型session验证码，false不移除
	 * @return
	 */
	private boolean checkCode(HttpServletRequest request, String code,String type,boolean reflesh){
		if(StringUtils.isEmpty(type)||StringUtils.isEmpty(code)){
			return false ;
		}
		String c = (String)request.getSession().getAttribute(type);
		if(!StringUtils.isEmpty(c)&&c.equalsIgnoreCase(code)){
			if(reflesh){
				request.getSession().removeAttribute(type);
			}
			return true;
		}else{
			request.getSession().removeAttribute(type);
			return false ;
		}
		
	}

	@RequestMapping(params = "func=activate")
	public ModelAndView activate(@RequestParam("username") String username,
			@RequestParam("password") String password) throws RTPServerException {
		UmtClient umt = UmtClient.getInstance();
		boolean flag = umt.login(username, password);
		if(flag){
			//add record into rtp
			ModelAndView mv = new ModelAndView(new RedirectView("/dchat/v1/register/handler"));
			RTPVmtUser u = rtpUserService.getUser(username);
			if(u == null){				
				rtpUserService.addDefaultUser(username);
				mv.addObject("status", "success");
			} else {
				mv.addObject("status", "existed");
			}
			mv.addObject("username",username);
			return mv;
		} else {
			//error handlers 
			ModelAndView mv = new ModelAndView(new RedirectView("/dchat/v1/register"));
			mv.addObject("username",username);
			mv.addObject("status", "error");
			mv.addObject("message", "This account don't exist or password is not right");
			return mv;
		}
	}
}
