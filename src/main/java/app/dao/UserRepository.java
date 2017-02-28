package app.dao;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Component;

import app.model.CustomUser;


@Component
public interface UserRepository extends CrudRepository<CustomUser, String> {

	
	
	
	
	
}
