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

import net.duckling.dchat.exception.RTPDeptNotFoundException;
import net.duckling.dchat.exception.RTPServerException;
import net.duckling.dchat.utils.RtpServerUtils;

import org.easymock.EasyMock;
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
@PrepareForTest({RtpServerUtils.class})
public class RTPDeptServiceTest {
	
	private static final String DEPT = "DEPT";
	
	private RTPDeptService service = new RTPDeptService();
	private String deptPath = "deptPath";
	private String newDeptPath = "newDeptPath";
	
	private IRtpSvc modifyRTP() throws IllegalArgumentException, IllegalAccessException{
		IRtpSvc rtp = PowerMock.createMock(IRtpSvc.class);
		MemberModifier.field(RTPDeptService.class, "rtp").set(service, rtp);
		return rtp;
	}
	
	@Test(expected = RTPServerException.class)
	public void testMoveDept() throws Exception{
		IRtpSvc rtp = modifyRTP();
		ISyncItem item = PowerMock.createMock(ISyncItem.class);
		EasyMock.expect(rtp.exec("syncData", deptPath, DEPT, "CHANGEPARENT")).andReturn(item);
		item.setProp("parentgroup", newDeptPath);
		item.commit();
		EasyMock.expectLastCall().andThrow(new Exception());
		
		PowerMock.replayAll();
		service.moveDept(deptPath, newDeptPath);
		PowerMock.verifyAll();
	}
	
	@Test(expected = RTPServerException.class)
	public void testRemoveDept() throws Exception{
		IRtpSvc rtp = modifyRTP();
		EasyMock.expect(rtp.exec("DelDept", deptPath, Boolean.TRUE)).andThrow(new Exception("xx"));
		
		PowerMock.replayAll();
		service.removeDept(deptPath);
		PowerMock.verifyAll();
	}
	
	@Test
	public void testGetDeptUsers__Success() throws Exception{
		IRtpSvc rtp = modifyRTP();
		IPagedResult result = PowerMock.createMock(IPagedResult.class);
		EasyMock.expect(rtp.exec("GetDeptUser", deptPath, Boolean.TRUE)).andReturn(result);
		prepareItems(result);
		
		PowerMock.replayAll();
		service.getDeptUsers(deptPath, true);
		PowerMock.verifyAll();
	}
	
	private void prepareItems(IPagedResult result){
		List<IDataItem> items = new ArrayList<IDataItem>();
		IDataItem item = PowerMock.createMock(IDataItem.class);
		IDataItem item2 = PowerMock.createMock(IDataItem.class);
		items.add(item);
		items.add(item2);
		EasyMock.expect(result.getItems()).andReturn(items);
		EasyMock.expect(item.getProp("user")).andReturn("a");
		EasyMock.expect(item.getProp("fullname")).andReturn("b");
		EasyMock.expect(item.getProp("nickname")).andReturn("c");
		EasyMock.expect(item2.getProp("user")).andReturn("d");
		EasyMock.expect(item2.getProp("fullname")).andReturn("e");
		EasyMock.expect(item2.getProp("nickname")).andReturn("f");
	}
	
	@Test(expected = RTPDeptNotFoundException.class)
	public void testGetDeptUsers__DeptNotFound() throws Exception{
		IRtpSvc rtp = modifyRTP();
		EasyMock.expect(rtp.exec("GetDeptUser", deptPath, Boolean.TRUE)).andThrow(new Exception("GroupNotFoundException"));
		
		PowerMock.replayAll();
		service.getDeptUsers(deptPath, true);
		PowerMock.verifyAll();
	}
	
	@Test(expected = RTPServerException.class)
	public void testGetDeptUsers__Server() throws Exception{
		IRtpSvc rtp = modifyRTP();
		EasyMock.expect(rtp.exec("GetDeptUser", deptPath, Boolean.TRUE)).andThrow(new Exception("xx"));
		
		PowerMock.replayAll();
		service.getDeptUsers(deptPath, true);
		PowerMock.verifyAll();
	}
	
	@Test(expected = RTPServerException.class)
	public void testAddDept() throws Exception{
		IRtpSvc rtp = modifyRTP();
		PowerMock.mockStatic(RtpServerUtils.class);
		EasyMock.expect(RtpServerUtils.getRTPSortWeights(1)).andReturn(1);
		ISyncItem item = PowerMock.createMock(ISyncItem.class);
		EasyMock.expect(rtp.exec("SyncData", deptPath, DEPT, "INSERT")).andReturn(item);
		item.setProp("sortweights", "1");
		item.commit();
		EasyMock.expectLastCall().andThrow(new Exception());
		
		PowerMock.replayAll();
		service.addDept(deptPath, 1);
		PowerMock.verifyAll();
	}
	
	@Test(expected = RTPServerException.class)
	public void testRenameDept() throws Exception{
		IRtpSvc rtp = modifyRTP();
		ISyncItem item = PowerMock.createMock(ISyncItem.class);
		EasyMock.expect(rtp.exec("SyncData", deptPath, DEPT, "RENAME")).andReturn(item);
		item.setProp("newname", newDeptPath);
		item.commit();
		EasyMock.expectLastCall().andThrow(new Exception());
		
		PowerMock.replayAll();
		service.renameDept(deptPath, newDeptPath);
		PowerMock.verifyAll();
	}
	
	@Test(expected = RTPServerException.class)
	public void testUpdateSortWeights() throws Exception{
		IRtpSvc rtp = modifyRTP();
		PowerMock.mockStatic(RtpServerUtils.class);
		EasyMock.expect(RtpServerUtils.getRTPSortWeights(1)).andReturn(1);
		ISyncItem item = PowerMock.createMock(ISyncItem.class);
		EasyMock.expect(rtp.exec("SyncData", deptPath, DEPT, "UPDATE")).andReturn(item);
		item.setProp("sortweights", "1");
		item.commit();
		EasyMock.expectLastCall().andThrow(new Exception());
		
		PowerMock.replayAll();
		service.updateSortWeights(deptPath, 1);
		PowerMock.verifyAll();
	}
}
