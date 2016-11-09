package com.example;

import java.util.HashMap;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DemoApplication {

	public static HashMap<Long,BusLine> hmBusLine;
	 
	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
		
		hmBusLine=new HashMap<Long,BusLine>();
		 
		BusLine one=new BusLine(1,"Norelan");
	      hmBusLine.put(new Long(one.getId()),one);
	 
	 
	      BusLine two=new BusLine(2,"else");
	      hmBusLine.put(new Long(two.getId()),two);
		
		
	}
}
