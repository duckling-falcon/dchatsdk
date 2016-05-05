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

public class LinkButton {
	
	public static enum Device {
		ANDROID,IOS,WEB;
		public String toString(){
			return this.name().toString().toLowerCase();
		}
	}
	
	public static enum ButtonPlace {
		TOOLBAR, BOTTOMTOOLBAR;
		public String toString(){
			return this.name().toString().toLowerCase();
		}
	}
	
	public static enum LinkType {
		BROWSER,EMBED,EMBEDMODAL,BACKGROUND;
		public String toString(){
			return this.name().toString().toLowerCase();
		}
	}
	
	/**
	 * @param 		appid
	 * @description 必填，当前按钮对应的应用ID号，获取访问URL时以此字段为准 
	 */
	protected String appid;
	
	/**
	 * @param imageKey
	 * @description rtp需要的按钮标识，对应的按钮图标为web路径/rtp/images/btn_${deviceos}_${imageKey}.png
	 */
	protected String imageKey;

	/**
	 * @param 		deviceos
	 * @description 访问设备的前缀标志
	 * @enum_values andriod - 安卓 | ios - 苹果 | web - 浏览器
	 */
	protected String deviceos;

	/**
	 * @param 		tooltip
	 * @description 可选，按钮说明文字，鼠标移动到按钮上时显示
	 */
	protected String tooltip;
	/**
	 * @param 		linkType 
	 * @description 可选，链接打开方式 
	 * @enum_values browser - 用默认浏览器打开 | embed - 用嵌入窗口打开 | 
	 * 				embedmodal - 用嵌入窗口以模态方式打开 | background - 只访问某个链接不打开网页
	 */ 
	protected String linkType;

	/**
	 * @param features
	 * @description 只有在option为embed和embedmodal的时候，此参数才有效.用来决定嵌网页窗口的各种状态，
	 * 				分号分隔的特性表达式,如dialogTop:100;dialogHeight:400;center:off;resizable:on;
	 * @enum_values dialogLeft,dialogTop,dialogWidth,dialogHeight - 点阵数指定对话框的大小和位置 
	 * 				center - on|off,是否居中 
	 * 				resizable - on|off,可否改变大小 
	 * 				scroll - on|off,是否显示滚动条 
	 * 				status - on|off,是否显示状态栏
	 */
	protected String features;

	/**
	 * @param 		buttonPlace
	 * @description 按钮类型，通过这个参数可以决定按钮出现在窗口中的特定位置等 ;
	 * @enum_values toptoolbar - 出现在顶部工具栏中(默认) | bottomtoolbar - 出现在底部工具栏中 
	 */
	protected String buttonPlace;
	
	/**
	 * @param 		refreshURL
	 * @description 获取图标上刷新数字的请求URL，访问该URL时将返回一个JSON对象。
	 * 				此JSON对象为{"caption":"5", "refresh":"30"}
	 */
	protected String refreshURL;

}
