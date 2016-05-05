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
package net.duckling.dchat.exception;
/**
 * RTP系统中未找到用户的异常
 * @author Yangxp
 * @since 2013-08-06
 */
public class RTPUserNotFoundException extends Exception{

	/**
	 * 序列化ID
	 */
	private static final long serialVersionUID = -2671627807058849817L;
	private String username;
	/**
	 * 构造函数
	 * @param username 用户名
	 */
	public RTPUserNotFoundException(String username){
		super("User "+username+" not found ");
		this.username = username;
	}
	/**
	 * 获取用户名
	 * @return
	 */
	public String getUsername(){
		return username;
	}
	
}
