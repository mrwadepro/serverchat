import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import javax.crypto.spec.SecretKeySpec;
import java.util.Arrays;
import java.net.Socket;

public class testConnectSetup
{
	public static void main(String args[]) throws Exception
	{
		DatagramSocket socket = new DatagramSocket();
		String key = "HA+~dmYybnj4y";
		long id = 12345678;
//SEND HELLO
		byte dbuff[] = new byte[9];
		dbuff[0] = 1;
		ByteBuffer out = ByteBuffer.allocate(Long.BYTES).putLong(id);
		out.position(0);
		for(int x =1; x< out.limit()+1; x++)
		{
			dbuff[x] = out.get(); 
		}


		DatagramPacket p = new DatagramPacket(dbuff, 9,	InetAddress.getLocalHost(), 4434);
		socket.send(p);
		System.out.println("sent Hello");
//SENT HELLO, WAIT FOR CHALLENGE(RAND)
		System.out.println("Waiting on RAND...");
		p.setData(new byte[17]);
		p.setLength(17);
		socket.receive(p);
		System.out.println("recieved RAND");
		System.out.println("Port: "+p.getPort());
		System.out.println("address: "+p.getAddress());
		System.out.println("Data length: "+p.getLength());
		System.out.println();
//RECIEVED CHALLENGE, SEND RESPONSE(long ID, int Verif)
		System.out.println("Recieved rand, computing verification...");
		ByteBuffer bf = ByteBuffer.allocate(16).put(p.getData(), 1, 16);
		bf.position(0);
		String randNum = "";
		for(int x= 0; x< 16; x++)
		{
			randNum+= (char)bf.get();
		}
		long vals[] = COMP_128.A3A8(randNum, key);
		long CK_A = vals[0];
		int authentication = (int)vals[1];
		System.out.println("have verification, sending...");
		p.setData(new byte[13]);
		p.setLength(13);
		bf = ByteBuffer.allocate(13).put((byte)3).putLong(id).putInt(authentication);
		bf.position(0);
		for(int x =0; x< 13; x++)
		{
			p.getData()[x] = bf.get();
		}
		socket.send(p);
		System.out.println("sent RESPONSE(id, verif), waiting for response...");
		System.out.println();

//SENT RESPONSE, WAIT FOR AUTH_SUCCESS OR FAIL
		bf = ByteBuffer.allocate(44);
		p.setData(bf.array());
		p.setLength(bf.limit());
		socket.receive(p);

		SecretKeySpec generatedKey = COMP_128.genKey(CK_A);

		System.out.println("received packet of length: "+p.getLength());
		String message = "";
		for(int x=0; x< p.getLength(); x++)
		{
			message+= (char)p.getData()[x];
		}
		message = COMP_128.decrypt(message, generatedKey);
		byte m[] = new byte[message.length()];
		for(int x =0; x< m.length; x++)
		{
			m[x] = (byte)message.charAt(x);
		}
		bf = ByteBuffer.allocate(4).put(m,17,4);
		bf.position(0);
		message = "";
		for(int x =1; x<=16; x++)
		{
			message+= (char)m[x];
		}
		int port = bf.getInt();

		System.out.println("cookie: "+message);
		System.out.println("Port: "+port);
		socket.close();

		Socket s = new Socket(InetAddress.getLocalHost(), port);
		System.out.println("Connected: "+s.isConnected());
		message = ""+ (char)6 + message;
		message = COMP_128.encrypt(message, generatedKey);
		m = new byte[message.length()];
		for(int x =0; x< message.length(); x++)
		{
			m[x] = (byte)message.charAt(x);
		}
		s.getOutputStream().write(m);
		System.out.println("Sent CONNECT(16b rand), length: "+message.length());
		System.out.println("encryption: "+message);

		s.close();

	}
}