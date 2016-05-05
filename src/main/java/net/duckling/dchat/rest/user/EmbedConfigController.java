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

import net.duckling.dchat.utils.Config;
import net.duckling.dchat.utils.DChatConstants;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/v1/embed")
public class EmbedConfigController {

	@Autowired
	private EmbedConfigService configService;

	@RequestMapping(value = "/display", method = RequestMethod.GET)
	public ModelAndView display(@RequestParam("id") int id, @RequestParam("account") String account) {
		ModelAndView mv = new ModelAndView("embed");
		mv.addObject("profile", DChatConstants.getProfile());
		EmbedConfig conf = configService.get(id);
		if (!StringUtils.isEmpty(account) && conf != null) {
			if (account.equals(conf.getAccount())) {
				mv.addObject("account", conf.getAccount());
				mv.addObject("content", conf.getDisplayName());
				mv.addObject("stype", conf.getDuration());
				mv.addObject("indexURL", conf.getIndexURL());
				mv.addObject("telephone", conf.getTelephone());
				mv.addObject("unitName", conf.getUnitName());
				mv.addObject("domain", DChatConstants.getDomain());
				mv.addObject("flag", true);
			} else {
				mv.addObject("flag", false);
			}
		} else {
			mv.addObject("flag", false);
		}
		return mv;
	}
	
	@RequestMapping(value = "/portal", method = RequestMethod.GET)
	public ModelAndView display(HttpServletRequest request) {
		ModelAndView mv = new ModelAndView("embed-portal");
		String state = request.getParameter("state");
		if ("talkto".equals(state)) {
			mv.addObject("state", "talkto");
			mv.addObject("account", request.getParameter("account"));
		} else if ("grouptalk".equals(state)) {
			mv.addObject("state", "grouptalk");
			mv.addObject("groupid", request.getParameter("groupid"));
		} else {
			mv.addObject("state", "login");
		}
		mv.addObject("oauthCallbackURI", Config.getProperty("oauth.dchat.redirect_uri"));
		mv.addObject("oauthClientID", Config.getProperty("oauth.dchat.client_id"));
		mv.addObject("passportDomain", DChatConstants.getPassportURL());
		mv.addObject("profile", DChatConstants.getProfile());
		return mv;
	}

	@RequestMapping(value = "/config", method = RequestMethod.GET)
	public ModelAndView showRegister() {
		ModelAndView mv = new ModelAndView("embed-config");
		mv.addObject("profile", DChatConstants.getProfile());
		mv.addObject("domain", DChatConstants.getDomain());
		return mv;
	}

}
