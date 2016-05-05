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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.duckling.dchat.email.AdminEmailSender;
import net.duckling.dchat.exception.RTPDeptNotFoundException;
import net.duckling.dchat.exception.RTPServerException;
import net.duckling.dchat.exception.RTPUserNotFoundException;
import net.duckling.dchat.rtp.ViRestClient;
import net.duckling.dchat.rtp.service.interf.IRTPDeptService;
import net.duckling.dchat.rtp.service.interf.IRTPUserService;
import net.duckling.dchat.utils.DChatConstants;
import net.duckling.dchat.utils.UserUtils;
import net.duckling.dchat.vmt.RTPVmtUser;
import net.duckling.dchat.vmt.service.VmtService;
import net.duckling.vmt.api.domain.TreeNode;
import net.duckling.vmt.api.domain.VmtDepart;
import net.duckling.vmt.api.domain.VmtOrg;
import net.duckling.vmt.api.domain.VmtUser;
import net.duckling.vmt.api.domain.message.MQBaseMessage;
import net.duckling.vmt.api.domain.message.MQCreateOrgMessage;
import net.duckling.vmt.api.domain.message.MQDeleteOrgMessage;
import net.duckling.vmt.api.domain.message.MQRefreshOrgMessage;
import net.duckling.vmt.api.domain.message.MQSwitchOrgDchatStatusMessage;
import net.duckling.vmt.api.domain.message.MQSwitchPhoneOrgMessage;
import net.duckling.vmt.api.domain.message.MQUpdateOrgMessage;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import cn.vlabs.rest.ServiceException;
/**
 * VMT组织消息处理器
 * @author Yangxp
 * @since 2013-08-05
 */

public class VMTOrgMessageHandler{
	private static final Logger LOG = Logger.getLogger(VMTOrgMessageHandler.class);
	
	private IRTPDeptService deptService;
	private IRTPUserService userService;
	private VmtService vmtService;
	
	public VMTOrgMessageHandler(IRTPDeptService deptService,
			IRTPUserService userService, VmtService vmtService){
		//vmtService.getVmtUserService().
		this.deptService = deptService;
		this.userService = userService;
		this.vmtService = vmtService;
	}
	
	public void handle(Object message) {
		try{
			if(message instanceof MQCreateOrgMessage){
				//创建VMT组织，VMT组织为LDAP中根节点下第一层，部门为组织下的内容
				MQCreateOrgMessage msg = (MQCreateOrgMessage)message;
				createOrg(msg);
			}else if(message instanceof MQDeleteOrgMessage){
				//删除VMT组织，和删除部门操作类似
				MQDeleteOrgMessage msg = (MQDeleteOrgMessage)message;
				deleteOrg(msg);
			}else if(message instanceof MQRefreshOrgMessage){
				//刷新VMT组织，需要重新构建整个组织
				MQRefreshOrgMessage msg = (MQRefreshOrgMessage)message;
				refreshOrg(msg);
			}else if(message instanceof MQUpdateOrgMessage){
				//更新VMT组织
				MQUpdateOrgMessage msg = (MQUpdateOrgMessage)message;
				updateOrg(msg);
			}else if (message instanceof MQSwitchOrgDchatStatusMessage){
				//开通或关闭科信
				MQSwitchOrgDchatStatusMessage msg = (MQSwitchOrgDchatStatusMessage)message;
				switchOrg(msg);
			}else if(message instanceof MQSwitchPhoneOrgMessage){
				//开通或关闭电话号码隐藏功能
				MQSwitchPhoneOrgMessage msg = (MQSwitchPhoneOrgMessage)message;
				switchPhone(msg);
			}
			else{
				LOG.info("vmt message is invalid, type: org");
			}
		}catch(RuntimeException e){
			String msgInfo = "Runtime Exception! bug or data error! ";
			LOG.error(msgInfo+ e.getMessage(), e);
			AdminEmailSender.getInstance().sendErrorEmail2Admins(e, null, msgInfo);
		}
		LOG.info("finished!");
	}

	private void switchPhone(MQSwitchPhoneOrgMessage msg) {
		if(msg.isVisible()){
			LOG.info("[open phone visible at org] " + msg.toJsonString());
			updatePhoneField(msg.getOrg(), false);
		} else {
			LOG.info("[delete phone field infor at org]" + msg.toJsonString());
			updatePhoneField(msg.getOrg(), true);
		}	
	}
	
	private void updatePhoneField(VmtOrg org, boolean isEmpty){
		try {
			Map<String,String> aa = vmtService.getVmtOrgService().searchUserAttribute(org.getDn(), "telephone");
			for(Map.Entry<String, String> en:aa.entrySet()){
				String phoneNume = isEmpty ? "" : en.getValue();
				try {
					userService.updatePhoneNum(en.getKey(), phoneNume);
				} catch (RTPServerException | RTPUserNotFoundException e) {
					LOG.error("Remove user phone field error "+ e.getMessage(), e);
				}
			}
		} catch (ServiceException e) {
			LOG.error("Update org user phone field error:"+ e.getMessage(), e);
		}
	}
	
	private void rebuildOrg(VmtOrg org, MQBaseMessage msg){
		try {
			deptService.removeDept(UserUtils.formatDept(org.getCurrentDisplay()));
			TreeNode root = vmtService.getVmtOrgService().getTree(org.getDn());
			List<RTPVmtUser> users = new ArrayList<RTPVmtUser>();
			Map<String, Integer> deptSortWeights = new HashMap<String, Integer>();
			fillUsersFromTree(root, users, deptSortWeights); 
			insertUsers(users);
			updateDeptSortWeights(deptSortWeights);
		} catch (RTPServerException e) {
			String msgInfo = "refresh org failed! ";
			LOG.error(msgInfo+ e.getMessage(), e);
			AdminEmailSender.getInstance().sendErrorEmail2Admins(e, msg, msgInfo);
		} catch (ServiceException e) {
			String msgInfo = "refresh org failed! vmt service throw an exception! ";
			LOG.error(msgInfo+ e.getMessage(), e);
			AdminEmailSender.getInstance().sendErrorEmail2Admins(e, msg, msgInfo);
		}
	}

	private void switchOrg(MQSwitchOrgDchatStatusMessage msg) {
		if(msg.isOpen()){
			//Open org means sync org
			LOG.info("[open org] "+msg.toJsonString());
			VmtOrg org = msg.getOrg();
			rebuildOrg(org, msg);
		} else {
			//Close org means delete org
			LOG.info("[close org] "+msg.toJsonString());
			VmtOrg org = msg.getOrg();
			String deptPath = UserUtils.formatDept(org.getCurrentDisplay());
			try {
				List<RTPVmtUser> uslist = deptService.getDeptUsers(deptPath, true);
				for(RTPVmtUser u:uslist){
					userService.removeUserDept(u.getCstnetId(), deptPath); 
				}
				deleteOrgLogoInfo(msg.getOrg());
				deptService.removeDept(deptPath);
			} catch (RTPServerException | RTPDeptNotFoundException | RTPUserNotFoundException   e) {
				String msgInfo = "delete org failed!";
				LOG.error(msgInfo+ e.getMessage(), e);
				AdminEmailSender.getInstance().sendErrorEmail2Admins(e, msg, msgInfo);
			}
		}
	}

	private void createOrg(MQCreateOrgMessage msg){
		LOG.info("[create org] "+msg.toJsonString());
		VmtOrg org = msg.getOrg();
		VmtUser creator = msg.getUser();
		String deptPath = UserUtils.formatDept(org.getCurrentDisplay());
		try {
			updateOrgLogoInfo(org,creator.getCstnetId(),false);
			boolean existFlag = deptService.isDeptExist(deptPath);
			if (existFlag) {
				LOG.error("This org named " + deptPath + " exist, so dchat sdk will ignore it."); //如果这个组织存在所以就忽略掉
			} else {
				deptService.addDept(deptPath, -100);
			}
			if(! userService.isUserExist(creator.getCstnetId())){ //如果创建者不存在dchat中，则自动加入到默认组织中
				userService.addDefaultUser(creator.getCstnetId());
			}
			userService.addUser(RTPVmtUser.buildFromVmtUser(creator));
		} catch (RTPServerException e) {
			String msgInfo = "create org failed!";
			LOG.error(msgInfo+ e.getMessage(), e);
			AdminEmailSender.getInstance().sendErrorEmail2Admins(e, msg, msgInfo);
		}
	}
	
	private void deleteOrgLogoInfo(VmtOrg org){
		try {
			ViRestClient vclient = new ViRestClient(DChatConstants.getWebid(),DChatConstants.getRtpDomain(),9191);
			vclient.deleteVi(org.getCurrentDisplay());
		} catch (Exception e) {
			LOG.error(e.getMessage(),e);
		}
	}

	private void updateOrgLogoInfo(VmtOrg org,String user, boolean isTitleChanged) {
		try {
			ViRestClient vclient = new ViRestClient(DChatConstants.getWebid(),DChatConstants.getRtpDomain(),9191);
			File windowsFile = getOrgLogoFile(org,"windows");
			File androidFile = getOrgLogoFile(org,"android");
			if(isTitleChanged || windowsFile != null || androidFile != null){
				vclient.setVi(org.getName(), windowsFile, androidFile, org.getCurrentDisplay());
			}
		} catch (Exception e) {
			LOG.error(e.getMessage(),e);
		}
	}

	private File getOrgLogoFile(VmtOrg org, String device) {
		String logoUrl = org.getLogo();
		if("windows".equals(device)){
			logoUrl = org.getPcLogo();
		} else {
			logoUrl = org.getMobileLogo();
		}
		if(StringUtils.isEmpty(logoUrl)){
			return null;
		}
		String tempLogoPath = getTempLogoPath(logoUrl,device);
		boolean flag = fetchRemoteLogoFile(logoUrl, tempLogoPath);
		if(flag){
			return new File(tempLogoPath);
		}
		return null;
	}
	
	private String getTempLogoPath(String logoUrl,String suffix) {
		String fileName = logoUrl.substring(logoUrl.lastIndexOf('/')+1);
		String path = DChatConstants.getTempDirPath() + fileName+suffix+".png";
		//System.out.println(path);
		return path;
	}

	public boolean fetchRemoteLogoFile(String httpUrl, String saveFile) {
		int byteread = 0;
		URL url = null;
		try {
			url = new URL(DChatConstants.getVmtDomain()+"/"+httpUrl);
		} catch (MalformedURLException e1) {
			e1.printStackTrace();
			return false;
		}
		FileOutputStream fs = null;
		InputStream inStream = null;
		URLConnection conn = null;
		try {
			conn = url.openConnection();
			inStream = conn.getInputStream();
			fs = new FileOutputStream(saveFile);
			byte[] buffer = new byte[1204];
			while ((byteread = inStream.read(buffer)) != -1) {
				fs.write(buffer, 0, byteread);
			}
			return true;
		} catch (FileNotFoundException e) {
			LOG.error(e.getMessage(),e);
			return false;
		} catch (IOException e) {
			LOG.error(e.getMessage(),e);
			return false;
		} finally {
			IOUtils.closeQuietly(fs);
			IOUtils.closeQuietly(inStream);
		}
	}

	private void deleteOrg(MQDeleteOrgMessage msg){
		LOG.info("[delete org] "+msg.toJsonString());
		VmtOrg org = msg.getOrg();
		String deptPath = UserUtils.formatDept(org.getCurrentDisplay());
		try {
			deleteOrgLogoInfo(msg.getOrg());
			List<RTPVmtUser> uslist = deptService.getDeptUsers(deptPath, true);
			for(RTPVmtUser u:uslist){
				userService.removeUserDept(u.getCstnetId(), deptPath); 
			}
			deptService.removeDept(deptPath);
		} catch (RTPServerException e) {
			String msgInfo = "delete org failed!";
			LOG.error(msgInfo+ e.getMessage(), e);
			AdminEmailSender.getInstance().sendErrorEmail2Admins(e, msg, msgInfo);
		} catch (RTPDeptNotFoundException e) {
			e.printStackTrace();
		} catch (RTPUserNotFoundException e) {
			e.printStackTrace();
		}
	}

	private void updateOrg(MQUpdateOrgMessage msg){
		LOG.info("[update org] "+msg.toJsonString());
		String oldDepartName = msg.getBeforeName();
		String user  = null;
		boolean isTitleChanged = !StringUtils.equals(msg.getBeforeName(), msg.getOrg().getCurrentDisplay());
		if(msg.getAdmins().size() > 0){
			user = msg.getAdmins().get(0).getCstnetId();
		}
		if(StringUtils.isBlank(oldDepartName)){
			updateOrgLogoInfo(msg.getOrg(),user, isTitleChanged);
			return;
		}
		String oldDepartPath = UserUtils.formatDept(oldDepartName);
		VmtOrg org = msg.getOrg();
		String newName = UserUtils.formatDept(org.getCurrentDisplay());
		if(newName.contains("/")){
			newName = newName.substring(newName.lastIndexOf('/'), newName.length());
		}
		try {
			deptService.renameDept(oldDepartPath, newName);
			updateOrgLogoInfo(org,user, isTitleChanged);
		} catch (RTPServerException e) {
			String msgInfo = "rename org failed! ";
			LOG.error(msgInfo+ e.getMessage(), e);
			AdminEmailSender.getInstance().sendErrorEmail2Admins(e, msg, msgInfo);
		}
	}
	
	private void refreshOrg(MQRefreshOrgMessage msg){//组织机构同步
		LOG.info("[refresh org] "+msg.toJsonString());
		VmtOrg org = msg.getOrg();
		if(org.isOpenDchat()){			
			try {
				deptService.removeDept(UserUtils.formatDept(org.getCurrentDisplay()));
				TreeNode root = vmtService.getVmtOrgService().getTree(org.getDn());
				List<RTPVmtUser> users = new ArrayList<RTPVmtUser>();
				Map<String, Integer> deptSortWeights = new HashMap<String, Integer>();
				fillUsersFromTree(root, users, deptSortWeights); 
				insertUsers(users);
			} catch (RTPServerException e) {
				String msgInfo = "refresh org failed! ";
				LOG.error(msgInfo+ e.getMessage(), e);
				AdminEmailSender.getInstance().sendErrorEmail2Admins(e, msg, msgInfo);
			} catch (ServiceException e) {
				String msgInfo = "refresh org failed! vmt service throw an exception! ";
				LOG.error(msgInfo+ e.getMessage(), e);
				AdminEmailSender.getInstance().sendErrorEmail2Admins(e, msg, msgInfo);
			}
		}
	}
	
	private void fillUsersFromTree(TreeNode root, List<RTPVmtUser> users, Map<String, Integer> deptSortWeights){
		if(null == root){
			return;
		}
		Object data = root.getData();
		if(data instanceof VmtUser){
			VmtUser user = (VmtUser)data;
			if(!user.isDisableDchat()){
				RTPVmtUser rtpUser = RTPVmtUser.buildFromVmtUser(user);
				users.add(rtpUser);
			}
		}else{
			if(data instanceof VmtDepart){
				VmtDepart depart = (VmtDepart)data;
				String deptPath = UserUtils.formatDept(depart.getCurrentDisplay());
				try {
					deptService.addDept(deptPath, depart.getListRank());
					deptService.updateDepartVisbility(deptPath, depart.isVisible());
				} catch (RTPServerException e) {
					e.printStackTrace();
				}
				deptSortWeights.put(deptPath, depart.getListRank());
			}
			List<TreeNode> nodes = root.getChildren();
			for(TreeNode node : nodes){
				fillUsersFromTree(node, users, deptSortWeights);
			}
		}
	}
	
	private void insertUsers(List<RTPVmtUser> users){
		int i=0;
		int size = users.size();
		for(RTPVmtUser user : users){
			try{
				userService.addUser(user);
				LOG.info("("+(++i)+"/"+size+") "+user.getCstnetId());
			}catch(RTPServerException e){
				LOG.error("("+(++i)+"/"+size+") add "+user.getCstnetId()+" failed! "+user.getCurrentDisplay(), e);
			}
		}
	}
	
	private void updateDeptSortWeights(Map<String, Integer> deptSortWeights){
		LOG.info("start update department sort weights");
		int i=0;
		int size = deptSortWeights.size();
		for(Map.Entry<String, Integer> entry : deptSortWeights.entrySet()){
			try{
				deptService.updateSortWeights(entry.getKey(), entry.getValue());
				LOG.info("("+(++i)+"/"+size+") "+entry.getKey()+" "+entry.getValue());
			}catch(RTPServerException e){
				LOG.info("("+(++i)+"/"+size+") [failed] "+entry.getKey()+" "+entry.getValue());
			}
		}
	}
}
