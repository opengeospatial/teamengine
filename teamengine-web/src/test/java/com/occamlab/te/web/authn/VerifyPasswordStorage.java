package com.occamlab.te.web.authn;

import static org.junit.Assert.*;
import org.junit.Test;

import com.occamlab.te.web.authn.PasswordStorage;
import com.occamlab.te.web.authn.PasswordStorage.CannotPerformOperationException;
import com.occamlab.te.web.authn.PasswordStorage.InvalidHashException;

public class VerifyPasswordStorage {

    @Test
    public void createAndVerifyHash() throws CannotPerformOperationException, InvalidHashException {
        String password = "alpha";
        String hash = PasswordStorage.createHash(password);
        assertTrue("Password verification failed.", PasswordStorage.verifyPassword(password, hash));
    }

}
