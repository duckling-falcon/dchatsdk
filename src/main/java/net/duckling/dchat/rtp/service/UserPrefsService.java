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
package net.duckling.dchat.rtp.service;

import java.util.Date;

import net.duckling.dchat.rtp.dao.UserPrefsDAO;
import net.duckling.dchat.rtp.domain.UserPrefs;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserPrefsService {
	@Autowired
	private UserPrefsDAO upo;
	
	public int createPrefs(String username, String switchNotice, String filterRule){
		UserPrefs p = new UserPrefs();
		p.setUsername(username);
		p.setSwitchNotice(switchNotice);
		p.setFilterRule(filterRule);
		p.setUpdateTime(new Date());
		return upo.insert(p);
	}
	
	public int updatePrefs(String username, String switchNotice, String filterRule){
		UserPrefs p = new UserPrefs();
		p.setUsername(username);
		p.setSwitchNotice(switchNotice);
		p.setUpdateTime(new Date());
		p.setFilterRule(filterRule);
		return upo.update(p);
	}
	
	public UserPrefs query(String username){
		return upo.query(username);
	}

	public boolean isOpenNotice(String user) {
		UserPrefs p = upo.query(user);
		if(p == null){
			return true;
		}
		return "1".equals(p.getSwitchNotice());
	}
}
