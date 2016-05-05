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
package net.duckling.dchat.rtp;


import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.text.MessageFormat;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;

public abstract class BaseRestClient {

	private String webId;
	private String server;
	private int port;

	public BaseRestClient(String webId, String server, int port) {
		super();
		this.webId = webId;
		this.server = server;
		this.port = port;
	}

	protected String getUrl(String act, String params) {
		String url = MessageFormat.format(
				"http://{0}:{1}/{2}.act?rtpwebid={3}&{4}", server, String.valueOf(port), act,
				webId, params);
		return url;
	}

	public static String send(String url, String requestBody) throws Exception {
		HttpParams params = new BasicHttpParams();
		ConnManagerParams.setMaxTotalConnections(params, 200);
		HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
		SchemeRegistry schemeRegistry = new SchemeRegistry();
		schemeRegistry.register(new Scheme("http", PlainSocketFactory
				.getSocketFactory(), 80));

		ClientConnectionManager cm = new ThreadSafeClientConnManager(params,
				schemeRegistry);
		DefaultHttpClient httpClient = new DefaultHttpClient(cm, params);
		HttpPost httRequest = new HttpPost(url);
		if (requestBody != null) {
			httRequest.setEntity(new StringEntity(requestBody, "UTF-8"));
		}
		HttpResponse response = httpClient.execute(httRequest);
		if (response.getStatusLine().getStatusCode() != 200) {
			System.err.println(response.getStatusLine().toString());
		}
		HttpEntity entity = response.getEntity();
		BufferedReader bufferedReader = new BufferedReader(
				new InputStreamReader(entity.getContent(), "UTF-8"));
		StringBuffer result = new StringBuffer();
		String line = "";
		while ((line = bufferedReader.readLine()) != null) {
			result.append(line);
		}
		httRequest.abort();
		return result.toString();
	}

}