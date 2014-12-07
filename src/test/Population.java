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

    Float electronProdPerSec;
    Float deltaElectronProdPerSec;

    Float electronDecayPerSec;
    Float deltaElectronDecayPerSec;

    public Population(){
        costElectron = new ResourceTable();
        costPhoton = new ResourceTable();
        costQuantum = new ResourceTable();

        costElectron.put(Resource.COPPER, 2.5F);
        costPhoton.put(Resource.SILVER, 0.5F);
        costQuantum.put(Resource.GLASS, 1.00F);

        deltaElectron = new ResourceTable();
        deltaPhoton = new ResourceTable();
        deltaQuantum = new ResourceTable();

        electronProdPerSec = 0.5F;
        deltaElectronProdPerSec = 0F;

        electronDecayPerSec = 0.5F;
        deltaElectronDecayPerSec = 0F;

    }

    public void update(float time){
        ResourceTable globalResources = Game.world.resources;
        // Deltas Berrechnen

        float cost = (globalResources.get(Resource.ELECTRON) + 1) * time;
        Resource res = Resource.COPPER;
        if(deltaElectron.get(res) > -1 && deltaElectron.get(res) < 1){
            if(globalResources.get(res) - costElectron.get(res).intValue() + 1 > 0){
                deltaElectron.change(res, costElectron.get(res) * cost );
                deltaElectronProdPerSec += time;
            } else {
                deltaElectron.change(res, -costElectron.get(res) * cost);
                deltaElectronDecayPerSec += time;
            }
        }

        if(deltaElectron.get(res) > 1){
            globalResources.change(res, -deltaElectron.get(res).intValue());
            deltaElectron.change(res, -deltaElectron.get(res).intValue());
        } else if(deltaElectron.get(res) < -1) {
            deltaElectron.change(res, -deltaElectron.get(res).intValue());
        }

        if(Game.world.resourceCapacity.get(Resource.ELECTRON) > globalResources.get(Resource.ELECTRON)){
            while(deltaElectronProdPerSec > electronProdPerSec){
                deltaElectronProdPerSec -= electronProdPerSec;
                globalResources.change(Resource.ELECTRON, 1);
            }
        }
        if(globalResources.get(Resource.ELECTRON) > 0) {
            while (deltaElectronDecayPerSec > electronDecayPerSec) {
                deltaElectronDecayPerSec -= electronDecayPerSec;
                globalResources.change(Resource.ELECTRON, -1);
            }
        }








    }
}
