package apapl;

import java.util.ArrayList;
import java.util.Vector;

import apapl.data.APLFunction;
import apapl.data.APLIdent;
import apapl.data.APLNum;
import apapl.data.Term;
import eis.iilang.Action;
import eis.iilang.ActionResult;
import eis.iilang.Identifier;
import eis.iilang.Numeral;
import eis.iilang.Parameter;
import eis.iilang.Percept;

public class IILConverter {

	public static Action convertToAction(APLFunction action) {
		
		Parameter[] params = new Parameter[action.getParams().size()];
		
		int index = 0;
		for( Term t : action.getParams() ) {
			
			Parameter p = convert(t);
			
			params[index] = p;
			index ++;
			
		}
		
		return new Action(action.getName(), params);
		
	}
	
	public static Parameter convert(Term term) {
		
		if( term instanceof APLNum ) {
			
			Numeral ret = new Numeral( ((APLNum)term).getVal() );
		
			return ret;
			
		}
		if( term instanceof APLIdent ) {
			
			return new Identifier( ((APLIdent)term).getName() );
		
		}
		else {
			
			assert false: "Unknown type " + term.getClass();
			
		}
			
		return null;
		
	}
	
	public static APLFunction convert(Percept percept) {

		// terms
		ArrayList<Term> params = new ArrayList<Term>();

		for ( Parameter p : percept.getParameters() ) {
		
			params.add( IILConverter.convert(p) );
			
		}
		
		return new APLFunction(percept.getName(), params);
		
	}
	
	public static Term convert(Parameter parameter) {

		Term ret = null;
		
		if ( parameter instanceof Identifier ) {
			
			return new APLIdent(((Identifier)parameter).getValue());
			
		}
		if ( parameter instanceof Numeral ) {
			
			return new APLNum( ((Numeral)parameter).getValue().doubleValue() );
			
		}
		else {
			
			assert false: "Unknown type " + parameter.getClass();
			
		}
		return ret;
		
	}

	public static Term convert(ActionResult result) {

		// terms
		ArrayList<Term> params = new ArrayList<Term>();

		for ( Parameter p : result.getParameters() ) {
		
			params.add( IILConverter.convert(p) );
			
		}
		
		return new APLFunction(result.getName(), params);
		
	}
	
}
