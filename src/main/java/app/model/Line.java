package app.model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.vividsolutions.jts.geom.MultiLineString;

@JsonIgnoreProperties
@Entity
@Table(name="busline")
public class Line {
	
	@JsonProperty("id")
	@Id
	Long id;
	@JsonProperty("label")
	String name;
	@JsonProperty("color")
	String color;
	
	MultiLineString lines=null;
	@JsonProperty("kml_path")
	String pathFile;
	
public Line(){
	
	lines=null;
}

public Line(long id)
{
	this.id=id;
}
public Line(Long id, String name, String color, String pathFile) {
	super();
	this.id = id;
	this.name = name;
	this.color = color;
	this.pathFile = pathFile;
	lines=null;
}

public Long getId() {
	return id;
}

public void setId(Long id) {
	this.id = id;
}

public String getName() {
	return name;
}

public void setName(String name) {
	this.name = name;
}

public String getColor() {
	return color;
}

public void setColor(String color) {
	this.color = color;
}

public MultiLineString getLines() {
	return lines;
}

public void setLines(MultiLineString lines) {
	this.lines = lines;
}

public String getPathFile() {
	return pathFile;
}

public void setPathFile(String pathFile) {
	this.pathFile = pathFile;
}

@Override
public int hashCode() {
	final int prime = 31;
	int result = 1;
	result = prime * result + ((color == null) ? 0 : color.hashCode());
	result = prime * result + ((id == null) ? 0 : id.hashCode());
	result = prime * result + ((lines == null) ? 0 : lines.hashCode());
	result = prime * result + ((name == null) ? 0 : name.hashCode());
	result = prime * result + ((pathFile == null) ? 0 : pathFile.hashCode());
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
	Line other = (Line) obj;
	if(other.getId() == this.id)
		return true;
	else
		return false;
		
	
}


}
