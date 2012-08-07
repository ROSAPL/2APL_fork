package apapl.beliefinertia.ruleselectors;

import java.util.ArrayList;

import apapl.APLModule;
import apapl.NoRuleException;
import apapl.SubstList;
import apapl.Unifier;
import apapl.beliefinertia.RuleOperations;
import apapl.benchmarking.APLBenchmarkParam;
import apapl.benchmarking.APLBenchmarker;
import apapl.data.APLFunction;
import apapl.data.Query;
import apapl.data.Term;
import apapl.program.Beliefbase;
import apapl.program.PCrule;

/**
 * The alternative PC rule selection class.
 * 
 * @author Marc van Zee (marcvanzee@gmail.com) - Utrecht University
 *
 */
public class PCruleSelector {

	/**
	 * Replaces the original rule selection method for PC rules in apapl.program.PCRulebase with
	 * one that uses belief inertia
	 * 
	 * @param beliefbase the beliefs
	 * @param a the event/message/abstract action
	 * @param unfreshVars the list variables that cannot be used anymore
	 * @param theta the substitution for applying the rule
	 * @param rules all the pc rules in this APLModule
	 * @return the selected PC-rule
	 * @throws NoRuleException thrown if no rule is specified for <code>a</code>
	 */
	public static PCrule selectPCrule(Beliefbase beliefbase, APLFunction a, 
			ArrayList<String> unfreshVars, SubstList<Term> theta, ArrayList<PCrule> rules, APLModule m )
	throws NoRuleException
	{
		boolean norulefound = true;
		boolean inertia;
		for (PCrule pcrule : rules) {
			SubstList<Term> theta2 = new SubstList<Term>();
			PCrule variant = pcrule.getVariant(unfreshVars);
			Query guard = variant.getGuard().clone();
			APLFunction head = variant.getHead();
			
			if (Unifier.unify(head,a.clone(),theta2)) {
				// we are going to evaluate the rule, so we can reset the inertia which means that we
				// possibly not have to evaluate it again if the belief base doesn't change
				
				// reset the inertia but store the current one in a temporal variable
				// excluded rules (rules that have a random element in the dependency set of
				// their guard) will never exert inertia.
				inertia = pcrule.exclude ? false : pcrule.inertia;
				pcrule.inertia = true;
				
				boolean sameSubHead = RuleOperations.equalSubs(theta2,pcrule.theta1);				
				pcrule.theta1 = theta2.clone();
				
				if (!inertia || (pcrule.connected && !sameSubHead)) {
					norulefound = false;
					
					guard.applySubstitution(theta2);
					
					APLBenchmarker.startTiming(m, APLBenchmarkParam.BEL_QUERY);
					boolean beliefQuery = beliefbase.doQuery(guard,theta2);
					APLBenchmarker.stopTiming(m, APLBenchmarkParam.BEL_QUERY);
					
					if (beliefQuery) 
					{
						pcrule.ruleApplied = true;
						theta.putAll(theta2);
						pcrule.theta2 = theta2.clone();
						
						APLBenchmarker.stopTiming(m, APLBenchmarkParam.PCRULE);
						
						return variant;
					} else {
						pcrule.ruleApplied = false;
					}
				} else if (pcrule.ruleApplied) {
					// if the head has changed we need to update the substitution
					theta.putAll(pcrule.theta1);
					
					APLBenchmarker.stopTiming(m, APLBenchmarkParam.PCRULE);
					
					return variant;
				}
			}
		}
		APLBenchmarker.stopTiming(m, APLBenchmarkParam.PCRULE);
		
		if (norulefound) throw new NoRuleException();	
		return null;
	}

}
