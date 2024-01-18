/*
 * The Open Geospatial Consortium licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 */
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

    @SuppressWarnings("deprecation")
    @Test
    public void verifyCreateGenericPrincipal() {
        String username = "user-1";
        String password = "password-1";
        List<String> roles = new ArrayList<>();
        roles.add(ROLE_1);
        UserFilesRealm iut = new UserFilesRealm();
        GenericPrincipal result = iut.createGenericPrincipal(username, roles);
        Assert.assertNotNull(result);
        Assert.assertEquals("Unexpected username", username, result.getName());
        Assert.assertTrue("Expected principal to have role " + ROLE_1, result.hasRole(ROLE_1));
    }
}
