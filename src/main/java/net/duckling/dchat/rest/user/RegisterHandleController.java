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

import net.duckling.dchat.utils.DChatConstants;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@RequestMapping("/v1/register/handler")
@Controller
public class RegisterHandleController {
	@RequestMapping
	public ModelAndView success(@RequestParam("status") String status,
			@RequestParam("username") String username,HttpServletRequest request) {
		ModelAndView mv = new ModelAndView("register_handler");
		mv.addObject("username", username);
		mv.addObject("status", status);
		if (!"success".equals(status)) {
			String message = request.getParameter("message");
			mv.addObject("message", message);
		}
		mv.addObject("homeURL", DChatConstants.getDchatPortalURL());
		return mv;
	}
}
