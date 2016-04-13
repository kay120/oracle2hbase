package mr2hbase;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.mapreduce.HFileOutputFormat2;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.SecurityUtils;
/**
 * Created by shaobo on 15-6-9.
 */
public class BulkLoadDriver extends Configured implements Tool {
	private static Logger logger = LoggerFactory.getLogger(BulkLoadDriver.class);
	private static  String DATA_SEPERATOR ;
	private static  String TABLE_NAME ;// 表名
	private static  String COLUMN_FAMILY_1 ;// 列组1
//	private static final String COLUMN_FAMILY_2 = "tempPerHour";// 列组2
    
	private static void setMember(Configuration conf){
		DATA_SEPERATOR = conf.get("data.seperator");
		TABLE_NAME = conf.get("hbase.table.name");
		COLUMN_FAMILY_1 = conf.get("COLUMN_FAMILY_1");
		logger.info("================ member ==============");
		logger.info("================ DATA_SEPERATOR :" + DATA_SEPERATOR);
		logger.info("================ TABLE_NAME :" + TABLE_NAME);
		logger.info("================ COLUMN_FAMILY_1 :" + COLUMN_FAMILY_1);
	}
	public static void main(String[] args) {
		try {
//			Configuration conn = HBaseConfiguration.create();
			Configuration conn = SecurityUtils.initConfiguration();
			BulkLoadDriver.setMember(conn);
//			conn.set("hbase.master", "master1:60000");
//			conn.set("hbase.zookeeper.quorum",
//					"192.168.8.101:2181,master2:2181,slave1:2181");
			
			int response = ToolRunner.run(conn,
					new BulkLoadDriver(), args);
			logger.info("===========" + args[0] +  " " + args[1]);
			if (response == 0) {
				System.out.println("Job is successfully completed...");
			} else {
				System.out.println("Job failed...");
			}
		} catch (Exception exception) {
			exception.printStackTrace();
		}
	}

	public int run(String[] args) throws Exception {
		String outputPath = args[1];
		/**
		 * 设置作业参数
		 */
		
		Configuration configuration = getConf();
		
//		configuration.set("data.seperator", DATA_SEPERATOR);
//		configuration.set("hbase.table.name", TABLE_NAME);
//		configuration.set("COLUMN_FAMILY_1", COLUMN_FAMILY_1);
//		configuration.set("COLUMN_FAMILY_2", COLUMN_FAMILY_2);
		Job job = Job.getInstance(configuration, "Bulk Loading HBase Table::"
				+ TABLE_NAME);
		job.setJarByClass(BulkLoadDriver.class);
		job.setInputFormatClass(TextInputFormat.class);
		job.setMapOutputKeyClass(ImmutableBytesWritable.class);// 指定输出键类
		job.setMapOutputValueClass(Put.class);// 指定输出值类
		job.setMapperClass(BulkLoadMapper.class);// 指定Map函数
		FileInputFormat.addInputPaths(job, args[0]);// 输入路径
		FileSystem fs = FileSystem.get(configuration);
		Path output = new Path(outputPath);
		if (fs.exists(output)) {
			fs.delete(output, true);// 如果输出路径存在，就将其删除
		}
		FileOutputFormat.setOutputPath(job, output);// 输出路径
		Connection connection = ConnectionFactory
				.createConnection(configuration);
		TableName tableName = TableName.valueOf(TABLE_NAME);
		HFileOutputFormat2.configureIncrementalLoad(job,
				connection.getTable(tableName),
				connection.getRegionLocator(tableName));
		job.waitForCompletion(true);
		if (job.isSuccessful()) {
			HFileLoader.doBulkLoad(outputPath, TABLE_NAME, configuration);// 导入数据
			return 0;
		} else {
			return 1;
		}
	}
}