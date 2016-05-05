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
package net.duckling.dchat.rest.user;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class EmbedConfigDAO {
	/*
	 * 
		CREATE TABLE `dchat_embed_config` (
		`id`  int NULL AUTO_INCREMENT ,
		`account`  varchar(80) NULL ,
		`displayName`  varchar(256) NULL ,
		`duration`  varchar(10) NULL ,
		`indexURL`  text NULL ,
		`telephone`  varchar(50) NULL ,
		`editTime`  timestamp NULL ON UPDATE CURRENT_TIMESTAMP ,
		PRIMARY KEY (`id`)
		)
		;
	 */

	@Autowired
	private JdbcTemplate jdbcTemplate;

	private RowMapper<EmbedConfig> configRowMapper = new RowMapper<EmbedConfig>() {
		@Override
		public EmbedConfig mapRow(ResultSet rs, int index) throws SQLException {
			EmbedConfig ec = new EmbedConfig();
			ec.setAccount(rs.getString("account"));
			ec.setDisplayName(rs.getString("displayName"));
			ec.setId(rs.getInt("id"));
			ec.setDuration(rs.getString("duration"));
			ec.setIndexURL(rs.getString("indexURL"));
			ec.setTelephone(rs.getString("telephone"));
			ec.setEditTime((Date) rs.getTimestamp("editTime"));
			ec.setUnitName(rs.getString("unitName"));
			return ec;
		}
	};

	public int save(final EmbedConfig ec) {
		final String sql = "insert into dchat_embed_config (account,unitName,displayName,duration,indexURL,telephone,editTime)"
				+ " values(?,?,?,?,?,?,?)";
		KeyHolder keyHolder = new GeneratedKeyHolder();
		jdbcTemplate.update(new PreparedStatementCreator() {
			public PreparedStatement createPreparedStatement(Connection conn) throws SQLException {
				int i = 0;
				PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
				ps.setString(++i, ec.getAccount());
				ps.setString(++i, ec.getUnitName());
				ps.setString(++i, ec.getDisplayName());
				ps.setString(++i, ec.getDuration());
				ps.setString(++i, ec.getIndexURL());
				ps.setString(++i, ec.getTelephone());
				ps.setTimestamp(++i, new Timestamp(new Date().getTime()));
				return ps;
			}
		}, keyHolder);
		return keyHolder.getKey().intValue();
	}

	public List<EmbedConfig> list(int offset, int size) {
		String sql = "select * from dchat_embed_config order by id ";
		List<EmbedConfig> list = jdbcTemplate.query(sql, new Object[] {}, configRowMapper);
		return (list.size() > 0) ? list : null;
	}
	
	public int update(final EmbedConfig p){
		final String sql = "update dchat_embed_config set account=?,unitName=?,displayName=?,indexURL=?,duration=?,editTime=?,telephone=? where id=?";
		int flag = jdbcTemplate.update(sql,new Object[]{
				p.getAccount(),
				p.getUnitName(),
				p.getDisplayName(),
				p.getIndexURL(),
				p.getDuration(),
				new Timestamp(p.getEditTime().getTime()),
				p.getTelephone(),
				p.getId()});
		return flag;
	}

	public int delete(int id) {
		final String sql = "delete from dchat_embed_config where id=?";
		return jdbcTemplate.update(sql, new Object[]{id});
	}

	public EmbedConfig get(int id) {
		final String sql = "select * from dchat_embed_config where id=?";
		List<EmbedConfig> confList = jdbcTemplate.query(sql, new Object[]{id},configRowMapper);
		return (confList!=null && confList.size()!=0)?confList.get(0):null;
	}

}
