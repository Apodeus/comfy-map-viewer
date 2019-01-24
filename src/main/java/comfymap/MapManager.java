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
import java.util.Arrays;
import java.util.zip.DataFormatException;

@Path("/")
@Singleton
public class MapManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(MapManager.class);
    private static final int TILE_SIZE = 1201;
    private HBaseDAO hBaseDAO;
    private final int[] LUT;
    private final byte[] waterImg;
    private final int shiftHeight; // to negative values for the lut

    //private final HBaseDAO hBaseDAO;

    public MapManager() throws IOException, DataFormatException {
        this.hBaseDAO = new HBaseDAO();

        //Test normal map ...
        LUT = initLUT();
        byte[] tmpWater = new byte[2 * TILE_SIZE * TILE_SIZE];
        Arrays.fill(tmpWater, (byte)0);
        for(int i = 1; i < tmpWater.length; i=i+2){
            tmpWater[i] = (byte) 200;
        }

        float[] tmpNormal = new float[TILE_SIZE * TILE_SIZE];
        Arrays.fill(tmpNormal, 1f);
        waterImg = getFileAsByte(tmpWater, tmpNormal);

        this.shiftHeight = AdrienColors.values()[0].getMaxHeight();
        //get normal map

        //float[] normalMap3 = getNormalMap("266056.jojo"); // todo fix it ...
        //this.tile2 = getFileAsByte(CompressionUtil.decompress(hBaseDAO.getCompressedTile(4, 1, 12, "tile")), normalMap3);
    }

    private float[] getNormalMap(int x, int y, int z) throws IOException, DataFormatException {
        float[] normalMap = new float[TILE_SIZE * TILE_SIZE];
        byte[] phongs = hBaseDAO.getCompressedTile(x, y, z, "phong");
        byte[] tmpNorm = CompressionUtil.decompress(phongs);
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
        int[] lut = new int[9400];
        int minMaxHeight = AdrienColors.values()[0].getMaxHeight();
        for(int i = 0; i < lut.length; ++i){
            lut[i] = AdrienColors.getRGBFromHeight(minMaxHeight + i);
        }
        return lut;
    }

    @GET
    @Path("/map/{z}/{x}/{y}")
    @Produces("image/png")
    public Response doStuff(@PathParam("x") int x, @PathParam("y") int y, @PathParam("z") int z) throws IOException, DataFormatException {
        z = 11 - z;
        byte[] tile = hBaseDAO.getCompressedTile(x, y, z, "tile");
        if(tile.length == 0){
            return Response.ok(waterImg).build();
        }
        float[] normalMap = getNormalMap(x, y, z);
        byte[] result = getFileAsByte(CompressionUtil.decompress(tile), normalMap);
        return Response.ok(result).build();
    }

    @GET
    @Path("/lut")
    @Produces("image/png")
    public Response getLUT() throws IOException {
        LOGGER.info("getting lut");
        int[] lut = this.LUT;
        BufferedImage bi = new BufferedImage(lut.length, 1, BufferedImage.TYPE_INT_RGB);
        for(int i = 0; i < lut.length; ++i){
            bi.setRGB(i, 0, lut[i]);
        }
        ByteArrayOutputStream bais = new ByteArrayOutputStream();
        ImageIO.write(bi, "png", bais);
        bais.flush();
        byte[] lutRes = bais.toByteArray();
        bais.close();
        return Response.ok(lutRes).build();
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
            short hauteur = (short)(bb.getShort(0) - shiftHeight);
            //LOGGER.info("hauteur : " + hauteur);
            //Apply coloration
            int rgb = LUT[hauteur];
            double intensity = normalMap[x + y * TILE_SIZE];

            if(hauteur <= 200){
                intensity = 1;
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
