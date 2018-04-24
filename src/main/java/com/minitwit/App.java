package com.minitwit;


import com.minitwit.config.WebConfig;
import com.minitwit.service.impl.MiniTwitService;
import com.minitwit.util.PasswordUtil;


public class App {
	
	public static void main(String[] args) {
    	new WebConfig( new MiniTwitService());
    }
    
    
}
