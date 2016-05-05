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

import net.duckling.dchat.exception.RTPServerException;
import net.duckling.dchat.rtp.domain.RTPRole;

import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.api.support.membermodification.MemberModifier;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.rooyeetone.rtp.sdk.IDataItem;
import com.rooyeetone.rtp.sdk.IRtpSvc;

@RunWith(PowerMockRunner.class)
@PrepareForTest({})
public class RTPRoleServiceTest {
	private RTPRoleService service = new RTPRoleService();
	
	private IRtpSvc modifyRTP() throws Exception{
		IRtpSvc rtp = PowerMock.createMock(IRtpSvc.class);
		MemberModifier.field(RTPRoleService.class, "rtp").set(service, rtp);
		return rtp;
	}
	
	@Test
	public void testAddRole() throws Exception{
		IRtpSvc rtp = modifyRTP();
		String name = "name",desc="desc",roleID = "1";
		EasyMock.expect(rtp.exec("Security.AddRole", name, desc)).andReturn(roleID);
		
		PowerMock.replayAll();
		int roleid = service.addRole(name, desc);
		PowerMock.verifyAll();
		Assert.assertEquals(Integer.valueOf(roleID), Integer.valueOf(roleid));
	}
	
	@Test(expected = RTPServerException.class)
	public void testAddRole__Exception() throws Exception{
		IRtpSvc rtp = modifyRTP();
		String name = "name",desc="desc";
		EasyMock.expect(rtp.exec("Security.AddRole", name, desc)).andThrow(new Exception());
		
		PowerMock.replayAll();
		service.addRole(name, desc);
		PowerMock.verifyAll();
	}
	
	@Test
	public void testGetRole() throws Exception{
		IRtpSvc rtp = modifyRTP();
		IDataItem item = PowerMock.createMock(IDataItem.class);
		int roleID = 1;
		EasyMock.expect(rtp.exec("Security.GetRoleInfo",String.valueOf(roleID))).andReturn(item);
		EasyMock.expect(item.getProp(RTPRole.RTP_ROLEID)).andReturn(String.valueOf(roleID));
		EasyMock.expect(item.getProp(RTPRole.RTP_USERID)).andReturn("1");
		EasyMock.expect(item.getProp(RTPRole.RTP_ROLE_NAME)).andReturn("rolename");
		EasyMock.expect(item.getProp(RTPRole.RTP_ROLE_DESC)).andReturn("roledesc");
		
		PowerMock.replayAll();
		RTPRole role = service.getRole(roleID);
		PowerMock.verifyAll();
		Assert.assertEquals(roleID, role.getRoleID());
	}
	
	@Test(expected = RTPServerException.class)
	public void testGetRole__Exception() throws Exception{
		IRtpSvc rtp = modifyRTP();
		int roleID = 1;
		EasyMock.expect(rtp.exec("Security.GetRoleInfo",String.valueOf(roleID))).andThrow(new Exception());
		
		PowerMock.replayAll();
		service.getRole(roleID);
		PowerMock.verifyAll();
	}
	
	@Test
	public void testGetRole__Null() throws Exception{
		IRtpSvc rtp = modifyRTP();
		int roleID = 1;
		EasyMock.expect(rtp.exec("Security.GetRoleInfo",String.valueOf(roleID))).andReturn(null);
		
		PowerMock.replayAll();
		RTPRole role = service.getRole(roleID);
		PowerMock.verifyAll();
		Assert.assertNull(role);
	}
	
	@Test
	public void testSetRoleRights() throws Exception{
		IRtpSvc rtp = modifyRTP();
		EasyMock.expect(rtp.exec("Security.SetRoleRights", "1","rights")).andReturn(new Object());
		
		PowerMock.replayAll();
		service.setRoleRights(1, "rights");
		PowerMock.verifyAll();
	}
	
	@Test(expected = RTPServerException.class)
	public void testSetRoleRights__Exception() throws Exception{
		IRtpSvc rtp = modifyRTP();
		EasyMock.expect(rtp.exec("Security.SetRoleRights", "1","rights")).andThrow(new Exception());
		
		PowerMock.replayAll();
		service.setRoleRights(1, "rights");
		PowerMock.verifyAll();
	}
	
	@Test
	public void testAddRoleRightTargets() throws Exception{
		IRtpSvc rtp = modifyRTP();
		EasyMock.expect(rtp.exec("Security.AddRoleRightTargets", "2","3","targets","type")).andReturn(new Object());
		
		PowerMock.replayAll();
		service.addRoleRightTargets(2, 3, "targets", "type");
		PowerMock.verifyAll();
	}
	
	@Test(expected = RTPServerException.class)
	public void testAddRoleRightTargets__Exception() throws Exception{
		IRtpSvc rtp = modifyRTP();
		EasyMock.expect(rtp.exec("Security.AddRoleRightTargets", "2","3","targetse","typee")).andThrow(new Exception());
		
		PowerMock.replayAll();
		service.addRoleRightTargets(2, 3, "targetse", "typee");
		PowerMock.verifyAll();
	}
}
