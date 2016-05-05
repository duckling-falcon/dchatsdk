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
package net.duckling.dchat.utils;

import java.io.File;
import java.io.IOException;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

public class DChatConstants {

	public static final String CURRENT_USER = "currentUser";
	public static String tempDir;
	public static String webRootDir;
	static {
		Resource res = new ClassPathResource("/");
		try {
			tempDir = res.getFile().getPath() + File.separator + "temp" + File.separator;
			webRootDir = res.getFile().getParent() + File.separator + ".." + File.separator;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static String getWebid() {
		return Config.getProperty("dchat.sdk.webid");
	}

	public static String getDomain() {
		return Config.getProperty("dchat.domain");
	}

	public static String getRtpDomain() {
		return Config.getProperty("rtp.server.domain");
	}

	public static String getDchatPortalURL() {
		return Config.getProperty("dchat.portal.url");
	}

	public static String getPassportURL() {
		return Config.getProperty("umt.passport.domain");
	}

	public static String getTempDirPath() {
		File file = new File(tempDir);
		if (!file.exists()) {
			file.mkdirs();
		}
		return tempDir;
	}

	public static String getVmtDomain() {
		return Config.getProperty("vmt.server.domain");
	}

	public static String getProfile() {
		String rtpDomain = DChatConstants.getRtpDomain();
		String profile = null;
		if ("rtp.escience.cn".equalsIgnoreCase(rtpDomain.trim())) {
			profile = "dev";
		} else if ("dchattest.escience.cn".equalsIgnoreCase(rtpDomain.trim())) {
			profile = "test";
		} else {
			profile = "product";
		}
		return profile;
	}
	
	public static String getWebRootDir(){
		 return webRootDir;
	}

}
