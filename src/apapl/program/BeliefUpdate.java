package apapl.program;

import java.util.ArrayList;
import java.util.LinkedList;

import apapl.APLModule;
import apapl.beliefinertia.BeliefInertiaModule;
import apapl.data.APLFunction;
import apapl.data.Literal;
import apapl.data.Query;
import apapl.data.True;


/**
 * A specification of the belief update action, consisting of 
 * a pre-condition, action, and post-condition.
 */
public class BeliefUpdate extends Rule
{
	private Query pre;
	private APLFunction act;
	private ArrayList<Literal> post;
	
	/**
	 * Variables for belief caching.
	 * 
	 * @author Marc van Zee (marcvanzee@gmail.com), Utrecht University
	 */
	
	// All rules that are possibly affected by this belief update, derived from atomClosure
	private LinkedList<Rule> relevantRules = new LinkedList<Rule>();
	
	/**
	 * Constructs a belief update.
	 *  
	 * @param pre the pre-condition
	 * @param act the action
	 * @param post the post-condition
	 */
	public BeliefUpdate(Query pre, APLFunction act, ArrayList<Literal> post)
	{
		this.pre = pre;
		this.act = act;
		this.post = post;
	}
	
	/**
	 * Add rules which are affected by this update
	 * 
	 * @author Marc van Zee (marcvanzee@gmail.com), Utrecht University
	 */
	public void addRelevantRules(LinkedList<Rule> rules) {
		relevantRules.addAll(rules);
	}
	
	/**
	 * Updates the updateBelief flag of all rules that can possibly be affected by this rule
	 * For belief caching
	 * 
	 * @author Marc van Zee (marcvanzee@gmail.com), Utrecht University
	 * 
	 */
	public void setBeliefFlags() {
		for (Rule r : this.relevantRules) {
			r.inertia = false;
		}
	}
	
	/**
	 * Returns the pre-condition of this belief update.
	 * 
	 * @return teh pre-condition
	 */
	public Query getPre()
	{
		return pre;
	}
	
	/**
	 * Returns the action of this belief update.
	 * 
	 * @return the action
	 */
	public APLFunction getAct()
	{
		return act;
	}
	
	/**
	 * Returns the post-condition of this belief update.
	 * 
	 * @return the post-condition
	 */
	public ArrayList<Literal> getPost()
	{
		return post;
	}
	
	/**
	 * Pretty print. Used for displaying.
	 * 
	 * @return the string corresponding to this belief update
	 */
	public String pp()
	{
		return toString();
	}
	
	/**
	 * Pretty print. Used for displaying.
	 * 
	 * @param a the number of spaces before the action
	 * @param b the number of spaeces after the action
	 * @return  the string corresponding to this belief update
	 */
	public String pp(int a, int b)
	{
		String l = (pre instanceof True?"":pre+"");
		String m = act+"";
		String r = Base.concatWith(post,", ");
		
		return "{ "+l+" }"+spaces(a+1-l.length())+m+spaces(b+1-m.length())+" { "+r+" }";
	}
	
	/**
	 * Returns a string of <code>j</code> spaces.
	 * 
	 * @param j the number of spaces
	 * @return the string of <code>j</code> spaces
	 */
	private String spaces(int j)
	{
		String r="";
		for (int i=0; i<j; i++) r=r+" ";
		return r;
	}

	/**
	 * Converts this belief update to a string representation.
	 * 
	 * @return the string representation
	 */
	public String toString()
	{
		String lit = "";
		for (Literal l : post) lit = lit + l.toString(false) + ", ";
		if (lit.length()>=2) lit = lit.substring(0,lit.length()-2);	
		return "{ "+(pre instanceof True?"":pre.toString(false))+" } "+act.toString(false)+" { "+lit+" }";
	}

	/**
	 * Converts this belief update to a RTF representation.
	 * 
	 * @return the RTF representation
	 */
	public String toRTF()
	{
		String r = "";
		for (Literal l : post) r = r + l.toRTF() + ", ";
		if (r.length()>=2) r = r.substring(0,r.length()-2);	
		
		return "\\{ "+(pre instanceof True?"":pre.toRTF()+"")+" \\} "+act.toRTF(false)+" \\{ "+r+" \\}";
	}
	
	/**
	 * Converts this belief update to a RTF representation.
	 * 
	 * @param a
	 * @param b
	 * @return the RTF representation
	 */
	public String toRTF(int a, int b)
	{
		String l = (pre instanceof True?"":pre.toRTF()+"");
		String m = act.toRTF(false)+"";
		String r = "";
		for (Literal i : post) r = r + i.toRTF() + ", ";
		if (r.length()>=2) r = r.substring(0,r.length()-2);	
		
		return "{ "+l+" }"+spaces(a+1-l.length())+m+spaces(b+1-m.length())+" { "+r+" }";
	}
	
	/**
	 * Returns a list of variables that can become bounded in this rule.
	 * 
	 * @return the list of variables that can become bounded
	 */
	public ArrayList<String> canBeBounded()
	{
		ArrayList<String> canBeBounded = pre.getVariables();
		canBeBounded.addAll(act.getVariables());
		return canBeBounded;
	}
	
	/**
	 * Returns a list of variables that must become bounded in this rule.
	 * 
	 * @return the list of variables that must become bounded 
	 */
	public ArrayList<String> mustBeBounded()
	{
		ArrayList<String> mustBeBounded = new ArrayList<String>();
		for (Literal l : post) mustBeBounded.addAll(l.getVariables()); 
		return mustBeBounded;
	}

	/**
	 * Returns the type of this rule as string.
	 * 
	 * @return string corresponding to the type of this rule
	 */
	public String getRuleType()
	{
		return "belief update";
	}
	
	/**
	 * Clones this belief update.
	 * 
	 * @return the clone
	 */
	public BeliefUpdate clone()
	{
		ArrayList<Literal> postclone = new ArrayList<Literal>();
		for (Literal l : post) postclone.add(l.clone());
		BeliefUpdate b = new BeliefUpdate(pre.clone(),act.clone(),postclone);
		b.setRelevantRules(this.relevantRules);
		
		return b;
	}
	
	/**
	 * Returns all the variables that occur in this belief update.
	 * 
	 * @return the variables
	 */
	public ArrayList<String> getVariables()
	{
		ArrayList<String> vars = pre.getVariables();
		vars.addAll(act.getVariables());
		for (Literal l : post) vars.addAll(l.getVariables());
		return vars;
	}
	
	/**
	 * Turn all variables of this belief update into fresh (unique) variables.
	 * 
	 * @param unfresh the list of variables that cannot be used anymore
	 * @param own all the variables in the context of this belief update
	 * @param changes list [[old,new],...] of substitutions
	 */
	public void freshVars(ArrayList<String> unfresh, ArrayList<String> own, ArrayList<ArrayList<String>> changes)
	{
		pre.freshVars(unfresh,own,changes);
		act.freshVars(unfresh,own,changes);
		for (Literal l : post) l.freshVars(unfresh,own,changes);
	}

	public void setRelevantRules(LinkedList<Rule> rules) {
		this.relevantRules = rules;
	}
}
