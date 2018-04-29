package com.oy.tv.dao.know;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.oy.tv.dao.DAOTestBase;
import com.oy.tv.schema.core.CorpusBO;
import com.oy.tv.schema.core.CustomerBO;
import com.oy.tv.schema.core.ProjectionBO;
import com.oy.tv.schema.core.TermBO;
import com.oy.tv.schema.core.TermsBO;

public final class DAOTests extends DAOTestBase {

	public DAOTests() {
		super("daotests_ITY_KNOW", new AllDAO());
	}
	
	public void testCorpus() throws SQLException {
		CustomerBO owner = new CustomerBO();		
		owner.setId(123);

		for (int i=0; i < 5; i++){
  		CorpusBO corpus = CorpusDAO.insert(db, owner, "Test name " + i, "Test desc " + i);		
  		assertEquals("Test name " + i, corpus.getName());
  		assertEquals("Test desc " + i, corpus.getDescription());
		}

		List<CorpusBO> all = CorpusDAO.lookup(db, owner);
		assertEquals(5, all.size());
		
		for (int i=0; i < 5; i++){
			CorpusBO corpus = all.get(i);
  		assertEquals("Test name " + i, corpus.getName());
  		assertEquals("Test desc " + i, corpus.getDescription());
		}
	}

	public void testDeletedInvisible() throws SQLException {
		CustomerBO owner = new CustomerBO();		
		owner.setId(234);

		CorpusBO corpus = CorpusDAO.insert(db, owner, "Test name", "Test desc");
		CorpusBO loaded = CorpusDAO.load(db, corpus.getId());
		
		assertEquals(corpus.getId(), loaded.getId());
		assertEquals(corpus.getName(), loaded.getName());
		assertEquals(corpus.getDescription(), loaded.getDescription());
		assertEquals(corpus.getAcl(), loaded.getAcl());
		
		assertTrue(CorpusDAO.delete(db, corpus));

		List<CorpusBO> all = CorpusDAO.lookup(db, owner);
		assertEquals(0, all.size());
		assertEquals(null, CorpusDAO.load(db, corpus.getId()));
	}
	
	public void testTermsInsert() throws SQLException {
		CustomerBO owner = new CustomerBO();		
		owner.setId(345);
		CorpusBO corpus = CorpusDAO.insert(db, owner, "Test name", "Test desc");
		
		TermBO term = TermDAO.lookup(db, corpus, "/dims/1", "/values/1", "/types/1");
		assertEquals(null, term);
		
		term = TermDAO.insert(db, corpus, "/dims/1", "/values/1", "/types/1", "/categories/1");
		TermBO loaded = TermDAO.lookup(db, corpus, "/dims/1", "/values/1", "/types/1");

		assertEquals(term.getId(), loaded.getId());
		assertEquals(term.getDimention(), loaded.getDimention());
		assertEquals(term.getValue(), loaded.getValue());
		assertEquals(term.getType(), loaded.getType());
		assertEquals(term.getCategory(), loaded.getCategory());
	}

	public void testLinkCrossTypeFailsCategoryDoesNot() throws SQLException {
		CustomerBO owner = new CustomerBO();		
		owner.setId(456);
		CorpusBO corpus = CorpusDAO.insert(db, owner, "Test name", "Test desc");

		try {
			TermBO from = TermDAO.insert(db, corpus, "/dims/from", "/values/from", "/types/from", "/categories/1");
			TermBO to = TermDAO.insert(db, corpus, "/dims/to", "/values/to", "/types/1", "/categories/1");
			TermDAO.link(db, from, to, "/relations/1", 0);
			fail();
		} catch (Exception e){}

		{
			TermBO from = TermDAO.insert(db, corpus, "/dims/from", "/values/from", "/types/1", "/categories/1");
			TermBO to = TermDAO.insert(db, corpus, "/dims/to", "/values/to", "/types/1", "/categories/2");
			TermsBO terms = TermDAO.link(db, from, to, "/relations/1", 1);
			
			assertEquals("/relations/1", terms.getType());
			assertEquals(from, terms.getFrom());
			assertEquals(to, terms.getTo());
		}
	}
	
	public void testCorpusTypesDimentionsCategories() throws SQLException {
		CustomerBO owner = new CustomerBO();		
		owner.setId(890);
		CorpusBO corpus = CorpusDAO.insert(db, owner, "Test name", "Test desc");
		for (int i=0; i < 5; i++){
			TermBO from = TermDAO.insert(db, corpus, "/dims/1-" + i, "/values/from", "/types/" + i, "/categories/1-" + i);
			TermBO to = TermDAO.insert(db, corpus, "/dims/2-" + i, "/values/to", "/types/" + i, "/categories/2-" + i);
			TermDAO.link(db, from, to, "/relations/is-a", i);
  	}

		corpus = CorpusDAO.load(db, corpus.getId());
		assertEquals(10, corpus.getDimentions().size());
		assertEquals(5, corpus.getTypes().size());
		assertEquals(10, corpus.getCategories().size());
	}

	public void testProjectOrphans() throws SQLException {
		CustomerBO owner = new CustomerBO();		
		owner.setId(2345);
		CorpusBO corpus = CorpusDAO.insert(db, owner, "Test name", "Test desc");
	
		TermBO one = TermDAO.insert(db, corpus, "/dims/1", "/values/1/1", "/types/1", "/categories/1");
		TermBO onex = TermDAO.insert(db, corpus, "/dims/1", "/values/1/2", "/types/1", "/categories/1");
		TermBO two = TermDAO.insert(db, corpus, "/dims/2", "/values/2/1", "/types/1", "/categories/1");
		TermBO twox = TermDAO.insert(db, corpus, "/dims/2", "/values/2/2", "/types/1", "/categories/1");
		
		TermDAO.link(db, one ,two, "/rel/1", 1);
		ProjectionBO proj = TermDAO.project(db, corpus, "/dims/1", "/dims/2", "/rel/1");
		
		assertEquals(3, proj.getTerms().size());
		assertTermsEquals(one, proj.getTerms().get(0).getFrom());
		assertTermsEquals(two, proj.getTerms().get(0).getTo());
		assertEquals(1, proj.getTerms().get(0).getIndex());
		
		assertTermsEquals(onex, proj.getTerms().get(1).getFrom());
		assertEquals(null, proj.getTerms().get(1).getTo());
		
		assertTermsEquals(twox, proj.getTerms().get(2).getTo());
		assertEquals(null, proj.getTerms().get(2).getFrom());
	}
	
	public void testTermsProject() throws SQLException {
		CustomerBO owner = new CustomerBO();		
		owner.setId(567);
		CorpusBO corpus = CorpusDAO.insert(db, owner, "Test name", "Test desc");
		CorpusBO corpusEx = CorpusDAO.insert(db, owner, "Test name 2", "Test desc 2");

		// same type
		TermBO [] froms = new TermBO [5];
		TermBO [] tos = new TermBO [5];
		for (int i=0; i < 5; i++){
			froms[i] = TermDAO.insert(db, corpus, "/dims/1", "/values/from", "/types/1", "/categories/1");
			tos[i] = TermDAO.insert(db, corpus, "/dims/2", "/values/to", "/types/1", "/categories/1");
			TermDAO.link(db, froms[i], tos[i], "/relations/is-a", i);
  	}
				
		// bad relation
		{
  		ProjectionBO proj = TermDAO.project(db, corpus, "/dims/1", "/dims/2", "/relations/foo");
  		assertEquals(10, proj.getTerms().size());
  		
  		for (int i=0; i < 5; i++){
  			assertEquals(null, proj.getTerms().get(i).getTo());
  			assertTermsEquals(froms[i], proj.getTerms().get(i).getFrom());
  		}
  		for (int i=0; i < 5; i++){
  			assertTermsEquals(tos[i], proj.getTerms().get(i + 5).getTo());
  			assertEquals(null, proj.getTerms().get(i + 5).getFrom());
  		}
		}
		
		// orphans
		TermDAO.insert(db, corpus, "/dims/1", "/values/from-orphan", "/types/2", "/categories/1");
		TermDAO.insert(db, corpus, "/dims/2", "/values/to-orphan", "/types/2", "/categories/1");

		// fakes from wrong corpus
		TermDAO.insert(db, corpusEx, "/dims/1", "/values/from-orphan", "/types/3", "/categories/1");
		TermDAO.insert(db, corpusEx, "/dims/2", "/values/to-orphan", "/types/3", "/categories/1");
		
		{
  		ProjectionBO proj = TermDAO.project(db, corpus, "/dims/1", "/dims/2", "/relations/is-a");
  		assertEquals(7, proj.getTerms().size());
		}
		
		{
			CorpusBO corpusXy = CorpusDAO.load(db, corpus.getId());
			assertEquals(new ArrayList<String>(Arrays.asList(new String [] {"/types/1", "/types/2"})), 
					corpusXy.getTypes());
		}
	}

	public void testIndexIsUnique() throws Exception {
		CustomerBO owner = new CustomerBO();		
		owner.setId(345);
		CorpusBO corpus = CorpusDAO.insert(db, owner, "Test name", "Test desc");
		
		TermBO term1 = TermDAO.insert(db, corpus, "/dims/1", "/values/1", "/types/1", "/categories/1");
		TermBO term2 = TermDAO.insert(db, corpus, "/dims/2", "/values/2", "/types/1", "/categories/2");

		TermDAO.link(db, term1, term2, "/is-a", 1);
		try {
  		TermDAO.link(db, term1, term2, "/has-a", 1);
  		fail();
		} catch (Exception e){}
	}
	
	public void testProjectByType() throws Exception {
		CustomerBO owner = new CustomerBO();		
		owner.setId(901);
		CorpusBO corpus = CorpusDAO.insert(db, owner, "Test name", "Test desc");
		
		TermBO term1 = TermDAO.insert(db, corpus, "/dims/1", "/values/1", "/types/1", "/categories/1");
		TermBO term2 = TermDAO.insert(db, corpus, "/dims/2", "/values/2", "/types/1", "/categories/2");

		TermDAO.link(db, term1, term2, "/is-a", 1);
		TermDAO.link(db, term1, term2, "/has-a", 2);

		assertEquals(1, TermDAO.project(db, corpus, 
				"/dims/1", "/dims/2", "/has-a").getTerms().size());

		assertEquals(1, TermDAO.project(db, corpus, 
				"/dims/1", "/dims/2", "/is-a").getTerms().size());
	
		assertEquals(2, TermDAO.project(db, corpus, 
				"/dims/1", "/dims/2").getTerms().size());

		ProjectionBO proj = TermDAO.project(db, corpus, "/dims/1", "/dims/2", "/non-existent"); 
		assertEquals(2, proj.getTerms().size());
		assertTermsEquals(term1, proj.getTerms().get(0).getFrom());
		assertEquals(null, proj.getTerms().get(0).getTo());
		assertEquals(null, proj.getTerms().get(1).getFrom());
		assertTermsEquals(term2, proj.getTerms().get(1).getTo());
	}
	
	private static  void assertTermsEquals(TermBO from, TermBO to){
		assertEquals(from.getId(), to.getId());
		assertEquals(from.getCategory(), to.getCategory());
		assertEquals(from.getCorpusId(), to.getCorpusId());
		assertEquals(from.getDimention(), to.getDimention());
		assertEquals(from.getType(), to.getType());
		assertEquals(from.getValue(), to.getValue());
	}
	
}
