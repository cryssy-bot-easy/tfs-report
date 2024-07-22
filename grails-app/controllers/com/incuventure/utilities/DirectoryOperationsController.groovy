package com.incuventure.utilities

import com.incuventure.utilities.ReportDirectory
import grails.converters.JSON

class DirectoryOperationsController {
	
	def reportDirectory

    def index() {
			redirect(actionName:'showAllFiles',controllerName:'DirectoryOperations')
	}	
	
	def showAllFiles = {
		println 'showAllFiles'		
		render reportDirectory.listFilesString() as JSON		
	}
  
	def downloadFile = {
	   def fileName = params.fileName
  
	   def fileToDownload = reportDirectory.findFile(fileName)
  
	   if (fileToDownload){
  
	   response.setContentType("application/octet-stream")
	   response.setHeader("Content-disposition", "attachment;filename=${fileToDownload.getName()}")
	   response.outputStream << fileToDownload.newInputStream()  //<-- ask the user to download
	  }else{
		  //handle when the file is not found
	  }  
	}
	
	def downloadFileByToken = {
		
		println 'downloadFileByToken'
		def token = params.token ?: 'temp' //default file token 
		def extension = params.extension ?: 'pdf' //default extension
		def fileName = token + ".pdf"
   
		def fileToDownload = reportDirectory.findFile(fileName)
		println fileToDownload
   
		if (fileToDownload){
   
		response.setContentType("application/octet-stream")
		response.setHeader("Content-disposition", "attachment;filename=${fileToDownload.getName()}")
		response.outputStream << fileToDownload.newInputStream()  //<-- ask the user to download
	   }else{
	   	println 'file not found in else'
		   //handle when the file is not found
	   }
	 }
	
  
	def deleteAllFiles ={
		reportDirectory.deleteAllFiles()
  
		[files:reportDirectory.listFiles()] //<-- return the remaining files, if any.
	}
	
	def deleteFile = {
		def fileName = params.fileName
		def fileToDownload = reportDirectory.findFile(fileName)
		reportDirectory.deleteFile(fileToDownload)
	} 
	
}
