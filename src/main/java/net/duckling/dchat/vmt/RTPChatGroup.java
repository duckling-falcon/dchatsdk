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

import java.util.Set;

import net.duckling.dchat.utils.Config;
import net.duckling.vmt.api.domain.VmtGroup;

import org.apache.commons.lang.StringUtils;

/**
 * 聊天时的群
 * @author Yangxp
 * @since 2012-06-14
 */
public class RTPChatGroup {
	
	// VMT Group Props
	public static final String VMT_CURRENT_DISPLAY = "vmt-current-display";
	public static final String VMT_NAME = "vmt-name";
	public static final String VMT_SYMBOL = "vmt-symbol";
	public static final String VMT_CREATOR = "vmt-creator";
	public static final String VMT_ADMIN = "vmt-admin";
	public static final String VMT_GROUP_DN = "entryDN";
	// RTP GroupChat Props
	public static final String RTP_JID = "jid";
	public static final String RTP_TITLE = "title";
	public static final String RTP_OWNER = "owner";
	public static final String RTP_CATEGORY = "category";
	public static final String RTP_AUTH = "auth";
	public static final String RTP_MAXUSER = "maxuser";
	public static final String RTP_DESC = "desc";
	public static final String RTP_USERS = "usersnum";
	public static final String RTP_AFFILIATION = "affiliation";
	
	public static final String RTP_AUTH_ANYONE = "anyone";
	public static final String RTP_AUTH_NONE = "none";
	public static final String RTP_AUTH_CONFIRM = "confirm";
	public static final String RTP_AFFILIATION_MEMBER = "member";
	public static final String RTP_AFFILIATION_ADMIN = "admin";
	public static final String RTP_AFFILIATION_OWNER = "owner";
	public static final int RTP_MAX_USER_NUM_DEFAULT = 500;
	
	private String groupID;
	private String owner;
	private Set<String> admins;
	private String category;
	private int maxuser;
	private String desc;
	private int users;
	/**
	 * 验证类型：
	 * 	anyone : 允许任何人加入
	 *  none : 禁止加入
	 *  confirm : 需要管理员确认
	 */
	private String auth;
	private String title;
	private String groupDN;
	
	public String getGroupID() {
		return groupID;
	}
	public void setGroupID(String groupID) {
		this.groupID = groupID;
	}
	public String getOwner() {
		return owner;
	}
	public void setOwner(String owner) {
		this.owner = owner;
	}
	public Set<String> getAdmins() {
		return admins;
	}
	public void setAdmins(Set<String> admins) {
		this.admins = admins;
	}
	public String getCategory() {
		return category;
	}
	public void setCategory(String category) {
		this.category = category;
	}
	public int getMaxuser() {
		return maxuser;
	}
	public void setMaxuser(int maxuser) {
		this.maxuser = maxuser;
	}
	public String getDesc() {
		return desc;
	}
	public void setDesc(String desc) {
		this.desc = desc;
	}
	public String getAuth() {
		return auth;
	}
	public void setAuth(String auth) {
		this.auth = auth;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getGroupDN() {
		return groupDN;
	}
	public void setGroupDN(String groupDN) {
		this.groupDN = groupDN;
	}
	public int getUsers() {
		return users;
	}
	public void setUsers(int users) {
		this.users = users;
	}
	
	@Override
	public int hashCode() {
		return groupID.hashCode();
	}
	@Override
	public boolean equals(Object obj) {
		if(null == obj){
			return false;
		}
		if(!(obj instanceof RTPChatGroup)){
			return false;
		}
		RTPChatGroup group = (RTPChatGroup)obj;
		return groupID.equals(group.getGroupID());
	}
	/**
	 * 构造RTP中的群对象
	 * @param group VMT群对象
	 * @param ownerCstnetID 群主的用户名
	 * @param adminsCstnetID 管理员的用户名
	 * @return RTP群对象
	 */
	public static RTPChatGroup buildFromVmtGroup(VmtGroup group, String ownerCstnetID, Set<String> adminsCstnetID){
		RTPChatGroup rtpGroup = new RTPChatGroup();
		rtpGroup.setOwner(ownerCstnetID);
		rtpGroup.setAuth(RTPChatGroup.RTP_AUTH_CONFIRM);
		rtpGroup.setTitle(group.getName());
		rtpGroup.setDesc(group.getDescription());
		rtpGroup.setGroupDN(group.getDn());
		rtpGroup.setGroupID(group.getSymbol());
		String maxUserNum = Config.getProperty("rtp.group.maxuser");
		int maxuser = StringUtils.isNotBlank(maxUserNum)?Integer.valueOf(maxUserNum):RTP_MAX_USER_NUM_DEFAULT;
		rtpGroup.setMaxuser(maxuser);
		rtpGroup.setAdmins(adminsCstnetID);
		return rtpGroup;
	}
}
