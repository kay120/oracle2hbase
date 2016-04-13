package mr2hbase;

import java.util.Arrays;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Created by shaobo on 15-6-9.
 */
public class BulkLoadMapper extends
		Mapper<LongWritable, Text, ImmutableBytesWritable, Put> {
	private static Logger logger = LoggerFactory.getLogger(BulkLoadMapper.class);
	private String hbaseTable;
	private String dataSeperator;
	private String columnFamily1;
//	private String columnFamily2;
	private Configuration configuration;
	private String s_colNum;
	private String [] s_rowkey ;
	List<String> cols_list ;
	public void setup(Context context) {
		configuration = context.getConfiguration();// 获取作业参数
		hbaseTable = configuration.get("hbase.table.name");
		dataSeperator = configuration.get("data.seperator");
		columnFamily1 = configuration.get("COLUMN_FAMILY_1");
//		columnFamily2 = configuration.get("COLUMN_FAMILY_2");
		s_colNum = configuration.get("oracle.table.col.num");
		s_rowkey = configuration.get("oracle.table.rowkey").split(",");
		cols_list = Arrays.asList(configuration.get("oracle.table.cols").split(","));
		logger.info("======================== map setup =========================");
		logger.info("======================== s_rowkey length : " + s_rowkey.length);
		logger.info("======================== cols_list length : " + cols_list.size());
		logger.info("======================= oracle.table.rowkey : " + configuration.get("oracle.table.rowkey"));
		logger.info("======================= oracle.table.cols : " + configuration.get("oracle.table.cols"));
		logger.info("======================== map setup end =========================");
	}

	public void map(LongWritable key, Text value, Context context) {
		try {
			logger.info("======================== dataSeperator : " + dataSeperator );
			logger.info("========================  " + value.toString() + "  value length : " + value.getLength());
			String[] cols_values = value.toString().split(dataSeperator);
			logger.info("======================= values length : " + cols_values.length);

			StringBuffer sb_rowKey = new StringBuffer();
			
			for (int i = 0; i < s_rowkey.length; i++) {
				logger.info("=============== i : " +  i  );
				logger.info("=============== s_rowkey[" + i + "] : " +  s_rowkey[i] );
				for (String col : cols_values) {
					logger.info("=============== " + col );
				}
				logger.info("=============== cols_list.indexOf : " +  cols_list.indexOf(s_rowkey[i]) );
				sb_rowKey.append(cols_values[cols_list.indexOf(s_rowkey[i])] + "_");
			}
			sb_rowKey.deleteCharAt(sb_rowKey.length() - 1);
			
			ImmutableBytesWritable rowKey = new ImmutableBytesWritable(
					Bytes.toBytes(sb_rowKey.toString()));
			logger.info("===================rowkey : " + rowKey.toString());
			Put put = new Put(rowKey.get());
			for (String col : cols_list) {
				put.addColumn(Bytes.toBytes(columnFamily1), Bytes.toBytes(col),
						Bytes.toBytes(cols_values[cols_list.indexOf(col)]));
			}
			
			context.write(rowKey, put);
		} catch (Exception exception) {
			exception.printStackTrace();
			System.exit(1);
		}
	}
}