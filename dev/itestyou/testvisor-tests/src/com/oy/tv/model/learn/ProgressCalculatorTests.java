package com.oy.tv.model.learn;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;

import junit.framework.TestCase;

public class ProgressCalculatorTests extends TestCase {

	public void testDeltaParse(){
		String delta = "ep={1,2,3}\nef={4,5,6}";
		
		assertEquals("{1:1,2:1,3:1,4:-1,5:-1,6:-1}", ProgressCalculator.renderUnitData(
				ProgressCalculator.parseUnitDeltaData(delta)));
	}
	
	public void testMerge (){
		assertEquals(
				"{1:3,2:4,3:1,4:-1,5:-1,6:-1}",
				ProgressCalculator.mergeUnitData("{1:2,2:3}", "ep={1,2,3}\nef={4,5,6}"));
	
		assertEquals(
				"{1:1,2:2,3:-1,4:1,5:1,6:1}",
				ProgressCalculator.mergeUnitData("{1:2,2:3}", "mf={1,2,3}\nmp={4,5,6}"));
		
		assertEquals(
				"{1:0}",
				ProgressCalculator.mergeUnitData("{1:1}", "ep={}\nef={1}"));

		assertEquals(
				"{1:2}",
				ProgressCalculator.mergeUnitData("{1:1}", "ep={1}\nef={}"));
	
		assertEquals(
				"{1:0}",
				ProgressCalculator.mergeUnitData("{1:-1}", "ep={1}\nef={}"));

		assertEquals(
				"{1:-2}",
				ProgressCalculator.mergeUnitData("{1:-1}", "ep={}\nef={1}"));
	}
	
	public void testParseData() {
		Map<Integer, Integer> data = ProgressCalculator.parseUnitData(
				"{1:2,4:5,7:8}");
		
		assertEquals(2, data.get(1).intValue());
		assertEquals(5, data.get(4).intValue());
		assertEquals(8, data.get(7).intValue());
	}
	
	public void testRangesBadRangesExcept(){
		try {
			ProgressCalculator.RangeContainer container = new ProgressCalculator.RangeContainer();
  		String text = "{1|10,-1|34,13,25|1}";
  		ProgressCalculator.parseUnitSummaryWithRanges(text, container);
  		fail();
		} catch (IllegalArgumentException iae){}
				
		try {
			ProgressCalculator.RangeContainer container = new ProgressCalculator.RangeContainer();
  		String text = "{1|10,3:15}";
  		ProgressCalculator.parseUnitSummaryWithRanges(text, container);
  		fail();
		} catch (IllegalArgumentException iae){}
	}
	
	public void testRanges(){
		String text = "{1|10,-1|34,13,25|999}";
		ProgressCalculator.RangeContainer container = new ProgressCalculator.RangeContainer();
		ProgressCalculator.parseUnitSummaryWithRanges(text, container);
		
		assertEquals("{-1|34,1|10,13,25|999}", container.render());
	}

	public void testRangeExpansion(){
		String text = "{1|10}";
		ProgressCalculator.RangeContainer container = new ProgressCalculator.RangeContainer();
		ProgressCalculator.parseUnitSummaryWithRanges(text, container);
		assertEquals(10, container.size());

		container.add(11);
		assertEquals("{1|11}", container.render());
		assertEquals(11, container.size());
		
		container.add(0);
		assertEquals("{0|11}", container.render());
		assertEquals(12, container.size());

		container.add(13);
		assertEquals("{0|11,13}", container.render());
		assertEquals(13, container.size());

		container.add(-2);
		assertEquals("{-2,0|11,13}", container.render());
		assertEquals(14, container.size());

		container.add(5);
		assertEquals("{-2,0|11,13}", container.render());
		assertEquals(14, container.size());
	}

	public void testRangeMerge(){
		{
  		String text = "{10|20}";
  		ProgressCalculator.RangeContainer container = new ProgressCalculator.RangeContainer();
  		ProgressCalculator.parseUnitSummaryWithRanges(text, container);
  
  		container.add(15);
  		assertEquals("{10|20}", container.render());
		}
		
		{
  		String text = "{11,13}";
  		ProgressCalculator.RangeContainer container = new ProgressCalculator.RangeContainer();
  		ProgressCalculator.parseUnitSummaryWithRanges(text, container);
  
  		container.add(14);
  		container.add(14);
  		container.add(13);
  		container.add(13);
  		assertEquals("{11,13|14}", container.render());
		}
		
		{
  		String text = "{1|10,12|15}";
  		ProgressCalculator.RangeContainer container = new ProgressCalculator.RangeContainer();
  		ProgressCalculator.parseUnitSummaryWithRanges(text, container);
  
  		container.add(11);
  		assertEquals("{1|15}", container.render());
		}
		
		{
  		String text = "{1|10,13|15}";
  		ProgressCalculator.RangeContainer container = new ProgressCalculator.RangeContainer();
  		ProgressCalculator.parseUnitSummaryWithRanges(text, container);
  
  		container.add(11);
  		assertEquals("{1|11,13|15}", container.render());
  		assertEquals(14, container.size());
		}
	}
	
	public void testRangesSimulate5000(){
		Random rnd = new Random(123);
		
		List<Integer> ids = new ArrayList<Integer>();
		for (int i=1; i <= 5000; i++){
			ids.add(i);
		}
		Collections.shuffle(ids, rnd);
		
		ProgressCalculator.RangeContainer container = new ProgressCalculator.RangeContainer();
		
		int maxLen = 0;
		for (int i=0; i < ids.size(); i++){
			container.add(ids.get(i));
			assertEquals(i + 1, container.size());
			
			if (i == 4){
				assertTrue(container.getRangeCount() == 5);
			}

			if (i == 4994){
				assertTrue(container.getRangeCount() == 6);
			}
			
			int len = container.render().length();
			if (len > maxLen){
				maxLen = len;
			}
		}
		
		assertEquals(maxLen, 9212);
		assertEquals("{1|5000}", container.render());
	}
	
	
	public void testRangesSimulate100(){
		Random rnd = new Random(456);
		
		List<Integer> ids = new ArrayList<Integer>();
		for (int i=1; i <= 1000; i++){
			ids.add(i);
		}
		Collections.shuffle(ids, rnd);
		
		ProgressCalculator.RangeContainer container = new ProgressCalculator.RangeContainer();
		
		int maxLen = 0;
		for (int i=0; i < ids.size(); i++){
			container.add(ids.get(i));
			assertEquals(i + 1, container.size());
			
			int len = container.render().length();
			if (len > maxLen){
				maxLen = len;
			}
		}
		
		assertEquals(maxLen, 1451);
		assertEquals("{1|1000}", container.render());
	}

}
