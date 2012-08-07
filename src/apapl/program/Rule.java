package apapl.program;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;

import apapl.SubstList;
import apapl.data.Query;
import apapl.data.Term;
import apapl.plans.PlanSeq;

/**
 * The root class of all rules.
 */
public abstract class Rule
{
	/**
	 * Variables for belief caching - see apapl.beliefcaching
	 */
	public boolean connected;
	public boolean inertia = false;
	public boolean ruleApplied = false;
	public SubstList<Term> theta1 = new SubstList<Term>(),
							theta2 = new SubstList<Term>();
	
	public HashSet<String> dependencySet = new HashSet<String>();
	
	//  when the transitive closure of the guard of a rule contains the "rand" term
	//  inertia should never hold.
	public boolean exclude = false;
	
	// the guard and the body are general objects shared by all rules
	protected Query guard;
	protected PlanSeq body;
	
	/**
	 * Pretty print, for displaying the rule in a readable format.
	 * 
	 * @return the string representation of this rule
	 */
	public abstract String pp();
	
	/**
	 * Converts this rule to a RTF representation.
	 * 
	 * @return the RTF string
	 */
	public abstract String toRTF();
	
	/**
	 * Returns a list of variables that can become bounded in this rule.
	 * 
	 * @return the list of variables
	 */
	public abstract ArrayList<String> canBeBounded();
	
	/**
	 * Returns a list of variables that must become bounded in this rule.
	 * 
	 * @return the list of variables
	 */
	public abstract ArrayList<String> mustBeBounded();
	
	/**
	 * Returns the type of this rule as a string.
	 * 
	 * @return the type string
	 */
	public abstract String getRuleType();
	
	/**
	 * Checks the variables in this rule based on whether all variables that must become
	 * bounded can actually become bounded. Used for generating warnings.
	 * 
	 * @param warnings the list of warnings
	 */
	public void checkVars(LinkedList<String> warnings)
	{
		ArrayList<String> canBeBounded = canBeBounded();
		ArrayList<String> mustBeBounded = new ArrayList<String>();
		for (String s : mustBeBounded())
			if (!mustBeBounded.contains(s)) mustBeBounded.add(s);
		
		ArrayList<String> problemVars = new ArrayList<String>();

		for(String v : mustBeBounded)
			if (!canBeBounded.contains(v)) problemVars.add(v);
		
		if (problemVars.size()==1) {
			//warnings.add("Variable \""+problemVars.get(0)+"\" can not become bounded in " + getRuleType() + "  ""+this+"\"");
			warnings.add("Variable \\b "+problemVars.get(0)+"\\b0  can not become bounded in " + getRuleType() + " \""+toRTF()+"\"");
		}
		else if (problemVars.size()>1) {
			String warning = "Variables ";
			int s = problemVars.size();
			//for (int i=0; i<problemVars.size()-2; i++) warning = warning + "\"" + problemVars.get(i) + "\", ";
			for (int i=0; i<problemVars.size()-2; i++) warning = warning + "\\b " + problemVars.get(i) + "\\b0, ";
			//warning = warning + "\"" + problemVars.get(s-2) + "\" and \"" + problemVars.get(s-1) + "\"";
			warning = warning + "\\b " + problemVars.get(s-2) + "\\b0  and \\b " + problemVars.get(s-1) + "\\b0 ";
			warnings.add(warning+" can not become bounded in " + getRuleType() + " \""+toRTF()+"\"");
		}
	}
	
	/**
	 * Returns the variables that occur inside this rule.
	 * 
	 * @return the list of variables
	 */
	public abstract ArrayList<String> getVariables();

	/**
	 * Turn all variables of this rule into fresh (unique) variables.
	 * 
	 * @param unfresh the list of variables that cannot be used anymore
	 * @param own all the variables in the context of this rule
	 * @param changes list [[old,new],...] of substitutions
	 */
	public void freshVars(ArrayList<String> unfresh, ArrayList<String> own, ArrayList<ArrayList<String>> changes) {}
	
	/**
	 * Turn all variables of this rule into fresh (unique) variables.
	 * 
	 * @param unfresh the list of variables that cannot be used anymore
	 * @param own all the variables in the context of this rule
	 */	
	public void freshVars(ArrayList<String> unfresh, ArrayList<String> own)
	{
		freshVars(unfresh,own,new ArrayList<ArrayList<String>>());
	}
	
	
	/**
	 * Returns the body of this rule.
	 * 
	 * @return the body
	 */	
	public PlanSeq getBody()
	{
		return body;
	}

	/**
	 * Returns the guard of this rule.
	 * 
	 * @return the guard
	 */
	public Query getGuard()
	{
		return guard;
	}
}