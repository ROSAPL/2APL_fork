package apapl.deliberation;

import java.util.ArrayList;
import java.util.Iterator;

import apapl.APLModule;
import apapl.PlanUnifier;
import apapl.SubstList;
import apapl.beliefinertia.BeliefInertiaModule;
import apapl.beliefinertia.BeliefInertiaParam;
import apapl.beliefinertia.RuleOperations;
import apapl.beliefinertia.ruleselectors.PRruleSelector;
import apapl.benchmarking.APLBenchmarkParam;
import apapl.benchmarking.APLBenchmarker;
import apapl.data.Query;
import apapl.data.Term;
import apapl.plans.PlanSeq;
import apapl.program.Beliefbase;
import apapl.program.PRrule;
import apapl.program.PRrulebase;
import apapl.program.Planbase;

/**
 * The deliberation step in which internal events are processed. For each received internal
 * event one applicable PR-rule (if any) is applied. This way the plans of the module change
 * as they are repaired by the PR-rules. Internal events themselve are stored in
 * the {@link apapl.APLModule}. Events for which no applicable rule is found are discarded.
 */
public class ProcessIEvents implements DeliberationStep
{
	/**
	 * Selects and applies PR rules.
	 * @return the result of executing this deliberation step.
	 **/
	public DeliberationResult execute(APLModule module)
	{
		APLBenchmarker.startTiming(module, APLBenchmarkParam.PRRULE);
		
	  ProcessIEventsResult r = new ProcessIEventsResult( );

		Beliefbase beliefs = module.getBeliefbase();
		PRrulebase prrules = module.getPRrulebase();
		Planbase plans = module.getPlanbase();
		ArrayList<Integer> iEvents = module.getIEvents();		
		
		for (Integer i : iEvents)
		{ // obtain plan that has failed
		  PlanSeq plan = plans.getPlan(i.intValue());

			// indicating whether a plan has been repaired
			boolean repaired = false;

		  // try to repair the plan with a PR-rule
			// try all PR-rules consecutively, but stop when a rule has been applied
			Iterator it = prrules.iterator();
			while ( it.hasNext() & !repaired )
		  { PRrule rule = (PRrule)(it.next());

			  // first try to fix the plan taking atomic blocks into account
				SubstList<PlanSeq> thetaP = new SubstList<PlanSeq>();
				SubstList<Term> theta = new SubstList<Term>();
		  	repaired = tryPlanRevision(plan,beliefs,rule, false, theta, thetaP, r, module);

        // if the plan cannot be fixed try again, now ignoring atomic blocks
				if( !repaired ) 
				{	thetaP = new SubstList<PlanSeq>();
					theta = new SubstList<Term>();
				  repaired = tryPlanRevision(plan,beliefs,rule, true, theta, thetaP, r, module);
				}
	  	}
		}
		module.clearIEvents();
		
		APLBenchmarker.stopTiming(module, APLBenchmarkParam.PRRULE);
		
		return( r );
	}

	/**
	 * Tries to repair a plan with a PR-rule. Tries to match the head of the rule with
	 * the plan and checks whether the guard can be satisfied given the set of beliefs.
	 * 
	 * @param plan the plan to repair
	 * @param beliefs the belief base
	 * @param rule the rule to try
	 * @param ignoreChunks if true, chunks are ignored
	 * @param theta the term substitutions (call by reference)
	 * @param thetaP the plan substitutions (call by reference)
	 * @param r the result storing information about the attempt
	 */
	boolean tryPlanRevision( PlanSeq plan, Beliefbase beliefs, PRrule rule, 
	                              boolean ignoreChunks, SubstList<Term> theta,
								  SubstList<PlanSeq> thetaP, ProcessIEventsResult r,
								  APLModule m
								)
	{
		if (BeliefInertiaParam.ENABLED) return PRruleSelector.tryPlanRevision(plan, beliefs, rule, ignoreChunks, theta, thetaP, r, m);
		
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
			Query q = variant.getGuard();
			q.applySubstitution(theta);
			
			APLBenchmarker.startTiming(m, APLBenchmarkParam.BEL_QUERY);
			boolean beliefQuery = beliefs.doQuery(q,theta);
			APLBenchmarker.stopTiming(m, APLBenchmarkParam.BEL_QUERY);
			
			if (beliefQuery)
			{ PlanSeq p = variant.getBody();
				p.applyPlanSubstitution(thetaP);
				theta.applyChanges(changes);
				p.applySubstitution(theta);

				r.addApplied( rule, theta, thetaP );

				plan.removeAll();
				plan.addLast(p);
				plan.addLast(rest);

				return true;
			}
		}

		// rule has not been applied
		return false;
	}
	public String toString()
	{
		return "Procces internal Events";
	}
}
