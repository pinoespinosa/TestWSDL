package xml;

public class EventoInteraccionMedicamentosa implements Evento{

	String 	idmanfarMed1,
			idmanfarMed2, 
			nombreMed1, 
			nombreMed2,
			presentacionMed1,
			presentacionMed2,
			idDrogaMed1,
			idDrogaMed2,
			nombreDrogaMed1,
			nombreDrogaMed2,
			gravedad,
			gravedadPalabra,
			explicacion, 
			tratamiento;
	EventoAmbiguedad[] ambiguedad;
	
	public void setData(String idmanfarMed1, String idmanfarMed2, String nombreMed1, String nombreMed2, String presentacionMed1, String presentacionMed2, String idDrogaMed1, String idDrogaMed2, String nombreDrogaMed1, String nombreDrogaMed2, String gravedad, String gravedadPalabra, String explicacion, String tratamiento) {
		this.idmanfarMed1 = idmanfarMed1;
		this.idmanfarMed2 = idmanfarMed2;
		this.nombreMed1 = nombreMed1;
		this.nombreMed2 = nombreMed2;
		this.presentacionMed1 = presentacionMed1;
		this.presentacionMed2 = presentacionMed2;
		this.idDrogaMed1 = idDrogaMed1;																																	
		this.idDrogaMed2 = idDrogaMed2;
		this.nombreDrogaMed1 = nombreDrogaMed1;
		this.nombreDrogaMed2 = nombreDrogaMed2;
		this.gravedad = gravedad;
		this.gravedadPalabra = gravedadPalabra;
		this.explicacion = explicacion;
		this.tratamiento = tratamiento;
	}

	public String getIdmanfarMed1() {
		return idmanfarMed1;
	}

	public String getIdmanfarMed2() {
		return idmanfarMed2;
	}

	public String getNombreMed1() {
		return nombreMed1;
	}

	public String getNombreMed2() {
		return nombreMed2;
	}

	public String getPresentacionMed1() {
		return presentacionMed1;
	}

	public String getPresentacionMed2() {
		return presentacionMed2;
	}

	public String getIdDrogaMed1() {
		return idDrogaMed1;
	}

	public String getIdDrogaMed2() {
		return idDrogaMed2;
	}

	public String getNombreDrogaMed1() {
		return nombreDrogaMed1;
	}

	public String getNombreDrogaMed2() {
		return nombreDrogaMed2;
	}

	public String getGravedad() {
		return gravedad;
	}

	public String getGravedadPalabra() {
		return gravedadPalabra;
	}

	public String getExplicacion() {
		return explicacion;
	}
	
	public String getTratamiento() {
		return tratamiento;
	}
	public EventoAmbiguedad[] getAmbiguedad() {
		return ambiguedad;
	}

	public void setAmbiguedad(EventoAmbiguedad[] ambiguedad) {
		this.ambiguedad = ambiguedad;
	}


}
