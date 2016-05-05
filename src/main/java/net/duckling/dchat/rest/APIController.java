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

import net.duckling.dchat.email.CoreMailService;
import net.duckling.dchat.rest.user.EmbedConfigService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping(value = "/rest/api")
public class APIController {
	
	@Autowired
	private EmbedConfigService configService;

	@Autowired
	private CoreMailService coreMailService;
	
	@ResponseBody
	@RequestMapping(value = "/newmailcount", method = RequestMethod.GET)
	public CountMessage query(@RequestParam("user") String user) {
		int count = coreMailService.getUnreadMailsCount(user);
		CountMessage cm = new CountMessage();
		cm.setUser(user);
		cm.setCount(count);
		if(count == -1){
			cm.setMessage("USER NOT FOUND");
		} else {
			cm.setMessage("SUCCESS");
		}
		return cm;
	}

	public static class CountMessage {
		public int getCount() {
			return count;
		}
		public void setCount(int count) {
			this.count = count;
		}
		public String getUser() {
			return user;
		}
		public void setUser(String user) {
			this.user = user;
		}
		public String getMessage() {
			return message;
		}
		public void setMessage(String message) {
			this.message = message;
		}
		private int count;
		private String user;
		private String message;
	}

}
