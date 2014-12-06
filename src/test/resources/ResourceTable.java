package test.resources;

import java.util.HashMap;
import java.util.Map;

public class ResourceTable {

    public Map<Resource, Float> resources;

    public ResourceTable(){
        resources = new HashMap<Resource,Float>();
    }

    public boolean greaterOrEqual(ResourceTable other){
        for(Resource r : Resource.values())
            if(this.get(r) < other.get(r))
                return false;
        return true;
    }

    public void add(ResourceTable other){
        other.resources.forEach(this::change);
    }

    public void subtract(ResourceTable other){
        other.resources.forEach((res, val) -> this.change(res, -val));
    }

    public void put(Resource r, Float f){
        resources.put(r,f);
    }

    public void putAll(ResourceTable other){
        resources.putAll(other.resources);
    }

    public void change(Resource r, Float f){
        resources.put(r, get(r) + f);
    }

    public float get(Resource r){
        return resources.getOrDefault(r, 0.0f);
    }

    public boolean canSubstract(Resource r, Float f){
        return get(r) >= f;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        resources.forEach((res, val) -> sb.append(res + ": " + val + ", "));
        return sb.toString();
    }
}
