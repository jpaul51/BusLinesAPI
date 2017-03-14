package app.service;



import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Scanner;

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
import app.dao.ScheduleRepository;
import app.dao.StopRepository;
import app.model.Line;
import app.model.LinesAndStops;
import app.model.Schedule;
import app.model.Stop;


@Service
public class StopService {

	@Autowired private StopRepository stopRepository;
	@Autowired private LineRepository lineRepository;
	@Autowired private ScheduleRepository scheduleRepository;
	
	//Resources that we use to build the database
	final String FILEPATH = "src/main/resources/stopList.json";
	final String DEPFILEPATH ="src/main/resources/stopgroups.json";
	final String CSVFILEPATH="src/main/resources/";

	

	ArrayList<Stop> stops;
	/**
	 * Reads json objects to generate stops and get closest neighbours for each stop
	 */
	public void init(){
		try {
			Iterable<Stop> stops = getStopsFromJson();
			
			stops = generatesSchedules((List<Stop>) stops); 
		
			
			this.stops=(ArrayList<Stop>) stops;

			stopRepository.deleteAll();
			stopRepository.save(stops);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	

	public List<Stop> getOneStopNeighbour(String stopLabel)
	{
		Stop stop = stopRepository.findByLabelToLower(stopLabel);
		ArrayList<Stop> neighbours = new ArrayList<>();
		
		for(long neighbourId : stop.getNeighboursId())
		{
			neighbours.add(stopRepository.findOne(neighbourId));
		}
		
		return neighbours;
	}
	

	
	public Stop findStopByLabel(String label)
	{
		return stopRepository.findByLabelToLower(label);
	}
	
	
	/**
	 * Shortest way between stops using Dijkstra
	 * @param startStop 
	 * @param endStop
	 * @param now can be any dateTime, initialized to current datetime if null
	 * @return LinesAndStops object that contains all the data we need to display the shortest way between two stops
	 */
	public LinesAndStops getShortestWayBetween(Stop startStop, Stop endStop, DateTime now)
	{
		
		ArrayList<Stop> allStop;
		HashMap<Stop,Integer> distanceByStop = new HashMap<>();
		HashMap<Stop,Stop> predecessor = new HashMap<>();
		
		
		
		
		allStop =  (ArrayList<Stop>) stopRepository.findAll();
		//Dijkstra initialization
		
		
		for(Stop eachStop : allStop)
		{
			distanceByStop.put(eachStop, Integer.MAX_VALUE);
		}
		distanceByStop.put(startStop, 0);
		//end ini
		
//		Dijkstra(G,Poids,sdeb)
//		1 Initialisation(G,sdeb)
//		2 Q := ensemble de tous les nœuds
//		3 tant que Q n'est pas un ensemble vide
//		4       faire s1 := Trouve_min(Q)
//		5       Q := Q privé de s1
//		6       pour chaque nœud s2 voisin de s1
//		7           faire maj_distances(s1,s2)
		
		while(!allStop.isEmpty())
		{
			Stop s1 = findClosestNode(allStop,distanceByStop);
			allStop.remove(s1);			
			for(long neighbourId :s1.getNeighboursId())
			{
				
				Stop neighbour = stopRepository.findOne(neighbourId);
			
				if(allStop.contains(neighbour))
					updateDistances(s1, neighbour,distanceByStop,predecessor,now);
			
			}
		}
		
		
		ArrayList<Stop> way = new ArrayList<>();
		Stop aStop = endStop;
		while(!aStop.equals(startStop))
		{			
			way.add(new Stop(aStop));	
			aStop = predecessor.get(aStop);		
		}
		
		way.add(startStop);
		ArrayList<Line> linesUsed = new ArrayList<>();
		for(Stop s : way)
		{
			for(Line l : s.getLines())
			{
			if(!linesUsed.contains(l))
			{
				linesUsed.add(l);
			}
			}
		}
		LinesAndStops las = new LinesAndStops();
				las.setLines(linesUsed);
		las.setStops(way);
		return las;
	}
	
//	
//	maj_distances(s1,s2)
//	1 si d[s2] > d[s1] + Poids(s1,s2)      /* Si la distance de sdeb à s2 est plus grande que */
//	2                                      /* celle de sdeb à S1 plus celle de S1 à S2 */
//	3    alors 
//	4        d[s2] := d[s1] + Poids(s1,s2) /* On prend ce nouveau chemin qui est plus court */
//	5        prédécesseur[s2] := s1        /* En notant par où on passe */
//	
	
	private void updateDistances(Stop a, Stop b, HashMap<Stop, Integer> distanceByStop, HashMap<Stop, Stop> predecessor, DateTime now)
	{
		
		if(distanceByStop.get(b) > distanceByStop.get(a) + getDistanceBetweenStops(a, b, now))
		{
			
			distanceByStop.put(b,  distanceByStop.get(a) + getDistanceBetweenStops(a, b,now));
			predecessor.put(b, a);
			
		}
		
	}
	
	
	private Stop findClosestNode(ArrayList<Stop> remainingStops, HashMap<Stop, Integer> distanceByStop)
	{
		int min = Integer.MAX_VALUE;
		Stop resultStop=null;
		
		for(Stop oneStop : remainingStops)
		{
			if(distanceByStop.get(oneStop) <= min)
			{
				min = distanceByStop.get(oneStop);
				resultStop = oneStop;
			}
		}
		return resultStop;
	}
	
	/**
	 * Calculates distance between two stops.
	 * @param firstStop
	 * @param secondStop
	 * @param timeWeAreOnFirstStop
	 * @return distance between two stops
	 */
	private int getDistanceBetweenStops(Stop firstStop, Stop secondStop, DateTime timeWeAreOnFirstStop)
	{
		if(!firstStop.getNeighboursId().contains(secondStop.getId()))
		{
			throw new RuntimeException("Stops are not neighbours");
		}
		
		DateTime earliestTime=new DateTime(3000,1,1,1,1);
		Line earliestTimeLine=null;
		
		for(Schedule oneLineSchedule : firstStop.getSchedules())
		{
			if(secondStop.getLines().contains(oneLineSchedule.getLine()))
			{
				
				
				for(DateTime oneLineScheduleTime : oneLineSchedule.getSchedules())
				{
					
					
					if(oneLineScheduleTime.getHourOfDay() >= timeWeAreOnFirstStop.getHourOfDay()  )
					{
						if( (oneLineScheduleTime.getHourOfDay()==timeWeAreOnFirstStop.getHourOfDay() && oneLineScheduleTime.getMinuteOfHour()>= timeWeAreOnFirstStop.getMinuteOfHour()) || oneLineScheduleTime.getHourOfDay() > timeWeAreOnFirstStop.getHourOfDay() )
						{
							if(oneLineScheduleTime.isBefore(earliestTime))
							{
								earliestTime = oneLineScheduleTime;
							
								earliestTimeLine  = oneLineSchedule.getLine();
								break;		
							}
						}
						
					}
				}
				
			}
		}
		
		for( Schedule secondStopSchedule :  secondStop.getSchedules())
		{
			if(secondStopSchedule.getLine().equals(earliestTimeLine))
			{
				for(DateTime secondStopTime : secondStopSchedule.getSchedules())
				{
					if(secondStopTime.getHourOfDay() >= earliestTime.getHourOfDay()  )
					{
						if( (secondStopTime.getHourOfDay()==earliestTime.getHourOfDay() && secondStopTime.getMinuteOfHour()>= earliestTime.getMinuteOfHour()) || secondStopTime.getHourOfDay() > earliestTime.getHourOfDay() )
						{
							return secondStopTime.getMinuteOfDay() - earliestTime.getMinuteOfDay();
						}
					}
				}
			}
			
			
		}
		
		
		
		return Integer.MAX_VALUE;
		
		
		
	}
	


	public Stop getStopBylabel(String label)
	{
		return stopRepository.findByLabelToLower(label);
	}


	// returns closest stop from location
	public Stop getClosestStop(Point p)
	{
		return stopRepository.findClosestStop(p).get(0);
	}


	/**
	 * Read schedules from files and updates the stops
	 * @param stopList
	 * @return Stop list with schedules
	 */
	private Iterable<Stop> generatesSchedules(List<Stop> stopList)
	{

		final String CSV_NULL_VALUE = "....";
		final String CSV_SECOND_NULL_VALUE = "|";
		ArrayList<Stop> stopsWithSchedules = new ArrayList<>();

	
		ArrayList<String> periodList = new ArrayList<>();
		periodList.add("PS");
	

		for(Stop eachStop : stopList)

		{

			ArrayList<Schedule> stopSchedulesObjectlist = new ArrayList<>();
			for(Line eachStopLine : eachStop.getLines())
			{
				for(int wayFile=1; wayFile<=2 ; wayFile++)
				{
					for(String period : periodList)
					{
						if((eachStopLine.getId() == 1 || eachStopLine.getId() == 2 || eachStopLine.getId() == 3 || eachStopLine.getId() == 4) )
						{
							File file = new File(CSVFILEPATH+"L"+eachStopLine.getId()+"-D"+wayFile+"-"+period+".csv");
							try (FileReader reader = new FileReader(file)) {
							
								CsvReader schedules = new CsvReader(reader,';');

								schedules.readHeaders();

								Schedule oneSchedule;
								String way="";
								ArrayList<DateTime> stopSchedules = new ArrayList<>();
								ArrayList<String> constraintList = new ArrayList<>();
								while(schedules.readRecord())
								{
									
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
												
												try{
													time = formatter.parseDateTime(schedules.get(i));
													stopSchedules.add(time);
												}
												catch(IllegalArgumentException invalidFormat){

												}
											}

										}
										
									}
							
									way = schedules.get(0).toString(); //keeps the last one

								}
								Boolean schoolPeriod= true;
								if(period == "VS")
									schoolPeriod = false;

								oneSchedule = new Schedule(stopSchedules,constraintList,way,eachStopLine,schoolPeriod);
								oneSchedule.setLineItBelongs(eachStopLine.getId());
								scheduleRepository.save(oneSchedule);
								stopSchedulesObjectlist.add(oneSchedule);


								
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

		HashMap<Long,Stop> stopsById = new HashMap<>();

		if ( stopRootNode.isArray() && stopLinesRootNode.isArray())
		{
			Iterator<JsonNode> stopNodeIterator = stopRootNode.iterator();

			while(stopNodeIterator.hasNext())
			{
				JsonNode stopNode = stopNodeIterator.next();
				Stop currentStop;
				HashMap<String,Integer> orderByWay = new HashMap<>();
				HashMap<Long,HashMap<String,Integer>> orderInLine;
				long stopId = stopNode.get("id").asLong();
				
				if(stopsById.containsKey(stopId))
				{
					currentStop = stopsById.get(stopId);
					orderInLine = currentStop.getOrderInLineByWay();
				}
				else
				{
					orderInLine = new HashMap<>();
					String stopLabel = stopNode.get("label").asText().trim();
					double latitude  = stopNode.get("latitude").asDouble();
					double longitude = stopNode.get("longitude").asDouble();
					GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();
					Coordinate coord = new Coordinate(latitude, longitude);
					Point point = geometryFactory.createPoint(coord);
					currentStop = new Stop(stopId,stopLabel,point);
				}
				//HashMap<Long,Integer> orderInLine = new HashMap<>();
				Iterator<JsonNode> stopLinesRootNodeIterator = stopLinesRootNode.iterator();

				while(stopLinesRootNodeIterator.hasNext())
				{
					JsonNode stopLineNode = stopLinesRootNodeIterator.next();
					String way = stopLineNode.get("way").asText();
					int order = stopLineNode.get("order").asInt();
					int lineStopId = stopLineNode.get("stop_id").asInt();
					long lineId = stopLineNode.get("line_id").asLong();

					if(lineStopId == currentStop.getId())
					{
						
						//if(way.equals("I"))
						
						if(orderInLine.containsKey(lineId))
						{
							orderByWay = orderInLine.get(lineId);						
						}
						else
						{
							orderByWay = new HashMap<>();
						}
						orderByWay.put(way, order);
						orderInLine.put(lineId, orderByWay);

					}
				}
				currentStop.setOrderInLineByWay(orderInLine);
				ArrayList<Line> linesAStopBelongs = new ArrayList<>();
				ArrayList<Long> linesIdAStopBelongs = new ArrayList<>();
				for( Entry<Long, HashMap<String,Integer>> orderInLineEntry : currentStop.getOrderInLineByWay().entrySet())
				{

					Long lineId = orderInLineEntry.getKey();
					Line aLine =lineRepository.findOne(lineId);
					ArrayList<Long> linesIdWeManage = new ArrayList<>(Arrays.asList(new Long(1), new Long(2), new Long(3), new Long(4)));
					
					if(linesIdWeManage.contains(aLine.getId()))//line 5 is loop, we'll manage this later, other lines don't have schedules yet
						linesAStopBelongs.add(aLine);						
						linesIdAStopBelongs.add(aLine.getId());
				}
				currentStop.setLines(linesAStopBelongs);
				currentStop.setLinesId(linesIdAStopBelongs);
				if(!linesAStopBelongs.isEmpty()) // We don't add a stop that belongs to a non managed line
					stopsById.put(currentStop.getId(),currentStop);


			}
		

			for(Stop oneStop : stopsById.values())
			{
				ArrayList<Long> oneStopNeighboursId = new ArrayList<>();
				for(Stop aNeighbour : stopsById.values())
				{
					if(!oneStopNeighboursId.contains(aNeighbour.getId()))
					{
						for(Entry<Long,HashMap<String,Integer>> orderByLineOneStop : oneStop.getOrderInLineByWay().entrySet())
						{
							if(aNeighbour.getOrderInLineByWay().containsKey(orderByLineOneStop.getKey()))
							{
								if(orderByLineOneStop != null)
								{
									if(aNeighbour.getOrderInLineByWay() != null)
									{
										if(!aNeighbour.getOrderInLineByWay().isEmpty())
										{
	
											for(Entry<String,Integer> oneWay: orderByLineOneStop.getValue().entrySet())
											{
												if(oneWay !=null)
												{
													if(orderByLineOneStop.getValue().get(oneWay.getKey()) != null)
													{
														if(aNeighbour.getOrderInLineByWay().get(orderByLineOneStop.getKey()).get(oneWay.getKey()) != null)
														{
															if(orderByLineOneStop.getValue().get(oneWay.getKey()) == aNeighbour.getOrderInLineByWay().get(orderByLineOneStop.getKey()).get(oneWay.getKey())+1  /*|| orderByLineOneStop.getValue() == aNeighbour.getOrderInLine().get(orderByLineOneStop.getKey())-1 */  )
															{
																if(!oneStopNeighboursId.contains(aNeighbour.getId()))
																{
																oneStopNeighboursId.add(aNeighbour.getId());
																}
															}
														}
													}
												}
											}
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
		
		
		List<Stop> returnList = new ArrayList<Stop>(stopsById.values());
		
		return returnList;
	}
	/**
	 * 
	 * @return list of all stops
	 */
	public List<Stop> getAllStops(){
		return (List<Stop>) stopRepository.findAll();
	}

	
	
	/**
	 * Command line method to check if everything is correctly loaded
	 */
	public void debug()
	{
		ArrayList<Stop> stops = (ArrayList<Stop>) stopRepository.findAll();
		 Scanner scanner = new Scanner(System.in);
		 
			boolean ok = true;
			while(ok)
			{
				String reponse = scanner.nextLine();
				switch(reponse){
					case "n":
					{
						ok=false;
						break;
					}
					case "exit":
					{
						ok = false;
					}
					default:
					{
						 Stop s = stopRepository.findByLabelToLower(reponse);
						 if(s !=null)
						 {
						 System.out.println("----------------------------------------------------");
							System.out.println("STOP: "+s.getLabel() );
							System.out.println("ID: "+s.getId() );
							System.out.println("LINES: ");
							for(Line l : s.getLines())
								System.out.print(l.getId()+" ");
							System.out.println();
							System.out.println("NEIGHBOURS");
							for(long nId : s.getNeighboursId())
							{
								Stop n =stopRepository.findOne(nId);
								System.out.println("\tID: "+n.getId() +" : " + n.getLabel());
							}
							System.out.println(s.getSchedules().size()+" SCHEDULES");
							for(Schedule sc : s.getSchedules())
							{
								System.out.println("\tLine: "+sc.getLine().getId()+", way: "+ sc.getway()+", dates: "+ sc.getSchedules().size());
							}
						break;
						 }
						 else
						 {
							 System.out.println("NOT FOUND");
						 }
					}
				}
					
			}
		
	
		scanner.close();
	}
	
	
	
	/**
	 * This could be used if we don't have the order of the stops
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

		
		LineString dist = (LineString) line.extractLine(s1Location, s2Location);


		return distance;


	}


}
