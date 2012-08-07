package apapl.beliefinertia.ruleselectors;

import java.util.ArrayList;

import apapl.APLModule;
import apapl.SubstList;
import apapl.benchmarking.APLBenchmarkParam;
import apapl.benchmarking.APLBenchmarker;
import apapl.data.Goal;
import apapl.data.Query;
import apapl.data.Term;
import apapl.data.True;
import apapl.plans.PlanSeq;
import apapl.program.Beliefbase;
import apapl.program.Goalbase;
import apapl.program.PGrule;
import apapl.program.Planbase;

/**
 * The alternative PG rule selection class.
 * 
 * @author Marc van Zee (marcvanzee@gmail.com) - Utrecht University
 *
 */
public class PGruleSelector 
{
	/**
	 * Replaces the original rule selection method for PG rules in apapl.program.PGRulebase with
	 * one that uses belief inertia
	 * 
	 * @param goalbase goalbase that is needed to select a rule
	 * @param beliefbase beliefbase that is needed to select a rule
	 * @param planbase planbase that is needed to check whether a rule may be selected
	 * @param onlyone if true, only one plan will be generated
	 * @return an list containing one or more plans that can be generate with the PG rules
	 */
	public static ArrayList<PlanSeq> generatePlans(Goalbase goalbase, Beliefbase beliefbase, 
			Planbase planbase, boolean onlyone, ArrayList<PGrule> rules, APLModule m)
	{
		ArrayList<PlanSeq> plans = new ArrayList<PlanSeq>();
		boolean inertia;
			
		// for each rule
		for (PGrule pgrule : rules)
		{ 
			// excluded rules (rules that have a random element in the dependency set of
			// their guard) will never exert inertia
			pgrule.inertia = (pgrule.exclude) ? false : pgrule.inertia;
			
			// if it is a reactive rule, try to match the guard with the beliefs
			if (pgrule.getHead() instanceof True)
			{
				// we have evaluated the guard because the head is always applicable
				// therefore we reset inertia, but we store the previous result so we
				// can use it below
				inertia = pgrule.inertia;
				pgrule.inertia = true;
				
				if (testGuard(pgrule, beliefbase, new SubstList<Term>(), inertia, m)) {
					PlanSeq p = buildPlan(pgrule);
			
					// it should not be the case that the module is working on an instance of the same rule
					if (!planbase.ruleOccurs( p.getActivationRule() ) )
					{
						plans.add(p);
						planbase.addPlan(p);
						if (onlyone) return plans;
					}
				}
			}
			
			// if it is not a reactive rule, try to match the head with the goals
			else 
			{
				// if the rule is connected we should be more careful because goal and belief queries might
				// affect each other
				if (pgrule.connected) 
				{
					// we use prevHead to find out whether the previous goal substitution is still valid (i.e.
					// the head has not  changed)
					Query prevHead = pgrule.getHead().clone();
					prevHead.applySubstitution(pgrule.theta1);
					
					
					if (pgrule.inertia)
					{	
						// if we have inertia and we can still infer the same goal from the goal base
						// as before, there is not reason to query to belief base.
						if (benchmarkGBquery(prevHead, m, goalbase) != null)
						{
							// simply reuse the previous substitutions if the rule succeeded before
							if (pgrule.ruleApplied) {
								
								// Goal is still a goal of the module								
								if (!benchmarkBBquery(pgrule.getHead(), pgrule.theta1, m, beliefbase) && 
										!planbase.sameRuleActiveForSameGoal( pgrule, pgrule.theta2 ))
								{ 
									PlanSeq p = buildPlan(pgrule);
									plans.add(p);
									planbase.addPlan(p);
									if (onlyone) return plans;
								}
							}
						}
						
						// inertia was true, so we do not have to reset it
						
					} 
					
					// if the rule is connected and inertia is not true, we have to query to goal base 
					// and if we succeed the belief base as well
					else
					{
						// resetting inertia is handled in testHeadAndGuard
						if (testHeadAndGuard(pgrule, beliefbase, goalbase, planbase, m)) 
						{
							PlanSeq p = buildPlan(pgrule); 
						
							plans.add(p);
							planbase.addPlan(p);
							if (onlyone) return plans;	
						}
					}
				} 
				
				// the rule is not connected...
				else
				{
					// we can do the goal and belief queries independently, for the goalbase we always query the goalbase, but
					// for the belief base we try to use inertia 
					if (testHead(pgrule, goalbase, m)) {
						// we evaluate the guard, so reset inertia, but store it so we can use it below
						inertia = pgrule.inertia;
						pgrule.inertia = true;
						
						if (testGuard(pgrule, beliefbase, pgrule.theta1, inertia, m)) {
							PlanSeq p = buildPlan(pgrule);
				
							Query variantHead = pgrule.getHead().clone();
							
							// put the substitutions of the head and the guard in the new head
							variantHead.applySubstitution(pgrule.theta2);
							
							// Goal is still a goal of the module							
							if (!benchmarkBBquery(variantHead, pgrule.theta1, m, beliefbase) && 
									!planbase.sameRuleActiveForSameGoal( pgrule, pgrule.theta2 ))
							{
								plans.add(p);
								planbase.addPlan(p);
								if (onlyone) return plans;
							} 
						}
					}
				}
			}

		}
		
		return plans;
	}		

	private static boolean benchmarkBBquery(Query query, SubstList<Term> theta, APLModule m, Beliefbase bb) 
	{
		APLBenchmarker.startTiming(m, APLBenchmarkParam.BEL_QUERY);
		boolean ret = bb.doQuery(query, theta);
		APLBenchmarker.stopTiming(m, APLBenchmarkParam.BEL_QUERY);
		
		return ret;
	}
	
	private static SubstList<Term> benchmarkGBquery(Query query, APLModule m, Goalbase gb) 
	{
		APLBenchmarker.startTiming(m, APLBenchmarkParam.GOAL_QUERY);
		SubstList<Term> ret = gb.testGoal(query);
		APLBenchmarker.stopTiming(m, APLBenchmarkParam.GOAL_QUERY);
		
		return ret;
	}
	
	private static ArrayList<SubstList<Term>> benchmarkPossibleSubs(Query query, APLModule m, Goal goal) 
	{
		APLBenchmarker.startTiming(m, APLBenchmarkParam.GOAL_QUERY);
		ArrayList<SubstList<Term>> ret = goal.possibleSubstitutions(query);
		APLBenchmarker.stopTiming(m, APLBenchmarkParam.GOAL_QUERY);
		
		return ret;
	}


	/**
	 * TODO
	 * @param pgrule
	 * @param beliefbase
	 */
	private static boolean testGuard(PGrule pgrule, Beliefbase beliefbase, SubstList<Term> theta1, boolean inertia, APLModule m)
	{	
		// if we do not have inertia we have to query the belief base
		if (!inertia)
		{
			SubstList<Term> theta2 = theta1.clone();
						
			if (benchmarkBBquery(pgrule.getGuard(), theta2, m, beliefbase)) 
			{
				pgrule.ruleApplied = true;
				pgrule.theta2 = theta2;
				return true;
			} else 
			{
				pgrule.ruleApplied = false;
				return false;
			}
		} else
		{
			// if we do have inertia we simply return what we previously returned
			// if the rule was applied then the guard succeeded
			return pgrule.ruleApplied;
		}
	}
	
	private static boolean testHead(PGrule pgrule, Goalbase goalbase, APLModule m)
	{
		SubstList<Term> theta1;
		
		if ((theta1 = benchmarkGBquery(pgrule.getHead(), m, goalbase)) != null) {
			pgrule.theta1 = theta1;
			
			// overwrite substitutions that have possibly changed
			pgrule.theta2.putAll(theta1);
			return true;
		}
		return false;
	}
	
	private static boolean testHeadAndGuard(PGrule pgrule, Beliefbase beliefbase, Goalbase goalbase, Planbase planbase, APLModule m) 
	{
		for (Goal goal : goalbase)
		{
			ArrayList<SubstList<Term>> substs;
			PGrule variant = pgrule.getVariant(goal.getVariables());
			substs = benchmarkPossibleSubs(variant.getHead(), m, goal);
		
			// for all possible substitutions of the head of the rule, try to match
			// it with the guard of the rule
			for (SubstList<Term> theta1 : substs)
			{
				variant.applySubstitution(theta1);
				// if the goal cannot be entailed from the belief base
				// and we are not working for the same goal already
				// and the guard is satisfied
				
				// ** note: we separate the test for the head and the guard, because we only should reset the
				//          inertia if the guard has been evaluated
				if (!benchmarkBBquery(variant.getHead(), theta1, m, beliefbase) 
						&& !planbase.sameRuleActiveForSameGoal( pgrule, theta1 ))
				{
					// we will now query the guard, so reset inertia
					pgrule.inertia = true;
					
					if (testGuard(variant, beliefbase, theta1, false, m)) {
						pgrule.theta1 = theta1;
						pgrule.ruleApplied = variant.ruleApplied;
						pgrule.theta2 = variant.theta2;
						return true;
					}
				}
			}
		}
		
		pgrule.ruleApplied = false;
		return false;
	}
	
	private static PlanSeq buildPlan(PGrule pgrule) 
	{
		PGrule variant = pgrule.clone();
		PlanSeq p = variant.getBody();
		p.applySubstitution(variant.theta2);
		p.setActivationRule(pgrule);
		p.setActivationGoal(variant.theta1); // TODO: perhaps clone?
		p.setActivationSubstitution(variant.theta2); // TODO: perhaps clone?
		
		return p;
	}
}
