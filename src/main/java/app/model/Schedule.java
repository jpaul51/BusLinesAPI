package app.model;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Type;
import org.joda.time.DateTime;


@Entity
@Table(name="stopschedule")
public class Schedule {

	@Id @GeneratedValue
	long id;
	@Type(type="org.joda.time.contrib.hibernate.PersistentDateTime")
	List<DateTime> schedules;
	@ManyToOne
	Stop way;
	@ManyToOne
	Line line;//Contains multiLineString, to remove 
	Boolean schoolPeriod=true;
	
	
	public Schedule() {
		super();
	}



	public Schedule(Stop way, Line line, Boolean schoolPeriod) {
		super();
		this.way = way;
		this.line = line;
		this.schoolPeriod = schoolPeriod;
	}



	public Schedule(long id, List<DateTime> schedules, Stop way, Line line, Boolean schoolPeriod) {
		super();
		this.id = id;
		this.schedules = schedules;
		this.way = way;
		this.line = line;
		this.schoolPeriod = schoolPeriod;
	}



	public Schedule(long id, List<DateTime> schedules, Stop way, Line line) {
		super();
		this.id = id;
		this.schedules = schedules;
		this.way = way;
		this.line = line;
	}



	public Boolean getSchoolPeriod() {
		return schoolPeriod;
	}



	public void setSchoolPeriod(Boolean schoolPeriod) {
		this.schoolPeriod = schoolPeriod;
	}



	public long getId() {
		return id;
	}



	public void setId(long id) {
		this.id = id;
	}



	public List<DateTime> getSchedules() {
		return schedules;
	}



	public void setSchedules(List<DateTime> schedules) {
		this.schedules = schedules;
	}



	public Stop getway() {
		return way;
	}



	public void setway(Stop way) {
		this.way = way;
	}



	public Line getLine() {
		return line;
	}



	public void setLine(Line line) {
		this.line = line;
	}



	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((way == null) ? 0 : way.hashCode());
		result = prime * result + (int) (id ^ (id >>> 32));
		result = prime * result + ((line == null) ? 0 : line.hashCode());
		result = prime * result + ((schedules == null) ? 0 : schedules.hashCode());
		result = prime * result + ((schoolPeriod == null) ? 0 : schoolPeriod.hashCode());
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
		Schedule other = (Schedule) obj;
		if (way == null) {
			if (other.way != null)
				return false;
		} else if (!way.equals(other.way))
			return false;
		if (id != other.id)
			return false;
		if (line == null) {
			if (other.line != null)
				return false;
		} else if (!line.equals(other.line))
			return false;
		if (schedules == null) {
			if (other.schedules != null)
				return false;
		} else if (!schedules.equals(other.schedules))
			return false;
		if (schoolPeriod == null) {
			if (other.schoolPeriod != null)
				return false;
		} else if (!schoolPeriod.equals(other.schoolPeriod))
			return false;
		return true;
	}


	
	
	
	
}
