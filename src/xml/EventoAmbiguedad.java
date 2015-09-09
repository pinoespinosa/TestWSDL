package xml;

public class EventoAmbiguedad {

	private String idMedicamento, nombreMedicamento, resumen, codigo;

	public String getIdMedicamento() {
		return idMedicamento;
	}

	public String getNombreMedicamento() {
		return nombreMedicamento;
	}

	public String getResumen() {
		return resumen;
	}

	public String getCodigo() {
		return codigo;
	}
	
	public void setIdMedicamento(String idMedicamento) {
		this.idMedicamento = idMedicamento;
	}

	public void setNombreMedicamento(String nombreMedicamento) {
		this.nombreMedicamento = nombreMedicamento;
	}

	public void setResumen(String resumen) {
		this.resumen = resumen;
	}
	
	public void setCodigo(String codigo) {
		this.codigo = codigo;
	}
	
	public void setData(String id, String NombreMed, String res, String cod ){
		idMedicamento=id;
		resumen=res;
		nombreMedicamento=NombreMed;
		codigo=cod;
	}
	
}
