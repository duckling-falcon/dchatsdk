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
package net.duckling.dchat.rtp.domain;

import org.apache.commons.lang.StringUtils;
/**
 * JID : XMPP协议中用来唯一标识用户/群等实体
 * @author Yangxp
 * @since 2013-07-31
 */
public class JID {
	private String node;
	private String domain;
	private String resource;
	
	public String getNode() {
		return node;
	}
	public void setNode(String node) {
		this.node = node;
	}
	public String getDomain() {
		return domain;
	}
	public void setDomain(String domain) {
		this.domain = domain;
	}
	public String getResource() {
		return resource;
	}
	public void setResource(String resource) {
		this.resource = resource;
	}
	
	@Override
	public String toString() {
		String result = node;
		if(node.contains("@")){
			result = node.replace("@", "\\40");
		}
		if(StringUtils.isNotBlank(domain)){
			result += "@"+domain;
		}
		if(StringUtils.isNotBlank(resource)){
			result += "/"+resource;
		}
		return result.toLowerCase();
	}
}
