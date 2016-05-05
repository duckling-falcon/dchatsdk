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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import net.duckling.dchat.exception.RTPGroupNotFoundException;
import net.duckling.dchat.exception.RTPServerException;
import net.duckling.dchat.exception.RTPUserNotFoundException;
import net.duckling.dchat.rtp.domain.GroupJID;
import net.duckling.dchat.rtp.domain.UserJID;
import net.duckling.dchat.rtp.service.interf.IRTPGroupService;
import net.duckling.dchat.rtp.service.interf.IRTPUserService;
import net.duckling.dchat.utils.UserUtils;
import net.duckling.dchat.vmt.RTPChatGroup;
import net.duckling.dchat.vmt.RTPVmtUser;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.rooyeetone.rtp.sdk.IDataItem;
import com.rooyeetone.rtp.sdk.IPagedResult;
import com.rooyeetone.rtp.sdk.ISyncItem;
/**
 * RTP的用户管理服务实现类
 * @author Yangxp
 * @since 2013-08-01
 */
@Service
public class RTPUserService extends BaseRTPService implements IRTPUserService {
	private static final Logger LOG = Logger.getLogger(RTPUserService.class);
	private static final String USER = "USER";
	private static final String SYNC_DATA = "SyncData";
	private static final int PAGE_NUM = 1000;
	
	//private UmtClient umt = UmtClient.getInstance();
	
	
	@Autowired
	private IRTPGroupService groupService;
	
	@Autowired
	private ReadOnlyGroupUserService rdgus;
	
	public void getCurrentUser() {
		try {
			IDataItem data = rtp.exec("CurrentUser");
			System.out.println(data.getProp("user"));
		} catch (Exception e) {
			LOG.error(e.getMessage(),e);
		}
	}
	/**
	 * 添加用户，存在则更新。除非访问RTP异常或RTP创建失败，否则均返回用户JID。
	 * 
	 */
	@Override
	public UserJID addUser(RTPVmtUser user) throws RTPServerException {
		String username = user.getCstnetId();
		LOG.info("Add User to dept " + username +" escaped = "+escapeNode(username));
		try {
			String formatDept = UserUtils.formatDept(user.getCurrentDisplay());
			String newDept = appendDept(formatDept, username);
			ISyncItem item = rtp.exec(SYNC_DATA, username, USER, "INSERT");
			item.setProp(RTPVmtUser.RTP_NICKNAME, user.getName());
			item.setProp(RTPVmtUser.RTP_FULLNAME, user.getName());
			item.setProp(RTPVmtUser.RTP_EMAIL, username);
			updateDeptProp(newDept, item);
			updateEncardInfo(newDept, user, item);
			item.setProp(RTPVmtUser.RTP_SORTWEIGHTS, String.valueOf(user.getSortWeights()));
			item.setProp(RTPVmtUser.RTP_TITLE, user.getTitle());
			item.setProp(RTPVmtUser.RTP_MOBILE, user.getTelephone());
			item.setProp(RTPVmtUser.RTP_WORK_PHONE, user.getOfficePhone());
			item.setProp(RTPVmtUser.RTP_SEX, user.getSex());
			item.commit();
		} catch (Exception e) {
			throw new RTPServerException(e);
		}
		return new UserJID(username, null);
	}
	
	private void updateDeptProp(String newDept, ISyncItem item) {
		item.setProp(RTPVmtUser.RTP_DEPT, newDept);
		item.setProp(RTPVmtUser.RTP_ORG_NAME, UserUtils.fetchOrgName(newDept));
		item.setProp(RTPVmtUser.RTP_ORG_UNIT,UserUtils.fetchDeptName(newDept));
	}
	
	private void updateEncardInfo(String newDept, RTPVmtUser user, ISyncItem item){
		item.setProp(RTPVmtUser.RTP_EV_NAME, user.getName());
		item.setProp(RTPVmtUser.RTP_EV_NUMBER, user.getOffice());
		item.setProp(RTPVmtUser.RTP_EV_ORGANIZATION, UserUtils.fetchOrgName(newDept));
		item.setProp(RTPVmtUser.RTP_EV_MOBILE, user.getTelephone());
		item.setProp(RTPVmtUser.RTP_EV_PHONE, user.getOfficePhone());
		item.setProp(RTPVmtUser.RTP_EV_EMAIL, user.getCurrentDisplay());
		item.setProp(RTPVmtUser.RTP_EV_SIP, user.getName());
		item.setProp(RTPVmtUser.RTP_EV_NOTE1, user.getTitle());
	}
	
	private void updatePhoneProp(String newPhoneNum, ISyncItem item){
		item.setProp(RTPVmtUser.RTP_EV_MOBILE, newPhoneNum);
		item.setProp(RTPVmtUser.RTP_MOBILE, newPhoneNum);
	}
	
	private String removeDept(String toRemoveDept,String username){
		Set<String> deptSet = rdgus.loadExistDepts(username);
		removeDeptFromSet(deptSet, toRemoveDept);	
		if(deptSet.size() == 0 ){
			deptSet.add("");
		}
		return this.serializeDeptList(deptSet);
	}
	
	private void removeDeptFromSet(Set<String> set,String dept){
		String toRemovePath = null;
		for(String s:set){
			if(s.equals(dept) || s.contains(dept+"/")){
				toRemovePath = s;
				break;
			}
		}
		if(toRemovePath != null){			
			set.remove(toRemovePath);
		}
	}
	
	private String moveDept(String srcDept, String destDept, String username){
		Set<String> deptList = rdgus.loadExistDepts(username);
		removeDeptFromSet(deptList, srcDept);
		deptList.add(destDept);
		return this.serializeDeptList(deptList);
	}
	
	private String appendDept(String newDeptName, String username) {
		Set<String> deptList = rdgus.loadExistDepts(username);
		if (!deptList.contains(newDeptName)) {
			deptList.add(newDeptName);
		}
		return this.serializeDeptList(deptList);
	}
	
	private String serializeDeptList(Set<String> deptList){
		StringBuilder sb = new StringBuilder();
		for(String d:deptList){
			sb.append(d+",");
		}
		//stem.out.println("current dept:"+sb.toString());
		return sb.substring(0, sb.length() - 1);
	}
	
	@Override
	public RTPVmtUser getUser(String username) throws RTPServerException {
		int page = 1;
		while(true){
			IPagedResult result = null;
			try {
				result = rtp.exec("SearchUser", username.toLowerCase(), "", "",page++,PAGE_NUM);
			} catch (Exception e) {
				throw new RTPServerException(e);
			}
			List<IDataItem> items = (null != result)?result.getItems():new ArrayList<IDataItem>();
			if(null != result && items.size()>0){
				for(IDataItem item : result.getItems()){
					String userResult = (String)item.get("user");
					if(userResult.equals(username)){
						return generateUser(item);
					}
				}
			}
			if(null == result || items.size() < PAGE_NUM){
				break;
			}
		}
		return null;
	}
	
	private RTPVmtUser generateUser(IDataItem item){
		RTPVmtUser user = new RTPVmtUser();
		user.setCstnetId(item.getProp("user"));
		user.setName(item.getProp(RTPVmtUser.RTP_FULLNAME));
		user.setCurrentDisplay(item.getProp(RTPVmtUser.VMT_CURRENT_DISPLAY));
		return user;
	}

	@Override
	public UserJID addDefaultUser(String username) throws RTPServerException {
		try {
			LOG.info("Add default user "+username+" escaped = "+escapeNode(username));
			ISyncItem item = rtp.exec(SYNC_DATA, username, USER, "INSERT");
			item.setProp(RTPVmtUser.RTP_NICKNAME, username);
			item.setProp(RTPVmtUser.RTP_FULLNAME, username);
			item.setProp(RTPVmtUser.RTP_EMAIL, username);
			item.commit();
		} catch (Exception e) {
			throw new RTPServerException(e);
		}
		return new UserJID(username, null);
	}
	
	/**
	 * 删除用户，用户存在则删除，不存在则不做操作。若用户创建过群则转让群
	 */
	@Override
	public void removeUser(UserJID jid) throws RTPServerException {
		String username = jid.getNode();
		if(isUserExist(username) && transferGroups(username)){
			try {
				ISyncItem item = rtp.exec(SYNC_DATA, username, USER, "DELETE");
				item.commit();
			} catch (Exception e) {
				throw new RTPServerException(e);
			}
		}
	}
	/**
	 * 将用户担任群主的所有群转让出去，群转让时的优先级为：admin > member；若群只包含群主，则删除之
	 * @param username 用户名
	 * @return true or false
	 * @throws RTPServerException 访问群服务异常或者RTP操作失败
	 */
	private boolean transferGroups(String username) throws RTPServerException{
		List<RTPChatGroup> groups = groupService.getUserGroups(username, RTPChatGroup.RTP_AFFILIATION_OWNER);
		for(RTPChatGroup group : groups){
			try {
				List<String> members = groupService.getMembers(group.getGroupID(), null, 1, 2);
				if(!members.isEmpty() && members.size()>=2){
					String newOwner = username.equals(members.get(0))?members.get(0):members.get(1);
					groupService.transferGroup(group.getGroupID(), newOwner, true);
				}else{
					groupService.removeGroup(new GroupJID(group.getGroupID()));
				}
			} catch (RTPGroupNotFoundException e) {
				LOG.error("Get "+username+"'s group and group "+group.getGroupID()
						+" doesn't exist when transfer group! it's weird!", e);
			}
		}
		return true;
	}
	
	@Override
	public void updateUserDept(String username, String srcDept, String destDept)
			throws RTPServerException, RTPUserNotFoundException {
		if(isUserExist(username)){
			try {
				String newDept = moveDept(srcDept, destDept, username);
				ISyncItem item = rtp.exec(SYNC_DATA, username, USER, "UPDATE");
				updateDeptProp(newDept, item);
				//TODO Fixme
				item.commit();
			} catch (Exception e) {
				e.printStackTrace();
				throw new RTPServerException(e);
			}
		}else{
			
			throw new RTPUserNotFoundException(username);
		}
	}
	
	@Override
	public void updateUser(String username, RTPVmtUser user)
			throws RTPServerException, RTPUserNotFoundException {
		Set<String> list = rdgus.loadExistDepts(username);
		String formatDept = UserUtils.formatDept(user.getCurrentDisplay());
		boolean isUserExistInDept =  list.contains(formatDept);
		String newDepts = null;
		if(!isUserExistInDept) { //用户不存在时添加
			newDepts = appendDept(formatDept, username);
		} else {
			newDepts = serializeDeptList(list);
		}
		if(isUserExist(username)){
			try {
				ISyncItem item = rtp.exec(SYNC_DATA, username, USER, "UPDATE");
				
				item.setProp(RTPVmtUser.RTP_DEPT, newDepts);
				item.setProp(RTPVmtUser.RTP_DESC, user.getOffice());
				item.setProp(RTPVmtUser.RTP_FULLNAME, user.getName());
				item.setProp(RTPVmtUser.RTP_NICKNAME, user.getName());
				item.setProp(RTPVmtUser.RTP_SORTWEIGHTS, String.valueOf(user.getSortWeights()));
				item.setProp(RTPVmtUser.RTP_TITLE, user.getTitle());
				if(StringUtils.isEmpty(user.getTelephone())){
					item.setProp(RTPVmtUser.RTP_MOBILE, "");
				}else{
					item.setProp(RTPVmtUser.RTP_MOBILE, user.getTelephone());
				}
				item.setProp(RTPVmtUser.RTP_WORK_PHONE, user.getOfficePhone());
				item.setProp(RTPVmtUser.RTP_SEX, user.getSex());
				item.setProp(RTPVmtUser.RTP_EMAIL, user.getCstnetId());
				item.setProp(RTPVmtUser.RTP_ORG_NAME,  UserUtils.fetchOrgName(newDepts));
				item.setProp(RTPVmtUser.RTP_ORG_UNIT, UserUtils.fetchDeptName(newDepts));
				updateEncardInfo(newDepts, user, item);
				item.commit();
			} catch (Exception e) {
				throw new RTPServerException(e);
			}
		}else{
			throw new RTPUserNotFoundException(username);
		}
	}

	/**
	 * 去RTP判断用户是否存在
	 */
	@Override
	public boolean isUserExist(String username) throws RTPServerException {
		return null != getUser(username);
	}
	
	@Override
	public void removeUserDept(String username, String dept) throws RTPServerException, RTPUserNotFoundException {
		if(isUserExist(username)){
			try {
				String newDept = removeDept(dept, username);
				ISyncItem item = rtp.exec(SYNC_DATA, username, USER, "UPDATE");
				updateDeptProp(newDept, item);
				item.commit();
			} catch (Exception e) {
				throw new RTPServerException(e);
			}
		}else{
			throw new RTPUserNotFoundException(username);
		}
	}
	
	@Override
	public void updatePhoneNum(String username, String phoneNum) throws RTPServerException, RTPUserNotFoundException {
		// TODO Auto-generated method stub
		if(isUserExist(username)){
			try {
				ISyncItem item = rtp.exec(SYNC_DATA, username, USER, "UPDATE");
				updatePhoneProp(phoneNum, item);
				item.commit();
			} catch (Exception e) {
				throw new RTPServerException(e);
			}
		}else{
			throw new RTPUserNotFoundException(username);
		}
	}

}
