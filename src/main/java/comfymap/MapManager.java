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
    private final Random random;

    //private List<byte[]> wololo;
    private byte[] bidoof;

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

        bidoof = new FileInputStream(new File("Bidoof.png")).readAllBytes();
        random = new Random();
    }

    @GET
    @Path("/{z}/{x}/{y}")
    @Produces("image/png")
    public Response doStuff(@PathParam("x") int x, @PathParam("y") int y, @PathParam("z") int z) throws IOException {
        //int ind = (x + y) % 4;
        ByteArrayOutputStream bais = new ByteArrayOutputStream();
        byte[] result = new byte[1201*1201] ;
        for (int i = 0; i < 1201*1201; ++i){
            result[i] = (byte) random.nextInt();
        }
        ImageIO.write(colorizeMap(result), "png", bais);
        bais.flush();
        bais.close();
        return Response.ok(bais.toByteArray()).build();
    }


    static int size = 1201;
    private BufferedImage colorizeMap(byte[] data){
        BufferedImage bi = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);

        for (int i = 0; i < data.length; ++i){
            int x = i % size;
            int y = i / size;
            int b = data[i];
            bi.setRGB(x, y, Color.HSBtoRGB((float)(b % 240 / 360), 1, 1));
        }

        return bi;
    }
}
