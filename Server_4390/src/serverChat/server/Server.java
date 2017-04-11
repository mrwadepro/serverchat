package serverChat.server;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.concurrent.*;

import keys.RSA;

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
	//AUTH_SUCCESS(128bit random 16b, int portNumb 4b)
	public static final byte AUTH_SUCCESS = 4;
	public static final byte AUTH_SUCCESS_LENGTH = 20;
	//AUTH_FAIL()
	public static final byte AUTH_FAIL = 5;
	public static final byte AUTH_FAIL_LENGTH = 1;
	//CONNECT(rand_cookie 16b)
	public static final byte CONNECT = 6;
	public static final byte CONNECT_LENGTH = 16;
	//
	public static final byte CONNECTED = 7;
	public static final byte CHAT_REQUEST = 8;
	public static final byte CHAT_STARTED = 9;
	public static final byte UNREACHABLE = 10;
	public static final byte END_REQUEST = 11;
	public static final byte END_NOTIF = 12;
	public static final byte CHAT = 13;
	public static final byte HISTORY_REQ = 14;
	public static final byte HISTORY_RESP = 15;
	
	private static Semaphore clientSem = new Semaphore(1,true);
	
	private static ArrayList<Client> clients;
	
	private ListeningSocket listener;
	
	
	public Server()
	{
		this(UDP_DEFAULT_PORT);
	}
	public Server(int portNum)
	{
		try 
		{
			listener = new ListeningSocket(portNum);
		}
		catch (SocketException e)
		{
			e.printStackTrace();
		}
	}
	
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
}
