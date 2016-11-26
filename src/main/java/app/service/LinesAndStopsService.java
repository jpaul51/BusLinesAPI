package app.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import app.model.LinesAndStops;

@Service
public class LinesAndStopsService {

	@Autowired
	LineService lineService;
	@Autowired
	StopService stopService;
	
	/**
	 * 
	 * @return all lines and stops
	 */
	public LinesAndStops getAllLinesAndStops(){
		LinesAndStops linesAndStops = new LinesAndStops();
		linesAndStops.setLines(lineService.getAllLines());
		linesAndStops.setStops(stopService.getAllStops());
		
		return linesAndStops;
	}
	
	
}
