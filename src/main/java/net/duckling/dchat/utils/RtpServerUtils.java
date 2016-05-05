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
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.rooyeetone.rtp.sdk.IDataItem;
import com.rooyeetone.rtp.sdk.IPagedResult;
import com.rooyeetone.rtp.sdk.IRtpSvc;

/**
 * Rtp Server 的一些工具类查询方法
 * @author Yangxp
 * @since 2012-06-14
 */
public class RtpServerUtils {
	
	public static final String RTP_SERVER_DOMAIN = Config.getProperty("rtp.server.domain");
	public static final String RTP_GROUPCHAT_DOMAIN = "groupchat."+RTP_SERVER_DOMAIN;
	public static final int MAX_SORT_WEIGHTS = 200;
	
	private static final Logger LOG = Logger.getLogger(RtpServerUtils.class);
	private static final String GROUP_ID_PATTERN = "[0-9a-zA-Z_\\-()\\u4e00-\\u9fa5]+";
	private static final Pattern groupIDPattern = Pattern.compile(GROUP_ID_PATTERN);
	
	private RtpServerUtils(){}
	/**
	 * 去RTP服务器判断群是否存在
	 * @param rtpServer rtp实例
	 * @param groupid 群ID（vmt-symbol）
	 * @return true or false
	 */
	public static boolean isGroupChatExist(IRtpSvc rtpServer, String groupid){
		boolean isExist = false;
		if(null != rtpServer && StringUtils.isNotBlank(groupid)){
			String groupJID = getGroupJIDFromID(groupid);
			try {
				IPagedResult result = rtpServer.exec("GroupChat.Search", groupid);
				List<IDataItem> items = (null != result)?result.getItems():new ArrayList<IDataItem>();
				if(items.size() > 0){
					for(IDataItem item : result.getItems()){
						String jid = item.getProp("jid");
						if(jid.equals(groupJID)){
							return true;
						}
					}
				}
			} catch (Exception e) {
				LOG.error("check group chat exist failed! groupid = "+ groupid, e);
			}
		}
		return isExist;
	}
	/**
	 * 去RTP服务器判断用户是否存在
	 * @param rtpServer rtp实例
	 * @param username 用户名（UMT中的email, VMT中的vmt-id）
	 * @return true or false
	 */
	public static boolean isUserExist(IRtpSvc rtpServer, String username){
		boolean isExist = false;
		if(null != rtpServer && StringUtils.isNotBlank(username)){
			try {
				String user = UserUtils.escapeCstnetId(username);
				user = user.replace("\\", "\\\\");
				IPagedResult result = rtpServer.exec("SearchUser", user, "", "",1,1000);
				List<IDataItem> items = (null != result)?result.getItems():new ArrayList<IDataItem>();
				if(items.size()>0){
					for(IDataItem item : result.getItems()){
						String userResult = (String)item.get("user");
						if(userResult.equals(user)){
							return true;
						}
					}
				}
			} catch (Exception e) {
				LOG.error("check user exist failed! username = "+ username, e);
			}
		}
		return isExist;
	}
	/**
	 * 判断用户是否是群的群主，群不存在时也返回false
	 * @param rtpServer rtp实例
	 * @param groupid 群ID（vmt-symbol）
	 * @param username 用户名（UMT中的email, VMT中的vmt-id）
	 * @return true or false
	 */
	public static boolean isOwnerInGroupChat(IRtpSvc rtpServer, String groupid,
			String username){
		boolean isAdmin = false;
		if(null != rtpServer && StringUtils.isNotBlank(groupid)
				&& StringUtils.isNotBlank(username)){
			try {
				IPagedResult result = rtpServer.exec("GroupChat.Search", groupid);
				List<IDataItem> items = (null != result)?result.getItems():new ArrayList<IDataItem>();
				if(items.size() >0){
					String owner = null;
					for(IDataItem group : result.getItems()){
						String jid = group.getProp("jid");
						if(jid.equals(getGroupJIDFromID(groupid))){
							owner = getUsernameFromGroupMember(group.getProp("owner"));
							break;
						}
					}
					isAdmin = username.equals(owner);
				}
			} catch (Exception e) {
				LOG.error("check group chat exist failed! groupid = "+ groupid, e);
			}
		}
		return isAdmin;
	}
	/**
	 * 给定群ID，返回群的JID。群服务地址在配置文件中给出
	 * @param groupid 群ID（vmt-symbol）
	 * @return 群的JID
	 */
	public static String getGroupJIDFromID(String groupid){
		if(StringUtils.isNotBlank(groupid) && !groupid.contains("@")){
			return groupid.toLowerCase() +"@"+RTP_GROUPCHAT_DOMAIN;
		}else{
			return groupid.toLowerCase();
		}
	}
	/**
	 * 给定群的JID， 返回群的ID
	 * @param groupjid 群JID
	 * @return 群ID
	 */
	public static String getGroupIDFromJID(String groupjid){
		if(StringUtils.isNotBlank(groupjid) && groupjid.contains("@")){
			return groupjid.substring(0, groupjid.indexOf('@'));
		}else{
			return groupjid;
		}
	}
	/**
	 * 将群成员JID换成群成员的用户名
	 * @param memberJID 群成员JID
	 * @return 用户名
	 */
	public static String getUsernameFromGroupMember(String memberJID){
		String result = memberJID;
		if(null!=result && result.contains("\\40") && result.contains("@")){
			result = result.replace("\\40", "@");
			result = result.substring(0, result.lastIndexOf('@'));
		}
		return result;
	}
	/**
	 * 检查群ID是否在RTP中合法，群ID必须由以下字符构成：数字，大小写的字母，中文，- _ ( )
	 * @param groupID 群ID
	 * @return true or false
	 */
	public static boolean checkGroupID(String groupID){
		Matcher matcher = groupIDPattern.matcher(groupID);
		return matcher.matches();
	}
	/**
	 * 返回符合RTP順序的排序權重值
	 * @param sortweights vmt排序權重值
	 * @return RTP排序權重值
	 */
	public static int getRTPSortWeights(int sortweights){
		return 0 - sortweights;
	}
}
