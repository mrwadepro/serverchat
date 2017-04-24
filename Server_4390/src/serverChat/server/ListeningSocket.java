package serverChat.server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;

import javax.crypto.spec.SecretKeySpec;

import keys.COMP_128;
import keys.UserKeys;


/**
 * does the entire connection sequence on its own thread
 */
public class ListeningSocket implements Runnable
{
	private DatagramSocket socket;
	private volatile boolean running;
	
	public ListeningSocket(int port) throws SocketException
	{
		socket = new DatagramSocket(port);
		socket.setSoTimeout(2000);
		running = true;
	}

	@Override
	/**
	 * socket receives a packet
	 * sends it to connection handler in another thread
	 * waits for more data
	 */
	public void run() 
	{
		byte dbuff[] = new byte[Server.HELLO_LENGTH+1];
		DatagramPacket p = new DatagramPacket(dbuff, dbuff.length);
//run until turned off	
		while(running)
		{
			try 
			{
//wait for HELLO
				socket.receive(p);
				
				ConnectionHandler h = new ConnectionHandler(p);
				Thread next = new Thread(h);
				next.start();
				dbuff = new byte[Server.HELLO_LENGTH+1];
				p = new DatagramPacket(dbuff, dbuff.length);
			}
			catch(SocketTimeoutException e)
			{
				
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
		socket.close();
	}
	
	/**
	 * used for a separate thread that handles an incoming connection
	 *
	 */
	private class ConnectionHandler implements Runnable
	{
		public static final int MAX_TIMEOUT = 10;
		DatagramPacket packet;
		DatagramSocket ds;
		
		public ConnectionHandler(DatagramPacket p)
		{
			packet = p;
		}
		
		public void run()
		{
			ds = null;
			try
			{
				ds = new DatagramSocket();
			} 
			catch (SocketException e)
			{
				e.printStackTrace();
				return;
			}
			
//check for HELLO
			if(packet.getData()[0] != Server.HELLO)
			{
				System.out.println("sent fail");
				failConnect();
				return;
			}
			
			ByteBuffer bf = ByteBuffer.allocate(Long.BYTES).put(packet.getData(), 1, Long.BYTES);
			bf.position(0);
			long id = bf.getLong();
			String key;
			if((key = UserKeys.getKey(id)) == null)
			{
				failConnect();
				return;
			}
//DONE check for HELLO
//send CHALLENGE(random)
			String rand = COMP_128.gen_rand_128Bit();
			packet.setData(new byte[Server.CHALLENGE_LENGTH+1]);
			packet.setLength(Server.CHALLENGE_LENGTH+1);
			packet.getData()[0] = Server.CHALLENGE;
			for(int x =0; x< rand.length(); x++)
				packet.getData()[x+1] = (byte)rand.charAt(x);
			try
			{
				ds.send(packet);
			}
			catch (IOException e)
			{
				System.out.println("Failed to send CHALLENGE");
				e.printStackTrace();
				ds.close();
				return;
			}
//DONE send CHALLENGE(random)
//Receive RESPONSE(ID, verification), gen CK_A
			long vals[] = COMP_128.A3A8(rand, key);
			long CK_A = vals[0];
			int authentication = (int)vals[1];
			int timeouts = 0;
			packet.setData(new byte[Server.RESPONSE_LENGTH+1]);
			packet.setLength(Server.RESPONSE_LENGTH+1);			
			while(timeouts < MAX_TIMEOUT)
			{
				try 
				{
					ds.receive(packet);					
					break;
				}
				catch(SocketTimeoutException e)
				{
					timeouts++;
				}
				catch (IOException e)
				{
					e.printStackTrace();
					ds.close();
					return;
				}
			}
			if(timeouts >= MAX_TIMEOUT)
			{
				failConnect();
				System.out.println("timeout");
				return;
			}
			bf = ByteBuffer.allocate(Long.BYTES+ Integer.BYTES).put(packet.getData(), 1, Long.BYTES+Integer.BYTES);
			bf.position(0);
			long idRet = bf.getLong();
			int authClient = bf.getInt();
			//authentication fail
			if(id != idRet || authentication != authClient)
			{
				System.out.println(""+id+" "+ idRet);
				System.out.println(""+authentication+" "+authClient);
				failConnect();
				System.out.println("Failed Verification");
				return;
			}
//DONE Receive RESPONSE(ID, verification)
//send AUTH_SUCCESS(rand_cookie, port_number)  encryption: CK-A
			ServerSocket ss = null;
			try 
			{
				ss = new ServerSocket(Server.TCP_DEFAULT_PORT);
			} 
			catch (IOException e)
			{
				e.printStackTrace();
				failConnect();
				return;
			}
			int port = ss.getLocalPort();
			String rand_cookie = COMP_128.gen_rand_128Bit();
			SecretKeySpec clientKey = COMP_128.genKey(CK_A);
			
			bf = ByteBuffer.allocate(1 + rand_cookie.length() + Integer.BYTES);
			bf.put(Server.AUTH_SUCCESS);
			bf.put(Server.stringToByteArray(rand_cookie));
			bf.putInt(port);
			
			byte toSend[] = COMP_128.encrypt(bf.array(), clientKey);
			
			packet.setData(toSend);
			packet.setLength(toSend.length);
			try 
			{
				ds.send(packet);
			}
			catch (IOException e)
			{
				e.printStackTrace();
				failConnect();
				try
				{
					ss.close();
				} 
				catch (IOException e1)
				{
					e1.printStackTrace();
				}
				return;
			}
//DONE send AUTH_SUCCESS(rand_cookie, port_number)  encryption: CK-A
//establish TCP connection on port port_number, store port
			timeouts = 0;
			Socket clientSock = null;
			while(timeouts < MAX_TIMEOUT)
			{
				try 
				{
					clientSock = ss.accept();
					break;
				} 
				catch(SocketTimeoutException e)
				{
					timeouts++;
				}
				catch (IOException e)
				{
					e.printStackTrace();
					failConnect();
					try 
					{
						ss.close();
					} 
					catch (IOException e1) 
					{
						e1.printStackTrace();
					}
					return;
				}
			}
			if(timeouts >= MAX_TIMEOUT)
			{
				failConnect();
				try
				{
					ss.close();
				} 
				catch (IOException e) 
				{
					e.printStackTrace();
				}
				return;
			}
			try
			{
				ss.close();
			} 
			catch (IOException e) 
			{
				e.printStackTrace();
			}
			
//DONE establish TCP connection on port port_number, store port
//receive CONNECT(rand_cookie 16 b)
			Client client = new Client(clientSock,id, clientKey);
			byte cookie_ret[] = new byte[32];
			try 
			{
				if(clientSock.getInputStream().read(cookie_ret)!=32)
				{
					throw new IOException();
				}
			} 
			catch (IOException e)
			{
				e.printStackTrace();
				failConnect();
				try
				{
					clientSock.close();
				} 
				catch (IOException e1) 
				{
					e1.printStackTrace();
				}
				return;
			}

			cookie_ret = COMP_128.decrypt(cookie_ret, client.CK_Key);
			String cRet = "";
			for(int x =1; x< cookie_ret.length; x++)
			{
				cRet += (char)cookie_ret[x];
			}
			if(!cRet.equals(rand_cookie))
			{
				System.out.println("Keys not the same, exiting");
				failConnect();
				try
				{
					clientSock.close();
				} 
				catch (IOException e) 
				{
					e.printStackTrace();
				}
				return;
			}
			client.setLastResponseTime();
			Server.addClient(client);
			ds.close();
//DONE receive CONNECT(rand_cookie)			
		}
		
		private void failConnect()
		{
			packet.setLength(1);
			packet.setData(new byte[]{Server.AUTH_FAIL});
			try
			{
				ds.send(packet);
			} 
			catch (IOException e)
			{
				e.printStackTrace();
				ds.close();
			}
		}
	}
	
	public void shutDown()
	{
		running = false;
	}
}
