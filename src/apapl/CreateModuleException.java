package apapl;

/**
 * Signals that an error occurred during the instantiation of the module.
 */
public class CreateModuleException extends Exception
{
	private static final long serialVersionUID = -9128414065568096399L;

	/**
	 * Constructs a create module exception.
	 * 
	 * @param message the message describing what has caused the exception
	 */
	public CreateModuleException(String message)
	{
		super(message);
	}
}
