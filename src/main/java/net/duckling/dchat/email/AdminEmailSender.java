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
package net.duckling.dchat.email;

import java.util.Date;
import java.util.Properties;

import javax.mail.Address;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import net.duckling.dchat.utils.Config;
import net.duckling.vmt.api.domain.message.MQBaseMessage;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.json.simple.JSONObject;

/**
 * 发送管理邮件给管理员
 * @author Yangxp
 * @since 2013-08-05
 */
public final class AdminEmailSender {
	private static final Logger LOG = Logger.getLogger(AdminEmailSender.class);
	private static final String SENDER_USERNAME_KEY = "email.sender.username";
	private static final String SENDER_PASSWORD_KEY = "email.sender.password";
	private static final String MAIL_SMTP_HOST = "mail.smtp.host";
	private String senderUsername;
	private String senderPassword;
	private String mailHost;
	
	private static class SingletonHolder{
		private static AdminEmailSender instance = new AdminEmailSender();
	}
	
	private AdminEmailSender(){
		senderUsername = Config.getProperty(SENDER_USERNAME_KEY);
		senderPassword = Config.getProperty(SENDER_PASSWORD_KEY);
		mailHost = Config.getProperty(MAIL_SMTP_HOST);
	}
	/**
	 * 获取AdminEmailSender实例
	 * @return
	 */
	public static AdminEmailSender getInstance(){
		return SingletonHolder.instance;
	}
	/**
	 * 发送警告邮件给管理员
	 * @param message 消息体
	 */
	public void sendWarnEmail2Admins(Exception e, MQBaseMessage msg, String additionalMsg){
		JSONObject message = generateMsgBody(e,msg,additionalMsg);
		sendEmail(message, "Warn");
	}
	/**
	 * 发送错误邮件给管理员
	 * @param message 消息体
	 */
	public void sendErrorEmail2Admins(Exception e, MQBaseMessage msg, String additionalMsg){
		JSONObject message = generateMsgBody(e,msg,additionalMsg);
		sendEmail(message, "Error");
	}
	
	private JSONObject generateMsgBody(Exception e, MQBaseMessage msg, String additionalMsg){
		JSONObject message = new JSONObject();
		if(null != e){
			message.put("exceptionName", e.getClass().getPackage()+e.getClass().getName());
			message.put("exceptionMsg", e.getMessage());
		}
		if(null != msg){
			message.put("MQMessage", msg.toJsonString());
		}
		if(StringUtils.isNotBlank(additionalMsg)){
			message.put("otherMessage", additionalMsg);
		}
		return message;
	}
	
	private void sendEmail(JSONObject msg, String type){
		try{
			Properties props = new Properties();
			Authenticator auth = new EmailAutherticator(senderUsername,senderPassword);
			props.put("mail.smtp.host", mailHost);
			props.put("mail.smtp.auth", "true");
			Session session = Session.getDefaultInstance(props, auth);
			MimeMessage message = new MimeMessage(session);
			Address address = new InternetAddress(senderUsername);
			message.setFrom(address);
			message.setSubject("dChat SDK Server [ "+type+" ] Mail");
			message.setText(msg.toString(), "UTF-8");
			message.setHeader("dChat SDK Server", type);
			message.setSentDate(new Date());
			message.addRecipients(Message.RecipientType.TO, new Address[]{address});
			Transport.send(message);
		}catch(Exception e){
			LOG.error("send notifier email failed! "+e.getMessage(), e);
		}
		LOG.info("Send Exception/Error Notifier Email to Admin Success!");
	}
	/**
	 * Email认证类
	 * @author Yangxp
	 *
	 */
	public static class EmailAutherticator extends Authenticator{
		private String username;
		private String password;
		public EmailAutherticator(){
			super();
		}
		public EmailAutherticator(String username, String password){
			super();
			this.username = username;
			this.password = password;
		}
		public PasswordAuthentication getPasswordAuthentication(){
			return new PasswordAuthentication(username,password);
		}
	}
}
