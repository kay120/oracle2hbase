package util;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;

public class DataExportor {

	public static void main(String[] args) throws Exception{
		final String exportFile = args[0] + ".bin";
		final String jdbcUrl = "jdbc:oracle:thin:@192.168.8.1:1521:hadoop";
		final String jdbcUsr = "KAY";
		final String jdbcPsw = "123456";
		final String sql = "select * from " + args[0];
		final boolean printHeader = false;
		PrintWriter pw = new PrintWriter(exportFile);
		
		Class.forName("oracle.jdbc.driver.OracleDriver").newInstance();
		
		try{
			Connection conn = DriverManager.getConnection(jdbcUrl, jdbcUsr, jdbcPsw);
			Statement stmt = conn.createStatement();
			
			ResultSet rs = stmt.executeQuery(sql);
			ResultSetMetaData metaData = rs.getMetaData();
			int columnCount = metaData.getColumnCount();
			
			if(printHeader){
				for(int i = 1; i <= columnCount; i++){
					String columnName = metaData.getColumnName(i);
					pw.print(columnName + "\t");
				}
				pw.println();
			}
			
			rs.setFetchSize(1000);
			while(rs.next()){
				for( int i = 1; i<= columnCount; i++){
					pw.print(rs.getObject(i) + "\t");
				}
				pw.println();
			}
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			if(pw != null){
				pw.close();
			}
		}
		System.out.println("END");
	}
}
