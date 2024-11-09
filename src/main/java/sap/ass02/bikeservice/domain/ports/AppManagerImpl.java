package sap.ass02.bikeservice.domain.ports;

import sap.ass02.bikeservice.domain.BusinessLogicL.PersistenceNotificationService;
import sap.ass02.bikeservice.domain.entities.EBike;
import sap.ass02.bikeservice.domain.entities.EBikeImpl;
import sap.ass02.bikeservice.domain.ports.dataAccessPorts.EBikeDA;
import sap.ass02.bikeservice.utils.EBikeState;

import java.util.List;

/**
 * The implementation of the AppManager interface.
 */
public class AppManagerImpl implements AppManager, PersistenceNotificationService {

    private final EBikeDA bikeDA;

    /**
     * Instantiates a new App Manager
     *
     * @param bikeDA      the persistence abstraction for bikes
     */
    public AppManagerImpl(EBikeDA bikeDA) {
        this.bikeDA = bikeDA;
    }

    @Override
    public List<EBike> getAllEBikes(int positionX, int positionY, boolean available) {
        List<EBike> res;

        if (available) {
            res = this.bikeDA.getAllAvailableEBikes();
        } else if (positionX > 0 || positionY > 0) {
            res = this.bikeDA.getAllEBikesNearby(positionX, positionY);
        } else {
            res = this.bikeDA.getAllEBikes();
        }

        return res;
    }

    @Override
    public EBike getEBike(int id) {
        return this.bikeDA.getEBikeById(id);
    }

    @Override
    public boolean createEBike(int positionX, int positionY) {
        return this.bikeDA.createEBike(positionX, positionY);
    }

    @Override
    public boolean updateEBike(int id, int battery, EBikeState state, int positionX, int positionY) {
        EBike bike = new EBikeImpl();
        bike.setBattery(battery);
        bike.setId(id);
        bike.setPositionX(positionX);
        bike.setPositionY(positionY);
        bike.setState(state.toString());
        return this.bikeDA.updateEBike(bike);
    }

    @Override
    public boolean updateEbikePosition(int id, int positionX, int positionY) {
        EBike bike = this.bikeDA.getEBikeById(id);
        return this.bikeDA.updateEBike(bike);
    }

    @Override
    public boolean deleteEBike(int id) {
        return this.bikeDA.deleteEBike(id);
    }

}
