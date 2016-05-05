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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;
/**
 * RTP中角色的权限
 * @author Yangxp
 *
 */
public class RTPRoleRight {
	public static final String RIGHT_ORGANIZATION = "0";
	public static final String RIGHT_FILE_TRANSFER = "1";
	public static final String RIGHT_AUDIO = "2";
	public static final String RIGHT_VEDIO = "3";
	public static final String RIGHT_REMOTE_HELP = "4";
	public static final String RIGHT_PRIVATE_FRIEND = "5";
	public static final String RIGHT_MODIFY_PERSONAL_INFO = "6";
	public static final String RIGHT_JOIN_GROUP = "8";
	
	private static List<Integer> rights = new ArrayList<Integer>(Arrays.asList(0,1,2,3,4,5,6,8));
	
	private RTPRoleRight(){}
	/**
	 * 判断权限值是否合法
	 * @param right 权限值
	 * @return
	 */
	public static boolean isValidRight(String right){
		boolean valid = false;
		if(StringUtils.isNotBlank(right)){
			try{
				int temp = Integer.valueOf(right);
				return rights.contains(Integer.valueOf(temp));
			}catch(NumberFormatException e){
				valid = false;
			}
		}
		return valid;
	}
	
	/**
	 * 获取所有合法的角色权限
	 * @return
	 */
	public static List<Integer> getAllMemberType(){
		List<Integer> result = new ArrayList<Integer>();
		Collections.copy(rights, result);
		return result;
	}
}
