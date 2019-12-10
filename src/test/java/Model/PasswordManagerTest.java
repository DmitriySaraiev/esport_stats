package Model;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class PasswordManagerTest {

    public PasswordManager passwordManager;

    @Before
    public void initPasswordManager(){
        passwordManager = new PasswordManager();
    }

    @Test
    public void getDatabasePassword() {
        String expectedDatabasePassword = "";
        String actualDatabasePassword = passwordManager.getDbPassword();
        Assert.assertEquals(expectedDatabasePassword, actualDatabasePassword);
    }

    @Test
    public void getDatabaseLogin() {
        String expectedDatabaseLogin = "";
        String actualDatabaseLogin = passwordManager.getDbLogin();
        Assert.assertEquals(expectedDatabaseLogin, actualDatabaseLogin);
    }

    @Test
    public void getServerIP() {
        String expectedServerIP ="";
        String actualServerIP = passwordManager.getServerIP();
        Assert.assertEquals(expectedServerIP, actualServerIP);
    }

}
