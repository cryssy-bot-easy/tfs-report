package com.ucpb.tfs.report.mco

import java.text.DecimalFormat;
import java.util.HashMap;
import com.ucpb.tfs.report.mco.BucketEntry;
import com.ucpb.tfs.report.mco.enums.Duration;

class Bucket {
	private Map<String,BucketEntry> durations;
	private DecimalFormat decimalFormat=new DecimalFormat("#,##0.00;(#,##0.00)")
		
	public Bucket(){
		durations.put(Duration.UPTO6DAYS,new BucketEntry(Duration.UPTO6DAYS));
		durations.put(Duration.UPTO4WEEKS,new BucketEntry(Duration.UPTO4WEEKS));
		durations.put(Duration.UPTO2MONTHS,new BucketEntry(Duration.UPTO2MONTHS));
		durations.put(Duration.UPTO3MONTHS,new BucketEntry(Duration.UPTO3MONTHS));
		durations.put(Duration.UPTO4MONTHS,new BucketEntry(Duration.UPTO4MONTHS));
		durations.put(Duration.UPTO6MONTHS,new BucketEntry(Duration.UPTO6MONTHS));
		durations.put(Duration.UPTO12MONTHS,new BucketEntry(Duration.UPTO12MONTHS));
		durations.put(Duration.UPTO4YEARS,new BucketEntry(Duration.UPTO4YEARS));
		durations.put(Duration.UPTO9YEARS,new BucketEntry(Duration.UPTO9YEARS));
		durations.put(Duration.UPTO9YEARSBEYOND,new BucketEntry(Duration.UPTO9YEARSBEYOND));
		decimalFormat.setParseBigDecimal(true);
	}
	
	public Bucket(Map<String,BucketEntry> durations){
		this.durations = durations;
		decimalFormat.setParseBigDecimal(true);		
	}

	public Map<String,BucketEntry> getDurations(){
		return this.durations;
	}

	public void setDurations(Map<String,BucketEntry> durations){
		this.durations = durations;
	}
	
	public void setAmount(Duration duration,String amount){
		durations.get(duration).setAmount(amount);
	}
	
	public void add(Duration duration,String amount){
		durations.get(duration).add(amount);
	}
	
	public Map<String,String> getFormattedBucket(boolean negate){
		Bucket bucket = new Bucket(this.durations);
		
		for(Map.Entry<String,BucketEntry> duration : bucket.getDurations().entrySet()){
			BucketEntry entry = (BucketEntry) duration.getValue(); 
			if(negate && BigDecimal.ZERO < entry.getAmount()){
				entry.negate()
			}
		}
		
		return bucket.convertToStringMap()
	}
	
	public Map<String,String> convertToStringMap(){
		Map<String,String> result=new HashMap<String,String>();
		
		for(Map.Entry<String,BucketEntry> duration : durations.entrySet()){
			BucketEntry entry = (BucketEntry) duration.getValue()
			result.put(duration.getKey(), decimalFormat.format(entry.getAmount()));
		}
		
		return result;
	}
}