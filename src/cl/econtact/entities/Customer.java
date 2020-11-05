package cl.econtact.entities;

import cl.econtact.utilities.*;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;


public class Customer {
	
	public JsonObject profile = null;
	IvrtodbClient ivrtodb = null;
	String ani = null;
	
	String servicio = "CASSANDRA";

	public Customer(String aAni) {
		
		String query = null;
		String parametros = "";
		String ivrtodbResponse = null;
		
		this.ani = aAni;
		
		// Obtener Perfil del Cliente usando el ANI como identificador.
		// Datos del perfil
		// 	RUT
		// 	TIPO_PRODUCTO			numeric
		// 	RANKING					numeric
		// 	TIPO_CLIENTE				string: "NO VIP"
		// 	CONTADOR_LLAMADAS
		// 	CAMPO_01 .. CAMPO_04		string
		  
		// Otros datos relevantes del cliente
		// Si está o no en Lista Blanca.
		// Si está o no en Lista Negra.
		
		// Buscar cliente en tabla NC_CLIENTES.
		query = "SELECT ani, campo_01, campo_02, campo_03, campo_04, ranking, rut, tipo_cliente, tipo_producto " + 
				"FROM derivacion.nc_clientes";
		
		if (this.ani != null && (! this.ani.equals("")) && (! this.ani.matches("\\s+")))
			query += " WHERE ani = '" + this.ani + "'";
		
		// debemos asegurarnos que siempre tendremos SOLO 1 REGISTRO de vuelta.
		query += " LIMIT 1";
		
		this.ivrtodb = new IvrtodbClient();
		
		try
		{
			ivrtodbResponse = this.ivrtodb.sendRequest(servicio, query, parametros);
			System.out.println("[Customer] Perfil: " + ivrtodbResponse + "\n");
			
			JsonArray profiles = new JsonParser().parse(ivrtodbResponse).getAsJsonArray();
			this.profile = profiles.remove(0).getAsJsonObject();
		}
		catch (Exception e) {
			// No hay Cliente con el ANI (o falló la conexión a la BD) asignamos valores por defecto.
			System.out.println("[Customer] Perfil EXCEPTION " + e.getMessage() + "\n");
			this.profile = new JsonObject();
			
			this.profile.addProperty("ani", this.ani);
			this.profile.addProperty("rut", "");
			this.profile.addProperty("tipo_producto", 1);
			this.profile.addProperty("ranking", "1");
			this.profile.addProperty("contador_llamadas", 1);
			this.profile.addProperty("tipo_cliente", "NO VIP");
			
			ivrtodbResponse = this.ivrtodb.sendRequest(servicio, "INSERT INTO derivacion.nc_clientes (ani) VALUES ('" + this.ani + "')", parametros);
			System.out.println("[Customer] Perfil: " + ivrtodbResponse + "\n");
		}
		
		
		// Buscar cliente en Lista Negra
		try {
			ivrtodbResponse = this.ivrtodb.sendRequest(servicio, "SELECT COUNT(ani) AS ocurrencias FROM derivacion.l_negra WHERE ani = '" + this.ani + "'", parametros);
			System.out.println("[Customer] Perfil - buscar en L_NEGRA: " + ivrtodbResponse + "\n");
			
			JsonArray l_negra = new JsonParser().parse(ivrtodbResponse).getAsJsonArray();
			this.profile.addProperty("ocurrencias_LN", l_negra.remove(0).getAsJsonObject().get("ocurrencias").getAsNumber());
		}
		catch (Exception e) {
			System.out.println("[Customer] Perfil EXCEPTION " + e.getMessage() + "\n");
			this.profile.addProperty("ocurrencias_LN", 0);
		}
		
		
		// Buscar cliente en Lista Blanca
		try {
			ivrtodbResponse = this.ivrtodb.sendRequest(servicio, "SELECT COUNT(ani) AS ocurrencias FROM derivacion.l_blanca_contador WHERE ani = '" + this.ani + "'", parametros);
			System.out.println("[Customer] Perfil - buscar en L_BLANCA_CONTADOR: " + ivrtodbResponse + "\n");
			
			JsonArray l_negra = new JsonParser().parse(ivrtodbResponse).getAsJsonArray();
			this.profile.addProperty("ocurrencias_LB", l_negra.remove(0).getAsJsonObject().get("ocurrencias").getAsNumber());
		}
		catch (Exception e) {
			System.out.println("[Customer] Perfil EXCEPTION " + e.getMessage() + "\n");
			this.profile.addProperty("ocurrencias_LB", 0);
		}
		
		// ivrtodb.close();
	}
	
	
	public int reintentosxDNIS(String DNIS)
	{
		String ivrtodbResponse = null;
		int nReintentos = 0;
		
		try {
			ivrtodbResponse = this.ivrtodb.sendRequest(servicio, "SELECT COUNT(ani) AS ocurrencias FROM derivacion.l_blanca_contador WHERE ani = '" + this.ani + "'", "");
			System.out.println("[Customer] reintentosxDNIS - buscar en L_BLANCA_CONTADOR: " + ivrtodbResponse + "\n");
			
			JsonArray reintentos = new JsonParser().parse(ivrtodbResponse).getAsJsonArray();
			nReintentos = reintentos.remove(0).getAsJsonObject().get("ocurrencias").getAsInt();
		}
		catch (Exception e) {
			System.out.println("[Customer] reintentosxDNIS EXCEPTION " + e.getMessage() + "\n");
			nReintentos = 0;
		}
		
		this.profile.addProperty("reintentosxDNIS", nReintentos);
		return nReintentos;
	}
	

}
