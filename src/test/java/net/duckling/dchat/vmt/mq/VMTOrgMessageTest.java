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

import net.duckling.falcon.api.mq.DFMQFactory;
import net.duckling.falcon.api.mq.DFMQMode;
import net.duckling.falcon.api.mq.IDFPublisher;
import net.duckling.vmt.api.domain.VmtOrg;
import net.duckling.vmt.api.domain.message.MQBaseMessage;
import net.duckling.vmt.api.domain.message.MQCreateOrgMessage;
import net.duckling.vmt.api.domain.message.MQDeleteOrgMessage;
import net.duckling.vmt.api.domain.message.MQRefreshOrgMessage;
import net.duckling.vmt.api.domain.message.MQUpdateOrgMessage;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations="classpath:springContext4Test.xml")
public class VMTOrgMessageTest {
	private IDFPublisher sender = null;
	
	@Before
	public void setUp(){
		sender = DFMQFactory.buildPublisher("guest","guest", "10.10.2.6","test_topic", DFMQMode.TOPIC);
	}
	
	@Test
	public void testCreateOrg(){
		VmtOrg org = new VmtOrg();
		org.setSymbol("neworg");
		org.setCreator("test@dchat.com");
		MQCreateOrgMessage msg = new MQCreateOrgMessage(org, null);
		sender.send(msg, "create.org");
	}
	
	@Test
	public void testDeleteOrg(){
		MQDeleteOrgMessage msg = new MQDeleteOrgMessage(
				MQBaseMessage.OPERATION_DELETE, MQBaseMessage.SCOPE_ORG);
		sender.send(msg, "delete.org");
	}
	
	@Test
	public void testUpdateOrg(){
		MQUpdateOrgMessage msg = new MQUpdateOrgMessage(null, null);
		sender.send(msg, "update.org");
	}
	
	@Test
	public void testRefreshOrg(){
		MQRefreshOrgMessage msg = new MQRefreshOrgMessage(null);
		sender.send(msg, "refresh.org");
	}
	
	@Test
	public void testInvalidUserMessage(){
		MQBaseMessage msg = new MQBaseMessage(MQBaseMessage.OPERATION_CREATE, MQBaseMessage.SCOPE_ORG);
		sender.send(msg, "xx.dept");
	}
	
	@After
	public void tearDown(){
		sender.close();
		sender = null;
	}
}
