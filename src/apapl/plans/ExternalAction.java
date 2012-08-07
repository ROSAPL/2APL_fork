package apapl.plans;

import java.util.ArrayList;
import java.util.LinkedList;

import apapl.APLModule;
import apapl.Environment;
import apapl.ExternalActionFailedException;
import apapl.SubstList;
import apapl.UnboundedVarException;
import apapl.data.APLFunction;
import apapl.data.APLIdent;
import apapl.data.APLNum;
import apapl.data.APLVar;
import apapl.data.Term;
import apapl.env.exceptions.ActException;
import apapl.env.exceptions.NoEnvironmentException;

/**
 * An external action.
 */
public class ExternalAction extends Plan {
    private String env;
    private APLFunction action;
    private APLVar result;
    private Term timeoutTerm = new APLNum(0);
    private int timeout = 0;
    private long firstExecuteTime = -1;

    private boolean eisDebug = false;
    
    public ExternalAction(String env, APLFunction action, APLVar result) {
        this(env, action, result, new APLNum(0));
    }

    public ExternalAction(String env, APLFunction action, APLVar result,
            Term timeoutTerm) {
        this.env = env;
        this.action = action;
        this.result = result;
        this.timeoutTerm = timeoutTerm;
    }

    public PlanResult execute(APLModule module) {

        try {
            timeoutTerm = Term.unvar(timeoutTerm);
            timeout = ((APLNum) timeoutTerm).toInt();
        } catch (UnboundedVarException e) {
            return new PlanResult(this, PlanResult.FAILED);
        }
        
        Environment e = module.getEnvironment(env);

        assert e != null : env;

        if (e == null) {
        	System.out.println("No environment found!");
            return new PlanResult(this, PlanResult.FAILED);
        }
        
        try {
            if (timeoutCheck()) {
                Term t = execute(action, e, module);
                if (t != null) {
                    SubstList<Term> theta = new SubstList<Term>();
                    theta.put(result.getName(), t);
                    parent.applySubstitution(theta);
                } else {
                }
                parent.removeFirst();
                return new PlanResult(this, PlanResult.SUCCEEDED);
            } else {
                firstExecuteTime = -1;
                return new PlanResult(this, PlanResult.FAILED);
            }
        } catch (ExternalActionFailedException ex) {
            if (firstExecuteTime == -1)
                firstExecuteTime = System.currentTimeMillis();

            // Only if timeout is 0 the first trial may cause the action to
            // fail. Note that this must me the first trial.
            if (timeout == 0) {
                firstExecuteTime = -1;
                return new PlanResult(this, PlanResult.FAILED);
            } else
                return new PlanResult(this, PlanResult.SUCCEEDED);
        }

    }

    private Term execute(APLFunction action, Environment e,
            APLModule module) throws ExternalActionFailedException 
    {
        try {

        	String agentName = module.getAgentName();
            return e.performAction(agentName, action);

        } catch (ActException e1) {
        	if(eisDebug) System.out.println("Fail 1");
        	if(eisDebug) e1.printStackTrace();
            throw new ExternalActionFailedException("Acting failed." + "\n"
                    + e1.getMessage());

        } catch (NoEnvironmentException e1) {
        	if(eisDebug) System.out.println("Fail 2");
        	if(eisDebug) e1.printStackTrace();
            throw new ExternalActionFailedException("Acting failed." + "\n"
                    + "No environment connected");

        }
    }

    public Term getTimeout() {
        return timeoutTerm;
    }

    /**
     * Perform a timeout check
     * 
     * @return false if the last time this action was executed is more than x ms
     *         ago where x is the timeout. true otherwise, so if the action is
     *         not executed before at all or is within the timeout.
     */
    private boolean timeoutCheck() {
        if (timeout < 0) {
            // Timeout is infinit.
            return true;
        } else if (firstExecuteTime < 0) {
            // This is the first time the action is executed.
            return true;
        } else if (System.currentTimeMillis() - firstExecuteTime > 1000 * timeout) {
            // Timeout expired.
            return false;
        } else {
            // Execution within timeout.
            return true;
        }

    }

    public String getEnv() {
        return env;
    }

    public APLFunction getAction() {
        return action;
    }

    public String toString() {
        return "@" + env + "(" + action.toString(false) + ","
                + result.toString(false) + "," + timeoutTerm.toString() + ")";
    }

    public String toRTF(int t) {
        return "\\cf4 @" + env + "\\cf0 (" + action.toRTF(true) + ","
                + result.toRTF(true) + "," + timeoutTerm.toRTF() + ")";
    }

    public APLVar getResultVar() {
        return result;
    }

    public ExternalAction clone() {
        return new ExternalAction(new String(env), action.clone(), result
                .clone(), timeoutTerm);
    }

    public void applySubstitution(SubstList<Term> theta) {
        action.applySubstitution(theta);
        result.applySubstitution(theta);
    }

    public ArrayList<String> getVariables() {
        ArrayList<String> vars = action.getVariables();
        vars.addAll(result.getVariables());
        return vars;
    }

    public void freshVars(ArrayList<String> unfresh, ArrayList<String> own,
            ArrayList<ArrayList<String>> changes) {
        action.freshVars(unfresh, own, changes);
        result.freshVars(unfresh, own, changes);
    }

    public ArrayList<String> canBeBounded() {
        ArrayList<String> canBeBounded = new ArrayList<String>();
        canBeBounded.add(result.getName());
        return canBeBounded;
    }

    public ArrayList<String> mustBeBounded() {
        return action.getVariables();
    }

    public void checkPlan(LinkedList<String> warnings, APLModule module) {
        Environment environment = module
                .getEnvironment(env.trim());
        if (environment == null) {
            // warnings.add("Action \""+this+"\" refers to an environment that is not available.");
            warnings.add("Action \"" + toRTF(0)
                    + "\" refers to an environment that is not available.");
            return;
        }

    }

    public Term getPlanDescriptor() {
        return new APLFunction("externalaction", new APLIdent(env), action, result, timeoutTerm);
    }


}
