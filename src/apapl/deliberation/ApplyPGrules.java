package apapl.deliberation;

import java.util.ArrayList;

import apapl.APLModule;
import apapl.benchmarking.APLBenchmarkParam;
import apapl.benchmarking.APLBenchmarker;
import apapl.plans.PlanSeq;
import apapl.program.Beliefbase;
import apapl.program.Goalbase;
import apapl.program.PGrulebase;
import apapl.program.Planbase;

/**
 * The deliberation step in which PG-rules are selected and applied. The application of 
 * a PG-rule results in the addition of that plan to the plan base of the module.
 */
public class ApplyPGrules implements DeliberationStep
{
	/**
	 * Selects and applies PG-rules.
	 * 
	 * @return the result of this deliberation step.
	 **/
	public DeliberationResult execute( APLModule module )
	{
		Goalbase goalbase = module.getGoalbase();
		Beliefbase beliefbase = module.getBeliefbase();
		PGrulebase pgrules = module.getPGrulebase();
		Planbase planbase = module.getPlanbase();
		
		APLBenchmarker.startTiming(module, APLBenchmarkParam.PGRULE);
		ArrayList<PlanSeq> ps = pgrules.generatePlans(goalbase,beliefbase,planbase,module);
		APLBenchmarker.stopTiming(module, APLBenchmarkParam.PGRULE);
		
	    return( new ApplyPGrulesResult( ps ) );
	}
	
	public String toString()
	{
		return "Apply PG rules";
	}
}
