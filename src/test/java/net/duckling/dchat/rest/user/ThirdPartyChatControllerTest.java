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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


import net.duckling.dchat.utils.UserUtils;

import static org.easymock.EasyMock.*;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import com.rooyeetone.rtp.sdk.IRtpSvc;
import com.rooyeetone.rtp.sdk.RtpSvc;

@RunWith(PowerMockRunner.class)
@PrepareForTest({RtpSvc.class, UserUtils.class})
public class ThirdPartyChatControllerTest {
	
	private static final String TO_USER = "to";
	private static final String FROM_USER = "from";
	
	private ThirdPartyChatController controller;
	private MockHttpServletRequest request;
	private MockHttpServletResponse response;
	
	@Before
	public void init(){
		controller = new ThirdPartyChatController();
		request = new MockHttpServletRequest();
		response = new MockHttpServletResponse();
		request.setContextPath("/");
		request.setScheme("http");
		request.setServerName("localhost");
		request.setServerPort(80);
	}
	
	@Test
	public void testGetRefJs__Success() throws Exception{
		String jsContent = "jsContent";
		PowerMock.mockStatic(RtpSvc.class);
		IRtpSvc rtp = PowerMock.createMock(IRtpSvc.class);
		expect(RtpSvc.getInstance(request)).andReturn(rtp);
		expect(rtp.exec("RefJS", "")).andReturn(jsContent);
		
		PowerMock.replayAll();
		controller.getRefJs(request, response);
		Assert.assertNotNull(response.getContentAsString());
		Assert.assertEquals(jsContent, response.getContentAsString());
	}
	
	@Test
	public void testGetRefJs__Exception() throws Exception{
		PowerMock.mockStatic(RtpSvc.class);
		expect(RtpSvc.getInstance(request)).andThrow(new Exception());
		
		PowerMock.replayAll();
		controller.getRefJs(request, response);
		Assert.assertEquals(404, response.getStatus());
	}
	
	@Test
	public void testGetTalkBtn__Success() throws Exception{
		String jsContent = "jsContent";
		PowerMock.mockStatic(RtpSvc.class);
		IRtpSvc rtp = PowerMock.createMock(IRtpSvc.class);
		expect(RtpSvc.getInstance(request)).andReturn(rtp);
		PowerMock.mockStatic(UserUtils.class);
		expect(UserUtils.escapeCstnetId(TO_USER)).andReturn(TO_USER);
		expect(UserUtils.escapeCstnetId(FROM_USER)).andReturn(FROM_USER);
		expect(rtp.exec(eq("GetTalkButton"), eq(TO_USER),anyObject(Map.class),
				eq(""),eq(FROM_USER))).andReturn(jsContent);
		
		PowerMock.replayAll();
		Map<String, String> params = new HashMap<String, String>();
		params.put("to", TO_USER);
		params.put("from", FROM_USER);
		params.put("other", "other");
//		controller.getTalkButton((HttpServletRequest)request, (HttpServletResponse)response, params);
		Assert.assertNotNull(response.getContentAsString());
		Assert.assertEquals(jsContent, response.getContentAsString());
	}
	
	@Test
	public void testGetTalkBtn__Exception() throws Exception{
		PowerMock.mockStatic(RtpSvc.class);
		expect(RtpSvc.getInstance(request)).andThrow(new Exception());
		
		PowerMock.replayAll();
		controller.getRefJs(request, response);
		Assert.assertEquals(404, response.getStatus());
	}
	
	@Test
	public void testSendChatPage__Success() throws IOException{
		Map<String, String> params = new HashMap<String, String>();
		params.put("talk_with", "talk_with");
		params.put("pageurl", "");
		
		controller.sendChatPage(request, response, params);
		Assert.assertEquals("?talk_with=talk_with", response.getRedirectedUrl());
	}
	
	@After
	public void destroy(){
		controller = null;
		request = null;
		response = null;
	}
	
}
