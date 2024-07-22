package com.incuventure.utilities

/**
*
* @author Giancarlo Angulo <gian.angulo@incuventure.net>
*
*/
import org.apache.commons.io.FileUtils

//From stackoverflow
class ReportDirectory {
	static final String path = "/opt/tfs/web-app/reports/"; //<-- generic path on your SERVER!
	
	static{
		println "in here"
		  //static initializer to make sure directory gets created.  Better ways to do this but this will work!
		def pathAsFile = new File(path)
		def fileExists = pathAsFile.mkdirs()
		
		  if (pathAsFile.exists()){
			 println("REPORT DIRECTORY @ ${pathAsFile.absolutePath}")
		  }else{
			 println("FAILED TO CREATE REPORT DIRECTORY @ ${pathAsFile.absolutePath}")
			  }
	
	  }
	
	  public static File[] listFiles(){
		  return new File(path).listFiles() //<-- maybe use filters to just pull pdfs?	   
	  }
	  
	  public static List listFilesString(){
		  def temp = new File(path).listFiles()
		  def temptemp = []
		   temp.each{ fileToPrint ->
			  	temptemp << fileToPrint.canonicalPath.toString()
			  }
		   return temptemp
		  }
	
	  public static void addFile(File file){
		  FileUtils.copyFileToDirectory(file, new File(path))
	  }
	
	  public static void deleteAll(){
		  listFiles().each(){ fileToDelete ->
			  fileToDelete.delete()
		  }
	  }
	  
	  public static void deleteFile(File file){
		  file.delete()
	  }
	
	  public static File findFile(String name){
		  def tempfile = null
		  listFiles().each(){ fileToCheck ->
	
			  if (fileToCheck.name.equals(name)){
				  println fileToCheck.absolutePath
				  println "FOUND"
				  tempfile = fileToCheck
			  }
		  }
		  println 'in null'
		  return tempfile
	  }
   
}
