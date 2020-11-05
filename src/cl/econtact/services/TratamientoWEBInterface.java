package cl.econtact.services;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.google.gson.*;

import cl.econtact.entities.*;

@Path("/tratamiento")

public class TratamientoWEBInterface {
	@GET
	@Path("/version")
	@Produces(MediaType.TEXT_PLAIN)
	public String version() {
		return this.getClass().getCanonicalName();
	}

	//
	@POST
	@Path("/selectPlataforma")
	@Consumes(MediaType.APPLICATION_JSON + "; charset=UTF-8") // UTF-8 -- ISO-8859-15")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8") // UTF-8 -- ISO-8859-15")

	public String selectPlataforma(String request) {

		JsonObject jsonRequest = null; // Parametros de entrada
		Map<String, Object> response = new LinkedHashMap<String, Object>(); // Parametros de salida.

		// Validacion de los parametros entrantes
		try {
			jsonRequest = new JsonParser().parse(request).getAsJsonObject();
		} catch (Exception e) {
			response.put("RC", "-100");
			response.put("MSG", "ERROR: Invalid request format  [" + e.getMessage() + "]");

			return new Gson().toJson(response);
		}

		if (!jsonRequest.has("ANI")) {
			response.put("RC", "-101");
			response.put("MSG", "ERROR: Invalid request - Missing ANI parameter");

			return new Gson().toJson(response);
		}

		if (!jsonRequest.has("DNIS")) {
			response.put("RC", "-101");
			response.put("MSG", "ERROR: Invalid request - Missing DNIS parameter");

			return new Gson().toJson(response);
		}

		if (!jsonRequest.has("TENANT")) {
			response.put("RC", "-101");
			response.put("MSG", "ERROR: Invalid request - Missing TENANT parameter");

			return new Gson().toJson(response);
		}

		// Estas variables solo se declaran para hacer el código más legible.
		String aANI = jsonRequest.get("ANI").getAsString();
		String aDNIS = jsonRequest.get("DNIS").getAsString();
		int aTenant = jsonRequest.get("TENANT").getAsInt();
		
		// Objetos para acceder a las caracteristicas del Cliente (Customer) y del Tratamiento.
		Customer customer = new Customer(aANI);
		
		Tratamiento tratamiento = new Tratamiento(aTenant);
		
		// Objetos que conformarán la respuesta
		int plataformaDestino = 0;
		
		// Validar Lista Negra
		if (customer.profile.has("ocurrencias_LN") && customer.profile.get("ocurrencias_LN").getAsInt() > 0) {
			response.put("RC", "0");
			response.put("MSG", "ANI " + aANI + " Se encuentra en Lista Negra");
			response.put("DATA", "accion:9;" + 
					"Numero_Transferencia_V2:;" + 
					"flagReportes:;" + 
					"PromptFueraHorario:;" + 
					"corta_llamada:SI;" + 
					"mensaje: " + "ANI " + aANI + " Se encuentra en Lista Negra" + ";" + 
					"PlatDbid:;" + 
					"RUT:;" + 
					"CAMPO_01:;" + 
					"CAMPO_02:;" + 
					"CAMPO_03:;" + 
					"CAMPO_04:");
			response.put("cliente", customer.profile);

			return new Gson().toJson(response);
		}

		// OJO: en esta condición hay mucha lógica incorporada en las clases "Customer" y "Tratamiento", por lo tanto, DEBE conservarse el orden de ejecución.
		if (customer.profile.has("ocurrencias_LB") 
				&& customer.profile.get("ocurrencias_LB").getAsInt() <= 0												// Cliente NO está en Lista Blanca
				&& tratamiento.desborde_por_reintentos(aDNIS) > 0														// AND el DNIS tiene configurado un Desborde por Reintentos
				&& customer.reintentosxDNIS(aDNIS) >= tratamiento.attributes.get("desborde_reintentos").getAsInt())	// AND El cliente (ANI) supero el maximo de reintentos permitidos para el DNIS
		{
			plataformaDestino = 	tratamiento.attributes.get("desborde_plataforma_dbid").getAsInt();					// THEN la Plataforma de Destino es la Plataforma de Desborde para el DNIS.
		}
		else 
		{
			plataformaDestino = tratamiento.get_plataforma(aDNIS, customer);
		}

		response.put("RC", "0");
		response.put("MSG", "SUCCESS: " + jsonRequest.get("ANI").getAsString());
		response.put("cliente", customer.profile);

		return new Gson().toJson(response);
	}

}
