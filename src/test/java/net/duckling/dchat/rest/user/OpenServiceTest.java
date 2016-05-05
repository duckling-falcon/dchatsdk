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

public class OpenServiceTest {
	
	/*
	 * 功能描述：
	 * 1.公共客户服务Tab页
	 * 
	 * 每个用户都将显示此页，内嵌若干个客服dchat号，用户随时能点击与某个客服进行会话
	 * 没有权限控制，需要使用dchat提供的内嵌对话框接口
	 * 
	 * 数据库设计：
	 * global_embed_chat
	 * id, user_display, org_name, user, edit_time
	 * 
	 */
	 public void testForTabChat(){
		 
	 }
	
	
	/*
	 * 功能描述：
	 * 2.个性化按钮显示功能
	 * 
	 * 每个用户默认情况下都开通所在单位提供服务按钮。显示在最下面一排
	 */
	 public void testForCustomerButton(){
		 
	 }
	 
	
	
	/*
	 * 功能描述：
	 * 3.个性化按钮取消/订阅功能
	 * 
	 * 每个用户通过配置界面能选择保留哪些按钮，去掉哪些按钮。
	 */
	 
	
	/*
	 * 功能描述：
	 * 4.部门隐藏子部门也跟着隐藏，继承父节点的属性
	 */
	

}
