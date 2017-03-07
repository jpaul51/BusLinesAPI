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
			stops = generatesSchedules((List<Stop>) stops); 
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
	HashMap<Integer,ArrayList<Stop>> stopRoads ;
	int currentRoad=0;

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
	
	public int minutesINeedToGoThereSameLine(String startStopLabel, String destinationStopLabel)
	{
		Stop startStop = stopRepository.findByLabelToLower(startStopLabel);
		Stop destinationStop = stopRepository.findByLabel(destinationStopLabel);
		
		
		Line lineWeUse=null;
		
		//if stops are  on the same line
		if(!Collections.disjoint(startStop.getLines(), destinationStop.getLines()))
		{
			
			for(Line line : startStop.getLines())
			{
				if(destinationStop.getLines().contains(line))
				{
					lineWeUse = line;
				}
			}
			
			HashMap<String,Stop> stopsOnRaodByWay = new HashMap<>();
			
			for(long neighbourId : startStop.getNeighboursId())
			{
				Stop oneNeighbour = stopRepository.findOne(neighbourId);
				if(oneNeighbour.getLines().contains(lineWeUse))
				{
					//TODO: parcours sur une ligne
				}
			}
			
			
		}
		
		
		return -1;
	}
	
	public Stop findStopByLabel(String label)
	{
		return stopRepository.findByLabelToLower(label);
	}
	
	
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
			System.out.println(s1.getLabel());
			allStop.remove(s1);
			System.out.println("NEIGHBOURS: "+s1.getNeighboursId().size());
			for(long neighbourId :s1.getNeighboursId())
			{
				
				Stop neighbour = stopRepository.findOne(neighbourId);
				System.out.println("NEIGHBOUR LABEL: "+neighbour.getLabel());
				if(allStop.contains(neighbour))
					updateDistances(s1, neighbour,distanceByStop,predecessor,now);
				System.out.println(distanceByStop.get(neighbour));
			}
		}
		
		System.out.println("NEXT");
		ArrayList<Stop> way = new ArrayList<>();
		Stop aStop = endStop;
		System.out.println("END STOP: "+endStop.getLabel());
		while(!aStop.equals(startStop))
		{			
			way.add(new Stop(aStop));
			
			aStop = predecessor.get(aStop);
			System.out.println("PREDECESSOR: "+aStop.getLabel());
			
		}
		
		way.add(startStop);
		System.out.println("WAY");
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
			System.out.println(s.getLabel());
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
			System.out.println(distanceByStop.get(b));
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
				System.out.println(distanceByStop.get(oneStop));
				resultStop = oneStop;
			}
		}
		return resultStop;
	}
	
	
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
					//System.out.println(oneLineScheduleTime +" : "+ timeWeAreOnFirstStop);
					
					if(oneLineScheduleTime.getHourOfDay() >= timeWeAreOnFirstStop.getHourOfDay()  )
					{
						if( (oneLineScheduleTime.getHourOfDay()==timeWeAreOnFirstStop.getHourOfDay() && oneLineScheduleTime.getMinuteOfHour()>= timeWeAreOnFirstStop.getMinuteOfHour()) || oneLineScheduleTime.getHourOfDay() > timeWeAreOnFirstStop.getHourOfDay() )
						{
							if(oneLineScheduleTime.isBefore(earliestTime))
							{
								earliestTime = oneLineScheduleTime;
								//System.out.println("EARLIEST TIME: "+earliestTime);
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
	
	
	//Method that finds all available roads to one destination stop
	private void checkNeighbours(Stop currentStop, Stop destinationStop,ArrayList<Stop> roadPrefix )
	{
		System.out.println(currentRoad+": "+currentStop.getLabel());
		ArrayList<Stop> oneRoad=null;
		if(stopRoads.get(currentRoad)==null)
		{
			oneRoad = new ArrayList<>();
			if(roadPrefix!=null)
				oneRoad.addAll(roadPrefix);
		}
		else
		{
			oneRoad = stopRoads.get(currentRoad);

		}
		oneRoad.add(currentStop);
		stopRoads.put(currentRoad,oneRoad);

		if(!currentStop.equals(destinationStop))
		{
			for(Long oneNeighbourId : currentStop.getNeighboursId())
			{
			
				Stop oneNeighbour = stopRepository.findOne(oneNeighbourId);
				ArrayList<Stop> roadPrefix2 = new ArrayList<>();
				if(roadPrefix!=null)
					roadPrefix2.addAll(roadPrefix);
				roadPrefix2.add(currentStop);
				
				boolean shouldRecurse = true;
				if(stopRoads.get(currentRoad) != null)
					if(stopRoads.get(currentRoad).contains(oneNeighbour))
						shouldRecurse=false;
				
				if(shouldRecurse)
					checkNeighbours( oneNeighbour,  destinationStop, roadPrefix2 );

				currentRoad +=1;

			}
		}

	}



	public int howMuchMinutesDoINeedToGetThere(String startStopLabel, String destinationStopLabel,DateTime now)
	{
		System.out.println("FIND STOPS");;
		Stop startStop =stopRepository.findByLabelToLower(startStopLabel);
		Stop destinationStop = stopRepository.findByLabel(destinationStopLabel);





		System.out.println("CHECKNEIGHBOURS");	
		stopRoads = new HashMap<>();
		checkNeighbours(startStop, destinationStop,null);



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

		ArrayList<Integer> eachRoadDuration = new ArrayList<>();
		for(ArrayList<Stop> oneRoad : stopRoads.values())
		{
			//Some roads end without reaching the destination
			if(oneRoad.contains(destinationStop))
			{

				ArrayList<Integer> eachDuration = new ArrayList<>();
				Stop firstStop = oneRoad.get(0);
				HashMap<Long,Integer> earliestScheduleIndexForEachLine = new HashMap<>();
				for(Schedule oneSchedule : firstStop.getSchedules())
				{
					if(oneSchedule.getSchoolPeriod())
					{
						int earliestIndex = getEarliestTimeIndex(oneSchedule, now, now);
						earliestScheduleIndexForEachLine.put(oneSchedule.getLine().getId(), earliestIndex);
						//if the destination stop is on the line of the start stop it's easy
						int durationInSeconds=0;
						int previousTimeInSecond=0;
						//System.out.println(oneSchedule.getSchedules().size());
						//System.out.println("EARLIEST INDEX: "+earliestIndex);
						if(earliestIndex !=-1)
						{
							if(destinationStop.getLines().contains(oneSchedule.getLine()))
							{
								for(Stop eachStopOnRoad : oneRoad)
								{
									System.out.println("Stop Schedule: "+eachStopOnRoad.getLabel());
									for(Schedule eachSchedule : eachStopOnRoad.getSchedules())
									{
										if(eachSchedule.getLine().equals(oneSchedule.getLine()) && eachSchedule.getSchoolPeriod())
										{
											//System.out.println(eachSchedule.getSchedules().size());
											DateTime earliestTime = eachSchedule.getSchedules().get(earliestIndex);
											System.out.println("EARLIEST DATE: "+earliestTime+": " + earliestTime.getMinuteOfDay());
											if(previousTimeInSecond==0)
												previousTimeInSecond = earliestTime.getMinuteOfDay();
											else
											{
												durationInSeconds = previousTimeInSecond - earliestTime.getMinuteOfDay();
											}
											//System.out.println(tripDuration.getMillis());
										}
									}
								}
								System.out.println("ADD DURATION");
								eachDuration.add(durationInSeconds);
							}
						}
					}
				}

				int shortesDuration = getShortestTime(eachDuration);
				eachRoadDuration.add(shortesDuration);
			}


		}


		return eachRoadDuration.get(0);

	}


	private int getShortestTime(ArrayList<Integer> durations)
	{
		int shortestDuration=-1;
		for(int oneDuration : durations)
		{



			if(oneDuration < shortestDuration || shortestDuration == -1)
			{
				shortestDuration = oneDuration;
			}
		}
		return shortestDuration;

	}





	private int getEarliestTimeIndex(Schedule oneSchedule,DateTime now, DateTime timeWeStartAt)
	{
		Iterator<DateTime> timeIterator = oneSchedule.getSchedules().iterator();
		int earliestDateIndex=-1;
		int currentIndex=0;
		boolean found=false;

		while(timeIterator.hasNext() && !found)
		{
			DateTime eachTime = timeIterator.next();
			System.out.println(eachTime +"   :    "+now);

			if(eachTime.getHourOfDay() >= now.getHourOfDay()  )
			{
				if( (eachTime.getHourOfDay()==now.getHourOfDay() && eachTime.getMinuteOfHour()>= now.getMinuteOfHour()) || eachTime.getHourOfDay() > now.getHourOfDay() )
				{

					earliestDateIndex = currentIndex;
					found=true;
				}
			}
			currentIndex++;
		}

		return earliestDateIndex;
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



	private Iterable<Stop> generatesSchedules(List<Stop> stopList)
	{

		final String CSV_NULL_VALUE = "....";
		final String CSV_SECOND_NULL_VALUE = "|";
		ArrayList<Stop> stopsWithSchedules = new ArrayList<>();

		//System.out.println(CSVFILEPATH+"L1-D1-PS.csv");
		ArrayList<String> periodList = new ArrayList<>();
		periodList.add("PS");
		//periodList.add("VS");

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
									System.out.println(schedules.get(0).toString().trim().toLowerCase().replaceAll(" ", "") +" == "+(eachStop.getLabel().toString().trim().toLowerCase().replaceAll(" ","")));
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
								oneSchedule.setLineItBelongs(eachStopLine.getId());
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
			System.out.println("Stops with lines done");

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
		
		//System.out.println(stops.isEmpty());
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
		
		 
//		for(Stop s : stops)
//		{
//			System.out.println("----------------------------------------------------");
//			System.out.println("STOP: "+s.getLabel() );
//			System.out.println("ID: "+s.getId() );
//			System.out.println("LINES: ");
//			for(Line l : s.getLines())
//				System.out.print(l.getId()+" ");
//			System.out.println();
//			System.out.println("NEIGHBOURS");
//			for(long nId : s.getNeighboursId())
//			{
//				Stop n =stopRepository.findOne(nId);
//				System.out.println("\tID: "+n.getId() +" : " + n.getLabel());
//			}
//			System.out.println(s.getSchedules().size()+" SCHEDULES");
//			for(Schedule sc : s.getSchedules())
//			{
//				System.out.println("\tLine: "+sc.getLine().getId()+", way: "+ sc.getway()+", dates: "+ sc.getSchedules().size());
//			}
//			
//			boolean ok = true;
//			while(ok)
//			{
//				String reponse = scanner.nextLine();
//				switch(reponse){
//					case "n":
//					{
//						ok=false;
//						break;
//					}
//					default:
//					{
//						break;
//					}
//				}
//					
//			}
//		}
//		
		scanner.close();
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
