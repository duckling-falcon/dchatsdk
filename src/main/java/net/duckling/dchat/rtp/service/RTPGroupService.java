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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.duckling.dchat.exception.RTPGroupNotFoundException;
import net.duckling.dchat.exception.RTPServerException;
import net.duckling.dchat.rtp.domain.GroupJID;
import net.duckling.dchat.rtp.domain.UserJID;
import net.duckling.dchat.rtp.service.interf.IRTPGroupService;
import net.duckling.dchat.rtp.service.interf.IRTPUserService;
import net.duckling.dchat.utils.RtpServerUtils;
import net.duckling.dchat.vmt.RTPChatGroup;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.rooyeetone.rtp.sdk.IDataItem;
import com.rooyeetone.rtp.sdk.IPagedResult;
/**
 * RTP群服务实现类
 * @author Yangxp
 * @since 2013-08-01
 */
@Service
public class RTPGroupService extends BaseRTPService implements IRTPGroupService {
	private static final Logger LOG = Logger.getLogger(RTPGroupService.class);
	private static final String JID = "jid";
	private static final int PAGE_NUM = 1000;
	@Autowired
	private IRTPUserService userService;
	/**
	 * 创建RTP群。群存在时不做任何操作；群不存在时创建。若群主不存在则创建默认用户
	 */
	@Override
	public GroupJID addGroup(RTPChatGroup group) throws RTPServerException {
		boolean isExist = isGroupExist(group.getGroupID());
		if(!isExist){
			boolean isOwnerExist = userService.isUserExist(group.getOwner());
			if(!isOwnerExist){
				userService.addDefaultUser(group.getOwner());
			}
			try {
				String groupJID = rtp.exec("GroupChat.Create", group.getGroupID(), escapeNode(group.getOwner()), 
						group.getCategory(), group.getMaxuser(), group.getDesc(), 
						group.getAuth(), group.getTitle());
				for(String admin : group.getAdmins()){
					rtp.exec("GroupChat.AddMember", groupJID, escapeNode(admin), RTPChatGroup.RTP_AFFILIATION_ADMIN);
				}
			} catch (Exception e) {
				LOG.error("Add admin to group chat failed", e);
				throw new RTPServerException(e);
			}
		}
		return new GroupJID(group.getGroupID());
	}

	@Override
	public RTPChatGroup getGroup(String groupID) throws RTPServerException{
		int page = 1;
		while(true){
			IPagedResult result = null;
			String jid = new GroupJID(groupID).toString();
			try {
				result = rtp.exec("GroupChat.Search", groupID,"","","",page++,PAGE_NUM);
			} catch (Exception e) {
				throw new RTPServerException(e);
			}
			List<IDataItem> items = (null != result)?result.getItems():new ArrayList<IDataItem>();
			if(items.size()>0){
				for(IDataItem item : result.getItems()){
					String groupResult = (String)item.get(JID);
					if(groupResult.equals(jid)){
						return generateGroup(item);
					}
				}
			}
			if(null == result || items.size() < PAGE_NUM){
				break;
			}
		}
		return null;
	}
	
	private RTPChatGroup generateGroup(IDataItem groupItem) throws RTPServerException{
		RTPChatGroup group = new RTPChatGroup();
		group.setGroupID(RtpServerUtils.getGroupIDFromJID(groupItem.getProp(RTPChatGroup.RTP_JID)));
		group.setTitle(groupItem.getProp(RTPChatGroup.RTP_TITLE));
		group.setOwner(RtpServerUtils.getUsernameFromGroupMember(groupItem.getProp(RTPChatGroup.RTP_OWNER)));
		group.setCategory(groupItem.getProp(RTPChatGroup.RTP_CATEGORY));
		group.setAuth(groupItem.getProp(RTPChatGroup.RTP_AUTH));
		group.setMaxuser((Integer)groupItem.get(RTPChatGroup.RTP_MAXUSER));
		group.setDesc(groupItem.getProp(RTPChatGroup.RTP_DESC));
		//group.setUsers((Integer)groupItem.get("users"));//ChatGroup.RTP_USERS，此处不能用这个常量，需要如意通统一在做修改
		group.setAdmins(getGroupAdminWithoutCheckGroup(group.getGroupID()));
		return group;
	}
	
	private Set<String> getGroupAdminWithoutCheckGroup(String groupID) throws RTPServerException{
		Set<String> adminsMember = new HashSet<String>();
		try {
			String groupJID = new GroupJID(groupID).toString();
			List<IDataItem> result = rtp.exec("GroupChat.GetAllMembers", groupJID);
			for(IDataItem item : result){
				String jid = item.getProp(JID);
				String aff = item.getProp(RTPChatGroup.RTP_AFFILIATION);
				if(RTPChatGroup.RTP_AFFILIATION_ADMIN.equals(aff)){
					adminsMember.add(RtpServerUtils.getUsernameFromGroupMember(jid));
				}
			}
		} catch (Exception e) {
			throw new RTPServerException(e);
		}
		return adminsMember;
	}
	
	/**
	 * 删除群。若群不存在则不做操作
	 */
	@Override
	public void removeGroup(GroupJID jid) throws RTPServerException {
		boolean isGroupExist = isGroupExist(jid.getNode());
		if(isGroupExist){
			try {
				rtp.exec("GroupChat.Destroy", jid.toString());
			} catch (Exception e) {
				throw new RTPServerException(e);
			}
		}
	}
	@Override
	public void changeGroupTitle(String groupID, String title)
			throws RTPServerException, RTPGroupNotFoundException {
		if(isGroupExist(groupID)){
			try{
				GroupJID jid = new GroupJID(groupID);
				rtp.exec("GroupChat.SetProperty", jid.toString(), RTPChatGroup.RTP_TITLE, title);
				
			}catch(Exception e){
				throw new RTPServerException(e);
			}
		}else{
			throw new RTPGroupNotFoundException(groupID);
		}
	}
	
	@Override
	public void changeGroupDesc(String groupID, String desc)
			throws RTPServerException, RTPGroupNotFoundException {
		if(isGroupExist(groupID)){
			try{
				GroupJID jid = new GroupJID(groupID);
				rtp.exec("GroupChat.SetProperty", jid.toString(), RTPChatGroup.RTP_DESC, desc);
				
			}catch(Exception e){
				throw new RTPServerException(e);
			}
		}else{
			throw new RTPGroupNotFoundException(groupID);
		}
	}

	/**
	 * 添加群成员。若成员不存在则创建默认用户；若affiliations中的元素个数小于usernames的元素个数，则usernames中多出的用户
	 * 身份均为普通成员(member)；若usernames中包含群主，则群主不会被添加，且返回列表中不会包含群主
	 */
	@Override
	public List<String> addMembers(String groupID, List<String> usernames, 
			List<String> affiliations) throws RTPServerException, RTPGroupNotFoundException {
		
		List<String> failMembers = new ArrayList<String>();
		int aSize = (null == affiliations)?0:affiliations.size(), aIndex = 0;
		RTPChatGroup group = getGroup(groupID);
		if(null == group){
			throw new RTPGroupNotFoundException(groupID);
		}
		
		String groupJID = new GroupJID(groupID).toString();
		for(String username : usernames){
			if(!userService.isUserExist(username)){
				userService.addDefaultUser(username);
			}
			if(!username.equals(group.getOwner())){
				String affiliation = aIndex >= aSize ? "member":affiliations.get(aIndex);
				aIndex++;
				try {
					rtp.exec("GroupChat.AddMember", groupJID, escapeNode(username), affiliation);
				} catch (Exception e) {
					LOG.error("Add user "+username+" to group "+groupID+" failed! ", e);
					failMembers.add(username);
				}
			}
		}
		return failMembers;
	}
	/**
	 * 从群中删除用户。若usernames中包含群主，则群主不会被删除，且返回列表中不会包含群主
	 */
	@Override
	public List<String> removeMembers(String groupID, List<String> usernames) 
			throws RTPServerException, RTPGroupNotFoundException{
		
		RTPChatGroup group = getGroup(groupID);
		if(null == group){
			throw new RTPGroupNotFoundException(groupID);
		}
		
		String groupJID = new GroupJID(groupID).toString();
		List<String> failMembers = new ArrayList<String>();
		for(String username : usernames){
			if(!username.equals(group.getOwner())){
				try {
					rtp.exec("GroupChat.RemoveMember", groupJID, escapeNode(username));
				} catch (Exception e) {
					LOG.error("Remove user "+username+" from group "+groupID+" failed! ", e);
					failMembers.add(username);
				}
			}
		}
		return failMembers;
	}
	
	@Override
	public List<String> getMembers(String groupID, String affiliation, int page, int number)
			throws RTPServerException, RTPGroupNotFoundException {
		RTPChatGroup group = getGroup(groupID);
		if(null == group){
			throw new RTPGroupNotFoundException(groupID);
		}
		
		String groupJID = new GroupJID(groupID).toString();
		List<String> members = new ArrayList<String>();
		try {
			IPagedResult result = rtp.exec("GroupChat.GetMembers", groupJID, page, number);
			for(IDataItem item : result.getItems()){
				String jid = item.getProp(JID);
				String aff = item.getProp(RTPChatGroup.RTP_AFFILIATION);
				if(null == affiliation  || affiliation.equals(aff)){
					members.add(RtpServerUtils.getUsernameFromGroupMember(jid));
				}
			}
		} catch (Exception e) {
			throw new RTPServerException(e);
		}
		return members;
	}

	@Override
	public List<String> getAllMembers(String groupID, String affiliation)
			throws RTPServerException, RTPGroupNotFoundException {
		RTPChatGroup group = getGroup(groupID);
		if(null == group){
			throw new RTPGroupNotFoundException(groupID);
		}
		
		String groupJID = new GroupJID(groupID).toString();
		List<String> members = new ArrayList<String>();
		try {
			List<IDataItem> result = rtp.exec("GroupChat.GetAllMembers", groupJID);
			for(IDataItem item : result){
				String jid = item.getProp(JID);
				String aff = item.getProp(RTPChatGroup.RTP_AFFILIATION);
				if(affiliation.equals(aff) || null == aff){
					members.add(RtpServerUtils.getUsernameFromGroupMember(jid));
				}
			}
		} catch (Exception e) {
			throw new RTPServerException(e);
		}
		return members;
	}

	/**
	 * 将群转让给某个用户，并且根据需要将原群主移除出群。此操作不管新群主是否在群中
	 */
	@Override
	public void transferGroup(String groupID, String username, boolean removeOldOwner) 
			throws RTPServerException, RTPGroupNotFoundException {
		
		RTPChatGroup group = getGroup(groupID);
		if(null == group){
			throw new RTPGroupNotFoundException(groupID);
		}
		String groupJID = new GroupJID(groupID).toString();
		try {
			rtp.exec("GroupChat.Transfer", groupJID, group.getOwner(), removeOldOwner, username);
		} catch (Exception e) {
			throw new RTPServerException(e);
		}
	}

	@Override
	public boolean isGroupExist(String groupID) throws RTPServerException {
		return null != getGroup(groupID);
	}

	@Override
	public boolean isGroupOwner(GroupJID jid, String username) throws RTPServerException {
		RTPChatGroup group = getGroup(jid.getNode());
		if(null != group){
			return username.equals(group.getOwner());
		}
		return false;
	}
	/**
	 * 获取群中用户为某个身份的所有群列表，若affiliation为非法身份，则返回空列表；若为空，则返回用户的所有群
	 */
	@Override
	public List<RTPChatGroup> getUserGroups(String username, String affiliation)
			throws RTPServerException {
		UserJID jid = new UserJID(username, null);
		List<RTPChatGroup> result = new ArrayList<RTPChatGroup>();
		if(RTPChatGroup.RTP_AFFILIATION_ADMIN.equals(affiliation) ||
				RTPChatGroup.RTP_AFFILIATION_MEMBER.equals(affiliation) ||
				RTPChatGroup.RTP_AFFILIATION_OWNER.equals(affiliation)){
			try {
				List<IDataItem> groups= rtp.exec("GroupChat.GetUserGroups", jid.toString(), affiliation);
				for(IDataItem group : groups){
					result.add(generateGroup(group));
				}
			} catch (Exception e) {
				throw new RTPServerException(e);
			}
		}
		return result;
	}

	@Override
	public boolean rebuildGroup(RTPChatGroup group, List<String> usernames) {
		try{
			if(isGroupExist(group.getGroupID())){
				try {
					rtp.exec("GroupChat.Destroy", new GroupJID(group.getGroupID()).toString());
				} catch (Exception e) {
					throw new RTPServerException(e);
				}
			}
			addGroup(group);
			addMembers(group.getGroupID(), usernames, null);
			return true;
		}catch(RTPServerException e){
			LOG.error("rebuild group failed! "+e.getMessage(), e);
		}catch(RTPGroupNotFoundException e){
			LOG.error("rebuild group failed! "+e.getMessage(), e);
		}
		return false;
	}
	
}
