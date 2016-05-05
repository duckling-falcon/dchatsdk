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
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.duckling.dchat.exception.RTPGroupNotFoundException;
import net.duckling.dchat.exception.RTPServerException;
import net.duckling.dchat.rtp.domain.GroupJID;
import net.duckling.dchat.rtp.domain.UserJID;
import net.duckling.dchat.rtp.service.interf.IRTPUserService;
import net.duckling.dchat.utils.RtpServerUtils;
import net.duckling.dchat.vmt.RTPChatGroup;

import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.api.support.membermodification.MemberModifier;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.rooyeetone.rtp.sdk.IDataItem;
import com.rooyeetone.rtp.sdk.IPagedResult;
import com.rooyeetone.rtp.sdk.IRtpSvc;

@RunWith(PowerMockRunner.class)
@PrepareForTest({RtpServerUtils.class})
public class RTPGroupServiceTest {
	private static final String GROUP_ID = "groupid";
	private static final String GROUP_JID = "groupid@groupchat.null";
	private static final int PAGE_NUM = 1000;
	private static final String JID = "jid";
	private RTPGroupService service = new RTPGroupService();
	
	private IRTPUserService userService;
	
	private IRtpSvc modifyRTP() throws IllegalArgumentException, IllegalAccessException{
		IRtpSvc rtp = PowerMock.createMock(IRtpSvc.class);
		MemberModifier.field(RTPGroupService.class, "rtp").set(service, rtp);
		userService = PowerMock.createMock(IRTPUserService.class);
		MemberModifier.field(RTPGroupService.class, "userService").set(service, userService);
		return rtp;
	}
	
	@Test
	public void testAddGroup() throws Exception{
		IRtpSvc rtp = modifyRTP();
		PowerMock.mockStatic(RtpServerUtils.class);
		mockIsGroupExist(rtp,false);
		RTPChatGroup group = createChatGroup();
		EasyMock.expect(userService.isUserExist(JID)).andReturn(Boolean.FALSE);
		EasyMock.expect(userService.addDefaultUser(JID)).andReturn(new UserJID("x","x"));
		EasyMock.expect(rtp.exec("GroupChat.Create", GROUP_ID,JID,null,0,null,null,null)).andReturn(GROUP_JID);
		EasyMock.expect(rtp.exec("GroupChat.AddMember", GROUP_JID,JID,RTPChatGroup.RTP_AFFILIATION_ADMIN)).andReturn(new Object());
		
		PowerMock.replayAll();
		service.addGroup(group);
		PowerMock.verifyAll();
	}
	
	private RTPChatGroup createChatGroup(){
		RTPChatGroup group = new RTPChatGroup();
		group.setGroupID(GROUP_ID);
		group.setOwner(JID);
		Set<String> admins = new HashSet<String>();
		admins.add(JID);
		group.setAdmins(admins);
		return group;
	}
	
	private void mockIsGroupExist(IRtpSvc rtp, boolean isExist) throws Exception{
		IPagedResult result = PowerMock.createMock(IPagedResult.class);
		EasyMock.expect(rtp.exec("GroupChat.Search", GROUP_ID,"","","",1,PAGE_NUM)).andReturn(result);
		List<IDataItem> items = isExist?prepareMockItems(rtp):new ArrayList<IDataItem>();
		EasyMock.expect(result.getItems()).andReturn(items).anyTimes();
	}
	
	private List<IDataItem> prepareMockItems(IRtpSvc rtp) throws Exception{
		List<IDataItem> items = new ArrayList<IDataItem>();
		IDataItem item = PowerMock.createMock(IDataItem.class);
		EasyMock.expect(item.get(JID)).andReturn(GROUP_JID);
		EasyMock.expect(item.getProp(RTPChatGroup.RTP_JID)).andReturn(JID);
		EasyMock.expect(RtpServerUtils.getGroupIDFromJID(JID)).andReturn(GROUP_ID);
		EasyMock.expect(item.getProp(RTPChatGroup.RTP_TITLE)).andReturn("title");
		EasyMock.expect(item.getProp(RTPChatGroup.RTP_OWNER)).andReturn(JID);
		EasyMock.expect(RtpServerUtils.getUsernameFromGroupMember(JID)).andReturn(JID);
		EasyMock.expect(item.getProp(RTPChatGroup.RTP_CATEGORY)).andReturn("category");
		EasyMock.expect(item.getProp(RTPChatGroup.RTP_AUTH)).andReturn("auth");
		EasyMock.expect(item.get(RTPChatGroup.RTP_MAXUSER)).andReturn(1);
		EasyMock.expect(item.getProp(RTPChatGroup.RTP_DESC)).andReturn("desc");
		prepareMockAdmins(rtp);
		items.add(item);
		return items;
	}
	
	private void prepareMockAdmins(IRtpSvc rtp) throws Exception{
		List<IDataItem> admins = new ArrayList<IDataItem>();
		IDataItem admin = PowerMock.createMock(IDataItem.class);
		admins.add(admin);
		EasyMock.expect(rtp.exec("GroupChat.GetAllMembers", GROUP_JID)).andReturn(admins);
		EasyMock.expect(admin.getProp(JID)).andReturn(JID);
		EasyMock.expect(admin.getProp(RTPChatGroup.RTP_AFFILIATION)).andReturn("admin");
		EasyMock.expect(RtpServerUtils.getUsernameFromGroupMember(JID)).andReturn(JID);
	}
	
	@Test(expected = RTPServerException.class)
	public void testRemoveGroup() throws Exception{
		IRtpSvc rtp = modifyRTP();
		PowerMock.mockStatic(RtpServerUtils.class);
		mockIsGroupExist(rtp,true);
		GroupJID jid = new GroupJID(GROUP_ID);
		EasyMock.expect(rtp.exec("GroupChat.Destroy", jid.toString())).andThrow(new Exception());
		
		PowerMock.replayAll();
		service.removeGroup(jid);
		PowerMock.verifyAll();
	}
	
	@Test(expected = RTPServerException.class)
	public void testChangeGroupTitle__ServerException() throws Exception{
		IRtpSvc rtp = modifyRTP();
		PowerMock.mockStatic(RtpServerUtils.class);
		mockIsGroupExist(rtp,true);
		GroupJID jid = new GroupJID(GROUP_ID);
		String title = "title";
		EasyMock.expect(rtp.exec("GroupChat.SetProperty", jid.toString(),RTPChatGroup.RTP_TITLE,title)).andThrow(new Exception());
		
		PowerMock.replayAll();
		service.changeGroupTitle(jid.getNode(), title);
		PowerMock.verifyAll();
	}
	
	@Test(expected = RTPGroupNotFoundException.class)
	public void testChangeGroupTitle__GroupNotFoundException() throws Exception{
		IRtpSvc rtp = modifyRTP();
		PowerMock.mockStatic(RtpServerUtils.class);
		mockIsGroupExist(rtp,false);
		
		PowerMock.replayAll();
		service.changeGroupTitle(GROUP_ID, "title");
		PowerMock.verifyAll();
	}
	
	@Test(expected = RTPGroupNotFoundException.class)
	public void testAddMembers__GroupNotFound() throws Exception{
		IRtpSvc rtp = modifyRTP();
		PowerMock.mockStatic(RtpServerUtils.class);
		mockIsGroupExist(rtp,false);
		
		PowerMock.replayAll();
		service.addMembers(GROUP_ID, new ArrayList<String>(), new ArrayList<String>());
		PowerMock.verifyAll();
	}
	
	@Test
	public void testAddMembers__Success() throws Exception{
		IRtpSvc rtp = modifyRTP();
		PowerMock.mockStatic(RtpServerUtils.class);
		mockIsGroupExist(rtp,true);
		String secondUser = "xxx";
		List<String> usernames =  new ArrayList<String>(Arrays.asList(secondUser, JID));
		EasyMock.expect(userService.isUserExist(secondUser)).andReturn(false);
		EasyMock.expect(userService.addDefaultUser(secondUser)).andReturn(new UserJID(secondUser,"x"));
		EasyMock.expect(rtp.exec("GroupChat.AddMember", GROUP_JID,secondUser,"member")).andReturn(new Object());
		EasyMock.expect(userService.isUserExist(JID)).andReturn(Boolean.TRUE);
		
		PowerMock.replayAll();
		service.addMembers(GROUP_ID, usernames, null);
		PowerMock.verifyAll();
	}
	
	@Test
	public void testRemoveMembers__Success() throws Exception{
		IRtpSvc rtp = modifyRTP();
		PowerMock.mockStatic(RtpServerUtils.class);
		mockIsGroupExist(rtp,true);
		String secondUser = "xxx";
		List<String> usernames =  new ArrayList<String>(Arrays.asList(secondUser, JID));
		EasyMock.expect(rtp.exec("GroupChat.RemoveMember", GROUP_JID,secondUser)).andThrow(new Exception());
		
		PowerMock.replayAll();
		List<String> failMembers = service.removeMembers(GROUP_ID, usernames);
		PowerMock.verifyAll();
		
		Assert.assertTrue(failMembers.size()>0);
		Assert.assertEquals(secondUser, failMembers.get(0));
	}
	
	@Test
	public void testGetMembers() throws Exception{
		IRtpSvc rtp = modifyRTP();
		PowerMock.mockStatic(RtpServerUtils.class);
		mockIsGroupExist(rtp,true);
		IPagedResult result = PowerMock.createMock(IPagedResult.class);
		EasyMock.expect(rtp.exec(EasyMock.anyObject(String.class), EasyMock.anyObject(String.class),
				EasyMock.anyInt(), EasyMock.anyInt())).andReturn(result);
		List<IDataItem> items = new ArrayList<IDataItem>();
		IDataItem item = PowerMock.createMock(IDataItem.class);
		items.add(item);
		EasyMock.expect(result.getItems()).andReturn(items);
		EasyMock.expect(item.getProp(JID)).andReturn(JID);
		EasyMock.expect(item.getProp(RTPChatGroup.RTP_AFFILIATION)).andReturn("aff");
		EasyMock.expect(RtpServerUtils.getUsernameFromGroupMember(JID)).andReturn(JID);
		
		PowerMock.replayAll();
		List<String> members = service.getMembers(GROUP_ID, "aff", 1, 1);
		PowerMock.verifyAll();
		Assert.assertNotEquals(0, members.size());
		Assert.assertEquals(JID, members.get(0));
	}
	
	@Test
	public void testGetAllMembers() throws Exception{
		IRtpSvc rtp = modifyRTP();
		PowerMock.mockStatic(RtpServerUtils.class);
		mockIsGroupExist(rtp,true);
		List<IDataItem> items = new ArrayList<IDataItem>();
		IDataItem item = PowerMock.createMock(IDataItem.class);
		items.add(item);
		EasyMock.expect(rtp.exec(EasyMock.anyObject(String.class), EasyMock.anyObject(String.class))).andReturn(items);
		EasyMock.expect(item.getProp(JID)).andReturn(JID);
		EasyMock.expect(item.getProp(RTPChatGroup.RTP_AFFILIATION)).andReturn("aff");
		EasyMock.expect(RtpServerUtils.getUsernameFromGroupMember(JID)).andReturn(JID);
		
		PowerMock.replayAll();
		List<String> members = service.getAllMembers(GROUP_ID, "aff");
		PowerMock.verifyAll();
		Assert.assertNotEquals(0, members.size());
		Assert.assertEquals(JID, members.get(0));
	}
	
	@Test(expected = RTPServerException.class)
	public void testTransferGroup() throws Exception{
		IRtpSvc rtp = modifyRTP();
		PowerMock.mockStatic(RtpServerUtils.class);
		mockIsGroupExist(rtp,true);
		rtp.exec("GroupChat.Transfer", GROUP_JID,JID,Boolean.FALSE,JID);
		EasyMock.expectLastCall().andThrow(new Exception());
		
		PowerMock.replayAll();
		service.transferGroup(GROUP_ID, JID, false);
		PowerMock.verifyAll();
	}
	
	@Test
	public void testIsGroupOwner() throws Exception{
		IRtpSvc rtp = modifyRTP();
		PowerMock.mockStatic(RtpServerUtils.class);
		mockIsGroupExist(rtp,true);
		
		PowerMock.replayAll();
		service.isGroupOwner(new GroupJID(GROUP_ID), JID);
		PowerMock.verifyAll();
	}
	
	@Test
	public void testGetUserGroups() throws Exception{
		IRtpSvc rtp = modifyRTP();
		List<IDataItem> items = prepareMockItems2(rtp);
		EasyMock.expect(rtp.exec("GroupChat.GetUserGroups", new UserJID(JID,null).toString(), 
				RTPChatGroup.RTP_AFFILIATION_ADMIN)).andReturn(items);
		
		PowerMock.replayAll();
		List<RTPChatGroup> groups = service.getUserGroups(JID, RTPChatGroup.RTP_AFFILIATION_ADMIN);
		PowerMock.verifyAll();
		Assert.assertNotNull(groups.get(0));
	}
	
	private List<IDataItem> prepareMockItems2(IRtpSvc rtp) throws Exception{
		List<IDataItem> items = new ArrayList<IDataItem>();
		IDataItem item = PowerMock.createMock(IDataItem.class);
		PowerMock.mockStatic(RtpServerUtils.class);
		EasyMock.expect(item.getProp(RTPChatGroup.RTP_JID)).andReturn(JID);
		EasyMock.expect(RtpServerUtils.getGroupIDFromJID(JID)).andReturn(GROUP_ID);
		EasyMock.expect(item.getProp(RTPChatGroup.RTP_TITLE)).andReturn("title");
		EasyMock.expect(item.getProp(RTPChatGroup.RTP_OWNER)).andReturn(JID);
		EasyMock.expect(RtpServerUtils.getUsernameFromGroupMember(JID)).andReturn(JID);
		EasyMock.expect(item.getProp(RTPChatGroup.RTP_CATEGORY)).andReturn("category");
		EasyMock.expect(item.getProp(RTPChatGroup.RTP_AUTH)).andReturn("auth");
		EasyMock.expect(item.get(RTPChatGroup.RTP_MAXUSER)).andReturn(1);
		EasyMock.expect(item.getProp(RTPChatGroup.RTP_DESC)).andReturn("desc");
		prepareMockAdmins(rtp);
		items.add(item);
		return items;
	}
	
	@Test
	public void testRebuildGroup() throws Exception{
		IRtpSvc rtp = modifyRTP();
		PowerMock.mockStatic(RtpServerUtils.class);
		mockIsGroupExist(rtp,true);
		EasyMock.expect(rtp.exec("GroupChat.Destroy", new GroupJID(GROUP_ID).toString())).andReturn(new Object());
		
		mockIsGroupExist(rtp,false);
		RTPChatGroup group = createChatGroup();
		EasyMock.expect(userService.isUserExist(JID)).andReturn(Boolean.FALSE);
		EasyMock.expect(userService.addDefaultUser(JID)).andReturn(new UserJID("x","x"));
		EasyMock.expect(rtp.exec("GroupChat.Create", GROUP_ID,JID,null,0,null,null,null)).andReturn(GROUP_JID);
		EasyMock.expect(rtp.exec("GroupChat.AddMember", GROUP_JID,JID,RTPChatGroup.RTP_AFFILIATION_ADMIN)).andReturn(new Object());
		
		mockIsGroupExist(rtp,true);
		String secondUser = "xxx";
		List<String> usernames =  new ArrayList<String>(Arrays.asList(secondUser, JID));
		EasyMock.expect(userService.isUserExist(secondUser)).andReturn(false);
		EasyMock.expect(userService.addDefaultUser(secondUser)).andReturn(new UserJID(secondUser,"x"));
		EasyMock.expect(rtp.exec("GroupChat.AddMember", GROUP_JID,secondUser,"member")).andReturn(new Object());
		EasyMock.expect(userService.isUserExist(JID)).andReturn(Boolean.TRUE);
		
		PowerMock.replayAll();
		boolean result = service.rebuildGroup(group, usernames);
		PowerMock.verifyAll();
		Assert.assertTrue(result);
	}
}
