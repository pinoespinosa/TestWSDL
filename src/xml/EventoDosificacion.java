package xml;



public class EventoDosificacion implements Evento{


	private String idDroga,nombreDroga,dosisrecetada,dosismaxima,explicacion,consecuencia,tipoalerta;
	private EventoAmbiguedad[] ambiguedad;

	public void setData(String idDroga, String nombreDroga, String dosisrecetada, String dosismaxima, String explicacion,	String consecuencia, String tipoalerta) {
		this.idDroga = idDroga;
		this.nombreDroga = nombreDroga;
		this.dosisrecetada = dosisrecetada;
		this.dosismaxima = dosismaxima;
		this.explicacion = explicacion;
		this.consecuencia = consecuencia;
		this.tipoalerta = tipoalerta;
		this.ambiguedad=new EventoAmbiguedad[0];
	}


	public String getIdDroga() {
		return idDroga;
	}

	public void setIdDroga(String idDroga) {
		this.idDroga = idDroga;
	}

	public String getNombreDroga() {
		return nombreDroga;
	}

	public void setNombreDroga(String nombreDroga) {
		this.nombreDroga = nombreDroga;
	}

	public String getDosisrecetada() {
		return dosisrecetada;
	}

	public void setDosisrecetada(String dosisrecetada) {
		this.dosisrecetada = dosisrecetada;
	}

	public String getDosismaxima() {
		return dosismaxima;
	}

	public void setDosismaxima(String dosismaxima) {
		this.dosismaxima = dosismaxima;
	}

	public String getExplicacion() {
		return explicacion;
	}

	public void setExplicacion(String explicacion) {
		this.explicacion = explicacion;
	}

	public String getConsecuencia() {
		return consecuencia;
	}

	public void setConsecuencia(String consecuencia) {
		this.consecuencia = consecuencia;
	}

	public String getTipoalerta() {
		return tipoalerta;
	}

	public void setTipoalerta(String tipoalerta) {
		this.tipoalerta = tipoalerta;
	}

	public EventoAmbiguedad[] getAmbiguedad() {
		return ambiguedad;
	}

	public void setAmbiguedad(EventoAmbiguedad[] ambiguedad) {
		this.ambiguedad = ambiguedad;
	}
	
	
}
