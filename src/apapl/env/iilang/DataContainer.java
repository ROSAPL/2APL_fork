package apapl.env.iilang;

import java.util.LinkedList;

/**
 * A superclass for actions, events, et cetera. 
 * Consists of a name and a sequence of parameters. 
 * Additionally it stores a time-stamp that indicates the time of creation,
 * and also a string that indicates the source.
 * 
 * @author tristanbehrens
 *
 */
public abstract class DataContainer extends IILElement {
	
	/** The name of the DataContainer. */
	protected String name = null;
	
	/** A list of parameters. */
	protected LinkedList<Parameter> params = new LinkedList<Parameter>();

	/** Time of creation */
	protected long timeStamp = System.currentTimeMillis();
	
	/** Source of the data-container */
	protected String source = null;
	
	protected DataContainer() {}
	
	
	/** 
	 * Contructs an DataContainer.
	 * 
	 * @param name
	 * @param parameters
	 */
	public DataContainer(String name, Parameter... parameters) {
		
		this.name = name;
		
		for( Parameter p : parameters )
			this.params.add(p);
		
	}

	/** 
	 * Contructs an DataContainer.
	 * 
	 * @param name
	 * @param parameters
	 */
	public DataContainer(String name, LinkedList<Parameter> parameters) {
		
		this.name = name;

		this.params = parameters;
		
	}

	/**
	 * Returns the name.
	 * 
	 * @return the name of the data-container
	 */
	public String getName() {
		
		return name;
		
	}

	/**
	 * Sets the name.
	 * 
	 * @param name the name of the data-container.
	 */
	public void setName(String name) {
		
		this.name = name;
		
	}
	
	/** 
	 * Returns the parameters.
	 * 
	 * @return the parameters of the data-container
	 */
	public LinkedList<Parameter> getParameters() {
		
		return params;
		
	}

	/** 
	 * Returns the parameters. Returns the parameters as a clone.
	 * 
	 * @return the parameters of the data-container
	 */
	public LinkedList<Parameter> getClonedParameters() {
		
		LinkedList<Parameter> ret = new LinkedList<Parameter>();
		
		for( Parameter p : params ) {
			
			ret.add((Parameter) p.clone());
			
		}
		
		return ret;
		
	}

	/**
	 * Sets the parameters.
	 * 
	 * @param params the parameters of the data-container
	 */
	public void setParameters(LinkedList<Parameter> params) {
		
		this.params = params;
		
	}
	
	/**
	 * Adds a parameter to the data-container.
	 * 
	 * @param p the new data-container
	 */
	public void addParameter(Parameter p) {
		
		params.add(p);
		
	}
	
	/** 
	 * Converts a data container to a percept.
	 * 
	 * @param container
	 * @return the percept
	 */
	public static Percept toPercept(DataContainer container) {
		
		Parameter[] parameters = new Parameter[container.params.size()];
		
		for(int a = 0 ; a < parameters.length ; a++ )
			parameters[a] = container.params.get(a);
		
		return new Percept(container.getName(), parameters);
		
	}

	/**
	 * Sets the source of the data-container.
	 * 
	 * @param source is the source of the data-container.
	 */
	public void setSource(String source) {
		
		this.source = source;
		
	}
	
	/**
	 * Returns the source of the data-container.
	 * 
	 * @return the source of the data-container.
	 */
	public String getSource() {
		
		return source;
		
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((params == null) ? 0 : params.hashCode());
		return result;
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {

		if (this == obj)
			return true;
		if (getClass() != obj.getClass())
			return false;
		DataContainer other = (DataContainer) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (params == null) {
			if (other.params != null)
				return false;
		} else if (!params.equals(other.params))
			return false;
		return true;
	
	}
		
}
