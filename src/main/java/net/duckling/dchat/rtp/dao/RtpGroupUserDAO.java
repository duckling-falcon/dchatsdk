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
import java.util.List;

import net.duckling.dchat.rtp.domain.RtpGroupUser;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class RtpGroupUserDAO {

	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	private RowMapper<RtpGroupUser> groupUserMapper =  new RowMapper<RtpGroupUser>(){
		@Override
        public RtpGroupUser mapRow(ResultSet rs, int index) throws SQLException {
			RtpGroupUser g = new RtpGroupUser();
			g.setUserID(rs.getInt("userID"));
			g.setGroupID(rs.getInt("groupID"));
            return g;
        }
	};
	
	public List<Integer> getGroupsForUser(int userID){
		String sql = "select * from rtpgroupuser where userID = ?";
		List<RtpGroupUser> list = jdbcTemplate.query(sql, new Object[]{userID},groupUserMapper);
		List<Integer> result = new ArrayList<Integer>();
		for(RtpGroupUser gu:list){
			result.add(gu.getGroupID());
		}
		return result;
	}
	
	public List<String> getUsersAtGroup(int groupID){
		String sql = "select userName from rtpuser a, rtpgroupuser b where a.userID = b.userID and b.groupID = ?";
		List<String> list = jdbcTemplate.queryForList(sql, new Object[]{groupID}, String.class);
		return list;
	}

}
