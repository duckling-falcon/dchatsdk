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
package net.duckling.dchat.rtp.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import net.duckling.dchat.rtp.domain.RtpGroup;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class RtpGroupDAO {
	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	private RowMapper<RtpGroup> groupRowMapper =  new RowMapper<RtpGroup>(){
		@Override
        public RtpGroup mapRow(ResultSet rs, int index) throws SQLException {
            RtpGroup g = new RtpGroup();
            g.setGroupID(rs.getInt("groupID"));
            g.setGroupName(rs.getString("groupName"));
            g.setParentID(rs.getInt("parentID"));
            g.setSource(rs.getString("source"));
            return g;
        }
	};
	
	public Map<Integer,RtpGroup> getRtpGroupMap(){
		String sql = "select * from rtpgroup";
		List<RtpGroup> RtpGroupList = jdbcTemplate.query(sql, new Object[]{},groupRowMapper);
		Map<Integer,RtpGroup> cacheMap = new HashMap<Integer,RtpGroup>();
		for(RtpGroup g:RtpGroupList){
			cacheMap.put(g.getGroupID(), g);
		}
		return cacheMap;
	}
	
	@PostConstruct
	public void init(){
		cacheMap = this.getRtpGroupMap();
	}
	
	private Map<Integer,RtpGroup> cacheMap = null;
	
	public String getRtpGroupPath(int groupID){
		RtpGroup g = getRtpGroup(groupID);
		StringBuilder sb = new StringBuilder();
		while(g != null && g.getParentID()!=0){
			String current = g.getGroupName();
			sb.insert(0, "/"+current);
			g = getRtpGroup(g.getParentID());
		}
		if(g!=null){			
			sb.insert(0, g.getGroupName());
		}
		return sb.toString();
	}
	
	
	public RtpGroup getRtpGroup(int groupID){
		//RtpGroupID,parentID, RtpGroupName
		RtpGroup result = cacheMap.get(groupID);
		if(result!= null){
			return result;
		}
		String sql = "select * from rtpgroup where groupID = ?";
		List<RtpGroup> groupList = jdbcTemplate.query(sql, new Object[]{groupID},groupRowMapper);
		if(groupList.size() == 1){
			return groupList.get(0);
		}
		return null;
	}
	
	
	public RtpGroup getRtpGroupByPath(String groupPath){
		String[] array = groupPath.split("/");
		String sql = "select * from rtpgroup where groupName=?";
		List<RtpGroup> glist = jdbcTemplate.query(sql, new Object[]{array[array.length - 1]},groupRowMapper);
		if(glist.size() == 1){ //查询叶子节点，若只有一个则直接返回
			return glist.get(0);
		} else if (glist.size() > 1) { //如果不止一个，则检查所有候选节点的全路径，如匹配则返回
			for(RtpGroup g:glist){
				String path = getRtpGroupPath(g.getGroupID());
				if(path.equals(groupPath)){
					return g;
				}
			}
			return null; //一般情况下这种情况是没有的，除非咱们的代码写错了。。。
		} 
		return null; //如果没有找到则直接返回null
	}
	
	public List<RtpGroup> getRtpGroupChildren(String currentPath){
		RtpGroup node  = this.getRtpGroupByPath(currentPath);
		String sql = "select * from rtpgroup where parentID=?";
		if(node != null){
			List<RtpGroup> result = jdbcTemplate.query(sql, new Object[]{node.getGroupID()}, groupRowMapper);
			return result;
		}else{
			return new ArrayList<RtpGroup>();
		}
	}

	@SuppressWarnings("deprecation")
	public boolean isLeafNode(int groupID) {
		String sql = "select count(*) from rtpgroup where parentID=?";
		int count = jdbcTemplate.queryForInt(sql, new Object[]{groupID});
		if(count > 0){
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public int getGroupSortWeight(int groupId){
		String sql = "select s.weight as weight from rtpsortweight s, rtpgroup g where g.groupID=? and g.groupID=s.sortid";
		return jdbcTemplate.queryForInt(sql, new Object[]{groupId});
	}

}
