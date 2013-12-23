package com.gorillalogic.agents.html.processor;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.gorillalogic.monkeytalk.Command;

public enum ElementLocator {
	  ID,
	  CLASSNAME,
	  TAGNAME,
	  NAME,
	  LINKTEXT,
	  PARTIALLINKTEXT,
	  CSS,
	  XPATH,
	  JAVASCRIPT,
	  COMPONENTTYPE;
	  
	  private static ElementLocator getFromMonkeyID(String monkeyID){
		   if(monkeyID.equalsIgnoreCase("*")){
			   return COMPONENTTYPE;
	       } else if(monkeyID.matches("classname=.*")){
			   return CLASSNAME;
		   } else if(monkeyID.matches("tagname=.*")){
			   return TAGNAME;
		   } else if(monkeyID.matches("name=.*")){
			   return NAME;
		   } else if(monkeyID.matches("linktext=.*")){
			   return LINKTEXT;
		   } else if(monkeyID.matches("partiallinktext=.*")){
			   return PARTIALLINKTEXT;
		   } else if(monkeyID.matches("css=.*")){
			   return CSS;
		   } else if(monkeyID.matches("javascript=.*")){
			   return JAVASCRIPT;
		   } else if(monkeyID.matches("xpath=.*")){
			   return XPATH;
		   }  else {
			   return ID;
		   }
	  }
	  public static WebElement findWebElement(Command c, WebDriver driver){
		  
		  switch(getFromMonkeyID(c.getMonkeyId())){
		  case CLASSNAME: driver.findElements(By.className(removeMetaData(c.getMonkeyId())));
		  case TAGNAME: return driver.findElement(By.tagName(removeMetaData(c.getMonkeyId())));
		  case COMPONENTTYPE: return driver.findElement(By.tagName(c.getComponentType()));
		  case NAME: return driver.findElement(By.name(removeMetaData(c.getMonkeyId())));
		  case LINKTEXT: return driver.findElement(By.linkText(removeMetaData(c.getMonkeyId())));
		  case PARTIALLINKTEXT: return driver.findElement(By.partialLinkText(removeMetaData(c.getMonkeyId())));
		  case CSS: return driver.findElement(By.cssSelector(removeMetaData(c.getMonkeyId())));
		  case JAVASCRIPT: return (WebElement) ((JavascriptExecutor)driver).executeScript(removeMetaData(c.getMonkeyId()));
		  case XPATH: return driver.findElements(By.xpath(removeMetaData(c.getMonkeyId()))).get(0);
          default: {
        	  //First find by ID
        	  WebElement element = null;
        	  if(element == null){
        	  List<WebElement> elements = driver.findElements(By.xpath("//"+c.getComponentType()+"[@id='"+ c.getMonkeyId()+"']"));
        	  if(elements.size() > 0 )
    		       element = elements.get(0);
        	  }
        	  //Second find by NAME
        	  if(element == null){
            	  List<WebElement> elements = driver.findElements(By.xpath("//"+c.getComponentType()+"[@name='"+ c.getMonkeyId()+"']"));
            	  if(elements.size() > 0 )
        		       element = elements.get(0);
        	  }
        	  
        	  //Third find by VALUE (value of the text node)
        	  if(element == null){
            	  List<WebElement> elements = driver.findElements(By.xpath("//"+c.getComponentType()+"[@value='"+ c.getMonkeyId()+"']"));
            	  if(elements.size() > 0 )
        		       element = elements.get(0);
        	  }
        	  
        	  //Forth find by TEXT
        	  if(element == null){
            	  List<WebElement> elements = driver.findElements(By.xpath("//"+c.getComponentType()+"[text()='"+ c.getMonkeyId()+"']"));
            	  if(elements.size() > 0 )
        		       element = elements.get(0);
        	  }
        	  return element;
          }

		  }
	  }
	private static String removeMetaData(String monkeyID) {
		  if(monkeyID.equalsIgnoreCase("*")){
			   return "*";
	       } else if(monkeyID.matches("classname=.*")){
			   return monkeyID.substring("classname=".length());
		   } else if(monkeyID.matches("tagname=.*")){
			   return monkeyID.substring("tagname=".length());
		   } else if(monkeyID.matches("name=.*")){
			   return monkeyID.substring("name=".length());
		   } else if(monkeyID.matches("linktext=.*")){
			   return monkeyID.substring("linktext=".length());
		   } else if(monkeyID.matches("partiallinktext=.*")){
			   return monkeyID.substring("partiallinktext=".length());
		   } else if(monkeyID.matches("css=.*")){
			   return monkeyID.substring("css=".length());
		   } else if(monkeyID.matches("javascript=.*")){
			   return monkeyID.substring("javascript=".length());
		   } else if(monkeyID.matches("xpath=.*")){
			   return monkeyID.substring("xpath=".length());
		   }  else {
			   return monkeyID;
		   }
	}

	}

