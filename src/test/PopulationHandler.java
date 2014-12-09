package test;

import test.resources.Resource;
import test.resources.ResourceTable;

import java.util.HashMap;

/**
 * Created by msk on 07.12.14.
 */
public class PopulationHandler {

    HashMap<Resource, ResourceTable> sustainCost;
    HashMap<Resource, ResourceTable> sustainCostDelta;
    HashMap<Resource, Boolean> isGrowing;
    HashMap<Resource, Boolean> couldNotPayLastUpdate;
    ResourceTable populationChangeDelta;
    ResourceTable growthPerSecond;
    ResourceTable decayPerSecond;

    public PopulationHandler(){
        sustainCost = new HashMap<Resource, ResourceTable>(5);
        sustainCostDelta = new HashMap<Resource, ResourceTable>(5);
        isGrowing = new HashMap<Resource, Boolean>(5);
        couldNotPayLastUpdate = new HashMap<Resource, Boolean>(5);
        for(Resource res : new Resource[]{Resource.ELECTRON, Resource.PHOTON, Resource.QUANTUM}) {
            isGrowing.put(res, false);
            couldNotPayLastUpdate.put(res, false);
        }
        populationChangeDelta = new ResourceTable();
        growthPerSecond = new ResourceTable();
        decayPerSecond = new ResourceTable();

        ResourceTable electronSustainCost = new ResourceTable();
        sustainCost.put(Resource.ELECTRON, electronSustainCost);
        ResourceTable photonSustainCost = new ResourceTable();
        sustainCost.put(Resource.PHOTON, photonSustainCost);
        ResourceTable quantumSustainCost = new ResourceTable();
        sustainCost.put(Resource.QUANTUM, quantumSustainCost);

        ResourceTable electronSustainCostDelta = new ResourceTable();
        sustainCostDelta.put(Resource.ELECTRON, electronSustainCostDelta);
        ResourceTable photonSustainCostDelta = new ResourceTable();
        sustainCostDelta.put(Resource.PHOTON, photonSustainCostDelta);
        ResourceTable quantumSustainCostDelta = new ResourceTable();
        sustainCostDelta.put(Resource.QUANTUM, quantumSustainCostDelta);

        electronSustainCost.put(Resource.SOUND, 0.02f);
        photonSustainCost.put(Resource.GRAPHICS, 0.04f);
        quantumSustainCost.put(Resource.BITCOINS, 0.08f);

        growthPerSecond.put(Resource.ELECTRON, 1.0f);
        decayPerSecond.put(Resource.ELECTRON, 1.0f);
        growthPerSecond.put(Resource.PHOTON, 0.3f);
        decayPerSecond.put(Resource.PHOTON, 0.3f);
        growthPerSecond.put(Resource.QUANTUM, 0.1f);
        decayPerSecond.put(Resource.QUANTUM, 0.1f);
    }

    public void update(float d){
        update(d, Resource.ELECTRON);
        update(d, Resource.PHOTON);
        update(d, Resource.QUANTUM);
    }

    private void update(float d, Resource popType){

        if(Game.world.resources.get(popType) == 0.0f){
            for(Resource resource : Resource.values())
                if(Game.world.resources.get(resource) == 0.0f && sustainCost.get(popType).get(resource) > 0.0f) {
//                    System.out.println("doesn't need to pay : standby");
                    return;
                }
            isGrowing.put(popType, true);
        }

        if(!needsToPay(popType)) {
            couldNotPayLastUpdate.put(popType, false);
            incrementSustainCostDelta(d, popType);
            if(isGrowing.get(popType)) {
//                System.out.println("doesn't need to pay : grow");
                grow(d, popType);
            }else {
                decay(d, popType);
//                System.out.println("doesn't need to pay : decay");
            }
        }

        else if(canPay(popType)){
            pay(popType);
            incrementSustainCostDelta(d, popType);
            if(couldNotPayLastUpdate.get(popType)) {
//                System.out.println("needs to pay & can pay : decay");
                decay(d, popType);
            }else{
//                System.out.println("needs to pay & can pay : grow");
                isGrowing.put(popType, true);
                grow(d, popType);
            }
        }

        else{
//            System.out.println("needs to pay & can't pay : decay");
            isGrowing.put(popType, false);
            couldNotPayLastUpdate.put(popType, true);
            decay(d, popType);
        }
    }

    private void incrementSustainCostDelta(float d, Resource popType){
        ResourceTable resources = Game.world.resources;
        ResourceTable rSustainCost = sustainCost.get(popType);
        ResourceTable rSustainCostDelta = sustainCostDelta.get(popType);
        for (Resource res : Resource.values())
            rSustainCostDelta.change(res, resources.get(popType) * rSustainCost.get(res) * d);
    }

    private void pay(Resource popType){
        ResourceTable resources = Game.world.resources;
        ResourceTable rSustainCostDelta = sustainCostDelta.get(popType);
        for(Resource res : Resource.values()){
            float rDelta = rSustainCostDelta.get(res).intValue();
            resources.change(res, -rDelta);
            rSustainCostDelta.change(res, -rDelta);
        }
    }

    private boolean canPay(Resource popType){
        ResourceTable rSustainCostDelta = sustainCostDelta.get(popType);
        for(Resource res : Resource.values())
            if(!Game.world.resources.canSubtract(res, rSustainCostDelta.get(res).intValue()))
                return false;
        return true;
    }

    private boolean needsToPay(Resource popType){
        ResourceTable rSustainCostDelta = sustainCostDelta.get(popType);
        for(Resource res : Resource.values())
            if(rSustainCostDelta.get(res).intValue() >= 1.0f)
                return true;
        return false;
    }

    private void grow(float d, Resource popType){
        ResourceTable resources = Game.world.resources;
        ResourceTable cap = Game.world.resourceCapacity;
        if(resources.get(popType) < cap.get(popType)){
            populationChangeDelta.change(popType, growthPerSecond.get(popType) * d);
            float rDelta = populationChangeDelta.get(popType).intValue();
            if(rDelta >= 1.0f){
                //TODO: proper capacity check
                resources.change(popType, rDelta);
                populationChangeDelta.change(popType, -rDelta);
            }
        }
    }

    private void decay(float d, Resource popType){
        ResourceTable resources = Game.world.resources;
        populationChangeDelta.change(popType, -decayPerSecond.get(popType) * d);
        float rDelta = populationChangeDelta.get(popType).intValue();
        if(rDelta <= -1.0f){
            float rRemove = Math.min(-rDelta, resources.get(popType));
            resources.change(popType, -rRemove);
            populationChangeDelta.change(popType, -rDelta);
        }
    }
}
