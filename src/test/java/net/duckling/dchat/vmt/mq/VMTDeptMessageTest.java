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
import java.util.List;

import net.duckling.dchat.email.AdminEmailSender;
import net.duckling.dchat.exception.RTPServerException;
import net.duckling.dchat.exception.RTPUserNotFoundException;
import net.duckling.dchat.rtp.domain.GroupNode;
import net.duckling.dchat.rtp.service.interf.IRTPDeptService;
import net.duckling.dchat.rtp.service.interf.IRTPUserService;
import net.duckling.dchat.utils.RtpServerUtils;
import net.duckling.dchat.utils.UserUtils;
import net.duckling.dchat.vmt.service.VmtService;
import net.duckling.vmt.api.IRestOrgService;
import net.duckling.vmt.api.domain.VmtDepart;
import net.duckling.vmt.api.domain.VmtOrg;
import net.duckling.vmt.api.domain.VmtUser;
import net.duckling.vmt.api.domain.message.MQCreateDepartMessage;
import net.duckling.vmt.api.domain.message.MQDeleteDepartMessage;
import net.duckling.vmt.api.domain.message.MQMoveDepartMessage;
import net.duckling.vmt.api.domain.message.MQUpdateDepartMessage;

import org.easymock.EasyMock;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import cn.vlabs.rest.ServiceException;

import com.rooyeetone.rtp.sdk.RtpSvc;

@RunWith(PowerMockRunner.class)
@PrepareForTest({RtpSvc.class, RtpServerUtils.class, UserUtils.class, AdminEmailSender.class})
public class VMTDeptMessageTest {
	
	private String deptPath = "deptPath";
	private String deptCurrentDisplay = "currentDisplay";
	private IRTPDeptService deptService = null;
	private IRTPUserService userService = null;
	private VmtService vmtService = null;
	
	@Test
	public void testCreateDepart__Success() throws RTPServerException{
		MQCreateDepartMessage msg = createCreateMsg();
		VMTDeptMessageHandler handler = createHandler();
		PowerMock.mockStatic(UserUtils.class);
		EasyMock.expect(UserUtils.formatDept(deptCurrentDisplay)).andReturn(deptPath);
		deptService.addDept(deptPath, 0);
		EasyMock.expectLastCall();
		
		PowerMock.replayAll();
		handler.handle(msg);
		PowerMock.verifyAll();
	}
	
	@Test
	public void testCreateDepart__Exception() throws RTPServerException{
		MQCreateDepartMessage msg = createCreateMsg();
		VMTDeptMessageHandler handler = createHandler();
		PowerMock.mockStatic(UserUtils.class);
		EasyMock.expect(UserUtils.formatDept(deptCurrentDisplay)).andReturn(deptPath);
		deptService.addDept(deptPath, 0);
		Exception e = new RTPServerException(new Exception());
		EasyMock.expectLastCall().andThrow(e);
		
		PowerMock.replayAll();
		handler.handle(msg);
		PowerMock.verifyAll();
	}
	
	private VMTDeptMessageHandler createHandler(){
		deptService = PowerMock.createMock(IRTPDeptService.class);
		userService = PowerMock.createMock(IRTPUserService.class);
		vmtService = PowerMock.createMock(VmtService.class);
		return new VMTDeptMessageHandler(deptService, userService, vmtService);
	}
	
	private MQCreateDepartMessage createCreateMsg(){
		VmtDepart depart = new VmtDepart();
		depart.setSymbol("newdepart");
		depart.setCreator("test@dchat.com");
		depart.setCurrentDisplay(deptCurrentDisplay);
		String parentDN = "vmt-symbol=parentDN,ou=org,dc=vmt";
		return new MQCreateDepartMessage(depart,parentDN);
	}
	
	@Test
	public void testDeleteDepart() throws RTPServerException{
		MQDeleteDepartMessage msg = createDelMsg();
		VMTDeptMessageHandler handler = createHandler();
		PowerMock.mockStatic(UserUtils.class);
		EasyMock.expect(UserUtils.formatDept(deptCurrentDisplay)).andReturn(deptPath);
		deptService.removeDept(deptPath);
		Exception e = new RTPServerException(new Exception());
		EasyMock.expectLastCall().andThrow(e);
		
		PowerMock.replayAll();
		handler.handle(msg);
		PowerMock.verifyAll();
	}
	
	private MQDeleteDepartMessage createDelMsg() throws RTPServerException{
		VmtDepart depart = new VmtDepart();
		depart.setCurrentDisplay(deptCurrentDisplay);
		return new MQDeleteDepartMessage(null, depart);
	}
	
	@Test
	public void testUpdateDepart() throws RTPServerException{
		MQUpdateDepartMessage msg = createUpdateMsg();
		VMTDeptMessageHandler handler = createHandler();
		String oldDept = deptCurrentDisplay+"/"+deptCurrentDisplay;
		PowerMock.mockStatic(UserUtils.class);
		EasyMock.expect(UserUtils.formatDept("currentDept")).andReturn("currentDept");
		EasyMock.expect(UserUtils.formatDept(deptCurrentDisplay)).andReturn(oldDept);
		deptService.renameDept("currentDept", deptCurrentDisplay);
		deptService.updateSortWeights(oldDept, 0);
		Exception e = new RTPServerException(new Exception());
		EasyMock.expectLastCall().andThrow(e);
		
		PowerMock.replayAll();
		handler.handle(msg);
		PowerMock.verifyAll();
	}
	
	private MQUpdateDepartMessage createUpdateMsg(){
		VmtDepart depart = new VmtDepart();
		depart.setCurrentDisplay(deptCurrentDisplay);
		MQUpdateDepartMessage msg = new MQUpdateDepartMessage(depart);
		msg.setCurrentDept("currentDept");
		return msg;
	}
	
	@Test
	public void testMoveDepart__ContainSelf() throws RTPServerException{
		MQMoveDepartMessage msg = createMoveMsg(true);
		VMTDeptMessageHandler handler = createHandler();
		PowerMock.mockStatic(UserUtils.class);
		EasyMock.expect(UserUtils.formatDept(deptCurrentDisplay)).andReturn(deptCurrentDisplay).times(3);
		
		deptService.moveDept(deptCurrentDisplay, deptCurrentDisplay);
		EasyMock.expectLastCall();
		
		PowerMock.replayAll();
		handler.handle(msg);
		PowerMock.verifyAll();
	}
	
	@Test
	public void testMoveDepart__NotContainSelf() throws ServiceException, 
			RTPServerException, RTPUserNotFoundException{
		MQMoveDepartMessage msg = createMoveMsg(false);
		VMTDeptMessageHandler handler = createHandler();
		PowerMock.mockStatic(UserUtils.class);
		EasyMock.expect(UserUtils.formatDept(deptCurrentDisplay)).andReturn(deptCurrentDisplay).times(3);
		IRestOrgService vmtOrgService = PowerMock.createMock(IRestOrgService.class);
		EasyMock.expect(vmtService.getVmtOrgService()).andReturn(vmtOrgService);
		
		deptService.moveDept(deptCurrentDisplay, deptCurrentDisplay);
		userService.updateUserDept("cstnetid","", deptCurrentDisplay);
		EasyMock.expectLastCall();
		
		PowerMock.replayAll();
		handler.handle(msg);
		PowerMock.verifyAll();
	}
	
	private MQMoveDepartMessage createMoveMsg(boolean isContainSelf){
		VmtDepart depart = new VmtDepart();
		depart.setCurrentDisplay(deptCurrentDisplay);
		depart.setDn("dn");
		
		VmtDepart target = new VmtDepart();
		target.setCurrentDisplay(deptCurrentDisplay);
		
		VmtOrg org = new VmtOrg();
		org.setSymbol("org");
		org.setCreator("test@dchat.com");
		
		MQMoveDepartMessage msg = new MQMoveDepartMessage(depart, false, target, org);
		msg.setContainSelf(isContainSelf);
		return msg;
	}
	
	@SuppressWarnings("unused")
	private List<Object> createChildNodes(){
		List<Object> result = new ArrayList<Object>();
		VmtUser user = new VmtUser();
		user.setCstnetId("cstnetid");
		result.add(user);
		VmtDepart depart = new VmtDepart();
		depart.setCurrentDisplay(deptCurrentDisplay);
		result.add(depart);
		return result;
	}
	

	/*  L
	 * 	|--A
	 *  |  |--B
	 * 	|  |--C
	 * 	|  |  |--E
	 * 	|  |  |--F
	 * 	|  |--D 
	 */
	private List<GroupNode> getLeftTree(){
		List<GroupNode> treeList = new ArrayList<GroupNode>();
		GroupNode[] array = new GroupNode[]{
				buildGroupNode(1,"/L/","A","1",9),
				buildGroupNode(2,"/L/A/","B","2",1),
				buildGroupNode(3,"/L/A/","C","2",1),
				buildGroupNode(4,"/L/A/","D","2",1),
				buildGroupNode(5,"/L/A/C/","E","3",3),
				buildGroupNode(6,"/L/A/C/","F","3",3)
		};
		for(int i=0;i<array.length;i++){
			treeList.add(array[i]);
		}
		return treeList;
	}
	
	

	/*  R
	 * 	|--A
	 * 	|  |--C
	 * 	|  |  |--E
	 * 	|  |  |--G
	 * 	|  |--D 
	 */
	private List<GroupNode> getRightTree(){
		List<GroupNode> treeList = new ArrayList<GroupNode>();
		GroupNode[] array = new GroupNode[]{
				buildGroupNode(11,"/R/","A","1",10),
				buildGroupNode(12,"/R/A/","C","2",11),
				buildGroupNode(13,"/R/A/","D","2",11),
				buildGroupNode(14,"/R/A/C/","E","3",12),
				buildGroupNode(15,"/R/A/C/","G","3",12)
		};
		for(int i=0;i<array.length;i++){
			treeList.add(array[i]);
		}
		return treeList;
	}
	
	private GroupNode buildGroupNode(int id, String prefix, String current, String level, int pid) {
		GroupNode n = new GroupNode();
		n.setGroupID(id);
		n.setPath(prefix+current);
		n.setParentID(pid);
		n.setLevel(level);
		n.setCurrent(current);
		return n;
	}

	@Test
	public void testMergeDepart() throws RTPServerException, RTPUserNotFoundException{
		EasyMock.expect(deptService.getSubDeptList("/L/A")).andReturn(getLeftTree());
		EasyMock.expect(deptService.getSubDeptList("/R/A")).andReturn(getRightTree());
		EasyMock.replay(deptService);
		//handler.moveDepartment2("/L/A", "/R/A");
	}
}
