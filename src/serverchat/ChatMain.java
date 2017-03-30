/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package serverchat;
import java.net.*;
import java.io.*;
import java.util.*;

/**
 *
 * @author Wade
 */
public class ChatMain 
{
    Socket socket;              //Socket for connection to server
    BufferedReader reader;      //Reading from server
    PrintWriter writer;         //Print data over to network
    Boolean isConnected = false; //Specifies if client is connected or not.    
}
