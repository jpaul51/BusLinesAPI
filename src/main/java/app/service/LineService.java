package app.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.geotools.geometry.jts.GeometryBuilder;
import org.geotools.kml.KML;
import org.geotools.kml.KMLConfiguration;
import org.geotools.xml.Parser;
import org.geotools.xml.PullParser;
import org.opengis.feature.simple.SimpleFeature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.xml.sax.SAXException;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;

import app.dao.LineRepository;
import app.model.Line;

@Service
public class LineService {

@Autowired
private LineRepository lineRepository;
	
	/**
	 * Reads json files to generate db
	 */
	public void init(){
		try {
			Iterable<Line> lines = getLinesFromJson();
			lineRepository.deleteAll();
			lineRepository.save(lines);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * returns lines objects frm json
	 */
	final String RESSOURCES_PATH="src/main/resources/";
	final String FILEPATH = "src/main/resources/lines.json";
	private Iterable<Line> getLinesFromJson() throws IOException
	{
		List<Line> lines=null;
		File file = new File(FILEPATH);
		BufferedReader reader = Files.newBufferedReader(file.toPath());
		
	
		ObjectMapper mapper = new ObjectMapper();
		mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

		 try {
			 lines = Arrays.asList(mapper.readValue(reader, Line[].class));
		} catch (JsonParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
		 String kmlFilePath = null;
		 Iterator<Line> lineIterator = lines.iterator();
		 try {
			 int countLine=0;
		 while(lineIterator.hasNext())
		 {
			 
			 kmlFilePath =RESSOURCES_PATH+ lineIterator.next().getPathFile();
			 File kmlFile = new File(kmlFilePath);
			 PullParser parser = new PullParser(new KMLConfiguration(),new FileInputStream(kmlFile) ,KML.Placemark);
			  int count = 0;
			  SimpleFeature f=null;
			  	
			  ArrayList<LineString> lineStrings = new ArrayList();
		        while ( (f = (SimpleFeature) parser.parse() )!= null) {
		           //f = (SimpleFeature )parser.parse();
		        	
		           LineString ls = (LineString)f.getDefaultGeometry();
		           lineStrings.add(ls);
		        	//System.out.println("GEO: "+(LineString)f.getDefaultGeometry());
		        }

		        
		        MultiLineString multiLine =null;
		        GeometryFactory factory = new GeometryFactory();
		        GeometryBuilder builder = new GeometryBuilder(factory);
		        if(lineStrings.size()==1)
		        {
		        	multiLine=builder.multiLineString(lineStrings.get(0));
		        }
		        else
		        {
		        GeometryCollection geometryCollection =
		             (GeometryCollection) factory.buildGeometry( lineStrings );
		        
		         multiLine = (MultiLineString)geometryCollection.union();
		        }
		      //  System.out.println(multiLine.toString());
		 lines.get(countLine).setLines(multiLine);
		  countLine++;
		 }
		 }catch(Exception e){
			 System.out.println("ERROR: "+e.getMessage());
			 e.printStackTrace();
		};
		
		//System.out.println("TEST: "+lines.get(0).getLines().toString());
		return lines;
	}
	
	
	public List<Line> getAllLines(){
		return (List<Line>) lineRepository.findAll();
	}
	
	
	
	
	
	
	
	
}
