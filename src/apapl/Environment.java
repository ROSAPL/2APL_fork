package apapl;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import apapl.data.APLFunction;
import apapl.data.Term;
import apapl.env.AgentListener;
import apapl.env.exceptions.ActException;
import apapl.env.exceptions.AgentException;
import apapl.env.exceptions.EnvironmentInterfaceException;
import apapl.env.exceptions.NoEnvironmentException;

/**
 * This class is the superclass for 2APL environments. It implements
 * the basic functionality to define custom made environments in which the
 * agents can perform external actions.
 * <p />
 * As for April 2012, 2APL does no longer use EIS (Environment Interface Standard).
 * Therefore this class can be used to extend and build a custom environment.
 * The 
 */
public class Environment {
	private HashMap<String,String> envParams = new HashMap<String,String>();
	
	/**
     * This method is meant to be overridden by sub-classes. It is invoked when
     * an agent enters the environment. Note that this method is also invoked each
     * time an agent is re-compiled.
     * 
     * @param name the local name of the specific agent.
     */
    protected void addAgent(String name) {
    };

    /**
     * This method is invoked when an agent operating in this environment is
     * removed. Note that this method is also invoked when an agent is
     * recompiled.
     * 
     * @param name the local name of the specific agent.
     */
    protected void removeAgent(String name) {
    };
	
	
	 /**
     * Stores for each agent (represented by a string) a listener.
     */
    protected HashMap<String, AgentListener> agents = null;

    /**
     * Instantiates the class.
     */
    public Environment() {
        agents = new HashMap<String, AgentListener>();
    }
    
    /**
     * Throws an event to one or more agents.
     * 
     * @param event the event to be thrown
     * @param receivers the agent listed to receive the event. If no
     *        no agents are listed, the event will be thrown to all agents.
     * @throws EnvironmentInterfaceException 
     */
    public final void throwEvent(APLFunction e, String... pAgents) {
        notifyAgents(e, pAgents);
    }

    /**
     * Returns the name of this environment. This is equal to the package name.
     */
    public final String getName() {
        String sourceEnv = getClass().getName();
        return sourceEnv.substring(0, sourceEnv.lastIndexOf("."));
    }

    /*
     * 
     * TODO: change doc
     * 
     */
    public final void registerAgent(String agent, AgentListener listener) {
        agents.put(agent, listener);
        addAgent(agent); // call to method that can be overridden by superclass
    }

    /*
     * 
     * TODO: change doc
     * 
     */
    public void deregisterAgent(String agent) throws AgentException {
        if (agents.containsKey(agent) == false)
            return;

        agents.remove(agent);
        removeAgent(agent);
    }

    /**
     * Notifies agents about a percept.
     * 
     * @param percept is the percept
     * @param agents is the array of agents that are to be notified about the
     *        event. If the array is empty, all registered agents will be
     *        notified. The array has to contain only registered agents.
     * @throws AgentException is thrown if at least one of the agents in the
     *         array is not registered.
     */
    protected final void notifyAgents(APLFunction msg, String... pAgents)
    {
    	try 
    	{
	        // send to all registered agents
	        if (pAgents.length == 0) {
	            for (String agent : agents.keySet()) {
	            	agents.get(agent).handleMessage(agent, msg);
	            }
	            return;
	        }
	
	        // send to specified agents
	        for (String agent : pAgents) {
	
	            if (!agents.containsKey(agent))
						throw new EnvironmentInterfaceException("Agent " + agent
						        + " has not registered to the environment.");
	
	            agents.get(agent).handleMessage(agent, msg);
	        }
    	} catch (EnvironmentInterfaceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }


    /**
     * Performs action in the original 2APL environment.
     */ 
    public final Term performAction(String agent,
            APLFunction action) throws ActException,
            NoEnvironmentException {

        // unregistered agents cannot act
        if (!agents.containsKey(agent))
            throw new ActException("Agent \"" + agent + "\" is not registered.");

        // Parameters expressed using 2APL data types
        ArrayList<Term> params = action.getParams();
        
        // determine class parameters for finding the method
        // and store the parameters as objects

        Class<?>[] classParams = new Class[params.size() + 1];
        classParams[0] = String.class; // entity name
        
        for (int a = 0; a < params.size(); a++)
            classParams[a + 1] = params.get(a).getClass();

        try {
            // lookup the method
            Method m = this.getClass().getMethod(action.getName(), classParams);

            if (Class.forName("apapl.data.Term").isAssignableFrom(
                    m.getReturnType()) == false)
                throw new ActException("Wrong return-type");

            // invoke
            Object[] objParams = new Object[params.size() + 1];
            objParams[0] = agent; // agent name
            for (int a = 0; a < params.size(); a++)
            	objParams[a + 1] = params.get(a);

            return (Term) m.invoke(this, objParams);

        } catch (ClassNotFoundException e) {
            throw new ActException("Class not found", e);
        } catch (SecurityException e) {
            throw new ActException("Security exception", e);
        } catch (NoSuchMethodException e) {
            throw new ActException("No such method", e);
        } catch (IllegalArgumentException e) {
            throw new ActException("Illegal argument", e);
        } catch (IllegalAccessException e) {
            throw new ActException("Illegal access", e);
        } catch (InvocationTargetException e) {
            
            // action has failed -> let fail
            if (e.getCause() instanceof ExternalActionFailedException)
                throw new ActException("Execution failed.", (Exception) e
                        .getCause()); // rethrow
            
            else if (e.getCause() instanceof NoEnvironmentException)
                throw (NoEnvironmentException) e.getCause(); // rethrow

            throw new ActException("Invocation target exception", e);
        }
    }
    
    /**
     * This method is called when all the environment parameters have been added
     * to the environment and it has been added to the module as well. It can be
     * overridden by an Environment developer to determine the point when the
     * environment has received all necessary information for the APAPLBuilder.
     * 
     */
    public void initialized() {
    }
    
    
    /*
     * Environment parameters functionality
     * 
     */
    
	void addEnvParameter(String key, String value) {
		envParams.put(key, value);
	}
	
	public HashMap<String, String> getEnvParameters() {
		return envParams;
	}
	
}
