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
package net.duckling.dchat.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.List;

import org.yaml.snakeyaml.Yaml;

public class YamlConfig {


	public static void main(String[] args) throws IOException {
		InputStream input = new FileInputStream(new File("D:\\code\\j2ee\\dchat\\target\\dchat\\WEB-INF\\conf\\public-service.yml "));
		Yaml yaml = new Yaml();
		
		Iterable<Object> list = (Iterable<Object>)yaml.loadAll(input);
		for(Object gdf:list){
			System.out.println(gdf);
		}
	}
	
	public void init(String path){
		
	}
	
	public String getProperty(String key){
		return null;
	}
	
	public LinkedHashMap<String, Object> getHashMap(){
		return null;
	}
	
	public List<String> getList(){
		return null;
	}
	
	

}



