package app.dao;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Component;

import com.vividsolutions.jts.geom.Point;

import app.model.Stop;

@Component
public interface StopRepository extends CrudRepository<Stop, Long> {

	Stop findByLabel(String label);
	@Query(value ="SELECT s FROM Stop s ORDER BY distance(:point,s.point) "
	 	)
	List<Stop> findClosestStop(@Param("point") Point point);
	
	
}
