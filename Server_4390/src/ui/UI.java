package ui;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Scanner;

import keys.UserKeys;
import serverChat.server.Server;

public class UI implements Runnable
{
	
	private boolean running;
	
	public UI()
	{
		running = true;
	}

	@Override
	public void run() 
	{
		System.out.println("<<Server 1.0>>\n\n");
		Scanner in = new Scanner(System.in);
		while(running)
		{
			System.out.print(">");
			switch(in.nextLine())
			{
			case "gen":
				generateUser();
				break;
			case "display":
				displayUsers();
				break;
			case "exit":
				running = false;
				break;
			case "help":
				displayHelp();
				break;
			case "current":
				System.out.println(Server.getClientList());
				break;
				default:
					System.out.println("Unknown input, type 'help' for more options");
			}
		}
		in.close();
		Server.exit();
	}
	
	public void displayUsers()
	{
		for(long id: UserKeys.keys.keySet())
		{
			System.out.println("ID: \""+id+"\" key: \""+UserKeys.keys.get(id)+"\"");
		}
	}
	public void generateUser()
	{
		String vals[] = UserKeys.genKey();
		File f = new File(vals[0]+".txt");
		if(f.exists())
		{
			try 
			{
				f.createNewFile();
			}
			catch (IOException e1) 
			{
				e1.printStackTrace();
			}
		}
		try
		{
			FileOutputStream out = new FileOutputStream(f);
			vals[0] = vals[0]+"~~~";
			for(int x =0; x< vals[0].length(); x++)
			{
				out.write(vals[0].charAt(x));
			}
			for(int x =0; x< vals[1].length(); x++)
			{
				out.write(vals[1].charAt(x));
			}
			out.close();
			
		} 
		catch (FileNotFoundException e) 
		{
			e.printStackTrace();
		} catch 
		(IOException e)
		{
			e.printStackTrace();
		}
	}
	public void displayHelp()
	{
		System.out.println("gen");
		System.out.println("display");
		System.out.println("current");
		System.out.println("exit");
		System.out.println("help");
		System.out.println();
	}
}
