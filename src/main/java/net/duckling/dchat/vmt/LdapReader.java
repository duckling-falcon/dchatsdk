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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import net.duckling.dchat.utils.Config;
import net.duckling.dchat.utils.RtpServerUtils;
import net.duckling.dchat.utils.UserUtils;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
/**
 * VMT的LDAP数据查询类
 * @author Yangxp
 * @since 2012-06-13
 */
public class LdapReader {
	
	private static final String LDAP_URL = "vmt.ldap.url";
	private static final String LDAP_ORG_BASE_DN = "vmt.ldap.org.basedn";
	private static final String LDAP_GROUP_BASE_DN = "vmt.ldap.group.basedn";
	private static final String LDAP_ADMIN_DN = "vmt.ldap.admindn";
	private static final String LDAP_ADMIN_PASS = "vmt.ldap.adminpass";
	private static final String LDAP_USER_FILTER = "vmt.ldap.userfilter";
	private static final String LDAP_GROUP_FILTER = "vmt.ldap.groupfilter";
	private static final String LDAP_DEPT_FILTER = "vmt.ldap.deptfilter";
	private static final String DEFAULT_GROUP_FILTER = "(&(objectClass=vmt-group)(vmt-member-visible=true))";
	private static final String DEFAULT_USER_FILTER = "(&(objectClass=vmt-user)(vmt-status=true))";
	private static final String DEFAULT_DEPT_FILTER = "(objectClass=vmt-depart)";
	
	private static final Logger LOG = Logger.getLogger(LdapReader.class);
	private String ldapURL;
	private String ldapOrgBaseDN;
	private String ldapGroupBaseDN;
	private String ldapAdminDN;
	private String ldapAdminPass;
	private String ldapUserFilter;
	private String ldapGroupFilter;
	private String ldapDeptFilter;
	
	private Map<String, String> umtID2CstnetID;
	
	private static class SingletonHolder{
		private static LdapReader instance = new LdapReader();
	}
	
	private LdapReader(){
		ldapURL = Config.getProperty(LDAP_URL);
		ldapOrgBaseDN = Config.getProperty(LDAP_ORG_BASE_DN);
		ldapGroupBaseDN = Config.getProperty(LDAP_GROUP_BASE_DN);
		ldapAdminDN = Config.getProperty(LDAP_ADMIN_DN);
		ldapAdminPass = Config.getProperty(LDAP_ADMIN_PASS);
		ldapUserFilter = Config.getProperty(LDAP_USER_FILTER);
		ldapGroupFilter = Config.getProperty(LDAP_GROUP_FILTER);
		ldapDeptFilter = Config.getProperty(LDAP_DEPT_FILTER);
		umtID2CstnetID = new HashMap<String, String>();
	}
	
	public static LdapReader getInstance(){
		return SingletonHolder.instance;
	}
	/**
	 * 获取VMT中所有的机构和成员
	 * @return 成员列表，机构信息有VmtUser对象的currentDisplay属性表示
	 */
	public List<RTPVmtUser> getRootOrgUsers(){
		return getUsers(ldapOrgBaseDN);
	}
	/**
	 * 获取VMT中特定DN的组织下所有的用户
	 * @param orgDN 组织的DN
	 * @return 成员列表，机构信息有VmtUser对象的currentDisplay属性表示
	 */
	public List<RTPVmtUser> getOrgUsers(String orgDN){
		return getUsers(orgDN);
	}
	
	/**
	 * 获取所有的群组
	 * @return 群组列表
	 */
	public List<RTPChatGroup> getAllGroup(){
		String groupFilter = (StringUtils.isNotBlank(ldapGroupFilter))?ldapGroupFilter:DEFAULT_GROUP_FILTER;
		String[] returnAttributes = new String[]{RTPChatGroup.VMT_GROUP_DN, RTPChatGroup.VMT_ADMIN, 
				RTPChatGroup.VMT_CURRENT_DISPLAY, RTPChatGroup.VMT_NAME, RTPChatGroup.VMT_SYMBOL};
		try {
			NamingEnumeration<SearchResult> results = search(ldapGroupBaseDN, groupFilter, returnAttributes, false);
			return generateGroupSearchResult(results);
		}catch (NamingException e) {
			LOG.error("get groups from ldap failed. ", e);
		}
		return new ArrayList<RTPChatGroup>();
	}
	
	/**
	 * 查找指定group symbol的群组
	 * @param groupSymbols 群组ID，对应VMT中的vmt-symbol值
	 * @return
	 */
	public List<RTPChatGroup> getGroups(String[] groupSymbols){
		List<RTPChatGroup> result = new ArrayList<RTPChatGroup>();
		if(null != groupSymbols && groupSymbols.length >0){
			String baseGroupFilter = (StringUtils.isNotBlank(ldapGroupFilter))?ldapGroupFilter:DEFAULT_GROUP_FILTER;
			String[] returnAttributes = new String[]{RTPChatGroup.VMT_GROUP_DN, RTPChatGroup.VMT_ADMIN, 
					RTPChatGroup.VMT_CURRENT_DISPLAY, RTPChatGroup.VMT_NAME, RTPChatGroup.VMT_SYMBOL};
			for(String groupSymbol : groupSymbols){
				try {
					String groupFilter = "(&"+baseGroupFilter+"(vmt-symbol="+groupSymbol+"))";
					NamingEnumeration<SearchResult> results = search(ldapGroupBaseDN, groupFilter, returnAttributes, false);
					List<RTPChatGroup> groups =  generateGroupSearchResult(results);
					result.addAll(groups);
				}catch (NamingException e) {
					LOG.error("get groups from ldap failed. ", e);
				}
			}
		}
		return result;
	}
	
	/**
	 * 获取某个群组的所有用户
	 * @param groupDN 群组的DN
	 * @return 成员列表
	 */
	public List<RTPVmtUser> getGroupMembers(String groupDN){
		return getUsers(groupDN);
	}
	
	public List<RTPVmtDepart> getAllDepart(){
		String baseDeptFilter = (StringUtils.isNotBlank(ldapDeptFilter))?ldapDeptFilter:DEFAULT_DEPT_FILTER;
		String[] returnAttributes = new String[]{RTPVmtDepart.VMT_CURRENT_DISPLAY, RTPVmtDepart.VMT_LIST_RANK};
		try {
			NamingEnumeration<SearchResult> results = search(ldapOrgBaseDN, baseDeptFilter, returnAttributes, true);
			return generateDeptSearchResult(results);
		}catch (NamingException e) {
			LOG.error("get groups from ldap failed. ", e);
		}
		return new ArrayList<RTPVmtDepart>();
	}
	
	/**
	 * 查询某个DN下的所有用户信息，遍历子树
	 * @param baseDN 
	 * @return 成员列表
	 */
	private List<RTPVmtUser> getUsers(String baseDN){
		String filter = (StringUtils.isNotBlank(ldapUserFilter))?ldapUserFilter:DEFAULT_USER_FILTER;
		String[] returnAttributes = new String[]{RTPVmtUser.VMT_ID, RTPVmtUser.VMT_NAME, RTPVmtUser.VMT_STATUS,
				RTPVmtUser.VMT_UMT_ID, RTPVmtUser.VMT_CURRENT_DISPLAY, RTPVmtUser.VMT_SORT_WEIGHTS};
		try {
			NamingEnumeration<SearchResult> results = search(baseDN, filter, returnAttributes, true);
			return generateSearchResult(results);
		} catch (NamingException e) {
			LOG.error("get users from ldap failed. ", e);
		}
		return new ArrayList<RTPVmtUser>();
	}
	/**
	 * 生成机构信息结果，由 getRootOrgUsers()调用
	 * @param results LDAP查询结果集
	 * @return 成员列表
	 * @throws NamingException
	 */
	private List<RTPVmtUser> generateSearchResult(NamingEnumeration<SearchResult> results) throws NamingException{
		List<RTPVmtUser> users = new ArrayList<RTPVmtUser>();
		while(null != results && results.hasMore()){
			SearchResult sr = results.next();
			Attributes attrs = sr.getAttributes();
			RTPVmtUser user = new RTPVmtUser();
			user.setCstnetId((String)attrs.get(RTPVmtUser.VMT_ID).get());
			user.setCurrentDisplay((String)attrs.get(RTPVmtUser.VMT_CURRENT_DISPLAY).get());
			user.setName((String)attrs.get(RTPVmtUser.VMT_NAME).get());
			user.setUmtId((String)attrs.get(RTPVmtUser.VMT_UMT_ID).get());
			Attribute attr = attrs.get(RTPVmtUser.VMT_SORT_WEIGHTS);
			if(null != attr && null != attr.get()){
				user.setSortWeights(Integer.valueOf((String)attr.get()));
			}
			users.add(user);
			umtID2CstnetID.put(user.getUmtId(), user.getCstnetId());
		}
		return users;
	}
	/**
	 * 生成群组结果，由getAllGroup()调用
	 * @param results LDAP查询结果集
	 * @return 群组列表
	 * @throws NamingException
	 */
	private List<RTPChatGroup> generateGroupSearchResult(NamingEnumeration<SearchResult> results) throws NamingException{
		List<RTPChatGroup> groups = new ArrayList<RTPChatGroup>();
		StringBuilder rejectGroups = new StringBuilder();
		while(null != results && results.hasMore()){
			SearchResult sr = results.next();
			Attributes attrs = sr.getAttributes();
			
			RTPChatGroup group = new RTPChatGroup();
			String groupDN = (String)attrs.get(RTPChatGroup.VMT_GROUP_DN).get();
			group.setGroupDN(groupDN);
			Attribute adminAttr = attrs.get(RTPChatGroup.VMT_ADMIN);
			String groupID = ((String)attrs.get(RTPChatGroup.VMT_SYMBOL).get()).toLowerCase();
			boolean hasOwner = getAndSetAdminsFromAttr(group, groupDN, adminAttr);
			if(hasOwner && RtpServerUtils.checkGroupID(groupID)){
				group.setAuth(RTPChatGroup.RTP_AUTH_CONFIRM);
				group.setCategory("");
				group.setDesc((String)attrs.get(RTPChatGroup.VMT_CURRENT_DISPLAY).get());
				group.setGroupID(groupID);
				group.setMaxuser(500);
				group.setTitle((String)attrs.get(RTPChatGroup.VMT_NAME).get());
				groups.add(group);
			}else{
				rejectGroups.append(groupDN+"\n");
			}
		}
		LOG.info("The following groups have no admin, reject to import: \n"+ rejectGroups.toString());
		return groups;
	}
	
	private List<RTPVmtDepart> generateDeptSearchResult(NamingEnumeration<SearchResult> results) throws NamingException{
		List<RTPVmtDepart> depts = new ArrayList<RTPVmtDepart>();
		while(null != results && results.hasMore()){
			SearchResult sr = results.next();
			Attributes attrs = sr.getAttributes();
			RTPVmtDepart dept = new RTPVmtDepart();
			String deptString = (String)attrs.get(RTPVmtDepart.VMT_CURRENT_DISPLAY).get();
			dept.setDeptPath(UserUtils.formatDept(deptString));
			String listRank = (String)attrs.get(RTPVmtDepart.VMT_LIST_RANK).get();
			if(StringUtils.isNotBlank(listRank)){
				dept.setSortWeights(RtpServerUtils.getRTPSortWeights(Integer.valueOf(listRank)));
			}else{
				dept.setSortWeights(200);
			}
			depts.add(dept);
		}
		return depts;
	}
	
	@SuppressWarnings("unchecked")
	private boolean getAndSetAdminsFromAttr(RTPChatGroup group, String groupDN, 
			Attribute attr) throws NamingException{
		if(null != attr && null !=attr.get()){
			NamingEnumeration<String> umtIDs = (NamingEnumeration<String>) attr.getAll();
			Set<String> result = new HashSet<String>();
			int i=0;
			String owner = null;
			while(umtIDs.hasMore()){
				String umtid = umtIDs.next();
				String admin = getOwnerCstnetID(groupDN,umtid);
				if(null == admin){
					continue;
				}
				
				if(i++ == 0){
					owner = admin;
				}else{
					result.add(admin);
				}
			}
			
			if(null != owner){
				group.setOwner(owner);
				group.setAdmins(result);
				return true;
			}
		}
		return false;
	}
	/**
	 * 根据UMTID获取用户的CstnetID
	 * @param umtID
	 * @return 经过转义@的cstnetID
	 */
	private String getOwnerCstnetID(String groupDN, String umtID){
		String result = null;
		if(StringUtils.isNotBlank(umtID)){
			String cstnetID = umtID2CstnetID.get(umtID);
			if(StringUtils.isBlank(cstnetID)){
				cstnetID = getCstnetIDFromLdap(groupDN, umtID);
				if(null != cstnetID){
					umtID2CstnetID.put(umtID, cstnetID);
				}
			}
			result = UserUtils.escapeCstnetId(cstnetID);
		}
		return result;
	}
	/**
	 * 从LDAP查询用户的cstnetID
	 * @param umtID 
	 * @return 未经转义的cstnetID, 查询不到则返回null
	 */
	private String getCstnetIDFromLdap(String groupDN, String umtID){
		String filter = "(&(objectClass=vmt-user)(vmt-umtId="+umtID+"))";
		try {
			NamingEnumeration<SearchResult> results = search(groupDN, filter, new String[]{RTPVmtUser.VMT_ID}, false);
			if(null != results && results.hasMore()){
				SearchResult result = results.next();
				Attributes attrs = result.getAttributes();
				return (String)attrs.get(RTPVmtUser.VMT_ID).get();
			}else{
				NamingEnumeration<SearchResult> results2 = search(ldapOrgBaseDN, filter, new String[]{RTPVmtUser.VMT_ID}, true);
				if(null != results2 && results2.hasMore()){
					Attributes attr = results2.next().getAttributes();
					return (String)attr.get(RTPVmtUser.VMT_ID).get();
				}
				return null;
			}
		} catch (Exception e) {
			LOG.error("get user from ldap failed. umtID = "+umtID, e);
		}
		return null;
	}
	
	/**
	 * 查询指定DN下满足指定条件的结果
	 * @param baseDN 基础DN
	 * @param filter 过滤条件
	 * @return 查询结果集
	 * @throws NamingException
	 */
	private NamingEnumeration<SearchResult> search(String baseDN, String filter, 
			String[] returnAttributes, boolean isUser) throws NamingException{
		Hashtable<String, Object> env = getEnv();
		DirContext ctx = new InitialDirContext(env);
		SearchControls controls = getSearchControls(returnAttributes, isUser);
		NamingEnumeration<SearchResult> result = ctx.search(baseDN, filter, controls);
		ctx.close();
		return result;
	}
	/**
	 * 设置查询参数
	 * @return
	 */
	private Hashtable<String, Object> getEnv(){
		Hashtable<String, Object> env = new Hashtable<String, Object>();
		env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
		env.put(Context.PROVIDER_URL, ldapURL);
		env.put(Context.SECURITY_AUTHENTICATION, "simple");
		env.put(Context.SECURITY_PRINCIPAL, ldapAdminDN);
		env.put(Context.SECURITY_CREDENTIALS, ldapAdminPass);
		env.put("com.sun.jnid.ldap.connect.pool", "true");
		return env;
	}
	/**
	 * 设置查询范围
	 * @return
	 */
	private SearchControls getSearchControls(String[] returnAttributes, boolean isUser){
		SearchControls controls = new SearchControls();
		int searchScope = isUser ? SearchControls.SUBTREE_SCOPE : SearchControls.ONELEVEL_SCOPE;
		controls.setSearchScope(searchScope);
		controls.setReturningAttributes(returnAttributes);
		controls.setCountLimit(0);
		return controls;
	}

}
