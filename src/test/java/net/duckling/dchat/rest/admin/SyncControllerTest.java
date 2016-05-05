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
package net.duckling.dchat.rest.admin;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import net.duckling.dchat.utils.UserUtils;
import net.duckling.dchat.vmt.LdapReader;
import net.duckling.dchat.vmt.RTPChatGroup;
import net.duckling.dchat.vmt.RTPVmtUser;

import org.easymock.EasyMock;
import org.json.simple.JSONObject;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.rule.PowerMockRule;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.rooyeetone.rtp.sdk.IDataItem;
import com.rooyeetone.rtp.sdk.IPagedResult;
import com.rooyeetone.rtp.sdk.IRtpSvc;
import com.rooyeetone.rtp.sdk.ISyncItem;
import com.rooyeetone.rtp.sdk.RtpSvc;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:springContext4Test.xml")
@PrepareForTest({RtpSvc.class, LdapReader.class, UserUtils.class})
public class SyncControllerTest {
	private static final String STATUS = "status";
	private static final int GROUP_PAGE_COUNT = 2000;
	private SyncGroupController controller;
	private HttpServletRequest request;
	
	@Rule
	public PowerMockRule rule = new PowerMockRule();
	
	@Before
	public void init(){
		controller = new SyncGroupController();
		request = new MockHttpServletRequest();
	}
	
	@Test
	public void testSyncAllGroup__Success() throws Exception{
		PowerMock.mockStatic(RtpSvc.class);
		IRtpSvc rtp = PowerMock.createMock(IRtpSvc.class);
		EasyMock.expect(RtpSvc.getInstance(request)).andReturn(rtp);
		removeAllGroupSuccess(rtp);
		LdapReader ldap = getGroupFromLdap(rtp, true);
		getGroupMember(rtp, ldap);
		
		PowerMock.replayAll();
		JSONObject result = controller.syncAllGroup(request);
		PowerMock.verifyAll();
		Assert.assertEquals("success", result.get(STATUS));
	}
	
	@Test
	public void testSyncAllGroup__Exception() throws Exception{
		PowerMock.mockStatic(RtpSvc.class);
		IRtpSvc rtp = PowerMock.createMock(IRtpSvc.class);
		EasyMock.expect(RtpSvc.getInstance(request)).andReturn(rtp);
		removeAllGroupSuccess(rtp);
		createIFNotExist(rtp);
		getGroupFromLdap(rtp, false);
		
		PowerMock.replayAll();
		JSONObject result = controller.syncAllGroup(request);
		PowerMock.verifyAll();
		Assert.assertEquals("success", result.get(STATUS));
	}
	
	private void removeAllGroupSuccess(IRtpSvc rtp) throws Exception{
		IPagedResult pResult = PowerMock.createMock(IPagedResult.class);
		EasyMock.expect(rtp.exec("GroupChat.Search", "","","","",1,GROUP_PAGE_COUNT)).andReturn(pResult);
		List<IDataItem> items = new ArrayList<IDataItem>();
		IDataItem item = PowerMock.createMock(IDataItem.class);
		items.add(item);
		EasyMock.expect(pResult.getItems()).andReturn(items);
		EasyMock.expect(item.getProp("jid")).andReturn("jid");
		EasyMock.expect(rtp.exec("GroupChat.Destroy", "jid")).andReturn("");
	}
	
	private LdapReader getGroupFromLdap(IRtpSvc rtp, boolean success) throws Exception{
		PowerMock.mockStatic(LdapReader.class);
		LdapReader ldap = PowerMock.createMock(LdapReader.class);
		EasyMock.expect(LdapReader.getInstance()).andReturn(ldap);
		List<RTPChatGroup> groups = new ArrayList<RTPChatGroup>();
		RTPChatGroup group = PowerMock.createMock(RTPChatGroup.class);
		groups.add(group);
		EasyMock.expect(ldap.getAllGroup()).andReturn(groups);
		if(success){
			EasyMock.expect(rtp.exec("GroupChat.Create",null,null,null,0,null,null,null)).andReturn("jid");
			EasyMock.expect(group.getGroupID()).andReturn(null).times(2);
			EasyMock.expect(group.getAdmins()).andReturn(new HashSet<String>());
			EasyMock.expect(group.getOwner()).andReturn(null).times(2);
			EasyMock.expect(group.getGroupDN()).andReturn(null);
		}else{
			EasyMock.expect(rtp.exec("GroupChat.Create",null,null,null,0,null,null,null)).andThrow(new Exception());
			EasyMock.expect(group.getGroupID()).andReturn(null).times(3);
			EasyMock.expect(group.getOwner()).andReturn(null).times(3);
		}
		EasyMock.expect(group.getCategory()).andReturn(null);
		EasyMock.expect(group.getMaxuser()).andReturn(0);
		EasyMock.expect(group.getDesc()).andReturn(null);
		EasyMock.expect(group.getAuth()).andReturn(null);
		EasyMock.expect(group.getTitle()).andReturn(null);
		return ldap;
	}
	
	private void getGroupMember(IRtpSvc rtp, LdapReader ldap) throws Exception{
		createIFNotExist(rtp);
		
		List<RTPVmtUser> members = new ArrayList<RTPVmtUser>();
		RTPVmtUser member = PowerMock.createMock(RTPVmtUser.class);
		members.add(member);
		EasyMock.expect(ldap.getGroupMembers(null)).andReturn(members);
		PowerMock.mockStatic(UserUtils.class);
		EasyMock.expect(member.getCstnetId()).andReturn(null);
		EasyMock.expect(UserUtils.escapeCstnetId(null)).andReturn("cstnetId");
		//EasyMock.expect(rtp.exec("GroupChat.AddMember", "jid", "cstnetId", "member")).andReturn("");
	}
	
	private void createIFNotExist(IRtpSvc rtp) throws Exception{
		ISyncItem item = PowerMock.createMock(ISyncItem.class);
		EasyMock.expect(rtp.exec("SyncData", null, "USER", "INSERT")).andReturn(item);
		item.setProp("nickname", null);
		item.setProp("fullname", null);
		item.setProp("email", null);
		item.setProp(RTPVmtUser.RTP_DEPT, "");
		item.commit();
		EasyMock.expectLastCall();
	}
	
	@Test
	public void testSyncAllGroup__RtpServerNull() throws Exception{
		PowerMock.mockStatic(RtpSvc.class);
		EasyMock.expect(RtpSvc.getInstance(request)).andThrow(new Exception());
		
		PowerMock.replayAll();
		JSONObject result = controller.syncAllGroup(request);
		PowerMock.verifyAll();
		Assert.assertEquals("fail", result.get(STATUS));
		Assert.assertEquals("RTP Server Error!", result.get("message"));
	}
	
	@Test
	public void testSyncAllGroup__RemoveGroupFail() throws Exception{
		PowerMock.mockStatic(RtpSvc.class);
		IRtpSvc rtp = PowerMock.createMock(IRtpSvc.class);
		EasyMock.expect(RtpSvc.getInstance(request)).andReturn(rtp);
		EasyMock.expect(rtp.exec("GroupChat.Search", "","","","",1,GROUP_PAGE_COUNT)).andThrow(new Exception());
		
		PowerMock.replayAll();
		JSONObject result = controller.syncAllGroup(request);
		PowerMock.verifyAll();
		Assert.assertEquals("fail", result.get(STATUS));
	}
	
	@Test
	public void testSyncGroup__Success(){
		
	}
	
	
	@After
	public void destroy(){
		controller = null;
		request = null;
	}
}
