package xml;

public class EventoInteraccionCondicion implements Evento{

private String iddroga, nombredroga, idcondicion, nombrecondicion;
private EventoAmbiguedad[] ambiguedad;


public void setData(String iddroga, String nombredroga,	String idcondicion, String nombrecondicion) {
	this.iddroga = iddroga;
	this.nombredroga = nombredroga;
	this.idcondicion = idcondicion;
	this.nombrecondicion = nombrecondicion;
	this.ambiguedad=new EventoAmbiguedad[0];
}

public String getIddroga() {
	return iddroga;
}

public String getNombredroga() {
	return nombredroga;
}

public String getIdcondicion() {
	return idcondicion;
}

public String getNombrecondicion() {
	return nombrecondicion;
}	

public EventoAmbiguedad[] getAmbiguedad() {
	return ambiguedad;
}

public void setAmbiguedad(EventoAmbiguedad[] ambiguedad) {
	this.ambiguedad = ambiguedad;
}

}
