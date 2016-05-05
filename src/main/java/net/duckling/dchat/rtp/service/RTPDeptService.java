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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import net.duckling.dchat.exception.RTPDeptNotFoundException;
import net.duckling.dchat.exception.RTPServerException;
import net.duckling.dchat.rtp.dao.RtpGroupDAO;
import net.duckling.dchat.rtp.dao.RtpGroupLevelDAO;
import net.duckling.dchat.rtp.dao.RtpGroupUserDAO;
import net.duckling.dchat.rtp.dao.RtpLevelDAO;
import net.duckling.dchat.rtp.domain.GroupNode;
import net.duckling.dchat.rtp.domain.Level;
import net.duckling.dchat.rtp.domain.RtpGroup;
import net.duckling.dchat.rtp.service.interf.IRTPDeptService;
import net.duckling.dchat.utils.RtpServerUtils;
import net.duckling.dchat.vmt.RTPVmtUser;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.rooyeetone.rtp.sdk.IDataItem;
import com.rooyeetone.rtp.sdk.IPagedResult;
import com.rooyeetone.rtp.sdk.ISyncItem;
/**
 * RTP的部门管理服务实现类
 * @author Yangxp
 * @since 2013-08-01
 */
@Service
public class RTPDeptService extends BaseRTPService implements IRTPDeptService {
	private static final Logger LOG = Logger.getLogger(RTPDeptService.class);
	private static final String DEPT = "DEPT";
	
	@Autowired
	private RtpGroupLevelDAO groupLevelDao;
	@Autowired
	private RtpGroupDAO groupDao;
	@Autowired
	private RtpGroupUserDAO groupUserDao;
	@Autowired
	private RtpLevelDAO levelDao;
	
	public boolean isDepartVisible(String deptPath){
		RtpGroup rg = groupDao.getRtpGroupByPath(deptPath);
		Level level = groupLevelDao.queryByGroup(rg.getGroupID());
		if(level == null){
			return false;
		}
		return level.isLevelVisible();
	}
	
	public void updateDepartVisbility(String deptPath, boolean isVisible) throws RTPServerException{
		String[] array = null;
		array = deptPath.split("/");
		int leveIndex = array.length;
		RtpGroup rg = groupDao.getRtpGroupByPath(deptPath);
		if( rg != null){
			try {
				int levelID = levelDao.query(leveIndex,isVisible);
				LOG.info("Change visible for "+deptPath + " to levelIndex:" +leveIndex +", visible:"+ isVisible);
				ISyncItem item = rtp.exec("syncData", deptPath, DEPT, "UPDATE");
				item.setProp("level", levelID+"");
				item.commit();
			}catch(Exception e) {
				LOG.error(e.getMessage(),e);
			}
		}
	}


	@Override
	public void moveDept(String deptPath, String newParentPath) throws RTPServerException{
		try {
			ISyncItem item = rtp.exec("syncData", deptPath, DEPT, "CHANGEPARENT");
			item.setProp("parentgroup", newParentPath);
			item.commit();
		} catch (Exception e) {
			throw new RTPServerException(e);
		}
	}

	@Override
	public void removeDept(String deptPath) throws RTPServerException {
		try {
			rtp.exec("DelDept", deptPath, Boolean.TRUE);
		} catch (Exception e) {
			String msg = e.getMessage();
			// 31020代表部門不存在時刪除用戶的代碼；31021非強制刪除時，部門下還有用戶時拋出的錯誤代碼
			boolean isDeptNotFoundException = msg.contains("31020") || msg.indexOf("GroupNotFoundException")>=0 ;
			if(!isDeptNotFoundException){
				throw new RTPServerException(e);
			}
		}
	}

	@Override
	public List<RTPVmtUser> getDeptUsers(String deptPath, boolean recursive)
			throws RTPServerException, RTPDeptNotFoundException {
		List<RTPVmtUser> users = new ArrayList<RTPVmtUser>();
		try {
			IPagedResult result = rtp.exec("GetDeptUser", deptPath, Boolean.valueOf(recursive));
			for(IDataItem item : result.getItems()){
				RTPVmtUser user = new RTPVmtUser();
				user.setCstnetId(item.getProp("user"));
				user.setName(item.getProp("fullname"));
				user.setCurrentDisplay(item.getProp("nickname"));
				
				users.add(user);
			}
		} catch (Exception e) {
			String msg = e.getMessage();
			if(StringUtils.isNotBlank(msg) && msg.contains("GroupNotFoundException")){
				throw new RTPDeptNotFoundException(deptPath);
			}else{
				throw new RTPServerException(e);
			}
		}
		return users;
	}

	@Override
	public void addDept(String deptPath, int sortweights)
			throws RTPServerException {
		try {
			int realSortWeights = RtpServerUtils.getRTPSortWeights(sortweights);
			ISyncItem item = rtp.exec("SyncData", deptPath, DEPT, "INSERT");
			item.setProp("sortweights", String.valueOf(realSortWeights));
			item.commit();
			LOG.info("Add dept "+deptPath+" with sortweights = " + realSortWeights);
		} catch (Exception e) {
			throw new RTPServerException(e);
		}
	}

	@Override
	public void renameDept(String oldDeptPath, String newDeptPath)
			throws RTPServerException {
		try {
			ISyncItem item = rtp.exec("SyncData", oldDeptPath, DEPT, "RENAME");
			item.setProp("newname", newDeptPath);
			item.commit();
		} catch (Exception e) {
			throw new RTPServerException(e);
		}
	}

	@Override
	public void updateSortWeights(String deptPath, int sortWeights) throws RTPServerException {
		try {
			int realSortWeights = RtpServerUtils.getRTPSortWeights(sortWeights);
			ISyncItem item = rtp.exec("SyncData", deptPath, DEPT, "UPDATE");
			item.setProp("sortweights", String.valueOf(realSortWeights));
			item.commit();
			LOG.info("update department ["+deptPath+"] 's sortweight to ["+sortWeights+"]");
		} catch (Exception e) {
			throw new RTPServerException(e);
		}
	}
	
	/*
	 * 获得某个节点下所有子部门信息,包括无人的组织和组织可见性
	 * 
	 */
	public List<GroupNode> getSubDeptList(String deptPath){
		Queue<String> queue = new LinkedBlockingQueue<String>();
		String current = null;
		queue.add(deptPath);
		List<GroupNode> groupList = new ArrayList<GroupNode>();
		while(!queue.isEmpty()){
			current = queue.poll();
			RtpGroup cn = groupDao.getRtpGroupByPath(current);
			if(cn == null){
				LOG.error("can not find the dept of " + current);
			} else{
				GroupNode node = new GroupNode();
				Level l = groupLevelDao.queryByGroup(cn.getGroupID());
				node.setPath(current);
				if(l == null){
					node.setLevel(null);
				}else{
					node.setLevel(l.getLevelName());
				}
				node.setCurrent(cn.getGroupName());
				node.setParentID(cn.getParentID());
				node.setGroupID(cn.getGroupID());
				groupList.add(node);
			}
			
			List<RtpGroup> children  = groupDao.getRtpGroupChildren(current);
			if(children != null){
				for(RtpGroup n:children){
					String subPath = current + "/" +n.getGroupName();
					queue.add(subPath);
				}
			}
		}
		return groupList;
	}
	
	/*
	 * 获得某个节点下所有人 以及 该人所在的组织路径
	 * user -> deptPathOfUser
	 */
	public Map<String ,String> getUserAndDeptPathMap(List<GroupNode> nodeList, String deptPath){
		Map<String, String> result = new HashMap<String, String>();
		for(GroupNode n:nodeList){
			List<String> users = groupUserDao.getUsersAtGroup(n.getGroupID());
			if(users!= null){		
				for(String u:users){ 
					result.put(u, n.getPath());
				}
			}
		}
		return result;
	}

	@Override
	public List<String> getDeptChildren(String deptPath) {
		List<RtpGroup> childrenNodes  = groupDao.getRtpGroupChildren(deptPath);
		List<String> result = new ArrayList<String>();
		for(RtpGroup g:childrenNodes){
			result.add(groupDao.getRtpGroupPath(g.getGroupID()));
		}
		return result;
	}

	@Override
	public boolean isDeptExist(String finalNewPath) {
		RtpGroup rtpGroup = groupDao.getRtpGroupByPath(finalNewPath);
		return (rtpGroup != null);
	}
	
}
