package apapl.beliefinertia.dependencyset;

import java.util.HashSet;

import apapl.APLModule;
import apapl.beliefinertia.RuleOperations;
import apapl.data.Literal;
import apapl.data.Query;
import apapl.program.Rule;

/**
 * A Dependency set build will build the dependency set (what in a name?) of
 * the guards of all the rules using the inference rules of a belief base.
 * 
 * @author Marc van Zee (marcvanzee@gmail.com) - Utrecht University
 *
 */
public class DependencySetBuilder 
{
	InferenceRules inferenceRules;
	APLModule m;
	
	public DependencySetBuilder(InferenceRules rules)
	{
		this.inferenceRules = rules;
	}
	
	/**
	 * Build the dependency sets for the guard of the rules and store them in the rules as well.
	 * 
	 * @param a
	 */
	public void build(APLModule a) 
	{
		for (Rule rule : a.getAllRules())
		{
			Query guard = rule.getGuard();
			rule.dependencySet = dependencySet(guard);
			
			// not all rules should participate in inertia, because reserved terms such as
			// "rand" (generates a random number x s.t. 0 < x < 1) will always have a 
			// different outcome. we call these rule "unpure" rules
			if (RuleOperations.containsImpureSymbol(rule.dependencySet))
				rule.exclude = true;
		}
	}
	
	/**
	 * Recursively calculate the depdendency set of a query by taking each literal
	 * in the query and collect all predicates that are in the body of all inference
	 * rules of which the literal is the head of. Repeat this until the set does not
	 * longer increase (fixed point).
	 * 
	 * @param query
	 * @return
	 */
	public HashSet<String> dependencySet(Query query) {
		HashSet<String> set = new HashSet<String>();
		
		for (Literal lit : query.toLiterals()) {
			String pred = RuleOperations.getPredicate(lit);
			set.addAll(dependencySet(pred, inferenceRules));
		}
		
		return set;
	}
	
	/**
	 * Calculate the dependency set of a predicate and use the inference rules for this.
	 * 
	 * @param pred
	 * @param beliefbase
	 * @return
	 */
   public HashSet<String> dependencySet(String pred, InferenceRules inferenceRules) 
   {
    	HashSet<String> retSet = new HashSet<String>();
    	InferenceRules infCopy = inferenceRules.clone();
    	
    	retSet.add(pred);
		
    	if (infCopy.containsKey(pred)) 
    	{    	
    		// add all predicates that are in the base of this rule
			HashSet<String> body = infCopy.get(pred);
			retSet.addAll(body);
			infCopy.remove(pred);
			for (String predBody : body) 
			{
				HashSet<String> depp = dependencySet(predBody, infCopy);
				retSet.addAll(depp);
			}
	
    	}
    	return retSet;
    }
}
