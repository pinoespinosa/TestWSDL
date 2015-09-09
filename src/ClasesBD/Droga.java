package ClasesBD;

import java.util.List;

import Web.Testing;
import Web.managerDB;

public class Droga {
	
	public static String getName(String id){
		String nombreDrog="";
		List<String> nombreList = managerDB.executeScript_Query("SELECT iddrogas_Anmat FROM "+Testing.esquema+".drogas_snomed where iddrogas_Snomed='"+id+"';", "iddrogas_Anmat");
		if(!nombreList.isEmpty())
			nombreDrog=nombreList.get(0);
		return nombreDrog;
	}
	
	public static String getMaximaDosisSegura(String id){
		String dosisSegura="";
		List<String> dosisList = managerDB.executeScript_Query("SELECT cantidad_minima FROM "+Testing.esquema+".dosis where idDroga='"+id+"' order by gravedad limit 1;", "cantidad_minima");
		if(!dosisList.isEmpty())
			dosisSegura=dosisList.get(0);
		return dosisSegura;
	}

}
