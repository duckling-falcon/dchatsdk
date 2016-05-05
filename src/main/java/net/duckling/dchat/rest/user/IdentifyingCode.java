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

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.Random;

public class IdentifyingCode {  
    /** 
     * 验证码图片的宽度。 
     */  
    private int width = 80;  
    /** 
     * 验证码图片的高度。 
     */  
    private int height = 40;  
    /**
     *  取随机产生的认证码(4位数字)
     */
 	private static String codeList = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890";
    
    /** 
     * 验证码的数量。 
     */  
    private static Random random = new Random(); 
    
    private final String code;
    
    public IdentifyingCode(String code){
    	this.code = code;
    }  
    /** 
     * 生成随机颜色 
     * @param fc    前景色 
     * @param bc    背景色 
     * @return  Color对象，此Color对象是RGB形式的。 
     */  
    private Color getRandomColor(int fc, int bc) {  
        if (fc > 255)  
            fc = 255;  
        if (bc > 255)  
            bc = 255;  
        int r = fc + random.nextInt(bc - fc);  
        int g = fc + random.nextInt(bc - fc);  
        int b = fc + random.nextInt(bc - fc);  
        return new Color(r, g, b);  
    }  
      
    /** 
     * 绘制干扰线 
     * @param g Graphics2D对象，用来绘制图像 
     * @param nums  干扰线的条数 
     */  
    private void drawRandomLines(Graphics2D g ,int nums ){  
        g.setColor(this.getRandomColor(160, 200)) ;  
        for(int i=0 ; i<nums ; i++){  
            int x1 = random.nextInt(width) ;  
            int y1 = random.nextInt(height);  
            int x2 = random.nextInt(12) ;  
            int y2 = random.nextInt(12) ;  
            g.drawLine(x1, y1, x1+x2, y1+y2) ;  
        }  
    }  
      
    /** 
     * 获取随机字符串， 
     *      此函数可以产生由大小写字母，汉字，数字组成的字符串 
     * @param length    随机字符串的长度 
     * @return  随机字符串 
     */  
    private void drawRandomString(Graphics2D g){  
    	String temp = null;
        for(int i=0 ; i<code.length() ; i++){  
		    temp=code.substring(i,i+1);
            Color color = new Color(20+random.nextInt(20) , 20+random.nextInt(20) ,20+random.nextInt(20) );  
            g.setColor(color) ;  
            //想文字旋转一定的角度  
            AffineTransform trans = new AffineTransform();  
            trans.rotate(random.nextInt(45)*3.14/180, 15*i+8, 7) ;  
            //缩放文字  
            float scaleSize = random.nextFloat() + 0.8f ;  
            if(scaleSize>1f)  
                scaleSize = 1f ;  
            trans.scale(scaleSize, scaleSize) ;  
            g.setTransform(trans) ;  
            g.drawString(temp, 15*i+18, 16) ;  
        }  
        g.dispose() ;  
    }  
    public int getWidth() {  
        return width;  
    }  
    public void setWidth(int width) {  
        this.width = width;  
    }  
    public int getHeight() {  
        return height;  
    }  
    public void setHeight(int height) {  
        this.height = height;  
    }  
    
    public BufferedImage getImage(){
    	BufferedImage image =new BufferedImage(getWidth() , getHeight() , BufferedImage.TYPE_INT_BGR) ;  
    	
        Graphics2D g = image.createGraphics() ;  
        //定义字体样式  
        Font myFont = new Font("Times New Roman" , Font.PLAIN , 18) ;  
        //设置字体  
        g.setFont(myFont) ;  
          
        g.setColor(getRandomColor(200 , 250)) ;  
        //绘制背景  
        g.fillRect(0, 0, getWidth() , getHeight()) ;  
          
        g.setColor(getRandomColor(180, 200)) ;  
        
        drawRandomLines(g, 160) ;  
        
        drawRandomString(g) ;  
        
        g.dispose() ; 
        return image;
    }
    
    public static String getRandomCode(int n){
    	StringBuilder sb = new StringBuilder();
    	for(int i=0;i<n;i++){
    		int a=random.nextInt(codeList.length()-1);
    		sb.append(codeList.substring(a,a+1));
    	}
    	return sb.toString();
    }
}  
