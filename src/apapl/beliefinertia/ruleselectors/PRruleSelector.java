package apapl.beliefinertia.ruleselectors;

import java.util.ArrayList;

import apapl.APLModule;
import apapl.PlanUnifier;
import apapl.SubstList;
import apapl.beliefinertia.RuleOperations;
import apapl.benchmarking.APLBenchmarkParam;
import apapl.benchmarking.APLBenchmarker;
import apapl.data.Query;
import apapl.data.Term;
import apapl.deliberation.ProcessIEventsResult;
import apapl.plans.PlanSeq;
import apapl.program.Beliefbase;
import apapl.program.PRrule;

/**
 * The alternative PR rule selection class.
 * 
 * @author Marc van Zee (marcvanzee@gmail.com) - Utrecht University
 *
 */
public class PRruleSelector {
	
	/**
	 * Replaces the original tryPlayRevision method with one that uses belief inertia
	 * 
	 * @param plan the plan to repair
	 * @param beliefs the belief base
	 * @param rule the rule to try
	 * @param ignoreChunks if true, chunks are ignored
	 * @param theta the term substitutions (call by reference)
	 * @param thetaP the plan substitutions (call by reference)
	 * @param r the result storing information about the attempt
	 */
	public static boolean tryPlanRevision( PlanSeq plan, Beliefbase beliefs, PRrule rule, 
	                              boolean ignoreChunks, SubstList<Term> theta,
								  SubstList<PlanSeq> thetaP, ProcessIEventsResult r, APLModule m
								)
	{
		PlanSeq rest = new PlanSeq();
		
		PRrule variant = ((PRrule)rule).clone();
		PlanSeq plancopy = plan.clone();
		
		if (ignoreChunks) plancopy.unChunk();
		
		// Make rule fresh
		ArrayList<String> unfresh = plan.getVariables();
		ArrayList<String> own = ((PRrule)rule).getBody().getVariables();
		ArrayList<ArrayList<String>> changes = new ArrayList<ArrayList<String>>();
		variant.freshVars(unfresh,own,changes);
		
		PlanUnifier pu = new PlanUnifier(variant.getHead(),plancopy);
		if (pu.unify(theta,thetaP,rest,ignoreChunks))
		{
			// we are going to evaluate the rule, so we can reset the inertia which means that we
			// possibly not have to evaluate it again if the belief base doesn't change
			
			// reset the inertia but store the current one in a temporal variable
			
			// excluded rules (rules that have a random element in the dependency set of
			// their guard) will never exert inertia.
			boolean inertia = (rule.exclude ? false : rule.inertia);
			rule.inertia = true;
			
			// we only have to consider the substitutions of terms, because we cannot
			// have substitutions of plan variables in the guard
			boolean sameSubHead = RuleOperations.equalSubs(theta,rule.theta1);				
			rule.theta1 = theta.clone();
			
			if (!inertia || (rule.connected && !sameSubHead)) 
			{
				Query q = variant.getGuard();
				q.applySubstitution(theta);
				
				APLBenchmarker.startTiming(m, APLBenchmarkParam.BEL_QUERY);
				boolean beliefQuery = beliefs.doQuery(q,theta);
				APLBenchmarker.stopTiming(m, APLBenchmarkParam.BEL_QUERY);
				
				if (beliefQuery)
				{
					rule.ruleApplied = true;
					rule.theta2 = theta.clone();
					
					PlanSeq p = buildPlan(rule, variant, theta, thetaP, changes, false);
					r.addApplied( rule, theta, thetaP );
					updatePlan(plan, p, rest);
	
					return true;
				} else 
				{
					rule.ruleApplied = false;
				}
			} else if (rule.ruleApplied) 
			{
				PlanSeq p = buildPlan(rule, variant, theta, thetaP, changes, true);
				r.addApplied( rule, theta, thetaP );
				updatePlan(plan, p, rest);
				return true;
			}
		}

		// rule has not been applied
		return false;
	}
	
	
	private static void updatePlan(PlanSeq plan, PlanSeq p, PlanSeq rest) {
		plan.removeAll();
		plan.addLast(p);
		plan.addLast(rest);
	}

	private static PlanSeq buildPlan(PRrule rule, PRrule variant, SubstList<Term> theta,
			SubstList<PlanSeq> thetaP, ArrayList<ArrayList<String>> changes,
			boolean b) {
		PlanSeq p = variant.getBody();
		p.applyPlanSubstitution(thetaP);
		theta.applyChanges(changes);
		
		if (b) theta.putAll(rule.theta2);
		
		p.applySubstitution(theta);

		return p;		
	}

}
