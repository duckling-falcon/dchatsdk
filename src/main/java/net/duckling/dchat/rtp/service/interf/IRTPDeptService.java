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
import java.util.Map;

import net.duckling.dchat.exception.RTPDeptNotFoundException;
import net.duckling.dchat.exception.RTPServerException;
import net.duckling.dchat.rtp.domain.GroupNode;
import net.duckling.dchat.vmt.RTPVmtUser;

/**
 * RTP组织管理服务
 * @author Yangxp
 * @since 2013-08-01
 */
public interface IRTPDeptService {
	/**
	 * 将组织dept移动到newParent下
	 * @param deptPath 待移动组织dept在RTP中的全路径
	 * @param newParentPath 目标父组织newParent在RTP中的全路径
	 * @return 成功与否的标志
	 * @throws RTPServerException 访问RTP服务异常或者RTP操作失败
	 */
	void moveDept(String deptPath, String newParentPath) throws RTPServerException;
	/**
	 * 删除组织，该方法将强制删除组织中的用户，删除后用户都放到默认组织
	 * @param deptPath 组织的全路径
	 * @throws RTPServerException 访问RTP服务异常或者RTP操作失败
	 */
	void removeDept(String deptPath) throws RTPServerException;
	/**
	 * 获取某个组织内的用户
	 * @param deptPath 组织的全路径，例如：中科院/网络中心/软件部
	 * @param recursive 是否递归获取所有子组织的用户：true为递归，false为只获取第一层用户
	 * @return 属于该组织的用户
	 * @throws RTPServerException 访问RTP服务异常或者RTP操作失败
	 * @throws RTPDeptNotFoundException 组织未找到
	 */
	List<RTPVmtUser> getDeptUsers(String deptPath, boolean recursive) throws RTPServerException, RTPDeptNotFoundException;
	/**
	 * 创建组织，并设置排序权重值
	 * @param deptPath 创建的组织路径
	 * @param sortweights 排序权重值，该值只在父组织中使用
	 */
	void addDept(String deptPath, int sortweights) throws RTPServerException;
	/**
	 * 修改组织名称，例如将 [中科院/网络中心/软件部] 修改为 [中科院/网络中心/软件组]，不能修改路径中的某个值，只能修改最后的那个值
	 * @param oldDeptPath 原组织全路径
	 * @param newDeptPath 新组织全路径
	 * @throws RTPServerException 访问RTP服务异常或者RTP操作失败
	 * @throws RTPDeptNotFoundException 部门未找到
	 */
	void renameDept(String oldDeptPath, String newDeptPath) throws RTPServerException;
	/**
	 * 修改组织的排序权重值
	 * @param deptPath 组织的全路径
	 * @param sortWeights VMT中的排序权重值
	 * @throws RTPServerException 
	 * @throws RTPServerException 访问RTP服务异常或者RTP操作失败
	 */
	void updateSortWeights(String deptPath, int sortWeights) throws RTPServerException;
	
	void updateDepartVisbility(String deptPath, boolean isVisible) throws RTPServerException;
	
	
	List<String> getDeptChildren(String deptPath);
	
	Map<String ,String> getUserAndDeptPathMap(List<GroupNode> nodes, String deptPath);
	List<GroupNode> getSubDeptList(String deptPath);
	boolean isDeptExist(String finalNewPath);
}
