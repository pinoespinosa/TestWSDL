package Web;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import xml.EventoAmbiguedad;
import ClasesBD.FormaFarmaceutica;
import ClasesBD.Script;


public class BuscadorMedicamentos {

	private static boolean hallado= false;
	
	public static boolean isFlagEventoAmbiguedadOFF(){
		return Testing.bufferAmbiguedades==null;
	}
	public static String getDroga(String droga_dosis){
		return droga_dosis.split("_")[0];
	}
	public static double getDosis(String droga_dosis){
		return Double.parseDouble(droga_dosis.split("_")[1].toString());
	}
	
	public static List<String> getDrogasEnMedicamento(String id){

		List<String> resultado = new ArrayList<String>();
	
		hallado=false;
		
		if (Integer.parseInt(id)>=0)
		{
			// Busco si esta referenciado directametne
			resultado.addAll(buscarMedRefDirecto(id));
			
			if (!hallado)
				resultado.addAll(buscarCacheDeBusquedas(id));

			if ( !hallado && BuscadorMedicamentos.isFlagEventoAmbiguedadOFF()){
				resultado.addAll(buscarMedPorCaract(id));}
				
			if (hallado)
				managerDB.executeScript_Void("INSERT INTO `"+Testing.esquema+"`.`reportes`VALUES ('"+ new SimpleDateFormat("yyyyMMdd_HHmmss.SSS").format(Calendar.getInstance().getTime())+"','Se hallo el equivalente en ANMAT para el medicamento idManfar=" + id+"')");
			else
				if (BuscadorMedicamentos.isFlagEventoAmbiguedadOFF())
					managerDB.executeScript_Void("INSERT INTO `"+Testing.esquema+"`.`reportes`VALUES ('"+ new SimpleDateFormat("yyyyMMdd_HHmmss.SSS").format(Calendar.getInstance().getTime())+"','No se hallo el equivalente en ANMAT para el medicamento idManfar=" + id+"')");
				else
					managerDB.executeScript_Void("INSERT INTO `"+Testing.esquema+"`.`reportes`VALUES ('"+ new SimpleDateFormat("yyyyMMdd_HHmmss.SSS").format(Calendar.getInstance().getTime())+"','Se hallaron varios  equivalentes en ANMAT para el medicamento idManfar=" + id+"')");

		}
		
		return resultado;
	}
	private static Set<String> buscarMedRefDirecto(String id){
	
		Set<String> resultado = new HashSet<String>();
		Set<String> listaMedicamentos = new HashSet<String>();
		listaMedicamentos.addAll(managerDB.executeScript_Query(Script.getMedRefDirecta(id),"nombre"));
		
		if (!listaMedicamentos.isEmpty())
			hallado=true;
			
		for (String medicamentos : listaMedicamentos) {
			resultado.addAll(managerDB.executeScript_Query(Script.getDrogas(medicamentos),"nombre"));
		}
		
		return resultado;
	
	}
	private static Set<String> buscarMedPorCaract(String id){

		List<String> hallados = new ArrayList<String>();
		Set<String> resultado = new HashSet<String>();
		
		String id_FormaFa="%", unidad="%";
		
		// Busco el id del medicamento y entre todos los sinonimos hallo su formaFarmaceutica
		
		// Conjunto de formasFarmaceuticas, (algunas son del aleman, otras null)
		Set<String> formas = new HashSet<String>();
		formas.addAll(managerDB.executeScript_Query("SELECT formaFarmaceutica as formFarm  FROM "+Testing.esquema+".medicamentos_manfar WHERE id_manfar = '"+id+"';","formFarm"));
		
		// Transforma las descripciones a id, sacando las invalidas o vacias
		List<String> formasFiltradas= new ArrayList<String>();
		for (String string : formas) {
			String aux = FormaFarmaceutica.descrip_to_idFormFarmBasic(string);
			if (!aux.equals(""))
				formasFiltradas.add(aux);
		}

		// Guardo el id de la formaFarmaceutica
		if (!formasFiltradas.isEmpty())
			id_FormaFa=formasFiltradas.get(0);
		
		
		// UNIDADES
		
		// Conjunto de formasFarmaceuticas, (algunas son del aleman, otras null)
		Set<String> unidades = new HashSet<String>();
		unidades.addAll(managerDB.executeScript_Query("SELECT unidades  FROM "+Testing.esquema+".medicamentos_manfar WHERE id_manfar = '"+id+"';","unidades"));
		
		// Transforma las descripciones a id, sacando las invalidas o vacias
		List<String> unidadesFiltradas= new ArrayList<String>();
		for (String string : unidades) {
			if (!string.equals("0"))
				unidadesFiltradas.add(string);
		}

		// Guardo el id de la formaFarmaceutica
		if (!unidadesFiltradas.isEmpty())
			unidad=unidadesFiltradas.get(0);
		
		Set<String> listaMedicamentos = new HashSet<String>();
		listaMedicamentos.addAll(managerDB.executeScript_Query(Script.getMedCarateristicasWithNombre(id, id_FormaFa, unidad),"nombre"));		
		
		if(listaMedicamentos.isEmpty()){
			listaMedicamentos.addAll(managerDB.executeScript_Query(Script.getMedCarateristicasLikeNombre(id, id_FormaFa, unidad),"nombre"));		
		}
		
		if (!listaMedicamentos.isEmpty())
			hallado=true;
		
		for (String medicamentos : listaMedicamentos) {
			resultado.addAll(managerDB.executeScript_Query(Script.getDrogas(medicamentos),"nombre"));
		}
		
		//-------------------------- DECISION *--------------------------------------------
		
		if (hallados.isEmpty())
		{
				managerDB.executeScript_Void("INSERT INTO "+Testing.esquema+".`cache_busquedas`VALUES ('"+id+"','null');");
		}
		else
			for (String string : hallados) {
			 if (string .length() <250)
				managerDB.executeScript_Void("INSERT INTO "+Testing.esquema+".`cache_busquedas`VALUES ('"+id+"','"+string+"');");
			}
		
		
		
		
		if (hallados.size()==1)
		{
			resultado.clear();
			resultado.addAll(managerDB.executeScript_Query(	
					"	SELECT concat(snom.iddrogas_Snomed,'_',anm.droga_cantidad) as nombre "
					+ 	"FROM  "
					+ 	""+Testing.esquema+".drogas_anmat as anm, "
					+ 	""+Testing.esquema+".droga_formasimplificada as anc,"
					+ 	""+Testing.esquema+".drogas_snomed as snom "
					+ "	where		"
					+ "		anm.nro_certificado_anmat="+hallados.get(0).split("_")[0] +" and "
					+ "		anm.indexCertif="+hallados.get(0).split("_")[1] +" and "
					+ "		(snom.iddrogas_Anmat=anm.droga_nombre or (anm.droga_nombre=anc.DrogaOrigen and anc.idDrogaAncestro=snom.iddrogas_Snomed))"
					+ "","nombre"));

		}
		else{
		// Puede que haya o muchos o ninguno
			if (hallados.size()>1)
			{
					// Dado que hay varios verifico si el profesional ya cargo una sugenrencia de cual medicamento se trata
					List<String> sugerencia = new ArrayList<String>();
					sugerencia.addAll(managerDB.executeScript_Query("SELECT concat(id_certif_anmat,'_',id_index_anmat) as med FROM "+Testing.esquema+".medicamentos_manfar_sugerencias WHERE id_medico='"+Testing.patientBankData.getIdDoctor()+"' && id_manfar='"+id+"';", "med"));

					if (sugerencia.isEmpty())
					{
						Testing.patientBankData=null;
						Testing.bufferAmbiguedades= new EventoAmbiguedad[hallados.size()];
						for (int i=0;i<hallados.size();i++) 
						{
							Testing.bufferAmbiguedades[i]= new EventoAmbiguedad();
							Testing.bufferAmbiguedades[i].setData(id, hallados.get(i).split("_")[2], hallados.get(i).split("_")[3], hallados.get(i).split("_")[0]+"_"+hallados.get(i).split("_")[1]  );
						}
					}
					else
					{
						hallados.clear();
						hallados.addAll(managerDB.executeScript_Query(	
								"	SELECT concat(snom.iddrogas_Snomed,'_',anm.droga_cantidad) as nombre "
								+ 	"FROM  "
								+ 	""+Testing.esquema+".drogas_anmat as anm, "
								+ 	""+Testing.esquema+".droga_formasimplificada as anc,"
								+ 	""+Testing.esquema+".drogas_snomed as snom "
								+ "	where		"
								+ "		anm.nro_certificado_anmat="+sugerencia.get(0).split("_")[0] +" and "
								+ "		anm.indexCertif="+sugerencia.get(0).split("_")[1] +" and "
								+ "		(snom.iddrogas_Anmat=anm.droga_nombre or (anm.droga_nombre=anc.DrogaOrigen and anc.idDrogaAncestro=snom.iddrogas_Snomed))"
								+ "","nombre"));
						
					}
					
			}
							
		
		}	
		return resultado;
	}
	private static Set<String> buscarCacheDeBusquedas(String id){
		List<String> hallados = managerDB.executeScript_Query("SELECT data_anmat FROM `"+Testing.esquema+"`.cache_busquedas WHERE idmanfar='"+id+"';", "data_anmat");
		
		Set<String> resultado = new HashSet<String>();
			
		if (!hallados.isEmpty())
			hallado=true;
			
		if (hallado && !hallados.get(0).equals("null"))
		{ 
			if (hallados.size()==1)
			{
				resultado.addAll(managerDB.executeScript_Query(	
						"	SELECT concat(snom.iddrogas_Snomed,'_',anm.droga_cantidad) as nombre "
								+ 	"FROM  "
								+ 	""+Testing.esquema+".drogas_anmat as anm, "
								+ 	""+Testing.esquema+".droga_formasimplificada as anc"
								+ 	","+Testing.esquema+".drogas_snomed as snom "
								+ "	where		"
								+ "		anm.nro_certificado_anmat="+hallados.get(0).split("_")[0] +" and "
								+ "		anm.indexCertif="+hallados.get(0).split("_")[1] +" "
								+ "	and	(snom.iddrogas_Anmat=anm.droga_nombre or (anm.droga_nombre=anc.DrogaOrigen and anc.idDrogaAncestro=snom.iddrogas_Snomed))"
								+ "","nombre"));

			}
			else{
				// Puede que haya o muchos o ninguno
				if (hallados.size()>1)
				{
					// Dado que hay varios verifico si el profesional ya cargo una sugenrencia de cual medicamento se trata
					List<String> sugerencia = new ArrayList<String>();
					sugerencia.addAll(managerDB.executeScript_Query("SELECT concat(id_certif_anmat,'_',id_index_anmat) as med FROM "+Testing.esquema+".medicamentos_manfar_sugerencias WHERE id_medico='"+Testing.patientBankData.getIdDoctor()+"' && id_manfar='"+id+"';", "med"));

					if (sugerencia.isEmpty())
					{
						Testing.patientBankData=null;
						Testing.bufferAmbiguedades= new EventoAmbiguedad[hallados.size()];
						for (int i=0;i<hallados.size();i++) 
						{
							Testing.bufferAmbiguedades[i]= new EventoAmbiguedad();
							Testing.bufferAmbiguedades[i].setData(id, hallados.get(i).split("_")[2], hallados.get(i).split("_")[3], hallados.get(i).split("_")[0]+"_"+hallados.get(i).split("_")[1]  );
						}
						resultado.clear();
					}
					else
					{
						resultado.clear();
						resultado.addAll(managerDB.executeScript_Query(	
								"	SELECT concat(snom.iddrogas_Snomed,'_',anm.droga_cantidad) as nombre "
										+ 	"FROM  "
										+ 	""+Testing.esquema+".drogas_anmat as anm, "
										+ 	""+Testing.esquema+".droga_formasimplificada as anc"
										+ 	", "+Testing.esquema+".drogas_snomed as snom "
										+ "	where		"
										+ "		anm.nro_certificado_anmat="+sugerencia.get(0).split("_")[0] +" and "
										+ "		anm.indexCertif="+sugerencia.get(0).split("_")[1] +" s"
										+ "	and	(snom.iddrogas_Anmat=anm.droga_nombre or (anm.droga_nombre=anc.DrogaOrigen and anc.idDrogaAncestro=snom.iddrogas_Snomed))"
										+ "","nombre"));

					}

				}

			}
		}
		return resultado;
		
		
	}
	
}