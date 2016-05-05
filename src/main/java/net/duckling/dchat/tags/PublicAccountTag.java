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
package net.duckling.dchat.tags;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

import net.duckling.dchat.utils.DChatConstants;
import net.duckling.dchat.utils.RTPCharUtils;

import org.apache.log4j.Logger;

import com.rooyeetone.rtp.sdk.IRtpSvc;
import com.rooyeetone.rtp.sdk.RtpSvc;

public class PublicAccountTag extends TagSupport {
	private static final long serialVersionUID = 1L;
	private static final Logger LOG = Logger.getLogger(PublicAccountTag.class);
	private String account;
	private String params;
	private boolean outside;
	private boolean autoclick;

	public void setAutoclick(boolean autoclick) {
		this.autoclick = autoclick;
	}

	public void setOutside(boolean outside) {
		this.outside = outside;
	}

	public void setParams(String params) {
		this.params = params;
	}

	public void setAccount(String account) {
		this.account = account;
	}

	public static final String DEFAULT_IMAGE = "<img class=\"rtp_state_img\" onload=\"rtp_show_state(this)\" border=0 src=\"/dchat/rtpsvc?rtpact=getres&type=pic&name=rtpstate.gif\"/>";
	public static final String DOWNLOAD_URL = "rtp_downurl=\"/dchat/rtp_download.html\"";
	
	public int doEndTag() throws JspException {
		long start = System.currentTimeMillis();
		HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();
		String formatAccount = account;
		try {
			IRtpSvc rtpsvc = RtpSvc.getInstance(request);
			if (outside) { //非cstnet.cn的帐号
				String username = (String) request.getSession().getAttribute(DChatConstants.CURRENT_USER);
				try{
					 rtpsvc.exec("Roster.AddFriend", RTPCharUtils.escapeNode(username), RTPCharUtils.escapeNode(formatAccount));
				}catch(Exception e){
					LOG.error(e.getMessage());
				}
			} else {
				String rtpDomain = DChatConstants.getRtpDomain();
				formatAccount = String.format("%s@%s", account.replace("@", "\\40"), rtpDomain);
			}
			String html = rtpsvc.exec("getTalkButton", formatAccount, params, "border=0");
			if (outside) {
				html = html.replace(DEFAULT_IMAGE, "<img class='rtp_state_img'"
						+ " border='0' src='/dchat/rtpsvc?rtpact=getres&amp;type=pic&amp;name=rtpstate.gif' "
						+ "id='rtp_state_online'>");
			}
			if(autoclick){
				html = html.replace(DOWNLOAD_URL, "rtp_downurl='https://dchat.escience.cn/client/dChatSetup_last.exe' autoclick='true'");
			}else{
				html = html.replace(DOWNLOAD_URL, "rtp_downurl='https://dchat.escience.cn/client/dChatSetup_last.exe'");
			}
			pageContext.getOut().print(html);
			long end = System.currentTimeMillis();
			LOG.info(String.format("public account tag use time: %d ms", (end - start)));
			return EVAL_PAGE;
		} catch (Exception e) {
			throw new JspException(e);
		}
	}
}
