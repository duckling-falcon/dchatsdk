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
import java.util.List;

import net.duckling.dchat.rtp.domain.RtpUser;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class RtpUserDAO {

	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	private RowMapper<RtpUser> userMapper =  new RowMapper<RtpUser>(){
		@Override
        public RtpUser mapRow(ResultSet rs, int index) throws SQLException {
			RtpUser g = new RtpUser();
			g.setJid(rs.getString("jid"));
			g.setSource(rs.getString("source"));
			g.setUserID(rs.getInt("userID"));
			g.setUserName(rs.getString("userName"));
			g.setPassword(rs.getString("password"));
			g.setState(rs.getInt("state"));
            g.setSource(rs.getString("source"));
            return g;
        }
	};
	
	public RtpUser query(String userName){
		String sql = "select * from rtpuser where userName = ?";
		List<RtpUser> userList = jdbcTemplate.query(sql, new Object[]{userName},userMapper);
		if(userList != null && userList.size()!=0) {
			return userList.get(0);
		}
		return null;
	}
}
