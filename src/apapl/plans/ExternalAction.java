package apapl.plans;

import apapl.Environment;
import apapl.ExternalActionFailedException;
import apapl.UnboundedVarException;
import apapl.data.APLIdent;
import apapl.data.APLNum;
import apapl.data.Term;
import apapl.data.APLFunction;
import apapl.data.APLVar;
import apapl.data.APLList;
import apapl.data.APLListVar;
import apapl.data.Literal;
import apapl.APLModule;
import apapl.program.Base;
import jade.lang.acl.ACLMessage;
import jade.core.AID;
import apapl.SubstList;
import java.util.ArrayList;
import java.util.LinkedList;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

/**
 * An external action.
 */
public class ExternalAction extends Plan
{
	private String env;
	private APLFunction action;
	private APLVar result;
	private Term timeoutTerm = new APLNum(0);
	private int timeout = 0;
	private long firstExecuteTime = -1;
	
	public ExternalAction(String env, APLFunction action, APLVar result)
	{
		this(env,action,result,new APLNum(0));
	}
	
	public ExternalAction(String env, APLFunction action, APLVar result, Term timeoutTerm)
	{
		this.env = env;
		this.action = action;
		this.result = result;
		this.timeoutTerm = timeoutTerm;
	}
		
	public PlanResult execute(APLModule module)
	{
		try {
			timeoutTerm = Term.unvar(timeoutTerm);
			timeout = ((APLNum)timeoutTerm).toInt();
		}
		catch (UnboundedVarException e) {
			return new PlanResult(this, PlanResult.FAILED);
		}
		
		Environment e = module.getEnvironment(env);

		if (e==null) {
			return new PlanResult(this, PlanResult.FAILED);
		}
		try	{
			if (timeoutCheck()) {
				Term t = execute(action,e,module);
				if (t!=null) {
					SubstList<Term> theta = new SubstList<Term>();
					theta.put(result.getName(),t);
					parent.applySubstitution(theta);
				}
				else {
				}
				parent.removeFirst();
				return new PlanResult(this, PlanResult.SUCCEEDED);
			}
			else {
				firstExecuteTime = -1;
				return new PlanResult(this, PlanResult.FAILED);
			}
		}
		catch (ExternalActionFailedException ex)	{
			if (firstExecuteTime==-1) firstExecuteTime = System.currentTimeMillis();
			
			long timeleft = 1000*timeout-(System.currentTimeMillis()-firstExecuteTime);
			String message;
			if (timeout>0) message = "Time left for retrials: "+timeleft+"ms";
			else if (timeout<0) message = "Time left for retrails: infinit";
			else message = "";
			
			// Only if timeout is 0 the first trial may cause the action to fail. Note that this must me the first trial.
			if (timeout==0) {
				firstExecuteTime = -1;
				return new PlanResult(this, PlanResult.FAILED);
			}
			else return new PlanResult(this, PlanResult.SUCCEEDED);
		}

	}
	
	public Term getTimeout()
	{
		return timeoutTerm;
	}
	
	/**
	 * Perform a timeout check
	 * @return false if the last time this action was executed is more than x ms ago where x is the timeout.
	 * true otherwise, so if the action is not executed before at all or is within the timeout.
	 */
	private boolean timeoutCheck()
	{
		if (timeout<0) {
			// Timeout is infinit.
			return true;
		}
		else if (firstExecuteTime<0) {
			// This is the first time the action is executed.
			return true;
		}
		else if (System.currentTimeMillis()-firstExecuteTime>1000*timeout) {
			// Timeout expired.
			return false;
		}
		else {
			// Execution within timeout.
			return true;
		}

	}
	
	public String getEnv()
	{
		return env;
	}
	
	public APLFunction getAction()
	{
		return action;
	}
		
	public String toString()
	{
		return "@"+env+"("+action.toString(5==9)+","+result.toString(5==9)+","+timeoutTerm.toString()+")";
	}
	
	public String toRTF(int t)
	{
		return "\\cf4 @"+env+"\\cf0 ("+action.toRTF(true)+","+result.toRTF(true)+","+timeoutTerm.toRTF()+")";
	}
	
	public APLVar getResultVar()
	{
		return result;
	}
	
	public ExternalAction clone()
	{
		return new ExternalAction(new String(env),action.clone(),result.clone(),timeoutTerm);
	}
	
	public void applySubstitution(SubstList<Term> theta)
	{
		action.applySubstitution(theta);
		result.applySubstitution(theta);
	}
	
	public Term execute (APLFunction a, Environment e, APLModule module) throws ExternalActionFailedException
	{
		ArrayList<Object> params = new ArrayList<Object>();
		ArrayList<Class> paramTypes = new ArrayList<Class>();
		
		try {
			a = (APLFunction)(Term.unvar(a));
		}
		catch (UnboundedVarException ex) {
			ex.printStackTrace();
			throw new ExternalActionFailedException(ex.toString());
		}

		try {
			params.add(module.getLocalName());
			
			for (Object x : a.getParams()) {
				if (x instanceof APLVar) {
					APLVar v = (APLVar)x;
					if (v.isBounded()) params.add(v.getSubst());
					else throw new ExternalActionFailedException("Unbounded var: "+ v);
				}
				else params.add(x);
			}
			
			paramTypes.add(Class.forName("java.lang.String"));
			for (Object t : a.getParams()) paramTypes.add(t.getClass());
			
			Method m = e.getClass().getMethod(a.getName(), paramTypes.toArray(new Class[0]));
			
			Class returnType = m.getReturnType();
			
			if (Class.forName("apapl.data.Term").isAssignableFrom(returnType)) {
				module.inEnvironment(true);
				Object result = m.invoke(e,params.toArray());
				module.inEnvironment(false);
				return (Term)result;
			}
			else  {
				module.inEnvironment(true);
				m.invoke(e,params.toArray());
				module.inEnvironment(false);
				return null;
			}
		}
		catch (NoSuchMethodException ex) {
			String w = a.getName()+"(";
			for (Class c : paramTypes) w = w + c.getSimpleName()+",";
			w = w.substring(0,w.length()-1)+")";
			
			throw new ExternalActionFailedException("No such method");
		}
		catch (InvocationTargetException ex) {
			module.inEnvironment(false);
			Throwable t = ex.getTargetException();
			if (t instanceof ExternalActionFailedException)
				throw (ExternalActionFailedException)t;
				else ex.printStackTrace();
		}
		catch (Exception ex) {ex.printStackTrace();}
		
		return null;
	}
	
	public ArrayList<String> getVariables()
	{
		ArrayList<String> vars =  action.getVariables();
		vars.addAll(result.getVariables());
		return vars;
	}
	
	public void freshVars(ArrayList<String> unfresh, ArrayList<String> own, ArrayList<ArrayList<String>> changes)
	{
		action.freshVars(unfresh,own,changes);
		result.freshVars(unfresh,own,changes);
	}
	
	public ArrayList<String> canBeBounded()
	{
		ArrayList<String> canBeBounded = new ArrayList<String>();
		canBeBounded.add(result.getName());
		return canBeBounded;
	}
	
	public ArrayList<String> mustBeBounded()
	{
		return action.getVariables();
	}
	
	private boolean checkMethod(Method m)
	{
		if(!m.getName().equals(action.getName())) return false;
		
		Class[] paramTypes = m.getParameterTypes();
		if (paramTypes.length!=action.getParams().size()+1) return false;
	
		try{
			for (int i = 1; i<paramTypes.length; i++)
			if (!Class.forName("apapl.data.Term").isAssignableFrom(paramTypes[i]))
				return false;
		}
		catch (ClassNotFoundException ex) {
		}

			
		return true;
	}	
	
	public void checkPlan(LinkedList<String> warnings, APLModule module)
	{
		Environment environment = module.getEnvironment(env.trim());
		if (environment==null) {
			//warnings.add("Action \""+this+"\" refers to an environment that is not available.");
			warnings.add("Action \""+toRTF(0)+"\" refers to an environment that is not available.");
			return;
		}

		boolean methodExists = false;
		for (Method m : environment.getClass().getMethods())
			methodExists = methodExists||checkMethod(m);
			
		//if (!methodExists)	warnings.add("Action \""+this+"\" refers to a method that is not available.");
		if (!methodExists)	warnings.add("Action \""+toRTF(0)+"\" refers to a method that is not available.");
	}

	public APLIdent getPlanQueryType() {
		return new APLIdent("externalaction");
	}
	
}
