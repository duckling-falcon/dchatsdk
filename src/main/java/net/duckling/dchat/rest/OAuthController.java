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
package net.duckling.dchat.rest;

import java.io.IOException;
import java.util.Date;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.duckling.dchat.rtp.domain.GroupJID;
import net.duckling.dchat.rtp.domain.UserJID;
import net.duckling.dchat.umt.UmtClient;
import net.duckling.dchat.utils.Config;
import net.duckling.dchat.utils.DChatConstants;
import net.duckling.dchat.vmt.VmtClient;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.rooyeetone.rtp.sdk.IRtpSvc;
import com.rooyeetone.rtp.sdk.RtpSvc;

import cn.vlabs.umt.oauth.AccessToken;
import cn.vlabs.umt.oauth.Oauth;
import cn.vlabs.umt.oauth.UMTOauthConnectException;
import cn.vlabs.umt.oauth.UserInfo;
import cn.vlabs.umt.oauth.common.exception.OAuthProblemException;

@Controller
@RequestMapping("/oauth")
public class OAuthController {

	private UmtClient umt = UmtClient.getInstance();
	
	private VmtClient vmt = VmtClient.getInstance();

	private static final Logger LOG = Logger.getLogger(OAuthController.class);

	public Properties getOauthConfig() {
		Properties prop = new Properties();
		prop.setProperty("client_id", Config.getProperty("oauth.dchat.client_id"));
		prop.setProperty("client_secret", Config.getProperty("oauth.dchat.client_secret"));
		prop.setProperty("redirect_uri", Config.getProperty("oauth.dchat.redirect_uri"));
		prop.setProperty("access_token_URL", Config.getProperty("oauth.dchat.access_token_URL"));
		prop.setProperty("authorize_URL", Config.getProperty("oauth.dchat.access_token_URL"));
		prop.setProperty("scope", "");
		prop.setProperty("theme", Config.getProperty("oauth.dchat.theme"));
		return prop;
	}

	@RequestMapping("/callback")
	public void oauthCallback(HttpServletRequest request, HttpServletResponse response) throws IOException,
			ServletException {
		Properties config = this.getOauthConfig();
		String state = request.getParameter("state");
		Oauth o = new Oauth(config);
		LOG.info(config.getProperty("client_id"));
		LOG.info(config.getProperty("redirect_uri"));
		LOG.info(config.getProperty("access_token_URL"));
		try {
			AccessToken t = o.getAccessTokenByRequest(request);
			String token = t.getAccessToken();
			UserInfo u = t.getUserInfo();
			if (u == null) {
				// 登录失败 TODO
				LOG.info("Login failed");
			} else {
				String user = u.getCstnetId();
				umt.setUmtToken(user, token);
				String embedToken = this.getEmbedToken(user);
				String viewUrl = DChatConstants.getDomain() + "/dchat/oauth/embed?token=" + embedToken;
				umt.produceEmbedToken(user, embedToken);
				if (state.startsWith("talkto_")) {
					String target = state.replace("talkto_", "");
					viewUrl += "&state=talkto&target=" + target;
				} else if(state.startsWith("login")) {
					viewUrl += "&state=login";
				} else if(state.startsWith("grouptalk_")){
					String target = state.replace("grouptalk_", "");
					viewUrl += "&state=grouptalk&target=" + target;
					vmt.addUserToGroup(u.getCstnetId(), target);
				}
				request.getSession().setAttribute(DChatConstants.CURRENT_USER, u.getCstnetId());
				response.sendRedirect(viewUrl);
				LOG.info("Successfully authenticated user:" + user + ";access_token:" + token);
			}
		} catch (UMTOauthConnectException e) {
			LOG.error("", e);
		} catch (OAuthProblemException e) {
			LOG.error("", e);
		}
	}

	@RequestMapping("/embed")
	public ModelAndView displayEmbed(HttpServletRequest request) {
		String username = (String) request.getSession().getAttribute(DChatConstants.CURRENT_USER);
		if (StringUtils.isEmpty(username)) {
			return null;
		}
		ModelAndView mv = new ModelAndView();
		String embedToken = request.getParameter("token");
		mv.addObject("user", username);
		String state = request.getParameter("state");
		if ("login".equals(state)) {
			mv.setViewName("client-login");
			mv.addObject("loginLink", getLoginURL(username, embedToken));
		}
		if ("talkto".equals(state)) {
			String target = request.getParameter("target");
			mv.setViewName("client-talkto");
			mv.addObject("account", target);
			try {
				IRtpSvc rtpsvc = RtpSvc.getInstance(request);
				String targetState = rtpsvc.exec("GetState", target);
				String stateImage = getImagePathByState(targetState);
				mv.addObject("stateImage", stateImage);
			} catch (Exception e) {
				LOG.error(e.getMessage());
			}
			mv.addObject("talkToLink", getTalkToURL(username, target, embedToken));
		}
		if ("grouptalk".equals(state)) {
			String groupid = request.getParameter("target");
			mv.setViewName("client-grouptalk");
			mv.addObject("targetGroup", groupid);
			
			String targetState = "online";
			String stateImage = getImagePathByState(targetState);
			mv.addObject("stateImage", stateImage);
			mv.addObject("groupTalkLink", getGroupTalkToURL(username, groupid, embedToken));
		}
		return mv;
	}

	private String getImagePathByState(String targetState) {
		//online-在线, chat-正在交谈, away-暂时离开, dnd-请勿打扰, xa-长时间离开, offline-离线 
		switch(targetState){
		case "online":
			return "talktome.png";
		case "chat":
			return "talktome.png";
		case "away":
			return "away.png";
		case "dnd":
			return "busy.png";
		case "xa":
			return "away.png";
		case "offline":
			return "offline.png";
		}
		return "offline.png";
	}

	private String base64Encode(String template) {
		byte[] bts = Base64.encodeBase64(template.getBytes());
		StringBuffer sb = new StringBuffer();
		for (byte b : bts) {
			sb.append((char) b);
		}
		return sb.toString();
	}

	public String getLoginURL(String user, String embedToken) {
		UserJID jid = new UserJID(user, null);
		String content = "//" + jid + "?login;pass=utoken_" + embedToken;
		return base64Encode(content);
	}

	public String getTalkToURL(String fromUser, String toUser, String embedToken) {
		UserJID fromJid = new UserJID(fromUser, null);
		UserJID toJid = new UserJID(toUser, null);
		String content = "//" + fromJid + "/" + toJid + "?message;pass=utoken_" + embedToken;
		return base64Encode(content);
	}
	
	private String getGroupTalkToURL(String fromUser, String toGroup, String embedToken){
		UserJID fromJid = new UserJID(fromUser, null);
		GroupJID toGroupJid = new GroupJID(toGroup);
		String content = "//" + fromJid + "/" + toGroupJid + "?message;pass=utoken_" + embedToken;
		return base64Encode(content);
	}

	private String getEmbedToken(String user) {
		Date now = new Date();
		return base64Encode(user + now.getTime());
	}

}
