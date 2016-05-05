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

import java.io.IOException;
import java.io.StringReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

public class MailInfo {
	private String mid;
	private String from;
	private Date dateTime;
	private String to;
	private String subject;
	public String getMid() {
		return mid;
	}
	public void setMid(String mid) {
		this.mid = mid;
	}
	public String getFrom() {
		return from;
	}
	public void setFrom(String from) {
		this.from = from;
	}
	public Date getDateTime() {
		return dateTime;
	}
	public void setDateTime(Date dateTime) {
		this.dateTime = dateTime;
	}
	public String getTo() {
		return to;
	}
	public void setTo(String to) {
		this.to = to;
	}
	public String getSubject() {
		return subject;
	}
	public void setSubject(String subject) {
		this.subject = subject;
	}
	
	private static final Logger LOG = Logger.getLogger(MailInfo.class);
	
	/*
	   <root>
	  		<mail>
	  			<mid>1tbiAQAFC1DtInIABwAEsh</mid>
				<msid>1</msid>
				<fid>1</fid>
				<flag>24</flag>
				<from>"李觊" &lt;liji@cstnet.cn&gt;</from>
				<to>liji@cstnet.cn</to>
				<subject>liji</subject>
				<size>1150</size>
				<date>2014-02-13 15:49:51</date>
			</mail>
		</root>
	 */
	@SuppressWarnings("rawtypes")
	public static List<MailInfo> buildFromXML(String content) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		SAXBuilder builder = new SAXBuilder(false);
		StringReader read = new StringReader(content); 
		List<MailInfo> mlist = new ArrayList<MailInfo>();
		try {
			Document document = builder.build(read);
			Element rootNode = document.getRootElement();
			List list = rootNode.getChildren("mail");
			for (int i = 0; i < list.size(); i++) {
				MailInfo result = new MailInfo();
				Element node = (Element) list.get(i);
				result.setMid(node.getChildText("mid"));
				result.setSubject(node.getChildText("subject"));
				result.setFrom(node.getChildText("from"));
				result.setTo(node.getChildText("to"));
				result.setDateTime(sdf.parse(node.getChildText("date")));
				mlist.add(result);
			}
		} catch (IOException e) {
			LOG.error(e.getMessage(),e);
		} catch (JDOMException e) {
			LOG.error("JDOMException XML Content as follow:"+content);
			//LOG.error(e.getMessage(),e);
		} catch (ParseException e) {
			LOG.error("ParseException XML Content as follow:"+content);
			//LOG.error(e.getMessage(),e);
		}
		return mlist;
	}
	
	public String toString(){
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		StringBuilder sb = new StringBuilder();
		sb.append("mid:"+this.mid+"\n");
		sb.append("from:"+this.from+"\n");
		sb.append("to:"+this.to+"\n");
		sb.append("subject:"+this.subject+"\n");
		sb.append("date:"+sdf.format(this.dateTime)+"\n");
		return sb.toString();
	}
}
