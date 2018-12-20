package comfymap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.io.*;
import java.security.GeneralSecurityException;

@Path("/map")
public class MapManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(MapManager.class);

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
    }

    @GET
    @Path("/{z}/{x}/{y}")
    @Produces("image/png")
    public Response doStuff(@PathParam("x") int x, @PathParam("y") int y, @PathParam("z") int z) throws IOException {
        //int ind = (x + y) % 4;
        return Response.ok(new ByteArrayInputStream(bidoof).readAllBytes()).build();
    }
}
