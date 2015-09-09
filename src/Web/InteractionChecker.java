package Web;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import xml.EventoInteraccionMedicamentosa;
import ClasesBD.Droga;
import ClasesBD.Medicamento;



public class InteractionChecker {

	private List<String> list_id_medicamentos, list_nombre_medicamentos, list_nombre_drogas;

	private Set<String> list_id_drogas;

	public InteractionChecker (List<String> medicametos){

		list_id_medicamentos=new ArrayList<String>();

		System.out.println("La cantidad de medicamentos es: " + medicametos.size());

		for (String droga : medicametos) {
			list_id_medicamentos.add(droga.split("_")[0]);
		}

	}

	public void chequear(){

	}

	public List<String> getList_id_medicamentos() {
		return list_id_medicamentos;
	}

	public void setList_id_medicamentos(List<String> list_id_medicamentos) {
		this.list_id_medicamentos = list_id_medicamentos;
	}

	public List<String> getList_nombre_medicamentos() {
		return list_nombre_medicamentos;
	}

	public void setList_nombre_medicamentos(List<String> list_nombre_medicamentos) {
		this.list_nombre_medicamentos = list_nombre_medicamentos;
	}

	public List<String> getList_nombre_drogas() {
		return list_nombre_drogas;
	}

	public void setList_nombre_drogas(List<String> list_nombre_drogas) {
		this.list_nombre_drogas = list_nombre_drogas;
	}

	public Set<String> getList_id_drogas() {
		return list_id_drogas;
	}

	public void setList_id_drogas(Set<String> list_id_drogas) {
		this.list_id_drogas = list_id_drogas;
	}


	public static List<EventoInteraccionMedicamentosa> existInteractionConExcepciones(Collection<String> listMed){

		long millisStart = Calendar.getInstance().getTimeInMillis();
		
		List<EventoInteraccionMedicamentosa> resultados = new ArrayList<EventoInteraccionMedicamentosa>();

		// A partir de los ID´s de los medicamentos cargo una hash con las drogas que cada unmo contiene
		Hashtable<String, List<String> > listaMedicamentosDroga = new  Hashtable<String, List<String>>();
		for (String idMedicamento : listMed) {
			if (BuscadorMedicamentos.isFlagEventoAmbiguedadOFF())
				listaMedicamentosDroga.put(idMedicamento,Testing.patientBankData.getDrogaEnMedicamentoCodificada(idMedicamento));
			else
				return makeEventAmbiguedad();
		}

		if (!BuscadorMedicamentos.isFlagEventoAmbiguedadOFF())
			return makeEventAmbiguedad();
			
			
			long millisStart2 = (Calendar.getInstance().getTimeInMillis() - millisStart)/1000;

			System.out.println(millisStart2);
			// En esta sección compruebo las interacciones
			List<String> listaIdMedicamentos = Collections.list(listaMedicamentosDroga.keys());
			for (String med1 : listaIdMedicamentos)
				for (String med2 : listaIdMedicamentos)

					if(!med1.equals(med2))				// Verifico que el par no tenga los dos medicamentos iguales
					{
						System.out.println("hola");
						// Consulto a la BD para ver si se realizo una excepción para el par de medicamentos
						List<String> listaExcepcion = managerDB.executeScript_Query("SELECT detalle FROM "+Testing.esquema+".interacciones_excepcion WHERE idDrogaAleman1='"+med1+"' AND idDrogaAleman2='"+med2+"';", "detalle");
						listaExcepcion.addAll(managerDB.executeScript_Query("SELECT detalle FROM "+Testing.esquema+".interacciones_excepcion WHERE idDrogaAleman1='"+med2+"' AND idDrogaAleman2='"+med1+"';", "detalle"));

						if ( listaExcepcion.isEmpty() )
						{
							List <String> drogasMedicamento1= listaMedicamentosDroga.get(med1);
							List <String> drogasMedicamento2= listaMedicamentosDroga.get(med2);

							for (String do1 : drogasMedicamento1) 
								for (String do2 : drogasMedicamento2) 
								{		
									String drog1 = do1.split("_")[0];
									String drog2 = do2.split("_")[0];

									int d1 = Integer.parseInt(drog1);
									int d2 = Integer.parseInt(drog2);

									if(!drog1.equals(drog2) && d1<d2)		// Verifico que no se trate de la misma droga
									{
										List <String> listaInteracciones = managerDB.executeScript_Query("SELECT concat(gravedadNumero,'_',gravedadPalabra,'_',explicacionIntereccion,'_',tratamiento) as gravedadNumero  FROM "+Testing.esquema+".interacciones WHERE idDroga1='"+drog1+"' and idDroga2='"+drog2+"';", "gravedadNumero");
										if (!listaInteracciones.isEmpty())
										{

											for (String string : listaInteracciones)
											{
												//											logTexto+= "Interactua el " + med1 +" con "+ med2+ " por la droga " +drog1 + " con " + drog2+" "+string+"\n\n";	


												EventoInteraccionMedicamentosa a = new EventoInteraccionMedicamentosa();
												a.setData(med1, med2, Medicamento.getName(med1), Medicamento.getName(med2) , "", "", drog1, drog2, Droga.getName(drog1), Droga.getName(drog2), string.split("_")[0], string.split("_")[1], string.split("_")[2],string.split("_")[3]);
												resultados.add(a);
												//	tesis.xmlPrinter.addInteraccionMed(new EventoInteraccionMedicamentosa(med1, med2, "", "", "", "", drog1, drog2, "", "", string.split("_")[0], string.split("_")[1], string.split("_")[2]));
											}

										}
										else
										{
											// Como no se hallo interacción, voy a probar con sus 'drogas simplidicadas' de clorhidrato de azufre --> azufre

											List<String> list_drogasSimplificadas= new ArrayList<String>();
											String drogSimp1=null, drogSimp2=null;

											// Miro la droga1 y lo vuelco a la lista, si hay algo lo paso al string drogaSimp1
											list_drogasSimplificadas.addAll(managerDB.executeScript_Query("select droga.idDrogaAncestro as ancestro from "+Testing.esquema+".droga_formasimplificada as droga where droga.idDrogaOrigen="+drog1,"ancestro"));
											if (!list_drogasSimplificadas.isEmpty())
											{
												drogSimp1=list_drogasSimplificadas.get(0);
												list_drogasSimplificadas.clear();
											}

											// Miro la droga2 y lo vuelco a la lista, si hay algo lo paso al string drogaSimp2
											list_drogasSimplificadas.addAll(managerDB.executeScript_Query("select droga.idDrogaAncestro as ancestro from "+Testing.esquema+".droga_formasimplificada as droga where droga.idDrogaOrigen="+drog2,"ancestro"));
											if (!list_drogasSimplificadas.isEmpty())
											{
												drogSimp2=list_drogasSimplificadas.get(0);
												list_drogasSimplificadas.clear();
											}

											if (drogSimp1 != null)
											{
												int dsimp1 = Integer.parseInt(drogSimp1);
												if (dsimp1<d2)
													listaInteracciones.addAll(managerDB.executeScript_Query("SELECT concat(gravedadNumero,'_',gravedadPalabra,'_',explicacionIntereccion,'_',tratamiento) as gravedadNumero  FROM "+Testing.esquema+".interacciones WHERE idDroga1='"+drogSimp1+"' and idDroga2='"+drog2+"';", "gravedadNumero"));
												else
													listaInteracciones.addAll(managerDB.executeScript_Query("SELECT concat(gravedadNumero,'_',gravedadPalabra,'_',explicacionIntereccion,'_',tratamiento) as gravedadNumero  FROM "+Testing.esquema+".interacciones WHERE idDroga1='"+drog2+"' and idDroga2='"+drogSimp1+"';", "gravedadNumero"));				
											}

											if (drogSimp2 != null){
												int dsimp2 = Integer.parseInt(drogSimp2);
												if (d1<dsimp2)
													listaInteracciones.addAll(managerDB.executeScript_Query("SELECT concat(gravedadNumero,'_',gravedadPalabra,'_',explicacionIntereccion,'_',tratamiento) as gravedadNumero  FROM "+Testing.esquema+".interacciones WHERE idDroga1='"+drog1+"' and idDroga2='"+drogSimp2+"';", "gravedadNumero"));
												else
													listaInteracciones.addAll(managerDB.executeScript_Query("SELECT concat(gravedadNumero,'_',gravedadPalabra,'_',explicacionIntereccion,'_',tratamiento) as gravedadNumero  FROM "+Testing.esquema+".interacciones WHERE idDroga1='"+drogSimp2+"' and idDroga2='"+drog1+"';", "gravedadNumero"));				
											}

											if (drogSimp1 != null && drogSimp2!=null)
											{
												int dsimp1 = Integer.parseInt(drogSimp1);
												int dsimp2 = Integer.parseInt(drogSimp2);
												if (dsimp1<dsimp2)
													listaInteracciones.addAll(managerDB.executeScript_Query("SELECT concat(gravedadNumero,'_',gravedadPalabra,'_',explicacionIntereccion,'_',tratamiento) as gravedadNumero  FROM "+Testing.esquema+".interacciones WHERE idDroga1='"+drogSimp1+"' and idDroga2='"+drogSimp2+"';", "gravedadNumero"));
												else
													listaInteracciones.addAll(managerDB.executeScript_Query("SELECT concat(gravedadNumero,'_',gravedadPalabra,'_',explicacionIntereccion,'_',tratamiento) as gravedadNumero  FROM "+Testing.esquema+".interacciones WHERE idDroga1='"+drogSimp2+"' and idDroga2='"+drogSimp1+"';", "gravedadNumero"));					
											}	

											for (String string : listaInteracciones) {
												EventoInteraccionMedicamentosa a = new EventoInteraccionMedicamentosa();
												a.setData(med1, med2, Medicamento.getName(med1), Medicamento.getName(med2) , "", "", drog1, drog2, Droga.getName(drog1), Droga.getName(drog2), string.split("_")[0], string.split("_")[1], string.split("_")[2],string.split("_")[3]);
												resultados.add(a);	
											}
										}
									}
								}
						}
						else
						{
							//Testing.writeLogDebug("Se detectó una excepción entre los medicamentos" + listaExcepcion);
							// Se detectó una excepción entre los medicamentos 
							System.out.println("Se detectó una excepción entre los medicamentos" + listaExcepcion);

						}
					}

			return resultados;

	}

private static List<EventoInteraccionMedicamentosa> makeEventAmbiguedad(){
	// Se encontro un evento de ambiguedad, retorno lo del evento y se corta lo q venia haciendo
	List<EventoInteraccionMedicamentosa> auxi = new ArrayList<EventoInteraccionMedicamentosa>();
	EventoInteraccionMedicamentosa a = new EventoInteraccionMedicamentosa();
	a.setAmbiguedad(Testing.bufferAmbiguedades);
	Testing.bufferAmbiguedades=null;
	auxi.add(a);
	return auxi;
}


}
