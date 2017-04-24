package client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.ByteBuffer;

import keys.COMP_128;

import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

public class ClientNetworking
{
	public static final int UDP_DEFAULT_PORT = 4434;
	public static final int TCP_DEFAULT_PORT = 4435;
	
	public static final int DGRAM_TIMEOUT = 2000;
	public static final int DGRAM_TIMEOUT_COUNT_MAX = 3;
	
	public static Socket socket;
	public static long lastResponse;
	
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
	
	
	public static byte[] checkForInput()
	{		
		byte[] input = null;
		try 
		{
			if(socket.getInputStream().available() > 0)
			{
				input = new byte[Integer.BYTES];
				if(socket.getInputStream().read(input) != input.length)
				{
					//TODO didn't read 4 bytes
				}
			}
			else
				return null;
		} 
		catch (IOException e)
		{
			e.printStackTrace();
			try 
			{
				socket.close();
			} 
			catch (IOException e1)
			{
				e1.printStackTrace();
			}
			return null;
		}
		
		ByteBuffer b = ByteBuffer.allocate(Integer.BYTES).put(input);
		b.position(0);
		int messageLength = b.getInt();
		input = new byte[messageLength];
		
		try
		{
			if(socket.getInputStream().read(input) != input.length)
			{
				//TODO didn't read in message right
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
			try 
			{
				socket.close();
			} 
			catch (IOException e1) 
			{
				e1.printStackTrace();
			}
			return null;
		}
		return COMP_128.decrypt(input, Client.CK_Key);
	}
	
	public static void send(byte[] message)
	{		
		byte toSend[] = COMP_128.encrypt(message, Client.CK_Key);
		
		ByteBuffer b = ByteBuffer.allocate(Integer.BYTES + toSend.length).putInt(toSend.length).put(toSend);
		try
		{
			socket.getOutputStream().write(b.array());
			socket.getOutputStream().flush();
		}
		catch (IOException e) 
		{
			e.printStackTrace();
		}
	}
	
	
	public static void exit()
	{
		if(socket != null)
		{
			try
			{
				socket.close();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
		
	}
	
	public static void connect()
	{
		DatagramSocket socket;
		try 
		{
			socket = new DatagramSocket();
		} 
		catch (SocketException e)
		{
			e.printStackTrace();
			return;
		}
		try 
		{
			socket.setSoTimeout(DGRAM_TIMEOUT);
		} 
		catch (SocketException e1) 
		{
			e1.printStackTrace();
		}
//SEND HELLO
		byte dbuff[] = new byte[Long.BYTES + 1];
		dbuff[0] = HELLO;
		ByteBuffer out = ByteBuffer.allocate(Long.BYTES).putLong(Client.id);
		out.position(0);
		for(int x =1; x< out.limit()+1; x++)
		{
			dbuff[x] = out.get(); 
		}


		DatagramPacket p = new DatagramPacket(dbuff, dbuff.length,	Client.hostAddress, UDP_DEFAULT_PORT);
		try
		{
			socket.send(p);
		}
		catch (IOException e)
		{
			socket.close();
			e.printStackTrace();
			return;
		}
//SENT HELLO, WAIT FOR CHALLENGE(RAND)
		int timeoutCount = 0;
		p.setData(new byte[CHALLENGE_LENGTH+1]);
		p.setLength(CHALLENGE_LENGTH+1);
		
		while(timeoutCount < DGRAM_TIMEOUT_COUNT_MAX)
		{
			if(Thread.interrupted())
			{
				socket.close();
				return;
			}
			
			try 
			{
				socket.receive(p);
				System.out.println("got packet");
				System.out.println("Address: "+p.getAddress());
				System.out.println("port: "+p.getPort());
				break;
			} 
			catch(SocketTimeoutException e)
			{
				System.out.println("Timeout...");
				timeoutCount++;
			}
			catch (IOException e) 
			{
				socket.close();
				e.printStackTrace();
				return;
			}
		}
		if(timeoutCount >= DGRAM_TIMEOUT_COUNT_MAX)
		{
			socket.close();
			return;
		}
//RECIEVED CHALLENGE, SEND RESPONSE(long ID, int Verif)
		ByteBuffer bf = ByteBuffer.allocate(CHALLENGE_LENGTH).put(p.getData(), 1, CHALLENGE_LENGTH);
		bf.position(0);
		String randNum = "";
		for(int x= 0; x< CHALLENGE_LENGTH; x++)
		{
			randNum+= (char)bf.get();
		}
		long vals[] = COMP_128.A3A8(randNum, Client.key);
		long CK_A = vals[0];
		int authentication = (int)vals[1];
		p.setData(new byte[RESPONSE_LENGTH+1]);
		p.setLength(RESPONSE_LENGTH+1);
		bf = ByteBuffer.allocate(RESPONSE_LENGTH+1).put(RESPONSE).putLong(Client.id).putInt(authentication);
		bf.position(0);
		for(int x =0; x< RESPONSE_LENGTH+1; x++)
		{
			p.getData()[x] = bf.get();
		}
		try 
		{
			socket.send(p);
		}
		catch (IOException e)
		{
			socket.close();
			e.printStackTrace();
			return;
		}

//SENT RESPONSE, WAIT FOR AUTH_SUCCESS OR FAIL
		bf = ByteBuffer.allocate(32);
		p.setData(bf.array());
		p.setLength(bf.limit());
		try
		{
			socket.receive(p);
		} 
		catch (IOException e)
		{
			socket.close();
			e.printStackTrace();
			return;
		}

		Client.CK_Key = COMP_128.genKey(CK_A);
		
		
		byte m[] = COMP_128.decrypt(p.getData(), Client.CK_Key);
		
		bf = ByteBuffer.allocate(Integer.BYTES).put(m,17,Integer.BYTES);
		bf.position(0);
		
		String message = "";
		for(int x =1; x<=16; x++)
		{
			message+= (char)m[x];
		}
		int port = bf.getInt();
		
		socket.close();

		Socket s = null;
		try
		{
			s = new Socket(Client.hostAddress, port);
		} 
		catch (IOException e)
		{
			e.printStackTrace();
		}
		message = ""+ (char)CONNECT + message;
		m = COMP_128.encrypt(stringToByteArray(message), Client.CK_Key);
		
		try
		{
			s.getOutputStream().write(m);
		} 
		catch (IOException e)
		{
			e.printStackTrace();
		}
		
		ClientNetworking.socket = s;
		socket.close();
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
	
}