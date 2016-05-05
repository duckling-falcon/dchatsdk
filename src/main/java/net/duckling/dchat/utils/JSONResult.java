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

import java.util.Map;

import org.json.simple.JSONObject;
/**
 * JSON结果生成工具类
 * @author Yangxp
 * @since 2012-06-17
 */
public final class JSONResult {
	
	private JSONResult(){}
	
	/**
	 * 生成表示成功的JSON信息：{status:"success", message:"message content", obj:"return object"}
	 * @param message 成功的消息
	 * @param obj 需要传递的JSON参数串
	 * @return JOSN对象
	 */
	public static JSONObject success(String message, JSONObject obj){
		JSONObject result = generateResultJSON("success", message);
		if(null != obj){
			result.put("obj", obj);
		}
		return result;
	}
	/**
	 * 生成表示失败的JSON信息：{status:"fail", message:"message content", 导致失败的参数(JSON格式)}
	 * @param message 失败的消息
	 * @param failParams 造成失败的请求参数
	 * @return JSON对象
	 */
	public static JSONObject fail(String message, Map<String, Object> failParams){
		JSONObject result = generateResultJSON("fail", message);
		if(null != failParams && !failParams.isEmpty()){
			for(Map.Entry<String, Object> entry : failParams.entrySet()){
				result.put(entry.getKey(), entry.getValue());
			}
		}
		return result;
	}
	
	private static JSONObject generateResultJSON(String status, String message){
		JSONObject result = new JSONObject();
		result.put("status", status);
		result.put("message", message);
		return result;
	}
}
