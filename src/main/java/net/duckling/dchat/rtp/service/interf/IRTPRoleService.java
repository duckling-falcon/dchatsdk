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

import net.duckling.dchat.exception.RTPDeptNotFoundException;
import net.duckling.dchat.exception.RTPServerException;

/**
 * RTP角色服务：供设置权限
 * @author Yangxp
 * @since 2013-08-12
 */
public interface IRTPRoleService {
	/**
	 * 设置部门的默认角色，每个部门的成员只能看见自己的部门
	 * @param deptPath 部门的全路径
	 * @throws RTPServerException 访问RTP服务异常
	 * @throws RTPDeptNotFoundException 部门未找到
	 */
	void setDefaultRoleForDept(String deptPath) throws RTPServerException, RTPDeptNotFoundException;
}
