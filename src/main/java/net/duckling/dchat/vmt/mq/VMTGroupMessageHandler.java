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
import java.util.Map;
import java.util.Set;

import net.duckling.dchat.email.AdminEmailSender;
import net.duckling.dchat.exception.RTPGroupNotFoundException;
import net.duckling.dchat.exception.RTPServerException;
import net.duckling.dchat.exception.RTPUserNotFoundException;
import net.duckling.dchat.rtp.domain.GroupJID;
import net.duckling.dchat.rtp.service.interf.IRTPGroupService;
import net.duckling.dchat.rtp.service.interf.IRTPUserService;
import net.duckling.dchat.utils.GroupServiceUtils;
import net.duckling.dchat.vmt.RTPChatGroup;
import net.duckling.dchat.vmt.service.VmtService;
import net.duckling.vmt.api.domain.VmtGroup;
import net.duckling.vmt.api.domain.VmtUser;
import net.duckling.vmt.api.domain.message.MQCreateGroupMessage;
import net.duckling.vmt.api.domain.message.MQDeleteGroupMessage;
import net.duckling.vmt.api.domain.message.MQRefreshGroupMessage;
import net.duckling.vmt.api.domain.message.MQSwitchGroupDchatStatusMessage;
import net.duckling.vmt.api.domain.message.MQSwitchPhoneGroupMessage;
import net.duckling.vmt.api.domain.message.MQUpdateGroupMessage;

import org.apache.log4j.Logger;
import org.springframework.util.StringUtils;

import cn.vlabs.rest.ServiceException;
/**
 * VMT群消息处理器
 * @author Yangxp
 * @since 2013-08-05
 */

public class VMTGroupMessageHandler{
	private static final Logger LOG = Logger.getLogger(VMTGroupMessageHandler.class);
	private static final int RTP_ADMIN_SIZE = 3;
	
	private IRTPGroupService groupService;
	private VmtService vmtService;
	private IRTPUserService userService;
	
	public VMTGroupMessageHandler(IRTPGroupService group, IRTPUserService usrs, VmtService vmt){
		groupService = group;
		//groupService.addMembers("", "", "members");
		vmtService = vmt;
		userService = usrs;
	}

	public void handle(Object message) {
		try{
			if(message instanceof MQCreateGroupMessage){
				MQCreateGroupMessage msg = (MQCreateGroupMessage)message;
				createGroup(msg);
			}else if(message instanceof MQDeleteGroupMessage){
				MQDeleteGroupMessage msg = (MQDeleteGroupMessage)message;
				deleteGroup(msg);
			}else if(message instanceof MQUpdateGroupMessage){
				MQUpdateGroupMessage msg = (MQUpdateGroupMessage)message;
				updateGroup(msg);
			}else if(message instanceof MQRefreshGroupMessage){
				MQRefreshGroupMessage msg = (MQRefreshGroupMessage)message;
				refreshGroup(msg);
			}else if(message instanceof MQSwitchGroupDchatStatusMessage){
				MQSwitchGroupDchatStatusMessage msg = (MQSwitchGroupDchatStatusMessage)message;
				switchGroup(msg);
			}else if(message instanceof MQSwitchPhoneGroupMessage){
				//开通或关闭电话号码隐藏功能
				MQSwitchPhoneGroupMessage msg = (MQSwitchPhoneGroupMessage)message;
				switchPhone(msg);
			}
			else{
				LOG.info("vmt message is invalid, type: group");
			}
		}catch(RuntimeException e){
			String msgInfo = "Runtime Exception! bug or data error! ";
			LOG.error(msgInfo+ e.getMessage(), e);
			AdminEmailSender.getInstance().sendErrorEmail2Admins(e, null, msgInfo);
		}
		LOG.info("finished!");
	}

	private void switchPhone(MQSwitchPhoneGroupMessage msg) {
		if(msg.isVisible()){
			LOG.info("[open phone visible at group] " + msg.toJsonString());
			updatePhoneField(msg.getGroup(), false);
		} else {
			LOG.info("[delete phone field infor at group]" + msg.toJsonString());
			updatePhoneField(msg.getGroup(), true);
		}
	}
	
	private void updatePhoneField(VmtGroup group, boolean isEmpty){
		try {
			Map<String,String> aa = vmtService.getVmtGroupService().searchUserAttribute(group.getDn(), "telephone");
			for(Map.Entry<String, String> en:aa.entrySet()){
				String phoneNume = isEmpty ? "" : en.getValue();
				try {
					userService.updatePhoneNum(en.getKey(), phoneNume);
				} catch (RTPServerException | RTPUserNotFoundException e) {
					LOG.error("Remove user phone field error "+ e.getMessage(), e);
				}
			}
		} catch (ServiceException e) {
			LOG.error("Update org user phone field error:"+ e.getMessage(), e);
		}
	}

	private void switchGroup(MQSwitchGroupDchatStatusMessage msg) {
		if(msg.isOpen()){
			LOG.info("[open group] "+msg.toJsonString());
			VmtGroup vmtGroup = msg.getGroup();
			GroupServiceUtils.rebuildGroup(vmtService, groupService, vmtGroup);
		}else{
			LOG.info("[close group] "+msg.toJsonString());
			VmtGroup group = msg.getGroup();
			try {
				groupService.removeGroup(new GroupJID(group.getSymbol()));
			} catch (RTPServerException e) {
				String msgInfo = "delete group failed";
				LOG.error(msgInfo+ e.getMessage(), e);
				AdminEmailSender.getInstance().sendErrorEmail2Admins(e, msg, msgInfo);
			}
		}
		
	}

	private void createGroup(MQCreateGroupMessage msg){
		LOG.info("[create group] "+msg.toJsonString());
		VmtGroup group = msg.getGroup();
		String owner = msg.getCreator().getCstnetId();
		RTPChatGroup rtpGroup = RTPChatGroup.buildFromVmtGroup(group, owner, new HashSet<String>());
		try {
			if(!groupService.isGroupExist(group.getSymbol())){
				groupService.addGroup(rtpGroup);
			}
		} catch (RTPServerException e) {
			String msgInfo = "create group failed";
			LOG.error(msgInfo+ e.getMessage(), e);
			AdminEmailSender.getInstance().sendErrorEmail2Admins(e, msg, msgInfo);
		}
	}
	
	private void deleteGroup(MQDeleteGroupMessage msg){
		LOG.info("[delete group] "+msg.toJsonString());
		VmtGroup group = msg.getGroup();
		try {
			groupService.removeGroup(new GroupJID(group.getSymbol()));
		} catch (RTPServerException e) {
			String msgInfo = "delete group failed";
			LOG.error(msgInfo+ e.getMessage(), e);
			AdminEmailSender.getInstance().sendErrorEmail2Admins(e, msg, msgInfo);
		}
	}
	
	private void updateGroup(MQUpdateGroupMessage msg){
		LOG.info("[update group] "+msg.toJsonString());
		VmtGroup vmtGroup = msg.getGroup();
		String groupID = vmtGroup.getSymbol();
		try {
			RTPChatGroup rtpGroup = groupService.getGroup(groupID);
			if(null == rtpGroup){
				GroupServiceUtils.rebuildGroup(vmtService, groupService, vmtGroup);
			}else{
				if(!vmtGroup.getName().equals(rtpGroup.getTitle())){
					changeGroupTitle(groupID, vmtGroup.getName());
				}
				if (!StringUtils.isEmpty(vmtGroup.getDescription())) {
					if (!"".equals(vmtGroup.getDescription().trim())) { //rtp不允许空白字符串
						if(!vmtGroup.getDescription().equals(rtpGroup.getDesc())){
							changeGroupDes(groupID, vmtGroup.getDescription());
						}
					} else {
						changeGroupDes(groupID, vmtGroup.getName());
					}
				}
				if(rtpGroup.getAdmins().size() < RTP_ADMIN_SIZE){
					changeAdmin2Group(rtpGroup, vmtGroup);
				}
			}
		} catch (RTPServerException e) {
			String msgInfo = "update group failed, rtp server throw a exception. ";
			LOG.error(msgInfo+ e.getMessage(), e);
			AdminEmailSender.getInstance().sendErrorEmail2Admins(e, msg, msgInfo);
		} catch (ServiceException e) {
			String msgInfo = "update group failed, vmt server throw a exception. ";
			LOG.error(msgInfo+ e.getMessage(), e);
			AdminEmailSender.getInstance().sendErrorEmail2Admins(e, msg, msgInfo);
		}
	}
	
	private void changeGroupDes(String groupID, String description) throws RTPServerException {
		try {
			groupService.changeGroupDesc(groupID, description);
		} catch (RTPGroupNotFoundException e) {
			LOG.error("group not found while changing group description, it is weird!", e);
		}
	}

	private void changeGroupTitle(String groupID, String title) throws RTPServerException{
		try {
			groupService.changeGroupTitle(groupID, title);
		} catch (RTPGroupNotFoundException e) {
			LOG.error("group not found while changing group title, it is weird!", e);
		}
	}
	
	private void changeAdmin2Group(RTPChatGroup rtpGroup, VmtGroup vmtGroup) throws RTPServerException, ServiceException{
		Set<String> rtpAdmins = rtpGroup.getAdmins();
		String rtpOwner = rtpGroup.getOwner();
		
		List<VmtUser> vmtAdminUsers = vmtService.getVmtUserService().getUsersByUmtIds(vmtGroup.getAdmins());
		Set<String> vmtAdmins = new HashSet<String>();
		boolean isOwnerDeleted = true;
		for(VmtUser vmtAdminUser : vmtAdminUsers){
			if(!rtpOwner.equals(vmtAdminUser.getCstnetId())){ //将VMT中的管理员加入到rtp管理员中，群主不加
				vmtAdmins.add(vmtAdminUser.getCstnetId());
			}else{ //VMT管理员中包含群主
				isOwnerDeleted = false;
			}
		}
		
		if(isOwnerDeleted && vmtAdmins.size()<=0){
			LOG.info("Group ["+rtpGroup.getGroupID()+"]'s owner has been removed and has no admin, remove this group!");
			groupService.removeGroup(new GroupJID(rtpGroup.getGroupID()));
		}else{
			rtpAdmins.removeAll(vmtAdmins);
			if(isOwnerDeleted && !transferGroup(vmtAdmins,rtpGroup)){
				LOG.info("Group ["+rtpGroup.getGroupID()+"]'s owner is removed and transfer group failed!");
				return;
			}
			
			List<String> members = new ArrayList<String>();
			List<String> affiliations = new ArrayList<String>();
			for(String admin : rtpAdmins){ //RTP中是管理员，但VMT中不是管理员，这些成员置为member
				members.add(admin);
				affiliations.add(RTPChatGroup.RTP_AFFILIATION_MEMBER);
			}
			
			for(String admin : vmtAdmins){ //VMT中的管理员置为RTP管理员
				members.add(admin);
				affiliations.add(RTPChatGroup.RTP_AFFILIATION_ADMIN);
			}
			
			try {
				groupService.addMembers(rtpGroup.getGroupID(), members, affiliations);
			}catch (RTPGroupNotFoundException e) {
				LOG.error("group not found while add admin to rtp group "
							+ rtpGroup.getGroupID()+", it is weird!", e);
			}
		}
	}
	
	private boolean transferGroup(Set<String> vmtAdmins, RTPChatGroup rtpGroup) throws RTPServerException{
		String newOwner = vmtAdmins.iterator().next();
		vmtAdmins.remove(newOwner);
		LOG.info("Group ["+rtpGroup.getGroupID()+"'s owner has been removed, transfer this group to "+newOwner);
		try {
			groupService.transferGroup(rtpGroup.getGroupID(), newOwner, false);
		} catch (RTPGroupNotFoundException e) {
			LOG.info("transfer group failed! ["+rtpGroup.getGroupID()+"], group not exist!");
			return false;
		}
		return true;
	}
	
	private void refreshGroup(MQRefreshGroupMessage msg){
		LOG.info("[refresh group] "+msg.toJsonString());
		VmtGroup vmtGroup = msg.getGroup();
		GroupServiceUtils.rebuildGroup(vmtService, groupService, vmtGroup);
	}
}
