package com.ucpb.tfs.report.mco

import com.ucpb.tfs.report.mco.enums.Duration;

class BucketEntry {
	private Duration duration;
	private BigDecimal amount;
	
	public BucketEntry(Duration duration){
		this.duration=duration;
		this.amount = new BigDecimal("0.00");
	}
	
	public BucketEntry(Duration duration,BigDecimal amount){
		this.duration=duration;
		this.amount= amount;	
	}
	
	public String getDuration(){
		return this.duration;
	}
	
	public BigDecimal getAmount(){
		return this.amount.toString();
	}
	
	public void setAmount(BigDecimal amount){
		this.amount=amount;
	}
	
	public void setAmount(String amount){
		this.amount=new BigDecimal(amount);
	}
	
	public void add(String amount){
		this.amount=this.amount.add(new BigDecimal(amount));
	}
	
	public void negate(){
		this.amount = this.amount.negate();
	}
}
