package app.service;

import org.geotools.geometry.GeometryBuilder;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.postgresql.util.PGobject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

import app.model.Stop;
import app.model.StopRowMapper;

@Service
public class ClosestStopService {

	@Autowired
    JdbcTemplate jdbcTemplate;
	
	public Stop getColsestStop(double latitude, double longitude)
	{
		GeometryFactory  builder = new GeometryFactory  (  );
		
		//PGobject point = (Point) builder.createPoint( new Coordinate(latitude, longitude));
		
		String sql = "select * from busstop stop order by ST_Distance(ST_GeomFromText('POINT("+latitude+" "+longitude+" )',4326),stop.point) limit 1;";
		 		
		Stop s = (Stop)jdbcTemplate.queryForObject(
				sql,
				new StopRowMapper());
		
		return s;
	}
	
}
