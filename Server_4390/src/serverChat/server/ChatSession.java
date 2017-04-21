package serverChat.server;

import java.util.ArrayList;

import serverExceptions.NoMemberException;

public class ChatSession
{
	public final long SESSION_ID;
	private Client clientA, clientB;
	private ArrayList<String> messageHistoryA;
	private ArrayList<String> messageHistoryB;
	
	public ChatSession(Client cA, Client cB, long sessionID)
	{
		clientA = cA;
		clientB = cB;
		
		SESSION_ID = sessionID;
		
		messageHistoryA = new ArrayList<String>();
		messageHistoryB = new ArrayList<String>();
	}
	
	public void addMessage(Client source, String message) throws NoMemberException
	{
		if(clientA == source)
			messageHistoryA.add(message);
		else if(clientB == source)
			messageHistoryB.add(message);
		else
			throw new NoMemberException("Client "+source+" is not a member of session "+SESSION_ID);
	}
	public boolean containsClient(Client client)
	{
		return clientA == client || clientB == client;
	}
	public Client getPartner(Client client) throws NoMemberException
	{
		if(clientA == client)
			return clientB;
		else if(clientB == client)
			return clientA;
		else
			throw new NoMemberException("Client "+client+" is not a member of session "+SESSION_ID);
			
	}
}
