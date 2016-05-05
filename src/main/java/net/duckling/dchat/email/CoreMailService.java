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
import java.net.Socket;
import java.util.List;

import net.duckling.dchat.utils.Config;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import tebie.applib.api.APIContext;
import tebie.applib.api.IClient;

@Service
public class CoreMailService {

	private static final Logger LOG = Logger.getLogger(CoreMailService.class);

	/**
	 * API声明说务必保证一个线程一个client，用完就关闭
	 * */
	private IClient getCoreMailClient() throws IOException {
		String server = Config.getProperty("coremail.sdk.server");
		int port = Integer.parseInt(Config.getProperty("coremail.sdk.port"));
		Socket socket = new Socket(server, port);
		return APIContext.getClient(socket);
	}

	private void closeClient(IClient client) {
		if (client != null) {
			try {
				client.close();
			} catch (IOException e) {
				LOG.error(e.getMessage(), e);
			}
		}
	}

	public int getUnreadMailsCount(String user) {
		int result = 0;
		IClient client = null;
		try {
			client = getCoreMailClient();
			int[] counts = getCountByType(user, client, "mbox_newmsgcnt=&mbox.folder.2.newmsgcnt=&mbox.folder.3.newmsgcnt=" +
					"&mbox.folder.4.newmsgcnt=&mbox.folder.5.newmsgcnt=&mbox.folder.6.newmsgcnt=");
			if(counts == null){
				return -1;
			}
			result = counts[0];
			for(int i=1;i< counts.length;i++){
				result -= counts[i];
			}
		} catch (IOException e) {
			LOG.error(e.getMessage(), e);
		} finally {
			closeClient(client);
		}
		return result;
	}

	private int[] getCountByType(String user, IClient client, String type) throws IOException {
		APIContext context = client.getAttrs(user, type);
		String str = context.getResult();
		if(str == null){
			return null;
		}
		String[] array = str.split("&");
		int[] result = new int[array.length];
		int i = 0;
		for(String a:array){
			if (a != null && !a.isEmpty()) {
				result[i++] = Integer.parseInt(a.substring(a.lastIndexOf('=') + 1));
			}
		}
		return result;
	}
	
	public List<MailInfo> getUnreadMailList(String user){
		IClient client = null;
		try {
			client = getCoreMailClient();
			APIContext context = client.getNewMailInfos(user, "limit=99&format=xml");
			String str = context.getResult();
			String prefix = "<?xml version=\"1.0\"?>";
			return MailInfo.buildFromXML(prefix+str);
		} catch (IOException e) {
			LOG.error(e.getMessage(), e);
		} finally {
			closeClient(client);
		}
		return null;
	}

	public static void main(String[] args) {
		System.setProperty("coremail.sdk.server", "159.226.14.143");
		System.setProperty("coremail.sdk.port", "6195");
		CoreMailService core = new CoreMailService();
		int count = core.getUnreadMailsCount("liji@cstnet.cn");
		System.out.println(count);
		List<MailInfo> mlist = core.getUnreadMailList("liji@cstnet.cn");
		for(MailInfo m:mlist){
			System.out.println(m);
		}
	}

	public String getUserSessionID(String to) {
		IClient client = null;
		try {
			client = getCoreMailClient();	
			APIContext context = client.userLoginEx(to, "face=XJS");
			String raw = context.getResult();
			if(StringUtils.isEmpty(raw)){
				return null;
			}
			return raw.substring(raw.indexOf('=')+1, raw.indexOf('&'));
		} catch (IOException e) {
			LOG.error(e.getMessage(), e);
		} finally {
			closeClient(client);
		}
		return null;
	}
	
	public boolean isUserExist(String user){
		IClient client = null;
		try{
			client = getCoreMailClient();
			APIContext context = client.userExist(user);
			if(APIContext.RC_NORMAL == context.getRetCode()){
				return true;
			}
			return false;
		} catch(IOException e){
			LOG.error(e.getMessage(), e);
		} finally{
			closeClient(client);
		}
		return false;
	}
}
