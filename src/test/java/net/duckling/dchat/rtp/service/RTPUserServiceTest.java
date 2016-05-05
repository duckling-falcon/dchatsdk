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
import java.util.List;

import net.duckling.dchat.exception.RTPServerException;
import net.duckling.dchat.exception.RTPUserNotFoundException;
import net.duckling.dchat.rtp.domain.GroupJID;
import net.duckling.dchat.rtp.domain.UserJID;
import net.duckling.dchat.rtp.service.interf.IRTPGroupService;
import net.duckling.dchat.utils.RtpServerUtils;
import net.duckling.dchat.utils.UserUtils;
import net.duckling.dchat.vmt.RTPChatGroup;
import net.duckling.dchat.vmt.RTPVmtUser;

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
import com.rooyeetone.rtp.sdk.ISyncItem;

@RunWith(PowerMockRunner.class)
@PrepareForTest({RtpServerUtils.class, UserUtils.class})
public class RTPUserServiceTest {
	private static final String CSTNET_ID = "cstnetid";
	private RTPUserService service = new RTPUserService();
	private IRTPGroupService groupService;
	
	private IRtpSvc modifyRTP() throws Exception{
		IRtpSvc rtp = PowerMock.createMock(IRtpSvc.class);
		MemberModifier.field(RTPUserService.class, "rtp").set(service, rtp);
		groupService = PowerMock.createMock(IRTPGroupService.class);
		MemberModifier.field(RTPUserService.class, "groupService").set(service, groupService);
		return rtp;
	}
	
	private RTPVmtUser createUser(){
		RTPVmtUser user = new RTPVmtUser();
		user.setCstnetId(CSTNET_ID);
		user.setCurrentDisplay(CSTNET_ID);
		user.setName(CSTNET_ID);
		user.setSortWeights(1);
		return user;
	}
	
	@Test
	public void testAddUser() throws Exception{
		IRtpSvc rtp = modifyRTP();
		RTPVmtUser user = createUser();
		PowerMock.mockStatic(UserUtils.class);
		ISyncItem item = PowerMock.createMock(ISyncItem.class);
		EasyMock.expect(rtp.exec("SyncData", CSTNET_ID, "USER", "INSERT")).andReturn(item);
		EasyMock.expect(UserUtils.formatDept(user.getCurrentDisplay())).andReturn("dept");
		item.setProp(RTPVmtUser.RTP_NICKNAME, user.getName());
		item.setProp(RTPVmtUser.RTP_FULLNAME, user.getName());
		item.setProp(RTPVmtUser.RTP_EMAIL, CSTNET_ID);
		item.setProp(RTPVmtUser.RTP_DEPT, "dept");
		item.setProp(RTPVmtUser.RTP_SORTWEIGHTS, "1");
		item.setProp(RTPVmtUser.RTP_TITLE, null);
		item.setProp(RTPVmtUser.RTP_MOBILE, null);
		item.setProp(RTPVmtUser.RTP_WORK_PHONE, null);
		item.setProp(RTPVmtUser.RTP_SEX, null);
		item.commit();
		EasyMock.expectLastCall();
		
		PowerMock.replayAll();
		UserJID jid = service.addUser(user);
		PowerMock.verifyAll();
		Assert.assertEquals(new UserJID(CSTNET_ID,null).toString(), jid.toString());
	}
	
	@Test(expected = RTPServerException.class)
	public void testAddUser__Exception() throws Exception{
		IRtpSvc rtp = modifyRTP();
		RTPVmtUser user = createUser();
		EasyMock.expect(rtp.exec("SyncData", CSTNET_ID, "USER", "INSERT")).andThrow(new Exception());
		
		PowerMock.replayAll();
		service.addUser(user);
		PowerMock.verifyAll();
	}
	
	@Test
	public void testGetUser() throws Exception{
		IRtpSvc rtp = modifyRTP();
		IPagedResult result = PowerMock.createMock(IPagedResult.class);
		EasyMock.expect(rtp.exec("SearchUser", CSTNET_ID,"","",1,1000)).andReturn(result);
		List<IDataItem> items = new ArrayList<IDataItem>();
		IDataItem item = PowerMock.createMock(IDataItem.class);
		items.add(item);
		EasyMock.expect(result.getItems()).andReturn(items).times(2);
		EasyMock.expect(item.get("user")).andReturn(CSTNET_ID);
		EasyMock.expect(item.getProp("user")).andReturn(CSTNET_ID);
		EasyMock.expect(item.getProp(RTPVmtUser.RTP_FULLNAME)).andReturn("fullname");
		EasyMock.expect(item.getProp(RTPVmtUser.RTP_NICKNAME)).andReturn("nickname");
		
		PowerMock.replayAll();
		RTPVmtUser user = service.getUser(CSTNET_ID);
		PowerMock.verifyAll();
		Assert.assertNotNull(user);
		Assert.assertEquals(CSTNET_ID, user.getCstnetId());
	}
	
	@Test(expected = RTPServerException.class)
	public void testGetUser__Exception() throws Exception{
		IRtpSvc rtp = modifyRTP();
		EasyMock.expect(rtp.exec("SearchUser", CSTNET_ID,"","",1,1000)).andThrow(new Exception());
		
		PowerMock.replayAll();
		service.getUser(CSTNET_ID);
		PowerMock.verifyAll();
	}
	
	@Test
	public void testAddDefaultUser() throws Exception{
		IRtpSvc rtp = modifyRTP();
		ISyncItem item = PowerMock.createMock(ISyncItem.class);
		EasyMock.expect(rtp.exec("SyncData",CSTNET_ID,"USER","INSERT")).andReturn(item);
		item.setProp(RTPVmtUser.RTP_NICKNAME, CSTNET_ID);
		item.setProp(RTPVmtUser.RTP_FULLNAME, CSTNET_ID);
		item.setProp(RTPVmtUser.RTP_EMAIL, CSTNET_ID);
		item.commit();
		EasyMock.expectLastCall();
		
		PowerMock.replayAll();
		UserJID jid = service.addDefaultUser(CSTNET_ID);
		PowerMock.verifyAll();
		Assert.assertEquals(new UserJID(CSTNET_ID,null).toString(), jid.toString());
	}
	
	@Test(expected = RTPServerException.class)
	public void testAddDefaultUser__Exception() throws Exception{
		IRtpSvc rtp = modifyRTP();
		EasyMock.expect(rtp.exec("SyncData",CSTNET_ID,"USER","INSERT")).andThrow(new Exception());
		
		PowerMock.replayAll();
		service.addDefaultUser(CSTNET_ID);
		PowerMock.verifyAll();
	}
	
	@Test
	public void testRemoveUser__TransferGroup() throws Exception{
		IRtpSvc rtp = modifyRTP();
		testGetUser(rtp, true);
		testTransferGroups(2);
		ISyncItem itm = PowerMock.createMock(ISyncItem.class);
		EasyMock.expect(rtp.exec("SyncData",CSTNET_ID,"USER","DELETE")).andReturn(itm);
		itm.commit();
		EasyMock.expectLastCall();
		
		PowerMock.replayAll();
		service.removeUser(new UserJID(CSTNET_ID, null));
		PowerMock.verifyAll();
	}
	
	@Test
	public void testRemoveUser__RemoveGroup() throws Exception{
		IRtpSvc rtp = modifyRTP();
		testGetUser(rtp, true);
		testTransferGroups(1);
		ISyncItem itm = PowerMock.createMock(ISyncItem.class);
		EasyMock.expect(rtp.exec("SyncData",CSTNET_ID,"USER","DELETE")).andReturn(itm);
		itm.commit();
		EasyMock.expectLastCall();
		
		PowerMock.replayAll();
		service.removeUser(new UserJID(CSTNET_ID, null));
		PowerMock.verifyAll();
	}
	
	private void testGetUser(IRtpSvc rtp, boolean exist) throws Exception{
		if(exist){
			IPagedResult result = PowerMock.createMock(IPagedResult.class);
			EasyMock.expect(rtp.exec("SearchUser", CSTNET_ID,"","",1,1000)).andReturn(result);
			List<IDataItem> items = new ArrayList<IDataItem>();
			IDataItem item = PowerMock.createMock(IDataItem.class);
			items.add(item);
			EasyMock.expect(result.getItems()).andReturn(items).times(2);
			EasyMock.expect(item.get("user")).andReturn(CSTNET_ID);
			EasyMock.expect(item.getProp("user")).andReturn(CSTNET_ID);
			EasyMock.expect(item.getProp(RTPVmtUser.RTP_FULLNAME)).andReturn("fullname");
			EasyMock.expect(item.getProp(RTPVmtUser.RTP_NICKNAME)).andReturn("nickname");
		}else{
			EasyMock.expect(rtp.exec("SearchUser", CSTNET_ID,"","",1,1000)).andReturn(null);
		}
	}
	
	private void testTransferGroups(int memberSize) throws Exception{
		List<RTPChatGroup> groups = createGroups();
		EasyMock.expect(groupService.getUserGroups(CSTNET_ID, RTPChatGroup.RTP_AFFILIATION_OWNER)).andReturn(groups);
		List<String> members = createMembers(memberSize);
		EasyMock.expect(groupService.getMembers("groupid", null, 1, 2)).andReturn(members);
		if(memberSize >=2){
			groupService.transferGroup("groupid", "member1", true);
		}else{
			groupService.removeGroup(EasyMock.anyObject(GroupJID.class));
		}
		EasyMock.expectLastCall();
	}
	
	private List<RTPChatGroup> createGroups(){
		List<RTPChatGroup> groups = new ArrayList<RTPChatGroup>();
		RTPChatGroup group = new RTPChatGroup();
		group.setGroupID("groupid");
		groups.add(group);
		return groups;
	}
	
	private List<String> createMembers(int size){
		List<String> members = new ArrayList<String>();
		for(int i=0; i<size; i++){
			members.add("member"+i);
		}
		return members;
	}
	
	@Test
	public void testUpdateUserDept__Success() throws Exception{
		IRtpSvc rtp = modifyRTP();
		testGetUser(rtp, true);
		ISyncItem item = PowerMock.createMock(ISyncItem.class);
		EasyMock.expect(rtp.exec("SyncData",CSTNET_ID,"USER","UPDATE")).andReturn(item);
		PowerMock.mockStatic(UserUtils.class);
		EasyMock.expect(UserUtils.formatDept("dept")).andReturn("dept");
		item.setProp(RTPVmtUser.RTP_DEPT, "dept");
		item.commit();
		EasyMock.expectLastCall();
		
		PowerMock.replayAll();
		service.updateUserDept(CSTNET_ID,"", "dept");
		PowerMock.verifyAll();
	}
	
	@Test(expected = RTPServerException.class)
	public void testUpdateUserDept__ServerExcpetion() throws Exception{
		IRtpSvc rtp = modifyRTP();
		testGetUser(rtp, true);
		EasyMock.expect(rtp.exec("SyncData",CSTNET_ID,"USER","UPDATE")).andThrow(new Exception());
		
		PowerMock.replayAll();
		service.updateUserDept(CSTNET_ID,"", "dept");
		PowerMock.verifyAll();
	}
	
	@Test(expected = RTPUserNotFoundException.class)
	public void testUpdateUserDept__UserNotFoundExcpetion() throws Exception{
		IRtpSvc rtp = modifyRTP();
		testGetUser(rtp, false);
		
		PowerMock.replayAll();
		service.updateUserDept(CSTNET_ID,"", "dept");
		PowerMock.verifyAll();
	}
	
	@Test
	public void testUpdateUser__Success() throws Exception{
		IRtpSvc rtp = modifyRTP();
		RTPVmtUser user = createUser();
		testGetUser(rtp, true);
		ISyncItem item = PowerMock.createMock(ISyncItem.class);
		EasyMock.expect(rtp.exec("SyncData",CSTNET_ID,"USER","UPDATE")).andReturn(item);
		PowerMock.mockStatic(UserUtils.class);
		EasyMock.expect(UserUtils.formatDept(user.getCurrentDisplay())).andReturn(user.getCurrentDisplay());
		item.setProp(RTPVmtUser.RTP_DEPT, user.getCurrentDisplay());
		item.setProp(RTPVmtUser.RTP_FULLNAME, user.getName());
		item.setProp(RTPVmtUser.RTP_NICKNAME, user.getName());
		item.setProp(RTPVmtUser.RTP_SORTWEIGHTS, "1");
		item.setProp(RTPVmtUser.RTP_TITLE, null);
		item.setProp(RTPVmtUser.RTP_MOBILE, null);
		item.setProp(RTPVmtUser.RTP_WORK_PHONE, null);
		item.setProp(RTPVmtUser.RTP_SEX, null);
		item.commit();
		EasyMock.expectLastCall();
		
		PowerMock.replayAll();
		service.updateUser(CSTNET_ID, user);
		PowerMock.verifyAll();
	}
	
	@Test(expected = RTPServerException.class)
	public void testUpdateUser__ServerExcpetion() throws Exception{
		IRtpSvc rtp = modifyRTP();
		RTPVmtUser user = createUser();
		testGetUser(rtp, true);
		EasyMock.expect(rtp.exec("SyncData",CSTNET_ID,"USER","UPDATE")).andThrow(new Exception());
		
		PowerMock.replayAll();
		service.updateUser(CSTNET_ID, user);
		PowerMock.verifyAll();
	}
	
	@Test(expected = RTPUserNotFoundException.class)
	public void testUpdateUser__UserNotFoundExcpetion() throws Exception{
		IRtpSvc rtp = modifyRTP();
		RTPVmtUser user = createUser();
		testGetUser(rtp, false);
		
		PowerMock.replayAll();
		service.updateUser(CSTNET_ID, user);
		PowerMock.verifyAll();
	}
}
