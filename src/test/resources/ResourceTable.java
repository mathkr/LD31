package test.resources;

import java.util.HashMap;
import java.util.Map;

public class ResourceTable {

    public Map<Resource, Float> resources;

    public ResourceTable(){
        resources = new HashMap<Resource,Float>();
    }

    public static ResourceTable getZeroTable(){
        ResourceTable res = new ResourceTable();
        for(Resource r : Resource.values())
            res.resources.put(r,0.0f);
        return res;
    }

    public boolean greaterOrEqual(ResourceTable other){
        for(Resource r : Resource.values())
            if(this.resources.get(r) < other.resources.getOrDefault(r, 0.0f))
                return false;
        return true;
    }

    public void put(Resource r, Float f){
        resources.put(r,f);
    }

    public void change(Resource r, Float f){
        resources.put(r, get(r) + f);
    }

    public float get(Resource r){
        return resources.getOrDefault(r, 0.0f);
    }

    public boolean canSubstract(Resource r, Float f){
        return resources.get(r) + f >= 0.0f;
    }
}
