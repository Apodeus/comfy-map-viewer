package comfymap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.security.GeneralSecurityException;
import java.util.Random;

@Path("/map")
public class MapManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(MapManager.class);
    public static final int SIZE = 1201;
    private final Random random;

    //private List<byte[]> wololo;
    private byte[] bidoof;
    private byte[] result;


    public MapManager() throws GeneralSecurityException, IOException {
        /*
        byte[] ul = new FileInputStream(new File("N44W002.pgm")).readAllBytes();
        byte[] ur = new FileInputStream(new File("N44W001.pgm")).readAllBytes();
        byte[] br = new FileInputStream(new File("N43W001.pgm")).readAllBytes();
        byte[] bl = new FileInputStream(new File("N43W002.pgm")).readAllBytes();

        wololo = new ArrayList<>();
        wololo.add(ul);
        wololo.add(ur);
        wololo.add(br);
        wololo.add(bl);
        */

        byte[] res = new byte[SIZE * SIZE];
        for(int x = 0; x < SIZE; ++x){
            for(int y = 0; y < SIZE; ++y){
                res[x + y * SIZE] = (byte)((x + y) * 255 / 2040);
            }
        }

        FileOutputStream fos = new FileOutputStream(new File("res.txt"));
        fos.write(res);
        result = res;

        bidoof = new FileInputStream(new File("Bidoof.png")).readAllBytes();
        random = new Random();
    }

    @GET
    @Path("/{z}/{x}/{y}")
    @Produces("image/png")
    public Response doStuff(@PathParam("x") int x, @PathParam("y") int y, @PathParam("z") int z) throws IOException {
        //int ind = (x + y) % 4;
        ByteArrayOutputStream bais = new ByteArrayOutputStream();

        ImageIO.write(colorizeMap(result), "png", bais);
        bais.flush();
        Response build = Response.ok(bais.toByteArray()).build();
        bais.close();
        return build;
    }

    private BufferedImage colorizeMap(byte[] data){
        BufferedImage bi = new BufferedImage(SIZE, SIZE, BufferedImage.TYPE_INT_RGB);

        for(int x = 0; x < SIZE; ++x){
            for(int y = 0; y < SIZE; ++y){
                int b = Byte.toUnsignedInt(data[x + y * SIZE]);
                float hue = b / 360.0f;
                bi.setRGB(x, y, Color.HSBtoRGB(hue, 1.0f, 1.0f));
            }
        }

        for (int i = 0; i < data.length; ++i){
            int x = i % SIZE;
            int y = i / SIZE;
            int b = Byte.toUnsignedInt(data[i]);
            bi.setRGB(x, y, Color.HSBtoRGB((((float) b) / 360.0f) , 1, 1));
        }

        return bi;
    }
}
