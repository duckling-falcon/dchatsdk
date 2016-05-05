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
package net.duckling.dchat.rest.notify;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Date;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.Test;

public class NotificationControllerTest {

//	@Test
	public void test() throws UnknownHostException, IOException  {
		JSONObject json = new JSONObject();
		json.put("creatTime",String.valueOf(new Date()));
		json.put("number", 123);
		json.put("str", "hello");
		System.out.println(json.toJSONString());
		JSONParser parser = new JSONParser();
		JSONObject obj;
		try {
			obj = (JSONObject)parser.parse(json.toJSONString());
			System.out.println(obj.get("creatTime"));
		} catch (ParseException e) {
			e.printStackTrace();
		}
		Socket socket = new Socket("mail.cstnet.cn",80);
		System.out.println(socket.getInetAddress());
		socket.close();
	}
	
	@Test
	public void testDDLNotice(){
		String json = "{\"count\":10}";
		JSONParser parser = new JSONParser();
		JSONObject obj = null;
		try {
			obj = (JSONObject)parser.parse(json);
			System.out.println(obj.toString());
			System.out.println(obj.get("count"));
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}
	
	

}
