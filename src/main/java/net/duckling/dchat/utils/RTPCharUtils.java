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

public class RTPCharUtils {
	public static String escapeNode(String node) {
		if (node == null) {
			return null;
		}
		final StringBuilder buf = new StringBuilder(node.length() + 8);
		for (int i = 0, n = node.length(); i < n; i++) {
			final char c = node.charAt(i);
			switch (c) {
				case '"' :
					buf.append("\\22");
					break;
				case '&' :
					buf.append("\\26");
					break;
				case '\'' :
					buf.append("\\27");
					break;
				case '/' :
					buf.append("\\2f");
					break;
				case ':' :
					buf.append("\\3a");
					break;
				case '<' :
					buf.append("\\3c");
					break;
				case '>' :
					buf.append("\\3e");
					break;
				case '@' :
					buf.append("\\40");
					break;
				case '\\' :
					final int c2 = (i + 1 < n) ? node.charAt(i + 1) : -1;
					final int c3 = (i + 2 < n) ? node.charAt(i + 2) : -1;
					if ((c2 == '2' && (c3 == '0' || c3 == '2' || c3 == '6'
							|| c3 == '7' || c3 == 'f'))
							|| (c2 == '3' && (c3 == 'a' || c3 == 'c' || c3 == 'e'))
							|| (c2 == '4' && c3 == '0')
							|| (c2 == '5' && c3 == 'c')) {
						buf.append(c);
					} else {
						buf.append("\\5c");
					}
					break;
				default : {
					if (Character.isWhitespace(c)) {
						buf.append("\\20");
					} else {
						buf.append(c);
					}
				}
			}
		}
		return buf.toString();
	}
}
