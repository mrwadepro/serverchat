package serverChat.server;

import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.concurrent.*;

import keys.COMP_128;
import keys.UserKeys;
import serverExceptions.NoMemberException;
import ui.UI;

public class Server
{
	public static final int UDP_DEFAULT_PORT = 4434;
	public static final int TCP_DEFAULT_PORT = 4435;
	
	//HELLO(long id 8b)
	public static final byte HELLO = 1;
	public static final byte HELLO_LENGTH = 8;//length of message in bytes
	//CHALLENGE(128bit random 16b)
	public static final byte CHALLENGE = 2;
	public static final byte CHALLENGE_LENGTH = 16;
	//RESPONSE(long id 8b, int verif 4b)
	public static final byte RESPONSE = 3;
	public static final byte RESPONSE_LENGTH = 12;
	//AUTH_SUCCESS(4b lengthOFMessage,Message, int portNumb 4b)
	public static final byte AUTH_SUCCESS = 4;
	//AUTH_FAIL()
	public static final byte AUTH_FAIL = 5;
	public static final byte AUTH_FAIL_LENGTH = 0;
	//CONNECT(rand_cookie 16b)
	public static final byte CONNECT = 6;
	public static final byte CONNECT_LENGTH = 16;
	//CONNECTED()
	public static final byte CONNECTED = 7;
	public static final byte CONNECTED_LENGTH = 0;
	//CHAT_REQUEST(long idB 8b)
	public static final byte CHAT_REQUEST = 8;
	public static final byte CHAT_REQUEST_LENGTH = 8;
	//CHAT_STARTED(long sessionID 8b, long idB 8b)
	public static final byte CHAT_STARTED = 9;
	public static final byte CHAT_STARTED_LENGTH = 16;
	//UNREACHABLE(long idB 8b)
	public static final byte UNREACHABLE = 10;
	public static final byte UNREACHABLE_LENGTH = 8;
	//END_REQUEST(long sessionID 8b)
	public static final byte END_REQUEST = 11;
	public static final byte END_REQUEST_LENGTH = 8;
	//END_NOTIF(long sessionID 8b)
	public static final byte END_NOTIF = 12;
	public static final byte END_NOTIF_LENGTH = 8;
	//CHAT(long sessionID 8b, message)
	public static final byte CHAT = 13;
	//HISTORY_REQ(long idB 8b)
	public static final byte HISTORY_REQ = 14;
	public static final byte HISTORY_REQ_LENGTH = 8;
	//HISTORY_RESP(long id 8b, message)
	public static final byte HISTORY_RESP = 15;
	
/*****************************************
 * Data members
 *****************************************/
	private static Semaphore clientSem = new Semaphore(1,true);
	
	private static ArrayList<Client> clients;
	private static int clientPosition;
	private static ArrayList<ChatSession> sessions;
	
	private volatile static boolean running;
	private ListeningSocket listener;
	private UI ui;
	private Thread listenerThread, UIThread;
	private Client currentClient;
	private InputStream curInput;
	
	
	public Server()
	{
		this(UDP_DEFAULT_PORT);
	}
	public Server(int portNum)
	{
		/*
		 * create 2 threads, UI, and Listener, 
		 * then this thread will handle the active clients
		 */
		ui =new UI();
		try 
		{
			listener = new ListeningSocket(portNum);
		}
		catch (SocketException e)
		{
			e.printStackTrace();
		}
		listenerThread = new Thread(listener);
		UIThread = new Thread(ui);
		clients = new ArrayList<Client>();
		sessions = new ArrayList<ChatSession>();
		clientPosition = 0;
		running = true;
	}
		
	public void start()
	{
		int inputLength;
		
		listenerThread.start();
		UIThread.start();
		
		while(running)
		{
			
			//get client
			currentClient = getNextClient();
			if(currentClient == null)
				continue;
			//check for input
			try
			{
				curInput = currentClient.getSocket().getInputStream();
			} 
			catch (IOException e) 
			{
				e.printStackTrace();
				abortClient(currentClient);
			}
			try 
			{
				if(curInput.available()==0)
				{
					if(currentClient.timeSinceLastResponse()> Client.TIMEOUT_MAX)
					{
						System.out.println("User" + currentClient.ID +" timed out");
						abortClient(currentClient);
					}
					continue;
				}
			} 
			catch (IOException e1)
			{
				e1.printStackTrace();
				abortClient(currentClient);
			}
			byte inputL[] = new byte[4];
			try 
			{
				if(curInput.read(inputL) != inputL.length)
				{
					abortClient(currentClient);
					continue;
				}
			} 
			catch (IOException e)
			{
				e.printStackTrace();
				abortClient(currentClient);
				continue;
			}
			
			ByteBuffer bf = ByteBuffer.allocate(Integer.BYTES).put(inputL);
			bf.position(0);
			inputLength = bf.getInt();
			
			
			byte in[] = new byte[inputLength];
			try 
			{
				if(curInput.read(in) != in.length)
				{
					//TODO did not read what was expected
					continue;
				}
			} 
			catch (IOException e)
			{
				e.printStackTrace();
			}
			in = COMP_128.decrypt(in, currentClient.CK_Key);
			
			bf = ByteBuffer.allocate(in.length).put(in);
			bf.position(0);
			
			switch(bf.get())
			{
			case CHAT_REQUEST:
				startChat(currentClient, bf);
				break;
			case CHAT:
				sendMessage(currentClient, bf);
				break;
			case END_REQUEST:
				endSession(currentClient, bf);
				break;
			case HISTORY_REQ:
				sendHistory(currentClient, bf);
				break;			
			}
			currentClient.setLastResponseTime();
		}
		for( Client c : clients)
		{
			try 
			{
				c.getSocket().close();
			} 
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
		listener.shutDown();
	}
	/**
	 * closes the socket and notifies the user they were chatting with if any
	 * does not notify the user being disconnected
	 * @param client the client to close
	 * @param input the input from the stream
	 */
	public void abortClient(Client client)
	{
		/*
		 * get possible session
		 * close client
		 * notify partner if any
		 * close session
		 */
		try
		{
			client.getSocket().close();
		} 
		catch (IOException e)
		{
			e.printStackTrace();
		}
		endSession(client, null);
		removeClient(client);
	}
	/**
	 * 	CHAT_REQUEST(long idB 8b)
	 * starts a ChatSession with another user
	 * @param client the client that sent the request
	 * @param input the message from the client in a Byte buffer, currently at position 1
	 */
	public void startChat(Client client, ByteBuffer input)
	{
		
		long otherID = input.getLong();
		Client otherClient = null;
		for(Client c : clients)
		{
			if(c.ID == otherID)
			{
				otherClient = c;
				break;
			}
		}
		if(otherClient == null || otherClient.inSession())
		{
			ByteBuffer bf = ByteBuffer.allocate(UNREACHABLE_LENGTH+1);
			bf.put(UNREACHABLE).putLong(otherID);
			
			byte sending[] = COMP_128.encrypt(bf.array(), client.CK_Key);
			ByteBuffer toSend = ByteBuffer.allocate(Integer.BYTES+sending.length).putInt(sending.length).put(sending);
			
			try 
			{
				client.getSocket().getOutputStream().write(toSend.array());
				client.getSocket().getOutputStream().flush();
			} 
			catch (IOException e) 
			{
				//TODO failed to send message to client
				e.printStackTrace();
			}
		}
		else //setup is possible
		{
			ChatSession cs = new ChatSession(client, otherClient,sessions.size()+1);
			sessions.add(cs);
			client.startSession(cs.SESSION_ID);
			otherClient.startSession(cs.SESSION_ID);
			
			//send CHAT_STARTED to idA
			//CHAT_STARTED(long sessionID 8b, long idB 8b)
			ByteBuffer bf = ByteBuffer.allocate(CHAT_STARTED_LENGTH+1);
			bf.put(CHAT_STARTED).putLong(cs.SESSION_ID).putLong(otherClient.ID);
			byte sending[] = COMP_128.encrypt(bf.array(),client.CK_Key);
			ByteBuffer toSend = ByteBuffer.allocate(Integer.BYTES+sending.length).putInt(sending.length).put(sending);
			
			try
			{
				client.getSocket().getOutputStream().write(toSend.array());
				client.getSocket().getOutputStream().flush();
			} 
			catch (IOException e)
			{
				//TODO failed to send message to client
				e.printStackTrace();
			}
			
			//send CHAT_STARTED to idB
			bf = ByteBuffer.allocate(CHAT_STARTED_LENGTH+1);
			bf.put(CHAT_STARTED).putLong(cs.SESSION_ID).putLong(client.ID);
			sending = COMP_128.encrypt(bf.array(), otherClient.CK_Key);
			toSend = ByteBuffer.allocate(Integer.BYTES+sending.length).putInt(sending.length).put(sending);
			try
			{
				otherClient.getSocket().getOutputStream().write(toSend.array());
				otherClient.getSocket().getOutputStream().flush();
			}
			catch(IOException e)
			{
				//TODO failed to send message to other client
				e.printStackTrace();
			}
		}
	}
	/**
	 * sends a message from one client to another
	 * @param client the sending client
	 * @param input the message from the client in a Byte buffer, currently at position 1
	 */
	public void sendMessage(Client client, ByteBuffer input)
	{
		long seshid = input.getLong();
		
		//confirm message data
		if(client.getCurSession() != seshid)
		{
			//TODO client trying to send message in another session
		}
		ChatSession sesh = null;
		for(ChatSession cs: sessions)
		{
			if(cs.SESSION_ID == seshid)
			{
				sesh = cs;
				break;
			}
		}
		if(sesh == null)
		{
			endSession(client, null);
		}
		//save message and  send
		else
		{
			String message = "";
			for(int x =9; x< input.array().length; x++)
			{
				message+=(char)input.array()[x];
			}
			
			Client otherClient = null;
			try 
			{
				sesh.addMessage(client, message);
				otherClient = sesh.getPartner(client);
			} 
			catch (NoMemberException e) 
			{
				e.printStackTrace();
			}
						
			ByteBuffer bf = ByteBuffer.allocate(Long.BYTES+message.length()+1).put(CHAT).putLong(seshid).put(stringToByteArray(message));
			
			byte toSend[] = COMP_128.encrypt(bf.array(), otherClient.CK_Key);
			bf = ByteBuffer.allocate(Integer.BYTES+toSend.length).putInt(toSend.length).put(toSend);
			
			try 
			{
				otherClient.getSocket().getOutputStream().write(bf.array());
				otherClient.getSocket().getOutputStream().flush();
			}
			catch (IOException e) 
			{
				e.printStackTrace();
			}
		}
		
	}
	
	/**
	 * END_REQUEST(long sessionID 8b)
	 * ends the session and notifies both users
	 * @param client the client that initiated the session end
	 * @param input the message from the client in a Byte buffer, currently at position 1  OR null if no message
	 */
	public void endSession(Client client, ByteBuffer input)
	{
		ChatSession sesh = null;
		
		if(input!=null)
		{
			long seshID = input.getLong();
			if(!client.inSession() || client.getCurSession()!= seshID)
			{
				return;
			}
		}
		client.endSession();
		
		for(ChatSession cs : sessions)
		{
			if(cs.containsClient(client))
			{
				sesh = cs;
				break;
			}
		}
		
		if(sesh == null)
			return;
		//END_NOTIF(long sessionID 8b)
		ByteBuffer bf = ByteBuffer.allocate(END_NOTIF_LENGTH+1);
		bf = bf.put(END_NOTIF).putLong(sesh.SESSION_ID);
					
		Client partner = null;
		try
		{
			partner = sesh.getPartner(client);
		} 
		catch (NoMemberException e)
		{
			e.printStackTrace();
		}
		
		byte sending[] = null;
		if(!partner.getSocket().isClosed())
		{
			sending = COMP_128.encrypt(bf.array(), partner.CK_Key);
			bf = ByteBuffer.allocate(sending.length+Integer.BYTES).putInt(sending.length).put(sending);
			
			try
			{
				partner.getSocket().getOutputStream().write(bf.array());
				partner.getSocket().getOutputStream().flush();
			} 
			catch (IOException e) 
			{
				e.printStackTrace();
			}
		}
		
		if(!client.getSocket().isClosed())
		{
			sending = COMP_128.encrypt(bf.array(), client.CK_Key);
			bf = ByteBuffer.allocate(sending.length+Integer.BYTES).putInt(sending.length).put(sending);
			try
			{
				client.getSocket().getOutputStream().write(bf.array());
				client.getSocket().getOutputStream().flush();
			} 
			catch (IOException e) 
			{
				e.printStackTrace();
			}
		}
		
		partner.endSession();
		client.endSession();
		sessions.remove(sesh);
	}

	/**
	 * HISTORY_REQ(long idB 8b)
	 * sends the chat history to the requesting client
	 * @param client the client requesting the history
	 * @param input the message from the client in a Byte buffer, currently at position 1
	 */
	public void sendHistory(Client client, ByteBuffer input)
	{
		long clientIdB = input.getLong();
		
		ChatSession sesh = null;
		if(!client.inSession())
		{
			return;
		}
		for(ChatSession s: sessions)
		{
			if(s.SESSION_ID == client.getCurSession())
			{
				sesh = s;
				break;
			}
		}
		try 
		{
			if(sesh == null || sesh.getPartner(client).ID!=clientIdB)
			{
				//TODO client partner not ID of request
				return;
			}
		} 
		catch (NoMemberException e) 
		{
			e.printStackTrace();
		}
		
		ByteBuffer bf;
		//HISTORY_RESP(long id 8b, message)
		for(String message : sesh.getPartnerHistory(client))
		{
			bf = ByteBuffer.allocate(1 + Long.BYTES + message.length()).put(HISTORY_RESP).putLong(clientIdB).put(stringToByteArray(message));
			byte enc[] = COMP_128.encrypt(bf.array(), client.CK_Key);
			bf = ByteBuffer.allocate(enc.length+ Integer.BYTES).putInt(enc.length).put(enc);
			try
			{
				client.getSocket().getOutputStream().write(bf.array());
				client.getSocket().getOutputStream().flush();
			}
			catch (IOException e) 
			{
				e.printStackTrace();
			}
		}
		//TODO sendHistory
	}

	/**
	 * adds a client to the client list
	 * @param c the client to add
	 */
	public static void addClient(Client c)
	{
		try
		{
			clientSem.acquire();
		} 
		catch (InterruptedException e) 
		{
			e.printStackTrace();
		}
		clients.add(c);
		clientSem.release();
	}
	public static void removeClient(Client c)
	{
		try
		{
			clientSem.acquire();
		}
		catch(InterruptedException e)
		{
			e.printStackTrace();
		}
		clients.remove(c);
		clientSem.release();
	}
	/**
	 * gets the next client in the cycle
	 * @return the next client to work with
	 */
	private static Client getNextClient()
	{
		Client ret = null;
		
		try
		{
			clientSem.acquire();
		}
		catch(InterruptedException e)
		{
			e.printStackTrace();
		}
		if(!clients.isEmpty())
		{
			if(clients.size() < clientPosition+1)
				clientPosition = 0;
			ret = clients.get(clientPosition);
			clientPosition++;
		}
		clientSem.release();
		return ret;
	}
	
	public static String getClientList()
	{
		try
		{
			clientSem.acquire();
		}
		catch(InterruptedException e)
		{
			e.printStackTrace();
		}
		
		String ret = "";
		
		for(Client c : clients)
		{
			ret += c.ID+"\n";
		}
		
		clientSem.release();
		return ret;
	}
	
	public static void exit()
	{
		running = false;
	}
	
	public static byte[] stringToByteArray(String s)
	{
		byte ret[] = new byte[s.length()];
		for(int x =0; x< s.length(); x++)
		{
			ret[x] = (byte)s.charAt(x);
		}
		return ret;
	}
	
	public static String byteToString(byte[] b)
	{
		String ret = "";
		for(int x =0; x< b.length; x++)
		{
			ret+= (char)b[x];
		}
		return ret;
	}	
	
	public static void main(String args[])
	{
		UserKeys.loadKeys();
		new Server().start();
		UserKeys.saveKeys();
	}
}
