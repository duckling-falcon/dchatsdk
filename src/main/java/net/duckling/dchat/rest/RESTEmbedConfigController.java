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
package net.duckling.dchat.rest;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import net.duckling.dchat.rest.user.EmbedConfig;
import net.duckling.dchat.rest.user.EmbedConfigService;
import net.duckling.dchat.utils.DChatConstants;

import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.util.WebUtils;

import com.rooyeetone.rtp.sdk.IRtpSvc;
import com.rooyeetone.rtp.sdk.RtpSvc;

@Controller
@RequestMapping(value = "/rest/ec")
public class RESTEmbedConfigController {
	
	@Autowired
	private EmbedConfigService configService;
	
	@ResponseBody
	@RequestMapping(value = "/create", method = RequestMethod.PUT)
	public List<EmbedConfig> createConfig(@RequestBody EmbedConfig config, HttpServletRequest request) {
		String sPage = WebUtils.findParameterValue(request, "page");
		String sPageSize = request.getParameter("pageSize");
		int page = Integer.parseInt(sPage);
		int pageSize = Integer.parseInt(sPageSize);
		configService.save(config);
		return configService.getConfigList(page, pageSize);
	}

	@ResponseBody
	@RequestMapping(value = "/read", method = RequestMethod.GET)
	public List<EmbedConfig> list() {
		List<EmbedConfig> userList = configService.getConfigList(1, 20);
		return userList;
	}

	@ResponseBody
	@RequestMapping(value = "/update", method = RequestMethod.POST)
	public List<EmbedConfig> updateConfig(@RequestBody EmbedConfig config) {
		configService.update(config);
		return configService.getConfigList(1, 20);
	}

	@ResponseBody
	@RequestMapping(value = "/delete", method = RequestMethod.DELETE)
	public List<EmbedConfig> deleteConfig(@RequestParam("id") int id, @RequestParam("token")String token) {
		System.out.println(token);
		configService.delete(id);
		return configService.getConfigList(1, 20);
	}
	
	@ResponseBody
	@RequestMapping(value = "/loginbytoken", method = RequestMethod.POST)
	public String loginByToken(@RequestParam("user") String user,
		@RequestParam("token")String token, HttpServletRequest request) throws Exception {
		System.out.println(request.getSession().getId());
		IRtpSvc rtp = RtpSvc.getInstance(request);
		rtp.exec("IMLogin", user);
		request.getSession().setAttribute(DChatConstants.CURRENT_USER, user );
		return "Success";
	}
	
	public static void main(String[] args){
		String token = "//liji\\40cstnet.cn@rtp.escience.cn/"
				+ "yangxuantest22\\40cstnet.cn@rtp.escience.cn"
				+ "?message;pass=utoken_123456";
		byte[] bts = Base64.encodeBase64(token.getBytes());
		StringBuffer sb = new StringBuffer();
		for(byte b:bts){
			sb.append((char)b);
		}
		System.out.println(sb.toString());
		System.out.println(getRtpURL("//liji\\40cstnet.cn@rtp.escience.cn"));
	}
	
	public static String getRtpURL(String user){
		String template = user+"//login";
		byte[] bts = Base64.encodeBase64(template.getBytes());
		StringBuffer sb = new StringBuffer();
		for(byte b:bts){
			sb.append((char)b);
		}
		
		return sb.toString();
	}


}
