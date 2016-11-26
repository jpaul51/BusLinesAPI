package app.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;


public class StopLineDeserializer  extends StdDeserializer<Long>{

	public StopLineDeserializer() {
		super(StopLineDeserializer.class);
		// TODO Auto-generated constructor stub
	}

	@Override
	public Long deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
		// TODO Auto-generated method stub
		 JsonNode node = jp.getCodec().readTree(jp);
	//	 node = node.findValue("id");
		 
		
		 long idStop =  (long) ( node.get("id")).asLong();
		node = node.findPath("lines");
		
		 //long idLines = node.get("lines.lines.id").asLong();
		
		System.out.println("ID: "+idStop );
		
		return idStop;
	}

	
	
	
	
	
	
	
	
	
	
}
