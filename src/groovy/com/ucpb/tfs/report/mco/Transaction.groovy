package com.ucpb.tfs.report.mco

import java.text.DecimalFormat;

import com.ucpb.tfs.report.mco.Bucket;

public class Transaction {

	private String type;
	private String code;
	private String currency;
	private String query;
	private Bucket bucket;
	private boolean negate;
	private BigDecimal grandTotal;
	private DecimalFormat decimalFormat=new DecimalFormat("#,##0.00;(#,##0.00)")

	private final String TYPEMAPPING = "transactionType"
	private final String CODEMAPPING = "transactionCode"
	private final String CURRENCYMAPPING = "currency"
	private final String GRANDTOTALMAPPING = "grandTotal"
	
	public Transaction(String type,String code,String currency,boolean negate){
		this.type=type;
		this.code=code;
		this.currency=currency;
		this.negate=negate;
		this.bucket=new Bucket();
		this.grandTotal=new BigDecimal("0.00");
		decimalFormat.setParseBigDecimal(true);
	}
	
	public void setQuery(String query){
		this.query=query;
	}
	
	public Bucket setBucket(Bucket bucket){
		this.bucket = bucket;
	}
	
	public void setGrandTotal(BigDecimal grandTotal){
		this.grandTotal=grandTotal
	}
	
	public void addToGrandTotal(BigDecimal amount){
		this.grandTotal = this.grandTotal.add(amount);
	}
	
	public Bucket getBucket(){
		return this.bucket;
	}
	
	public String getType(){
		return this.type
	}
	
	public String getCode(){
		return this.code;
	}
	
	public String getCurrency(){
		return this.currency;
	}
	
	public String getQuery(){
		return this.query;
	}
	
	public Map<String,String> getAllDetails(){
		Map<String,String> result = new HashMap<String,String>();
		result.put(TYPEMAPPING, this.type);
		result.put(CODEMAPPING, this.code);
		result.put(CURRENCYMAPPING, this.currency);
		result.put(GRANDTOTALMAPPING, getGrandTotal());
		result.putAll(this.bucket.getFormattedBucket(negate))
	}
	
	private String getGrandTotal(){
		if(negate){
			return decimalFormat.format(this.grandTotal);			
		}else{
			if(BigDecimal.ZERO < this.grandTotal){
				this.grandTotal = this.grandTotal.negate() 				
			}
			return decimalFormat.format(this.grandTotal);
		}
	}
	
}