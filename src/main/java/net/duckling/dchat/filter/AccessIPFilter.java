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
package net.duckling.dchat.filter;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpStatus;
import org.apache.log4j.Logger;

/**
 * 旨在对Rest接口中超级管理功能（url中包含sa）的访问权限做IP上的限制.
 * 
 * @author Yangxp
 * @since 2012-06-18
 */
public class AccessIPFilter implements Filter {

	private static final Logger LOG = Logger.getLogger(AccessIPFilter.class);
	private static final String SUPER_ADMIN_URL_PATTERN = "[http|https]://[0-9a-zA-Z\\.\\-_/:]+/v1/sa/.*+";
	private static final String SUPER_CONFIG_URL_PATTERN = "[http|https]://[0-9a-zA-Z\\.\\-_/:]+/v1/embed/config";
	private static final String SUPER_REST_URL_PATTERN = "[http|https]://[0-9a-zA-Z\\.\\-_/:]+/rest/(ec|api)/.*+";

	private List<Pattern> pats = new ArrayList<Pattern>();
	private Set<String> allowIPs = new HashSet<String>();

	@Override
	public void destroy() {
		allowIPs.clear();
		allowIPs = null;
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
			ServletException {
		HttpServletRequest req = (HttpServletRequest) request;
		HttpServletResponse resp = (HttpServletResponse) response;
		String url = req.getRequestURL().toString();
		String remoteAddr = req.getRemoteAddr();
		if (isSuperAdminURL(url) && !allowIPs.contains(remoteAddr)) {
			resp.setStatus(HttpStatus.SC_FORBIDDEN);
		} else {
			chain.doFilter(req, resp);
		}
	}

	@Override
	public void init(FilterConfig config) throws ServletException {
		addLocalIP();

		pats.add(Pattern.compile(SUPER_ADMIN_URL_PATTERN));
		pats.add(Pattern.compile(SUPER_CONFIG_URL_PATTERN));
		pats.add(Pattern.compile(SUPER_REST_URL_PATTERN));

		String appRootPath = System.getProperty("webapp.root");
		String configFile = config.getInitParameter("ipConfigFile");
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(appRootPath + configFile)));
			String line = null;
			while (null != (line = reader.readLine())) {
				allowIPs.add(line.trim());
			}
		} catch (FileNotFoundException e) {
			LOG.error("Initial Access IPs Failed! ", e);
		} catch (IOException e) {
			LOG.error("Initial Access IPs Failed! ", e);
		} finally {
			if (null != reader) {
				try {
					reader.close();
				} catch (IOException e) {
					LOG.error(e);
				}
			}
		}
	}

	private boolean isSuperAdminURL(String uri) {
		if (StringUtils.isNotBlank(uri)) {
			for (Pattern pat : pats) {
				Matcher matcher = pat.matcher(uri);
				if (matcher.find()) {
					return true;
				}
			}
		}
		return false;
	}

	private void addLocalIP() {
		allowIPs.add("127.0.0.1");
		allowIPs.add("localhost");
		try {
			InetAddress addr = InetAddress.getLocalHost();
			allowIPs.add(addr.getHostAddress());
		} catch (UnknownHostException e) {
			LOG.error("Add Localhost Failed! " + e.getMessage());
		}
	}

}
