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

@Path("/map")
@Singleton
public class MapManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(MapManager.class);
    private static final int TILE_SIZE = 1201;
    private final byte[] bl; // bottom left
    private final byte[] ul; // up left
    private final byte[] br; // bottom right
    private final byte[] ur; // up right
    private final HBaseDAO hBaseDAO;

    private final byte[] defaultTile;

    public MapManager() throws IOException, DataFormatException {
        this.hBaseDAO = new HBaseDAO();

        byte[] tmp = new byte[2 * TILE_SIZE * TILE_SIZE];
        Arrays.fill(tmp, (byte)0);

        defaultTile = getFileAsByte(tmp);

        ul = CompressionUtil.decompress(new FileInputStream(new File("N44W002.dio")).readAllBytes());
        ur = CompressionUtil.decompress(new FileInputStream(new File("N44W001.dio")).readAllBytes());
        br = CompressionUtil.decompress(new FileInputStream(new File("N43W001.dio")).readAllBytes());
        bl = CompressionUtil.decompress(new FileInputStream(new File("N43W002.dio")).readAllBytes());
    }

    private byte[] generateTest() {
        byte[] res = new byte[TILE_SIZE * TILE_SIZE];
        for(int x = 0; x < TILE_SIZE; ++x){
            for(int y = 0; y < TILE_SIZE; ++y){
                int c = ((x + y) * 256) / (2* TILE_SIZE);
                res[x + y * TILE_SIZE] = (byte) c;
            }
        }
        return res;
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

        byte[] resHBase = hBaseDAO.getCompressedTile(x, y, 0);
        if(resHBase.length == 0){
            result = defaultTile;
        } else {
            try {
                result = getFileAsByte(CompressionUtil.decompress(resHBase));
            } catch (DataFormatException e) {
                LOGGER.info(e.getMessage());
                result = defaultTile;
            }
        }

        return Response.ok(result).build();
    }

    //Generate png files as byte array
    private byte[] getFileAsByte(byte[] data) throws IOException {
        byte[] resp;
        ByteArrayOutputStream bais = new ByteArrayOutputStream();
        ImageIO.write(colorizeMap(data), "png", bais);
        bais.flush();
        resp = bais.toByteArray();
        bais.close();
        return resp;
    }

    private BufferedImage colorizeMap(byte[] data){
        BufferedImage bi = new BufferedImage(TILE_SIZE, TILE_SIZE, BufferedImage.TYPE_INT_RGB);
        int max = 0;
        int min = 10;
        int sizeOf = 2; //size of Short

        for (int i = 0; i < data.length; i = i + sizeOf){
            int x = (i / sizeOf) % TILE_SIZE;
            int y = (i / sizeOf) / TILE_SIZE;
            ByteBuffer bb = getHeightFromBytes(data, sizeOf, i);
            short hauteur = bb.getShort(0);
            // MAX
            if(max < hauteur){
                max = hauteur;
            }
            // MIN
            if(min > hauteur){
                min = hauteur;
            }
            if(hauteur < 0){
                LOGGER.info("BAD HEIGHT : " + hauteur + " | (" + x + ", " + y + ") ");
            }
            //Apply coloration
            bi.setRGB(x, y, clamp2(hauteur));
        }
        LOGGER.info("Max is : " + max);
        LOGGER.info("Min is : " + min);
        return bi;
    }

    //Detect bad pixels

    private int clamp3(int height){
        if(height < 0){
            return new Color(255, 0, 0).getRGB();
        }
        return new Color(0, 0, 0).getRGB();
    }
    //Adrien Coloration

    private int clamp2(int height){
        int waterlevelCap = 0;
        int beachlevelCap = 15;
        int mountainlevelCap = 80;
        int highmountainlevelCap = 2500;
        int snowmountainlevelCap = 5000;

        int lowCap = waterlevelCap;
        int topCap = beachlevelCap;
        float hueBase = 240f / 360f;
        float hueVariation = 0;
        float satBase = 1f;
        float satVariation = 0;
        float lumBase = 0.2f;
        float lumVariation = 0;

        if (height > waterlevelCap && height < beachlevelCap){
            lowCap = waterlevelCap;
            topCap = beachlevelCap;
            hueBase = 40f/360f;
            satBase = 1f;
            lumBase = 0.7f;
            lumVariation = -0.5f;
            hueVariation = 80f/360f;
        } else if (height >= beachlevelCap && height < mountainlevelCap) {
            lowCap = beachlevelCap;
            topCap = mountainlevelCap;
            hueBase = 120f/360f;
            satBase = 1f;
            lumBase = 0.2f;
            lumVariation = 0f;
            hueVariation = -90f/360f;
        }else if (height >= mountainlevelCap && height < highmountainlevelCap){
            lowCap = mountainlevelCap;
            topCap = highmountainlevelCap;
            hueBase = 30f/360f;
            satBase = 1f;
            lumBase = 0.2f;
            lumVariation = 0.7f;
            hueVariation = 0f;

        }else if (height >= highmountainlevelCap && height < snowmountainlevelCap) {
            lowCap = highmountainlevelCap;
            topCap = snowmountainlevelCap;
            lumBase = 0.9f;
            satBase = 0f;
            hueBase = 30f/360f;
            lumVariation = 0.1f;
        }

        float hueRatio = 1f / (topCap - lowCap) * hueVariation;
        float satRatio = 1f / (topCap - lowCap) * satVariation;
        float lumRatio = 1f / (topCap - lowCap) * lumVariation;

        float hue = hueBase + (height - lowCap) * hueRatio;
        float sat = satBase + (height - lowCap) * satRatio;
        float lum = lumBase + (height - lowCap) * lumRatio;

        return Color.HSBtoRGB(hue, sat, lum);
    }


    private ByteBuffer getHeightFromBytes(byte[] data, int sizeOf, int i) {
        ByteBuffer bb = ByteBuffer.allocate(sizeOf);
        for(int j = 0; j < sizeOf; ++j){
            bb.put(data[i + j]);
        }
        return bb;
    }
}
