package com.oy.tv.parts;

public interface ICellTextCustomizer {

	public String [] customize(String [] values);

	class NullCellTextCustomizer implements ICellTextCustomizer {
		public String [] customize(String [] values){
			return values;
		}
	}
	
}
