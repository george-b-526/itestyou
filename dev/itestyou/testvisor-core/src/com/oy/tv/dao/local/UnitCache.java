package com.oy.tv.dao.local;

import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import org.xml.sax.InputSource;

import com.oy.tv.app.UserMessage;
import com.oy.tv.dao.core.UnitDAO;
import com.oy.tv.db.AnyDB;
import com.oy.tv.model.vocb.WordRepository;
import com.oy.tv.model.vocb.WordSet;
import com.oy.tv.schema.core.EObjectState;
import com.oy.tv.schema.core.UnitBO;

public class UnitCache {

	private static Object lock = new Object();
	private static Map<Integer, WordSet> wordSets = new HashMap<Integer, WordSet>(); 
	
	public static UnitBO getUnit(AnyDB db, int unitId) {
		try {
			UnitBO unit = UnitDAO.loadUnit(db, unitId);	
			if (unit == null || !unit.getState().equals(EObjectState.ACTIVE)){    
				throw new UserMessage("Item not found.");
			} else {
				if (!unit.getState().equals(EObjectState.ACTIVE)){    
					throw new UserMessage("Item not accessible.");
				}
			}			
			return unit;
		} catch (UserMessage um){
			throw um;
		} catch (Exception e){
			throw new RuntimeException(e);
		}
	}
	
	public static WordSet getWordSet(AnyDB db, int unitId){
		WordSet result;
		
		synchronized(lock){
			result = wordSets.get(unitId);
			if (result == null){
				try {    
					UnitBO unit = UnitCache.getUnit(db, unitId);
					result = WordRepository.fromXML(new InputSource(new StringReader(unit.getXml())));
				} catch (Exception e){
					e.printStackTrace();
					throw new RuntimeException(e);
				}
				
				wordSets.put(unitId, result);
			}
		}
		
		return result;
	}
	
}
