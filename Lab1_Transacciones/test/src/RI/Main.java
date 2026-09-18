package RI;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import com.mchange.v2.c3p0.DataSources;

public class Main {

    private static final String URL = "jdbc:hsqldb:hsql://localhost";
    private static final String USERNAME = "sa";
    private static final String PASSWORD = "";
    private static final int TIMES = 100;

    private static final String QUERY="UPDATE TInvoices SET amount = amount + ? WHERE state = 'PAID'";


    public static void main(String[] args) throws SQLException {

        Main m = new Main();

        m.withStatement();
        //m.withPreparedStatement();
        //m.withBach();
       // m.withPool();
    }
//cae en el examen la diferencia entre prepared statement y statrement
    public void withStatement() throws SQLException {
        long inicio = System.currentTimeMillis();

        try (Connection c = DriverManager.getConnection(URL, USERNAME, PASSWORD);
             Statement st = c.createStatement()) {


            for (int i = 0; i < TIMES; i++) {
                st.executeUpdate(QUERY);
            }


            
        }
        long fin = System.currentTimeMillis();
        System.out.println("Statement:");
        System.out.println("Tiempo: " + (fin - inicio) + " ms");
    }
    
    private static void withPool() throws SQLException {
    	DataSource unpooled =DataSources.unpooledDataSource(URL,USERNAME,PASSWORD);
    	
    	Map<String,Object> overrides = new HashMap<>();
    	overrides.put("minPoolSize", 3);
    	overrides.put("maxPoolSize", 15);
    	overrides.put("initialPoolSize", 3);

    	DataSource pooled = DataSources.pooledDataSource(unpooled,overrides);
    	
    	long ti = System.currentTimeMillis();
    	for (int i = 0; i < TIMES; i++) {
			try(Connection con = pooled.getConnection()){
				execute(con,i);
			}
		}
    	long tf = System.currentTimeMillis();
    	System.out.printf("with pool    %d ms%n",tf-ti);
    	
    	DataSources.destroy(pooled);
    }

	private static void execute(Connection con, int i) throws SQLException {
		try(PreparedStatement pst = con.prepareStatement(QUERY)){
			pst.setInt(1, i);
			pst.executeUpdate();
		}
	}
}

    