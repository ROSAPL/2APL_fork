package apapl.beliefinertia;

/**
 * Here belief inertia can be enabled/disabled. It is on by default.
 * 
 * @author Marc van Zee (marcvanzee@gmail.com) - Utrecht University
 *
 */
public class BeliefInertiaParam {
	public static boolean ENABLED = false;
	
	/**
	 * Impure functions are functions that are excluded from inertia, so if
	 * a query has an impure function in its depedency set it will never
	 * exert inertia, because the outcomes of these predicates is non-deterministic
	 */
	public static String[] IMPURE_FUNCTIONS = { "rand", "random" };
}
