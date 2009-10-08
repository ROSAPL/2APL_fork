package apapl.program;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import apapl.APLModule;
import apapl.ActivationGoalAchievedException;
import apapl.ModuleDeactivatedException;
import apapl.SubstList;
import apapl.data.APLIdent;
import apapl.data.GoalCompare;
import apapl.data.Literal;
import apapl.data.OrQuery;
import apapl.data.Query;
import apapl.data.Term;
import apapl.data.True;
import apapl.plans.Plan;
import apapl.plans.PlanSeq;

/**
 * The base in which the {@link apapl.plans.PlanSeq}s the module is currently 
 * executing are stored. The plan base can be iterated over by an iterator. 
 */
public class Planbase extends Base implements Iterable<PlanSeq>
{
	List<PlanSeq> plans;
	
	/**
	 * Constructs a new empty plan base.
	 */		
	public Planbase()
	{
		plans = new ArrayList<PlanSeq>();
	}
	
	/**
	 * Checks whether a plan that has been generated by PG-rule <code>rule</code> 
	 * occurs in the plan base.
	 * 
	 * @param rule the PG-rule
	 * @return true if there is a plan that has been generated 
	 *   by <code>rule</code>, false otherwise.
	 */
	public boolean ruleOccurs( PGrule rule )
	{
	  for (PlanSeq ps : plans)
		{ if( ps.getActivationRule() == rule )
		  { return( true );
			}
		}
		return( false );
	}

	/**
	 * Checks whether a rule occurs in this plan base that has been generated
	 * by <code>rule</code> for the same goal, i.e. with the same substitution
	 * applied to it as that was used for <code>rule</code>.
	 * 
	 * @param rule the rule to check
	 * @param theta the substitution that has been applied for <code>rule</code>
	 * @return true if the same rule is active for the same goal, false
	 *   otherwise
	 */
	public boolean sameRuleActiveForSameGoal( PGrule rule, SubstList<Term> theta )
	{
	  // Check rule against all plans in the planbase
		for (PlanSeq ps : plans)
		{ 
		  // If the plan is not an initial plan (no activationGoal) and the same
		  // rule has been used, we check if the heads of the rules with
			// substiutions applied to them are the same. 
			if( ps.getActivationGoal() != null && rule == ps.getActivationRule() )
			{ 
			  // Apply substitution to head of both rules
			  Query g1 = ps.getActivationRule().getHead().clone();
		    g1.applySubstitution( ps.getActivationGoal() );
			  Query g2 = rule.getHead().clone();
		    g2.applySubstitution( theta );

				

        // Sort the literals that occur in the head of each rule
			  // Note that a goal 'a and b' is equal to 'b and a'
			  LinkedList<Literal> lits1 = g1.toLiterals();
			  Collections.sort(lits1,GoalCompare.INSTANCE);
			  LinkedList<Literal> lits2 = g2.toLiterals();
			  Collections.sort(lits2,GoalCompare.INSTANCE);
			
			  // Compare them based on string comparison. In the future this should be
			  // implemented by means of a compare in the Query class.
				if( lits1.toString().equals(lits2.toString()) )
			  { return true;
		  	}
			}	
		}
		return false;
	}

	/**
	 * Checks whether the module is already working on a goal, i.e. a plan
	 * occurs in the plan base that has been generated for this goal.
	 * 
	 * @param goal the goal to check
	 * @return true if some plan occurs that has been generated for this goal,
	 *   false otherwise
	 */
	public boolean workingOnGoal( Query goal )
	{
	  // Check goal against all plans in the planbase
		for (PlanSeq ps : plans)
		{ // If the plan is not an initial plan (no activationGoal) we check if the
		  // plan in the planbase is generated for the same goal. In fact, we
			// compare the heads of the pg-rules with corresponding substitutions.
		  if( ps.getActivationGoal() != null )
			{ Query g = ps.getActivationRule().getHead().clone();
		    g.applySubstitution( ps.getActivationGoal() );

        // Sort the literals that occur in the head of each rule
			  // Note that a goal 'a and b' is equal to 'b and a'
			  LinkedList<Literal> lits1 = goal.toLiterals();
			  Collections.sort(lits1,GoalCompare.INSTANCE);
			  LinkedList<Literal> lits2 = g.toLiterals();
			  Collections.sort(lits2,GoalCompare.INSTANCE);
			
			  // Compare them based on string comparison. In the future this should be
			  // implemented by means of a compare in the Query class.
				if( lits1.toString().equals(lits2.toString()) )
			  { return true;
		  	}
			}	
		}
		// Same goal could not be found
		return false;
	}

	/**
	 * Executes all plans in this plan base.
	 * 
	 * @param module the module that executes the actions
	 * @return true if some plan has been executed, false otherwise
	 */
	public boolean executeAll(APLModule module) throws ModuleDeactivatedException
	{
		boolean planexecuted = false;
		if (plans.size()==0) {return planexecuted;}
	
		ArrayList<PlanSeq> toRemove = new ArrayList<PlanSeq>();
		for (PlanSeq p : plans) {
			try {
				boolean e = p.execute(module);
				planexecuted = planexecuted || e ;
				if (p.isEmpty()) toRemove.add(p);
			}
			catch (ActivationGoalAchievedException e) {toRemove.add(p); planexecuted = true;}
			catch (ModuleDeactivatedException e) {throw e;}
		}
		for (PlanSeq p : toRemove) plans.remove(p);
		return planexecuted;
	}
	
	/**
	 * Executes the first plan in this plan base.
	 * 
	 * @param module The module that executes the actions
	 * @return true if a plan has been executed, false otherwise
	 */
	public boolean executeOne(APLModule module) throws ModuleDeactivatedException
	{
		if (plans.size()==0) {return false;}
		
		PlanSeq p = plans.get(0);
		try {
			boolean e = p.execute(module);
			if (p.isEmpty()) plans.remove(p);
			return e;
		}
		catch (ActivationGoalAchievedException e) {plans.remove(p); return false;}
		catch (ModuleDeactivatedException e) { throw e; }
	}
	
	/**
	 * Return the plan identified by <code>id</code>
	 * 
	 * @param id Id of the plan to return.
	 * @return the plan corresponding to id, null if this plan could not be found
	 */
	public PlanSeq getPlan(int id)
	{
		for (PlanSeq p : plans) 
			if (p.getID()==id) 
				return p;
		
		return null;
	}
	
	
	/**
	 * Adds a plan to this plan base.
	 * 
	 * @param p the plan to be added to the plan base
	 */
	public void addPlan(PlanSeq p)
	{
		plans.add(p);
	}

	/**
	 * Removes a plan from this plan base.
	 * 
	 * @param p the plan to be removed from the plan base.
	 */
	public void removePlan(PlanSeq p)
	{
		plans.remove(p);
	}	
	
	/**
	 * Convert this plan base to a string representation.
	 */
	public String toString()
	{
		String s = "";
		for (PlanSeq p : plans)
			s = s + p.pp(0)+",\n";
		
		if (s.length()>=2) s = s.substring(0,s.length()-2);	
		
		return s+"\n\n";
	}

	/**
	 * Convert this plan base to a RTF representation.
	 */
	public String toRTF()
	{
		String s = "";
		for (PlanSeq p : plans)
			s = s + p.toRTF(0)+",\\par\n";
		
		if (s.length()>=6) s = s.substring(0,s.length()-6);	
		
		return s;
	}
	
	/**
	 * Returns an iterator to iterate over the plans in the plan base.
	 * 
	 * @return the iterator
	 */
	public Iterator<PlanSeq> iterator()
	{
		return plans.iterator();
	}
	
	/**
	 * Clones the planbase
	 * @return deep clone of the planbase
	 */
	public Planbase clone()
	{
		Planbase pb = new Planbase();
		for(PlanSeq p : plans)
		{
			pb.addPlan(p.clone());
		}
		return pb;
	}
	
	/**
	 * Performs a test on the planbase
	 * @param query
	 * @return true if the test succeeded, false otherwise
	 * @throws Exception if the plan query has unsupported syntax
	 */
	public boolean doTest(Query query) 
	throws Exception
	{
		if (query instanceof True) 
			return true;		
		else if (query instanceof Literal) {		
			// Iterate over plans in the planbase
			for(PlanSeq p : plans) {
				if (plans.size() > 0) {	
					Plan fp = p.getPlans().getFirst();	
					APLIdent plantype;
					if (((Literal)query).getBody() instanceof APLIdent)	{
						plantype = (APLIdent)((Literal)query).getBody();
					}
					else {
						throw new Exception("Plan query syntax error: Plan query must contain only ground atoms.");
					}				
					if (fp.isType(plantype)) 
						return true;				
				}
			}
		}		
		else if (query instanceof OrQuery) {
			return doTest(((OrQuery)query).getLeft()) || doTest(((OrQuery)query).getRight());
		}
		else {
			throw new Exception("Plan query syntax error: Plan query must be a disjunction of ground atoms.");
		}		
		
		return false;
	}	
}
