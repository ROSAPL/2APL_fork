package apapl.program;

import java.util.ArrayList;

import apapl.APLModule;
import apapl.NoRuleException;
import apapl.SubstList;
import apapl.Unifier;
import apapl.beliefinertia.BeliefInertiaParam;
import apapl.beliefinertia.RuleOperations;
import apapl.beliefinertia.ruleselectors.PCruleSelector;
import apapl.benchmarking.APLBenchmarkParam;
import apapl.benchmarking.APLBenchmarker;
import apapl.data.APLFunction;
import apapl.data.Query;
import apapl.data.Term;
import apapl.plans.AbstractAction;

/**
 * The base in which {@link apapl.program.PCrule}s are stored.
 */
public class PCrulebase extends Rulebase<PCrule>
{
	/**
	 * Constructs a new PC-rule base.
	 */
	public PCrulebase( )
	{
	}
	
	/**
	 * Selects an applicable PC-rule. A PC-rule is applicable if the head matches the
	 * event/message/abstract action <code>a</code> and the guard is satisfied by
	 * the beliefs of the module. Returns a copy of the rule with fresh (unique) variables
	 * such that is does not interfere with the variables that already occur in the plan 
	 * from which the abstract action is called.
	 * 
	 * @param beliefbase the beliefs
	 * @param a the event/message/abstract action
	 * @param unfreshVars the list variables that cannot be used anymore
	 * @param theta the substitution for applying the rule
	 * @return the selected PC-rule
	 * @throws NoRuleException thrown if no rule is specified for <code>a</code>
	 */
	public PCrule selectRule(Beliefbase beliefbase, APLFunction a, ArrayList<String> unfreshVars, SubstList<Term> theta, APLModule m )
	throws NoRuleException
	{
		APLBenchmarker.startTiming(m, APLBenchmarkParam.PCRULE);
		
		if (BeliefInertiaParam.ENABLED) return PCruleSelector.selectPCrule(beliefbase, a, unfreshVars, theta, rules, m);
		
		boolean norulefound = true;
		for (PCrule pcrule : rules) {
			SubstList<Term> theta2 = new SubstList<Term>();
			PCrule variant = pcrule.getVariant(unfreshVars);
			Query guard = variant.getGuard().clone();
			APLFunction head = variant.getHead();
			
			if (Unifier.unify(head,a.clone(),theta2)) {
				norulefound = false;
				guard.applySubstitution(theta2);
				
				APLBenchmarker.startTiming(m, APLBenchmarkParam.BEL_QUERY);
				boolean beliefQuery = beliefbase.doQuery(guard,theta2);
				APLBenchmarker.stopTiming(m, APLBenchmarkParam.BEL_QUERY);
				
				if (beliefQuery) {
					theta.putAll(theta2);
					APLBenchmarker.stopTiming(m, APLBenchmarkParam.PCRULE);
					return variant;
				}
			}
		}
		if (norulefound) throw new NoRuleException();
		
		APLBenchmarker.stopTiming(m, APLBenchmarkParam.PCRULE);
		return null;
	}
	
	/**
	 * Returns whether an abstract action is defined by some PC-rule, i.e. whether the
	 * abstract action matches with the head of some PC-rule.
	 *  
	 * @param a the abstract action
	 * @return true if the action is defined by some rule, false otherwise
	 */
	public boolean defines(AbstractAction a)
	{
		APLFunction plan = a.getPlan();
		for (PCrule r : rules)  {
			APLFunction head = r.getHead();
			if (head.getName().equals(plan.getName())&&head.getParams().size()==plan.getParams().size()) return true;
		}
		return false;
	}
	
	/**
	 * @return  clone of the PC rulebase
	 */
	public PCrulebase clone()
	{
		PCrulebase b = new PCrulebase(); 
		b.setRules(getRules());
		return b;
	}
		
}
