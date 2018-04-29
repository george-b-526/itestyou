package com.oy.tv.wdgt.model;

import java.io.Serializable;

public class ViewModel implements Serializable {
	static final long serialVersionUID = 0;
	    
	public ViewModel(String activityId){
		this.activityId = activityId;
	} 
	          
	public String referer;				// referrer url
	public String activityId;			// unique id of this object instance for tracking  
	public int seed;							// random shuffle seed
	public int gradeId;						// grade id
	public int unitId;						// unit id
	public int variationId;				// variation id
	public int passCount;					// number of pass in a row
	public int failCount;					// number of fail in a row
	public int skipCount;					// number of skips in a row
	public int tryCount;					// number of tries in a session
	public int [] unitIdHistory;	// recent unitId's
	public long createdOn;				// time stamp of the last update
	public String locale;					// locale

	public String getReferer() {
  	return referer;
  }
	
	public String getActivityId() {
  	return activityId;
  }
	
	public int getSeed() {
  	return seed;
  }
	
	public int getGradeId() {
  	return gradeId;
  }
	
	public int getUnitId() {
  	return unitId;
  }
	
	public int getVariationId() {
  	return variationId;
  }
	
	public int getPassCount() {
  	return passCount;
  }
	
	public int getFailCount() {
  	return failCount;
  }
	
	public int getSkipCount() {
  	return skipCount;
  }

	public int[] getUnitIdHistory() {
  	return unitIdHistory;
  }
	
	public long getCreatedOn() {
  	return createdOn;
  }
	
}
