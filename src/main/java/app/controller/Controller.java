package app.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.security.oauth2.client.EnableOAuth2Sso;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import com.bedatadriven.jackson.datatype.jts.JtsModule;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;

import app.dao.UserRepository;
import app.model.CustomUser;
import app.model.LinesAndStops;
import app.model.Schedule;
import app.model.Stop;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import app.service.LineService;
import app.service.LinesAndStopsService;
import app.service.StopService;
import app.service.UserService;

import com.fasterxml.jackson.datatype.joda.JodaModule;
/**
 * My controller links my httprequests with my services !
 * @author Jonas
 *
 */


@RestController
@Configuration
@EnableOAuth2Sso
@EnableJpaRepositories("app.dao")
@EntityScan("app.model")
public class Controller {

	
@Autowired LineService lineService;	
@Autowired StopService stopService;	
@Autowired LinesAndStopsService linesAndStopsService;
@Autowired UserService userService;

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
	
	@RequestMapping(value="/location", method = RequestMethod.GET)
	@ResponseBody
	public String whereIsMyAppHosted()
	{
		String ip="";
		try {

		URL whatismyip = new URL("http://ip-api.com/json");
		BufferedReader in = new BufferedReader(new InputStreamReader(
		                whatismyip.openStream()));

		
	
			ip = in.readLine();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} //you get the IP as a String
		return ip;
	}

	
	@RequestMapping(value="/user", method = RequestMethod.GET)
	@ResponseBody
	  public boolean registerUser(Principal principal, @RequestParam(value="cats", required=false) Optional<Boolean> likesCats) {

		
	
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();	
		
		if(!userService.userExists(authentication.getName()))
		{
			userService.registerUser(authentication.getName(), (likesCats.isPresent() && likesCats.get() ));
			
		}
		else
		{
			userService.deleteUser(authentication.getName());
			userService.registerUser(authentication.getName(),(likesCats.isPresent() && likesCats.get() ));
			
		}
		
		return true;
		
	  }
	
	
	@RequestMapping(value="/doilikecats", method = RequestMethod.GET)
	@ResponseBody
	  public String doesUserLikesCats(Principal principal)
	  {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();	
		
		if(userService.userExists(authentication.getName()))
		{
			if(userService.findCustomUser(authentication.getName()).isLikeCats())
				return "true";
			else
				return "false";
		}
		else
			return "User does not exists";
	  }
	
	
	
	
	@RequestMapping(value="/getShortestWayBetween",method = RequestMethod.GET)
	@ResponseBody
	
	public LinesAndStops getShortestWayBetween(@RequestParam("start")String firstStop, @RequestParam("end")String endStop,@RequestParam(value="date",required=false)DateTime now)
	{
		
		if(now == null)
		{
			now = DateTime.now();
		}
		
		Stop sstartStop = stopService.findStopByLabel(firstStop);
		Stop sendStop = stopService.findStopByLabel(endStop);
		
		if(sstartStop == null)
			throw new NullPointerException("The start stop is null");
		if(sendStop==null)
			throw new NullPointerException("The end stop is null");
		
		if(sstartStop!=null && sendStop!=null)
		{
			return stopService.getShortestWayBetween(sstartStop,sendStop,now);
			
		}
		else
		{
			return null;
		}
	}
	
	
	@RequestMapping(value="/getStopSchedules",method = RequestMethod.GET)
	@ResponseBody
	public List<Schedule>getStopSchedules(@RequestParam("stop")String stop)
	{
		//
		List<Schedule> schedules = stopService.getStopBylabel(stop).getSchedules();
		return schedules;
	}
	
	@RequestMapping(value="/debug",method = RequestMethod.GET)
	@ResponseBody
	public void debug()
	{
		//
		stopService.debug();
	}
	
	
	
	@RequestMapping(value="/getOneStopNeighbour",method = RequestMethod.GET)
	@ResponseBody
	public List<Stop> getOneStopNeighbour(@RequestParam("stop")String stop)
	{
		//
		List<Stop> neighbours = stopService.getOneStopNeighbour(stop);
		return neighbours;
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
	
		GeometryFactory factory = new GeometryFactory();
		
		return stopService.getClosestStop(factory.createPoint(new Coordinate(latitude,longitude)));
	}
	
	/**
	 * find a stop by label
	 * @param label
	 * @return Stop json object
	 */
	@RequestMapping(value="/getstopbylabel",method = RequestMethod.GET)
	@ResponseBody
	public Stop getStopByLabel(@RequestParam("label")String label)
	{
		return stopService.getStopBylabel(label);
	}
	
	/**
	 * Custom Object mapper that parses jts geometry objects to geojson
	 * @return
	 */
	@Bean
    public ObjectMapper objectMapper() {
		ObjectMapper mapper = new ObjectMapper();
		  mapper.registerModules(new JodaModule(),new JtsModule());
	       
	        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
		return mapper;
	}
	
}
