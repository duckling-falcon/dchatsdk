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
package net.duckling.dchat.rtp.service;

import net.duckling.dchat.utils.RTPCharUtils;

import org.apache.log4j.Logger;

import com.rooyeetone.rtp.sdk.IRtpSvc;
import com.rooyeetone.rtp.sdk.RtpSvc;
/**
 * RTP服务的基础类，提供rtp实例
 * 
 * @author Yangxp
 * @since 2013-08-02
 */
public abstract class BaseRTPService {

	private static final Logger LOG = Logger.getLogger(BaseRTPService.class);
	protected IRtpSvc rtp = null;

	public BaseRTPService() {
		try {
			rtp = RtpSvc.getInstance(null);
		} catch (Exception e) {
			LOG.error(
					"Get RTPSvc instance failed, all rtp service cannot be used!",
					e);
		}
	}

	protected String escapeNode(String node) {
		return RTPCharUtils.escapeNode(node);
	}
}