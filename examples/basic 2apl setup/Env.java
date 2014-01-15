import apapl.Environment;
import apapl.ExternalActionFailedException;
import apapl.data.APLFunction;
import apapl.data.APLIdent;
import apapl.data.APLNum;
import apapl.data.Term;

/**
 * === About this file
 * This is an example of a very simple environment that communicates with a single 2APl agent.
 * As you can see, this class extends the Environment class. This will give us several
 * methods that we can use to communicate with the agents, and we can create methods that the
 * agents can use to perform external actions.
 * 
 * Below you will find all the basic functionality that an environment offers explained.
 * 
 * === How to get this example running
 * First of all, I strongly recommend to use Eclipse as your editor since it has been used to
 * develop 2APL as well. The use of the Eclipse plugin that comes with the full package of 2APL
 * is not recommended, since it is not completely bug-free. In what follows I will assume you are
 * wise and have chosen for Eclipse.
 * 
 * 1. Create a new java project in Eclipse and add this file.
 * 2. Add 2apl.jar to the build path by adding it as an external jar.
 * 3. Before this environment can be used, it needs to be compiled into a JAR-file first.
 *    In the folder of this example you will also find a pre-compiled version of this JAR 
 *    already (env.jar), but it is fairly easy to do it yourself. You need to create a runnable jar 
 *    from this project, with this class (Env.java) as its main class. Therefore this class is REQUIRED 
 *    to have a main method. 
 * 4. Once you have created this JAR, you can refer to it in the .mas file that specifies the
 *    components of the multiagent system. The mas file of this example is called config.mas. This file 
 *    defines what environment to use, and what agents will exist. Agents are specified as .2apl files. 
 *    In our example we have one agent that is built from agent.2apl.
 * 5. Put all the files (the JAR, config.mas and agent.2apl) in one directory and run 2APL. You can
 *    run 2APL simply by running 2apl.jar. If you want debug information as well, you need to run
 *    this jar from Eclipse using APAPL as its main class.
 * 6. Open the .mas file, press 'play' and the example should be working.
 * 
 * @author Marc van Zee (marcvanzee@gmail.com), Utrecht University
 *
 */
public class Env extends Environment {
	private final boolean log = true;
	
    /**
     * We do not use this method, but we need it so that the JAR file that we will create can point
     * to this class as the main class. This is only possible if the class contains  main method.
     * @param args arguments
     */
	public static void main(String [] args) {
	}
	
	/**
	 * This method is automatically called whenever an agent enters the MAS.
	 * @param agName the name of the agent that just registered
	 */
	protected void addAgent(String agName) {
		log("env> agent " + agName + " has registered to the environment.");
		
		/* If we want to send information to a 2APL agent, we need to code this into special
		 * objects. We can then send these objects to the agent so that he can parse them correctly.
		 * All the objects extend the basic class "Term".
		 *
		 * We distinguish between the following objects:
		
		 * APLNum			This is equal to int and is for example instantiated by: new APLNum(0)
		 * APLIdent			Equal to String, instantiated by: new APLIdent("string")
		 * APLList			Can be seen as a LinkedList and will be parsed as a Prolog list in 2APL
		 *					See the constructor comments of this class for information on how to use it
		 * APLFunction		Represents a function, where the arguments of the function again need to be
		 *					Term objects. For example, the function: func(0) should be instantiated as
		 *					new APLFunction("func", new APLNum(0))
		 */
		APLIdent aplagName = new APLIdent(agName);
		APLFunction event = new APLFunction("name", aplagName);
		
		// If we throw an event, we always need to throw an APLFunction.
		throwEvent(event, agName);
		
		// note: we can also throw an event to all agents by letting out the last parameter: 
		// throwEvent(event);
	}
	
	/**
	 * External actions of agents can be caught by defining methods that have a Term as return value.
	 * This method can be called by a 2APL agents as follows: \@env(square(5), X).
	 * X will now contain the return value, in this case 25.
	 * @param agName The name of the agent that does the external action
	 * @param num The num to calculate the square of, coded in an APLNum
	 * @return The square of the input, coded in an APLNum
	 */
	public Term square(String agName, APLNum aplNum) throws ExternalActionFailedException {
		int num = aplNum.toInt();
		
		log("env> agent " + agName + " requests the square of " + num + ".");
				
		try {
			return new APLNum(num*num);
			
		} catch (Exception e) {
			//exception handling
			System.err.println("env> external action square() of " + agName + " failed: " +e.getMessage());
			return null;
		}
	}
	
	private void log(String str) {
		if (log) System.out.println(str);
	}
}