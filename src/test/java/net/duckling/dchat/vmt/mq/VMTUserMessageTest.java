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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import net.duckling.dchat.exception.RTPGroupNotFoundException;
import net.duckling.dchat.exception.RTPServerException;
import net.duckling.dchat.exception.RTPUserNotFoundException;
import net.duckling.dchat.rtp.domain.UserJID;
import net.duckling.dchat.rtp.service.interf.IRTPGroupService;
import net.duckling.dchat.rtp.service.interf.IRTPUserService;
import net.duckling.dchat.utils.GroupServiceUtils;
import net.duckling.dchat.utils.UserUtils;
import net.duckling.dchat.vmt.RTPChatGroup;
import net.duckling.dchat.vmt.RTPVmtUser;
import net.duckling.dchat.vmt.service.VmtService;
import net.duckling.vmt.api.IRestGroupService;
import net.duckling.vmt.api.domain.VmtDepart;
import net.duckling.vmt.api.domain.VmtGroup;
import net.duckling.vmt.api.domain.VmtOrg;
import net.duckling.vmt.api.domain.VmtUser;
import net.duckling.vmt.api.domain.message.MQBaseMessage;
import net.duckling.vmt.api.domain.message.MQLinkUserMessage;
import net.duckling.vmt.api.domain.message.MQMoveUserMessage;
import net.duckling.vmt.api.domain.message.MQUnlinkUserMessage;
import net.duckling.vmt.api.domain.message.MQUpdateUserMessage;

import org.easymock.EasyMock;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import cn.vlabs.rest.ServiceException;

@RunWith(PowerMockRunner.class)
@PrepareForTest({GroupServiceUtils.class, UserUtils.class})
public class VMTUserMessageTest {
	private static final String DN = "dn";
	private static final String CSTNET_ID = "cstnetid";
	private IRTPUserService userService;
	private IRTPGroupService groupService;
	private VmtService vmtService;
	
	private VMTUserMessageHandler createHandler(){
		userService = PowerMock.createMock(IRTPUserService.class);
		groupService = PowerMock.createMock(IRTPGroupService.class);
		vmtService = PowerMock.createMock(VmtService.class);
		return new VMTUserMessageHandler(userService, groupService, vmtService);
	}
	
	@Test
	public void testAddGroupMember__Exist() throws RTPServerException, RTPGroupNotFoundException{
		MQLinkUserMessage msg = (MQLinkUserMessage)createMsg(true,true);
		VMTUserMessageHandler handler = createHandler();
		EasyMock.expect(groupService.isGroupExist(msg.getGroup().getSymbol())).andReturn(Boolean.TRUE);
		EasyMock.expect(groupService.addMembers(EasyMock.anyObject(String.class), 
				EasyMock.<List<String>>anyObject(), EasyMock.<List<String>>anyObject())).andReturn(new ArrayList<String>());
		
		PowerMock.replayAll();
		handler.handle(msg);
		PowerMock.verifyAll();
	}
	
	@Test
	public void testAddGroupMember__NotExist() throws RTPServerException{
		MQLinkUserMessage msg = (MQLinkUserMessage)createMsg(true, true);
		VMTUserMessageHandler handler = createHandler();
		EasyMock.expect(groupService.isGroupExist(msg.getGroup().getSymbol())).andReturn(Boolean.FALSE);
		PowerMock.mockStatic(GroupServiceUtils.class);
		EasyMock.expect(GroupServiceUtils.rebuildGroup(vmtService, groupService, msg.getGroup())).andReturn(Boolean.FALSE);
		
		PowerMock.replayAll();
		handler.handle(msg);
		PowerMock.verifyAll();
	}
	
	@Test
	public void testAddDeptUser() throws RTPServerException{
		MQLinkUserMessage msg = (MQLinkUserMessage)createMsg(false, true);
		VMTUserMessageHandler handler = createHandler();
		EasyMock.expect(userService.addUser(EasyMock.anyObject(RTPVmtUser.class))).andReturn(new UserJID("x","x")).times(2);
		
		PowerMock.replayAll();
		handler.handle(msg);
		PowerMock.verifyAll();
	}
	
	private MQBaseMessage createMsg(boolean isGroup, boolean isLink){
		List<VmtUser> users = new ArrayList<VmtUser>();
		VmtUser user = new VmtUser();
		user.setCstnetId(CSTNET_ID);
		VmtUser user2 = new VmtUser();
		user2.setCstnetId("x");
		users.add(user);
		users.add(user2);
		
		VmtOrg org = null;
		VmtDepart depart = null;
		VmtGroup group = null;
		if(!isGroup){
			org = new VmtOrg();
			org.setDn(DN);
			depart= new VmtDepart();
			depart.setDn(DN);
		}else{
			group = new VmtGroup();
			group.setDn(DN);
			group.setSymbol("groupID");
		}
		return isLink?new MQLinkUserMessage(users, org, depart, group):
			new MQUnlinkUserMessage(users, org, depart, group);
	}
	
	@Test
	public void testRemoveGroupMember__Member() throws RTPServerException, RTPGroupNotFoundException, ServiceException{
		MQUnlinkUserMessage msg = (MQUnlinkUserMessage)createMsg(true, false);
		VMTUserMessageHandler handler = createHandler();
		RTPChatGroup group = new RTPChatGroup();
		group.setOwner(CSTNET_ID);
		group.setAdmins(new HashSet<String>(Arrays.asList("x")));
		EasyMock.expect(groupService.getGroup(EasyMock.anyObject(String.class))).andReturn(group);
		IRestGroupService vmtGroupService = PowerMock.createMock(IRestGroupService.class);
		EasyMock.expect(vmtService.getVmtGroupService()).andReturn(vmtGroupService);
		List<VmtUser> users = new ArrayList<VmtUser>();
		VmtUser user = new VmtUser();
		user.setCstnetId("aa");
		users.add(user);
		EasyMock.expect(vmtGroupService.getAdmins(EasyMock.anyObject(String.class))).andReturn(users);
		groupService.transferGroup(EasyMock.anyObject(String.class), EasyMock.anyObject(String.class), EasyMock.anyBoolean());
		EasyMock.expectLastCall();
		EasyMock.expect(groupService.removeMembers(EasyMock.anyObject(String.class), 
				EasyMock.<List<String>>anyObject())).andReturn(new ArrayList<String>());
		
		PowerMock.replayAll();
		handler.handle(msg);
		PowerMock.verifyAll();
	}
	
	@Test
	public void testRemoveGroupMember__Owner() throws RTPServerException, ServiceException, RTPGroupNotFoundException{
		MQUnlinkUserMessage msg = (MQUnlinkUserMessage)createMsg(true, false);
		VMTUserMessageHandler handler = createHandler();
		RTPChatGroup group = new RTPChatGroup();
		group.setOwner("b");
		EasyMock.expect(groupService.getGroup(EasyMock.anyObject(String.class))).andReturn(group);
		EasyMock.expect(groupService.removeMembers(EasyMock.anyObject(String.class), 
				EasyMock.<List<String>>anyObject())).andThrow(new RTPGroupNotFoundException("x"));
		
		PowerMock.replayAll();
		handler.handle(msg);
		PowerMock.verifyAll();
	}
	
	@Test
	public void testRemoveDeptUser() throws RTPServerException, RTPUserNotFoundException{
		MQUnlinkUserMessage msg = (MQUnlinkUserMessage)createMsg(false, false);
		VMTUserMessageHandler handler = createHandler();
		userService.updateUserDept(EasyMock.anyObject(String.class),"", EasyMock.anyObject(String.class));
		EasyMock.expectLastCall().times(2);
		
		PowerMock.replayAll();
		handler.handle(msg);
		PowerMock.verifyAll();
	}
	
	@Test
	public void testUpdateUser() throws RTPServerException, RTPUserNotFoundException{
		MQUpdateUserMessage msg = createUpdateMsg();
		VMTUserMessageHandler handler = createHandler();
		userService.updateUser(EasyMock.anyObject(String.class), EasyMock.anyObject(RTPVmtUser.class));
		EasyMock.expectLastCall().andThrow(new RTPUserNotFoundException("xx"));
		EasyMock.expect(userService.addUser(EasyMock.anyObject(RTPVmtUser.class))).andThrow(new RTPServerException(new Exception()));
		
		PowerMock.replayAll();
		handler.handle(msg);
		PowerMock.verifyAll();
	}
	
	private MQUpdateUserMessage createUpdateMsg(){
		VmtUser user = new VmtUser();
		user.setCstnetId(CSTNET_ID);
		return new MQUpdateUserMessage(user);
	}
	
	@Test
	public void testMoveUserMessage() throws RTPServerException, RTPUserNotFoundException{
		MQMoveUserMessage msg = createMoveMsg();
		VMTUserMessageHandler handler = createHandler();
		PowerMock.mockStatic(UserUtils.class);
		String targetPath = "x";
		EasyMock.expect(UserUtils.formatDept(EasyMock.anyObject(String.class))).andReturn(targetPath);
		userService.updateUserDept(CSTNET_ID,"", targetPath);
		EasyMock.expectLastCall().andThrow(new RTPServerException(new Exception()));
		userService.updateUserDept(CSTNET_ID+"2","", targetPath);
		EasyMock.expectLastCall().andThrow(new RTPUserNotFoundException("xxxx"));
		
		PowerMock.replayAll();
		handler.handle(msg);
		PowerMock.verifyAll();
	}
	
	private MQMoveUserMessage createMoveMsg(){
		List<VmtUser> users = new ArrayList<VmtUser>();
		VmtUser user = new VmtUser();
		user.setCstnetId(CSTNET_ID);
		VmtUser user2 = new VmtUser();
		user2.setCstnetId(CSTNET_ID+"2");
		users.add(user);
		users.add(user2);
		
		VmtOrg org = new VmtOrg();
		org.setDn(DN);
		VmtDepart depart = new VmtDepart();
		depart.setDn(DN);
		depart.setCurrentDisplay("currentDisplay");
		return new MQMoveUserMessage(users, org, depart);
	}
}
