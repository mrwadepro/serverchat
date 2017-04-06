package serverChat.server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;


/**
 * does the entire connection sequence on its own thread
 * @author McGiv
 *
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
	 * @author McGiv
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
				packet.setData(new byte[]{Server.AUTH_FAIL});
				try 
				{
					ds.send(packet);
				} 
				catch (IOException e)
				{
					e.printStackTrace();
				}
				ds.close();
				return;
			}
			
			ByteBuffer bf = ByteBuffer.allocate(Long.BYTES).put(packet.getData(), 1, Long.BYTES);
			bf.position(0);
			long id = bf.getLong();
//TODO check against subscriber list
//send CHALLENGE(random)
			packet.setLength(Long.BYTES+1);
			packet.getData()[0] = Server.CHALLENGE;
			long randomNum = (long)(Math.random()*Long.MAX_VALUE);
			bf = ByteBuffer.allocate(Long.BYTES).putLong(randomNum);
			bf.position(0);
			for(int x =1; x<=Long.BYTES; x++)
				packet.getData()[x] = bf.get();
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
//Receive RESPONSE(ID, random)
			System.out.println("working on response...");
			int timeouts = 0;
			packet.setData(new byte[Server.RESPONSE_LENGTH+1]);
			packet.setLength(Server.RESPONSE_LENGTH+1);			
			while(timeouts < MAX_TIMEOUT)
			{
				try 
				{
					ds.receive(packet);
					System.out.println("recieved RESPONSE");
					bf = ByteBuffer.allocate(Long.BYTES).put(packet.getData(), 1, Long.BYTES);
					bf.position(0);
					id = bf.getLong();
					bf = ByteBuffer.allocate(Long.BYTES);
					bf = bf.put(packet.getData(),Long.BYTES+1, Long.BYTES);
					bf.position(0);
					randomNum = bf.getLong();
					break;
				}
				catch(SocketTimeoutException e)
				{
					timeouts++;
				}
				catch (IOException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if(timeouts >= MAX_TIMEOUT)
			{
				packet.setData(new byte[]{Server.AUTH_FAIL});
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
			}			
		}
	}
	
	public void shutDown()
	{
		running = false;
	}
	
	public static void main(String args[])
	{
		ListeningSocket ls = null;
		try 
		{
			ls = new ListeningSocket(4434);
		} 
		catch (SocketException e)
		{
			e.printStackTrace();
			System.exit(1);
		}
		
		System.out.println("running...");
		ls.run();
		System.out.println("Done");
	}
}
