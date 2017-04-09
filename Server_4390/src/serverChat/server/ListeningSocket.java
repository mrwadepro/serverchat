package serverChat.server;

import java.io.IOException;
import java.math.BigInteger;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.util.HashMap;

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
				socket.receive(p);
				System.out.println("recieved packet from "+p.getAddress());
				
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
		public static final int MAX_TIMEOUT = 3;
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
//send CHALLENGE(random) TODO add encryption 2.2
			String rand = COMP_128.gen_rand_128Bit();
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
				e.printStackTrace();
				ds.close();
				return;
			}
//Receive RESPONSE(ID, verification)
			System.out.println("working on response...");
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
					System.out.println("recieved RESPONSE");					
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
				return;
			}
			bf = ByteBuffer.allocate(Long.BYTES+ Integer.BYTES).put(packet.getData(), 1, Long.BYTES+Integer.BYTES);
			bf.position(0);
			long idRet = bf.getLong();
			int authClient = bf.getInt();
			//authentication fail
			if(id != idRet || authentication != authClient)
			{
				failConnect();
				return;
			}
//TODO send AUTH_SUCCESS(rand_cookie, port_number)  encryption: CK-A
//establish TCP connection on port port_number, store port
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
	
	public static void main(String args[])
	{
				
//		ListeningSocket ls = null;
//		try 
//		{
//			ls = new ListeningSocket(4434);
//		} 
//		catch (SocketException e)
//		{
//			e.printStackTrace();
//			System.exit(1);
//		}
//		
//		System.out.println("running...");
//		ls.run();
//		System.out.println("Done");
		
		COMP_128.test();
		long key = 1235351312;
		long frame = 1;
		String data = "what a day";
		System.out.println("data:" +data);
		
	}
}
