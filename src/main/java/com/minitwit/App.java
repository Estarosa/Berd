package com.minitwit;


import com.minitwit.config.WebConfig;
import com.minitwit.service.impl.MiniTwitService;


public class App {
	
	public static void main(String[] args) {

    	new WebConfig( new MiniTwitService());
    }
    
    
}
