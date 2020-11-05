package cl.econtact.utilities;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import com.google.gson.Gson;

public class IvrtodbClient {
	
	String HOST = "127.0.0.1";
	int PORT	 = 50089;
	int timeout = 1;
	private SocketClient socket;
	
	public IvrtodbClient()
	{
		this.socket = new SocketClient(this.HOST, this.PORT, this.timeout);
	}
	
	public String sendRequest(String servicio, String funcion, String parametros)
	{
		String responseMsg = null;
		String requestMsg = null;
		
		try {
			
			Map<String, Object> request = new LinkedHashMap<String, Object>();
			
			request.put("servicio", servicio);
			request.put("query", funcion);
			request.put("parameters", parametros);
			request.put("requestID", 1234567890);
			request.put("select", 1);
			
			requestMsg = new Gson().toJson(request);
			
			if (! this.socket.isConnected)
			{
				this.socket.connect(this.HOST, this.PORT);
			} 

			this.socket.send(requestMsg);
		
			responseMsg = this.socket.receive();
			System.out.println("[sendRequest] Mensaje Recibido: " + responseMsg + "\n");
			
			// El ivrtodb desconecta la session despues de enviar la respuesta.
			this.socket.disconnect();
			
		} 
		catch (IOException e) {
			Map<String, Object> errorMsg = new LinkedHashMap<String, Object>();
			
			errorMsg.put("RC", "-302");
			errorMsg.put("MSG", "ERROR: IOException on sendRequest  [" + e.getMessage() + "]");
	        
	        responseMsg = new Gson().toJson(errorMsg);
		}
		catch (Exception e) {
			Map<String, Object> errorMsg = new LinkedHashMap<String, Object>();
			
			errorMsg.put("RC", "-303");
			errorMsg.put("MSG", "ERROR: Exception on sendRequest  [" + e.getMessage() + "]");
	        
	        responseMsg = new Gson().toJson(errorMsg);
		}
		
		System.out.println("[sendRequest] Respuesta enviada: " + responseMsg + "\n");
		return responseMsg;
	}
	
	public boolean close()
	{
		if (this.socket.isConnected)
			return this.socket.disconnect();
		else
			return true;
	}
}

