package test;

public class ResourceTable {

    public int copper;
    public int silver;
    public int glass;
    public int silicon;
    public int energy;

    public boolean greaterOrEqual(ResourceTable other){
        if(copper < other.copper
                || silver < other.silver
                || glass < other.glass
                || silicon < other.silicon
                || energy < other.energy)
            return false;
        return true;
    }
}
