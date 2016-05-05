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
package net.duckling.dchat.vmt;

/**
 * RTP服务器内的部门对象
 * @author Yangxp
 * @since 2013-08-11
 */
public class RTPVmtDepart {
	
	public static final String VMT_CURRENT_DISPLAY = "vmt-current-display";
	public static final String VMT_LIST_RANK = "vmt-list-rank";
	
	public static final String RTP_DEPT_PATH = "dept";
	public static final String RTP_SORT_WEIGHTS = "sortweights";
	
	private String deptPath;
	private int sortWeights;
	
	public String getDeptPath() {
		return deptPath;
	}
	public void setDeptPath(String deptPath) {
		this.deptPath = deptPath;
	}
	public int getSortWeights() {
		return sortWeights;
	}
	public void setSortWeights(int sortWeights) {
		this.sortWeights = sortWeights;
	}
	
	@Override
	public int hashCode() {
		return deptPath.hashCode();
	}
	@Override
	public boolean equals(Object obj) {
		if(null == obj){
			return false;
		}
		if(!(obj instanceof RTPVmtDepart)){
			return false;
		}
		RTPVmtDepart temp = (RTPVmtDepart)obj;
		return deptPath.equals(temp.getDeptPath());
	}
	@Override
	public String toString() {
		return "[Dept : "+deptPath+", "+sortWeights+"]";
	}
	
}
