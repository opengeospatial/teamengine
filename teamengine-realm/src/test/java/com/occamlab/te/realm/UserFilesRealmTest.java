package com.occamlab.te.realm;

import java.util.ArrayList;
import java.util.List;
import org.apache.catalina.realm.GenericPrincipal;
import org.junit.Assert;
import org.junit.Test;

/**
 * Verifies the behavior of UserFilesRealm.
 */
public class UserFilesRealmTest {

    private static final String ROLE_1 = "role-1";

    @Test
    public void verifyCreateGenericPrincipal() {
        String username = "user-1";
        String password = "password-1";
        List<String> roles = new ArrayList<String>();
        roles.add(ROLE_1);
        UserFilesRealm iut = new UserFilesRealm();
        GenericPrincipal result = iut.createGenericPrincipal(username,
                password, roles);
        Assert.assertNotNull(result);
        Assert.assertEquals("Unexpected username", username, result.getName());
        Assert.assertEquals("Unexpected password", password,
                result.getPassword());
        Assert.assertTrue("Expected principal to have role " + ROLE_1,
                result.hasRole(ROLE_1));
    }
}
