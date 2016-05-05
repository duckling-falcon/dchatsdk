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

import org.junit.Test;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class ViRestClientTest {

	ViRestClient c = new ViRestClient("dchatRTP","159.226.11.116",9191);

	@Test
	public void testGetGroupList() throws Exception {
		String str = c.getGroupList("yangxuan@cstnet.cn");
		System.out.println(str);
		
		JsonParser parser = new JsonParser();
		JsonObject json = (JsonObject) parser.parse(str);
		JsonArray array = json.get("result").getAsJsonObject()
							  .get("groups").getAsJsonArray();
		for(int i = 0; i < array.size();i ++){
			System.out.println(array.get(i).getAsString());
		}
	}
	
	@Test
	public void testSetGroupVi() throws Exception {
		//http://rtp.escience.cn:9191/SetGroupVi.act?rtpwebid=dchatRTP&group=0603tb12
		c.setVi("hello", null, null, "qq123");
	}

}
