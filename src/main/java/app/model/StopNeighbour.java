package app.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

@Entity
public class StopNeighbour {

	 @Id @GeneratedValue long id;
	
	 @ManyToOne
	Line lineItBelongs;
	double distance;
	@ManyToOne
	Stop neighbour;
	
	public StopNeighbour()
	{
		
	}
	
	public StopNeighbour(Line lineItBelongs, double distance, Stop neighbour) {
		super();
		this.lineItBelongs = lineItBelongs;
		this.distance = distance;
		this.neighbour = neighbour;
	}

	public Line getLineItBelongs() {
		return lineItBelongs;
	}

	public void setLineItBelongs(Line lineItBelongs) {
		this.lineItBelongs = lineItBelongs;
	}

	public double getDistance() {
		return distance;
	}

	public void setDistance(double distance) {
		this.distance = distance;
	}

	public Stop getNeighbour() {
		return neighbour;
	}

	public void setNeighbour(Stop neighbour) {
		this.neighbour = neighbour;
	}

	@Override
	public String toString() {
		return "StopNeighbour [lineItBelongs=" + lineItBelongs + ", distance=" + distance + ", neighbour=" + neighbour
				+ "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(distance);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + ((lineItBelongs == null) ? 0 : lineItBelongs.hashCode());
		result = prime * result + ((neighbour == null) ? 0 : neighbour.hashCode());
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
		StopNeighbour other = (StopNeighbour) obj;
		if (Double.doubleToLongBits(distance) != Double.doubleToLongBits(other.distance))
			return false;
		if (lineItBelongs == null) {
			if (other.lineItBelongs != null)
				return false;
		} else if (!lineItBelongs.equals(other.lineItBelongs))
			return false;
		if (neighbour == null) {
			if (other.neighbour != null)
				return false;
		} else if (!neighbour.equals(other.neighbour))
			return false;
		return true;
	}
	
	
	
	
}
