package ClasesBD;

import java.util.ArrayList;
import java.util.List;
import Web.managerDB;
import Web.Testing;

public class FormaFarmaceutica {


	public static String descrip_to_id(String descrip){

		List<String> resultado = new ArrayList<String>();
		if (!descrip.equals("null"))
			resultado.addAll(managerDB.executeScript_Query(""
					+ "select idcalificador_Snomed as id "
					+ "from  "
					+ "		"+Testing.esquema+".calificadores_snomed as sno "
					+ "where "
					+ "		sno.detalle_calificador = '"+descrip+"'","id"));

		if (resultado.isEmpty())
			return "";
		else
			return resultado.get(0);		
	}

	public static String descrip_to_idFormFarmBasic(String descrip){

		List<String> resultado = new ArrayList<String>();
		if (!descrip.equals("null"))
			resultado.addAll(managerDB.executeScript_Query(""
					+ "select ancestro_calificador as id "
					+ "from  "
					+ "		"+Testing.esquema+".calificadores_snomed as sno "
					+ "where "
					+ "		sno.ancestro_calificador <> '' and "
					+ "		sno.ancestro_calificador <> '0' and "
					+ "		sno.detalle_calificador = '"+descrip+"'","id"));

		if (resultado.isEmpty())
			return descrip_to_id(descrip);
		else
			return resultado.get(0);


	}
}
