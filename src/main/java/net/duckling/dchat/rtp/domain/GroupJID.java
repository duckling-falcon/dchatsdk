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
 * RTP中群组的JID
 * @author Yangxp
 * @since 2012-07-31
 */
public class GroupJID extends JID {
	
	public static final String GROUP_DOMAIN = "groupchat."+Config.getProperty("rtp.server.domain");
	/**
	 * 群JID构造函数
	 * @param groupID 群ID（对应VMT中的vmt-symbol）
	 */
	public GroupJID(String groupID){
		setNode(groupID);
		setDomain(GROUP_DOMAIN);
		setResource(null);
	}
}
