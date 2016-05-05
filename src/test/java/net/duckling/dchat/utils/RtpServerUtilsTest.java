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
package net.duckling.dchat.utils;

import java.util.ArrayList;
import java.util.List;

import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.rooyeetone.rtp.sdk.IDataItem;
import com.rooyeetone.rtp.sdk.IPagedResult;
import com.rooyeetone.rtp.sdk.IRtpSvc;
import com.rooyeetone.rtp.sdk.RtpSvc;

@RunWith(PowerMockRunner.class)
@PrepareForTest({RtpSvc.class, UserUtils.class})
public class RtpServerUtilsTest {
	
	private static final String USERNAME = "username";
	private static final String GROUP_ID = "groupid";
	
	@Test
	public void testIsGroupChatExists__True() throws Exception{
		IRtpSvc rtp = PowerMock.createMock(IRtpSvc.class);
		IPagedResult result = PowerMock.createMock(IPagedResult.class);
		EasyMock.expect(rtp.exec("GroupChat.Search", GROUP_ID)).andReturn(result);
		IDataItem item = PowerMock.createMock(IDataItem.class);
		EasyMock.expect(item.getProp("jid")).andReturn(GROUP_ID+"@groupchat.null");
		List<IDataItem> items = new ArrayList<IDataItem>();
		items.add(item);
		EasyMock.expect(result.getItems()).andReturn(items).times(2);
		
		PowerMock.replayAll();
		boolean exist = RtpServerUtils.isGroupChatExist(rtp, GROUP_ID);
		PowerMock.verifyAll();
		Assert.assertTrue(exist);
	}
	
	@Test
	public void testIsGroupChatExists__False() throws Exception{
		IRtpSvc rtp = PowerMock.createMock(IRtpSvc.class);
		EasyMock.expect(rtp.exec("GroupChat.Search", GROUP_ID)).andThrow(new Exception());
		
		PowerMock.replayAll();
		boolean exist = RtpServerUtils.isGroupChatExist(rtp, GROUP_ID);
		PowerMock.verifyAll();
		Assert.assertFalse(exist);
	}
	
	@Test
	public void testIsUserExist__True() throws Exception{
		IRtpSvc rtp = PowerMock.createMock(IRtpSvc.class);
		PowerMock.mockStatic(UserUtils.class);
		EasyMock.expect(UserUtils.escapeCstnetId(USERNAME)).andReturn(USERNAME);
		IPagedResult result = PowerMock.createMock(IPagedResult.class);
		EasyMock.expect(rtp.exec("SearchUser", USERNAME,"","",1,1000)).andReturn(result);
		
		IDataItem item = PowerMock.createMock(IDataItem.class);
		List<IDataItem> items = new ArrayList<IDataItem>();
		items.add(item);
		EasyMock.expect(result.getItems()).andReturn(items).times(2);
		EasyMock.expect(item.get("user")).andReturn(USERNAME);
		
		PowerMock.replayAll();
		boolean exist = RtpServerUtils.isUserExist(rtp, USERNAME);
		PowerMock.verifyAll();
		Assert.assertTrue(exist);
	}
	
	@Test
	public void testIsUserExist__False() throws Exception{
		IRtpSvc rtp = PowerMock.createMock(IRtpSvc.class);
		PowerMock.mockStatic(UserUtils.class);
		EasyMock.expect(UserUtils.escapeCstnetId(USERNAME)).andReturn(USERNAME);
		EasyMock.expect(rtp.exec("SearchUser", USERNAME,"","",1,1000)).andThrow(new Exception());
		
		PowerMock.replayAll();
		boolean exist = RtpServerUtils.isUserExist(rtp, USERNAME);
		PowerMock.verifyAll();
		Assert.assertFalse(exist);
	}
	
	@Test
	public void testIsAdminInGroupChat__True() throws Exception{
		IRtpSvc rtp = PowerMock.createMock(IRtpSvc.class);
		IPagedResult result = PowerMock.createMock(IPagedResult.class);
		EasyMock.expect(rtp.exec("GroupChat.Search", GROUP_ID)).andReturn(result);
		List<IDataItem> items = new ArrayList<IDataItem>();
		IDataItem item = PowerMock.createMock(IDataItem.class);
		items.add(item);
		EasyMock.expect(result.getItems()).andReturn(items).times(2);
		PowerMock.mockStatic(UserUtils.class);
		EasyMock.expect(item.getProp("jid")).andReturn(GROUP_ID+"@groupchat.null");
		EasyMock.expect(item.getProp("owner")).andReturn(USERNAME);
		
		PowerMock.replayAll();
		boolean exist = RtpServerUtils.isOwnerInGroupChat(rtp, GROUP_ID, USERNAME);
		PowerMock.verifyAll();
		Assert.assertTrue(exist);
	}
	
	@Test
	public void testIsAdminInGroupChat__False() throws Exception{
		IRtpSvc rtp = PowerMock.createMock(IRtpSvc.class);
		EasyMock.expect(rtp.exec("GroupChat.Search", GROUP_ID)).andThrow(new Exception());
		
		PowerMock.replayAll();
		boolean exist = RtpServerUtils.isOwnerInGroupChat(rtp, GROUP_ID, USERNAME);
		PowerMock.verifyAll();
		Assert.assertFalse(exist);
	}
}
