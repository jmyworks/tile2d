package koumuu.game.engine;

/*
 * 0             0                       000000 00000000 00000000 00000000
 * sign flag     collisional flag        image index
 * */
public class TileData {
    public int encodedData;
    public int imageIndex;
    public boolean isCollisional;

    public TileData(int encodedData) {
        this.encodedData = encodedData;
        this.imageIndex = encodedData & 0x3FFFFFFF;
        this.isCollisional = (encodedData >>> 30) == 1;
    }

    public TileData(int index, boolean isCollisional) {
        this.imageIndex = index;
        this.isCollisional = isCollisional;
        this.encodedData = index | (isCollisional ? 0x40000000 : 0);
    }
}
