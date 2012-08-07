package apapl.plans;

import java.util.ArrayList;
import java.util.LinkedList;

import apapl.APLModule;
import apapl.SubstList;
import apapl.beliefinertia.BeliefInertiaModule;
import apapl.benchmarking.APLBenchmarkParam;
import apapl.benchmarking.APLBenchmarker;
import apapl.data.APLIdent;
import apapl.data.Literal;
import apapl.data.Term;
import apapl.program.Beliefbase;
import apapl.program.Goalbase;
import apapl.program.Rule;

/**
 * AssertBelief is a 2APL plan that directly adds or removes a single literal from the beliefbase of the executing agent.
 * It allows a 2APL programmer to add and remove beliefs without making use of a Beliefupdate. It is implemented using the
 * Jason-like notation: +belief for adding a belief, and -belief for removing it.
 * 
 * @author Marc van Zee (marcvanzee@gmail.com), Utrecht University
 *
 */
public class AssertBelief extends Plan {
	
	private Literal literal;

	/**
	 * Variables for belief caching.
	 * 
	 * @author Marc van Zee (marcvanzee@gmail.com), Utrecht University
	 */
	
	// All rules that are possibly affected by this belief update, derived from atomClosure
	private LinkedList<Rule> relevantRules = new LinkedList<Rule>();
	
	/**
	 * Constructs a new belief update action.
	 * @param literal the literal that will be asserted to the beliefbase
	 * @param positive whether this literal is positive or negative
	 */
	public AssertBelief(Literal literal)
	{
		this.literal = literal;
	}

	/**
	 * Executes this action.
	 * @param module The module to execute this action for
	 * @return true if the action succeeds, false otherwise
	 */
	public PlanResult execute(APLModule module)
	{		
		SubstList<Term> theta = new SubstList<Term>();
		Beliefbase beliefbase = module.getBeliefbase();
		Goalbase goalbase = module.getGoalbase();
		
		Literal lcopy = literal.clone();
		lcopy.applySubstitution(theta);
		
		APLBenchmarker.startTiming(module, APLBenchmarkParam.BEL_UPD);
		module.getBeliefbase().assertBelief(lcopy);
		APLBenchmarker.stopTiming(module, APLBenchmarkParam.BEL_UPD);

		parent.removeFirst();
		
		APLBenchmarker.startTiming(module, APLBenchmarkParam.GOAL_UPD);
		goalbase.removeReachedGoals(beliefbase);
		APLBenchmarker.stopTiming(module, APLBenchmarkParam.GOAL_UPD);
		
		setInertiaFlags();
		
		return new PlanResult(this, PlanResult.SUCCEEDED);
	}
		
	/**
	 * Applies substitution to this action.
	 * 
	 * @param theta Substitution to be applied
	 */
	public void applySubstitution(SubstList<Term> theta)
	{
		literal.applySubstitution(theta);
	}
		
	public String toRTF(int t)
	{
		return literal.toRTF(true);
	}
		
	
	/**
	 * @return All variables contained in this action
	 */	
	public ArrayList<String> getVariables()
	{
		return literal.getVariables();
	}
	
	/**
	 * 
	 * @return the belief update as an arraylist
	 */
	public ArrayList<Literal> getPost()
	{
		ArrayList<Literal> ret = new ArrayList<Literal>();
		ret.add(literal);
		return ret;
	}
	
	/**
	 * Clones this object.
	 */
	public AssertBelief clone()
	{
		AssertBelief copy = new AssertBelief(literal.clone());
		copy.addRelevantRules(relevantRules);
		return copy;
	}
	
	/**
	 * Gives a string representation of this object
	 */
	public String toString()
	{
		return (literal.getSign()?"+":"-") + literal.getBody().toString();
	}
	
	public void freshVars(ArrayList<String> unfresh, ArrayList<String> own, ArrayList<ArrayList<String>> changes)
	{
		literal.freshVars(unfresh,own,changes);
	}
	
	public APLIdent getPlanDescriptor() {
		return new APLIdent("beliefupdateaction");
	}
	

	
	/**
	 * Updates the updateBelief flag of all rules that can possibly be affected by this rule
	 * For belief caching
	 * 
	 */
	public void setInertiaFlags() {
		for (Rule r : this.relevantRules) {
			r.inertia = false;
		}
	}
	
	public void addRelevantRules(LinkedList<Rule> rules) {
		relevantRules.addAll(rules);
	}
}
