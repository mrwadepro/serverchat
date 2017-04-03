package serverChat.server;

import java.util.ArrayList;

public class ThreadPool
{
	private int maxWork;
	private ArrayList<Thread> workers;
	public ThreadPool(int max)
	{
		maxWork = max;
		workers = new ArrayList<Thread>();
	}
	
	public void work(Runnable[] r)
	{
		int w = r.length;
		int threads = 1;
		while(w>maxWork)
		{
			w/=2;
			threads++;
		}
	}
}
