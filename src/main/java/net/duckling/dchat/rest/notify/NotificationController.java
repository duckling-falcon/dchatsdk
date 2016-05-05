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
package net.duckling.dchat.rest.notify;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.duckling.dchat.email.CoreMailService;
import net.duckling.dchat.email.MailInfo;
import net.duckling.dchat.rtp.domain.UserPrefs;
import net.duckling.dchat.rtp.service.RestUtils;
import net.duckling.dchat.rtp.service.UserPrefsService;
import net.duckling.dchat.rtp.service.interf.IRTPUserService;
import net.duckling.dchat.umt.UmtClient;
import net.duckling.dchat.utils.Config;
import net.duckling.dchat.utils.DChatConstants;
import net.duckling.dchat.utils.RTPCharUtils;
import net.duckling.dchat.utils.UserUtils;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.rooyeetone.rtp.sdk.IRtpSvc;
import com.rooyeetone.rtp.sdk.RtpSvc;

@Controller
@RequestMapping("/v1/notify")
public class NotificationController {

	private static final String CHAR_SET = "UTF-8";

	private static final Logger LOG = Logger.getLogger(NotificationController.class);

	@Autowired
	private CoreMailService coreMailService;
	@Autowired
	private UserPrefsService ups;

	private Map<String, Date> lastPopTimeMap = new ConcurrentHashMap<String, Date>();

	@ResponseBody
	@RequestMapping(value = "/email", method = RequestMethod.GET)
	public JSONObject getNewEmailsCount(HttpServletRequest request, @RequestParam("user") String user) {
		int count = coreMailService.getUnreadMailsCount(user);
		JSONObject json = new JSONObject();
		json.put("caption", count);
		if (count != 0) {
			Date lastCheckDate = lastPopTimeMap.get(user);
			UserPrefs prefs = ups.query(user);
			if (isFromWindows(request) && isOpenNotice(prefs) && isTimeOver(lastCheckDate)) { // 如果超过15分钟才做邮件检查
				List<MailInfo> mlist = coreMailService.getUnreadMailList(user);
				FilterResult r = null;
				if (mlist.size() > 0) {
					if (prefs != null) {
						r = filterMailList(mlist, prefs.getFilterRule(), lastCheckDate);
					} else {
						r = filterMailList(mlist, null, lastCheckDate);
					}
				}
				if (r != null && r.newMailCount > 0) {
					popNewEmail(request, user, r.newMailCount, r.latestMail);
					LOG.info("Send a pop notice for " + user);
					lastPopTimeMap.put(user, new Date());
				}
			}
			json.put("refresh", 30 * 1000);
		} else {
			json.put("refresh", 15 * 60 * 1000);
		}
		return json;
	}

	private class FilterResult {
		public int newMailCount = 0;
		public MailInfo latestMail = null;
	}

	private FilterResult filterMailList(List<MailInfo> mlist, String filterRule, Date lastCheckDate) {
		MailInfo latestMail = null;
		List<Pattern> whiteRules = getRulePatterns(filterRule, true);
		List<Pattern> blackRules = getRulePatterns(filterRule, false);
		int tempCount = 0;
		for (MailInfo m : mlist) { // 未读邮件且符合过滤条件的邮件
			if (isUnreadMail(m, lastCheckDate)) {
				if (isMatchFilter(m, blackRules, false)) {
					continue;
				}
				if (isMatchFilter(m, whiteRules, true)) {
					tempCount++;
					if (latestMail == null) { // 获得最新的邮件
						latestMail = m;
					} else {
						if (m.getDateTime().after(latestMail.getDateTime())) {
							latestMail = m;
						}
					}
				}
			}
		}
		FilterResult result = new FilterResult();
		result.newMailCount = tempCount;
		result.latestMail = latestMail;
		return result;
	}

	private boolean isUnreadMail(MailInfo m, Date lastCheckDate) {
		if (lastCheckDate == null) {
			return true;
		}
		return m.getDateTime().getTime() > lastCheckDate.getTime();
	}

	private List<Pattern> getRulePatterns(String filterRule, boolean choseWhiteFlag) {
		if (StringUtils.isEmpty(filterRule)) {
			return null;
		}
		String[] strs = filterRule.split(",");
		List<Pattern> patList = new ArrayList<Pattern>();
		for (int i = 0; i < strs.length; i++) {
			String raw = strs[i].trim().toLowerCase();
			if (choseWhiteFlag) {
				if (!raw.startsWith("!")) {
					patList.add(Pattern.compile(raw.replace("!", "")));
				}
			} else {
				if (raw.startsWith("!")) {
					patList.add(Pattern.compile(raw.replace("!", "")));
				}
			}
		}
		return patList;
	}

	private boolean isMatchFilter(MailInfo m, List<Pattern> rules, boolean isWhiteFilter) {
		if (rules == null || rules.size() == 0) {
			return isWhiteFilter;
		} else {
			String from = m.getFrom();
			for (Pattern rule : rules) {
				Matcher matcher = rule.matcher(from);
				if (matcher.find()) {
					return true;
				}
			}
		}
		return false;
	}

	private boolean isFromWindows(HttpServletRequest request) {
		return "web".equals(request.getParameter("device"));
	}

	private boolean isOpenNotice(UserPrefs p) {
		if (p == null) {
			return true;
		}
		return "1".equals(p.getSwitchNotice());
	}

	private boolean isTimeOver(Date lastCheckDate) {
		boolean result = false;
		if (lastCheckDate == null) { // 第一次检查默认超过
			result = true;
		} else {
			Date now = new Date();
			long interval = Long.parseLong(Config.getProperty("dchat.mail.check.interval"));
			long delta = now.getTime() - (lastCheckDate.getTime() + interval * 1000); // 是否超过15分钟
			result = delta >= 0;
		}
		return result;
	}

	@ResponseBody
	@RequestMapping(value = "/testmessage", method = RequestMethod.GET)
	public JSONObject test(HttpServletRequest request) throws Exception {
		MailInfo m = new MailInfo();
		m.setDateTime(new Date());
		m.setFrom("test");
		m.setSubject("test");
		m.setTo("you");
		IRtpSvc rtp = RtpSvc.getInstance(request);
		rtp.exec("IMSend", "admin", RTPCharUtils.escapeNode("liji@cstnet.cn"), "纯文本消息", "testtesttest", "", "headline");
		this.popNewEmail(request, "liji@cstnet.cn", 10, m);
		JSONObject json = new JSONObject();
		json.put("status", "ok");
		return json;
	}

	private void popNewEmail(HttpServletRequest request, String user, int tempCount, MailInfo latestMail) {
		try {
			IRtpSvc rtp = RtpSvc.getInstance(request);

			String style = "name='Tahoma' size='9' color='#000000' bold='1' underline='1' strikeOut='0' text-align='left'";
			String from = latestMail.getFrom();
			String newFrom = null;
			if (from.indexOf('<') > 0) {
				newFrom = from.substring(from.indexOf('<') + 1, from.indexOf('>'));
			} else {
				newFrom = from;
			}

			String subject = latestMail.getSubject().replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
					.replace("\"", "&quot;").replace("'", "&apos;");
			rtp.exec(
					"IMSend",
					"admin",
					RTPCharUtils.escapeNode(user),
					"您收到" + tempCount + "封新邮件",
					"",
					"<rooyee xmlns='urn:xmpp:rooyee:richtext:v1'>" + "<text " + style + ">" + "最新一封邮件\n" + "From："
							+ newFrom + "\n" + "Subject：" + subject + "</text>" + "</rooyee>"
							+ "<x xmlns='jabber:x:oob'>" + "<url>" + DChatConstants.getDomain()
							+ request.getContextPath() + "/v1/notify/access/coremail?rtplink=dchatRTP" + "</url></x>",
					"headline");
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
		}
	}

	@ResponseBody
	@RequestMapping(value = "/ddlnotice", method = RequestMethod.GET)
	public JSONObject getDDLNoticeCount(@RequestParam("user") String user) {
		JSONObject json = new JSONObject();
		int count = getDdlNoticeCount(user);
		json.put("caption", count);
		if (count != 0) {
			json.put("refresh", 30 * 1000);
		} else {
			json.put("refresh", 15 * 60 * 1000);
		}
		return json;
	}

	private int getDdlNoticeCount(String user) {
		String url = Config.getProperty("ddl.notice.url");
		String jsonstr = RestUtils.requestJson(url + "?uid=" + user);
		if (StringUtils.isEmpty(jsonstr)) {
			LOG.error("Empty message");
			return 0;
		}
		JSONParser parser = new JSONParser();
		JSONObject obj = null;
		try {
			obj = (JSONObject) parser.parse(jsonstr);
			if (obj.get("message") != null) {
				LOG.error("Query ddl notice status error : " + obj.get("message"));
				return 0;
			}
			if (obj.get("count") != null) {
				return Integer.parseInt(obj.get("count") + "");
			}
		} catch (ParseException e) {
			LOG.error(e.getMessage(), e);
		}
		return 0;
	}

	@RequestMapping(value = "/access/vmt", method = RequestMethod.GET)
	public void accessVmtCreateGroup(HttpServletRequest request, HttpServletResponse response) {
		String device = request.getParameter("device");
		if ("android".equals(device) || "ios".equals(device)) { // for android
			accessByConfig(request, response, "dchat.redirect.vmt.mobile", null);
		} else {
			accessByConfig(request, response, "dchat.redirect.vmt", null);
		}
	}

	private void accessByConfig(HttpServletRequest request, HttpServletResponse response, String prop, String params) {
		String user = (String) request.getSession().getAttribute(DChatConstants.CURRENT_USER);
		LOG.info("sessionid:" + request.getSession().getId() + ",currentUser:" + user + ",ip:"
				+ request.getRemoteAddr());
		if (StringUtils.isNotEmpty(user)) {
			if (StringUtils.isEmpty(params)) {
				accessService(user, Config.getProperty(prop), response);
			} else {
				accessService(user, Config.getProperty(prop) + params, response);
			}
		} else {
			LOG.warn("Current session user is missing.");
		}
	}

	@RequestMapping(value = "/access/ddl", method = RequestMethod.GET)
	public void accessDDL(HttpServletRequest request, HttpServletResponse response) {
		accessByConfig(request, response, "dchat.redirect.ddl", null);
	}

	@RequestMapping(value = "/access/coremail", method = RequestMethod.GET)
	public void accessCoreMail(HttpServletRequest request, HttpServletResponse response)
			throws UnsupportedEncodingException {
		String device = request.getParameter("device");
		if ("android".equals(device) || "ios".equals(device)) { // for android
			accessByConfig(request, response, "dchat.redirect.coremail", null);
		} else {
			String template1 = Config.getProperty("dchat.redirect.coremail")
					+ "/coremail/XJS/index.jsp?sid=${sid}&firstShowPage=mbox/search.jsp?search_mode=search%26read=false%26sid=${sid}";
			String params = "?redirectUrl=" + URLEncoder.encode(template1, "UTF-8");
			accessByConfig(request, response, "dchat.redirect.coremail", params);
		}
	}

	@RequestMapping(value = "/access/dhome", method = RequestMethod.GET)
	public void accessDhome(HttpServletRequest request, HttpServletResponse response) {
		accessByConfig(request, response, "dchat.redirect.dhome", null);
	}

	@RequestMapping(value = "/access/cos", method = RequestMethod.GET)
	public void accessCos(HttpServletRequest request, HttpServletResponse response) {
		String device = request.getParameter("device");
		if ("android".equals(device) || "ios".equals(device)) { // for android
			accessByConfig(request, response, "dchat.redirect.iask", null);
		} else {
			accessByConfig(request, response, "dchat.redirect.cos", null);
		}
	}

	private UmtClient umt = UmtClient.getInstance();

	private void accessService(String username, String redirectURL, HttpServletResponse response) {
		LOG.info("User[" + username + "] want to access service at " + redirectURL);
		String accessToken = umt.getLoginToken(username);
		if (!StringUtils.isEmpty(accessToken)) {
			String passportDomain = Config.getProperty("umt.passport.domain");
			String fetchTempTokenURL = passportDomain + "/request/loginToken?action=apply&accessToken=" + accessToken;
			long start = System.currentTimeMillis();
			String tempToken = getTempToken(fetchTempTokenURL);
			long end = System.currentTimeMillis();
			LOG.debug("get temp token use time:" + (end - start) + " ms");
			try {
				String finalURL = passportDomain + "/request/loginToken?action=login&token=" + tempToken
						+ "&returnUrl=" + URLEncoder.encode(redirectURL, "UTF-8");
				response.sendRedirect(finalURL);
			} catch (IOException e) {
				LOG.error(e.getMessage(), e);
			}
		}
	}

	/*
	 * 注意现在这种获取tempToken的方式只支持HTTP协议，https协议不支持。
	 */
	private String getTempToken(String fetchTempTokenURL) {
		try {
			setSSLIgnore();
		} catch (KeyManagementException | NoSuchAlgorithmException e2) {
			e2.printStackTrace();
		}
		String token = null;
		URL url = null;
		try {
			url = new URL(fetchTempTokenURL);
		} catch (MalformedURLException e1) {
			LOG.error(e1.getMessage(), e1);
		}
		InputStream ins = null;
		BufferedReader reader = null;
		try {
			HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
			ins = con.getInputStream();
			reader = new BufferedReader(new InputStreamReader(ins, CHAR_SET));
			String line;
			StringBuffer sb = new StringBuffer();
			while ((line = reader.readLine()) != null) {
				sb.append(line);
			}
			JSONParser parser = new JSONParser();
			JSONObject obj = (JSONObject) parser.parse(sb.toString());
			token = (String) obj.get("token");
		} catch (IOException | ParseException e) {
			LOG.error(e.getMessage(), e);
		} finally {
			IOUtils.closeQuietly(reader);
			IOUtils.closeQuietly(ins);
		}
		return token;
	}
	
	private void setSSLIgnore() throws NoSuchAlgorithmException, KeyManagementException{
		TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
			public java.security.cert.X509Certificate[] getAcceptedIssuers() {
				return null;
			}

			public void checkClientTrusted(X509Certificate[] certs, String authType) {
			}

			public void checkServerTrusted(X509Certificate[] certs, String authType) {
			}
		} };
		// Install the all-trusting trust manager
		SSLContext sc = SSLContext.getInstance("SSL");
		sc.init(null, trustAllCerts, new java.security.SecureRandom());
		HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
		// Create all-trusting host name verifier
		HostnameVerifier allHostsValid = new HostnameVerifier() {
			public boolean verify(String hostname, SSLSession session) {
				return true;
			}
		};
		// Install the all-trusting host verifier
		HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
	}

	@Autowired
	private IRTPUserService rtpUserService;

	@ResponseBody
	@RequestMapping(value = "/publish/notice", method = RequestMethod.POST)
	public JSONObject sendNotification(HttpServletRequest request, @RequestParam("title") String title,
			@RequestParam("content") String content, @RequestParam("sender") String sender) throws Exception {
		String targetURL = request.getParameter("targetURL");
		String style1 = "name='Tahoma' size='9' color='#52687e' bold='1' strikeOut='0' text-align='left'";
		String template = "<rooyee xmlns='urn:xmpp:rooyee:richtext:v1'>" + "<text " + style1 + ">${content}</text>"
				+ "</rooyee>";
		String linkPart = "<x xmlns='jabber:x:oob'><url>${url}</url></x>";
		if (StringUtils.isNotEmpty(targetURL)) {
			template += linkPart;
		}
		String html = template.replace("${content}", "您收到了一封主题为\"" + title + "\"的群发邮件，请登录邮箱查看。");
		if (StringUtils.isNotEmpty(targetURL)) {
			html = html.replace("${url}", targetURL);
		}
		String[] names = request.getParameterValues("names");
		IRtpSvc rtp = RtpSvc.getInstance(request);
		boolean flag = rtpUserService.isUserExist(sender);
		if (flag) {
			for (String name : names) {
				rtp.exec("IMSend", RTPCharUtils.escapeNode(sender), RTPCharUtils.escapeNode(name), "群发邮件通知", "", html, "normal");
				LOG.info(String.format("Send note %s from %s to %s", title, sender, name));
			}
			JSONObject json = new JSONObject();
			json.put("status", "ok");
			return json;
		} else {
			LOG.info("Skip send note as can not find user=" + sender);
			JSONObject json = new JSONObject();
			json.put("status", "skip");
			return json;
		}
	}

}
