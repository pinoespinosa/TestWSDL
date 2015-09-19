package Web;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import xml.EventoAmbiguedad;
import ClasesBD.Medicamento;
import ClasesBD.Script;


public class BuscadorMedicamentos {

	private static boolean hallado= false;
	private static boolean cacheNull= false;

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
		System.out.println("Medicamento: "+id + "-------------------------------------------------------------------");
		cacheNull=false;

		if (Integer.parseInt(id)>=0)
		{
			// Busco si esta referenciado directamente
			resultado.addAll(buscarMedRefDirecto(id));

			if (!hallado)
				resultado.addAll(buscarCacheDeBusquedas(id));

			if ( !hallado || cacheNull ){
				resultado.addAll(buscarMedPorCaract(id));}

			if (!hallado)
				System.out.println("No se hallo: "+id+"\n");

			if (hallado && !cacheNull && BuscadorMedicamentos.isFlagEventoAmbiguedadOFF() )
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
		List<String> listaMedicamentos = new ArrayList<String>();
		listaMedicamentos.addAll(managerDB.executeScript_Query(Script.getMedRefDirecta(id),"nombre"));


		if (!listaMedicamentos.isEmpty()){
			hallado=true;

			for (String medicamentos : listaMedicamentos)
				resultado.addAll(managerDB.executeScript_Query(Script.getDrogas(medicamentos),"nombre"));

		}


		if (!listaMedicamentos.isEmpty()){
			System.out.println("Ref Directa hallados: "+resultado.size());
			System.out.println();}

		return resultado;

	}
	private static Set<String> buscarMedPorCaract(String id){


		// ****************************** Busco el/los medicamento ******************************

		String 	id_FormaFa=Medicamento.generateFormaFarmaceutica(id),
				unidad=Medicamento.generateUnidades(id);

		List<String> hallados = new ArrayList<String>();
		
		// Busco los medicamentos y si no se hallaron relajo el nombre
		hallados.addAll(managerDB.executeScript_Query(Script.getMedCarateristicasWithNombre(id, id_FormaFa, unidad),"nombre"));		

		if(hallados.isEmpty()){
			hallados.addAll(managerDB.executeScript_Query(Script.getMedCarateristicasLikeNombre(id, id_FormaFa, unidad),"nombre"));		
		}		


		// ****************************** Lo guardo en el cache **********************************

		if (hallados.isEmpty()){
			managerDB.executeScript_Void("INSERT INTO "+Testing.esquema+".`cache_busquedas`VALUES ('"+id+"','null');");
		}
		else{
			int i=0;
			for (String string : hallados) {
				if (string .length() <2000)
					managerDB.executeScript_Void("INSERT INTO "+Testing.esquema+".`cache_busquedas`VALUES ('"+id+"','"+i+"','"+string+"');");
				i++;
			}
			hallado=true;
			System.out.println("Caracteristicas total:" + Script.getDrogasCantidad(hallados.get(0)));
		}
		

		// ****************************** Busco las drogas **********************************
		
		Set<String> resultado =hallarDrogas(hallados, id, "Carac");


		return resultado;
	}

	private static Set<String> buscarCacheDeBusquedas(String id) {
		List<String> hallados = managerDB.executeScript_Query( "SELECT data_anmat FROM `" + Testing.esquema	+ "`.cache_busquedas WHERE idmanfar='" + id + "';", "data_anmat");

		if (!hallados.isEmpty()) {
			hallado = true;
		}

		Set<String> resultado = null;
		if (hallado)
			if (hallados.get(0).equals("null")) {
				System.out.println("Cache: null\n");
				cacheNull = true;
			} else {
				resultado = hallarDrogas(hallados, id, "Cache");
			}

		return resultado;

	}
	
	public static void createEventoAmbiguedad(List<String> hallados, String id){
		
		Testing.patientBankData=null;
		Testing.bufferAmbiguedades= new EventoAmbiguedad[hallados.size()];
		for (int i=0;i<hallados.size();i++) 
		{
			Testing.bufferAmbiguedades[i] = new EventoAmbiguedad();			

			String descripcion_drogas = "[ ";

			List<String> info = managerDB.executeScript_Query(
					"	SELECT DISTINCT anm.drogas_texto as cant"
							+ "	FROM 	"
							+Testing.esquema+".medicamentos_manfar as man, "
							+Testing.esquema+".drogas_anmat as anm		"
							+ "	WHERE "
							+ "	man.nombre ="+id+" and "
							+ "	anm.nro_certificado_anmat="+hallados.get(i).split("_")[0] +" and "
							+ "	anm.indexCertif="+hallados.get(i).split("_")[1],"cant");

			for (String string : info) {
				descripcion_drogas+= string + " / ";
			}

			descripcion_drogas+="]";

			Testing.bufferAmbiguedades[i].setData(id, hallados.get(i).split("_")[2], hallados.get(i).split("_")[3] + " " + descripcion_drogas, hallados.get(i).split("_")[0]+"_"+hallados.get(i).split("_")[1]  );

		}
	}
	
	public static Set<String> hallarDrogas(List<String> hallados, String id, String contexto){
		
		Set<String> resultado = new HashSet<String>();
		
		if (hallados.size()==1){
			resultado.addAll(managerDB.executeScript_Query(Script.getDrogas(hallados.get(0)),"nombre"));
			System.out.println(contexto + " halladas: "+resultado.size()+"\n");
		}
		else{
			// Puede que haya o muchos o ninguno
			if (hallados.size()>1)
			{
				// Dado que hay varios verifico si el profesional ya cargo una sugenrencia de cual medicamento se trata
				List<String> sugerencia = new ArrayList<String>();
				sugerencia.addAll(managerDB.executeScript_Query("SELECT concat(id_certif_anmat,'_',id_index_anmat) as med FROM "+Testing.esquema+".medicamentos_manfar_sugerencias WHERE id_medico='"+Testing.patientBankData.getIdDoctor()+"' && id_manfar='"+id+"';", "med"));
				
				resultado.clear();
				if (sugerencia.isEmpty())
				{
					createEventoAmbiguedad(hallados,id);
				}
				else
				{
					hallados.clear();
					resultado.addAll(managerDB.executeScript_Query(Script.getDrogas(sugerencia.get(0)),"nombre"));	
					System.out.println(contexto + "halladas: "+resultado.size()+"\n");
				}

			}	
		}
		return resultado;
	}
	


}
