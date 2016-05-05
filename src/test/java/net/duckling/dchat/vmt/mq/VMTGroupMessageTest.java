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
import java.util.HashSet;
import java.util.List;

import net.duckling.dchat.exception.RTPGroupNotFoundException;
import net.duckling.dchat.exception.RTPServerException;
import net.duckling.dchat.rtp.domain.GroupJID;
import net.duckling.dchat.rtp.service.interf.IRTPGroupService;
import net.duckling.dchat.rtp.service.interf.IRTPUserService;
import net.duckling.dchat.utils.GroupServiceUtils;
import net.duckling.dchat.utils.RtpServerUtils;
import net.duckling.dchat.utils.UserUtils;
import net.duckling.dchat.vmt.RTPChatGroup;
import net.duckling.dchat.vmt.service.VmtService;
import net.duckling.vmt.api.IRestUserService;
import net.duckling.vmt.api.domain.VmtGroup;
import net.duckling.vmt.api.domain.VmtUser;
import net.duckling.vmt.api.domain.message.MQCreateGroupMessage;
import net.duckling.vmt.api.domain.message.MQDeleteGroupMessage;
import net.duckling.vmt.api.domain.message.MQRefreshGroupMessage;
import net.duckling.vmt.api.domain.message.MQUpdateGroupMessage;

import org.easymock.EasyMock;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import cn.vlabs.rest.ServiceException;

import com.rooyeetone.rtp.sdk.RtpSvc;

@RunWith(PowerMockRunner.class)
@PrepareForTest({RtpSvc.class, RtpServerUtils.class, UserUtils.class, GroupServiceUtils.class})
public class VMTGroupMessageTest {
	
	private String cstnetID = "cstnetid";
	private String groupID = "groupid";
	private IRTPGroupService groupService;
	private IRTPUserService userService;
	private VmtService vmtService;
	
	private VMTGroupMessageHandler createHandler(){
		groupService = PowerMock.createMock(IRTPGroupService.class);
		vmtService = PowerMock.createMock(VmtService.class);
		userService = PowerMock.createMock(IRTPUserService.class);
		return new VMTGroupMessageHandler(groupService, userService, vmtService);
	}
	
	@Test
	public void testCreateGroup() throws RTPServerException{
		MQCreateGroupMessage msg = createCreateMsg();
		VMTGroupMessageHandler handler = createHandler();
		EasyMock.expect(groupService.isGroupExist(groupID)).andReturn(Boolean.FALSE);
		RTPServerException e = new RTPServerException(new Exception());
		EasyMock.expect(groupService.addGroup(EasyMock.anyObject(RTPChatGroup.class))).andThrow(e);
		
		PowerMock.replayAll();
		handler.handle(msg);
		PowerMock.verifyAll();
	}
	
	private MQCreateGroupMessage createCreateMsg(){
		VmtGroup group = new VmtGroup();
		group.setSymbol(groupID);
		group.setCreator(cstnetID);
		
		VmtUser creator = new VmtUser();
		creator.setCstnetId(cstnetID);
		return new MQCreateGroupMessage(group, creator);
	}
	
	@Test
	public void testDeleteGroup() throws RTPServerException{
		MQDeleteGroupMessage msg = createDelMsg();
		VMTGroupMessageHandler handler = createHandler();
		groupService.removeGroup(EasyMock.anyObject(GroupJID.class));
		RTPServerException e = new RTPServerException(new Exception());
		EasyMock.expectLastCall().andThrow(e);
		
		PowerMock.replayAll();
		handler.handle(msg);
		PowerMock.verifyAll();
	}
	
	private MQDeleteGroupMessage createDelMsg(){
		VmtGroup group = new VmtGroup();
		group.setSymbol(groupID);
		return new MQDeleteGroupMessage(group);
	}
	
	@Test
	public void testUpdateGroup__RTPNoGroup() throws RTPServerException{
		MQUpdateGroupMessage msg = createUpdateMsg();
		VMTGroupMessageHandler handler = createHandler();
		EasyMock.expect(groupService.getGroup(groupID)).andReturn(null);
		PowerMock.mockStatic(GroupServiceUtils.class);
		EasyMock.expect(GroupServiceUtils.rebuildGroup(vmtService, groupService, msg.getGroup())).andReturn(Boolean.TRUE);
		
		PowerMock.replayAll();
		handler.handle(msg);
		PowerMock.verifyAll();
	}
	
	@Test
	public void testUpdateGroup() throws RTPServerException, RTPGroupNotFoundException, ServiceException{
		MQUpdateGroupMessage msg = createUpdateMsg();
		VMTGroupMessageHandler handler = createHandler();
		RTPChatGroup group = createRTPGroup();
		EasyMock.expect(groupService.getGroup(groupID)).andReturn(group);
		IRestUserService userService = PowerMock.createMock(IRestUserService.class);
		EasyMock.expect(vmtService.getVmtUserService()).andReturn(userService);
		List<VmtUser> vmtAdminUsers = createVmtAdminUsers();
		EasyMock.expect(userService.getUsersByUmtIds(EasyMock.aryEq(msg.getGroup().getAdmins()))).andReturn(vmtAdminUsers);
		EasyMock.expect(groupService.addMembers(EasyMock.anyObject(String.class), 
				EasyMock.<List<String>>anyObject(), EasyMock.<List<String>>anyObject())).andReturn(new ArrayList<String>());
		groupService.changeGroupTitle(groupID, "name");
		EasyMock.expectLastCall();
		
		PowerMock.replayAll();
		handler.handle(msg);
		PowerMock.verifyAll();
	}
	
	private MQUpdateGroupMessage createUpdateMsg(){
		VmtGroup group = new VmtGroup();
		group.setSymbol(groupID);
		group.setDn("dn");
		group.setName("name");
		group.setAdmins(new String[]{cstnetID});
		return new MQUpdateGroupMessage(group, new ArrayList<VmtUser>());
	}
	
	private RTPChatGroup createRTPGroup(){
		RTPChatGroup group = new RTPChatGroup();
		group.setTitle("title");
		group.setOwner(cstnetID);
		group.setGroupID(groupID);
		group.setAdmins(new HashSet<String>());
		return group;
	}
	
	private List<VmtUser> createVmtAdminUsers(){
		List<VmtUser> users = new ArrayList<VmtUser>();
		VmtUser owner = new VmtUser();
		owner.setCstnetId(cstnetID);
		VmtUser user = new VmtUser();
		user.setCstnetId("temp");
		users.add(owner);
		users.add(user);
		return users;
	}
	
	@Test
	public void testRefreshGroup(){
		MQRefreshGroupMessage msg = createRefreshMsg();
		VMTGroupMessageHandler handler = createHandler();
		PowerMock.mockStatic(GroupServiceUtils.class);
		EasyMock.expect(GroupServiceUtils.rebuildGroup(vmtService, groupService, msg.getGroup())).andReturn(Boolean.TRUE);
		
		PowerMock.replayAll();
		handler.handle(msg);
		PowerMock.verifyAll();
	}
	
	private MQRefreshGroupMessage createRefreshMsg(){
		VmtGroup group = new VmtGroup();
		group.setDn("dn");
		return new MQRefreshGroupMessage(group);
	}
}
