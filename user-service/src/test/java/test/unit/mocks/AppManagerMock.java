package test.unit.mocks;

import sap.ass02.userservice.domain.entities.User;
import sap.ass02.userservice.domain.entities.UserImpl;
import sap.ass02.userservice.domain.ports.AppManager;
import sap.ass02.userservice.domain.ports.ResourceNotification;

import java.util.ArrayList;
import java.util.List;

public class AppManagerMock implements AppManager {

    User mockUser = new UserImpl();

    public AppManagerMock() {
        mockUser.setId(1);
        mockUser.setId(1);
        mockUser.setName("test");
        mockUser.setIsAdmin(true);
    }

    @Override
    public void attachResourceNotification(ResourceNotification resource) {

    }

    @Override
    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        users.add(mockUser);
        return users;
    }

    @Override
    public User getUser(int id, String userName) {
        return mockUser;
    }

    @Override
    public boolean login(String userName, String password) {
        return true;
    }

    @Override
    public boolean createUser(String userName, String password) {
        return true;
    }

    @Override
    public boolean updateUser(int id, int credit) {
        return true;
    }

    @Override
    public boolean updateUserRole(int id, boolean admin) {
        return true;
    }

    @Override
    public boolean deleteUser(int id) {
        return true;
    }
}
