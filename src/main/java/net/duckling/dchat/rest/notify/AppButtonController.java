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
package net.duckling.dchat.rest.notify;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.duckling.dchat.umt.UmtClient;
import net.duckling.dchat.utils.Config;
import net.duckling.dchat.utils.DChatConstants;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/v1/apps")
public class AppButtonController {
	
	private static final String CHAR_SET = "UTF-8";
	private static final Logger LOG = Logger.getLogger(AppButtonController.class);
	
	@RequestMapping(method = RequestMethod.GET)
	public void access(@RequestParam("id")String appid, @RequestParam("redirect") String redirectURL,
			HttpServletRequest request,HttpServletResponse response){
        String user = (String) request.getSession().getAttribute(DChatConstants.CURRENT_USER);
        LOG.info("sessionid:"+request.getSession().getId()+",currentUser:"+user+",ip:"+request.getRemoteAddr());
		if(StringUtils.isNotEmpty(user) ){
			accessService(user, redirectURL, response);
		} else {
			LOG.warn("Current session user is missing.");
		}
	}
	
	private UmtClient umt = UmtClient.getInstance();
	
	private void accessService(String username, String redirectURL,HttpServletResponse response){
		LOG.info("User["+username+"] want to access service at " + redirectURL);
		String accessToken = umt.getLoginToken(username);
		if(!StringUtils.isEmpty(accessToken)){
			String passportDomain = Config.getProperty("umt.passport.domain");
			String fetchTempTokenURL = passportDomain + "/request/loginToken?action=apply&accessToken="+accessToken;
			long start = System.currentTimeMillis();
			String tempToken = getTempToken(fetchTempTokenURL);
			long end = System.currentTimeMillis();
			LOG.debug("get temp token use time:" + (end - start) + " ms");
			try {
				String finalURL = passportDomain + "/request/loginToken?action=login&token=" + tempToken
						+ "&returnUrl=" + URLEncoder.encode(redirectURL,"UTF-8");
				response.sendRedirect(finalURL);
			} catch (IOException e) {
				LOG.error(e.getMessage(),e);
			}
		}
	}

	/*
	 * 注意现在这种获取tempToken的方式只支持HTTP协议，https协议不支持。
	 */
	private String getTempToken(String fetchTempTokenURL) {
		String token = null;
		URL url = null;
		try {
			url = new URL(fetchTempTokenURL);
		} catch (MalformedURLException e1) {
			LOG.error(e1.getMessage(),e1);
		}
		InputStream ins = null;
		BufferedReader reader = null;
		try {
			ins = url.openConnection().getInputStream();
			reader = new BufferedReader(new InputStreamReader(ins, CHAR_SET));
			String line;
			StringBuffer sb = new StringBuffer();
			while ((line = reader.readLine()) != null) {
				sb.append(line);
			}
			JSONParser parser = new JSONParser();
			JSONObject obj = (JSONObject)parser.parse(sb.toString());
			token = (String)obj.get("token");
		} catch (IOException | ParseException  e) {
			LOG.error(e.getMessage(),e);
		} finally {
			IOUtils.closeQuietly(reader);
			IOUtils.closeQuietly(ins);
		}
		return token;
	}

}
