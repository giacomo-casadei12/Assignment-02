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
        return this.userDA.updateUser(id, credit);
    }

    @Override
    public boolean updateUserRole(int id, boolean admin) {
        return this.userDA.updateUserRole(id, admin);
    }

    @Override
    public boolean deleteUser(int id) {
        return this.userDA.deleteUser(id);
    }

}
