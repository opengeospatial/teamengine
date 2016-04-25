package com.occamlab.te.web.authn;

import java.security.Principal;
import java.util.Collections;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.apache.catalina.realm.GenericPrincipal;
import org.junit.BeforeClass;
import org.junit.Test;

import com.occamlab.te.web.authn.PasswordStorage.CannotPerformOperationException;

public class VerifyUserFilesRealm {

    private static final String USERNAME = "alpha";
    private static GenericPrincipal principal;

    @BeforeClass
    public static void initPrincipal() throws CannotPerformOperationException {
        String hash = PasswordStorage.createHash("correct");
        principal = new GenericPrincipal(USERNAME, hash, Collections.singletonList("user"));
    }

    @Test
    public void correctPassword() {
        UserFilesRealm iut = new UserFilesRealm();
        UserFilesRealm realmSpy = spy(iut);
        doReturn(principal).when(realmSpy).getPrincipal(USERNAME);
        Principal aPrincipal = realmSpy.authenticate(USERNAME, "correct");
        assertNotNull("Expected authentication to succeed.", aPrincipal);
    }

    @Test
    public void incorrectPassword() {
        UserFilesRealm iut = new UserFilesRealm();
        UserFilesRealm realmSpy = spy(iut);
        doReturn(principal).when(realmSpy).getPrincipal(USERNAME);
        Principal aPrincipal = realmSpy.authenticate(USERNAME, "incorrect");
        assertNull("Expected authentication failure.", aPrincipal);
    }
}
