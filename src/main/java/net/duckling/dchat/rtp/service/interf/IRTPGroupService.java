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
package net.duckling.dchat.rtp.service.interf;

import java.util.List;

import net.duckling.dchat.exception.RTPGroupNotFoundException;
import net.duckling.dchat.exception.RTPServerException;
import net.duckling.dchat.rtp.domain.GroupJID;
import net.duckling.dchat.vmt.RTPChatGroup;

/**
 * 封装RTP处理群组的操作
 * @author Yangxp
 * @since 2013-07-31
 */
public interface IRTPGroupService {
	/**
	 * 创建群，若群不存在，则不创建。群主不存在则创建默认用户
	 * @param group 群对象
	 * @return 群JID
	 * @throws RTPServerException 访问RTP服务异常或RTP操作失败
	 */
	GroupJID addGroup(RTPChatGroup group) throws RTPServerException;
	/**
	 * 查询RTP群
	 * @param groupID 群ID
	 * @return 群对象
	 * @throws RTPServerException 访问RTP服务异常或RTP操作失败
	 */
	RTPChatGroup getGroup(String groupID) throws RTPServerException;
	/**
	 * 删除组
	 * @param jid 组JID
	 * @throws RTPServerException 访问RTP服务异常或RTP操作失败
	 */
	void removeGroup(GroupJID jid) throws RTPServerException;
	/**
	 * 修改群名称
	 * @param groupID 群ID
	 * @param title 新的群名称
	 * @throws RTPServerException 访问RTP服务异常或RTP操作失败
	 * @throws RTPGroupNotFoundException 群未找到
	 */
	void changeGroupTitle(String groupID, String title) throws RTPServerException, RTPGroupNotFoundException;
	/**
	 * 添加群成员
	 * @param groupID 群ID
	 * @param usernames 成员用户名数组
	 * @param affiliation 每个群成员对应的群身份：admin/member，若为空数组，则默认所有群成员为member
	 * @return 添加失败的用户名数组，若无则为空数组
	 * @throws RTPServerException 访问RTP服务异常或RTP操作失败
	 * @throws RTPGroupNotFoundException 群不存在
	 */
	List<String> addMembers(String groupID, List<String> usernames, List<String> affiliation) throws RTPServerException, RTPGroupNotFoundException;
	/**
	 * 删除群成员，不能删除群主
	 * @param groupID 群ID
	 * @param usernames 成员用户名数组
	 * @return 删除失败的用户名数组，若无则为空数组
	 * @throws RTPServerException 访问RTP服务异常或RTP操作失败
	 * @throws RTPGroupNotFoundException 群不存在
	 */
	List<String> removeMembers(String groupID, List<String> usernames) throws RTPServerException, RTPGroupNotFoundException;
	/**
	 * 查询群成员
	 * @param groupID 群ID
	 * @param affiliation 成员身份: admin | member
	 * @param page 页号
	 * @param number 每页个数
	 * @return 指定身份的群成员列表
	 * @throws RTPServerException 访问RTP服务异常或RTP操作失败 
	 * @throws RTPGroupNotFoundException 群不存在
	 */
	List<String> getMembers(String groupID, String affiliation, int page, int number) throws RTPServerException, RTPGroupNotFoundException;
	/**
	 * 查询群成员
	 * @param groupID 群ID
	 * @param affiliation 成员身份: admin | member
	 * @return 群成员列表
	 * @throws RTPServerException 访问RTP服务异常或RTP操作失败 
	 * @throws RTPGroupNotFoundException 群不存在
	 */
	List<String> getAllMembers(String groupID, String affiliation) throws RTPServerException, RTPGroupNotFoundException;
	/**
	 * 转让群
	 * @param groupID 群ID
	 * @param username 新群主
	 * @param removeOldOwner 是否从群中删除原群主
	 * @throws RTPServerException 访问RTP服务异常或RTP操作失败
	 * @throws RTPGroupNotFoundException 群不存在
	 */
	void transferGroup(String groupID, String username, boolean removeOldOwner) throws RTPServerException, RTPGroupNotFoundException;
	/**
	 * 去RTP判断群是否存在
	 * @param groupID 群的ID，唯一标示群
	 * @return true or false
	 * @throws RTPServerException 访问RTP服务异常或RTP操作失败
	 */
	boolean isGroupExist(String groupID) throws RTPServerException;
	/**
	 * 去RTP判断用户是否是群的群主
	 * @param jid 群JID
	 * @param username 用户名
	 * @return true or false
	 * @throws RTPServerException 访问RTP服务异常或RTP操作失败
	 */
	boolean isGroupOwner(GroupJID jid, String username) throws RTPServerException;
	/**
	 * 获取用户某个身份的所有群
	 * @param username 用户名
	 * @param affiliation 群身份：owner | member | admin
	 * @return 群列表
	 * @throws RTPServerException 访问RTP服务异常或RTP操作失败 
	 */
	List<RTPChatGroup> getUserGroups(String username, String affiliation) throws RTPServerException;
	/**
	 * 重建RTP中的群，若群存在则先删除再重建
	 * @param group RTP群对象，其中包含了有效的群主和群管理员
	 * @param usernames RTP群成员的username，这些成员均为普通成员
	 * @return 成功与否
	 * @throws RTPServerException 访问RTP服务异常或RTP操作失败 
	 */
	boolean rebuildGroup(RTPChatGroup group, List<String> usernames) throws RTPServerException, RTPGroupNotFoundException;
	
	void changeGroupDesc(String groupID, String desc)
			throws RTPServerException, RTPGroupNotFoundException;
}
