package apapl;

import apapl.messaging.Messenger;
import apapl.parser.*;
import apapl.data.*;
import java.util.*;
import java.io.*;
import java.lang.reflect.*;
import java.net.*;

/**
 * A builder used to construct a multi-agent system. The builder uses parser to
 * parse the mas file and the 2APL files that specify the modules.
 * 
 * @see apapl.Parser
 */
public class APAPLBuilder
{
	private Parser parser = new Parser();

	/**
	 * Builds a multi-agent system from a MAS specification file.
	 * 
	 * @param masfile the file that specifies the MAS
	 * @param msgr the messenger used by the modules for communication
	 * @param exec the executor implementing the strategy for executing the
	 *        modules
	 * @return the MAS constructed from the specification file
	 * @throws ParseMASException
	 * @throws ParseModuleException
	 * @throws ParsePrologException
	 * @throws LoadEnvironmentException
	 */
	public APLMAS buildMas(File masfile, Messenger msgr, Executor exec)
			throws ParseMASException, ParseModuleException,
			ParsePrologException, LoadEnvironmentException
	{
		// Build the MAS
		APLMAS mas = new APLMAS(masfile.getParentFile(), msgr, exec);
		// Keep track of the environments that are created
		HashMap<String, Environment> envs = new HashMap<String, Environment>();

		// parseMas returns a list of strings (as) per agent in the MAS
		// as:
		// agentname as[0], main module specification as[1], environments
		// as[2..n]
		ArrayList<ArrayList<String>> ass = parser.parseMas(masfile);

		// Build the main modules for each agent that resides in this MAS
		for (ArrayList<String> as : ass)
		{
			// Build the module
			Tuple<APLModule, LinkedList<File>> t = buildModule(as.get(1), as
					.get(0), mas);
			// Main module starts implicitly as active
			mas.addModule(t.l, t.r, true);

			// For all environments this module participates in
			for (int i = 2; i < as.size(); i++)
			{
				Environment env = envs.get(as.get(i));

				// Create environment if it does not already exist
				if (env == null)
				{
					env = buildEnvironment(as.get(i));
					envs.put(as.get(i), env);
					mas.addEnvironment(env);
				}

				// Link agent's main module to environment
				t.l.addEnvironment(as.get(i), env);
				env.addModule(t.l);
			}
		}

		return (mas);
	}

	/**
	 * Builds a module instance from the module specification.
	 * 
	 * @param moduleSpec the name of the module specification
	 * @param name the name of the module instance
	 * @param mas the multi-agent system in which will the module reside
	 * @return a tuple containing the module instance and the list of processed
	 *         specification files
	 * @throws ParseModuleException if the module cannot be instantiated, e.g.
	 *         it contains syntax errors
	 * @throws ParsePrologException if the module's belief base could not be
	 *         instantiated
	 */
	public Tuple<APLModule, LinkedList<File>> buildModule(String moduleSpec,
			String name, APLMAS mas) throws ParseModuleException,
			ParsePrologException
	{
		File file = getModuleSpecificationFile(mas, moduleSpec);

		// Parse the module file
		Tuple<APLModule, LinkedList<File>> t = parser.parseFile(file);

		// set name of the newly created module
		t.l.setName(name);

		// Assign mas to the module
		t.l.setMas(mas);

		return (t);
	}

	/**
	 * Builds an environment. The environment is specified by a class file
	 * Env.class that is expected to be in a jar file that is located in the
	 * directory 'environments' and that has the name of the environment. This
	 * method then loads and instantiates this class.
	 * 
	 * @param environment the name that identifies the environment
	 * @return the environment
	 * @throws LoadEnvironmentException if an error occurred during loading
	 */
	public Environment buildEnvironment(String environment)
			throws LoadEnvironmentException
	{
		Environment env = null;

		// Construct the location of the jar file the environment is in
		environment = environment.trim();
		String jarfile = System.getProperty("user.dir") + File.separator
				+ "environments" + File.separator + environment + ".jar";

		try
		{
			URL[] urls = new URL[1];
			urls[0] = new URL("file:" + File.separator + File.separator
					+ jarfile);

			// Add the jar file to the classpath
			ClassPathHacker.addFile(jarfile);

			// Obtain environment class
			URLClassLoader cloader = new URLClassLoader(urls);
			Class envClass = cloader.loadClass(environment + ".Env");

			// Instantiate the environment class
			Constructor co = envClass.getConstructor();
			env = (Environment) (co.newInstance());
		}

		// Lots of things might go wrong when loading an environment
		// just throw the exception to the calling method
		catch (Exception e)
		{
			throw (new LoadEnvironmentException(environment, e.getMessage()));
		}

		return (env);
	}

	/**
	 * Determines the file that contains given module specification. By
	 * convention, the module specification will be loaded from the file named
	 * identically as the module specification followed by <code>.2apl</code>
	 * suffix. If the expected file does not yet exist, an empty one will be
	 * created.
	 * 
	 * @param mas the multi-agent system this module belongs to
	 * @param moduleSpec the name of the module specification
	 * @return the file containing the module specification
	 */
	private File getModuleSpecificationFile(APLMAS mas, String moduleSpec)
	{
		// Support for old MAS file syntax
		if (!moduleSpec.toLowerCase().endsWith(".2apl"))
			moduleSpec = new String(moduleSpec + ".2apl");

		File file = new File(mas.getModuleSpecDir(), moduleSpec);
		try
		{
			file.createNewFile();
		} catch (IOException e)	{}

		return file;
	}

}
