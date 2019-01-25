package comfymap;

import exception.TileNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import javax.inject.Singleton;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.zip.DataFormatException;

@Path("/")
@Singleton
public class MapManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(MapManager.class);
    private static final int TILE_SIZE = 1201;
    public static final int MAX_ZOOM_LEAFLET = 11;
    private final float[] fakeNormalMap;
    private HBaseDAO hBaseDAO;
    private final int[] LUT;
    private final byte[] waterImg;
    private final int shiftHeight; // to negative values for the lut

    public MapManager() throws IOException {
        /// ---- NEW
        this.shiftHeight = Math.abs(AdrienColors.values()[0].getMaxHeight());
        this.hBaseDAO = new HBaseDAO();

        this.LUT = initLUT();

        this.fakeNormalMap = new float[TILE_SIZE * TILE_SIZE];
        Arrays.fill(fakeNormalMap, 1f);

        short[] tmpWater = new short[TILE_SIZE * TILE_SIZE];
        Arrays.fill(tmpWater, (short)0);

        this.waterImg = writeAsImage(colorize(tmpWater));
    }

    private int[] initLUT() {
        AdrienColors[] values = AdrienColors.values();
        int minMaxHeight = values[0].getMaxHeight();
        int maxHeight = values[values.length - 1].getMaxHeight();
        int[] lut = new int[Math.abs(minMaxHeight) + maxHeight + 1];
        for(int i = 0; i < lut.length; ++i){
            lut[i] = AdrienColors.getRGBFromHeight(minMaxHeight + i);
        }
        return lut;
    }

    @GET
    @Path("/map/{z}/{x}/{y}")
    @Produces("image/png")
    public Response getImageTile(@PathParam("x") int x, @PathParam("y") int y, @PathParam("z") int z) throws IOException, DataFormatException {
        z = MAX_ZOOM_LEAFLET - z; // Conversion of requested Zoom level to our Zoom level system
        return Response.ok(composeImage(x, y, z)).build();
    }

    @GET
    @Path("/oldMap/{z}/{x}/{y}")
    @Produces("image/png")
    public Response getOldImageTile(@PathParam("x") int x, @PathParam("y") int y, @PathParam("z") int z) throws IOException, DataFormatException {
        z = MAX_ZOOM_LEAFLET - z; // Conversion of requested Zoom level to our Zoom level system
        return Response.ok(composeOldImage(x, y, z)).build();
    }

    @GET
    @Path("/toto")
    public Response getAsBinary() throws IOException, DataFormatException, TileNotFoundException {
        byte[] tile = hBaseDAO.getCompressedBytes(1, 0, 7, "tile");
        byte[] decompressedTile = CompressionUtil.decompress(tile);
        return Response.ok(decompressedTile).build();
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
        return Response.ok(writeAsImage(bi)).build();
    }

    private byte[] composeImage(int x, int y, int z) throws IOException, DataFormatException {
        byte[] heightMapAsByte;
        byte[] normalMapAsByte;
        try {
            heightMapAsByte = getDecompressedDatas(x, y, z, "tile");
            normalMapAsByte = getDecompressedDatas(x, y, z, "phong");
        } catch (TileNotFoundException e) {
            return waterImg;
        }
        short[] heightMap = getHeightMap(heightMapAsByte);
        float[] normalMap = getNormalMap(normalMapAsByte);
        return writeAsImage(colorize(heightMap, normalMap));
    }

    private byte[] composeOldImage(int x, int y, int z) throws IOException, DataFormatException {
        byte[] heightMapAsByte;
        try {
            heightMapAsByte = getDecompressedDatas(x, y, z, "tile");
        } catch (TileNotFoundException e) {
            return waterImg;
        }
        short[] heightMap = getHeightMap(heightMapAsByte);
        return writeAsImage(colorize(heightMap));
    }

    private byte[] getDecompressedDatas(int x, int y, int z, String qualifier) throws IOException, DataFormatException, TileNotFoundException {
        byte[] compressedBytes = hBaseDAO.getCompressedBytes(x, y, z, qualifier);
        return CompressionUtil.decompress(compressedBytes);
    }

    private float[] getNormalMap(byte[] data){
        float[] normalMap = new float[TILE_SIZE * TILE_SIZE];
        for(int i = 0; i < normalMap.length; ++i){
            float factor = getValueFromBytes(data, 4, i).getFloat(0);
            normalMap[i] = factor;
        }
        return normalMap;
    }

    private short[] getHeightMap(byte[] data){
        short[] heightMap = new short[TILE_SIZE * TILE_SIZE];
        for(int i = 0; i < heightMap.length; ++i){
            short height = getValueFromBytes(data, 2, i).getShort(0);
            heightMap[i] = height;
        }
        return heightMap;
    }

    private ByteBuffer getValueFromBytes(byte[] array, int sizeOf, int index){
        ByteBuffer bb = ByteBuffer.allocate(sizeOf);
        for(int j = index * sizeOf; j < index*sizeOf + sizeOf; ++j){
            bb.put(array[j]);
        }
        return bb;
    }

    private byte[] writeAsImage(BufferedImage bi) throws IOException {
        ByteArrayOutputStream bais = new ByteArrayOutputStream();
        ImageIO.write(bi, "png", bais);
        bais.flush();
        byte[] image = bais.toByteArray();
        bais.close();
        return image;
    }

    private BufferedImage colorize(short[] heightMap, float[] normalMap){
        BufferedImage bufferedImage = new BufferedImage(TILE_SIZE, TILE_SIZE, BufferedImage.TYPE_INT_RGB);
        for(int y = 0; y < TILE_SIZE; ++y){
            int col = y * TILE_SIZE;
            for(int x = 0; x < TILE_SIZE; ++x){
                int index = x + col;
                short height = heightMap[index];
                short height4Lut = (short)(height + shiftHeight);
                int rgb = this.LUT[height4Lut];
                double intensity = normalMap[index];
                if(height == 0){
                    intensity = 1;
                }
                Color lutColor = new Color(rgb);
                int r = (int)(Math.min(255, Math.max(0, lutColor.getRed() * intensity)));
                int g = (int)(Math.min(255, Math.max(0, lutColor.getGreen() * intensity)));
                int b = (int)(Math.min(255, Math.max(0, lutColor.getBlue() * intensity)));
                Color colorWithNormal = new Color(r, g, b);
                bufferedImage.setRGB(x, y, colorWithNormal.getRGB());
            }
        }
        return bufferedImage;
    }

    private BufferedImage colorize(short[] heightMap){
        return colorize(heightMap, fakeNormalMap);
    }
}
