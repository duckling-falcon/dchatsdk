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
 * RTP中的角色所指向的目标对象
 * @author Yangxp
 * @since 2013-08-12
 */
public class RTPRoleTargetType {
	public static final String TYPE_USER = "0";
	public static final String TYPE_ORGANIZATION_ONE_LEVEL = "1";
	public static final String TYPE_ALLUSER = "3";
	public static final String TYPE_DEPARTMENT = "7";
	public static final String TYPE_ORGANIZATION_ALL = "9";
	public static final String TYPE_CUSTOM = "10";
	
	private static List<Integer> types = new ArrayList<Integer>(Arrays.asList(0,1,3,7,9,10));
	
	private RTPRoleTargetType(){}
	
	/**
	 * 判断所给目标对象是否合法
	 * @param memberType
	 * @return
	 */
	public static boolean isValidMemberType(String memberType){
		boolean valid = false;
		if(StringUtils.isNotBlank(memberType)){
			try{
				int temp = Integer.valueOf(memberType);
				return types.contains(Integer.valueOf(temp));
			}catch(NumberFormatException e){
				valid = false;
			}
		}
		return valid;
	}
	/**
	 * 获取所有合法的目标对象列表
	 * @return
	 */
	public static List<Integer> getAllMemberType(){
		List<Integer> result = new ArrayList<Integer>();
		Collections.copy(types, result);
		return result;
	}
}
