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
package net.duckling.dchat.vmt.mq;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.duckling.dchat.email.AdminEmailSender;
import net.duckling.dchat.exception.RTPGroupNotFoundException;
import net.duckling.dchat.exception.RTPServerException;
import net.duckling.dchat.exception.RTPUserNotFoundException;
import net.duckling.dchat.rtp.domain.GroupJID;
import net.duckling.dchat.rtp.service.interf.IRTPGroupService;
import net.duckling.dchat.rtp.service.interf.IRTPUserService;
import net.duckling.dchat.utils.GroupServiceUtils;
import net.duckling.dchat.utils.UserUtils;
import net.duckling.dchat.vmt.RTPChatGroup;
import net.duckling.dchat.vmt.RTPVmtUser;
import net.duckling.dchat.vmt.service.VmtService;
import net.duckling.vmt.api.domain.VmtDepart;
import net.duckling.vmt.api.domain.VmtGroup;
import net.duckling.vmt.api.domain.VmtOrg;
import net.duckling.vmt.api.domain.VmtUser;
import net.duckling.vmt.api.domain.message.MQBaseMessage;
import net.duckling.vmt.api.domain.message.MQLinkUserMessage;
import net.duckling.vmt.api.domain.message.MQMoveUserMessage;
import net.duckling.vmt.api.domain.message.MQUnlinkUserMessage;
import net.duckling.vmt.api.domain.message.MQUpdateUserMessage;

import org.apache.log4j.Logger;

import cn.vlabs.rest.ServiceException;
/**
 * VMT用户消息处理器
 * @author Yangxp
 * @since 2013-08-05
 */
public class VMTUserMessageHandler{
	private static final Logger LOG = Logger.getLogger(VMTUserMessageHandler.class);
	
	private IRTPUserService userService;
	private IRTPGroupService groupService;
	private VmtService vmtService;
	
	public VMTUserMessageHandler(IRTPUserService userService,
			IRTPGroupService groupService, VmtService vmtService){
		this.userService = userService;
		this.groupService = groupService;
		this.vmtService = vmtService;
	}
	
	public void handle(Object message) {
		try{
			if(message instanceof MQLinkUserMessage){//创建用户
				MQLinkUserMessage msg = (MQLinkUserMessage)message;
				createUser(msg);
			}else if(message instanceof MQUnlinkUserMessage){//VMT删除用户，仅仅删除关联关系
				MQUnlinkUserMessage msg = (MQUnlinkUserMessage)message;
				deleteUser(msg);
			}else if(message instanceof MQUpdateUserMessage){//更新用户
				MQUpdateUserMessage msg = (MQUpdateUserMessage)message;
				updateUser(msg);
			}else if(message instanceof MQMoveUserMessage){//移动用户
				MQMoveUserMessage msg = (MQMoveUserMessage)message;
				moveUser(msg);
			}else{
				LOG.info("receive message about user but not be processed, ignore!");
			}
		}catch(RuntimeException e){
			String msgInfo = "Runtime Exception!";
			LOG.error(msgInfo+ e.getMessage(), e);
			AdminEmailSender.getInstance().sendErrorEmail2Admins(e, (MQBaseMessage)message, msgInfo);
		} catch (ServiceException e) {
			String msgInfo = "access vmt service failed! ";
			LOG.error(msgInfo+ e.getMessage(), e);
			AdminEmailSender.getInstance().sendErrorEmail2Admins(e, (MQBaseMessage)message, msgInfo);
		}
		LOG.info("finished! ");
	}
	
	private void createUser(MQLinkUserMessage msg){
		LOG.info("[create user] "+msg.toJsonString());
		try{
			if(msg.isGroup()){
				addGroupMember(msg);
			}else{
				addDeptUser(msg);
			}
		}catch(RTPServerException e){
			String msgInfo = "link user failed! ";
			LOG.error(msgInfo+ e.getMessage(), e);
			AdminEmailSender.getInstance().sendErrorEmail2Admins(e, msg, msgInfo);
		} catch (RTPGroupNotFoundException e) {
			String msgInfo = "link user failed! group not found!";
			LOG.error(msgInfo+ e.getMessage(), e);
			AdminEmailSender.getInstance().sendErrorEmail2Admins(e, msg, msgInfo);
		}
		
	}
	
	private void addGroupMember(MQLinkUserMessage msg) 
			throws RTPServerException, RTPGroupNotFoundException{
		List<VmtUser> users = msg.getUsers();
		VmtGroup group = msg.getGroup();
		if(groupService.isGroupExist(group.getSymbol())){
			List<String> usernames = new ArrayList<String>();
			for(VmtUser user : users){
				usernames.add(user.getCstnetId());
			}
			groupService.addMembers(group.getSymbol(), usernames, null);
		}else{
			boolean success = GroupServiceUtils.rebuildGroup(vmtService, groupService, group);
			if(!success){
				String msgInfo = "rebuild group failed! but no exception!";
				LOG.error(msgInfo+" group "+group.getSymbol());
				AdminEmailSender.getInstance().sendErrorEmail2Admins(null, msg, msgInfo);
			}
		}
	}
	
	private void addDeptUser(MQLinkUserMessage msg) throws RTPServerException{
		for(VmtUser user : msg.getUsers()){
			RTPVmtUser rtpUser = RTPVmtUser.buildFromVmtUser(user);
			userService.addUser(rtpUser);
		}
	}
	
	private void deleteUser(MQUnlinkUserMessage msg) throws ServiceException{
		LOG.info("delete user "+msg.toJsonString());
		try{
			if(msg.isGroup()){
				removeGroupMember(msg);
			}else{
				removeDeptUser(msg);
			}
		}catch(RTPServerException e){
			String msgInfo = "unlink user failed!";
			LOG.error(msgInfo+ e.getMessage(), e);
			AdminEmailSender.getInstance().sendErrorEmail2Admins(e, msg, msgInfo);
		}
	}
	
	private void removeGroupMember(MQUnlinkUserMessage msg) throws RTPServerException, ServiceException{
		VmtGroup vmtGroup = msg.getGroup();
		String groupID = vmtGroup.getSymbol();
		RTPChatGroup rtpGroup = groupService.getGroup(groupID);
		if(null != rtpGroup){
			try{
				List<String> usernames = new ArrayList<String>();
				for(VmtUser user : msg.getUsers()){
					usernames.add(user.getCstnetId());
				}
				if(usernames.contains(rtpGroup.getOwner())){//移除的是群主
					rtpGroup.setGroupDN(vmtGroup.getDn());//rtp中的group是沒有DN值的
					transferGroup(usernames, rtpGroup);
				}else{//删除的是管理员或普通成员
					usernames.remove(rtpGroup.getOwner());
					groupService.removeMembers(groupID, usernames);
				}
			}catch(RTPGroupNotFoundException e){
				String msgInfo = "remove group member failed! group is not found: "+groupID;
				LOG.error(msgInfo+ e.getMessage(), e);
				AdminEmailSender.getInstance().sendErrorEmail2Admins(e, msg, msgInfo);
			}
		}
	}
	
	private void transferGroup(List<String> usernames, RTPChatGroup rtpGroup) 
			throws RTPServerException, RTPGroupNotFoundException, ServiceException{
		String groupID = rtpGroup.getGroupID();
		Set<String> admins = rtpGroup.getAdmins();
		Set<String> adminsCopy = new HashSet<String>(admins);
		admins.removeAll(usernames);
		String newOwner = null;
		if(admins.isEmpty()){//从VMT中找一个管理员过来
			List<VmtUser> vmtAdminUsers = vmtService.getVmtGroupService().getAdmins(rtpGroup.getGroupDN());
			List<String> vmtAdmins = getUserCstnetIDs(vmtAdminUsers);
			if(vmtAdmins.isEmpty()){ //VMT中沒有管理員了，那麼刪除這個團隊
				groupService.removeGroup(new GroupJID(groupID));
				return;
			}else{//找一個管理員做群主
				vmtAdmins.removeAll(adminsCopy);
				vmtAdmins.remove(rtpGroup.getOwner());
				newOwner = vmtAdmins.get(0);
			}
		}else{
			newOwner = admins.iterator().next();
		}
		groupService.transferGroup(groupID, newOwner, true);
		groupService.removeMembers(groupID, usernames);
	}
	
	private List<String> getUserCstnetIDs(List<VmtUser> users){
		List<String> result = new ArrayList<String>();
		for(VmtUser user : users){
			result.add(user.getCstnetId());
		}
		return result;
	}
	
	private void removeDeptUser(MQUnlinkUserMessage msg) throws RTPServerException{
		for(VmtUser user : msg.getUsers()){
			try {
				userService.removeUserDept(user.getCstnetId(), msg.getOrg().getCurrentDisplay());
			}catch (RTPUserNotFoundException e) {
				LOG.warn("remove user"+user.getCstnetId()+" from dept, but no this user.", e);
			}
		}
	}
	
	private void updateUser(MQUpdateUserMessage msg){
		LOG.info("[update user] "+msg.toJsonString());
		VmtUser user = msg.getUser();
		RTPVmtUser rtpUser = RTPVmtUser.buildFromVmtUser(user);
		try {
			String username = user.getCstnetId();
			try {
				if(user.isDisableDchat()){
					userService.removeUserDept(username,  UserUtils.formatDept(user.getCurrentDisplay()));
				} else {
					userService.updateUser(username, rtpUser);					
				}
			} catch (RTPUserNotFoundException e) {
				userService.addUser(rtpUser);
			}
		} catch (RTPServerException e) {
			String msgInfo = "update user"+user.getCstnetId()+" failed , rtp server throw an exception.";
			LOG.error(msgInfo+ e.getMessage(), e);
			AdminEmailSender.getInstance().sendErrorEmail2Admins(e, msg, msgInfo);
		}
	}
	
	/**
	 * 重要提示：移动用户只可能在一个机构下的子部门或根中完成,所以org就是源
	 */
	private void moveUser(MQMoveUserMessage msg){
		LOG.info("[move user] "+msg.toJsonString());
		List<VmtUser> users = msg.getUser();
		VmtDepart depart = msg.getDept();
		VmtOrg org = msg.getOrg();
		String targetDeptPath = (null != depart)?depart.getCurrentDisplay():org.getCurrentDisplay();
		targetDeptPath = UserUtils.formatDept(targetDeptPath);
		for(VmtUser user : users){
			try {
				if(userService.isUserExist(user.getCstnetId())){						
					userService.updateUserDept(user.getCstnetId(),org.getCurrentDisplay(), targetDeptPath);
				}else{
					RTPVmtUser rtpUser = RTPVmtUser.buildFromVmtUser(user);
					userService.addUser(rtpUser);
				}
			} catch (RTPServerException e) {
				LOG.error("move user "+user.getCstnetId()+" to "+targetDeptPath+" failed!", e);
			} catch (RTPUserNotFoundException e) {
				LOG.error("user not found while move user "+user.getCstnetId()+" to "+targetDeptPath, e);
			}
		}
	}
	
	
}
