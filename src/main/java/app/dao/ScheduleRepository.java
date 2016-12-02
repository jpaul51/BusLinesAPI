package app.dao;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;

import app.model.Schedule;

@Component
public interface ScheduleRepository extends CrudRepository<Schedule,Long> {
	
	

}
