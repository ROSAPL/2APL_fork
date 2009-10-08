package apapl.plans;

import apapl.CartagoWrapper;
// import apapl.APLMAS;
import apapl.data.APLFunction;
import apapl.data.APLIdent;
import apapl.APLModule;
import apapl.ActionFailedException;
import apapl.data.*;
import apapl.SubstList;

import java.util.ArrayList;

/**
 * A Cartago action.
 */
public class CartagoAction extends Plan
{

	private APLFunction act;
	
	public CartagoAction(APLFunction act)
	{
		this.act = act;
	}
	
	public PlanResult execute(APLModule module)
	{
		CartagoWrapper wrapper = null; // APLMAS.getCartagoWrapper();
		try {
			if (act.getName().equals("use")) {
				String artifact = act.getParams().get(0).toString();
				APLFunction op = (APLFunction)(act.getParams().get(1));
				String sensor = act.getParams().get(2).toString();
				if (sensor.equals("")) sensor = null;
				int timeout = ((APLNum)(act.getParams().get(3))).toInt();
				wrapper.use(module.getLocalName(),artifact,op,sensor,timeout);
				parent.removeFirst();
				return new PlanResult(this, PlanResult.SUCCEEDED);
			}
			else if (act.getName().equals("sense")) {
				String sensor = act.getParams().get(0).toString();
				String filter = act.getParams().get(1).toString();
				APLVar result = (APLVar)(act.getParams().get(3));
				if (filter.equals("")) filter = null;
				int dt = ((APLNum)(act.getParams().get(2))).toInt();
				Term t = wrapper.sense(module.getLocalName(),sensor,filter,dt);
				if (t!=null) {
					SubstList<Term> theta = new SubstList<Term>();
					theta.put(result.getName(),t);
					parent.removeFirst();
					parent.applySubstitution(theta);
				}
			else parent.removeFirst();
			return new PlanResult(this, PlanResult.SUCCEEDED);
			}
			else if (act.getName().equals("createArtifact")) {
				String name = act.getParams().get(0).toString();
				String def = act.getParams().get(1).toString();
				wrapper.createArtifact(name,def);
				parent.removeFirst();
				return new PlanResult(this, PlanResult.SUCCEEDED);
			}
			else if (act.getName().equals("disposeArtifact")) {
				String name = act.getParams().get(0).toString();
				wrapper.disposeArtifact(name);
				parent.removeFirst();
				return new PlanResult(this, PlanResult.SUCCEEDED);
			}
			else if (act.getName().equals("linkSensor")) {
				String sensor = act.getParams().get(0).toString();
				wrapper.linkSensor(module.getLocalName(),sensor);
				parent.removeFirst();
				return new PlanResult(this, PlanResult.SUCCEEDED);
			}
			else if (act.getName().equals("unlinkSensor")) {
				String sensor = act.getParams().get(0).toString();
				wrapper.unlinkSensor(module.getLocalName(),sensor);
				parent.removeFirst();
				return new PlanResult(this, PlanResult.SUCCEEDED);
			}
			else if (act.getName().equals("focus")) {
				String artifact = act.getParams().get(0).toString();
				String sensor = act.getParams().get(1).toString();
				wrapper.focus(module.getLocalName(),artifact,sensor);
				parent.removeFirst();
				return new PlanResult(this, PlanResult.SUCCEEDED);
			}
			else if (act.getName().equals("stopFocus")) {
				String artifact = act.getParams().get(0).toString();
				wrapper.stopFocus(module.getLocalName(),artifact);
				parent.removeFirst();
				return new PlanResult(this, PlanResult.SUCCEEDED);
			}
			else return new PlanResult(this, PlanResult.FAILED);
		}
		catch (ActionFailedException e) {
			return new PlanResult(this, PlanResult.FAILED);
		}
		catch (ClassCastException e) {
			return new PlanResult(this, PlanResult.FAILED);
		}
	}
	
	public APLFunction getAct()
	{
		return act;
	}
	
	public ArrayList<String> getVariables()
	{
		return act.getVariables();
	}
	
	public String toRTF(int t)
	{
		return act.toRTF(true);
	}
	
	public CartagoAction clone()
	{
		return new CartagoAction(act.clone());
	}
	
	public String toString()
	{
		return act.toString(true);
	}
	
	public void freshVars(ArrayList<String> unfresh, ArrayList<String> own, ArrayList<ArrayList<String>> changes)
	{
		act.freshVars(unfresh,own,changes);
	}
	
	public ArrayList<String> canBeBounded()
	{
		ArrayList<String> a = new ArrayList<String>();
		if (act.getName().equals("sense")) {
			Term t = act.getParams().get(3);
			a.add(((APLVar)t).getName());
		}
		return a;
	}
	
	
	public ArrayList<String> mustBeBounded()
	{
		if (act.getName().equals("sense")) {
			ArrayList<String> a = new ArrayList<String>();
			a.addAll(act.getParams().get(0).getVariables());
			a.addAll(act.getParams().get(1).getVariables());
			return a;
		}
		else return act.getVariables();
	}

	public APLIdent getPlanQueryType() {
		return new APLIdent("cartago");
	}
}
