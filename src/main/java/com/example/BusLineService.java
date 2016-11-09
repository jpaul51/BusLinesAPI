package com.example;

import java.util.HashMap;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping(value="/rest/busline")
public class BusLineService {
	@RequestMapping(value="/",method = RequestMethod.GET)
	   public HashMap<Long,BusLine> getAllStudents(){
	      return DemoApplication.hmBusLine;
	   }
}
