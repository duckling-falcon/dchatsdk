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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import net.duckling.dchat.rtp.domain.UserPrefs;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class UserPrefsDAO {
	
	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	private RowMapper<UserPrefs> prefsMapper =  new RowMapper<UserPrefs>(){
		@Override
        public UserPrefs mapRow(ResultSet rs, int index) throws SQLException {
			UserPrefs g = new UserPrefs();
			g.setSwitchNotice(rs.getString("switchNotice"));
            g.setUsername(rs.getString("username"));
            g.setFilterRule(rs.getString("filterRule"));
            //ALTER TABLE `rtpnew`.`userprefs` ADD COLUMN `filterRule` text NOT NULL AFTER `updateTime`;
            g.setUpdateTime((Date)rs.getTimestamp("updateTime"));
            return g;
        }
	};
	
	public UserPrefs query(String username){
		final String sql = "select * from userPrefs where username = ?";
		List<UserPrefs> list = jdbcTemplate.query(sql, new Object[]{username},prefsMapper);
		if(list!=null && list.size()!=0){
			return list.get(0);
		}
		return null;
	}
	
	public int update(final UserPrefs p){
		final String sql = "update userPrefs set switchNotice=?,updateTime=?,filterRule=? where username=?";
		int flag = jdbcTemplate.update(sql,new Object[]{
				p.getSwitchNotice(),
				new Timestamp(p.getUpdateTime().getTime()),
				p.getFilterRule(),
				p.getUsername()});
		return flag;
	}
	
	public synchronized int insert(final UserPrefs p) {
		final String sql = "insert into userPrefs (username,switchNotice,updateTime,filterRule) values(?,?,?,?)";
		KeyHolder keyHolder = new GeneratedKeyHolder();
		jdbcTemplate.update(new PreparedStatementCreator() {
			public PreparedStatement createPreparedStatement(Connection conn) throws SQLException {
				int i = 0;
				PreparedStatement ps = conn.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS);
				ps.setString(++i, p.getUsername());
				ps.setString(++i, p.getSwitchNotice());
				ps.setTimestamp(++i, new Timestamp(new Date().getTime()));
				ps.setString(++i, p.getFilterRule());
				return ps;
			}
		}, keyHolder);
		return keyHolder.getKey().intValue();
	}

}
