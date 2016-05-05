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
package net.duckling.dchat.umt;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.security.cert.X509Certificate;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import net.duckling.dchat.utils.Config;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
/**
 * UMT客户端，实现UMT认证
 * @author Yangxp
 * @since 2012-06-14
 */
public class UmtClient {
	
	private static final String UMT_SERVER_URL = "umt.server.url";
	private static final String REDIRECT_URI = "umt.dchat.redirecturi";
	private static final String DEFAULT_CONTENT_CHARSET = "UTF-8";
	private static final Logger LOG = Logger.getLogger(UmtClient.class);
	private String oauthUrl;  
	private String redirectUri;
	
	private Map<String,String> embedTokenMap = new ConcurrentHashMap<String,String>();
	
	private Map<String,String> loginTokenMap = new ConcurrentHashMap<String,String>();
	private Map<String,String> umtIdMap = new ConcurrentHashMap<String,String>();
	private Set<String> loginDomains = new HashSet<String>();
	private Set<String> coremailSet = new HashSet<String>(); //只加不减，因此线程不安全应该也不要紧
	
	
	public boolean consumeEmbedToken(String user, String embedToken){
		String token = this.embedTokenMap.get(user);
		if(StringUtils.equals(token, embedToken)){
			//this.embedTokenMap.remove(user);
			return true;
		}
		return false;
	}
	
	public void produceEmbedToken(String user, String token){
		this.embedTokenMap.put(user, token);
	}
	
	/**
	 * 如果配置项中dchat.login.enable.domains没有配置，则所有用户均可登录，否则只有以配置的域名结尾的用户方可登录
	 * @param username
	 * @return
	 */
	public boolean isCstnetUser(String username){
		if(StringUtils.isNotBlank(username)){
			initDomainSet();
			if(loginDomains.isEmpty()){
				return true;
			}else{
				Iterator<String> itr = loginDomains.iterator();
				while(itr.hasNext()){
					if(username.endsWith(itr.next())){
						return true;
					}
				}
			}
		}
		return false;
	}
	
	public void initDomainSet(){
		if(loginDomains.isEmpty()){
			String loginDomainsStr = Config.getProperty("dchat.login.enable.domains");
			if(StringUtils.isNotBlank(loginDomainsStr)){
				String[] domains = loginDomainsStr.split(",");
				for(String domain : domains){
					if(StringUtils.isNotBlank(domain)){
						this.loginDomains.add(domain);
					}
				}
			}
		}
	}
	
	private static class SingletonHolder{
		private static UmtClient instance = new UmtClient();
	}
	
	private UmtClient(){
		oauthUrl = Config.getProperty(UMT_SERVER_URL);
		redirectUri = Config.getProperty(REDIRECT_URI);
	}
	
	public static UmtClient getInstance(){
		return SingletonHolder.instance;
	}
	/**
	 * 去UMT登录
	 * @param username
	 * @param password
	 * @return
	 */
	public boolean login(String username, String password){
		if(StringUtils.isBlank(username) || StringUtils.isBlank(password)){
			return false;
		}
		String responseBody = auth2UMTBySSL(username, password);
		JSONObject json = getJSONObject(responseBody);
		String infoStr = (String)json.get("userInfo");
		JSONObject info = getJSONObject(infoStr);
		if(null != info){
			String type = (String)info.get("type");
			String umtid  = (String)info.get("umtId");
			umtIdMap.put(username, umtid);
			if("coremail".equalsIgnoreCase(type) || "uc".equalsIgnoreCase(type)){
				coremailSet.add(username);
			}
		}
		if(null != json && null != json.get("error")){
			return false;
		}
		loginTokenMap.put(username, json.get("access_token").toString());
		return true;
	}
	
	public String getUmtId(String username){
		return umtIdMap.get(username);
	}
	
	public String getLoginToken(String username){
		return loginTokenMap.get(username);
	}
	
	public boolean isCoreMailUser(String username){
		return coremailSet.contains(username);
	}

	private JSONObject getJSONObject(String body) {
		JSONObject result = null;
		if(StringUtils.isNotBlank(body)){
			try {
				JSONParser parser = new JSONParser();
				result = (JSONObject)parser.parse(body);
			} catch (ParseException e) {
				LOG.error("resolve json object failed from umt login response body!", e);
			}
		}
		return result;
	}
	
	public void setUmtToken(String username, String token){
		this.loginTokenMap.put(username, token);
	}

	private String auth2UMTBySSL(String username, String password) {
		TrustManager easyTrustManager = new MyX509TrustManager();
		TrustManager[] tm = {easyTrustManager};
		
		try{
			SSLContext sslContext = SSLContext.getInstance("SSL", "SunJSSE");
			sslContext.init(null, tm, new java.security.SecureRandom());
			SSLSocketFactory ssf = sslContext.getSocketFactory();
			URL url = new URL(oauthUrl);
			URLConnection conn = url.openConnection();
			if(conn instanceof HttpsURLConnection){
				HttpsURLConnection httpsURLConn = (HttpsURLConnection)conn;
				httpsURLConn.setHostnameVerifier(new javax.net.ssl.HostnameVerifier() {
					public boolean verify(String hostname, javax.net.ssl.SSLSession sslSession) {
						return true;
					}
				});
				httpsURLConn.setSSLSocketFactory(ssf);
				httpsURLConn.setRequestMethod("POST");
				httpsURLConn.setDoOutput(true);
				httpsURLConn.addRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");
				OutputStream stream = httpsURLConn.getOutputStream();
				String up = "username="+URLEncoder.encode(username,"utf-8")+"&password="+URLEncoder.encode(password,"utf-8")
						+ "&redirect_uri="+URLEncoder.encode(redirectUri,"utf-8");
				stream.write(up.getBytes("UTF-8"));
				httpsURLConn.connect();
				InputStream ins = null;
				int responseCode = httpsURLConn.getResponseCode();
				if(responseCode == 400){
					ins = httpsURLConn.getErrorStream();
				}else{
					ins = httpsURLConn.getInputStream();
				}
				return getStringFromStream(ins, DEFAULT_CONTENT_CHARSET);
			}
		}catch(Exception e){
			LOG.error("authentication failed!", e);
		}
		return null;
	}
	
	public void removeLoginCache(String username) {
		loginTokenMap.remove(username);
	}
	
	private String getStringFromStream(InputStream is, String defaultCharset) throws IOException{
		if (is == null) {
            throw new IllegalArgumentException("InputStream may not be null");
        }

        String charset = defaultCharset;
        Reader reader = new InputStreamReader(is, charset);
        StringBuilder sb = new StringBuilder();
        int l;
        try {
            char[] tmp = new char[4096];
            while ((l = reader.read(tmp)) != -1) {
                sb.append(tmp, 0, l);
            }
        } finally {
            reader.close();
        }
        return sb.toString();
	}
	
	private static class MyX509TrustManager implements X509TrustManager{
		@Override
		public void checkClientTrusted(X509Certificate[] arg0,
				String arg1) {
			// empty implement
		}

		@Override
		public void checkServerTrusted(X509Certificate[] arg0,
				String arg1) {
			// empty implement
		}

		@Override
		public X509Certificate[] getAcceptedIssuers() {
			// empty implement
			return null;
		}
	}

}
