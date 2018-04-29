package com.oy.tv.model.core;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.sf.yacas.YacasEvaluatorEx;

import com.oy.tv.model.unit.UnitContext;
import com.oy.tv.model.unit.UnitProcessor;
import com.oy.tv.schema.core.UnitBO;
import com.oy.tv.util.XmlUtil;

public class VariationUniquesnessEvaluator {

	private boolean must(boolean desired, boolean actual){
		if (desired){
			if (actual){
				return true;
			} else {   
				return false;
			}
		} else {
			return true;
		}
	}
	  
	private boolean mustnot(boolean desired, boolean actual){
		if (desired){
			if (actual){
				return false;
			} else {
				return true;
			}
		} else {
			return true;
		}
	}
	
	public void evaluate(UnitBO unit, NumberKind must, NumberKind mustnot, List<String> all, List<String> undesired, List<String> localDuplicates, List<String> globalDuplicates, List<String> unique){
		YacasEvaluatorEx eval = YacasEvaluatorEx.leaseBegin();
		try {   
			Set<String> questions = new HashSet<String>();
			for (int i=0; i < all.size(); i++){
				UnitProcessor up = new UnitProcessor();
				
				up._doc = XmlUtil.loadXmlFrom(unit.getXml());
				up._ctx = new UnitContext();
				up._ctx.values = all.get(i);
				up._log = System.out;
				up._eval = eval;
							  
	 			up.evaluate();
	 			  
	 			{
	 				NumberKind actual = up._ctx.kind;
	 				boolean can = 
	 					must(must.hasComplex, actual.hasComplex)
	 					&&
	 					must(must.hasFraction, actual.hasFraction)
	 					&&
	 					must(must.hasMinus, actual.hasMinus)
	 					&&
	 					must(must.hasSqrt, actual.hasSqrt)
	 					
	 					&&
	 					
	 					mustnot(mustnot.hasComplex, actual.hasComplex)
	 					&&
	 					mustnot(mustnot.hasFraction, actual.hasFraction)
	 					&&
	 					mustnot(mustnot.hasMinus, actual.hasMinus)
	 					&&
	 					mustnot(mustnot.hasSqrt, actual.hasSqrt)
	 					  
	 					;
	 				
	 				if (!can){
	 					undesired.add(all.get(i));
	 					continue;
	 				}
	 			}

	 			{
		 			String question = up._ctx.question.trim(); 
		 			if (questions.contains(question)){
		 				globalDuplicates.add(all.get(i));
		 				continue;
		 			}
		 			questions.add(question);
	 			}
	 			
	 			{
		 			boolean clean = true;
		 			Set<String> choices = new HashSet<String>();
		 			for (int j=0; j < up._ctx.choices.size(); j++){
		 				String choice = up._ctx.choices.get(j).trim();
		 				
		 				if (choices.contains(choice)){
		 					localDuplicates.add(all.get(i));
		 					clean = false;
		 					break;
		 				} else {
		 					choices.add(choice);
		 				}  
		 			}
	
		 			if (clean){
		 				unique.add(all.get(i));
		 			}
	 			}
			}
			
			YacasEvaluatorEx.leaseComplete(eval);
		} catch (Exception e){
			YacasEvaluatorEx.leaseFail(eval);
			throw new RuntimeException("Failed to render unit.", e);
		}
	}
	
}
