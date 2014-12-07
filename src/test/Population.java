package test;

import test.resources.Resource;
import test.resources.ResourceTable;

import java.util.Map;

/**
 * Created by msk on 07.12.14.
 */
public class Population {

    ResourceTable costElectron;
    ResourceTable costPhoton;
    ResourceTable costQuantum;

    ResourceTable deltaElectron;
    ResourceTable deltaPhoton;
    ResourceTable deltaQuantum;

    public Population(){
        costElectron = new ResourceTable();
        costPhoton = new ResourceTable();
        costQuantum = new ResourceTable();

        costElectron.put(Resource.COPPER,0.1F);
        costPhoton.put(Resource.SILVER, 0.25F);
        costQuantum.put(Resource.GLASS, 0.66F);

        deltaElectron = new ResourceTable();
        deltaPhoton = new ResourceTable();
        deltaQuantum = new ResourceTable();

    }

    public void update(float time){
        ResourceTable globalResources = Game.world.resources;
        // Deltas Berrechnen

        float cost = (globalResources.get(Resource.ELECTRON) + 1) * time;
        Resource res = Resource.COPPER;
        if(deltaElectron.get(res) > -1 && deltaElectron.get(res) < 1){
            if(globalResources.get(res) - costElectron.get(res).intValue() + 1 > 0){
                deltaElectron.change(res, costElectron.get(res) * cost );
            } else {
                deltaElectron.change(res, -costElectron.get(res) * cost);
            }
        }

        if(deltaElectron.get(res) > 1){
            globalResources.change(res, -deltaElectron.get(res).intValue());
            deltaElectron.change(res, -deltaElectron.get(res).intValue());
        } else if(deltaElectron.get(res) < -1) {
            deltaElectron.change(res, -deltaElectron.get(res).intValue());
        }





    }
}
