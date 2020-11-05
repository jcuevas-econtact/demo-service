package cl.econtact.utilities;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class SocketClient {

	private Socket socket;
	private String host;
	private int port;
	
	private BufferedReader in;
	private PrintWriter out;

	public boolean isConnected = false;
	private int timeout;

	public SocketClient(String host, int port, int timeoutquery)
	{
		this.timeout = timeoutquery;
		this.host = host;
		this.port = port;

		this.connect(host, port);
	}

	public void send(String msjRequest) throws IOException
	{

		System.out.println("[SockClient] Se enviara el siguiente mensaje al destino: \n" + msjRequest + "\n");
		try
		{
			this.socket.setSoTimeout(this.timeout * 1000);

			this.out.println(msjRequest);
			this.out.flush();

		}
		catch (Exception e)
		{
			System.out.println("[SockClient] ERROR: to send the message");
			System.out.println("[SockClient] EXCEPTION: " + e.getMessage());
		}

	}
	
	public String receive()
	{
		String message = "";
		String line = null;
		
		try
		{
			while ((line = this.in.readLine()) != null) {
				message += line;
			}
			System.out.println("[SockClient] Mensaje Recibido: " + message + "\n");
		}
		catch (Exception e)
		{
			System.out.println("[SockClient] EXCEPTION: on receive " + e.getMessage());
		}
		
		return message;
	}
	
	public boolean connect()
	{
		return this.connect(this.host, this.port);
	}

	public boolean connect(String host, int port)
	{
		this.isConnected = false;
		
		try
		{
			System.out.println("[SockClient] Will connect with Transact Service, host: " + host + " port: " + port);

			this.socket = new Socket(host, port);
			this.out = new PrintWriter(this.socket.getOutputStream(), true);
			this.in = new BufferedReader(new InputStreamReader(this.socket.getInputStream(), "UTF-8"));

			this.isConnected = true;
		}
		catch (UnknownHostException e)
		{
			System.err.println("Don't know about host: " + host);
		}
		catch (IOException e)
		{
			System.err.println("[SockClient] Couldn't get I/O for the connection to: " + port);
		}

		return this.isConnected;
	}
	
	public boolean disconnect()
	{
		try {
			this.out.close();
			this.in.close();
			this.socket.close();
			
			this.isConnected = false;
		}
		catch (IOException e)
		{
			System.err.println("[SockClient] Couldn't get I/O for the connection to: ");
		}
		
		return true;
	}


}
