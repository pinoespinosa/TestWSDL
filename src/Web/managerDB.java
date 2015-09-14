package Web;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class managerDB {
	private static Connection connect = null;
	private static Statement statement = null;


	private static String 	nombreConnection = "root",
							passConnection= "36442114";//"PINO";

	private static boolean ErroresVisibles = true;

	public static List<String> buscar( String tabla, String campo, String valorCampo, String campo_retornar, boolean exact){
		// Consulta por un campo especifico en una tabla; es un caso tribial del "buscarExacto" al que se le pasan una lista de campos
		
		List<String> resultado = new ArrayList<String>();

		try {

			if (managerDB.conectionWasSucefull())
			{
				String scripInsertar;
				if (exact)
					scripInsertar="select * from " + tabla + " where " + campo + " like '"+ valorCampo + "'";
				else
					scripInsertar="select * from " + tabla + " where " + campo + " like '%"+ valorCampo + "%'";
				ResultSet resultSet = statement.executeQuery(scripInsertar);

				while (resultSet.next())
				{  
						resultado.add(resultSet.getString(campo_retornar));
				}
				managerDB.desconectar();			
			}

		} catch (Exception e)
		{
			if (ErroresVisibles)
				e.printStackTrace();

			resultado = null;
		}

		return resultado;
	}
	public static List<List<String>> buscar( String tabla,String campo, String valorCampo, List<String> campos_retornar, boolean exact){
		List<List<String>> resultado = new ArrayList<List<String>>();
		
		try {

			if (managerDB.conectionWasSucefull())
			{
				String scripInsertar;
				if (exact)
					scripInsertar="select * from " + tabla + " where " + campo + " like '"+ valorCampo + "'";
				else
					scripInsertar="select * from " + tabla + " where " + campo + " like '%"+ valorCampo + "%'";
				ResultSet resultSet = statement.executeQuery(scripInsertar);

				while (resultSet.next())
				{  
					for (int i=0; i<campos_retornar.size();i++)	
						resultado.get(i).add(resultSet.getString(campos_retornar.get(i)));
				}
				managerDB.desconectar();			
			}

		} catch (Exception e)
		{
			if (ErroresVisibles)
				e.printStackTrace();

			resultado = null;
		}
			

		return resultado;
	}
	
	public static List<List<String>> consultar( String tabla,String condicion, List<String> campos_retornar){
		// Realiza una consulta con los campos ingresados, si la consulta es invalida retorna una lista null.
		List<List<String>> resultado = new ArrayList<List<String>>();		
		try {

			if (managerDB.conectionWasSucefull())
			{
				String scripInsertar="select distinct * from " + tabla + " where " + condicion;
				ResultSet resultSet = statement.executeQuery(scripInsertar);
				int j=0;

				while (resultSet.next())
				{  
					resultado.add(new ArrayList<String>());
					for (int i=0; i<campos_retornar.size();i++)					
						resultado.get(j).add(resultSet.getString(campos_retornar.get(i)));
					j++;
				}
				managerDB.desconectar();			
			}

		} catch (Exception e)
		{
			if (ErroresVisibles)
				e.printStackTrace();
			resultado = null;
		}
		return resultado;
	}
		
	public static boolean exist( String tabla, String campo, String valorCampo){
		boolean resultado = false;
		try 
		{
			if (managerDB.conectionWasSucefull())
			{	
				String scripInsertar="select * from " + tabla + " where " + campo + " like '%" + valorCampo + "%'";
				ResultSet resultSet = statement.executeQuery(scripInsertar);

				if (resultSet.next())
					resultado=true;

				managerDB.desconectar();
			}
		} 
		catch (Exception e)
		{
			if (ErroresVisibles)
				e.printStackTrace();
		}
		return resultado;
	}

	public static boolean executeScript_Void(String Script){
		// Es para ejecutar scripts que no devuelvan resultados, como por ejemplo insertar, actualizar, borrar datos o crear una datos a una tabla. 
		// Retorna un valor booleano que indica si fallo la ejecucion.
		boolean resultado=false;
		try
		{
			if (managerDB.conectionWasSucefull())
			{
				statement.execute(Script);
				managerDB.desconectar();
				resultado = true;
			}
		} 
		catch (Exception e)
		{
			if (ErroresVisibles)
				e.printStackTrace();
		}

		return resultado;
	}
	public static List<String> executeScript_Query( String Script, String campo_retornar){
		// Es la complementaria de la anterior, funciones que retornan como consultas.
		
		
		List<String> resultado = new ArrayList<String>();
		ResultSet resultSet = null;
		try{
			if (managerDB.conectionWasSucefull())
			{
		//		System.out.println(Script);
				resultSet = statement.executeQuery(Script);
								
				while (resultSet.next())
					resultado.add(resultSet.getString(campo_retornar));
			}
			managerDB.desconectar();

		} catch (Exception e)
		{
			
			if (ErroresVisibles)
				e.printStackTrace();
		}
		return resultado;
	}

	private static boolean conectionWasSucefull()
	{
		try{
			Class.forName("com.mysql.jdbc.Driver");

			// Conectar a la base de datos
			connect = DriverManager.getConnection("jdbc:mysql://localhost/", nombreConnection, passConnection);
			statement = connect.createStatement();
			
			return true;
		}

		catch (Exception e) 
		{
			if (ErroresVisibles)
				e.printStackTrace();

			return false;
		}
	}
	private static void desconectar()
	{
		try 
		{
			statement.close();
			connect.close();
		} 
		catch (SQLException e) 
		{
			if (ErroresVisibles)
				e.printStackTrace();	
		}
	}

}
