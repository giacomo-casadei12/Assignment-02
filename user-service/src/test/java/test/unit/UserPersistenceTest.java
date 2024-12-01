package test.unit;
import org.junit.Test;
import sap.ass02.userservice.domain.entities.User;
import sap.ass02.userservice.domain.ports.dataAccessPorts.UserDA;
import sap.ass02.userservice.infrastructure.DataAccessL.UserDB;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class UserPersistenceTest {

    final UserDA userDA;

    public UserPersistenceTest() {
        userDA = new UserDB();
    }

    //there is always at least the admin
    @Test
    public void getAtLeastOneUser() {
        List<User> x = userDA.getAllUsers();
        assertFalse(x.isEmpty());
        assertTrue(x.getFirst().admin());
    }

    @Test
    public void getFirstUser() {
        User x = userDA.getUserById(1);
        assertNotNull(x);
        assertEquals(1, x.id());
    }

    @Test
    public void loginUser() {
        boolean x = userDA.login("GiacomoCasadei","password");
        assertFalse(x);
        x = userDA.login("GiacomoC","passwordStorta");
        assertFalse(x);
        x = userDA.login("GiacomoCasadei","passwordStorta");
        assertFalse(x);
        x = userDA.login("GiacomoC","password");
        assertTrue(x);
    }


    @Test
    public void createUpdateDeleteUser() {
        boolean b = userDA.createUser("Giangurgulo","Pulcinella");
        assertTrue(b);
        User u = userDA.getUserByName("Giangurgulo");
        assertNotNull(u);
        assertEquals("Giangurgulo", u.userName());
        int id = u.id();
        b = userDA.updateUser(id,200);
        assertTrue(b);
        u = userDA.getUserById(id);
        assertNotNull(u);
        assertEquals(200,u.credit());
        b = userDA.deleteUser(id);
        assertTrue(b);
        u = userDA.getUserById(id);
        assertNull(u);
    }

}


