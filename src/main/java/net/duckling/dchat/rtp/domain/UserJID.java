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

import net.duckling.dchat.utils.Config;

/**
 * RTP中用户的JID
 * @author Yangxp
 * @since 2012-07-31
 */
public class UserJID extends JID {
	public static final String RTP_SERVER_DOMAIN = Config.getProperty("rtp.server.domain");
	/**
	 * 用户JID的构造函数
	 * @param username 用户名（对应VMT中的vmt-id）
	 * @param resource 资源名（用户使用的客户端）
	 */
	public UserJID(String username, String resource){
		setNode(username);
		setDomain(RTP_SERVER_DOMAIN);
		setResource(resource);
	}
	
}
