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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.stereotype.Service;

@Service
public class IUserService {

	private User dataInit() {
		User u = new User();
//		u.setAddress("China");
//		u.setEmail("abc@cnic.cn");
		u.setId("1");
//		u.setIdentityCard("437300");
		u.setRealname("clive");
		u.setPassword("123");
		u.setUsername("croud");
//		u.setZip(100190);
		return u;
	}
	
	Map<String,User> map = new HashMap<String,User>();

	public void deleteUser(User user) {
		map.remove(user.getRealname());
	}

	public List<User> getUserList(int page, int pageSize) {
		if(map.isEmpty()){
			User mock = this.dataInit();
			map.put(mock.getRealname()	, mock);
		}
		List<User> ulist = new ArrayList<User>();
		for(Entry<String,User> entry:map.entrySet()){
			ulist.add(entry.getValue());
		}
		return ulist;
	}

	public void saveOrUpdateUser(User user) {
		map.put(user.getRealname(), user);
	}

}
