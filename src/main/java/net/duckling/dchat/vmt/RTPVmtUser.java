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
/**
 * 
 */
package net.duckling.dchat.vmt;

import net.duckling.dchat.utils.RtpServerUtils;
import net.duckling.vmt.api.domain.VmtUser;

/**
 * vmt中的用户
 * @author Yangxp
 * @since 2013-06-14
 */
public class RTPVmtUser {
	
	// VMT User props
	public static final String VMT_ID = "vmt-id";
	public static final String VMT_NAME = "vmt-name";
	public static final String VMT_CURRENT_DISPLAY = "vmt-current-display";
	public static final String VMT_STATUS = "vmt-status";
	public static final String VMT_UMT_ID = "vmt-umtId";
	public static final String VMT_SORT_WEIGHTS = "vmt-list-rank";
	
	//RTP User props
	public static final String RTP_NICKNAME = "nickname";
	public static final String RTP_DEPT = "dept";
	public static final String RTP_FULLNAME = "fullname";
	public static final String RTP_URL = "url";
	public static final String RTP_SEX = "sex";
	public static final String RTP_BIRTHDAY = "bday";
	public static final String RTP_EMAIL = "email";
	public static final String RTP_DESC = "desc";
	public static final String RTP_ORG_NAME = "org_name";
	public static final String RTP_ORG_UNIT = "org_unit";
	public static final String RTP_TITLE = "title";
	public static final String RTP_MOBILE = "mobile";
	public static final String RTP_SORTWEIGHTS = "sortweights";
	public static final String RTP_WORK_PHONE = "tel_work_voice_number";
	
	//企业名片::姓名 
	public static final String RTP_EV_NAME = "ev_name";
	//企业名片::单位 
	public static final String RTP_EV_ORGANIZATION = "ev_organization";
	//企业名片::工号  => VMT办公室
	public static final String RTP_EV_NUMBER = "ev_number";
	//企业名片::电话 
	public static final String RTP_EV_PHONE = "ev_phone";
	//企业名片::手机 
	public static final String RTP_EV_MOBILE = "ev_mobile";
	// 	企业名片::电子邮件 
	public static final String RTP_EV_EMAIL = "ev_email";
	// 	企业名片::SIP号码 
	public static final String RTP_EV_SIP = "ev_sip";
	// 	企业名片::备注1 => VMT职称/职务
	public static final String RTP_EV_NOTE1 = "ev_note1";
	// 	企业名片::备注2 => 自定义 送VMT中自定义1的内容
	public static final String RTP_EV_NOTE2 = "ev_note2";

	
	/**
	 * 用户dn
	 * */
	private String dn;
	/**
	 * 用户的umtId，需要umt给出
	 * */
	private String umtId;
	/**
	 * 用户的真实姓名
	 * */
	private String name;
	/**
	 * 用户的激活状态，有三个值，true-已激活，false-未激活，refuse-拒绝
	 * */
	private String status;
	/**
	 * 用户的登录邮箱
	 * */
	private String cstnetId;
	/**
	 * 用户目录的显示状态，用逗号分隔
	 * */
	private String currentDisplay;
	/**
	 * 用戶排序權值
	 */
	private int sortWeights;
	/**
	 * 性别：男/女
	 */
	private String sex;
	/**
	 * 办公地址
	 */
	private String office;
	/**
	 * 办公电话
	 */
	private String officePhone;
	/**
	 * 职称
	 */
	private String title;
	/**
	 * 手机
	 */
	private String telephone;
	
	/*
	 * 是否可见
	 */
	private boolean visible;
	
	
	public boolean isVisible() {
		return visible;
	}
	
	public void setVisible(boolean visible) {
		this.visible = visible;
	}
	
	public String getDn() {
		return dn;
	}
	public void setDn(String dn) {
		this.dn = dn;
	}
	public String getUmtId() {
		return umtId;
	}
	public void setUmtId(String umtId) {
		this.umtId = umtId;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getCurrentDisplay() {
		return currentDisplay;
	}
	public void setCurrentDisplay(String currentDisplay) {
		this.currentDisplay = currentDisplay;
	}
	/**
	 * @return the cstnetId
	 */
	public String getCstnetId() {
		return cstnetId;
	}
	/**
	 * @param cstnetId the cstnetId to set
	 */
	public void setCstnetId(String cstnetId) {
		this.cstnetId = cstnetId;
	}
	
	public int getSortWeights() {
		return sortWeights;
	}
	public void setSortWeights(int sortWeights) {
		this.sortWeights = sortWeights;
	}
	public String getSex() {
		return sex;
	}
	public void setSex(String sex) {
		this.sex = sex;
	}
	public String getOffice() {
		return office;
	}
	public void setOffice(String office) {
		this.office = office;
	}
	public String getOfficePhone() {
		return officePhone;
	}
	public void setOfficePhone(String officePhone) {
		this.officePhone = officePhone;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getTelephone() {
		return telephone;
	}
	public void setTelephone(String telephone) {
		this.telephone = telephone;
	}
	@Override
	public int hashCode() {
		return cstnetId.hashCode();
	}
	@Override
	public boolean equals(Object obj) {
		if(null == obj){
			return false;
		}
		if(!(obj instanceof RTPVmtUser)){
			return false;
		}
		RTPVmtUser user = (RTPVmtUser)obj;
		return user.getCstnetId().equals(cstnetId);
	}
	/**
	 * 構造RTP中的VMT user對象，此處sortweights經過了修改。
	 * @param user
	 * @return
	 */
	public static RTPVmtUser buildFromVmtUser(VmtUser user){
		RTPVmtUser rtpUser = new RTPVmtUser();
		rtpUser.setCstnetId(user.getCstnetId());
		rtpUser.setCurrentDisplay(user.getCurrentDisplay());
		rtpUser.setDn(user.getDn());
		rtpUser.setName(user.getName());
		rtpUser.setStatus(user.getStatus());
		rtpUser.setUmtId(user.getUmtId());
		rtpUser.setSortWeights(RtpServerUtils.getRTPSortWeights(user.getListRank()));
		rtpUser.setSex(user.getSex());
		rtpUser.setOffice(user.getOffice());
		rtpUser.setOfficePhone(user.getOfficePhone());
		rtpUser.setTitle(user.getTitle());
		rtpUser.setTelephone(user.getTelephone());
		rtpUser.setVisible(user.isVisible());
		return rtpUser;
	}
}
