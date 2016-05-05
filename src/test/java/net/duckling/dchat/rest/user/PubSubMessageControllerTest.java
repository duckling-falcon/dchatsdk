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
package net.duckling.dchat.rest.user;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import net.duckling.dchat.utils.RtpServerUtils;
import net.duckling.dchat.utils.UserUtils;

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
import com.rooyeetone.rtp.sdk.RtpSvc;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:springContext4Test.xml")
@PrepareForTest({RtpSvc.class, UserUtils.class, RtpServerUtils.class})
public class PubSubMessageControllerTest {
	
	private static final String STATUS = "status";
	private static final String FROM_USER = "from";
	private static final String TO_USER = "to";
	private static final String SUBJECT = "subject";
	private static final String BODY = "body";
	private static final String GROUP_ID = "groupID";
	private HttpServletRequest request;
	private PubSubMessageController controller;
	
	@Rule
	public PowerMockRule rule = new PowerMockRule();
	
	
	@Before
	public void init(){
		controller = new PubSubMessageController();
		request = new MockHttpServletRequest();
	}
	
	@Test
	public void testPush2User__Success() throws Exception{
		PowerMock.mockStatic(RtpSvc.class);
		IRtpSvc rtp = PowerMock.createMock(IRtpSvc.class);
		EasyMock.expect(RtpSvc.getInstance(request)).andReturn(rtp);
		PowerMock.mockStatic(UserUtils.class);
		EasyMock.expect(UserUtils.escapeCstnetId(FROM_USER)).andReturn(FROM_USER);
		EasyMock.expect(UserUtils.escapeCstnetId(TO_USER)).andReturn(TO_USER);
		EasyMock.expect(rtp.exec("IMSend", FROM_USER+"@cstnet", TO_USER+"@cstnet", SUBJECT, BODY, getHtmlBody2(), "normal")).andReturn(null);
		
		PowerMock.replayAll(rtp);
		JSONObject result = controller.push2User(request, "dhome", FROM_USER, TO_USER, SUBJECT, BODY, getHtmlBody2());
		PowerMock.verifyAll();
		Assert.assertEquals("success", result.get("status"));
	}
	
	@Test
	public void testPush2User__FailAuth(){
		JSONObject result = controller.push2User(request, "dlhome", null, null, null, null, null);
		Assert.assertEquals("fail", result.get(STATUS));
	}
	
	@Test
	public void testPush2User__Exception() throws Exception{
		PowerMock.mockStatic(RtpSvc.class);
		EasyMock.expect(RtpSvc.getInstance(request)).andThrow(new Exception("exception"));
		
		PowerMock.replayAll();
		JSONObject result = controller.push2User(request, "dhome", null, null, null, null, null);
		PowerMock.verifyAll();
		Assert.assertEquals("fail", result.get(STATUS));
		Assert.assertEquals("exception", result.get("message"));
	}
	
	@Test
	public void testPush2Group__Success() throws Exception{
		PowerMock.mockStatic(RtpSvc.class);
		IRtpSvc rtp = PowerMock.createMock(IRtpSvc.class);
		EasyMock.expect(RtpSvc.getInstance(request)).andReturn(rtp);
		PowerMock.mockStatic(UserUtils.class);
		EasyMock.expect(UserUtils.escapeCstnetId(FROM_USER)).andReturn(FROM_USER);
		mockGetGroupMember(rtp);
		
		EasyMock.expect(rtp.exec("IMSend", FROM_USER, "jid", SUBJECT, BODY, getHtmlBody2(), "normal")).andReturn(null);
		
		PowerMock.replayAll(rtp);
		JSONObject result = controller.push2Group(request, "dhome", FROM_USER, GROUP_ID, SUBJECT, BODY, getHtmlBody2());
		PowerMock.verifyAll();
		Assert.assertEquals("success", result.get("status"));
	}
	
	@Test
	public void testPush2Group__FailAuth(){
		JSONObject result = controller.push2Group(request, "dlhome", null, null, null, null, null);
		Assert.assertEquals("fail", result.get(STATUS));
	}
	
	@Test
	public void testPush2Group__Exception() throws Exception{
		PowerMock.mockStatic(RtpSvc.class);
		EasyMock.expect(RtpSvc.getInstance(request)).andThrow(new Exception("exception"));
		
		PowerMock.replayAll();
		JSONObject result = controller.push2Group(request, "dhome", null, null, null, null, null);
		PowerMock.verifyAll();
		Assert.assertEquals("fail", result.get(STATUS));
		Assert.assertEquals("exception", result.get("message"));
	}
	
	@Test
	public void testPush2Orgnization__Fail(){
		JSONObject result = controller.push2Orgnization(request, "dhome", null, null, null, null, null);
		Assert.assertEquals("fail", result.get(STATUS));
	}
	
	@After
	public void destroy(){
		controller = null;
		request = null;
	}
	
	private String getHtmlBody2(){
		return "<body>" + "<img src=\"http://static.oschina.net/uploads/user/582/1165356_50.jpg?t=1370571557000\"/><p>hahahahahaha</p>" +
				"</body>";
	}
	
	private void mockGetGroupMember(IRtpSvc rtp) throws Exception{
		PowerMock.mockStatic(RtpServerUtils.class);
		EasyMock.expect(RtpServerUtils.getGroupJIDFromID(GROUP_ID)).andReturn(GROUP_ID);
		IPagedResult result = PowerMock.createMock(IPagedResult.class);
		EasyMock.expect(rtp.exec("GroupChat.GetMembers", GROUP_ID)).andReturn(result);
		List<IDataItem> items = new ArrayList<IDataItem>();
		IDataItem item = PowerMock.createMock(IDataItem.class);
		items.add(item);
		EasyMock.expect(result.getItems()).andReturn(items);
		EasyMock.expect(item.getProp("jid")).andReturn("jid");
	}
	
}
