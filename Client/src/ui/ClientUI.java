package ui;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.WindowConstants;

import client.Client;

import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.concurrent.Semaphore;

public class ClientUI
{
	private Semaphore sem;
	
	private JFrame frame;
	private JPanel panel;
	
	private JTextArea textArea;
	private JTextField input;
	private JButton sendButton;
		
	private volatile boolean ready;
	
	public ClientUI()
	{
		sem = new Semaphore(1);
		ready = false;
	}
	public void init()
	{
		
		frame = new JFrame("Client");
		frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		frame.setVisible(true);
		frame.addWindowListener(new WindowAdapter()
		{
			public void windowClosed(WindowEvent e)
			{
				System.out.println("Window closed");
				Client.exit();
			}
		});
		frame.setSize(new Dimension(500,500));
		frame.setLayout(new GridLayout(1,1));
		
		panel = new JPanel();
		frame.add(panel);
		
		panel.setLayout(new GridBagLayout());
		
		GridBagConstraints c = new GridBagConstraints();
		
		c.gridx = 0;
		c.gridy = 0;
		c.gridheight = 2;
		c.gridwidth = 3;
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 1;
		c.weighty = .66;
		
		textArea = new JTextArea();
		textArea.setEditable(false);
		JScrollPane scroll = new JScrollPane(textArea);
		panel.add(scroll, c);
		
		c= new GridBagConstraints();
		
		c.gridx = 0;
		c.gridy = 2;
		c.gridheight =1;
		c.gridwidth = 2;
		c.fill = GridBagConstraints.BOTH;
		c.weightx = .5;
		c.weighty = .33;
		
		input = new JTextField();
		input.setFocusable(true);
		input.addKeyListener(new KeyAdapter()
		{
			public void keyPressed(KeyEvent e)
			{
				if(e.getKeyCode() == KeyEvent.VK_ENTER)
				{
					handleInput();
				}
			}
		});
		panel.add(input, c);
		
		c = new GridBagConstraints();
		c.gridx = 2;
		c.gridy = 2;
		c.gridheight =1;
		c.gridwidth = 1;
		c.fill = GridBagConstraints.BOTH;
		c.weightx = .5;
		c.weighty = .33;
		
		sendButton = new JButton("Send");
		sendButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				handleInput();
			}
		});
		
		panel.add(sendButton, c);
		
		ready = true;
		
	}
	
	
	public boolean isReady()
	{
		return ready;
	}
	public void handleInput()
	{
		Client.addTask(input.getText());
		input.setText("");
	}
	
	public void appendText(String message)
	{
		try 
		{
			sem.acquire();
		} 
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
		
		textArea.append(message);
		
		sem.release();
	}
}
