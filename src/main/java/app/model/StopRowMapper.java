package app.model;

import java.sql.ResultSet;
import java.sql.SQLException;


import org.postgresql.util.PGobject;
import org.springframework.jdbc.core.RowMapper;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.io.WKBReader;

public class StopRowMapper implements RowMapper{

	@Override
	public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
		// TODO Auto-generated method stub
		Point p = (Point)rs.getObject("point");
		
		
		
	
		
		 
		GeometryFactory  builder = new GeometryFactory  (  );
		com.vividsolutions.jts.geom.Point point = (com.vividsolutions.jts.geom.Point) builder.createPoint(new Coordinate(p.getX(),p.getY()));
		
		//PGobject point = (Point) builder.createPoint( new Coordinate(latitude, longitude));
		
		Stop stop = new Stop(rs.getLong("id"),rs.getString("label"),point);
	
		return stop;
	
	}

}
