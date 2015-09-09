package ClasesBD;

import java.util.List;

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

}
