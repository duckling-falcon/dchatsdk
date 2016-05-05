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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;

import net.duckling.dchat.email.AdminEmailSender;
import net.duckling.dchat.exception.RTPServerException;
import net.duckling.dchat.exception.RTPUserNotFoundException;
import net.duckling.dchat.rtp.domain.GroupNode;
import net.duckling.dchat.rtp.service.interf.IRTPDeptService;
import net.duckling.dchat.rtp.service.interf.IRTPUserService;
import net.duckling.dchat.utils.UserUtils;
import net.duckling.dchat.vmt.service.VmtService;
import net.duckling.vmt.api.domain.VmtDepart;
import net.duckling.vmt.api.domain.VmtOrg;
import net.duckling.vmt.api.domain.message.MQCreateDepartMessage;
import net.duckling.vmt.api.domain.message.MQDeleteDepartMessage;
import net.duckling.vmt.api.domain.message.MQMoveDepartMessage;
import net.duckling.vmt.api.domain.message.MQUpdateDepartMessage;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
/**
 * VMT部门消息处理器
 * @author Yangxp
 * @since 2013-08-05
 */

public class VMTDeptMessageHandler{
	private static final Logger LOG = Logger.getLogger(VMTDeptMessageHandler.class);
	
	private IRTPDeptService deptService;
	private IRTPUserService userService;
	@SuppressWarnings("unused")
	private VmtService vmtService;
	
	public VMTDeptMessageHandler(IRTPDeptService dept, IRTPUserService user,
			VmtService vmt){
		deptService = dept;
		userService = user;
		vmtService = vmt;
	}
	
	public void handle(Object message) {
		try{
			if(message instanceof MQCreateDepartMessage){//创建VMT部门
				MQCreateDepartMessage msg = (MQCreateDepartMessage)message;
				createDepartment(msg);
			}else if(message instanceof MQDeleteDepartMessage){//删除VMT部门，部门内可能有子部门，删除部门时内部用户都会删除，整个操作只一条消息，不会拆分成多条
				MQDeleteDepartMessage msg = (MQDeleteDepartMessage)message;
				deleteDepartment(msg);
			}else if(message instanceof MQUpdateDepartMessage){//更新VMT部门属性
				MQUpdateDepartMessage msg = (MQUpdateDepartMessage)message;
				updateDepartment(msg);
			}else if(message instanceof MQMoveDepartMessage){//移动部门
				MQMoveDepartMessage msg = (MQMoveDepartMessage)message;
				moveDepartment2(msg);
			}else{
				LOG.info("vmt message is invalid, type: dept");
			}
		}catch(RuntimeException e){
			String msgInfo = "Runtime Exception! ";
			LOG.error(msgInfo+ e.getMessage(), e);
			AdminEmailSender.getInstance().sendErrorEmail2Admins(e, null, msgInfo);
		}
		LOG.info("finished!");
	}

	private void createDepartment(MQCreateDepartMessage msg){
		LOG.info("[create department] "+msg.toJsonString());
		VmtDepart dept = msg.getDept();
		String deptPath = UserUtils.formatDept(dept.getCurrentDisplay());
		try {
			deptService.addDept(deptPath, dept.getListRank());
			deptService.updateDepartVisbility(deptPath, dept.isVisible());
		} catch (RTPServerException e) {
			String msgInfo = "create department failed!";
			LOG.error(msgInfo+ e.getMessage(), e);
			AdminEmailSender.getInstance().sendErrorEmail2Admins(e, msg, msgInfo);
		}
	}
	
	private void deleteDepartment(MQDeleteDepartMessage msg){
		LOG.info("[delete department] "+msg.toJsonString());
		VmtDepart dept = msg.getDept();
		String deptPath = UserUtils.formatDept(dept.getCurrentDisplay());
		try {
			deptService.removeDept(deptPath);
		} catch (RTPServerException e) {
			String msgInfo = "delete department failed!";
			LOG.error(msgInfo+ e.getMessage(), e);
			AdminEmailSender.getInstance().sendErrorEmail2Admins(e, msg, msgInfo);
		}
	}
	
	private void updateDepartment(MQUpdateDepartMessage msg){
		msg.getDept();
		LOG.info("[update department] "+msg.toJsonString());
		String oldDeptPath = UserUtils.formatDept(msg.getCurrentDept());
		VmtDepart depart = msg.getDept();
		String newDeptPath = UserUtils.formatDept(depart.getCurrentDisplay());
		String newDeptName = newDeptPath;
		if(newDeptName.contains("/")){
			newDeptName = newDeptName.substring(newDeptName.lastIndexOf('/')+1, newDeptName.length());
		}
		try {
			String tmpPath = (oldDeptPath != null)? oldDeptPath : newDeptPath; //需要获得以前的旧路径来更新每个人的属性
			List<GroupNode> nodes = deptService.getSubDeptList(tmpPath); //更新前获得所有子节点及自身节点
			for(GroupNode node:nodes){
				deptService.updateDepartVisbility(node.getPath(), depart.isVisible());
			}
			if(StringUtils.isNotBlank(oldDeptPath)){
				deptService.renameDept(oldDeptPath, newDeptName);
				updateDeptUsersProp(nodes, oldDeptPath, newDeptPath);
			}
			deptService.updateSortWeights(newDeptPath, depart.getListRank());
		} catch (RTPServerException e) {
			String msgInfo = "rename/update department sort weight failed! from "+oldDeptPath+" to "+newDeptName+", "+msg.getCurrentDept();
			LOG.error(msgInfo+ e.getMessage(), e);
			AdminEmailSender.getInstance().sendErrorEmail2Admins(e, msg, msgInfo);
		} 
	}
	
	/*
	 * 更新所有组织下的用户信息,需要递归遍历所有用户
	 */
	private void updateDeptUsersProp(List<GroupNode> nodes, String sourceDeptPath, String targetDeptPath){
		Map<String,String> map = deptService.getUserAndDeptPathMap(nodes, sourceDeptPath);
		for(Entry<String,String> en:map.entrySet()){
			String newPath = en.getValue().replace(sourceDeptPath, targetDeptPath);
			try {
				userService.updateUserDept(en.getKey(), en.getValue(), newPath);
				LOG.info("update user dept for " + en.getKey()+" from "+ en.getValue()+ " to "+ newPath);
			} catch (RTPServerException | RTPUserNotFoundException e) {
				LOG.error( e.getMessage(), e);
			}
		}
	}
	
	private Map<String,GroupNode> changeToNewPath(List<GroupNode> nodeList, String src, String des){
		Map<String,GroupNode> map = new HashMap<String,GroupNode>();
		for(GroupNode n:nodeList){
			map.put(n.getPath().replace(src, des), n);
		}
		return map;
	}
	
	private Set<String> getPathSet(List<GroupNode> nodeList){
		Set<String> set = new TreeSet<String>();
		for(GroupNode n:nodeList){
			set.add(n.getPath());
		}
		return set;
	}
	
	public void moveDepartment2(MQMoveDepartMessage msg) {
		try {
			LOG.info("[move department] " + msg.toJsonString());
			VmtDepart sourceDept = msg.getDept();
			Boolean isContainSelf = msg.isContainSelf();
			String sourceDeptPath = UserUtils.formatDept(sourceDept.getCurrentDisplay());
			String targetDeptPath = getTargetDeptPath(msg.getTargetOrg(), msg.getTargetDept(), sourceDept,
					isContainSelf);
			List<GroupNode> nodes = deptService.getSubDeptList(sourceDeptPath); // 更新前获得所有子节点及自身节点
			boolean isTargetVisible = (msg.getTargetDept() == null) ? (msg.getDept().isVisible()) : msg.getTargetDept()
					.isVisible();
			String finalNewPath = (isContainSelf) ? targetDeptPath + "/" + sourceDept.getName() : targetDeptPath;
			// change all directory to new path
			Map<String, GroupNode> toUpdateFolders = changeToNewPath(nodes, sourceDeptPath, finalNewPath);
			// get all target directory list
			List<GroupNode> desFolders = deptService.getSubDeptList(targetDeptPath);
			Set<String> desPathSet = getPathSet(desFolders);
			// compare the two list to find the conflict file path
			Stack<GroupNode> toDeleteNodes = new Stack<GroupNode>();
			for (Entry<String, GroupNode> en : toUpdateFolders.entrySet()) {
				// if conflict then
				if (desPathSet.contains(en.getKey())) { 
					toDeleteNodes.add(en.getValue());
				} else {
					String newPath = en.getKey().substring(0,en.getKey().lastIndexOf('/'))+"/";
					LOG.info("move dept from " + en.getValue().getPath() + " to " + newPath);
					deptService.moveDept(en.getValue().getPath(), newPath);
				}
			}
			// update visibility
			updateSubDeptsVisible(isContainSelf, sourceDeptPath, nodes, finalNewPath, isTargetVisible);
			// update user info
			updateDeptUsersProp(nodes, sourceDeptPath, finalNewPath);// 更新原组织下的所有用户的部门信息
			while(!toDeleteNodes.isEmpty()){
				GroupNode gn = toDeleteNodes.pop();
				LOG.info("remove sub dept "+ gn.getPath());
				deptService.removeDept(gn.getPath());
			}
			if (!isContainSelf) { // 这时原节点已经是空目录了，需要删除
				deptService.removeDept(sourceDeptPath);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void updateSubDeptsVisible(Boolean isContainSelf, String sourceDeptPath, List<GroupNode> nodes,
			String targetDeptPath, boolean targetDeptIsVisible) throws RTPServerException {
		int index = 0;
		if(nodes.size()==0){
			LOG.error("you should not be empty");
		}
		for(GroupNode n:nodes){
			String newPath = n.getPath().replace(sourceDeptPath, targetDeptPath);
			if(index != 0 || isContainSelf){
				deptService.updateDepartVisbility(newPath, targetDeptIsVisible);
			}
			index++;
		}
	}
	
	private String getTargetDeptPath(VmtOrg targetOrg, VmtDepart targetDept, VmtDepart sourceDept, boolean isContainSelf){
		String targetDeptPath = "";
		if(null != targetDept){
			targetDeptPath = UserUtils.formatDept(targetDept.getCurrentDisplay());
		}else if(null != targetOrg){
			targetDeptPath = UserUtils.formatDept(targetOrg.getCurrentDisplay());
		}else{
			LOG.info("no target department or org while move depart "+ UserUtils.formatDept(sourceDept.getCurrentDisplay()));
		}
		return targetDeptPath;
	}
}
