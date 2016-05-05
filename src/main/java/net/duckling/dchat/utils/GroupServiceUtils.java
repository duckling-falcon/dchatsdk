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
package net.duckling.dchat.utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.duckling.dchat.email.AdminEmailSender;
import net.duckling.dchat.exception.RTPGroupNotFoundException;
import net.duckling.dchat.exception.RTPServerException;
import net.duckling.dchat.rtp.domain.GroupJID;
import net.duckling.dchat.rtp.service.interf.IRTPGroupService;
import net.duckling.dchat.vmt.RTPChatGroup;
import net.duckling.dchat.vmt.service.VmtService;
import net.duckling.vmt.api.domain.TreeNode;
import net.duckling.vmt.api.domain.VmtGroup;
import net.duckling.vmt.api.domain.VmtUser;

import org.apache.log4j.Logger;

import cn.vlabs.rest.ServiceException;
/**
 * 群服务辅助类，结合多个服务进行群的综合操作
 * @author Yangxp
 * @since 2013-08-06
 */
public class GroupServiceUtils {
	
	private static final Logger LOG = Logger.getLogger(GroupServiceUtils.class);
	
	private GroupServiceUtils(){}
	
	/**
	 * 重建RTP群，所有群数据从VMT接口中获取。若群存在，则先删除再重建
	 * @param vmtService VMT服务接口
	 * @param groupService RTP群服务
	 * @param group VMT群对象
	 * @return true：成功；false：失败
	 */
	public static boolean rebuildGroup(VmtService vmtService, 
			IRTPGroupService groupService, VmtGroup group){
		try {
			TreeNode node = vmtService.getVmtGroupService().getMember(group.getDn());
			List<VmtUser> adminUsers = vmtService.getVmtGroupService().getAdmins(group.getDn());
			if(null==adminUsers ||adminUsers.isEmpty()){
				LOG.error("rebuild group failed! vmt group "+group.getSymbol()+" has no admin. delete it!");
				groupService.removeGroup(new GroupJID(group.getSymbol()));
				return true;
			}
			
			List<String> users = new ArrayList<String>();
			Set<String> admins = getAdminsCstnetID(adminUsers);
			for(TreeNode child : node.getChildren()){
				VmtUser user = (VmtUser)child.getData();
				if(!admins.contains(user.getCstnetId())){
					users.add(user.getCstnetId());
				}
			}
			String owner = admins.iterator().next();
			admins.remove(owner);
			RTPChatGroup rtpGroup = RTPChatGroup.buildFromVmtGroup(group, owner, admins);
			return groupService.rebuildGroup(rtpGroup, users);
		} catch (ServiceException e) {
			String msgInfo = "rebuild group failed while get members from vmt!";
			LOG.error(msgInfo+ e.getMessage(), e);
			AdminEmailSender.getInstance().sendErrorEmail2Admins(e, null, msgInfo);
		} catch (RTPServerException e) {
			String msgInfo = "rebuild group failed: rtp server exception!";
			LOG.error(msgInfo+ e.getMessage(), e);
			AdminEmailSender.getInstance().sendErrorEmail2Admins(e, null, msgInfo);
		} catch (RTPGroupNotFoundException e) {
			String msgInfo = "rebuild group failed: group not found!";
			LOG.error(msgInfo+ e.getMessage(), e);
			AdminEmailSender.getInstance().sendErrorEmail2Admins(e, null, msgInfo);
		}
		return false;
	}
	/**
	 * 获取管理员用户对象的cstnetid集合
	 * @param admins 管理员对象
	 * @return 用户名集合
	 */
	public static Set<String> getAdminsCstnetID(List<VmtUser> admins){
		Set<String> result = new HashSet<String>();
		for(VmtUser user : admins){
			result.add(user.getCstnetId());
		}
		return result;
	}
}
