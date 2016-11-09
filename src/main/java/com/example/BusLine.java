package com.example;

public class BusLine {

	int id;
	String label;
	int number;
	String color;
	int order;
	String traceFile;
	String scheduleFile;
	
	
	public BusLine(int id, String label, int number, String color, int order,
			String traceFile, String scheduleFile) {
		super();
		this.id = id;
		this.label = label;
		this.number = number;
		this.color = color;
		this.order = order;
		this.traceFile = traceFile;
		this.scheduleFile = scheduleFile;
	}

public BusLine(int id, String label){
	this.id = id;
	this.label=label;
}
	
	
	@Override
	public String toString(){
		return "Line{id=" + id +
                ", name='" + label + "'\''}";
	}
	
	public int getId() {
		return id;
	}


	public void setId(int id) {
		this.id = id;
	}


	public String getLabel() {
		return label;
	}


	public void setLabel(String label) {
		this.label = label;
	}


	public int getNumber() {
		return number;
	}


	public void setNumber(int number) {
		this.number = number;
	}


	public String getColor() {
		return color;
	}


	public void setColor(String color) {
		this.color = color;
	}


	public int getOrder() {
		return order;
	}


	public void setOrder(int order) {
		this.order = order;
	}


	public String getTraceFile() {
		return traceFile;
	}


	public void setTraceFile(String traceFile) {
		this.traceFile = traceFile;
	}


	public String getScheduleFile() {
		return scheduleFile;
	}


	public void setScheduleFile(String scheduleFile) {
		this.scheduleFile = scheduleFile;
	}
	
	
	
	
	
	
}
