package apapl.plans;

import apapl.Environment;
import apapl.ExternalActionFailedException;
import apapl.IILConverter;
import apapl.UnboundedVarException;
import apapl.data.APLIdent;
import apapl.data.APLNum;
import apapl.data.Term;
import apapl.data.APLFunction;
import apapl.data.APLVar;
import apapl.data.APLList;
import apapl.APLModule;
import apapl.SubstList;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Vector;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

import eis.EnvironmentInterfaceStandard;
import eis.exceptions.ActException;
import eis.exceptions.NoEnvironmentException;
import eis.exceptions.PerceiveException;
import eis.exceptions.RelationException;
import eis.iilang.Action;
import eis.iilang.ActionResult;
import eis.iilang.Percept;

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
		
		EnvironmentInterfaceStandard e = module.getEnvironmentInterface(env);

		assert e!=null : env;
		
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
	
	private Term execute(APLFunction action, EnvironmentInterfaceStandard e,
			APLModule module) throws ExternalActionFailedException {
		
		System.out.println("Start execute");
		
		if( action.getName().equals("getFreeEntities") ) {
			
			// get free entities
			LinkedList<String> freeOnes = e.getFreeEntities();
			
			// convert to list
			LinkedList<Term> terms = new LinkedList<Term>();
			
			for( String freeOne : freeOnes )
				terms.add( new APLIdent(freeOne) );
			
			APLList ret = new APLList(terms);

			// return
			return ret;
			
		}
		else if( action.getName().equals("getAllEntities") ) {

			// get free entities
			LinkedList<String> allOnes = e.getEntities();
			
			// convert to list
			LinkedList<Term> terms = new LinkedList<Term>();
			
			for( String one : allOnes )
				terms.add( new APLIdent(one) );
			
			APLList ret = new APLList(terms);

			// return
			return ret;

		}
		else if( action.getName().equals("associateWith") ) {

			System.out.println("associateWith");

			Term term = action.getParams().get(0);
			
			//System.out.println("Term: " + term.toString());
			
			Vector<String> entities = new Vector<String>();
			
			// only one parameter -- and identifier
			if( term instanceof APLIdent ) {
			
				entities.add( ((APLIdent)term).getName() );
				
			}
			// only one parameter -- a number
			else if ( term instanceof APLNum ) {
				
				entities.add( term.toString() );
				
			} 
			else if( term instanceof APLList ) {
				
				LinkedList<Term> list = ((APLList) term).toLinkedList();
				
				for( int a = 0; a < list.size() ; a++ ) {
					
					entities.add( ((APLIdent)list.get(a)).getName() );
					
				}
				
			} 
			else {
				
				System.out.println("ALarm");
				
				assert false : "Unexpected type " + term.getClass();
				
			}
			
			// System.out.println("Entities: " + entities);
			
			for( String entity : entities ) {
	
				try {
				
					e.associateEntity(module.getLocalName(), entity);
				
				} catch (RelationException e1) {
					
					throw new ExternalActionFailedException("Agent could not be associated"  + "\n" + e1.getMessage() );
				
				}
			
			}
			
		}
		else if( action.getName().equals("disassociateFrom") ) {

			String entity = ((APLIdent)action.getParams().get(0)).getName();
			
			//try {

				assert false : "Implement for version 0.2!";
				//e.freePair(module.getLocalName(), entity);
			
			//} catch (RelationException e1) {

				//throw new ExternalActionFailedException("Agent could not be dis associated" + "\n" + e1.getMessage() );

//			}
			
		}
		else if( action.getName().equals("getAllPercepts") ) {

			ArrayList<Term> params = action.getParams();

			String[] entities = null;
			
			if ( params.size() == 0 ) {
			
				// System.out.println(e);
				
			}
			else if ( params.size() == 1 ) { 
				
			}
			else {
			
				assert false : "Wrong number of arguments";
				
			}
			
			try {
				
				LinkedList<Percept> percepts = e.getAllPercepts(module.getLocalName());
				
				LinkedList<Term> terms = new LinkedList<Term>();
				for( Percept p : percepts ) {
					
					terms.add( IILConverter.convert(p) );
					
				}
				
				// System.out.println(percepts);
				
				return APLList.constructList(terms, null);
			
			} catch (PerceiveException e1) {

				throw new ExternalActionFailedException("Perceiving failed." + "\n" + e1.getMessage());
			
			} catch (NoEnvironmentException e1) {
				
				throw new ExternalActionFailedException("Perceiving failed." + "\n" + "No environment connected");
				
			}

		}
		else {

			Action iilaction = IILConverter.convertToAction(action);
			
			//System.out.println("Converted: " + iilaction.toProlog());

			// System.out.println(iilaction.toProlog());

			try {
				
				LinkedList<ActionResult> results = e.performAction(module.getLocalName(), iilaction);
			
				//System.out.println("Action performed");
				
				LinkedList<Term> terms = new LinkedList<Term>();
				
				for( ActionResult result : results ) {
					
					terms.add( IILConverter.convert(result) );
				}
				
				//System.out.println("Done");
				
				return APLList.constructList(terms, null);
				
			} catch (ActException e1) {

				//System.out.println("Fail 1");

				e1.printStackTrace();
				
				throw new ExternalActionFailedException("Acting failed." + "\n" + e1.getMessage());
			
			} catch (NoEnvironmentException e1) {
				
				//System.out.println("Fail 2");

				e1.printStackTrace();
				
				throw new ExternalActionFailedException("Acting failed." + "\n" + "No environment connected");
			
			}
			
			
		}
		
		return new APLIdent("success");
	
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
	
	/**
	 * 
	 * @param a
	 * @param e
	 * @param module
	 * @return
	 * @throws ExternalActionFailedException
	 * 
	 * @obsolete
	 */
	private Term execute (APLFunction a, Environment e, APLModule module) throws ExternalActionFailedException
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
		EnvironmentInterfaceStandard environment = module.getEnvironmentInterface(env.trim());
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