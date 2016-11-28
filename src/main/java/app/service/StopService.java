package app.service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.geotools.geometry.jts.JTSFactoryFinder;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.csvreader.CsvReader;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.linearref.LinearLocation;
import com.vividsolutions.jts.linearref.LocationIndexedLine;

import app.dao.LineRepository;
import app.dao.StopRepository;
import app.model.Line;
import app.model.Schedule;
import app.model.Stop;
import app.model.StopNeighbour;

@Service
public class StopService {

	@Autowired
	private StopRepository stopRepository;
	@Autowired LineRepository lineRepository;
	final String FILEPATH = "src/main/resources/stopList.json";
	final String DEPFILEPATH ="src/main/resources/stops_line.json";
	final String CSVFILEPATH="src/main/resources/";
	/**
	 * Reads json objects to generate stops and get closest neighbours for each stop
	 */
	public void init(){
		try {
			Iterable<Stop> stops = getStopsFromJson();
			stops =generatesClosestNeighboors((List<Stop>)stops);
			//stops = generatesSchedules((List<Stop>) stops); //Needs debug
			//System.out.println(stops.iterator().next().getNeighbours().get(0).getNeighbour().getLabel());
			
			/*for(Stop stop :stops)
			{
				
				for (StopNeighbour neighbour : stop.getNeighbours())
				{
					System.out.println(stop.getLabel()+" : "+neighbour.toString());
				}
			}*/
			for(Stop stop : stops)
			{
				
					//System.out.println(stop.getLabel() + ": ");
				
			}
			
			stopRepository.deleteAll();
			stopRepository.save(stops);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public Stop getStopBylabel(String label)
	{
		return stopRepository.findByLabel(label);
	}
	
	
	// returns closest stop from location
	public Stop getClosestStop(Point p)
	{
		return stopRepository.findClosestStop(p).get(0);
	}
	
	
	
	private Iterable<Stop> generatesSchedules(List<Stop> stopList)
	{
		
		final int SCHEDULE_NUMBER=53;
		String way=null;
		DateTimeFormatter formatter = DateTimeFormat.forPattern("HH:mm");
		int count=0;
		ArrayList<Stop> stopsWithSchedules = new ArrayList<>();
		for(Stop eachStop : stopList)
		{
			ArrayList<Schedule> stopSchedules = new ArrayList<>();
			for(int i=0; i<eachStop.getLines().size();i++)
			{
				way=null;
				try{
				  File file = new File(CSVFILEPATH+"L"+i+"-D1-PS.csv");//Add loop here to manage line direction
			        try (FileReader reader = new FileReader(file)) {
			        	System.out.println("File found");
			            CsvReader schedules = new CsvReader(reader,';');
			            schedules.readHeaders();
			            Schedule schedule = new Schedule();
			            ArrayList<DateTime> scheduleList = new ArrayList<>();
			            
			            while (schedules.readRecord()) {
			            	if ( schedules.get("way") != null )
			            		if(way==null)
			            			way =  schedules.get("way");
			            	System.out.println(schedules.get("ARRETS")+" == " + eachStop.getLabel());
			            	if(schedules.get("Arrets") == eachStop.getLabel())
			            	{						           							            
				            	for(int scheduleIndex=1; scheduleIndex < SCHEDULE_NUMBER;scheduleIndex++){
				            		
				            		scheduleList.add(formatter.parseDateTime(schedules.get("B"+scheduleIndex)));
				            	}
			            	}
			            
			            }
			            schedule.setSchedules(scheduleList);
			            schedule.setSchoolPeriod(true);// TODO: detect schoolPeriod
			            schedule.setLine(eachStop.getLines().get(i));
			            schedule.setway(stopRepository.findByLabel(way));
			            stopSchedules.add(schedule);
			        }
				}catch(FileNotFoundException e){
					System.out.println("File not found");
					
				}catch(IOException e){}
			
		}
			count++;
			eachStop.setSchedules(stopSchedules);
			stopsWithSchedules.add(eachStop);
		}
		System.out.println("COUNT: "+stopsWithSchedules.size());
		return stopsWithSchedules;
	}
	
	
	/**
	 * 
	 * @return stop list from json
	 * @throws IOException
	 */
	private Iterable<Stop> getStopsFromJson() throws IOException
	{
		ArrayList<Stop> stops=new ArrayList<Stop>();
		File jsonStops = new File(FILEPATH);
	//	BufferedReader reader = Files.newBufferedReader(file.toPath());
		
		ObjectMapper mapper = new ObjectMapper();
		
		
		JsonFactory jsonFactory = mapper.getFactory();
		JsonParser stopParser = jsonFactory.createParser(jsonStops);
		File jsonStopLines = new File(DEPFILEPATH);
		JsonParser stopLinesParser = jsonFactory.createParser(jsonStopLines);
		
		JsonNode stopRootNode = stopParser.getCodec().readTree(stopParser);
		JsonNode stopLinesRootNode = stopLinesParser.getCodec().readTree(stopLinesParser);
		
		if ( stopRootNode.isArray() && stopLinesRootNode.isArray()){
			Iterator<JsonNode> stopNodeIterator = stopRootNode.iterator();
			Iterator<JsonNode> stopLinesNodeIterator = stopLinesRootNode.iterator();
			while ( stopNodeIterator.hasNext()){
				JsonNode stopNode = stopNodeIterator.next();
				long stopId = stopNode.get("id").asLong();
				stopLinesNodeIterator = stopLinesRootNode.iterator();
				while ( stopLinesNodeIterator.hasNext()){
					JsonNode stopLineNode = stopLinesNodeIterator.next();
					long linesNodeId = stopLineNode.get("id").asLong();
					//
					if ( stopId == linesNodeId ){
						//System.out.println("ID: "+stopId+","+linesNodeId);
						JsonNode lineList = stopLineNode.get("lines").get("lines");
						Iterator<JsonNode> lineNodeIterator = lineList.iterator();
						ArrayList<Line> lines =new ArrayList();
						while (lineNodeIterator.hasNext()){
							JsonNode lineNode = lineNodeIterator.next();
							long lineId = lineNode.get("id").asLong();
							Line aLine =lineRepository.findOne(lineId);
							lines.add(aLine);
						}
						double latitude = stopNode.get("latitude").asDouble();
						double longitude = stopNode.get("longitude").asDouble();
						GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();
					    Coordinate coord = new Coordinate(latitude, longitude);
					    Point point = geometryFactory.createPoint(coord);
						Stop stop = new Stop(linesNodeId,stopNode.get("label").asText(),point,lines);
						stop.setSchedules(null);
						
						
						stops.add(stop);
						//System.out.println("ADD");
					}
				}
			}
		}
		else{
			throw new RuntimeException("???"); 
		}

		//System.out.println("RESULT: "+ stops.size()+stops.get(0).getLabel()+stops.get(0).getLinesID().get(1));
		//System.out.println("RESULT: "+ stops.size()+stops.get(1).getLabel()+stops.get(1).getLinesID().get(0));
		/*
		 stops =Arrays.asList( mapper.readValue(reader, Stop[].class));
		
		 file = new File(DEPFILEPATH);
		 reader = Files.newBufferedReader(file.toPath());
		 mapper = new ObjectMapper();
		 module = new SimpleModule();
		 module.addDeserializer(Long.class, new StopLineDeserializer());
			mapper.registerModule(module);
		 List<Long> l= Arrays.asList(mapper.readValue(reader, Long[].class));
		 */
		
		return stops;
	}
	/**
	 * 
	 * @return list of all stops
	 */
	public List<Stop> getAllStops(){
		return (List<Stop>) stopRepository.findAll();
	}
	
	/**
	 * Finds out for each stop it's neighboors on the busline graph
	 * 
	 * @param stopList : all the stops
	 * @return updated stop list in which all stops have closest neighboors
	 */
	private List<Stop> generatesClosestNeighboors(List<Stop> stopList)
	{
		/*
		 * First we create a busline - stop index
		 */
		List<Line> lineList = (List<Line>) lineRepository.findAll();
		HashMap<Line,List<Stop>> stopByLine = new HashMap<>();
		
		
		for(Stop stop : stopList)
		{
			List<Line> linesByStop = stop.getLines();
			for(Line line : linesByStop)
			{
			
				if (stopByLine.containsKey(line))
				{
					List<Stop> keyListStop = stopByLine.get(line);
					keyListStop.add(stop);
					stopByLine.put(line, keyListStop);
				}
				else
				{
					List<Stop> keyListStop = new ArrayList<Stop>();
					keyListStop.add(stop);
					stopByLine.put(line, keyListStop);
					
				}
			}
		}
		/*
		for(Line l : stopByLine.keySet()){
			System.out.println("LINE: "+ l.getName());
			for(Stop s : stopByLine.get(l))
			{
				System.out.println(s.getLabel());
			}
		}*/
		
		ArrayList<Stop> stopsWithNeighBours = new ArrayList<>();
		
		//Now we look over each stop to find it's closest neighbours
		for( Stop stop : stopList )
		{
			
			List<StopNeighbour> stopNeighBours=new ArrayList<>();
			//We need the 2 closest neighbours for each busline
			for(Line line : stop.getLines())
			{
				
				
				List<Stop> stopsOnline = stopByLine.get(line);
				
				LocationIndexedLine locationIndexedLine = new LocationIndexedLine(line.getLines());
				LinearLocation stopLocation = locationIndexedLine.indexOf(stop.getPoint().getCoordinate());
				
				System.out.println(stopLocation.toString());
				stopLocation = locationIndexedLine.project(stop.getPoint().getCoordinate());
				System.out.println(stopLocation.toString());
				
				double shortestDist= Double.MAX_VALUE; //distance between our stop and it's closest neighbour on the current line
				double secondShortestDist = Double.MAX_VALUE; //distance between our stop and it's closest neighbour on the current line going the other way
				Stop closestStop = null;
				Stop secondClosestStop = null; //closest stop on the oposite side of the bus line as the closest stop
				//we go through all stops on current line and find the 2 neighbours
				for(Stop stopOnLine : stopsOnline)
				{
					
					//System.out.println(stopOnLine.getPoint().getCoordinate().toString()+" : "+ stop.getPoint().getCoordinate().toString());
					double currentDist = getDistanceStop(locationIndexedLine,stop,stopOnLine);
					//System.out.println("DISTANCE "+stop.getLabel()+" "+stopOnLine.getLabel()+": "+currentDist);
					if(currentDist != 0)
					{
						if(currentDist < shortestDist)
						{
							//old closest stop is only worth keeping if it's on the other side of the line compared to the new closest stop
							if(closestStop != null && shortestDist < getDistanceStop(locationIndexedLine,stopOnLine,closestStop))
							{
								secondShortestDist = shortestDist;
								secondClosestStop = closestStop;
							}
							shortestDist = currentDist;
							closestStop = stopOnLine;
						}
						else if(currentDist < secondShortestDist)
						{
							secondShortestDist = currentDist;
							//needs to be on the other side of the line compared to closest stop
							if(secondClosestStop != null && secondShortestDist < getDistanceStop(locationIndexedLine,secondClosestStop,stopOnLine))
							{
								secondShortestDist = currentDist;
								secondClosestStop = closestStop;
							}
						}
						
						
					}
				}
				//Add neighbours to stops
				if(closestStop != null)
				{
					StopNeighbour firstNeighbour = new StopNeighbour(line, shortestDist, closestStop);
					stopNeighBours.add(firstNeighbour);

					
				}
				if(secondClosestStop != null)
				{
					StopNeighbour secondNeighbour = new StopNeighbour(line, secondShortestDist, secondClosestStop);
					stopNeighBours.add(secondNeighbour);
					
				}
				
				
			}
			//Add new stops to stopList
			stop.setNeighbours(stopNeighBours);
			stopsWithNeighBours.add(stop);
		}
		return stopsWithNeighBours;
	}

	/**
	 * 
	 * @param line
	 * @param s1
	 * @param s2
	 * @return distance between two stops using line
	 */
	private double getDistanceStop(LocationIndexedLine line, Stop s1, Stop s2)
	{
		
		LinearLocation s1Location = line.project(s1.getPoint().getCoordinate());
		LinearLocation s2Location = line.project(s2.getPoint().getCoordinate());
		
		Coordinate snap = line.extractPoint(s1Location);
		Coordinate snap2 = line.extractPoint(s2Location);
		
		double distance = snap.distance(snap2);
		
		//System.out.println("LOCATION: "+snap.toString()+" ; "+ snap2.toString());
		LineString dist = (LineString) line.extractLine(s1Location, s2Location);
	
		
		return distance;
		
	
	}
	
	
}
