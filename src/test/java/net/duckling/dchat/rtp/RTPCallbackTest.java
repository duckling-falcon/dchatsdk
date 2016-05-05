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
package net.duckling.dchat.rtp;

import net.duckling.dchat.umt.UmtClient;
import net.duckling.dchat.utils.UserUtils;

import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.rooyeetone.rtp.sdk.IEvent;

@RunWith(PowerMockRunner.class)
@PrepareForTest({UserUtils.class, UmtClient.class})
public class RTPCallbackTest {
	private static final String USERNAME = "username";
	
	@Test
	public void testLogin__reLogin() throws Exception{
		IEvent event = PowerMock.createMock(IEvent.class);
		RTPCallback callback = new RTPCallback();
		
		PowerMock.replayAll();
		boolean result = callback.login(event, USERNAME, "password", false);
		Assert.assertEquals(true, result);
	}
	
	@Test
	public void testLogin__loginFirst__Success() throws Exception{
		IEvent event = PowerMock.createMock(IEvent.class);
		RTPCallback callback = new RTPCallback();
		PowerMock.mockStatic(UserUtils.class);
		EasyMock.expect(UserUtils.unescapeCstnetId(USERNAME)).andReturn(USERNAME);
		PowerMock.mockStatic(UmtClient.class);
		UmtClient instance = PowerMock.createMock(UmtClient.class);
		EasyMock.expect(UmtClient.getInstance()).andReturn(instance);
		EasyMock.expect(instance.login(USERNAME, "password")).andReturn(true);
		
		PowerMock.replayAll();
		boolean result = callback.login(event, USERNAME, "password", false);
		Assert.assertEquals(true, result);
	}
	
	@Test
	public void testLogin__loginFirst__Fail() throws Exception{
		IEvent event = PowerMock.createMock(IEvent.class);
		RTPCallback callback = new RTPCallback();
		PowerMock.mockStatic(UserUtils.class);
		EasyMock.expect(UserUtils.unescapeCstnetId(USERNAME)).andReturn(USERNAME);
		PowerMock.mockStatic(UmtClient.class);
		UmtClient instance = PowerMock.createMock(UmtClient.class);
		EasyMock.expect(UmtClient.getInstance()).andReturn(instance);
		EasyMock.expect(instance.login(USERNAME, "password")).andReturn(false);
		
		PowerMock.replayAll();
		boolean result = callback.login(event, USERNAME, "password", false);
		Assert.assertEquals(false, result);
	}

}
