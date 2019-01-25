package comfymap;

import exception.TileNotFoundException;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;

import static java.text.MessageFormat.*;

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

    public byte[] getCompressedBytes(int x, int y, int z, String qualifier) throws IOException, TileNotFoundException {
        String key = x + "-" + y + "-" + z;
        LOGGER.info(format("key is : {0}", key));

        Table t = this.connection.getTable(TableName.valueOf(TABLE_NAME));
        Get get = new Get(Bytes.toBytes(key));
        Result result = t.get(get);
        if(result.isEmpty()){
            throw new TileNotFoundException(format("No tile found in HBase for coordinates : ({0},{1},{2})", x, y, z));
        }
        return result.getValue(TILE_FAMILY, Bytes.toBytes(qualifier));
    }
}