package apapl.messaging;

import java.util.ArrayList;

import apapl.Parser;
import apapl.data.APLFunction;
import apapl.data.APLIdent;
import apapl.data.Term;

/**
 * A message that can be sent to 2APL modules.
 */
public class APLMessage
{
	private String sender;
	private String receiver;
	private String performative;
	private String language;
	private String ontology;
	private APLFunction content;
	
	public APLMessage()
	{
	}
	
	public void setSender(String sender)
	{
		this.sender = sender;
	}
	
	public String getSender()
	{
		return sender;
	}
	
	public void setReceiver(String receiver)
	{
		this.receiver = receiver;
	}
	
	public String getReceiver()
	{
		return receiver;
	}
	
	public void setPerformative(String performative)
	{
		this.performative = performative;
	}
	
	public String getPerformative()
	{
		return performative;
	}
	
	public void setLanguage(String language)
	{
		this.language = language;
	}
	
	public String getLanguage()
	{
		return language;
	}
	
	public void setOntology(String ontology)
	{
		this.ontology = ontology;
	}
	
	public String getOntology()
	{
		return ontology;
	}
	
	public void setContent(APLFunction content)
	{
		this.content = content;
	}
	
	public APLFunction getContent()
	{
		return content;
	}
	
	public APLMessage clone()
	{
		APLMessage message = new APLMessage();
		message.setSender(sender);
		message.setReceiver(receiver);
		message.setOntology(ontology);
		message.setPerformative(performative);
		message.setLanguage(language);
		message.setContent(content.clone());
		return message;
	}
	
	public APLFunction toAPLFunction()
	{
		Parser parser = new Parser();
		
		ArrayList<Term> args = new ArrayList<Term>();
		args.add(new APLIdent(sender));
		args.add(new APLIdent(performative));
		args.add(new APLIdent(language));
		args.add(new APLIdent(ontology));
		args.add(content);
		return new APLFunction("message",args);
	}
	
	public String toString()
	{
		return "message("+sender+","+receiver+","+performative+","+language+","+ontology+","+content+")";
	}
}