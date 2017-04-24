package serverExceptions;

public class NoMemberException extends Exception
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -9067078872233784516L;
	public NoMemberException(){}
	public NoMemberException(String message)
	{
		super(message);
	}
}
