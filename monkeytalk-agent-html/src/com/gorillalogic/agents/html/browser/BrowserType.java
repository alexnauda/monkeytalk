package com.gorillalogic.agents.html.browser;

public enum BrowserType {
  FIREFOX,
  IE,
  CHROME,
  SAFARI, 
  IOS;
  
  public static BrowserType getFromString(String type){
	  if(type.equalsIgnoreCase("firefox")){
		  return FIREFOX;
	  }
	  if(type.equalsIgnoreCase("ie") || type.equalsIgnoreCase("internet explorer") || type.equalsIgnoreCase("internet exploder")){
		  return IE;
	  }
	  if(type.equalsIgnoreCase("chrome")){
		  return CHROME;
	  }
	  if(type.equalsIgnoreCase("safari")){
		  return SAFARI;
	  }
	  if(type.equalsIgnoreCase("ios") || type.equalsIgnoreCase("iphone") || type.equalsIgnoreCase("ipad") || type.equalsIgnoreCase("apple")){
		  return IOS;
	  }
	  return FIREFOX;
  }
}
