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

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import net.duckling.dchat.rtp.dao.RtpGroupDAO;
import net.duckling.dchat.rtp.dao.RtpGroupUserDAO;
import net.duckling.dchat.rtp.dao.RtpUserDAO;
import net.duckling.dchat.rtp.domain.RtpUser;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ReadOnlyGroupUserService {
	
	@Autowired
	private RtpGroupDAO groupDao;
	@Autowired
	private RtpUserDAO userDao;
	@Autowired
	private RtpGroupUserDAO groupUserDao;
	
	public Set<String> loadExistDepts(String username){
		Set<String> groupPaths = new TreeSet<String>();
		RtpUser u = userDao.query(username);
		if(u != null){			
			List<Integer> groupIDs = groupUserDao.getGroupsForUser(u.getUserID());
			for(Integer gid:groupIDs) {
				groupPaths.add(groupDao.getRtpGroupPath(gid));
			}
		}
		return groupPaths;
	}

}
