package cl.econtact.entities;

import cl.econtact.utilities.*;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;




public class Tratamiento {
	
	public JsonObject attributes = null;
	IvrtodbClient ivrtodb = null;
	
	int tenant = 0;
	
	String servicio = "CASSANDRA";

	public Tratamiento(int aTenant) {
		
		this.ivrtodb = new IvrtodbClient();
		this.tenant = aTenant;
		
	}
	
	
	public int desborde_por_reintentos(String dnis)
	{
		String ivrtodbResponse = null;
		int nro_reintentos = 0;
		int plat_dbid_desb = 0;
		
		String query = "SELECT plat_dbid_desb, nro_reintentos"
				+ " FROM derivacion.desborde_por_reintentos"
				+ " WHERE sdis_dbid = '" + dnis + "'" 
				+ " AND tenant_dbid = " + this.tenant
				// " AND (GETDATE() BETWEEN FEC_INICIO AND FEC_FINAL)" +
				+ " AND ('2017-01-03 10:00:00' BETWEEN fec_inicio AND fec_final)" 
				+ " AND id_status = 1"
				+ " AND nro_reintentos > 0" 
				+ " LIMIT 1";

		try {
			ivrtodbResponse = ivrtodb.sendRequest(servicio, query, "");
			System.out.println("[Tratamiento] desborde_por_reintentos - buscar en derivacion.DESBORDE_POR_REINTENTOS: " + ivrtodbResponse + "\n");

			JsonObject desborde = new JsonParser().parse(ivrtodbResponse).getAsJsonArray().remove(0).getAsJsonObject();
			
			nro_reintentos = desborde.get("nro_reintentos").getAsInt();
			plat_dbid_desb = desborde.get("plat_dbid_desb").getAsInt();
		} 
		catch (Exception e) {
			System.out.println("[Tratamiento] desborde_por_reintentos EXCEPTION " + e.getMessage() + "\n");
		}
		
		this.attributes.addProperty("desborde_DNIS", dnis);
		this.attributes.addProperty("desborde_reintentos", nro_reintentos);
		this.attributes.addProperty("desborde_plataforma_dbid", plat_dbid_desb);
		
		return nro_reintentos;
	}
	
	public int get_plataforma(String dnis, Customer customer)
	{
		int plat_dbid = 0;
		int gdist_dbid = 0;
		String ivrtodbResponse = null;
		
		String query = "SELECT parametro, condicion, valor_condicion, evalua_condicion, gdist_dbid"
				+ " FROM derivacion.criterio_servicio_derivacion_grupo "
				+ " WHERE sdis_dbid = '" + dnis + "'" 
				+ " AND tenant_dbid = " + this.tenant;

		try {
			ivrtodbResponse = ivrtodb.sendRequest(servicio, query, "");
			System.out.println("[Tratamiento] get_plataforma - buscar en derivacion.criterio_servicio_derivacion_grupo: " + ivrtodbResponse + "\n");

			
			JsonObject criterio = null;
			
			String parametro = null;
			String condicion = null;
			
			JsonArray criterios = new JsonParser().parse(ivrtodbResponse).getAsJsonArray();
			int nCriterios = criterios.size();
			
			while (nCriterios > 0 && gdist_dbid == 0) {
				criterio = criterios.remove(0).getAsJsonObject();
				
				parametro = criterio.get("parametro").getAsString();
				condicion = criterio.get("condicion").getAsString();
				
				int valor = criterio.get("valor").getAsInt();
				int evalua = criterio.get("evalua").getAsInt();
				int gdist = criterio.get("gdist").getAsInt();
				
				int variable;
				
				if (evalua == 1) {
					if (parametro.equalsIgnoreCase("CONTADOR_LLAMADAS"))
						variable = customer.profile.get("contador_llamadas").getAsInt();
					else if (parametro.equalsIgnoreCase("TIPO_CLIENTE"))	
						variable = customer.profile.get("tipo_cliente").getAsInt();
					else
						variable = customer.profile.get("ranking").getAsInt();
					
					if (condicion.equalsIgnoreCase("MAYOR IGUAL") && variable >= valor)
						gdist_dbid = gdist;
					else if (condicion.equalsIgnoreCase("MENOR IGUAL") && variable <= valor)
						gdist_dbid = gdist;
					else if (condicion.equalsIgnoreCase("IGUAL") && variable == valor)
						gdist_dbid = gdist;
					
				}
				else {
					gdist_dbid = gdist;
				}
				
				nCriterios--;
			}
			
			
			

		} 
		catch (Exception e) {
			System.out.println("[Tratamiento] get_plataforma EXCEPTION " + e.getMessage() + "\n");
		}
		
		this.attributes.addProperty("gdist_dbid", gdist_dbid);
		this.attributes.addProperty("plat_dbid", plat_dbid);
		
		return plat_dbid;
	}

}
