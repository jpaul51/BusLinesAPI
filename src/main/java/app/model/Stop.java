package app.model;


import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
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
	
	
	  
	//We can access lines a stop belongs to 
	@ManyToMany
	@JsonIgnore
	List<Line> lines;
	
	//neighbours from lines the stop belongs to
	@JsonIgnore
	@OneToMany
	List<StopNeighbour> neighbours;
	
	@OneToMany(cascade={CascadeType.REMOVE})
	List<Schedule> schedules;
	
	public Stop()
	{
		
	}



	public List<StopNeighbour> getNeighbours() {
		return neighbours;
	}



	public void setNeighbours(List<StopNeighbour> neighbours) {
		this.neighbours = neighbours;
	}



	public Stop(long id, String label, Point point, List<Line> lines, List<StopNeighbour> neighbours,
			List<Schedule> schedules) {
		super();
		this.id = id;
		this.label = label;
		this.point = point;
		this.lines = lines;
		this.neighbours = neighbours;
		this.schedules = schedules;
	}



	public Stop(long id, String label, Point point, List<Line> lines, List<StopNeighbour> neighbours) {
		super();
		this.id = id;
		this.label = label;
		this.point = point;
		this.lines = lines;
		this.neighbours = neighbours;
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



	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (id ^ (id >>> 32));
		result = prime * result + ((label == null) ? 0 : label.hashCode());
		result = prime * result + ((lines == null) ? 0 : lines.hashCode());
		result = prime * result + ((neighbours == null) ? 0 : neighbours.hashCode());
		result = prime * result + ((point == null) ? 0 : point.hashCode());
		result = prime * result + ((schedules == null) ? 0 : schedules.hashCode());
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
		if (neighbours == null) {
			if (other.neighbours != null)
				return false;
		} else if (!neighbours.equals(other.neighbours))
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
		return true;
	}








	
	
}
