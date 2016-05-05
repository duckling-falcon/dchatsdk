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

import net.duckling.dchat.exception.RTPDeptNotFoundException;
import net.duckling.dchat.exception.RTPServerException;
import net.duckling.dchat.rtp.domain.RTPRole;
import net.duckling.dchat.rtp.service.interf.IRTPRoleService;

import com.rooyeetone.rtp.sdk.IDataItem;
/**
 * RTP的角色管理服务，该服务目前尚未实际测试
 * @author Yangxp
 * @since 2013-08-21
 */
public class RTPRoleService extends BaseRTPService implements IRTPRoleService {
	/**
	 * 设置每个部门的默认角色，让每个部门中的人只能看到自身的部门。（尚未实现，此功能RTP已提供）
	 */
	@Override
	public void setDefaultRoleForDept(String deptPath)
			throws RTPServerException, RTPDeptNotFoundException {
		
	}
	/**
	 * 获取角色
	 * @param roleId 角色ID
	 * @return
	 * @throws RTPServerException 访问RTP服务异常
	 */
	public RTPRole getRole(int roleId) throws RTPServerException{
		try {
			IDataItem item = rtp.exec("Security.GetRoleInfo", String.valueOf(roleId));
			if(null != item){
				RTPRole role = new RTPRole();
				role.setRoleID(Integer.valueOf(item.getProp(RTPRole.RTP_ROLEID)));
				role.setUserID(Integer.valueOf(item.getProp(RTPRole.RTP_USERID)));
				role.setRoleName(item.getProp(RTPRole.RTP_ROLE_NAME));
				role.setRoleDesc(item.getProp(RTPRole.RTP_ROLE_DESC));
				return role;
			}
		} catch (Exception e) {
			throw new RTPServerException(e);
		}
		return null;
	}
	/**
	 * 添加角色
	 * @param name 角色名
	 * @param desc 角色描述
	 * @return 角色ID
	 * @throws RTPServerException 访问RTP服务异常
	 */
	public int addRole(String name, String desc) throws RTPServerException{
		try {
			String roleID = rtp.exec("Security.AddRole", name, desc);
			return Integer.valueOf(roleID);
		} catch (Exception e) {
			throw new RTPServerException(e);
		}
	}
	/**
	 * 删除角色
	 * @param roleName 角色名
	 * @return true or false
	 */
	public boolean removeRole(String roleName){
		return false;
	}
	/**
	 * 设置角色权限
	 * @param roleID 角色ID
	 * @param rights 角色的权限
	 * @throws RTPServerException 访问RTP服务异常
	 */
	public void setRoleRights(int roleID, String rights) throws RTPServerException{
		try {
			rtp.exec("Security.SetRoleRights", String.valueOf(roleID), rights);
		} catch (Exception e) {
			throw new RTPServerException(e);
		}
	}
	/**
	 * 添加角色权限的目标对象
	 * @param roleID 角色ID
	 * @param rightID 权限ID
	 * @param targets 目标对象
	 * @param type 类型
	 * @throws RTPServerException 访问RTP服务异常
	 */
	public void addRoleRightTargets(int roleID, int rightID, 
			String targets, String type) throws RTPServerException{
		try {
			rtp.exec("Security.AddRoleRightTargets", String.valueOf(roleID), 
					String.valueOf(rightID), targets, type);
		} catch (Exception e) {
			throw new RTPServerException(e);
		}
	}
	
	
}
