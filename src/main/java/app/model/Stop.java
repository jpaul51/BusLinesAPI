package app.model;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Type;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.vividsolutions.jts.geom.Point;
import app.model.Line;
@Entity
@Table(name="busstop")
public class Stop {

	@Id
	long id;
	String label;

 
	Point point;
	
	Boolean firstDirection;
	Boolean secondDirection;
	  
	@JsonIgnore
	HashMap<Long,Integer> orderInLine;
	
	//We can access lines a stop belongs to 
	@ManyToMany
	@JsonIgnore
	List<Line> lines;
	
	//neighbours from lines the stop belongs to
	@JsonIgnore
	@ElementCollection(targetClass=Long.class)
	List<Long> neighboursId;
	
	@OneToMany(cascade={CascadeType.REMOVE})
	List<Schedule> schedules;
	
	public Stop()
	{
		
	}


	public Stop(long id, String label, Point point, Boolean firstDirection, Boolean secondDirection, List<Line> lines,
			List<Long> neighbours, List<Schedule> schedules) {
		super();
		this.id = id;
		this.label = label;
		this.point = point;
		this.firstDirection = firstDirection;
		this.secondDirection = secondDirection;
		this.lines = lines;
		this.neighboursId = neighbours;
		this.schedules = schedules;
	}



	public Stop(long id, String label, Point point, Boolean firstDirection, Boolean secondDirection,
			HashMap<Long, Integer> orderInLine, List<Line> lines, List<Long> neighbours,
			List<Schedule> schedules) {
		super();
		this.id = id;
		this.label = label;
		this.point = point;
		this.firstDirection = firstDirection;
		this.secondDirection = secondDirection;
		this.orderInLine = orderInLine;
		this.lines = lines;
		this.neighboursId = neighbours;
		this.schedules = schedules;
	}



	public Stop(long id, String label, Point point, List<Line> lines, List<Long> neighbours,
			List<Schedule> schedules) {
		super();
		this.id = id;
		this.label = label;
		this.point = point;
		this.lines = lines;
		this.neighboursId = neighbours;
		this.schedules = schedules;
	}



	public Stop(long id, String label, Point point, List<Line> lines, List<Long> neighbours) {
		super();
		this.id = id;
		this.label = label;
		this.point = point;
		this.lines = lines;
		this.neighboursId = neighbours;
	}



	public Stop(long id, String label, Point point, List<Line> lines) {
		super();
		this.id = id;
		this.label = label;
		this.point = point;
		this.lines = lines;
	}
	


	public Stop(long id, String label, Point point) {
		super();
		this.id = id;
		this.label = label;
		this.point = point;
	}



	public Boolean getFirstDirection() {
		return firstDirection;
	}



	public void setFirstDirection(Boolean firstDirection) {
		this.firstDirection = firstDirection;
	}



	public Boolean getSecondDirection() {
		return secondDirection;
	}



	public void setSecondDirection(Boolean secondDirection) {
		this.secondDirection = secondDirection;
	}



	public List<Schedule> getSchedules() {
		return schedules;
	}



	public void setSchedules(List<Schedule> schedules) {
		this.schedules = schedules;
	}



	public long getId() {
		return id;
	}



	public void setId(long id) {
		this.id = id;
	}



	public HashMap<Long, Integer> getOrderInLine() {
		return orderInLine;
	}



	public void setOrderInLine(HashMap<Long, Integer> orderInLine) {
		this.orderInLine = orderInLine;
	}



	public String getLabel() {
		return label;
	}



	public void setLabel(String label) {
		this.label = label;
	}



	public Point getPoint() {
		return point;
	}



	public void setPoint(Point point) {
		this.point = point;
	}



	public List<Line> getLines() {
		return lines;
	}



	public void setLines(List<Line> lines) {
		this.lines = lines;
	}


	public List<Long> getNeighboursId() {
		return neighboursId;
	}


	public void setNeighboursId(List<Long> neighboursId) {
		this.neighboursId = neighboursId;
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((firstDirection == null) ? 0 : firstDirection.hashCode());
		result = prime * result + (int) (id ^ (id >>> 32));
		result = prime * result + ((label == null) ? 0 : label.hashCode());
		result = prime * result + ((lines == null) ? 0 : lines.hashCode());
		result = prime * result + ((neighboursId == null) ? 0 : neighboursId.hashCode());
		result = prime * result + ((orderInLine == null) ? 0 : orderInLine.hashCode());
		result = prime * result + ((point == null) ? 0 : point.hashCode());
		result = prime * result + ((schedules == null) ? 0 : schedules.hashCode());
		result = prime * result + ((secondDirection == null) ? 0 : secondDirection.hashCode());
		return result;
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Stop other = (Stop) obj;
		if (firstDirection == null) {
			if (other.firstDirection != null)
				return false;
		} else if (!firstDirection.equals(other.firstDirection))
			return false;
		if (id != other.id)
			return false;
		if (label == null) {
			if (other.label != null)
				return false;
		} else if (!label.equals(other.label))
			return false;
		if (lines == null) {
			if (other.lines != null)
				return false;
		} else if (!lines.equals(other.lines))
			return false;
		if (neighboursId == null) {
			if (other.neighboursId != null)
				return false;
		} else if (!neighboursId.equals(other.neighboursId))
			return false;
		if (orderInLine == null) {
			if (other.orderInLine != null)
				return false;
		} else if (!orderInLine.equals(other.orderInLine))
			return false;
		if (point == null) {
			if (other.point != null)
				return false;
		} else if (!point.equals(other.point))
			return false;
		if (schedules == null) {
			if (other.schedules != null)
				return false;
		} else if (!schedules.equals(other.schedules))
			return false;
		if (secondDirection == null) {
			if (other.secondDirection != null)
				return false;
		} else if (!secondDirection.equals(other.secondDirection))
			return false;
		return true;
	}









	







	
	
}
