package Web;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;

public class Paciente {

public static final int EDAD_INVALIDA= -1, PESO_INVALIDO= -1;
public static final String SEXO_INVALIDO= "N";	
public static final String COND_EMBARAZO= "embarazo";	
	


	private Hashtable< String, List<String>> data;
	Hashtable<String, Integer> listaMedicamentos = null;
	Hashtable<String, List<String>> drogasMedicamentos = null;
	
	
	public Paciente (Hashtable< String, List<String>> dataPatient){
		if (dataPatient!=null)
			data=dataPatient;
		else
			data = new Hashtable<String, List<String>>();
	}
	
	public Paciente (int edad, int peso, String sexo, Hashtable< String, List<String>> dataPatient){
		if (dataPatient!=null)
			data=dataPatient;
		else
			data = new Hashtable<String, List<String>>();
		dataPatient.put("peso",Arrays.asList(peso+""));
		dataPatient.put("edad",Arrays.asList(edad+""));
		dataPatient.put("sexo",Arrays.asList(sexo));
		
	}
	
	
	
	public void clear(){
//		if (listaMedicamentos!=null)
//		listaMedicamentos.clear();
//		if (drogasMedicamentos!=null)
//		drogasMedicamentos.clear();
//		if (data!=null)
//		data.clear();
	}
	
	
	
	public boolean takesMeditation(){
		List<String> idMedicamentos = data.get("id");
		if (idMedicamentos!=null)
			return idMedicamentos.size()>0;
		else
				return false;
		}	
	public int getAge(){
		List<String> edad = data.get("edad");
		if (edad!= null)
			return Integer.parseInt(edad.get(0));
		else{
			return EDAD_INVALIDA;
			}
	}
	public int getWeight(){
		List<String> peso = data.get("peso");
		if (peso!= null)
			return Integer.parseInt(peso.get(0));
		else
			return PESO_INVALIDO;
	}
	public Hashtable<String, Integer> getMedications(){
		if (listaMedicamentos==null)
		{
			listaMedicamentos = new Hashtable<String, Integer>();
			if (takesMeditation()){
				for (String string : data.get("id")) {
					if (string.contains("_"))
						listaMedicamentos.put((String) string.subSequence(0, string.indexOf("_")),Integer.parseInt((String) string.subSequence(string.indexOf("_")+1, string.length() )   ) );
				}
			}
		}
		return listaMedicamentos;
	}
	
	
	/**
	 * *
	 * @param idMed
	 */
	public List<String> getDrogaEnMedicamentoCodificada(String idMed){
		if (drogasMedicamentos==null)
			drogasMedicamentos=new Hashtable<String, List<String>>();
			
		if (!drogasMedicamentos.containsKey(idMed))
			drogasMedicamentos.put(idMed, BuscadorMedicamentos.getDrogasEnMedicamento(idMed));
		
		return drogasMedicamentos.get(idMed);
	}

	public Hashtable<String, List<String>> getDrogaEnTodosLosMedicamentoCodificadas(){
		if (drogasMedicamentos!=null)
			return drogasMedicamentos;
		else 
			return null;
	}
	
	public String getSex(){
		List<String> sexo = data.get("sexo");
		if (sexo!= null)
			return sexo.get(0);
		else
			return SEXO_INVALIDO;
	}
	
	public List<String> getEspetialConditions(){
		if (data.get("conditions")==null)
			return new ArrayList<String>();
		else
			return data.get("conditions");	
	} 
	
	public boolean isWoman(){
		return getSex().equals("F");
		
	}
	
	public String getIdDoctor(){
		List<String> idMed = data.get("idDoctor");
		if (idMed!= null)
			return idMed.get(0);
		else
			return "";
	}
	
	
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Paciente){
			Paciente comp =((Paciente)obj);
			return 	comp.getAge()==getAge() &&
					comp.getWeight()==getWeight() &&
					comp.getSex().equals(getSex()) &&
					comp.getIdDoctor().equals(getIdDoctor()) &&
					comp.getMedications().equals(getMedications()) &&
					comp.getEspetialConditions().equals(getEspetialConditions());
			
		}
				
		return false;
	}

	public Hashtable<String, List<String>> getData() {
		return data;
	}
	
	
}
