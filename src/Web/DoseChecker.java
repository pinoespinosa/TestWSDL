package Web;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;

import xml.EventoDosificacion;
import xml.EventoInteraccionMedicamentosa;
import ClasesBD.Droga;

public class DoseChecker {

	public static final float INVALID_VALUE=-1;
	public static final int UMBRAL_DOSIS_SEGURA=2;
	
	public static List<EventoDosificacion> isDoseValid( Hashtable<String, Integer> hashIdMedicamentos_Cantidad, int edad, int peso, String sexo){

		List<EventoDosificacion> resultado = new ArrayList<EventoDosificacion>();
		HashMap<String, Double> drogasyDosis = new HashMap<String, Double>(); 
		
		// Completo la hash drogasyDosis con las drogas de cada medicamento
		for (String idMedicamento : hashIdMedicamentos_Cantidad.keySet()) 
		{

			// Obtengo los datos de un medicamento
			HashSet<String> listDrogasDataInMedicamento= new HashSet<String>(Testing.patientBankData.getDrogaEnMedicamentoCodificada(idMedicamento));
			
			if (BuscadorMedicamentos.isFlagEventoAmbiguedadOFF())
			{
				// Descomopongo la data de la droga y la dosis y la agrego en la hash
				for (String droga_dosis : listDrogasDataInMedicamento) 
				{
					String idDroga = BuscadorMedicamentos.getDroga(droga_dosis);
					double dosis = BuscadorMedicamentos.getDosis(droga_dosis)*hashIdMedicamentos_Cantidad.get(idMedicamento);
				
					if (drogasyDosis.containsKey(idDroga))	{
						Double storedDosis = drogasyDosis.get(idDroga);
						drogasyDosis.put(idDroga, new Double(storedDosis+dosis));}
					else
						drogasyDosis.put(idDroga, new Double(dosis) );
				}
			}
			else
				return makeEventAmbiguedad();
		}

		if (!BuscadorMedicamentos.isFlagEventoAmbiguedadOFF())
			return makeEventAmbiguedad();

			
		// Reviso de a una las drogas que consume el paciente para ver si alguna dosis es riesgosa
		for (String idDroga : drogasyDosis.keySet()) {
			
			// Metodo de calculo Nº 1	-------------------------------------------------------------------------------------------------
			double form_divDosisPeso =	drogasyDosis.get(idDroga)/peso;	
			List<String> listInt_FORMULA1 = managerDB.executeScript_Query("	select concat(consecuencia,'_',explicacion )as res "
					+ "						from "+Testing.esquema+".dosis as dosis"
					+ "						where 	"
					+ "							dosis.idDroga ='"+idDroga+"' and "
					+ "							(dosis.edad_minima<" + edad + " and dosis.edad_maxima>" + edad + ") and"
					+ "							(dosis.peso_minimo<" + peso + " and dosis.peso_maximo>" + peso + ") and"
					+ "							(dosis.sexo='" + sexo + "' or dosis.sexo='A') and"
					+ "							(dosis.formula='div(dosis,peso)' and (dosis.cantidad_minima <= "+form_divDosisPeso+" and dosis.cantidad_maxima > "+form_divDosisPeso+"))","res");
			
			
			// Si encontre algo creo un evento reportandolo y sino miro la dosis estimadas
			if (!listInt_FORMULA1.isEmpty())
			{
				EventoDosificacion a = new EventoDosificacion();
				float dosisMaxima = Float.parseFloat(Droga.getMaximaDosisSegura(idDroga))*peso;
				a.setData( idDroga, Droga.getName(idDroga),drogasyDosis.get(idDroga)+"",dosisMaxima+"",listInt_FORMULA1.get(0).split("_")[1],listInt_FORMULA1.get(0).split("_")[0],"DosisMaxima");
				resultado.add(a);
			}
			else
				resultado.addAll(isEstimatedValidDoses(idDroga, drogasyDosis.get(idDroga).floatValue() ,edad, peso, sexo));
			
		}
		
			return resultado;

	}
	public static List<EventoDosificacion> isEstimatedValidDoses (String id_droga, float dosis, int edad, int peso, String sexo){
	
		List<EventoDosificacion> resultado = new ArrayList<EventoDosificacion>();
		int rangoEdad = getRangeAge(edad);
		float desviacion=-1, promedioPond=1;


		// Verifico si tengo almacenado la dosis para un perfil de este tipo
		promedioPond = getPerfilatedDoseHistorical(id_droga, dosis, rangoEdad, sexo, peso);

		// Si no lo pude levantar del historial
		if (promedioPond==DoseChecker.INVALID_VALUE){

			List<String> listaDosificacionesHistoricas = managerDB.executeScript_Query("SELECT concat(cantidad,'_',peso,'_',edad,'_',sexo) as data FROM "+Testing.esquema+".dosis_aplicada WHERE id_droga='"+id_droga+"';", "data");

			if (listaDosificacionesHistoricas.size()<3)
			{	// No hay suficientes registros como para evaluar, se alerta.  ----------------- Se inserta por desconocimiento --------------
				managerDB.executeScript_Void("INSERT INTO `"+Testing.esquema+"`.`dosis_aplicada` VALUES('" + new SimpleDateFormat("yyyyMMdd_HHmmss.SSS").format(Calendar.getInstance().getTime())+"','"+id_droga+"','"+dosis+"','" + peso + "','" + edad + "','" + sexo + "');");
			}
			else
			{

				float acumuladaFSimulitud=0, acumuladaDosisAjustadas=0;
				
				for (String dataHist : listaDosificacionesHistoricas) 
				{
					// Parseo los valores dentro del dataHist
					float dosisHist = Float.valueOf(dataHist.split("_")[0]);
					int pesoHist = Integer.valueOf(dataHist.split("_")[1]);
					int rangoEdadHist=getRangeAge(Integer.valueOf(dataHist.split("_")[2]));
					String sexoHist = dataHist.split("_")[3];
					
					// Calculo las similitudes
					float simEdad= 1 /(1+Math.abs(rangoEdad - rangoEdadHist));
					float simPeso= (float) ((1 + Math.abs(pesoHist-peso))/1);
					float simSexo;	if (sexo.equals(sexoHist))	simSexo=1;	else 	simSexo=(float) 0.9;

					float simTotal = (float) Math.pow(simPeso*simEdad*simSexo,2);

					// Acumulo en las sumatorias
					acumuladaFSimulitud=acumuladaFSimulitud+simTotal;
					acumuladaDosisAjustadas=acumuladaDosisAjustadas+(simTotal*dosisHist);

				}
				
				promedioPond = acumuladaDosisAjustadas/ acumuladaFSimulitud;
			}
		}

		if (promedioPond!= DoseChecker.INVALID_VALUE)
			desviacion = Math.max(promedioPond, dosis)/Math.min(promedioPond,dosis);

		// TODO Ajustar el valor de la desviacion en base a encuestas a los medicos
		if (desviacion > DoseChecker.UMBRAL_DOSIS_SEGURA){
			
			EventoDosificacion a = new EventoDosificacion();
			a.setData( id_droga, Droga.getName(id_droga),dosis+"",promedioPond+"","Esta es una alarma que se genero debido a que la dosificación discrepa en relación a lo recetado historicamente","desconocida","DosisMaximaEstimada");
			resultado.add(a);
		}else
			{
			//  ----------------- Se inserta porque paso todos los controles --------------
			managerDB.executeScript_Void("INSERT INTO `"+Testing.esquema+"`.`dosis_aplicada` VALUES('"+ new SimpleDateFormat("yyyyMMdd_HHmmss.SSS").format(Calendar.getInstance().getTime())+"','"+id_droga+"','"+dosis+"','" + peso + "','" + edad + "','" + sexo + "');");
			}




		return resultado;
	}
	
	private static float getPerfilatedDoseHistorical(String id_droga, float dosis, int rangoEdad, String sexo, int peso ){
			 
		List<String> listaDosificacionesPerfiladas = managerDB.executeScript_Query("SELECT concat(timestamp,'_',dosis) as dosis_perfil FROM "+Testing.esquema+".cache_dosis_aplicadas_pocesadas WHERE id_droga='"+id_droga+"' AND sexo='"+sexo+"' AND rango_edad='"+rangoEdad+"' AND rango_peso='"+getRangeWeigth(peso)+"';", "dosis_perfil");
		String aa = "SELECT concat(timestamp,'_',dosis) as dosis_perfil FROM "+Testing.esquema+".cache_dosis_aplicadas_pocesadas WHERE id_droga='"+id_droga+"' AND sexo='"+sexo+"' AND rango_edad='"+rangoEdad+"' AND rango_peso='"+getRangeWeigth(peso)+"';"; 
		float promedioPond;
		if (!listaDosificacionesPerfiladas.isEmpty())
		{
			String date = listaDosificacionesPerfiladas.get(0).split("_")[0];
			promedioPond = Float.valueOf(listaDosificacionesPerfiladas.get(0).split("_")[2]);
			
			int diffInDays = Integer.MAX_VALUE;
			try { diffInDays = (int)( (System.currentTimeMillis() - new SimpleDateFormat("yyyyMMdd").parse(date).getTime()) / (1000 * 60 * 60 * 24) ); } catch (ParseException e) { e.printStackTrace();}
			
			if(diffInDays<30)
				return promedioPond;
		}
		return DoseChecker.INVALID_VALUE;
	}
	private static int getRangeAge(int edad){
		if(edad<18)
			if (edad<12)
				return 0;
			else
				return 1;
		else
			if (edad<60)
				return 2;
			else
				return 3;
		
	}
	private static int getRangeWeigth(int peso){
		return Math.round(peso/10);
		
	}
	
	private static List<EventoDosificacion> makeEventAmbiguedad(){
		List<EventoDosificacion> auxi = new ArrayList<EventoDosificacion>();
		EventoDosificacion a = new EventoDosificacion();
		a.setAmbiguedad(Testing.bufferAmbiguedades);
		Testing.bufferAmbiguedades=null;
		auxi.add(a);
		return auxi;
	}	
}
