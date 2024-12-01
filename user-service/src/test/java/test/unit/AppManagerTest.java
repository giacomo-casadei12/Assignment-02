package test.unit;

import org.junit.BeforeClass;
import org.junit.Test;
import sap.ass02.userservice.domain.entities.User;
import sap.ass02.userservice.domain.ports.AppManager;
import sap.ass02.userservice.domain.ports.AppManagerImpl;
import sap.ass02.userservice.domain.ports.dataAccessPorts.UserDA;
import test.unit.mocks.UserDBMock;

import java.util.List;

import static org.junit.Assert.*;

public class AppManagerTest {

    private static AppManager appManager;

    @BeforeClass
    public static void setUpBeforeClass() {
        UserDA uda = new UserDBMock();
        appManager = new AppManagerImpl(uda);
    }

    @Test
    public void testGetAllUsers() {
        List<User> uList = appManager.getAllUsers();
        assertFalse(uList.isEmpty());
    }

    @Test
    public void testLogin() {
        boolean result = appManager.login("admin", "admin");
        assertTrue(result);
    }
    @Test
    public void testGetUser() {
        User user = appManager.getUser(1,"");
        assertNotNull(user);
        user = appManager.getUser(0,"test");
        assertNotNull(user);
    }
    @Test
    public void testCreateUser() {
        boolean result = appManager.createUser("admin", "admin");
        assertTrue(result);
    }
    @Test
    public void testUpdateUser() {
        boolean result = appManager.updateUser(1,10);
        assertTrue(result);
    }
    @Test
    public void testUpdateUserRole() {
        boolean result = appManager.updateUserRole(1, true);
        assertTrue(result);
    }
    @Test
    public void testDeleteUser() {
        boolean result = appManager.deleteUser(1);
        assertTrue(result);
    }

}
