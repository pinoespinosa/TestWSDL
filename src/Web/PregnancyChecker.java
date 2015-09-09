package Web;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import xml.EventoDosificacion;
import xml.EventoInteraccionCondicion;
import ClasesBD.Droga;

public class PregnancyChecker {

	
	public static List<EventoInteraccionCondicion> isContraindicatedWithCondition (Collection<String> listDrog, String condition){
	//	String logText="";
		List<EventoInteraccionCondicion> resultado = new ArrayList<EventoInteraccionCondicion>();	
		List<String> interaccionesCond;
		boolean existInteraction=false;
		for (String drog : listDrog) 
		{	
			String droga=drog.split("_")[0];
			
			// Verifico si esta contraindicada para la condicion
			interaccionesCond = managerDB.executeScript_Query(""
					+ " select 		nivel_certeza "
					+ " from 	"+	Testing.esquema+".condition_interactions as cond join "
					+				Testing.esquema+".condition_snomed as snom on cond.id_condition=snom.idcontitions_snomed"
					+ " where "
					+ "				snom.idconditions_word='"+condition+"' and "
					+ "				cond.id_droga='"+droga+"';", "nivel_certeza");
		
			
			// Verifivo si esta contraindicada el padre de la droga para la condicion 
			if (interaccionesCond.isEmpty())
			{		
				interaccionesCond = managerDB.executeScript_Query(""
					+ " select 		nivel_certeza "
					+ " from "+		Testing.esquema+".condition_interactions as cond join "																								
					+ 			  	Testing.esquema+".condition_snomed as snom on cond.id_condition=snom.idcontitions_snomed,"
					+				Testing.esquema+".droga_formasimplificada as anc"
										
					+ " where "
					+ "				snom.idconditions_word='"+condition+"' and "
					+ "				cond.id_droga=anc.idDrogaOrigen and idDrogaAncestro='"+droga+"';", "nivel_certeza");
		
			}
	
			
			if (!interaccionesCond.isEmpty())
			{	
				existInteraction=true;
				EventoInteraccionCondicion aux = new EventoInteraccionCondicion();
				aux.setData(droga, Droga.getName(droga), condition, "");
				resultado.add(aux);
			}
			
			interaccionesCond.clear();
		}
		
		return resultado;
	}
	
	public static EventoInteraccionCondicion[] makeEventAmbiguedad(){
	
		List<EventoInteraccionCondicion> auxi = new ArrayList<EventoInteraccionCondicion>();
		EventoInteraccionCondicion a = new EventoInteraccionCondicion();
		a.setAmbiguedad(Testing.bufferAmbiguedades);
		Testing.bufferAmbiguedades=null;
		auxi.add(a);
		return auxi.toArray(new EventoInteraccionCondicion[auxi.size()]);
	}	
	
}
