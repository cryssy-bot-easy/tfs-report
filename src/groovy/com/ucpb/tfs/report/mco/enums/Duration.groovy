package com.ucpb.tfs.report.mco.enums

enum Duration {
	UPTO6DAYS ("UPTO6DAYS"),
	UPTO4WEEKS ("UPTO4WEEKS"),
	UPTO2MONTHS ("UPTO2MONTHS"),
	UPTO3MONTHS ("UPTO3MONTHS"),
	UPTO4MONTHS ("UPTO4MONTHS"),
	UPTO6MONTHS ("UPTO6MONTHS"),
	UPTO12MONTHS ("UPTO12MONTHS"),
	UPTO4YEARS  ("UPTO4YEARS"),
	UPTO9YEARS ("UPTO9YEARS"),
	UPTO9YEARSBEYOND ("UPTO9YEARSBEYOND");
	
	private String duration;
	
	public Duration(String duration){
		this.duration=duration;
	}
	
	@Override
	public String toString(){
		return this.duration;
	}
}
