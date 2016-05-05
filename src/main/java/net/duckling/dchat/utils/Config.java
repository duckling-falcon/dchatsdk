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

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
/**
 * 读取配置文件、系统属性、环境变量中的配置参数值
 * @author Yangxp
 * @since 2012-06-27
 */
public class Config extends PropertyPlaceholderConfigurer {

	private static Map<String, Object> properties = new HashMap<String, Object>();
	
	@Override
	protected void processProperties(
			ConfigurableListableBeanFactory beanFactoryToProcess,
			Properties props) throws BeansException {
		super.processProperties(beanFactoryToProcess, props);
		for(Object key : props.keySet()){
			String keyStr = key.toString();
			String value = props.getProperty(keyStr);
			properties.put(keyStr, value);
		}
	}
	/**
	 * 获取属性值，优先级：自定义属性 > 系统属性  > 环境变量
	 * @param key 属性的键值
	 * @return 属性值，若不存在则返回null
	 */
	public static String getProperty(String key){
		String result = (String)properties.get(key);
		if(StringUtils.isBlank(result)){
			result = System.getProperty(key);
			if(StringUtils.isBlank(result)){
				result = System.getenv(key);
			}
		}
		return result;
	}

}
