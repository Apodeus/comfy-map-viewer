package exception;

public class TileNotFoundException extends Exception {
    public TileNotFoundException(String msg){
        super(msg);
    }

    public TileNotFoundException(){
        super();
    }
}
