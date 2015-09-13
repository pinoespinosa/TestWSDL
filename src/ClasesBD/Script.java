package ClasesBD;

import Web.Testing;

public class Script {
	
	
	/**
	 * Busca un las referencias directas para un id retornando anm.droga_nombre,'_',anm.droga_cantidad
	 * @param id
	 * @return
	 */
	public static String getMedRefDirecta(String id){
		return 	"select concat(anm.droga_nombre,'_',anm.droga_cantidad) as nombre "
				+ "from  "
				+ "		"+Testing.esquema+".drogas_anmat as anm "
				+ "where "
				+ "		anm.idManfar like "+id;

	};

	
	public static String getMedCarateristicasWithNombre(String id, String id_FormaFa, String unidad){
		return 	
				  " select concat(anm.droga_nombre,'_',anm.droga_cantidad) as nombre  "
				+ "	FROM 	"
				  	+Testing.esquema+".medicamentos_manfar as man, "
					+Testing.esquema+".calificadores_snomed as form,"
					+Testing.esquema+".drogas_anmat as anm,	"
					+Testing.esquema+".drogas_snomed as snom "
				+ "	WHERE "
					+ "	man.nombre = anm.medicamento_nombre and "
					+ "	form.detalle_calificador=anm.formaFarmaceutica and "
					+ " (form.idcalificador_Snomed like '"+id_FormaFa+"' or form.ancestro_calificador like '"+id_FormaFa+"') and "
					+ "	(anm.presentacion_unidades like '"+unidad+"' or  anm.presentacion_unidades=0 ) and "
					+ "	man.id_manfar="+id
				+ "	GROUP BY anm.nro_certificado_anmat,anm.indexCertif,man.nombre,anm.medicamento_nombre ";

	};
	
	public static String getMedCarateristicasLikeNombre(String id, String id_FormaFa, String unidad){
		return 	
				  " select concat(anm.droga_nombre,'_',anm.droga_cantidad) as nombre  "
				+ "	FROM 	"
				  	+Testing.esquema+".medicamentos_manfar as man, "
					+Testing.esquema+".calificadores_snomed as form,"
					+Testing.esquema+".drogas_anmat as anm,	"
					+Testing.esquema+".drogas_snomed as snom "
				+ "	WHERE "
					+ "	man.nombre = anm.medicamento_nombre and "
					+ "	form.detalle_calificador=anm.formaFarmaceutica and "
					+ " (form.idcalificador_Snomed like '"+id_FormaFa+"' or form.ancestro_calificador like '"+id_FormaFa+"') and "
					+ "	(anm.presentacion_unidades like '"+unidad+"' or  anm.presentacion_unidades=0 ) and "
					+ "	man.id_manfar="+id
				+ "	GROUP BY anm.nro_certificado_anmat,anm.indexCertif,man.nombre,anm.medicamento_nombre ";

	};
	
	/*
	
			hallados.addAll(managerDB.executeScript_Query("	SELECT DISTINCT CONCAT(anm.nro_certificado_anmat,'_',anm.indexCertif,'_',man.nombre,'_',anm.medicamento_nombre,' ', anm.formaFarmaceutica,' ',anm.presentacion_texto,' ',anm.drogas_texto )  as cant"
				+ "																										 "
				+ "														FROM 	"+Testing.esquema+".medicamentos_manfar as man, "
				+ "																"+Testing.esquema+".calificadores_snomed as form,"
				+ "																"+Testing.esquema+".droga_formasimplificada as anc,"
				+ "	 															"+Testing.esquema+".drogas_anmat as anm		"
				+ "																, "+Testing.esquema+".drogas_snomed as snom "
				+ "														WHERE 													 "
				+ "																man.nombre = anm.medicamento_nombre and 		"	// Se llaman igual
				+ "																form.detalle_calificador=anm.formaFarmaceutica and "
				+ "																(form.idcalificador_Snomed like '"+id_FormaFa+"' or form.ancestro_calificador like '"+id_FormaFa+"') and 	"
				+ "																(anm.presentacion_unidades like '"+unidad+"' or  anm.presentacion_unidades=0 )   and"
				+ "																man.id_manfar="+id+" "
						+ " and											(snom.iddrogas_Anmat=anm.droga_nombre or (anm.droga_nombre=anc.DrogaOrigen and anc.idDrogaAncestro=snom.iddrogas_Snomed)) "
				+ "																"
				+ "														GROUP BY anm.nro_certificado_anmat,anm.indexCertif,man.nombre,anm.medicamento_nombre	"
				+ "																"
				+ "														","cant"));		
		
	
	*/
	/**
	 * 
	 */
	public static String getDrogas(String droga_nombre_droga_cantidad){
		return 	"select * "
				+ "from ("
						+ "select concat(sno.iddrogas_Snomed,'_','"+droga_nombre_droga_cantidad.split("_")[1]+"') as nombre "
						+ "from  "+Testing.esquema+".drogas_snomed as sno "
						+ "where "
						+ "		sno.iddrogas_Anmat='"+droga_nombre_droga_cantidad.split("_")[0]+"'"
						+ ""
						+ " union "
						+ ""
						+ "select concat(sno.iddrogas_Snomed,'_','"+droga_nombre_droga_cantidad.split("_")[1]+"') as nombre "
						+ "from  "
						+ "		"+Testing.esquema+".drogas_anmat as anm, "+Testing.esquema+".drogas_snomed as sno, "
						+ "		"+Testing.esquema+".droga_formasimplificada as anc "
						+ "where "
						+ "		'"+droga_nombre_droga_cantidad.split("_")[0]+"'=anc.DrogaOrigen and anc.idDrogaAncestro=sno.iddrogas_Snomed ) as dro"
						+ ";";

	};
	
	

}


