package apapl.deliberation;

import java.util.ArrayList;

import apapl.APLModule;
import apapl.NoRuleException;
import apapl.SubstList;
import apapl.data.APLFunction;
import apapl.messaging.APLMessage;
import apapl.messaging.Messenger;
import apapl.plans.PlanSeq;
import apapl.program.Beliefbase;
import apapl.program.PCrule;
import apapl.program.PCrulebase;
import apapl.program.Planbase;

/**
 * The deliberation step in which messages are processed. For each received message
 * one applicable PC-rule (if any) is applied. Messages themselve are stored in
 * the {@link apapl.APLModule}. Messages for which no applicable rule is found are discarded.
 */
public class ProcessMessages implements DeliberationStep
{
	/**
	 * Processes all received messages that are not already processed.
	 * 
	 * @return the result of this deliberation step
	 */
  public DeliberationResult execute( APLModule module )
	{
		ProcessMessagesResult result = new ProcessMessagesResult( );			

		APLMessage m;

		Beliefbase beliefs = module.getBeliefbase();
		Planbase plans = module.getPlanbase();
		PCrulebase pcrules = module.getPCrulebase();
		Messenger msgr = module.getMessenger();
		String name = module.getLocalName();

		while((m = msgr.receiveMessage(name)) != null)
		{ 
			SubstList theta = new SubstList();
	  		PCrule rule = null;
			APLFunction event = m.toAPLFunction();
	
	  		ArrayList<String> unfreshVars = new ArrayList<String>();
	  		try	
	  		{ 
	  			rule = pcrules.selectRule(beliefs,event,unfreshVars,theta, module);
			}
	  		catch (NoRuleException e) {}
				
	  		if (rule != null)
	  	 	{ 
	  			result.addProcessed( m, rule, theta );
				PlanSeq p = rule.getBody().clone();
	  			p.applySubstitution(theta);
	  			plans.addPlan(p);
	  		}
	  		else
			{ 
	  			result.addUnprocessed( m );
			}				
		}

		return( result );
	}

	public String toString()
	{
		return "Procces Messages";
	}
}
