package ClasesBD;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import Web.Testing;
import Web.managerDB;

public class Medicamento {
	
	public static String getName(String id){
		String nombreDrog="";
		List<String> nombreList = managerDB.executeScript_Query("SELECT nombre FROM "+Testing.esquema+".medicamentos_manfar where id_manfar='"+id+"';", "nombre");
		if(!nombreList.isEmpty())
			nombreDrog=nombreList.get(0);
		return nombreDrog;
	}

	/**
	 *  Retorna el idSnomed de la forma farmaceutica simplificada del medicamento
	 * @param idManfar
	 * @return
	 */
	public static String generateFormaFarmaceutica(String idManfar){
		String id_FormaFa="%";

		// Busco el id del medicamento y entre todos los sinonimos hallo su formaFarmaceutica

		// Conjunto de formasFarmaceuticas, (algunas son del aleman, otras null)
		Set<String> formas = new HashSet<String>();
		formas.addAll(managerDB.executeScript_Query("SELECT formaFarmaceutica as formFarm  FROM "+Testing.esquema+".medicamentos_manfar WHERE id_manfar = '"+idManfar+"';","formFarm"));

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
		
		return id_FormaFa;
	}

	/**
	 * Retorna la cantidad de unidades o el simbolo porcentaje
	 * @param idManfar
	 * @return
	 */
	public static String generateUnidades (String idManfar){
		String 	unidad="%";

		// Conjunto de formasFarmaceuticas, (algunas son del aleman, otras null)
		Set<String> unidades = new HashSet<String>();
		unidades.addAll(managerDB.executeScript_Query("SELECT unidades  FROM "+Testing.esquema+".medicamentos_manfar WHERE id_manfar = '"+idManfar+"';","unidades"));

		// Transforma las descripciones a id, sacando las invalidas o vacias
		List<String> unidadesFiltradas= new ArrayList<String>();
		for (String string : unidades) {
			if (!string.equals("0"))
				unidadesFiltradas.add(string);
		}

		// Guardo el id de la formaFarmaceutica
		if (!unidadesFiltradas.isEmpty())
			unidad=unidadesFiltradas.get(0);
		
		return unidad;
	}

}
