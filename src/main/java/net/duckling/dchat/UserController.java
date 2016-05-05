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
package net.duckling.dchat;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.util.WebUtils;

@Controller
@RequestMapping(value = "/rest/user")
public class UserController {

	/**
	 * 获取用户列表()
	 * 
	 * @return
	 * @throws IOException
	 */
	@RequestMapping(value = "/map", method = RequestMethod.POST)
	@ResponseBody
	public Map<String, Object> getUserMap(HttpServletRequest request, HttpServletResponse response) {
		String sPage = WebUtils.findParameterValue(request, "page");
		String sPageSize = request.getParameter("pageSize");
		int page = Integer.parseInt(sPage);
		int pageSize = Integer.parseInt(sPageSize);
		Map<String, Object> map = new HashMap<String, Object>();
		List<User> userList = userService.getUserList(page, pageSize);
		map.put("userList", userList);
		return map;
	}

	/**
	 * 获取用户列表
	 * 
	 * @return
	 */
	@RequestMapping(value = "/list", method = RequestMethod.GET)
	@ResponseBody
	public List<User> getUserList(HttpServletRequest request,HttpServletResponse response) {
		String sPage = WebUtils.findParameterValue(request, "page");
		String sPageSize = request.getParameter("pageSize");
		int page = Integer.parseInt(sPage);
		int pageSize = Integer.parseInt(sPageSize);
		List<User> userList = userService.getUserList(page, pageSize);
		return userList;
	}

	/**
	 * 添加用户
	 * 
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/add", method = RequestMethod.PUT)
	public List<User> addUserBean(@RequestBody User user, HttpServletRequest request,
			HttpServletResponse response) {
		String sPage = WebUtils.findParameterValue(request, "page");
		String sPageSize = request.getParameter("pageSize");
		int page = Integer.parseInt(sPage);
		int pageSize = Integer.parseInt(sPageSize);
		userService.saveOrUpdateUser(user);
		List<User> userList = userService.getUserList(page, pageSize);
		return userList;
	}

	/**
	 * 更新用户
	 * 
	 * @param user
	 *            前台获取用户对象
	 * @param request
	 *            请求对象
	 * @return
	 */
	@RequestMapping(value = "/update", method = RequestMethod.POST)
	@ResponseBody
	public List<User> updateUser(@RequestBody User user, HttpServletRequest request,
			HttpServletResponse response) {
		String sPage = WebUtils.findParameterValue(request, "page");
		String sPageSize = request.getParameter("pageSize");
		int page = Integer.parseInt(sPage);
		int pageSize = Integer.parseInt(sPageSize);
		userService.saveOrUpdateUser(user);
		List<User> list = userService.getUserList(page, pageSize);
		return list;
	}

	/**
	 * 删除用户
	 * 
	 * @param request
	 *            请求对象
	 * @return
	 */
	@RequestMapping(value = "/delete", method = RequestMethod.DELETE)
	@ResponseBody
	public List<User> deleteUser(HttpServletRequest request,
			HttpServletResponse response) {
		String userId = request.getParameter("id");
		String sPage = WebUtils.findParameterValue(request, "page");
		String sPageSize = request.getParameter("pageSize");
		int page = Integer.parseInt(sPage);
		int pageSize = Integer.parseInt(sPageSize);
		User user = new User(userId);
		userService.deleteUser(user);
		List<User> list = userService.getUserList(page, pageSize);
		return list;
	}

	/**
	 * 用户业务接口
	 */
	@Autowired
	private IUserService userService;
}
