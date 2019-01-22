package comfymap;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class HBaseDAO {

    private static final String TABLE_NAME = "ordonez";
    private static final byte[] TILE_FAMILY = Bytes.toBytes("tile");
    private static final Logger LOGGER = LoggerFactory.getLogger(HBaseDAO.class);

    private Connection connection;

    public HBaseDAO() throws IOException {

        Configuration conf = HBaseConfiguration.create();
        conf.addResource("hbase-site.xml");
        conf.addResource("hdfs-site.xml");
        conf.addResource("core-site.xml");

        this.connection = ConnectionFactory.createConnection(conf);
    }

    public byte[] getCompressedTile(int x, int y, int z, String qualifier) throws IOException {


        String key = x + "-" + y + "-" + z;
        LOGGER.info("key is : " + key);
        //key = String.valueOf(x * 1000 + y);

        Table t = this.connection.getTable(TableName.valueOf(TABLE_NAME));
        Get get = new Get(Bytes.toBytes(key));
        Result result = t.get(get);
        if(result.isEmpty()){
            return new byte[0];
        }
        return result.getValue(TILE_FAMILY, Bytes.toBytes(qualifier));
    }
}