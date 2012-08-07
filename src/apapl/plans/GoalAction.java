package apapl.plans;

import java.util.ArrayList;

import apapl.APLModule;
import apapl.ModuleAccessException;
import apapl.SubstList;
import apapl.UnboundedVarException;
import apapl.benchmarking.APLBenchmarkParam;
import apapl.benchmarking.APLBenchmarker;
import apapl.data.APLIdent;
import apapl.data.Goal;
import apapl.data.Term;
import apapl.program.Beliefbase;
import apapl.program.Goalbase;

/**
 * An action to update the goal base.
 */
public class GoalAction extends ModulePlan
{

	private Goal g;
	private String action;
	
	public GoalAction(APLIdent moduleId, String action, Goal g)
	{
		this.moduleId = moduleId;
		this.g = g;
		this.action = action.toLowerCase().trim();
	}
	
	private boolean testGoal(Beliefbase bb)
	{
		SubstList<Term> theta = new SubstList<Term>();
		return bb.doGoalQuery(g,theta);
	}
	
	public PlanResult execute(APLModule module)
	{
		APLModule updatedModule;
		
		if (moduleId != null) {
			try {
				if (moduleId == null)
					updatedModule = module;
				else
					updatedModule = module.getMas().getModule(module, moduleId.getName());
			} catch (ModuleAccessException e) {
				return new PlanResult(this, PlanResult.FAILED, "Module is not accessible: " + e.getMessage());
			}
		}
		else {
			updatedModule = module;
		}		
		
		Goalbase goals = updatedModule.getGoalbase();
		int r = PlanResult.FAILED;
		
		APLBenchmarker.startTiming(module, APLBenchmarkParam.GOAL_QUERY);
		
		if (action.startsWith("adopt")) {
			try {
				g.unvar();
				if(testGoal(module.getBeliefbase())) r = PlanResult.FAILED;
				else {
					if (action.equals("adopta")) goals.assertGoalHead(g);
					else if (action.equals("adoptz")) goals.assertGoal(g);
					parent.removeFirst();
					r = PlanResult.SUCCEEDED;
				}
			}
			catch (UnboundedVarException e) {
				r = PlanResult.FAILED;
			}
		}
		else if (action.equals("dropgoal")) {
			goals.dropGoal(g);
			parent.removeFirst();
			r = PlanResult.SUCCEEDED;
		}
		else if (action.equals("dropsubgoals")) {
			goals.dropSubGoals(g);
			parent.removeFirst();
			r = PlanResult.SUCCEEDED;
		}
		else if (action.equals("dropsupergoals")) {
			goals.dropSuperGoals(g);
			parent.removeFirst();
			r = PlanResult.SUCCEEDED;
		}
		
		APLBenchmarker.stopTiming(module, APLBenchmarkParam.GOAL_QUERY);
		
		return new PlanResult(this, r) ;
	}
	
	public String toString()
	{
		return ( moduleId == null ? "" : (moduleId + "." ) ) + action + "(" + g.toString(false) + ")";
	}
	
	public GoalAction clone()
	{
		return new GoalAction(moduleId, new String(action), g.clone());
	}
	
	public void applySubstitution(SubstList<Term> theta)
	{
		g.applySubstitution(theta);
	}
	
	public String toRTF(int t)
	{		
		return ( moduleId == null ? "" : (moduleId + "." ) ) + "\\cf4 " + action + "\\cf0 (" + g.toRTF(true)+ ")";
	}
	
	public void freshVars(ArrayList<String> unfresh, ArrayList<String> own, ArrayList<ArrayList<String>> changes)
	{
		g.freshVars(unfresh,own,changes);
	}
	
	public ArrayList<String> getVariables()
	{
		return g.getVariables();
	}
	
	public String getAction()
	{
		return action;
	}
	
	public Goal getGoal()
	{
		return g;
	}

	public APLIdent getPlanDescriptor() {
		return new APLIdent("goalaction");
	}
	
	
	
}
