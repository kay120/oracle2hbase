package util;

import java.io.File;

//import mr2hbase.;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SecurityUtils {
	private static Logger logger = LoggerFactory.getLogger(SecurityUtils.class);
	private static String PRINCIPAL = "username.client.kerberos.principal";
	private static String KEYTAB = "username.client.keytab.file";
	private static Configuration conf;
	
	@SuppressWarnings("unused")
	public static Configuration initConfiguration(){
		conf = new Configuration();
		String projectRoot = System.getenv("PROJECT_ROOT") +  File.separator 
								+ "conf" + File.separator;
		if(projectRoot == null){
			logger.error("==================== need PROJECT_ROOT env ====================");
			System.exit(1);
		}
		conf.addResource(new Path(projectRoot + "yarn-site.xml"));
		conf.addResource(new Path(projectRoot + "mapred-site.xml"));
		conf.addResource(new Path(projectRoot + "core-site.xml"));
		conf.addResource(new Path(projectRoot + "hdfs-site.xml"));
		conf.addResource(new Path(projectRoot + "hbase-site.xml"));
		conf.addResource(new Path(projectRoot + "user-site.xml"));
//		conf.addResource(new Path(projectRoot + "table-site.xml"));
		logger.info("============================= init conf ==========================");
		logger.info("projectRoot : " + projectRoot);
//		logger.info("hbase.master : " + conf.get("hbase.master"));
		logger.info("hbase.zookeeper.quorum : " + conf.get("hbase.zookeeper.quorum"));
		logger.info("hbase.zookeeper.property.clientPort : " + conf.get("hbase.zookeeper.property.clientPort"));
		logger.info("data.seperator : " + conf.get("data.seperator"));
		logger.info("hbase.table.name : " + conf.get("hbase.table.name"));
//		conf.set(PRINCIPAL, value);
		logger.info(PRINCIPAL + " : " + conf.get(PRINCIPAL));
		logger.info(KEYTAB + " : " + conf.get(KEYTAB));
		
		return conf;
	}
	
	public static Configuration getConfiguration(){
		return conf;
	}
	
}
