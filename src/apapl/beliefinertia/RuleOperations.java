package apapl.beliefinertia;

import java.util.ArrayList;
import java.util.HashSet;

import apapl.SubstList;
import apapl.data.APLFunction;
import apapl.data.Literal;
import apapl.data.Query;
import apapl.data.Term;
import apapl.plans.PlanSeq;

/**
 * Some helper methods that are used throughout the belief inertia package 
 * 
 * @author Marc van Zee (marcvanzee@gmail.com) - Utrecht University
 *
 */
public class RuleOperations {
	/**
	 * Check whether the head and the guard of a PG rule share variables
	 * @param head
	 * @param body
	 * @return
	 */
	public static boolean isConnected(Query head, Query body) {
		return isConnected(head.getVariables(), body.getVariables());
	}
	
	/**
	 * Check whether the head and the guard of a PC rule share variables
	 * @param head
	 * @param body
	 * @return
	 */
	public static boolean isConnected(APLFunction head, Query body) {
		return isConnected(head.getVariables(), body.getVariables());
	}
	
	/**
	 * Check whether the head and the guard of a PR rule share variables
	 * @param head
	 * @param body
	 * @return
	 */
	public static boolean isConnected(PlanSeq head, Query body) {
		return isConnected(head.getVariables(), body.getVariables());
	}
	
	/**
	 * Compare two substitutions and return true if they are equal
	 * @param sub1 the first substitution
	 * @param sub2 the third substitution, no just kidding it's the second.
	 * @return
	 */
	public static boolean equalSubs(SubstList<Term> sub1, SubstList<Term> sub2) {
		for (String t1 : sub1.keySet()) {
			if (sub2.get(t1) == null || !sub1.get(t1).equals(sub2.get(t1))) {
					return false;
			}
		}
		
		return true;
	}
	
	/**
	 * Check whether the head and the guard share variables
	 */
	private static boolean isConnected(ArrayList<String> a, ArrayList<String> b) {
		for (String var1 : a) {
			for (String var2 : b) {
				if (var1.equals(var2)) {
					return true;
				}
			}
		}
		
		return false;
	}
	
	/**
	 * Returns the predicate of a literal: not p(X) --> p
	 * 
	 * @param l The literal
	 * @return The predicate as a string
	 */
	public static String getPredicate(Literal l) {
		String s = l.getBody().toString();
		return getPredicate(s);
	}
	
	/**
	 * Returns the predicate of an atom: p(X) --> p
	 * 
	 * @param s The atom as a string
	 * @return The predicate as a string
	 */
	public static String getPredicate(String s) {
		while (s.startsWith(" ")) s = s.substring(1);
		s =  s.substring(0, (s.indexOf("(") < 1 ? s.length() : s.indexOf("(")));
		while (s.endsWith(" ")) s= s.substring(0, s.length()-1);
		
		return s;
	}
	
	/**
	 * Returns true if the input set of predicates contains an impure function
	 * 
	 * @param set The input set of predicates, without arguments or braces
	 * @return true if the input set of predicates contains an impure function
	 */
	public static boolean containsImpureSymbol(HashSet<String> set)
	{
		for (String s : BeliefInertiaParam.IMPURE_FUNCTIONS)
			if (set.contains(s)) return true;
		return false;
	}
}
