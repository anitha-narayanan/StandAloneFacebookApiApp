package com.proxy;

import java.net.*;
import java.io.*;

public class ProxyServer extends Thread {
	String args[] = null;
	boolean listening = true;
	public String code=null;
	public String state=null;
	
	public ProxyServer(){
		//this is to satisfy compiler
	}
	public ProxyServer(String[] args){
		this.args = args;
	}
	

	public void startServer(String[] args)throws IOException{
		ServerSocket serverSocket = null;
        
        int port = 10000;	//default
        try {
            port = Integer.parseInt(args[0]);
        } catch (Exception e) {
            //ignore me
        }

        try {
            serverSocket = new ServerSocket(port);
            System.out.println("Started on: " + port);
        } catch (IOException e) {
            System.err.println("Could not listen on port: " + args[0]);
            System.exit(-1);
        }

        
        ProxyThread pt =   new ProxyThread(this, serverSocket.accept());
        pt.start();
        try {
			pt.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        serverSocket.close();
	}
	
	public void run(){
		try {
			this.startServer(args);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) throws IOException {
        ProxyServer pServer = new ProxyServer(args);
        pServer.start();
	}
}