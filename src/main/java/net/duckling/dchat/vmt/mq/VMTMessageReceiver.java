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

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import net.duckling.dchat.utils.Config;
import net.duckling.falcon.api.mq.DFMQFactory;
import net.duckling.falcon.api.mq.DFMQMode;
import net.duckling.falcon.api.mq.IDFMessageHandler;
import net.duckling.falcon.api.mq.IDFSubscriber;
import net.duckling.falcon.api.mq.NotFoundHandlerException;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * VMT消息接收器
 * @author Yangxp
 * @since 2013-08-01
 */
@Component
public class VMTMessageReceiver {
	private static final Logger LOG = Logger.getLogger(VMTMessageReceiver.class);
	
	private String host;
	private String exchange;
	private String queue;
	private String username;
	private String password;
	@Autowired 
	private VMTMessageHandler msgHandler;
	
	private List<Thread> messageThreads = new ArrayList<Thread>();
	private List<IDFSubscriber> receivers = new ArrayList<IDFSubscriber>();
	
	/**
	 * 初始化MQ的配置，并绑定队列和路由
	 */
	@PostConstruct
	public void init(){
		host = Config.getProperty("vmt.mq.host");
		exchange = Config.getProperty("vmt.mq.exchange");
		queue = Config.getProperty("vmt.mq.queue");
		username = Config.getProperty("vmt.mq.username");
		password = Config.getProperty("vmt.mq.password");
		if(StringUtils.isBlank(host) || StringUtils.isBlank(exchange)){
			LOG.error("init VMTMessageReceiver failed, please check config of vmt.mq.* in dchat.properties");
			return;
		}
		String[] routingKey = new String[]{"#.user","#.group","#.org","#.dept"};
		bindQueueAndStartThread(queue,msgHandler,routingKey);
	}
	
	private void bindQueueAndStartThread(String queueName, IDFMessageHandler handler, String[] routingKey){
		IDFSubscriber receiver = DFMQFactory.buildSubscriber(username, password, host, exchange, queueName, DFMQMode.TOPIC);
		for(String rkey : routingKey){
			receiver.registHandler(rkey, handler);
		}
		receivers.add(receiver);
		Thread thread = new MessageThread(receiver);
		messageThreads.add(thread);
		thread.start();
	}
	/**
	 * 关闭所有的MQ消息接收器，并将对应的线程结束
	 */
	@PreDestroy
	public void destroy(){
		for(IDFSubscriber receiver : receivers){
			receiver.close();
		}
		for(Thread thread : messageThreads){
			thread.interrupt();
		}
	}
	/**
	 * 消息接收器的处理线程
	 * @author Yangxp
	 *
	 */
	public static class MessageThread extends Thread{
		private IDFSubscriber myReceiver;
		public MessageThread(IDFSubscriber receiver){
			this.myReceiver = receiver;
		}
		@Override
		public void run() {
			try {
				myReceiver.receive();
			} catch (NotFoundHandlerException e) {
				LOG.error("Not Found Hanlder", e);
			}
		}
		
	}
}
