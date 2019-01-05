package comfymap;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.util.Tool;

import java.io.IOException;
import java.io.UncheckedIOException;

public class HBaseDAO {

    private static final String TABLE_NAME = "ordonez";
    private static final byte[] TILE_FAMILY = Bytes.toBytes("tile");

    private Connection connection;

    public HBaseDAO() throws IOException {
        Configuration conf = HBaseConfiguration.create();
        conf.addResource("hbase-site.xml");
        conf.addResource("hdfs-site.xml");
        conf.addResource("core-site.xml");

        connection = ConnectionFactory.createConnection(conf);
    }

    public byte[] getCompressedTile(int x, int y, int z) throws IOException {
        String key = x + "-" + y + "-" + z;

        Table t = connection.getTable(TableName.valueOf(TABLE_NAME));
        Get get = new Get(Bytes.toBytes(key));
        Result result = t.get(get);
        if(result.isEmpty()){
            return new byte[0];
        }
        return result.getValue(TILE_FAMILY, TILE_FAMILY);
    }
}
