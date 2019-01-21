package comfymap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import javax.inject.Singleton;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.ByteBuffer;
import java.util.zip.DataFormatException;

@Path("/map")
@Singleton
public class MapManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(MapManager.class);
    private static final int TILE_SIZE = 1201;
    //private final byte[] tile1;
    private final byte[] tile2;
    private byte[] bl; // bottom left
    private byte[] ul; // up left
    private byte[] br; // bottom right
    private byte[] ur; // up right

    private byte[] testTile; // up right
    private final int[] LUT;

    //private final HBaseDAO hBaseDAO;

    public MapManager() throws IOException, DataFormatException {
        //this.hBaseDAO = new HBaseDAO();

        //Test normal map ...
        //byte[] tileExample = CompressionUtil.decompress(new FileInputStream(new File("179040.dio")).readAllBytes());
        LUT = initLUT();
        //get normal map
        //float[] normalMap = getNormalMap("179040.jojo");
        //float[] normalMap1 = getNormalMap("89019.jojo");
        //float[] normalMap2 = getNormalMap("89020.jojo");
        float[] normalMap3 = getNormalMap("266056.jojo");

        //this.testTile = getFileAsByte(tileExample, normalMap);
        //this.tile1 = getFileAsByte(CompressionUtil.decompress(new FileInputStream(new File("89019.dio")).readAllBytes()), normalMap1);
        //this.tile2 = getFileAsByte(CompressionUtil.decompress(new FileInputStream(new File("89020.dio")).readAllBytes()), normalMap2);
        this.tile2 = getFileAsByte(CompressionUtil.decompress(new FileInputStream(new File("266056.dio")).readAllBytes()), normalMap3);
    }

    private float[] getNormalMap(String file) throws IOException, DataFormatException {
        float[] normalMap = new float[TILE_SIZE * TILE_SIZE];
        //byte[] tmpNorm = CompressionUtil.decompress(new FileInputStream(new File(file)).readAllBytes());
        byte[] tmpNorm = new FileInputStream(new File(file)).readAllBytes();
        for(int i = 0; i < TILE_SIZE * TILE_SIZE; ++i){
            ByteBuffer bb = ByteBuffer.allocate(4);
            for(int j = i * 4; j < i*4 + 4; ++j){
                bb.put(tmpNorm[j]);
            }
            normalMap[i] = bb.getFloat(0);
        }
        return normalMap;
    }

    private int[] initLUT() {
        int[] lut = new int[8900];
        for(int i = 0; i < 8900; ++i){
            lut[i] = AdrienColors.getRGBFromHeight(i);
        }
        return lut;
    }

    @GET
    @Path("/{z}/{x}/{y}")
    @Produces("image/png")
    public Response doStuff(@PathParam("x") int x, @PathParam("y") int y, @PathParam("z") int z) throws IOException {
        //Select the good image to display to get the famous Sud-Ouest in the good order
        byte[] result;
        if(x % 2 == 0){
            if(y % 2 == 0){
                result = ul;
            } else {
                result = bl;
            }
        } else {
            if(y % 2 == 0){
                result = ur;
            } else {
                result = br;
            }
        }

        /*byte[] resHBase = hBaseDAO.getCompressedTile(x, y, 0);
        if(resHBase.length == 0){
            result = defaultTile;
        } else {
            try {
                result = CompressionUtil.decompress(resHBase);
            } catch (DataFormatException e) {
                LOGGER.info(e.getMessage());
                result = defaultTile;
            }
        }*/
        if(y%2 == 0){
            result = tile2;
        } else {
            result = tile2;
        }
        return Response.ok(result).build();
    }

    //Generate png files as byte array
    private byte[] getFileAsByte(byte[] data, float[] normalMap) throws IOException {
        byte[] resp;
        ByteArrayOutputStream bais = new ByteArrayOutputStream();
        ImageIO.write(colorizeMap(data, normalMap), "png", bais);
        bais.flush();
        resp = bais.toByteArray();
        bais.close();
        return resp;
    }

    private BufferedImage colorizeMap(byte[] data, float[] normalMap){
        BufferedImage bi = new BufferedImage(TILE_SIZE, TILE_SIZE, BufferedImage.TYPE_INT_RGB);
        int sizeOf = 2; //size of Short

        for (int i = 0; i < data.length; i = i + sizeOf){
            int x = (i / sizeOf) % TILE_SIZE;
            int y = (i / sizeOf) / TILE_SIZE;
            ByteBuffer bb = getHeightFromBytes(data, sizeOf, i);
            short hauteur = bb.getShort(0);
            if(hauteur < 0){
                LOGGER.info("BAD HEIGHT : " + hauteur + " | (" + x + ", " + y + ") ");
                hauteur = 0;
            }
            //Apply coloration
            int rgb = LUT[hauteur];
            double intensity = normalMap[x + y * TILE_SIZE];

            if(hauteur == 0){
                intensity = 1;
                //LOGGER.info("intensity : " + intensity + " | (" + x + ", " + y + ") ");
            }
            Color c = new Color(rgb);
            int r = (int)(Math.min(255, Math.max(0, c.getRed() * intensity)));
            int g = (int)(Math.min(255, Math.max(0, c.getGreen() * intensity)));
            int b = (int)(Math.min(255, Math.max(0, c.getBlue() * intensity)));
            Color c1 = new Color(r, g, b);
            bi.setRGB(x, y, c1.getRGB());
        }
        return bi;
    }

    private ByteBuffer getHeightFromBytes(byte[] data, int sizeOf, int i) {
        ByteBuffer bb = ByteBuffer.allocate(sizeOf);
        for(int j = 0; j < sizeOf; ++j){
            bb.put(data[i + j]);
        }
        return bb;
    }
}
