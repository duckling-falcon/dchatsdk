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
package net.duckling.dchat.vmt.service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import net.duckling.dchat.utils.Config;
import net.duckling.vmt.api.IRestGroupService;
import net.duckling.vmt.api.IRestOrgService;
import net.duckling.vmt.api.IRestUserService;
import net.duckling.vmt.api.impl.GroupService;
import net.duckling.vmt.api.impl.OrgService;
import net.duckling.vmt.api.impl.UserService;

import org.springframework.stereotype.Service;
/**
 * VMT服务访问类
 * @author Yangxp
 * @since 2013-08-05
 */
@Service
public class VmtService {
	private static final String VMT_SERVER_URL_KEY = "vmt.service.url";
	private IRestGroupService groupService;
	private IRestOrgService orgService;
	private IRestUserService userService;
	
	@PostConstruct
	public void init(){
		String vmtURL = Config.getProperty(VMT_SERVER_URL_KEY);
		groupService = new GroupService(vmtURL);
		orgService = new OrgService(vmtURL);
		userService = new UserService(vmtURL);
	}
	
	@PreDestroy
	public void destroy(){
		groupService = null;
		orgService = null;
		userService = null;
	}
	
	public IRestGroupService getVmtGroupService(){
		return groupService;
	}
	
	public IRestOrgService getVmtOrgService(){
		return orgService;
	}
	
	public IRestUserService getVmtUserService(){
		return userService;
	}
}
