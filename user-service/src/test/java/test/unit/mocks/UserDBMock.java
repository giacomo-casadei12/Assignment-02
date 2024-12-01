package test.unit.mocks;

import sap.ass02.userservice.domain.entities.User;
import sap.ass02.userservice.domain.entities.UserImpl;
import sap.ass02.userservice.domain.ports.dataAccessPorts.UserDA;

import java.util.ArrayList;
import java.util.List;

public class UserDBMock implements UserDA {

    User user;
    List<User> users;

    public UserDBMock() {
        this.user = new UserImpl();
        this.users = new ArrayList<>();
        this.user.setId(1);
        this.user.setName("John Doe");
        this.user.setIsAdmin(false);
        this.user.setCredit(1);
        this.users.add(user);
    }

    @Override
    public List<User> getAllUsers() {
        return users;
    }

    @Override
    public User getUserByName(String userName) {
        return user;
    }

    @Override
    public User getUserById(int id) {
        return user;
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
