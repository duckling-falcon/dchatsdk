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

import net.duckling.dchat.exception.RTPServerException;
import net.duckling.dchat.exception.RTPUserNotFoundException;
import net.duckling.dchat.rtp.domain.UserJID;
import net.duckling.dchat.vmt.RTPVmtUser;

/**
 * RTP的用户管理服务
 * @author Yangxp
 * @since 2013-08-01
 */
public interface IRTPUserService {
	/**
	 * 新建用户，存在则更新
	 * @param user vmt用户对象
	 * @return 用户JID
	 * @throws RTPServerException 访问RTP服务器异常或RTP操作失败
	 */
	UserJID addUser(RTPVmtUser user) throws RTPServerException;
	/**
	 * 查询用户，若用户不存在则返回null
	 * @param username 用户名
	 * @return
	 * @throws RTPServerException
	 */
	RTPVmtUser getUser(String username) throws RTPServerException;
	/**
	 * 创建默认用户，此类用户仅在VMT群组中的成员不在机构中时创建，用户的机构属性默认为空，即RTP中的默认组织。另外，此方法默认RTP中不存在该用户
	 * @param username 用户名
	 * @return 用户JID
	 * @throws RTPServerException 访问RTP服务器异常或RTP操作失败
	 */
	UserJID addDefaultUser(String username) throws RTPServerException;
	/**
	 * 删除用户
	 * @param jid 用户JID
	 * @throws RTPServerException 访问RTP服务器异常或RTP操作失败
	 */
	void removeUser(UserJID jid) throws RTPServerException;
	/**
	 * 修改用户所属的组织，若要将用户移到默认组织，则deptPath为空串("")
	 * @param username 用户名
	 * @param deptPath 组织全路径
	 * @throws RTPServerException 访问RTP服务器异常或RTP操作失败
	 * @throws RTPUserNotFoundException 用户不存在
	 */
	void updateUserDept(String username,String srcDept, String destDept) throws RTPServerException, RTPUserNotFoundException;
	
	void removeUserDept(String username, String dept) throws RTPServerException, RTPUserNotFoundException;
	/**
	 * 更新用户属性，目前只能更新用户显示名称和部门属性
	 * @param username 用户名
	 * @param user 用户属性信息
	 * @throws RTPServerException 访问RTP服务器异常或RTP操作失败
	 * @throws RTPUserNotFoundException 用户不存在
	 */
	void updateUser(String username, RTPVmtUser user) throws RTPServerException, RTPUserNotFoundException;
	/**
	 * 去RTP判断是否存在指定用户名的用户
	 * @param username 用户名
	 * @return true or false
	 * @throws RTPServerException 访问RTP服务器异常或RTP操作失败
	 */
	boolean isUserExist(String username) throws RTPServerException;
	
	void updatePhoneNum(String username, String phoneNum) throws RTPServerException, RTPUserNotFoundException;
	
}
