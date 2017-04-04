import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.InetAddress;
import java.nio.ByteBuffer;

public class testJava
{
	public static void main(String args[]) throws Exception
	{
		DatagramSocket socket = new DatagramSocket();

		byte dbuff[] = new byte[9];
		dbuff[0] = 1;
		long id = 112323241;
		ByteBuffer out = ByteBuffer.allocate(Long.BYTES).putLong(id);
		out.position(0);
		for(int x =1; x< out.limit()+1; x++)
		{
			dbuff[x] = out.get(); 
		}


		DatagramPacket p = new DatagramPacket(dbuff, 9,	InetAddress.getLocalHost(), 4434);
		socket.send(p);
		System.out.println("Port: "+p.getPort());
		System.out.println("address: "+p.getAddress());
		System.out.print("Data: ");
		for(int x= 0; x<p.getData().length; x++)
		{
			System.out.print(p.getData()[x]);
		}
		System.out.println();
		
		System.out.println("Waiting...");
		socket.receive(p);
		System.out.println("recieved");
		System.out.println("Port: "+p.getPort());
		System.out.println("address: "+p.getAddress());
		System.out.println("Data length: "+p.getLength());
		ByteBuffer bf = ByteBuffer.allocate(Long.BYTES).put(p.getData(), 1, Long.BYTES);
		bf.position(0);
		long randomNum = bf.getLong();

		System.out.print("Data: "+randomNum);
		System.out.println();

		p.setData(new byte[17]);
		p.setLength(17);
		bf = ByteBuffer.allocate(17).put((byte)3).putLong(id).putLong(randomNum);
		bf.position(0);
		for(int x =0; x< 17; x++)
		{
			p.getData()[x] = bf.get();
		}
		socket.send(p);




		socket.close();
	}
}