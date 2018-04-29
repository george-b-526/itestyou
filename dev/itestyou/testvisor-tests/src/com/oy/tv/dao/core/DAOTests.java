package com.oy.tv.dao.core;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import org.xml.sax.InputSource;

import com.oy.tv.dao.DAOTestBase;
import com.oy.tv.dao.runtime.EventDAO;
import com.oy.tv.model.unit.TranslationContext;
import com.oy.tv.model.unit.TranslationWeaver;
import com.oy.tv.model.unit.TranslationWeaverTest;
import com.oy.tv.model.vocb.Word;
import com.oy.tv.model.vocb.WordRepository;
import com.oy.tv.model.vocb.WordSet;
import com.oy.tv.schema.core.CustomerBO;
import com.oy.tv.schema.core.EEventState;
import com.oy.tv.schema.core.EEventType;
import com.oy.tv.schema.core.EObjectState;
import com.oy.tv.schema.core.EventBO;
import com.oy.tv.schema.core.TagBO;
import com.oy.tv.schema.core.TranslationBO;
import com.oy.tv.schema.core.UnitBO;
import com.oy.tv.schema.core.UserBO;

public class DAOTests extends DAOTestBase {
  
	private final static String NR_RUNTIME = "daotests_ITY_RUNTIME";
	
	public DAOTests(){
		super("daotests_ITY_ADMIN", new AllDAO());
		clearAllData(NR_RUNTIME, new com.oy.tv.dao.runtime.AllDAO());
	}

	public void testEventEnQueue() throws Exception {
		CustomerBO user = new CustomerBO();
		user.setId(12345);
		
		EventBO evt = EventDAO.queueEvent(db, NR_RUNTIME, user, EEventType.JOINED, "JUNIT", "foo");
		
		assertEquals(user.getId(), evt.getOwnerId());
		assertEquals(evt.getState(), EEventState.QUEUED);
	}
	
	public void testUnitUtf8() throws Exception {
		UserBO user = new UserBO();
		user.setId(1);
				
		Word w = new Word();
		w.setDefinition("Común");
		w.setWord("Común");
		w.setType("Común");
		w.setCategory("Común");
				
		List<Word> words = new ArrayList<Word>();
		words.add(w);
		
		StringWriter out = new StringWriter();
    WordRepository.toXML(
    		"Test", words, out, true);
		    
		UnitBO unit = UnitDAO.addUnit(db, user);
		unit.setXml(out.toString());
		unit.setGrade(0);
		
		final int id = unit.getId();
		
		UnitDAO.updateUnit(db, unit, EObjectState.ACTIVE);
		
		// load 
		{
  		UnitBO unitx = UnitDAO.loadUnit(db, id);
  		validate(unitx, w);
		}

		// rnd load 
		{
  		UnitBO unitx = UnitDAO.loadRndUnit(db, 0).get(0);
  		validate(unitx, w);  
		}

		// rnd load 
		{
  		UnitBO unitx = UnitDAO.loadUnitsOldestLast(db, 0, TranslationDAO.LANG_EN).get(0);
  		validate(unitx, w);
		}				
	}
	
	private void validate(UnitBO unitx, Word w) throws Exception {
		WordSet ws = WordRepository.fromXML(
				new InputSource(new StringReader(unitx.getXml())));
		Word wx = ws.getWords().get(0);
		

		assertEquals(w.getDefinition(), wx.getDefinition());		
		assertEquals(w.getType(), wx.getType());		
		assertEquals(w.getWord(), wx.getWord());
		assertEquals(w.getCategory(), wx.getCategory());
	}

	public void testTranslation() throws Exception {
		TranslationBO en = TranslationDAO.put(db, "en", "my.key", "my value");
		TranslationBO ru = TranslationDAO.put(db, "ru", "my.key", "женщина");
		TranslationBO zh = TranslationDAO.put(db, "zh", "my.key", "中英文");

		// dups update, dont fail
		en = TranslationDAO.put(db, "en", "my.key", "sample value");
		
		TranslationBO enx = TranslationDAO.get(db, en.getLang(), en.getKey());
		assertEquals(en.getData(), enx.getData());

		TranslationBO rux = TranslationDAO.get(db, ru.getLang(), ru.getKey());
		assertEquals(ru.getData(), rux.getData());

		TranslationBO zhx = TranslationDAO.get(db, zh.getLang(), zh.getKey());
		assertEquals(zh.getData(), zhx.getData());
	}

	public void testXmlGenerateAndPut() throws Exception {
		UnitBO unit = new UnitBO();	
		unit.setXml(TranslationWeaverTest.XML);
		
		TranslationContext ctx = TranslationWeaver.extractTranslatable(unit); 
		ctx.resetToOriginal();

		{
  		String xml = TranslationContext.saveToXml(ctx);
  		TranslationDAO.put(db, "ru", "/unit/math/" + unit.getId(), xml);	
  		
  		TranslationBO ru = TranslationDAO.get(db, "ru", "/unit/math/" + unit.getId());
  		assertEquals(xml, ru.getData());
		}

		{
			ctx.getQuestion().setNewValue("Какое из следующих выражений равно <$0/><#0/>");
			
  		String xml = TranslationContext.saveToXml(ctx);
  		TranslationDAO.put(db, "ru", "/unit/math/" + unit.getId(), xml);	
  		
  		TranslationBO ru = TranslationDAO.get(db, "ru", "/unit/math/" + unit.getId());
  		assertEquals(xml, ru.getData());
		}
	}

	public void testPutUtf8() throws Exception {
		byte [] utfBytes = new byte [] {60, 63, 120, 109, 108, 32, 118, 101, 114, 115, 105, 111, 110, 61, 34, 49, 46, 48, 34, 32, 101, 110, 99, 111, 100, 105, 110, 103, 61, 34, 85, 84, 70, 45, 56, 34, 63, 62, 13, 10, 60, 109, 97, 116, 104, 45, 116, 108, 110, 62, 13, 10, 60, 113, 117, 101, 115, 116, 105, 111, 110, 62, -48, -102, -48, -80, -48, -70, -48, -66, -48, -75, 32, -48, -72, -48, -73, 32, -47, -127, -48, -69, -48, -75, -48, -76, -47, -125, -47, -114, -47, -119, -48, -72, -47, -123, 32, -48, -78, -47, -117, -47, -128, -48, -80, -48, -74, -48, -75, -48, -67, -48, -72, -48, -71, 32, -47, -128, -48, -80, -48, -78, -48, -67, -48, -80, 32, 38, 108, 116, 59, 35, 48, 47, 38, 103, 116, 59, 63, 60, 47, 113, 117, 101, 115, 116, 105, 111, 110, 62, 13, 10, 60, 99, 104, 111, 105, 99, 101, 115, 62, 13, 10, 60, 99, 104, 111, 105, 99, 101, 62, 38, 108, 116, 59, 35, 48, 47, 38, 103, 116, 59, 60, 47, 99, 104, 111, 105, 99, 101, 62, 13, 10, 60, 99, 104, 111, 105, 99, 101, 62, 38, 108, 116, 59, 35, 48, 47, 38, 103, 116, 59, 60, 47, 99, 104, 111, 105, 99, 101, 62, 13, 10, 60, 99, 104, 111, 105, 99, 101, 62, 38, 108, 116, 59, 35, 48, 47, 38, 103, 116, 59, 60, 47, 99, 104, 111, 105, 99, 101, 62, 13, 10, 60, 99, 104, 111, 105, 99, 101, 62, 38, 108, 116, 59, 35, 48, 47, 38, 103, 116, 59, 60, 47, 99, 104, 111, 105, 99, 101, 62, 13, 10, 60, 47, 99, 104, 111, 105, 99, 101, 115, 62, 13, 10, 60, 47, 109, 97, 116, 104, 45, 116, 108, 110, 62, 13, 10};
		String xml = new String(utfBytes, "UTF-8");
		TranslationDAO.put(db, "ru", "/unit/math/12345", xml);
	
		TranslationBO tln = TranslationDAO.get(db, "ru", "/unit/math/12345");
		assertEquals(xml, tln.getData());
	}

	public void testTags() throws Exception {
		TagBO tag = new TagBO();
		tag.setNs("//my/ns");
		tag.setParentId(0);
		tag.setName("My tag");
		tag.setBody("My body");
		
		tag = TagsDAO.addTag(db, tag);
		
		for (int i=0; i < 100; i++) {
			TagBO child = new TagBO();
			child.setNs("//my/ns");
			child.setParentId(tag.getId());
			child.setName("My tag " + i);
			child.setBody("My body " + i);
			
			TagsDAO.addTag(db, child);
		}
		
		List<TagBO> all = TagsDAO.getAllTagsFor(db, "//my/ns");
		assertEquals(101, all.size());
		
		for (int i=1; i <= 100; i++) {
			assertEquals(tag.getId(), all.get(i).getParentId());
		}
	}
	
}
