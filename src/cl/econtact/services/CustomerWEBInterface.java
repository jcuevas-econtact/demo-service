package cl.econtact.services;

import javax.ws.rs.GET;
import javax.ws.rs.POST; 
import javax.ws.rs.Path;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces; 
import javax.ws.rs.core.MediaType;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.econtact.entities.Customer; 

@Path("/cliente") 

public class CustomerWEBInterface {

	  @GET
	  @Path("/version")
	  @Produces(MediaType.TEXT_PLAIN)
	  public String version() {
	    return this.getClass().getCanonicalName(); 
	  }

	  //
	  @POST
	  @Path("/perfil")
	  @Consumes(MediaType.APPLICATION_JSON + "; charset=UTF-8") //UTF-8 -- ISO-8859-15")
	  @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8") //UTF-8 -- ISO-8859-15")
	  
	  public String getProfile(String request) {

		  JsonObject jsonRequest = null;
		  Map<String, Object> response = new LinkedHashMap<String, Object>();
		  
		  // Validacion de los parametros entrantes
		  try {
			  jsonRequest = new JsonParser().parse(request).getAsJsonObject();
		  }
		  catch (Exception e){
	            response.put("RC", "-100");
		        response.put("MSG", "ERROR: Invalid request format  [" + e.getMessage() + "]");
		        
		        return new Gson().toJson(response);
		  }
		  
		  if (! jsonRequest.has("ANI")) {
			  response.put("RC", "-101");
			  response.put("MSG", "ERROR: Invalid request - Missing ANI parameter");
			  
			  return new Gson().toJson(response);
		  }
		  
		  // Obtener Perfil del Cliente usando el ANI como identificador.
		  
		  Customer customer = new Customer(jsonRequest.get("ANI").getAsString());
		  
		  response.put("RC", "0");
		  response.put("MSG", "SUCCESS: " + jsonRequest.get("ANI").getAsString());
		  response.put("profile", customer.profile);
		  
		  return new Gson().toJson(response);
	  }

	   
}
