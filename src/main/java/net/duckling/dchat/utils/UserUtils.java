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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
/**
 * 用户工具类
 * @author Yangxp
 * @since 2012-06-14
 */
public class UserUtils {
	private static final String DEPT_NAME = "[^a-zA-Z0-9\\-_\\.!~\\*'()\\/\\u4e00-\\u9fa5]+";
	
	private static final Pattern deptPat = Pattern.compile(DEPT_NAME);
	
	private UserUtils(){}
	/**
	 * 用来转义cstnetID中的@符号，现已作废，直接原样返回
	 * @param cstnetId 用户名
	 * @return 用户名
	 */
	public static String escapeCstnetId(String cstnetId){
		return cstnetId;
	}
	/**
	 * 用来将\\40替换成@符号，现已作废，直接原样返回
	 * @param cstnetId 用户名
	 * @return 用户名
	 */
	public static String unescapeCstnetId(String cstnetId){
		return cstnetId;
	}
	/**
	 * 将VMT中的currentDisplay属性（以逗号分隔），转换成RTP接受的部门属性（以斜线分隔）
	 * @param dept VMT中的CurrentDisplay属性
	 * @return RTP接受的用户的部门属性
	 */
	public static String formatDept(String dept){
		if(null == dept){
			return null;
		}
		String result = replace(dept, ",", "/");
		Matcher matcher = deptPat.matcher(result);
		while(matcher.find()){
			result = result.replace(matcher.group(), "");
		}
		return result;
	}
	
	public static String fetchOrgName(String depts){
		if(null == depts){
			return null;
		}
		String[] array = depts.split(","); //分隔多个组织
		StringBuilder sb = new StringBuilder();
		for(String d:array){ //提取每个组织的第一级目录
			int i = d.indexOf('/');
			if(i > 0){  
				sb.append(d.substring(0, i));
			} else {
				sb.append(d);
			}
			sb.append(",");
		}
		return sb.substring(0, sb.length()-1);
	}
	
	public static String fetchDeptName(String depts){
		if(null == depts){
			return null;
		}
		String[] array = depts.split(",");
		if(array.length > 1){ //多个组织时直接返回原串
			return depts;
		} else if(array.length == 1) { //单个组织时，提取第一级目录后面部分
			StringBuilder sb = new StringBuilder();
			String d = array[0];
			int i = d.indexOf('/');
			if(i > 0){  
				sb.append(d.substring(i+1, d.length()));
			} else {
				sb.append(""); //没有第二级目录
			}
			return sb.toString();
		}
		return null;
	}
	
	private static String replace(String src, String oldStr, String newStr){
		String email = src;
		if(StringUtils.isNotBlank(src)){
			email = email.replace(oldStr, newStr);
		}
		return email;
	}
}
