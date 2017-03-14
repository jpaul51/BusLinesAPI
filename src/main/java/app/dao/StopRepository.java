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
	
	/*
	 * Finds a stop by label. We make sure to ignorecase. There are no accent in our database
	 */
	@Query(value="SELECT s FROM Stop s WHERE lower(s.label)=lower(:label)")
	Stop findByLabelToLower(@Param("label")String label);
	
	/*
	 * Finds closest Stop using postgis extension
	 */
	@Query(value ="SELECT s FROM Stop s ORDER BY distance(:point,s.point) "
	 	)
	List<Stop> findClosestStop(@Param("point") Point point);
	
	
}
