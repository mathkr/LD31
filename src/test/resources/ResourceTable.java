package test.resources;

import java.util.HashMap;
import java.util.Map;

public class ResourceTable {

    public Map<Resource, Float> resources;

    public ResourceTable(){
        resources = new HashMap<>();
    }

    public boolean greaterOrEqual(ResourceTable other){
        for(Resource r : Resource.values())
            if(this.get(r) < other.get(r))
                return false;
        return true;
    }

    public void add(ResourceTable other){
        for (Map.Entry<Resource, Float> entry : other.resources.entrySet()) {
            Resource res = entry.getKey();
            float val = entry.getValue();
            this.change(res, val);
        }
    }

    public void subtract(ResourceTable other){
        for (Map.Entry<Resource, Float> entry : other.resources.entrySet()) {
            Resource res = entry.getKey();
            float val = entry.getValue();
            this.change(res, -val);
        }
    }

    public void put(Resource r, float f){
        resources.put(r,f);
    }

    public void putAll(ResourceTable other){
        resources.putAll(other.resources);
    }

    public void multiply(Resource r, float f) { resources.put(r, get(r) * f); }

    public ResourceTable getMultiple(float f) {
        ResourceTable res = new ResourceTable();
        for (Resource resource : Resource.values()) {
            res.put(resource, get(resource) * f);
        }
        return res;
    }

    public void truncateToInt() {
        for (Resource resource : Resource.values()) {
            put(resource, get(resource).intValue());
        }
    }

    public void change(Resource r, float f){
        resources.put(r, get(r) + f);
    }

    public Float get(Resource r){
        Float res = resources.get(r);
        return res == null ? 0.0f : res;
    }

    public boolean canSubtract(Resource r, float f) { return get(r) >= f; }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<Resource, Float> entry : resources.entrySet()) {
            Resource res = entry.getKey();
            float val = entry.getValue();
            sb.append(res + ": " + val + ", ");
        }
        return sb.toString();
    }
}
