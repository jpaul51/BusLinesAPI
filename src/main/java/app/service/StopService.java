package app.service;

import static org.mockito.Matchers.anyBoolean;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.geotools.geometry.jts.JTSFactoryFinder;
import org.joda.time.DateTime;
import org.joda.time.Minutes;
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
import app.dao.ScheduleRepository;
import app.dao.StopRepository;
import app.model.Line;
import app.model.Schedule;
import app.model.Stop;


@Service
public class StopService {

	@Autowired private StopRepository stopRepository;
	@Autowired private LineRepository lineRepository;
	@Autowired private ScheduleRepository scheduleRepository;
	final String FILEPATH = "src/main/resources/stopList.json";
	final String DEPFILEPATH ="src/main/resources/stopgroups.json";
	final String CSVFILEPATH="src/main/resources/";
	
	final char firstDirection='I';
	final char secondDirection='O';
	
	ArrayList<Stop> stops;
	/**
	 * Reads json objects to generate stops and get closest neighbours for each stop
	 */
	public void init(){
		try {
			Iterable<Stop> stops = getStopsFromJson();
			//stops =generatesClosestNeighboors((List<Stop>)stops);
			stops = generatesSchedules((List<Stop>) stops); //Needs debug
			//System.out.println(stops.iterator().next().getNeighbours().get(0).getNeighbour().getLabel());
			System.out.println("END SCHEDULES");
			this.stops=(ArrayList<Stop>) stops;
			for(Stop stop :stops)
			{
				
				//System.out.println(stop.getId() +" : "+stop.getNeighboursId().isEmpty() );
				
					//System.out.println(stop.getLabel()+" : "+ stop.getId() +"firstNeighbour: "+ stop.getNeighboursId().get(0));
				if(stop.getLines().contains(new Line(2)))
				{
					System.out.println(stop.getLabel()+": "+stop.getSchedules());
				System.out.println(stop.getSchedules().size() + " --- "+stop.getSchedules().get(0).getSchedules().size());
				}
			}


			
			
			stopRepository.deleteAll();
			stopRepository.save(stops);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	 HashMap<Long,ArrayList<Stop>> stopRoads = new HashMap<>();
	 
	private void checkNeighbours(Stop currentStop, Stop destinationStop, int currentRoad)
	{
		int i =0;
		for(long neighbourId : currentStop.getNeighboursId())
		{
			
			Stop oneNeighbour = stopRepository.findOne(neighbourId);
			
			System.out.println(oneNeighbour.getLabel() +" on road "+ currentRoad+i);
			
			if(!oneNeighbour.equals(destinationStop))
			{
			 checkNeighbours( oneNeighbour,  destinationStop,  currentRoad+i);
			i++;
			}
			
		
		}
	}
	
	
	public void atWhatTimeDoIGetThere(String startStopLabel, String destinationStopLabel,DateTime now)
	{
		
		Stop startStop =stopRepository.findByLabel(startStopLabel);
		Stop destinationStop = stopRepository.findByLabel(destinationStopLabel);
		
		
		ArrayList<Schedule> incomingSchedules = new ArrayList<>();
		
		
		
		DateTime dt = new DateTime();
		Minutes timeToGo = Minutes.minutes(0);
	
		
		System.out.println("CHECKNEIGHBOURS");						
		checkNeighbours(startStop, destinationStop,0);
					
					
		
		Iterator<ArrayList<Stop>> roadIterator = stopRoads.values().iterator();
		int i=0;
		while(roadIterator.hasNext())
		{
			ArrayList<Stop> oneRoad= roadIterator.next();
			System.out.println("ROAD "+i);
			for(Stop stop : oneRoad)
			{
				System.out.println(stop.getLabel());
			}
			System.out.println("");
			i++;
			
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
		
		final String CSV_NULL_VALUE = "....";
		final String CSV_SECOND_NULL_VALUE = "|";
		ArrayList<Stop> stopsWithSchedules = new ArrayList<>();
		
		//System.out.println(CSVFILEPATH+"L1-D1-PS.csv");
		ArrayList<String> periodList = new ArrayList<>();
		periodList.add("PS");
		periodList.add("VS");
		
		for(Stop eachStop : stopList)
		
		{
			
			ArrayList<Schedule> stopSchedulesObjectlist = new ArrayList<>();
			for(Line eachStopLine : eachStop.getLines())
			{
				for(int wayFile=2; wayFile<=2 ; wayFile++)
				{
					for(String period : periodList)
					{
						if(eachStopLine.getId() == 2 || eachStopLine.getId() == 3){
					  File file = new File(CSVFILEPATH+"L"+eachStopLine.getId()+"-D"+wayFile+"-"+period+".csv");
					   try (FileReader reader = new FileReader(file)) {
				        	//System.out.println("File found");
				            CsvReader schedules = new CsvReader(reader,';');
				           
				            schedules.readHeaders();
				            
				            Schedule oneSchedule;
				            String way="";
				            ArrayList<DateTime> stopSchedules = new ArrayList<>();
				            ArrayList<String> constraintList = new ArrayList<>();
				            while(schedules.readRecord())
				            {
				            	//System.out.println(schedules.get(0).toString().trim()+": "+schedules.get(0).compareTo("norelan"));
				            	//System.out.println(schedules.get(0).toString().trim().toLowerCase()+" == norelan");
				            	//System.out.println(schedules.get(0).toString().trim().toLowerCase().replaceAll(" ", "") +" == "+(eachStop.getLabel().toString().trim().toLowerCase().replaceAll(" ","")));
				            	if(schedules.get(0).toString().trim().toLowerCase().replaceAll(" ", "").contentEquals(eachStop.getLabel().toString().trim().toLowerCase().replaceAll(" ","")))
				            	{
				            		
				            		for(int i=1;i<schedules.getColumnCount();i++)
				            		{
				            			DateTimeFormatter formatter = DateTimeFormat.forPattern("HH:mm");
			            				DateTime time = new DateTime();
			            				
				            			if(schedules.get(i).contentEquals(CSV_NULL_VALUE) || schedules.get(i).contentEquals(CSV_SECOND_NULL_VALUE))
				            			{
				            				time =null;
				            			}
				            			else
				            			{
				            				//System.out.println(eachStop.getLabel()+": "+ file.getName());
				            				try{
				            				time = formatter.parseDateTime(schedules.get(i));
				            				stopSchedules.add(time);
				            				}
				            				catch(IllegalArgumentException invalidFormat){
				            					
				            				}
				            			}
				            				
				            		}
				            		//	
				            		//System.out.println(schedules.getColumnCount());
				            	}
				            	/*
				            	if(schedules.get(0).toString().trim().toLowerCase().contentEquals("constraints"))
				            	{
				            		
				            		for(int i=1;i<schedules.getColumnCount();i++)
				            		{
				            			if(schedules.get(i).toString().length()>0)
				            				constraintList.add(schedules.get(i));
				            			else
				            				constraintList.add("null");
				            		}
				            	}*/
				            	way = schedules.get(0).toString(); //keeps the last one
				            	
				            }
				            Boolean schoolPeriod= true;
				            if(period == "VS")
				            	schoolPeriod = false;
				            
				            oneSchedule = new Schedule(stopSchedules,constraintList,way,eachStopLine,schoolPeriod);
				           
				           scheduleRepository.save(oneSchedule);
				            stopSchedulesObjectlist.add(oneSchedule);
				            
				            
				          // System.out.println("Column Count: "+schedules.getColumnCount());
				           // System.out.println(schedules.get(0));
					   } catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
						   
						//e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				}
				}
			}
			
			Stop oneStopWithSchedules = eachStop;
			oneStopWithSchedules.setSchedules(stopSchedulesObjectlist);
			stopsWithSchedules.add(oneStopWithSchedules);
			
			
	}
		/*final int SCHEDULE_NUMBER=53;
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
		
		*/
		return stopsWithSchedules;
	}
	
	
	/**
	 * 
	 * @return stop list from json
	 * @throws IOException
	 */
	private Iterable<Stop> getStopsFromJson() throws IOException
	{
	
		File jsonStops = new File(FILEPATH);
	
		//	BufferedReader reader = Files.newBufferedReader(file.toPath());
			
			ObjectMapper mapper = new ObjectMapper();
			
			
			JsonFactory jsonFactory = mapper.getFactory();
			JsonParser stopParser = jsonFactory.createParser(jsonStops);
			File jsonStopLines = new File(DEPFILEPATH);
			JsonParser stopLinesParser = jsonFactory.createParser(jsonStopLines);
			
			JsonNode stopRootNode = stopParser.getCodec().readTree(stopParser);
			JsonNode stopLinesRootNode = stopLinesParser.getCodec().readTree(stopLinesParser);
			
			ArrayList<Stop> stops = new ArrayList<>();
			
			if ( stopRootNode.isArray() && stopLinesRootNode.isArray())
			{
				Iterator<JsonNode> stopNodeIterator = stopRootNode.iterator();
				
				while(stopNodeIterator.hasNext())
				{
					JsonNode stopNode = stopNodeIterator.next();
					long stopId = stopNode.get("id").asLong();
					String stopLabel = stopNode.get("label").asText();
					double latitude  = stopNode.get("latitude").asDouble();
					double longitude = stopNode.get("longitude").asDouble();
					GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();
					Coordinate coord = new Coordinate(latitude, longitude);
					Point point = geometryFactory.createPoint(coord);
					Stop currentStop = new Stop(stopId,stopLabel,point);
					
					HashMap<Long,Integer> orderInLine = new HashMap<>();
					Iterator<JsonNode> stopLinesRootNodeIterator = stopLinesRootNode.iterator();
					
					while(stopLinesRootNodeIterator.hasNext())
					{
						JsonNode stopLineNode = stopLinesRootNodeIterator.next();
						String way = stopLineNode.get("way").asText();
						int order = stopLineNode.get("order").asInt();
						int stopLineId = stopLineNode.get("stop_id").asInt();
						long lineId = stopLineNode.get("line_id").asLong();
						
						if(stopLineId == currentStop.getId())
						{
							if(way.equals("I"))
							{
								currentStop.setFirstDirection(way.equals("I"));
								currentStop.setSecondDirection(way.equals("O"));
								orderInLine.put(lineId, order);
							}
						}
					}
					currentStop.setOrderInLine(orderInLine);
					ArrayList<Line> linesAStopBelongs = new ArrayList<>();
					for( Entry<Long, Integer> orderInLineEntry : currentStop.getOrderInLine().entrySet())
					{
						
						Long lineId = orderInLineEntry.getKey();
						Line aLine =lineRepository.findOne(lineId);
						linesAStopBelongs.add(aLine);						
						
					}
					currentStop.setLines(linesAStopBelongs);
					
					stops.add(currentStop);
					
					
				}
				System.out.println("Stops with lines done");
				
				for(Stop oneStop : stops)
				{
					ArrayList<Long> oneStopNeighboursId = new ArrayList<>();
					for(Stop aNeighbour : stops)
					{
						for(Entry<Long,Integer> orderByLineOneStop : oneStop.getOrderInLine().entrySet())
						{
							if(aNeighbour.getOrderInLine().containsKey(orderByLineOneStop.getKey()))
							{
								if(orderByLineOneStop != null)
								{
									if(aNeighbour.getOrderInLine() != null)
									{
										if(!aNeighbour.getOrderInLine().isEmpty())
										{
										
											
											if(orderByLineOneStop.getValue() == aNeighbour.getOrderInLine().get(orderByLineOneStop.getKey())+1  /*|| orderByLineOneStop.getValue() == aNeighbour.getOrderInLine().get(orderByLineOneStop.getKey())-1 */  )
											{
												oneStopNeighboursId.add(aNeighbour.getId());
											}
										}
									}
								}
							
							}
						}
					}
					oneStop.setNeighboursId(oneStopNeighboursId);
				}
				
			}
			System.out.println(stops.isEmpty());
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
		/*
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
		*/
		/*
		for(Line l : stopByLine.keySet()){
			System.out.println("LINE: "+ l.getName());
			for(Stop s : stopByLine.get(l))
			{
				System.out.println(s.getLabel());
			}
		}*/
		/*
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
		*/
		return null;
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
