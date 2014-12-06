package test.resources;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ResourceTable {

    public Map<Resource, Integer> resources;

    private ResourceTable(){
        resources = new HashMap<Resource,Integer>();
        for(Resource r : Resource.values())
            resources.put(r,0);
    }

    public ResourceTable getEmptyTable(){
        return new ResourceTable();
    }

    public boolean greaterOrEqual(ResourceTable other){
        for(Resource r : Resource.values())
            if(this.resources.get(r) < other.resources.get(r))
                return false;
        return true;
    }
}
