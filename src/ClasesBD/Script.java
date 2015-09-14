package ClasesBD;

import java.util.List;

import Web.Testing;
import Web.managerDB;

public class Script {
	
	
	/**
	 * Busca un las referencias directas para un id retornando anm.droga_nombre,'_',anm.droga_cantidad
	 * @param id
	 * @return
	 */
	public static String getMedRefDirecta(String id){
		return 	"select concat(anm.nro_certificado_anmat,'_',anm.indexCertif) as nombre "
				+ "from  "
				+ "		"+Testing.esquema+".drogas_anmat as anm "
				+ "where "
				+ "		anm.idManfar like "+id;

	};

	
	public static String getMedCarateristicasWithNombre(String id, String id_FormaFa, String unidad){
		return 	
				  " select concat(anm.nro_certificado_anmat,'_',anm.indexCertif, '_', man.nombre,'_',anm.medicamento_nombre,' ', anm.formaFarmaceutica,' ',anm.presentacion_texto) as nombre  "
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
		String s= 	
				  " select concat(anm.nro_certificado_anmat,'_',anm.indexCertif, '_', man.nombre,'_',anm.medicamento_nombre,' ', anm.formaFarmaceutica,' ',anm.presentacion_texto) as nombre  "
				+ "	FROM 	"
				  	+Testing.esquema+".medicamentos_manfar as man, "
					+Testing.esquema+".calificadores_snomed as form,"
					+Testing.esquema+".drogas_anmat as anm	"
				+ "	WHERE "
					+ " ((man.nombre like concat(anm.medicamento_nombre,'%')) or (anm.medicamento_nombre like concat(man.nombre,'%')) ) and "
					+ "	form.detalle_calificador=anm.formaFarmaceutica and "
					+ " (form.idcalificador_Snomed like '"+id_FormaFa+"' or form.ancestro_calificador like '"+id_FormaFa+"') and "
					+ "	(anm.presentacion_unidades like '"+unidad+"' or  anm.presentacion_unidades=0 ) and "
					+ "	man.id_manfar="+id
				+ "	GROUP BY anm.nro_certificado_anmat,anm.indexCertif,man.nombre,anm.medicamento_nombre ";
	//	System.out.println(s);
	return s;
	};
	
	/**
	 * 
	 */
	public static String getDrogas(String nro_certificado_anmat_anm_indexCertif){
		return 	"select * "
				+ "from ("
						+ "select concat(sno.iddrogas_Snomed,'_',anm.droga_cantidad) as nombre "
						+ "from  "
								+Testing.esquema+".drogas_snomed as sno, "
								+Testing.esquema+".drogas_anmat as anm	"
						+ "where "
						+ "		sno.iddrogas_Anmat=anm.droga_nombre "
						+ " 	and anm.nro_certificado_anmat = " + nro_certificado_anmat_anm_indexCertif.split("_")[0] 
						+ "     and anm.indexCertif = " + nro_certificado_anmat_anm_indexCertif.split("_")[1]
						+ " union "
						+ ""
						+ "select concat(sno.iddrogas_Snomed,'_',anm.droga_cantidad) as nombre "
						+ "from  "
								+Testing.esquema+".drogas_anmat as anm,	"
								+Testing.esquema+".drogas_snomed as sno, "
								+Testing.esquema+".droga_formasimplificada as anc "
						+ "where "
						+ " 	anm.nro_certificado_anmat = " + nro_certificado_anmat_anm_indexCertif.split("_")[0] 
						+ "     and anm.indexCertif = " + nro_certificado_anmat_anm_indexCertif.split("_")[1]
						+ "		and anm.droga_nombre=anc.DrogaOrigen and anc.idDrogaAncestro=sno.iddrogas_Snomed ) as dro"
						+ ";";

	};
	
	public static String getDrogasCantidad(String nro_certificado_anmat_anm_indexCertif){
		List<String> aux = managerDB.executeScript_Query("select concat(anm.droga_nombre,'_',anm.droga_cantidad) as nombre "
						+ "from  "
								+Testing.esquema+".drogas_anmat as anm	"
						+ "where "
						+ " 	anm.nro_certificado_anmat = " + nro_certificado_anmat_anm_indexCertif.split("_")[0] 
						+ "     and anm.indexCertif = " + nro_certificado_anmat_anm_indexCertif.split("_")[1], "nombre");
	//	for (String string : aux) {
	//		System.out.println(string);
	//	}

		return aux.size()+"";
	};
	
	

}


