package app.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.bedatadriven.jackson.datatype.jts.JtsModule;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

import app.model.LinesAndStops;
import app.model.Stop;

import app.service.LineService;
import app.service.LinesAndStopsService;
import app.service.StopService;


/**
 * My controller links my httprequests with my services !
 * @author Jonas
 *
 */


@RestController
@Configuration
@EnableJpaRepositories("app.dao")
@EntityScan("app.model")
public class Controller {

	
@Autowired LineService lineService;	
@Autowired StopService stopService;	
@Autowired LinesAndStopsService linesAndStopsService;

	/**
	 * Loads DB from json files
	 * Call this once to initialize the app
	 * @return
	 */
	@RequestMapping(value="/init",method = RequestMethod.GET)
	@ResponseBody
	public String init(){
		lineService.init();
		stopService.init();
		return "oui";
	}
	
	
	
	/**
	 * returns json with all lines and all stops
	 * @return
	 */
	@RequestMapping(value="/getlinesandstops",method = RequestMethod.GET)
	@ResponseBody
	public LinesAndStops getLinesAndStops(){
		
		return linesAndStopsService.getAllLinesAndStops();
	}
	
	/**
	 * returns closest stop from user location
	 * @param latitude double 
	 * @param longitude double
	 * @return Stop json object
	 */
	@RequestMapping(value="/getcloseststop",method = RequestMethod.GET)
	@ResponseBody
	public Stop getClosestStop(@RequestParam("latitude")double latitude,@RequestParam("longitude") double longitude){
		System.out.println("PARAMETERS: "+ latitude + " : " + longitude);
		GeometryFactory factory = new GeometryFactory();
		
		return stopService.getClosestStop(factory.createPoint(new Coordinate(latitude,longitude)));
	}
	/**
	 * Custom Object mapper that parses jts geometry objects to geojson
	 * @return
	 */
	@Bean
    public ObjectMapper objectMapper() {
		ObjectMapper mapper = new ObjectMapper();
		mapper.registerModule(new JtsModule());
		return mapper;
	}
	
}
