package com.incuventure.dto

class DocumentsEnclosedDTO {

	String documentName
	String original1
	String original2
	String duplicate1
	String duplicate2
	
	public DocumentsEnclosedDTO(documentName, original1, original2, duplicate1, duplicate2){
		this.documentName = documentName
		this.original1 = original1
		this.original2 = original2
		this.duplicate1 = duplicate1
		this.duplicate2 = duplicate2
	}
}
