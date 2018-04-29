package com.oy.tv.wdgt.view;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import com.oy.tv.app.UserMessage;
import com.oy.tv.schema.core.UnitBO;
import com.oy.tv.util.MementoManager;
import com.oy.tv.wdgt.model.ViewModel;

public class WidgetsTests extends TestCase {

	private List<UnitBO> units;
	private ViewModel model;
	
	@Override
	public void setUp(){
		units = new ArrayList<UnitBO>();

		UnitBO _unit;
		for (int i=0; i < 10; i++){
			_unit = new UnitBO();
			_unit.setId(i);
			units.add(_unit);
		}
		
		model = new ViewModel("qwertyy");
	}
	
	public void testPickNext(){
		model.unitIdHistory = new int [] {0, 1, 2, 3};
		List<Option> options = MainView.getNextUnitOptions(units, model.unitIdHistory);
		model.unitId = options.get(0).unitId;
		MainView.updateUnitHistory(model);
		assertEquals(5, model.unitIdHistory.length);
		assertEquals(4, model.unitIdHistory[0]);  
		assertEquals(4, model.unitId);
		assertEquals(toString(new int [] {4, 0, 1, 2, 3}), toString(model.unitIdHistory));
	}

	public void testShuffleAndAvoidRecent(){
		model.unitIdHistory = new int [] {3, 2, 1, 0};
		List<Option> options = MainView.getNextUnitOptions(units, model.unitIdHistory);
		model.unitId = options.get(0).unitId;
		MainView.updateUnitHistory(model);
		assertEquals(4, model.unitId);
	}
		
	public void testOldestPickedIfNoOtherOptions(){
		model.unitIdHistory = new int [] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
		List<Option> options = MainView.getNextUnitOptions(units, model.unitIdHistory);
		model.unitId = options.get(0).unitId;
		MainView.updateUnitHistory(model);
		assertEquals(9, model.unitId);		
	}

	public void testOldestPickedIfNoOptions2(){
		model.unitIdHistory = new int [] {9, 8, 7, 6, 5, 4, 3, 2, 1, 0};
		List<Option> options = MainView.getNextUnitOptions(units, model.unitIdHistory);
		model.unitId = options.get(0).unitId;
		MainView.updateUnitHistory(model);
		assertEquals(0, model.unitId);		
	}
	
	public void testOldestNotPickedIfSeenRecently(){
		model.unitIdHistory = new int [] {0, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0};
		List<Option> options = MainView.getNextUnitOptions(units, model.unitIdHistory);
		model.unitId = options.get(0).unitId;
		MainView.updateUnitHistory(model);
		assertEquals(1, model.unitId);		
	}

	public void testNullHistoryIsOk(){
		model.unitIdHistory = null;
		List<Option> options = MainView.getNextUnitOptions(units, model.unitIdHistory);
		model.unitId = options.get(0).unitId;
		MainView.updateUnitHistory(model);
		assertEquals(0, model.unitId);		
	}
		
	public void testEmptyhistoryIsOk(){
		model.unitIdHistory = new int [] {};
		List<Option> options = MainView.getNextUnitOptions(units, model.unitIdHistory);
		model.unitId = options.get(0).unitId;
		MainView.updateUnitHistory(model);
		assertEquals(0, model.unitId);		
	}
	
	public void testAllSame(){
		model.unitIdHistory = new int [] {0, 0, 0, 0, 0};
		List<Option> options = MainView.getNextUnitOptions(units, model.unitIdHistory);
		model.unitId = options.get(0).unitId;
		MainView.updateUnitHistory(model);
		assertEquals(1, model.unitId);
	}
	
	public void testEmptryOptionsNotOk(){
		model.unitIdHistory = null;
		try {
			List<Option> options = MainView.getNextUnitOptions(new ArrayList<UnitBO>(), 
					model.unitIdHistory);
			model.unitId = options.get(0).unitId;
			MainView.updateUnitHistory(model);
			fail("Must have failed");
		} catch (UserMessage um){}
	}

	public void testMementoUnder2K(){
		ViewModel model = new ViewModel("ewqewqewqewqewqewqewqewqwqe");
		model.referer = "ldjsalkdjlsajdlksajdlksajdlksajdlksajdlksajdlksajd";
		model.failCount = 10;
		model.gradeId = 100;
		model.passCount = 1000;
		model.seed = 12345;
		model.unitId = 10000;
		
		model.unitIdHistory = new int [MainView.MAX_HISTORY_SIZE];
		for (int i=0; i < model.unitIdHistory.length; i++){
			model.unitIdHistory[i] = i;
		}
		
		model.createdOn = 7890;
		model.variationId = 100000;

		String memento = MementoManager.encode(model);
		assertTrue(1024 > memento.length());	
	}

	public void testHIstoryDoesNotGrowInfinitely(){
		List<UnitBO> units = new ArrayList<UnitBO>();

		UnitBO _unit;
		for (int i=0; i < 10; i++){
			_unit = new UnitBO();
			_unit.setId(i);
			units.add(_unit);
		}

		ViewModel model = new ViewModel("qwertyy");
		model.unitIdHistory = new int [] {0, 1, 2, 3};
		for (int i=0; i < 1000; i++){
			MainView.getNextUnitOptions(units, model.unitIdHistory);
			MainView.updateUnitHistory(model);
		}

		assertEquals(MainView.MAX_HISTORY_SIZE, model.unitIdHistory.length);
	}
	
	public String toString(int [] items){
		StringBuffer sb = new StringBuffer();
		for (int item : items){
			sb.append(item + "\n");
		}
		return sb.toString();
	}

	
}
