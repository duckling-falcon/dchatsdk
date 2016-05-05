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
 * 组织未找到异常
 * @author Yangxp
 * @since 2013-08-06
 */
public class RTPDeptNotFoundException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1956628857961517566L;
	private String deptPath;
	
	public RTPDeptNotFoundException(String deptPath){
		this.deptPath = deptPath;
	}

	@Override
	public String getMessage() {
		return " Department "+deptPath+" not found! "+ super.getMessage();
	}
}
