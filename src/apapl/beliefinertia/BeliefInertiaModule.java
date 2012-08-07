package apapl.beliefinertia;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.LinkedList;

import apapl.APLModule;
import apapl.beliefinertia.dependencyset.DependencySetBuilder;
import apapl.beliefinertia.dependencyset.InferenceRules;
import apapl.data.Literal;
import apapl.parser.prolog.PrologParser;
import apapl.plans.AssertBelief;
import apapl.program.BeliefUpdate;
import apapl.program.Rule;

/**
 * The belief inertia module implements a theory to reduce the number of belief queries.
 * All belief update actions (both direct and indirect) are evaluated at compile-time,
 * and dependency sets are generated for them. Then the guards of all the practical
 * reasoning rules are connected to these belief updates if they occur in the dependency
 * set. When a belief update action is executed at run-time, all the rules that are in
 * the dependency set of this rule are "activated", meaning they are excluded from inertia.
 * When a rule is applied, only the guard which have no belief inertia will have to be
 * evaluated. For this, the selection mechanisms for all the rules had to be adjusted
 * accordingly. These updated rule selectors can be found in the package
 * apapl.beliefinertia.dependencyset.
 * 
 * Belief inertia can be enabled and disabled, because it is still in testing mode. This
 * can be set in the class apapl.beliefinertia.BeliefInertiaParam. When it has been
 * tested enough we will integrate it into 2APL completely.
 * 
 * @author Marc van Zee (marcvanzee@gmail.com) - Utrecht University
 *
 */
public class BeliefInertiaModule 
{
	APLModule a;
	InferenceRules inferenceRules;
		
	/**
	 * Initialize the direct and indirect belief updates and the rules of this APLModule for belief inertia.
	 * This will set the belief and goal update criteria for each rule.
	 *  
	 * @param a the APLModule that needs to be initialized
	 * @param beliefBase the belief base of this module as one string, this is required to generate the dependency sets
	 * @param directBUs a linked listed of direct belief updates (see apapl.plans.AssertBelief). they cannot be infered from
	 * the APLModule easily because they are hidden inside the plans.
	 */
	public void initalize(APLModule a) 
	{
		this.a = a;
		String bb = a.getBeliefbase().toString();
		
		inferenceRules = parseInferenceRules(bb);
		
		// TODO: these three methods are highly inefficient (we iterate over all the rules many times)
		// but if we do not parse too many 2APL files at runtime this should be no problem
		// still, we might consider optimizing it
		
		// generate the dependency sets for the rules: all the predicates in the belief base that each rule can affect
		(new DependencySetBuilder(inferenceRules)).build(a);
		
		// assign for all (direct/indirect) belief updates what rules they possibly can affect		
		assignBUrules();
	}
	
	/**
	 * Parse the inference rules of a belief base using a dedicated Prolog parser. 
	 * 
	 * @param bb The belief base of an APLModule in a String
	 * @return An InferenceRules object which contains two HashMaps of the predicates in the inference rules
	 */
	private InferenceRules parseInferenceRules(String bb) 
	{
		PrologParser parser = new PrologParser(new StringReader(""));
		
		parser.ReInit(new StringReader(bb));
		
		try {
			return parser.Prolog();
		} catch (apapl.parser.prolog.ParseException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	/**
	 * For each belief update action (direct and indirect) find the relevant practical reasoning
	 * rules and add them as "relevant rules" to the belief updates. These relevant rules will
	 * have their belief inertia flag disabled when the belief update action is executed. 
	 */
	private void assignBUrules()
	{
		// indirect belief updates are belief update specifications of the form { pre } Update(X1,...,Xn) { post }
		for (BeliefUpdate b : a.getBeliefUpdates())	 		
			b.addRelevantRules(findRelevantRules(b.getPost()));
		
		// direct belief updates are plans of the form +belief(X1,..,Xn) and -belief(X1,..,Xn)
		for (AssertBelief ab : a.getDirectBeliefUpdates()) 	
			ab.addRelevantRules(findRelevantRules(ab.getPost()));
	}
	
	/**
	 * Find the relevant rules of a belief query by iterating over all practical reasoning rules
	 * and see whether there is a predicate in the dependency set that occurs in the belief query
	 * as well. If so, at it as a relevant rule 
	 * 
	 * @param query The belief query represented as an arraylist of literals
	 * @return the relevant rules as a linkedlist of rules
	 */
	private LinkedList<Rule> findRelevantRules(ArrayList<Literal> query) {
		LinkedList<Rule> ret = new LinkedList<Rule>();
		
		for (Rule rule : a.getAllRules()) {
			
			// rules are excluded from inertia (meaning their guard should always be checked)
			// if a random number is generated in their dependency set
			if (RuleOperations.containsImpureSymbol(rule.dependencySet))
				continue;
			
			for (Literal l : query) {
				String p = RuleOperations.getPredicate(l);
				if (rule.dependencySet.contains(p)) {
					ret.add(rule);
					break;
				}
			}
		}
		
		return ret;
	}
}
