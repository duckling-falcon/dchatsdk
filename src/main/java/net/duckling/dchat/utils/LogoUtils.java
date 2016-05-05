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

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

public class LogoUtils {

	private static final Logger LOG = Logger.getLogger(LogoUtils.class);

	public static boolean fetchRemoteLogoFile(String httpUrl, String saveFile) {
		String tempURL = httpUrl;
		if (StringUtils.isNotEmpty(httpUrl)) {
			if (httpUrl.startsWith("http://")) {
				tempURL = httpUrl;
			} else if (httpUrl.startsWith("https://")) {
				return downloadFromHttps(httpUrl, saveFile);
			} else {
				tempURL = DChatConstants.getVmtDomain() + "/" + httpUrl;
			}
		} else {
			return false;
		}
		return downloadFromHttp(tempURL,saveFile);
	}

	private static boolean downloadFromHttp(String httpURL,String saveFile) {
		int byteread = 0;
		URLConnection conn = null;
		InputStream inStream = null;
		FileOutputStream fs = null;
		try {
			URL url = new URL(httpURL);
			conn = url.openConnection();
			inStream = conn.getInputStream();
			fs = new FileOutputStream(saveFile);
			byte[] buffer = new byte[1204];
			while ((byteread = inStream.read(buffer)) != -1) {
				fs.write(buffer, 0, byteread);
			}
			return true;
		} catch (IOException e) {
			LOG.error(e.getMessage(), e);
			return false;
		} finally {
			IOUtils.closeQuietly(inStream);
			IOUtils.closeQuietly(fs);
		}
	}

	private static boolean downloadFromHttps(String httpsURL, String saveFile) {
		int byteread = 0;
		TrustManager easyTrustManager = new MyX509TrustManager();
		TrustManager[] tm = { easyTrustManager };
		InputStream inStream = null;
		FileOutputStream fs = null;
		try {
			SSLContext sslContext = SSLContext.getInstance("SSL", "SunJSSE");
			sslContext.init(null, tm, new java.security.SecureRandom());
			SSLSocketFactory ssf = sslContext.getSocketFactory();
			URL url = new URL(httpsURL);
			URLConnection conn = url.openConnection();
			if (conn instanceof HttpsURLConnection) {
				HttpsURLConnection httpsURLConn = (HttpsURLConnection) conn;
				httpsURLConn.setHostnameVerifier(new javax.net.ssl.HostnameVerifier() {
					public boolean verify(String hostname, javax.net.ssl.SSLSession sslSession) {
						return true;
					}
				});
				httpsURLConn.setSSLSocketFactory(ssf);
				httpsURLConn.setRequestMethod("GET");
				httpsURLConn.setDoOutput(true);
				httpsURLConn.connect();
				int responseCode = httpsURLConn.getResponseCode();
				inStream = httpsURLConn.getInputStream();
				if (responseCode == 400) {
					inStream = httpsURLConn.getErrorStream();
					return false;
				}
				fs = new FileOutputStream(saveFile);
				byte[] buffer = new byte[1204];
				while ((byteread = inStream.read(buffer)) != -1) {
					fs.write(buffer, 0, byteread);
				}
				return true;
			}
		} catch (Exception e) {
			LOG.error("download logo failed!", e);
		}finally {
			IOUtils.closeQuietly(inStream);
			IOUtils.closeQuietly(fs);
		}
		
		return false;
	}

	private static class MyX509TrustManager implements X509TrustManager {

		@Override
		public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType)
				throws CertificateException {
			// TODO Auto-generated method stub

		}

		@Override
		public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType)
				throws CertificateException {
			// TODO Auto-generated method stub

		}

		@Override
		public X509Certificate[] getAcceptedIssuers() {
			// TODO Auto-generated method stub
			return null;
		}
	}
	
	public static void main(String[] args) {
		fetchRemoteLogoFile("https://passport.escience.cn/logo?logoId=145&size=small","D:\\tmp\\1.png");
		fetchRemoteLogoFile("http://passport.escience.cn/logo?logoId=145&size=small","D:\\tmp\\2.png");
		fetchRemoteLogoFile("logo/147","D:\\tmp\\3.png");
	}
}
