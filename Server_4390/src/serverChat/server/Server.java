package serverChat.server;

import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.concurrent.*;

import javax.crypto.spec.SecretKeySpec;

import keys.COMP_128;
import keys.UserKeys;
import serverExceptions.NoMemberException;

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
	//CHAT(long sessionID 8b, int messageLength 4b, message)
	public static final byte CHAT = 13;
	//HISTORY_REQ(long idB 8b)
	public static final byte HISTORY_REQ = 14;
	public static final byte HISTORY_REQ_LENGTH = 8;
	//HISTORY_RESP(long id 8b, int messageLength 4b, message)
	public static final byte HISTORY_RESP = 15;
	
/*****************************************
 * Data members
 *****************************************/
	private static Semaphore clientSem = new Semaphore(1,true);
	
	private static ArrayList<Client> clients;
	private static int clientPosition;
	private static ArrayList<ChatSession> sessions;
	
	private boolean running;
	private ListeningSocket listener;
	private Thread listenerThread;
	private Client currentClient;
	private InputStream curInput;
	private byte buffer[];
	
	
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
		try 
		{
			listener = new ListeningSocket(portNum);
		}
		catch (SocketException e)
		{
			e.printStackTrace();
		}
		listenerThread = new Thread(listener);
		clients = new ArrayList<Client>();
		sessions = new ArrayList<ChatSession>();
		clientPosition = 0;
		running = true;
	}
		
	public void start()
	{
		int inputLength;
		
		listenerThread.start();
		
		while(running)
		{
			
			//get client
			currentClient = getNextClient();
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
						abortClient(currentClient);
					continue;
				}
			} 
			catch (IOException e1)
			{
				e1.printStackTrace();
				abortClient(currentClient);
			}
			try 
			{
				inputLength = curInput.read();
			} 
			catch (IOException e)
			{
				e.printStackTrace();
				abortClient(currentClient);
				continue;
			}
			if(inputLength == -1)
			{
				abortClient(currentClient);
				continue;
			}
			
			byte in[] = new byte[inputLength];
			if(curInput.read(in) != in.length)
			{
				//TODO did not read what was expected
				continue;
			}
			
			in = decrypt(in, currentClient.CK_Key);
			
			ByteBuffer bf = ByteBuffer.allocate(in.length).put(in);
			bf.position(0);
			
			switch(bf.getInt())
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
			bf.put(UNREACHABLE).putLong(otherClient.ID);
			
			byte sending[] = encrypt(bf.array(), client.CK_Key);
			
			try 
			{
				client.getSocket().getOutputStream().write(sending.length);
				client.getSocket().getOutputStream().write(sending);
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
			ByteBuffer bf = ByteBuffer.allocate(CHAT_STARTED_LENGTH+1);
			bf = bf.put(CHAT_STARTED).putLong(cs.SESSION_ID).putLong(otherClient.ID);
			
			byte sending[] = encrypt(bf.array(),client.CK_Key);
			
			try
			{
				client.getSocket().getOutputStream().write(sending.length);
				client.getSocket().getOutputStream().write(sending);
				client.getSocket().getOutputStream().flush();
			} 
			catch (IOException e)
			{
				//TODO failed to send message to client
				e.printStackTrace();
			}
			
			//send CHAT_STARTED to idB
			bf.position(Long.BYTES+1);
			bf.putLong(client.ID);
			
			sending = encrypt(bf.array(), otherClient.CK_Key);
			
			try
			{
				otherClient.getSocket().getOutputStream().write(sending.length);
				otherClient.getSocket().getOutputStream().write(sending);
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
		int messageLength = input.getInt();
		
		//confirm message data
		if(client.getCurSession() != seshid)
		{
			//TODO client trying to send message in another session
		}
		if(messageLength < 0)
		{
			//TODO error with message length
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
			//TODO session ended without notifying client
		}
		//save message and  send
		else
		{
			buffer = new byte[messageLength];
			
			try
			{
				if(client.getSocket().getInputStream().read(buffer) != messageLength)
				{
					//TODO ERROR reading in message
				}
			} 
			catch (IOException e)
			{
				e.printStackTrace();
			}
			
			ByteBuffer bf = ByteBuffer.allocate(messageLength).put(buffer);
			String message = "";
			for(int x =0; x< bf.array().length; x++)
			{
				message += (char)bf.array()[x];
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
			
			
			bf = ByteBuffer.allocate(Long.BYTES+Integer.BYTES+messageLength);
			bf = bf.putLong(sesh.SESSION_ID).putInt(messageLength).put(buffer);
			
			byte toSend[] = encrypt(bf.array(), otherClient.CK_Key);
			
			try 
			{
				otherClient.getSocket().getOutputStream().write(toSend.length);
				otherClient.getSocket().getOutputStream().write(toSend);
				otherClient.getSocket().getOutputStream().flush();
			}
			catch (IOException e) 
			{
				e.printStackTrace();
			}
		}
		
	}
	
	/**
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
		
		byte[] sending = encrypt(bf.array(), partner.CK_Key);
		
		try
		{
			partner.getSocket().getOutputStream().write(sending.length);
			partner.getSocket().getOutputStream().write(sending);
			partner.getSocket().getOutputStream().flush();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
		
		sending = encrypt(bf.array(), client.CK_Key);
		try
		{
			client.getSocket().getOutputStream().write(sending.length);
			client.getSocket().getOutputStream().write(sending);
			client.getSocket().getOutputStream().flush();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
		partner.endSession();
		client.endSession();
		sessions.remove(sesh);
	}

	/**
	 * sends the chat history to the requesting client
	 * @param client the client requesting the history
	 * @param input the message from the client in a Byte buffer, currently at position 1
	 */
	public void sendHistory(Client client, ByteBuffer input)
	{
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
	
	
	private byte[] encrypt(byte[] b, SecretKeySpec key)
	{
		return stringToByteArray(COMP_128.encrypt(byteToString(b),key));
	}
	private byte[] decrypt(byte[] b, SecretKeySpec key)
	{
		return stringToByteArray(COMP_128.decrypt(byteToString(b),key));
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
			ret+= (byte)b[x];
		}
		return ret;
	}
	
	
	public static void main(String args[])
	{
		/*
		UserKeys.loadKeys();
		ListeningSocket listener = null;
		try 
		{
			listener = new ListeningSocket(UDP_DEFAULT_PORT);
		}
		catch (SocketException e)
		{
			e.printStackTrace();
		}
		Thread listenerThread = new Thread(listener);
		listenerThread.start();
		//new Server().start();
		UserKeys.saveKeys();
		/*
		SecretKeySpec s = COMP_128.genKey(123456l);
		String message = "00001121000Fabcdefsdfefsdcdlhjvkjvkjvykca;lsdkfjas;fliawebfjawei;vbawiejf";
		System.out.println(message);
		System.out.println("Message length: "+message.length());
		String enc = COMP_128.encrypt(message, s);
		System.out.println("enc length: "+enc.length());
		message = COMP_128.decrypt(enc, s);
		System.out.println(message);
		*/
	}
}
