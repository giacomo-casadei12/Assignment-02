package sap.ass02.userservice.domain.ports;

import sap.ass02.userservice.domain.BusinessLogicL.PersistenceNotificationService;
import sap.ass02.userservice.domain.entities.User;
import sap.ass02.userservice.domain.ports.dataAccessPorts.UserDA;

import java.util.List;

/**
 * The implementation of the AppManager interface.
 */
public class AppManagerImpl implements AppManager, PersistenceNotificationService {

    private final UserDA userDA;
    private ResourceNotification resourceNotification;

    /**
     * Instantiates a new App Manager
     *
     * @param userDA      the persistence abstraction for users
     */
    public AppManagerImpl(UserDA userDA) {
        this.userDA = userDA;
    }

    @Override
    public List<User> getAllUsers() {
        return this.userDA.getAllUsers();
    }

    @Override
    public User getUser(int id, String userName) {
        return id != 0 ? this.userDA.getUserById(id) :
                this.userDA.getUserByName(userName);
    }

    @Override
    public boolean login(String userName, String password) {
        return this.userDA.login(userName, password);
    }

    @Override
    public boolean createUser(String userName, String password) {
        return this.userDA.createUser(userName, password);
    }

    @Override
    public boolean updateUser(int id, int credit) {
        boolean result = this.userDA.updateUser(id, credit);
        if (this.resourceNotification != null) {
            this.resourceNotification.spreadUserChange(id, credit);
        }
        return result;
    }

    @Override
    public boolean updateUserRole(int id, boolean admin) {
        return this.userDA.updateUserRole(id, admin);
    }

    @Override
    public boolean deleteUser(int id) {
        return this.userDA.deleteUser(id);
    }

    @Override
    public void attachResourceNotification(ResourceNotification resource) {
        this.resourceNotification = resource;
    }
}
