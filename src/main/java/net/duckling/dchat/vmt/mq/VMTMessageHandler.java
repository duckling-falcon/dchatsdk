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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import net.duckling.dchat.rtp.service.interf.IRTPDeptService;
import net.duckling.dchat.rtp.service.interf.IRTPGroupService;
import net.duckling.dchat.rtp.service.interf.IRTPUserService;
import net.duckling.dchat.utils.Config;
import net.duckling.dchat.vmt.service.VmtService;
import net.duckling.falcon.api.mq.IDFMessageHandler;
import net.duckling.vmt.api.domain.message.MQBaseMessage;
import net.duckling.vmt.api.domain.message.MQCreateGroupMessage;
import net.duckling.vmt.api.domain.message.MQDeleteGroupMessage;
import net.duckling.vmt.api.domain.message.MQLinkUserMessage;
import net.duckling.vmt.api.domain.message.MQRefreshGroupMessage;
import net.duckling.vmt.api.domain.message.MQUnlinkUserMessage;
import net.duckling.vmt.api.domain.message.MQUpdateGroupMessage;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
/**
 * VMT MQ消息处理入口
 * @author Yangxp
 * @since 2013-08-07
 */
@Component
public class VMTMessageHandler implements IDFMessageHandler {
	private static final Logger LOG = Logger.getLogger(VMTMessageHandler.class);
	private static final String IGNORE_GROUPID_KEY = "vmt.mq.ignore.groupid";
	
	@Autowired
	private IRTPUserService userService;
	@Autowired
	private IRTPGroupService groupService;
	@Autowired
	private VmtService vmtService;
	@Autowired
	private IRTPDeptService deptService;
	
	private VMTDeptMessageHandler deptHandler;
	private VMTUserMessageHandler userHandler;
	private VMTOrgMessageHandler orgHandler;
	private VMTGroupMessageHandler groupHandler;
	private Set<String> ignoreGroups = new HashSet<String>();
	
	@PostConstruct
	public void init(){
		this.deptHandler = new VMTDeptMessageHandler(deptService, userService, vmtService);
		this.userHandler = new VMTUserMessageHandler(userService, groupService, vmtService);
		this.orgHandler = new VMTOrgMessageHandler(deptService, userService, vmtService);
		this.groupHandler = new VMTGroupMessageHandler(groupService,  userService, vmtService);
		String ignoreGroupsStr = Config.getProperty(IGNORE_GROUPID_KEY); //初始化该被无视的群组列表，以群的symbol为唯一标示
		if(StringUtils.isNotBlank(ignoreGroupsStr)){
			ignoreGroups.addAll(Arrays.asList(ignoreGroupsStr.split(",")));
		}
	}
	
	@PreDestroy
	public void destroy(){
		deptHandler = null;
		userHandler = null;
		orgHandler = null;
		groupHandler = null;
	}
	
	@Override
	public void handle(Object obj, String routingKey) {
		try{
			MQBaseMessage msg = (MQBaseMessage)obj;
			String messageType = msg.getType();
			if(messageType.endsWith("user")){
				if(isIgnoreGroup(msg)){
					LOG.info("add/remove user from ingored groups is not allowed, drop this message! "+msg.toJsonString());
					return;
				}
				userHandler.handle(msg);
			}else if(messageType.endsWith("dept")){
				deptHandler.handle(msg);
			}else if(messageType.endsWith("org")){
				orgHandler.handle(msg);
			}else if(messageType.endsWith("group")){
				if(isIgnoreGroup(msg)){
					LOG.info("message about processing ingored groups is not allowed, drop this message! "+msg.toJsonString());
					return;
				}
				groupHandler.handle(msg);
			}else{
				LOG.info("receive message "+messageType+" but it's invalid, ignore! "+msg.toJsonString());
			}
		}catch(RuntimeException e){
			LOG.info("Runtime Exception! ", e);
			throw e;
		}
	}
	/**
	 * 判断消息是否是群消息或者向群增删成员的消息，若是则查看该群是否应该被忽视
	 * @param msg MQ消息对象
	 * @return true：无视，不处理该消息；false：正常处理消息
	 */
	private boolean isIgnoreGroup(MQBaseMessage msg){
		String groupSymbol = null;
		if(msg instanceof MQCreateGroupMessage){
			groupSymbol = ((MQCreateGroupMessage)msg).getGroup().getSymbol();
		}else if(msg instanceof MQDeleteGroupMessage){
			groupSymbol = ((MQDeleteGroupMessage)msg).getGroup().getSymbol();
		}else if(msg instanceof MQUpdateGroupMessage){
			groupSymbol = ((MQUpdateGroupMessage)msg).getGroup().getSymbol();
		}else if(msg instanceof MQRefreshGroupMessage){
			groupSymbol = ((MQRefreshGroupMessage)msg).getGroup().getSymbol();
		}else if(msg instanceof MQLinkUserMessage){
			MQLinkUserMessage message = (MQLinkUserMessage)msg;
			if(message.isGroup()){
				groupSymbol = message.getGroup().getSymbol();
			}
		}else if(msg instanceof MQUnlinkUserMessage){
			MQUnlinkUserMessage message = (MQUnlinkUserMessage)msg;
			if(message.isGroup()){
				groupSymbol = message.getGroup().getSymbol();
			}
		} 
		return StringUtils.isNotBlank(groupSymbol)?ignoreGroups.contains(groupSymbol):false;
	}

}
