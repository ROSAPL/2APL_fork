package apapl;

import apapl.data.*;
import java.util.*;

/**
 * This class is the superclass for all 2APL environments. It implements the
 * basic functionality to define custom made environments in which the modules
 * can perform external actions.
 */
public abstract class Environment
{
	private HashMap<String, APLModule> modules = new HashMap<String, APLModule>();

	/**
	 * This method is invoked when a module enters the environment.
	 * 
	 * @param module The module to be added.
	 */
	public final void addModule(APLModule module)
	{
		modules.put(module.getLocalName(), module);
		// Only one of these methods should be overridden
		// and implemented in subclasses.
		addAgent(module.getLocalName());
		addModule(module.getLocalName());
	}

	/**
	 * This method is invoked when a module leaves the environment.
	 * 
	 * @param module The module to be removed.
	 */
	public final void removeModule(APLModule module)
	{
		modules.remove(module.getLocalName());
		// Only one of these methods should be overridden
		// and implemented in subclasses.
		removeAgent(module.getLocalName());
		removeModule(module.getLocalName());
	}

	/**
	 * This method is invoked when a module enters the environment. Note that
	 * this method is invoked each time the module is compiled.
	 * 
	 * @param name The local name of the specific module.
	 */
	protected void addModule(String name)
	{
	};

	/**
	 * This method is invoked when a module operating in this environment is
	 * removed. Note that this method is also invoked when the module is
	 * recompiled.
	 * 
	 * @param name The local name of the specific module.
	 */
	protected void removeModule(String name)
	{
	};

	/**
	 * This method is invoked when a module enters the environment. Note that
	 * this method is invoked each time the module is compiled.
	 * 
	 * @param name The local name of the specific agent.
	 * @deprecated This method stays only to keep backward-compatibility.
	 *             Use {@link #addModule(String)} instead.
	 */
	protected void addAgent(String name)
	{
	};

	/**
	 * This method is invoked when a module operating in this environment is
	 * removed. Note that this method is also invoked when the module is
	 * recompiled.
	 * 
	 * @param name The local name of the specific agent.
	 * @deprecated This method stays only to keep backward-compatibility. Use
	 *             {@link #removeModule(String)} instead.
	 */
	protected void removeAgent(String name)
	{
	};

	/**
	 * Throws an event to one or more modules.
	 * 
	 * @param event the event to be thrown
	 * @param receivers The modules listed here will receive the event. If no modules
	 *        are listed, the event will be thrown to all modules.
	 */
	protected final void throwEvent(APLFunction e, String... receivers)
	{
		// System.out.println("Sent event to " + receivers.length);

		// Send event to all receivers, if no receivers specified,
		// send to all modules in environment
		if (receivers.length > 0)
		{
			for (String receiver : receivers)
			{
				APLModule module = modules.get(receiver);
				if (module != null)
					module.notifyEEevent(e, getName());
				// System.out.println("Sent event to " + agent.getLocalName() );
			}
		} else
		{
			for (APLModule module : modules.values())
			{
				module.notifyEEevent(e, getName());
				// System.out.println("Sent event to " + agent.getLocalName() );
			}
		}
	}

	/**
	 * Returns the name of this environment. This is equal to the package name.
	 */
	public final String getName()
	{
		String sourceEnv = getClass().getName();
		return sourceEnv.substring(0, sourceEnv.lastIndexOf("."));
	}

	/**
	 * Invoked whenever the MAS is closed. This method can be used to clean up
	 * resources when the environment is about to be deinstantiated.
	 */
	public void takeDown()
	{
	}
}
