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

import net.duckling.dchat.rtp.domain.UserPrefs;
import net.duckling.dchat.rtp.service.UserPrefsService;
import net.duckling.dchat.utils.DChatConstants;

import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

@Controller("/v1/preferences")
public class UserPreferenceController {

	private static final Logger LOG = Logger.getLogger(UserPreferenceController.class);
	
	@Autowired
	private UserPrefsService ups;

	@RequestMapping
	public ModelAndView display(HttpServletRequest request) {
		String username = (String) request.getSession().getAttribute(DChatConstants.CURRENT_USER);
		if (!StringUtils.isEmpty(username)) {
			ModelAndView mv = new ModelAndView("preferences");
			UserPrefs up = ups.query(username);
			if(up == null){
				ups.createPrefs(username, "1","");
				mv.addObject("switchNotice", "1");
				mv.addObject("filterRule", "");
			}else {	
				mv.addObject("switchNotice", up.getSwitchNotice());
				mv.addObject("filterRule", up.getFilterRule());
			}
			mv.addObject("username", username);
			mv.addObject("dchatDomain", DChatConstants.getDomain());
			return mv;
		}
		return new ModelAndView("preferences");
	}

	@RequestMapping(params = "func=changeConfig")
	@ResponseBody
	public JSONObject validate(HttpServletRequest request, 
			@RequestParam("switchNotice") String switchNotice,
			@RequestParam("filterRule")String filterRule) {
		String username = (String) request.getSession().getAttribute(DChatConstants.CURRENT_USER);
		JSONObject re = new JSONObject();
		if(!StringUtils.isEmpty(username)){
			re.put("switchNotice", switchNotice);
			UserPrefs old = ups.query(username);
			int flag = ups.updatePrefs(username, switchNotice,filterRule);
			if (flag == 1) {
				LOG.info(username+" change prefs from ["+ old.getSwitchNotice()+"],["+old.getFilterRule()+"] to "
						+ "["+switchNotice+"],["+filterRule+"]");
				re.put("status", "success");
			}else{
				re.put("status", "failed");
			}
		}else {
			re.put("status", "failed");
		}
		return re;
	}
}
