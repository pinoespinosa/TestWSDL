package Web;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Hashtable;
import java.util.List;

import xml.EventoAmbiguedad;
import xml.EventoDosificacion;
import xml.EventoInteraccionCondicion;
import xml.EventoInteraccionMedicamentosa;

public class Testing {

	public static Paciente patientBankData;
	public static String esquema= "pruebainstall2";
	public static boolean dirty=false;
	public static EventoAmbiguedad[] bufferAmbiguedades=null;
	public static boolean TESTING=true;


	private void updatePaciente( String idMedico, int peso, int edad, String sexo, String[] medicaciones, String[] conditions){
		Hashtable <String,List<String>> patientData = new Hashtable< String, List<String>> ();

		List<String> medList = new ArrayList<String>();
		for (String med : medicaciones) {
			medList.add(med);
		}
		patientData.put("id",medList);

		List<String> condList = new ArrayList<String>();
		for (String cond : conditions) {
			condList.add(cond);
		}
		List<String> med = new ArrayList<String>();
		med.add(idMedico);
		
		patientData.put("conditions",condList);
		patientData.put("idDoctor",med);

		Paciente nuevo = new Paciente(edad,peso,sexo,patientData);

		if (patientBankData==null || !patientBankData.equals(nuevo))
		{
		
			if (patientBankData!=null)			
				patientBankData.clear();
			bufferAmbiguedades=null;

			patientBankData=nuevo;
		
		
		}
	}

	public EventoInteraccionMedicamentosa[] getInteractionsWithMedicaments(String idMedico, int peso, int edad, String sexo, String[] medicaciones, String[] conditions){
		updatePaciente(idMedico, peso, edad, sexo, medicaciones, conditions);

		List<EventoInteraccionMedicamentosa> pino = InteractionChecker.existInteractionConExcepciones(patientBankData.getMedications().keySet());

		EventoInteraccionMedicamentosa[] aux = new EventoInteraccionMedicamentosa[pino.size()];
		
		int i=0;
		for (EventoInteraccionMedicamentosa string : pino) {
			aux[i]=pino.get(i);
			i++;
		}

		return aux;		

	}
	public EventoDosificacion[] getDoseEvents(String idMedico, int peso, int edad, String sexo, String[] medicaciones, String[] conditions){

		
		updatePaciente(idMedico, peso, edad, sexo, medicaciones, conditions);

		
		List<EventoDosificacion> pino = DoseChecker.isDoseValid(patientBankData.getMedications(), edad, peso, sexo);

		EventoDosificacion[] aux = new EventoDosificacion[pino.size()];

		int i=0;
		for (EventoDosificacion string : pino) {
			aux[i]=pino.get(i);
			i++;
		}

		return aux;	
	}
	public EventoInteraccionCondicion[] getInteractionsWithProblems(String idMedico, int peso, int edad, String sexo, String[] medicaciones, String[] conditions){

		long init= System.currentTimeMillis(); 
		
		int i =0;
		
		updatePaciente(idMedico, peso, edad, sexo, medicaciones, conditions);
		List<EventoInteraccionCondicion> resultado=new ArrayList<EventoInteraccionCondicion>();
		for (String condition : patientBankData.getEspetialConditions()) {
			for (String med : patientBankData.getMedications().keySet()) {
				if (BuscadorMedicamentos.isFlagEventoAmbiguedadOFF())
					resultado.addAll(PregnancyChecker.isContraindicatedWithCondition(patientBankData.getDrogaEnMedicamentoCodificada(med),condition));
				else
				{					
					long resta = System.currentTimeMillis()-init;
				//	System.out.println(resta + " " + medicaciones[0]);
					if (TESTING){
							setSugerencia( (System.currentTimeMillis()+ "").substring(11,13) , bufferAmbiguedades[0].getIdMedicamento(), bufferAmbiguedades[0].getCodigo());
						}
					EventoInteraccionCondicion[] aux = PregnancyChecker.makeEventAmbiguedad();
					return aux;
					// Se encontro un evento de ambiguedad, retorno lo del evento y se corta lo q venia haciendo
	
				}
			}
			if (!BuscadorMedicamentos.isFlagEventoAmbiguedadOFF()){
				
				long resta = System.currentTimeMillis()-init;
		//		System.out.println(resta + " " + medicaciones[0]);
				if (TESTING){
					setSugerencia( System.currentTimeMillis()+ "" , bufferAmbiguedades[0].getIdMedicamento(), bufferAmbiguedades[0].getCodigo());
					}
				EventoInteraccionCondicion[] aux = PregnancyChecker.makeEventAmbiguedad();
				return aux;
		}
		}

		long resta = System.currentTimeMillis()-init;
	//	System.out.println(resta + " " + medicaciones[0]+" " +i);
		i++;
		return resultado.toArray(new EventoInteraccionCondicion[resultado.size()]);	
	}
	public void setSugerencia(String idMedico, String idManfar, String codigo){

		patientBankData=null;
		
		// Agrego la sugerencia
		managerDB.executeScript_Void("INSERT INTO `"+esquema+"`.`medicamentos_manfar_sugerencias` VALUES ('"+idManfar+"','"+codigo.split("_")[0]+"','"+codigo.split("_")[1]+"','"+idMedico+"','" + new SimpleDateFormat("yyyyMMdd_HHmmss.SSS").format(Calendar.getInstance().getTime())+"');");

		// Chequeo si hay consenso
		List<String> sugerencia = managerDB.executeScript_Query(	
				"SELECT concat(id_certif_anmat,'_', id_index_anmat) as cod "
				+ "FROM `"+esquema+"`.`medicamentos_manfar_sugerencias` "
				+ "where 	"
				+ "id_manfar='"+idManfar+"' "
				+ "group by id_certif_anmat, id_index_anmat "
				+ "having count(*)>5 "
				+ "order by count(*) desc", "cod");

		if (!sugerencia.isEmpty())
		{
			List<String> updatesRows = managerDB.executeScript_Query(
					"SELECT concat(nro_certificado_anmat,'_', medicamento_nombre,'_',formaFarmaceutica,'_',presentacion_texto,'_',drogas_texto,'_',presentacion_unidades,'_',indexCertif) as script "
					+ "FROM "+esquema+".drogas_anmat "
					+ "where nro_certificado_anmat='"+sugerencia.get(0).split("_")[0]+"' and indexCertif='"+sugerencia.get(0).split("_")[1]+"';","script");

			for (String string : updatesRows) {
				managerDB.executeScript_Void("UPDATE `"+esquema+"`.`drogas_anmat` SET `idManfar`='"+idManfar+"' " + "WHERE `nro_certificado_anmat`='"+string.split("_")[0]+"' and `medicamento_nombre`='"+string.split("_")[1]+"' and `formaFarmaceutica`='"+string.split("_")[2]+"' and `presentacion_texto`='"+string.split("_")[3]+"' and `drogas_texto`='"+string.split("_")[4]+"' and `presentacion_unidades`='"+string.split("_")[5]+"' and `indexCertif`='"+string.split("_")[6]+"';");
			}
		}
		bufferAmbiguedades=null;
		//#		UPDATE `pruebainstall`.`drogas_anmat` SET `idManfar`='3608' 
	}

}



