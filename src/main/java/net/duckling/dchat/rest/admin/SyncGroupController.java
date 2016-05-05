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
package net.duckling.dchat.rest.admin;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import net.duckling.dchat.exception.RTPServerException;
import net.duckling.dchat.rtp.service.interf.IRTPGroupService;
import net.duckling.dchat.utils.JSONResult;
import net.duckling.dchat.utils.RTPCharUtils;
import net.duckling.dchat.utils.RtpServerUtils;
import net.duckling.dchat.utils.UserUtils;
import net.duckling.dchat.vmt.LdapReader;
import net.duckling.dchat.vmt.RTPChatGroup;
import net.duckling.dchat.vmt.RTPVmtUser;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.rooyeetone.rtp.sdk.IDataItem;
import com.rooyeetone.rtp.sdk.IPagedResult;
import com.rooyeetone.rtp.sdk.IRtpSvc;
import com.rooyeetone.rtp.sdk.ISyncItem;
import com.rooyeetone.rtp.sdk.RtpSvc;
/**
 * 从VMT同步群组到RTP
 * @author Yangxp
 * @since 2012-06-17
 */
@Controller
@RequestMapping("/v1/sa/sync")
public class SyncGroupController {
	
	private static final int GROUP_PAGE_COUNT = 2000;
	private static final String RTP_SERVER_ERROR = "RTP Server Error!";
	private static final Logger LOG = Logger.getLogger(SyncGroupController.class);
	private boolean isCreateDefaultUser = true;
	
	@Autowired
	private IRTPGroupService groupService;
	/**
	 * 同步所有群组, 先删除再插入
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/group/all", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject syncAllGroup(HttpServletRequest request){
		
		setCreateDefaultFlag(request);
		
		IRtpSvc rtpServer = getRtpServer(request);
		if(null == rtpServer){
			return JSONResult.fail(RTP_SERVER_ERROR, null);
		}
		//删除所有RTP中现有群组
		if(! removeAllGroup(rtpServer)){
			return JSONResult.fail("Remove rtp group failed!", null);
		}
		//从VMT的ldap直接读取群组并写入rtp
		LdapReader ldap = LdapReader.getInstance();
		List<RTPChatGroup> groups = ldap.getAllGroup();
		List<String> failGroups = new ArrayList<String>();
		
		insertGroups2RTP(rtpServer, ldap, groups, failGroups);
		LOG.info("sync group data complete!");
		String message = "sync group data from vmt ldap success, "+(groups.size()-failGroups.size())+" groups! failGroups = [" +
				failGroups.toString() + "]";
		
		isCreateDefaultUser = true;
		return JSONResult.success(message, null);
	}
	
	private void setCreateDefaultFlag(HttpServletRequest request){
		String createDefault = request.getParameter("createDefault");
		if(StringUtils.isNotBlank(createDefault)){
			isCreateDefaultUser = false;
		}
	}
	
	/**
	 * 同步指定群组，先删除再插入
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/group", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject syncGroup(HttpServletRequest request, @RequestParam("groupSymbols[]")String[] groupSymbols){
		setCreateDefaultFlag(request);
		
		IRtpSvc rtpServer = getRtpServer(request);
		if(null == rtpServer){
			return JSONResult.fail(RTP_SERVER_ERROR, null);
		}
		//从VMT的ldap直接读取群组并写入rtp
		LdapReader ldap = LdapReader.getInstance();
		List<RTPChatGroup> groups = ldap.getGroups(groupSymbols);
		
		//删除RTP中现有群组
		if(! removeGroup(rtpServer,groups)){
			return JSONResult.fail("Remove rtp group failed!", null);
		}
		
		List<String> failGroups = new ArrayList<String>();
		
		insertGroups2RTP(rtpServer, ldap, groups, failGroups);
		LOG.info("sync group data complete!");
		String message = "sync group data from vmt ldap success, "+(groups.size()-failGroups.size())+" groups! failGroups = [" +
				failGroups.toString() + "]";
		
		isCreateDefaultUser = true;
		return JSONResult.success(message, null);
	}
	
	private void insertGroups2RTP(IRtpSvc rtpServer, LdapReader ldap, List<RTPChatGroup> groups, List<String> failGroups){
		int size = groups.size();
		int i = 0;
		for(RTPChatGroup group : groups){
			try {
				createIfUserNotExist(rtpServer, group.getOwner());
				String groupJid = rtpServer.exec("GroupChat.Create", group.getGroupID(), 
						RTPCharUtils.escapeNode(group.getOwner()), group.getCategory(), group.getMaxuser(), 
						group.getDesc(), group.getAuth(), group.getTitle());
				List<RTPVmtUser> members = ldap.getGroupMembers(group.getGroupDN());
				groupJid = groupJid.toLowerCase();
				Set<String> admins = addAdmins2Group(rtpServer, group, groupJid);
				for(RTPVmtUser member : members){
					String memberJid = UserUtils.escapeCstnetId(member.getCstnetId());
					if(memberJid.contains("@") && !memberJid.equals(group.getOwner()) 
							&& !admins.contains(memberJid)){
						try{
							createIfUserNotExist(rtpServer, memberJid);
							rtpServer.exec("GroupChat.AddMember", groupJid, RTPCharUtils.escapeNode(memberJid), "member");
						}catch (Exception e) {
							LOG.error("add member failed to rtp, group : "+group.getGroupID()+", "+memberJid+", exception :"+e.getMessage());
						}
					}
				}
				i++;
				LOG.info("("+i+"/"+size+"). VMT group ["+group.getGroupID()+"], "+members.size()+" members.");
				if(i % 100 ==0){
					LOG.info("start to sleep 30s ...");
					Thread.sleep(30*1000);
					LOG.info("end to sleep 30s 。。。。。");
				}
			} catch (Exception e) {
				failGroups.add(group.getGroupID());
				String message = "insert group from vmt ldap to rtp server failed! groupid = "
							+group.getGroupID()+", owner = "+group.getOwner();
				LOG.error(message, e);
			}
		}
	}
	
	private void createIfUserNotExist(IRtpSvc rtp, String username) throws RTPServerException{
		if(isCreateDefaultUser && !RtpServerUtils.isUserExist(rtp, username)){
			try{
				ISyncItem item = rtp.exec("SyncData", username, "USER", "INSERT");
				item.setProp("nickname", username);
				item.setProp("fullname", username);
				item.setProp("email", username);
				item.setProp(RTPVmtUser.RTP_DEPT, "");
				item.commit();
				LOG.info("create default user: "+username);
			}catch(Exception e){
				throw new RTPServerException(e);
			}
		}
	}
	
	private Set<String> addAdmins2Group(IRtpSvc rtp, RTPChatGroup group, String groupJid) throws RTPServerException{
		Set<String> admins = group.getAdmins();
		Iterator<String> itr = admins.iterator();
		int i=0;
		while(itr.hasNext()){
			String username = itr.next();
			createIfUserNotExist(rtp, username);
			String affiliation = (i++ < 3)?"admin":"member";
			try {
			    LOG.info("Add Member to Group["+groupJid+"]: username="+username+",affiliation="+affiliation);
				rtp.exec("GroupChat.AddMember", groupJid, RTPCharUtils.escapeNode(username), affiliation);
			} catch (Exception e) {
				throw new RTPServerException(e);
			}
		}
		return admins;
	}
	
	private IRtpSvc getRtpServer(HttpServletRequest req){
		try {
			return RtpSvc.getInstance(req);
		} catch (Exception e1) {
			LOG.error(RTP_SERVER_ERROR, e1);
		}
		return null;
	}
	/**
	 * 删除RTP现有群组
	 * @param rtpServer
	 * @return
	 */
	private boolean removeAllGroup(IRtpSvc rtpServer){
		LOG.info("remove all groups in rtp server! start .....");
		try {
			do{
				IPagedResult result = rtpServer.exec("GroupChat.Search", "","","","",1,GROUP_PAGE_COUNT);
				List<IDataItem> items = result.getItems();
				for(IDataItem item : items){
					String groupJID = item.getProp("jid");
					if(StringUtils.isNotBlank(groupJID)){
						rtpServer.exec("GroupChat.Destroy", groupJID);
						LOG.info("remove group "+groupJID);
					}
				}
				if(items.size() < GROUP_PAGE_COUNT){
					break;
				}
			}while(true);
		} catch (Exception e) {
			LOG.error("error occured while remove all group!", e);
			return false;
		}
		LOG.info("remove all groups in rtp server! completed!!");
		return true;
	}
	
	private boolean removeGroup(IRtpSvc rtpServer, List<RTPChatGroup> groups){
		LOG.info("remove groups in rtp server! start .....");
		boolean result = true;
		for(RTPChatGroup group : groups){
			String groupID = group.getGroupID();
			if(StringUtils.isNotBlank(groupID)){
				try {
					if(groupService.isGroupExist(groupID)){ //删除时若群不存在，则会造成异常，导致整个操作失败。
						rtpServer.exec("GroupChat.Destroy", RtpServerUtils.getGroupJIDFromID(groupID));
					}
					LOG.info("remove group "+groupID);
				} catch (Exception e) {
					result = false;
					LOG.error("error occured while remove group : "+groupID, e);
				}
			}
		}
		LOG.info("remove groups in rtp server! completed!!");
		return result;
	}
}
