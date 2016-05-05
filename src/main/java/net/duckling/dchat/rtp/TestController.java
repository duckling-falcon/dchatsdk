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
package net.duckling.dchat.rtp;

import javax.servlet.http.HttpServletRequest;

import net.duckling.dchat.utils.RTPCharUtils;
import net.duckling.dchat.utils.RtpServerUtils;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.rooyeetone.rtp.sdk.IRtpSvc;
import com.rooyeetone.rtp.sdk.ISyncItem;
import com.rooyeetone.rtp.sdk.RtpSvc;

@Controller
@RequestMapping("/test")
public class TestController {
	
	private static final Logger LOG = Logger.getLogger(TestController.class);
	/**
	 * 测试新建用户时，部门属性置为空是否会将用户放入默认组织
	 * @param req
	 */
	@RequestMapping("/t1")
	public void test(HttpServletRequest req){
		try{
			String username = "yangxp@cnic.cn";
			IRtpSvc rtp = RtpSvc.getInstance(req);
			ISyncItem item = rtp.exec("SyncData", username,"USER","INSERT");
			item.setProp("nickname", username);
			item.setProp("fullname", username);
			item.setProp("dept", "");
			item.setProp("email", username);
			item.commit();
		}catch(Exception e){
			LOG.error(e);
		}
	}
	/**
	 * 测试新建用户时，不设置部门属性是否会将用户放入默认组织
	 * @param req
	 */
	@RequestMapping("/t2")
	public void test2(HttpServletRequest req){
		try{
			String username= "ab@c.com";
			IRtpSvc rtp = RtpSvc.getInstance(req);
			ISyncItem item = rtp.exec("SyncData", username,"USER","INSERT");
			item.setProp("nickname", username);
			item.setProp("fullname", username);
			item.setProp("email", username);
			item.commit();
		}catch(Exception e){
			LOG.error(e);
		}
	}
	/**
	 * 测试删除群组操作是否正常，写死JID
	 * @param req
	 */
	@RequestMapping("/delgroup")
	public void test3(HttpServletRequest req){
		try{
			IRtpSvc rtp = RtpSvc.getInstance(req);
			rtp.exec("GroupChat.Destroy", "非营利组织ngo@groupchat.dchat.escience.cn");
		}catch(Exception e){
			LOG.error(e);
		}
	}
	/**
	 * 测试删除群组操作是否正常，拼JID
	 * @param req
	 * @param groupID
	 */
	@RequestMapping("/delgroup2")
	public void test4(HttpServletRequest req, @RequestParam("groupID") String groupID){
		try{
			IRtpSvc rtp = RtpSvc.getInstance(req);
			String groupJID = RtpServerUtils.getGroupJIDFromID(groupID);
			LOG.info("remove groupID: "+groupJID);
			rtp.exec("GroupChat.Destroy", groupJID);
		}catch(Exception e){
			LOG.error(e);
		}
	}
}
