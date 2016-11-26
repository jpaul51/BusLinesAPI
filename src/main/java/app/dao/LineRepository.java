package app.dao;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;

import app.model.Line;

@Component
public interface LineRepository extends CrudRepository<Line, Long> {

    Line findByName(String name);
    
}