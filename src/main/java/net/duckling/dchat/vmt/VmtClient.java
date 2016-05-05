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
package net.duckling.dchat.vmt;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.duckling.dchat.rtp.DynamicButton;
import net.duckling.dchat.vmt.service.VmtService;
import net.duckling.vmt.api.domain.VmtGroup;
import net.duckling.vmt.api.domain.VmtUser;

import org.apache.log4j.Logger;

import cn.vlabs.rest.ServiceException;

public class VmtClient {

	private VmtService vmtService = null;
	private static final Logger LOG = Logger.getLogger(VmtClient.class);

	public VmtService getVmtService() {
		return vmtService;
	}

	private static class SingletonHolder {
		private static VmtClient instance = new VmtClient();
	}

	private VmtClient() {
		vmtService = new VmtService();
		vmtService.init();
	}

	public static VmtClient getInstance() {
		return SingletonHolder.instance;
	}

	private Map<String, DynamicButton> dBtnsMap = new ConcurrentHashMap<String, DynamicButton>();

	public void set(DynamicButton ab) {
		dBtnsMap.put(ab.getAppid(), ab);
	}

	public DynamicButton get(String appid) {
		return dBtnsMap.get(appid);
	}

	private String getGroupDN(String groupid) {
		try {
			VmtGroup group = vmtService.getVmtGroupService().getGroupBySymbol(groupid);
			if (group != null) {
				return group.getDn();
			}
		} catch (ServiceException e) {
			LOG.error(String.format("Fetch group dn of groupid=[%s] throws an exception.", groupid), e);
		}
		return null;
	}

	private boolean isUserInGroup(String groupdn, String uid) {
		try {
			List<VmtUser> userlist = vmtService.getVmtUserService().searchUserByCstnetId(groupdn, new String[] { uid });
			if (userlist != null && userlist.size() > 0) {
				return true;
			}
		} catch (ServiceException e) {
			LOG.error(String.format("Query user=[%s] exists at the groupdn=[%s] throws exception.", uid, groupdn), e);
		}
		return false;
	}

	public boolean addUserToGroup(String uid, String groupid) {
		try {
			String groupdn = getGroupDN(groupid);
			if (isUserInGroup(groupdn, uid)) {
				LOG.info(String.format("The user=[%s] exists in the team=[%s]", uid, groupid));
				return false;
			}
			boolean[] result = vmtService.getVmtUserService().addUnkownUserToDN(groupdn, new String[] { uid }, true);
			if(result != null && result.length > 0){
				return result[0];
			}else{
				return false;
			}
		} catch (ServiceException e) {
			e.printStackTrace();
			LOG.error(String.format("Add the user=[%s] to group which is groupid=[%s] throws exception.", uid, groupid), e);
		}
		return false;
	}

}
