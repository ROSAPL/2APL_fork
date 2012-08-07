package apapl.beliefinertia.dependencyset;

import java.util.HashMap;
import java.util.HashSet;

import apapl.beliefinertia.BeliefInertiaParam;

/**
 * This class can store inference rules as Hash maps with the head of the predicate
 * as key and the predicates in the body in a Hash set as value. Both the predicates
 * and functions of a rule are stored, because if impure functions such as "random"
 * occur in the body, the rule is excluded from inertia. If two different rules with
 * the same predicate as head occur, they will be added to the same value.
 * For example, consider the following belief base:
 * 
 * p(X) :- q(X), r.
 * p(X) :- s, t(v(X)).
 * 
 * This will result in the following hash maps:
 * rulesPredicates = {p->{q,r,s,t}}
 * rulesFunctions  = {p->{v}}
 * 
 * Which is exactly what we want, because the rules
 * 
 * p :- q1,..,qn.
 * p :- r1,..,rn.
 * 
 * Mean
 * 
 * ( q1&...&qn -> p ) & (r1&...&rn) -> p
 * 
 * Which is equivalent to
 * 
 * (q1&...&qn&r1&...&rn) -> p
 * 
 * @author Marc van Zee (marcvanzee@gmail.com) - Utrecht University
 *
 */
public class InferenceRules 
{
	String curPred = "";
	HashMap<String,HashSet<String>> rulePredicates = new HashMap<String,HashSet<String>>();
	HashMap<String,HashSet<String>> ruleFunctions = new HashMap<String,HashSet<String>>();
	
	public InferenceRules() {}
	
	public InferenceRules(HashMap<String,HashSet<String>> rulePredicates, 
			HashMap<String,HashSet<String>> ruleFunctions)
	{
		this.rulePredicates = rulePredicates;
		this.ruleFunctions  = ruleFunctions;
	}
	
	public InferenceRules clone()
	{
		HashMap<String,HashSet<String>> rP = (HashMap<String, HashSet<String>>) this.rulePredicates.clone();
		HashMap<String,HashSet<String>> rF = (HashMap<String, HashSet<String>>) this.ruleFunctions.clone();
		
		return new InferenceRules(rP, rF);
	}
	
	public boolean containsKey(String p)
	{
		return this.rulePredicates.containsKey(p);
	}
	
	/**
	 * Retrieve all predicates and functions that are in the body of rules in which the argument
	 * is the head.
	 * 
	 * @param p The predicate of the head (without arguments and braces)
	 * @return A hash set of predicates
	 */
	public HashSet<String> get(String p)
	{
		HashSet<String> ret = new HashSet<String>();
		
		if (this.rulePredicates.containsKey(p))
		{
			ret.addAll(this.rulePredicates.get(p));
			ret.addAll(getImpureFunctions(p));
		}
		
		return ret;
	}
	
	/**
	 * Set the current head, this is used by the Prolog parser who collects
	 * the predicates of interence rules using this class
	 * 
	 * @param p
	 */
	public void setCurrentHead(String p)
	{
		this.curPred = p;
		if (!rulePredicates.containsKey(p))
		{
			rulePredicates.put(p, new HashSet<String>());
			ruleFunctions.put(p, new HashSet<String>());
		}
			
	}
	
	public void addPredicate(String p)
	{
		if (curPred != "")
			this.rulePredicates.get(curPred).add(p);
	}
	
	public void addFunction(String f)
	{
		if (curPred != "")
			this.ruleFunctions.get(curPred).add(f);
	}
	
	public void remove(String p)
	{
		rulePredicates.remove(p);
		ruleFunctions.remove(p);
	}
	
	/**
	 * Return all the functions in the body of the given head that are unpure, meaning that the 
	 * value is non-deterministic. This happens for example if "rand" occurs somewhere in the rule. 
	 * When a predicate is unpure, a query in which it occurs should always be carried out, meaning 
	 * that its value can never be inferred.
	 * 
	 */
	public HashSet<String> getImpureFunctions(String p)
	{
		HashSet<String> ret = new HashSet<String>();
		
		if (this.rulePredicates.containsKey(p))
		{
			for (String funct : BeliefInertiaParam.IMPURE_FUNCTIONS)
			{
				if (this.ruleFunctions.get(p).contains(funct))
					ret.add(funct);
			}
		}
		
		return ret;
	}
	
	public String toString()
	{
		return "head to predicates: " + rulePredicates + "\nhead to functions: " + ruleFunctions;
	}
}
