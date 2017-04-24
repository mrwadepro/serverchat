package client;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.Queue;
import java.util.Scanner;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.crypto.spec.SecretKeySpec;
import javax.swing.SwingUtilities;

import ui.ClientUI;

public class Client 
{
	enum State
	{
		DISCONNECTED, 
		CONNECTED, 
		IN_CHAT;
	}
	
	private static final String DEFAULT_FILE_NAME = "Info.txt";
	
	public static String key;
	public static long id;
	public static InetAddress hostAddress;
	public static SecretKeySpec CK_Key;
	
	private static volatile boolean done;
	
	private ClientUI ui;
	
	private static Queue<String> tasks;
	
	private static State state;
	private long otherUserID;
	private long sessionID;
	
	private static Thread mainThread = Thread.currentThread();
	
	public Client()
	{
		try
		{
			hostAddress = InetAddress.getLocalHost();
		} 
		catch (UnknownHostException e)
		{
			e.printStackTrace();
		}
		state = State.DISCONNECTED;
		ui = new ClientUI();
		tasks = new ConcurrentLinkedQueue<String>();
	}
	
	public void start()
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				ui.init();
			}
		});
		
		while(!ui.isReady());
		
		loadInfo();
		
		while(!done)
		{
			String task = tasks.poll();
			if(task!=null)
			{
				if(task.equalsIgnoreCase("Log on"))
				{
					logOn();
				}
				else if(task.startsWith("chat") || task.startsWith("Chat"))
				{
					if(state == State.CONNECTED)
					{
						long otherID = 0;
						try
						{
							otherID = Long.parseLong(task.substring(task.indexOf("t")+1).trim());
						}
						catch(NumberFormatException e)
						{
							ui.appendText("Local: id is NaN\n");
							continue;
						}
						//CHAT_REQUEST(long idB 8b)
						System.out.println("sending chat request for id "+otherID);
						ByteBuffer bf = ByteBuffer.allocate(ClientNetworking.CHAT_REQUEST_LENGTH+1).put(ClientNetworking.CHAT_REQUEST);
						bf.putLong(otherID);
						ClientNetworking.send(bf.array());
					}
					else if (state == State.DISCONNECTED)
					{
						ui.appendText("Local: Not connected to server, type 'log on' to connect\n");
					}
					else //state == State.IN_CHAT
					{
						ui.appendText("Local: Already in chat with user "+otherUserID+"\n");
					}
				}
				else if(task.startsWith("History") || task.startsWith("history"))
				{
					if(state == State.IN_CHAT)
					{
						long otherID = Long.parseLong(task.substring(task.indexOf("y")+1).trim());
						//HISTORY_REQ(long idB 8b)
						ByteBuffer bf = ByteBuffer.allocate(ClientNetworking.HISTORY_REQ_LENGTH+1);
						bf.put(ClientNetworking.HISTORY_REQ).putLong(otherID);
						ClientNetworking.send(bf.array());
					}
					else if (state == State.DISCONNECTED)
					{
						ui.appendText("Local: Not connected to server, type 'log on' to connect\n");
					}
					else //state == State.Connected
					{
						ui.appendText("Local: Not in chat session, type 'chat <userid>' to begin chat\n");
					}
				}
				else if(task.equalsIgnoreCase("end chat"))
				{
					if(state == State.IN_CHAT)
					{
						endChat();
						ui.appendText("Local: disconnected from chat with "+otherUserID+"\n");
						otherUserID = -1;
						sessionID = -1;
						state = State.CONNECTED;
					}
					else if (state == State.CONNECTED)
					{
						ui.appendText("Local: Not in chat session, type 'chat <userid>' to begin chat\n");
					}
					else//state == State.DISCONNECTED
					{
						ui.appendText("Local: Not connected to server, type 'log on' to connect\n");
					}

					
				}
				else if(task.equalsIgnoreCase("log off"))
				{
					if(state == State.IN_CHAT)
					{
						endChat();
					}
					ClientNetworking.exit();
					state = State.DISCONNECTED;
					ui.appendText("Local: Disconnected from server\n");
				}
				else
				{
					if(state == State.IN_CHAT)
					{
						//CHAT(long sessionID 8b, message)
						ByteBuffer bf = ByteBuffer.allocate(1 + Long.BYTES + task.length());
						bf.put(ClientNetworking.CHAT).putLong(sessionID).put(ClientNetworking.stringToByteArray(task));
						ClientNetworking.send(bf.array());
						ui.appendText("You: "+task+"\n");
					}
					else if (state == State.DISCONNECTED)
					{
						ui.appendText("Local: Not connected to server, type 'log on' to connect\n");
					}
					else //state == State.CONNECTED
					{
						ui.appendText("Local: Not in chat session, type 'chat <userid>' to begin chat\n");
					}
				}
				
				
			}
			if(state != State.DISCONNECTED)
			{
				byte in[] = ClientNetworking.checkForInput();
				
				if(in !=null)
				{
					ByteBuffer bf = ByteBuffer.allocate(in.length).put(in);
					bf.position(0);
					
					switch(bf.get())
					{
					//CHAT_STARTED(long sessionID 8b, long idB 8b)
					case ClientNetworking.CHAT_STARTED:
						sessionID = bf.getLong();
						otherUserID = bf.getLong();
						state = State.IN_CHAT;
						ui.appendText("Started chat with ID "+otherUserID+"\n");
						break;
						//UNREACHABLE(long idB 8b)
					case ClientNetworking.UNREACHABLE:
						ui.appendText("Correspondent is unreachable\n");
						break;
						//END_NOTIF(long sessionID 8b)
					case ClientNetworking.END_NOTIF:
						if(bf.getLong() == sessionID)
						{
							ui.appendText("Ended chat session with "+otherUserID+"\n");
							sessionID = -1;
							otherUserID = -1;
							state = State.CONNECTED;
						}
						break;
						//HISTORY_RESP(long id 8b, message)
					case ClientNetworking.HISTORY_RESP:
						long id = bf.getLong();
						String message = "";
						for(int x =9; x< bf.array().length; x++)
						{
							message += (char)bf.array()[x];
						}
						ui.appendText("History ID "+id+": "+message+"\n");
						break;
						//CHAT(long sessionID 8b, message)
					case ClientNetworking.CHAT:
						message ="";
						for(int x =9; x< bf.array().length; x++)
						{
							message += (char)bf.array()[x];
						}
						ui.appendText("ID "+otherUserID+": "+message+"\n");
						break;
						
					}
				}
			}
		}
		System.out.println("done");
	}

	private void endChat()
	{
		//END_REQUEST(long sessionID 8b)
		ByteBuffer bf = ByteBuffer.allocate(ClientNetworking.END_REQUEST_LENGTH+1);
		bf.put(ClientNetworking.END_REQUEST).putLong(sessionID);
		ClientNetworking.send(bf.array());
	}
	private void logOn()
	{
		if(state == State.DISCONNECTED)
		{
			ui.appendText("Connecting...");
			ClientNetworking.connect();
			if(ClientNetworking.socket!=null)
			{
				ui.appendText("success\n");
				state = State.CONNECTED;
			}
			else
				ui.appendText("Fail\n");
		}
		else
		{
			ui.appendText("Already connected\n");
		}
	}
	
	public static void addTask(String task)
	{
		tasks.add(task);
	}
	
	public void loadInfo()
	{
		Scanner in = null;
		File f = new File(DEFAULT_FILE_NAME);
		
		if(!f.exists())
		{
			ui.appendText("No file: "+DEFAULT_FILE_NAME+"...aborting\n");
			exit();
			return;
		}
		try 
		{
			in = new Scanner(f);
			in.useDelimiter("~~~");
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
		id = Long.parseLong(in.next());
		key =in.next();		
		
		System.out.println("added ID: "+id);
		System.out.println("added key: "+key);
	}
	
	public static void exit()
	{
		done = true;
		mainThread.interrupt();
		ClientNetworking.exit();
	}
}
