package com.occamlab.te.realm;

import static com.occamlab.te.realm.PasswordStorage.*;
import static org.junit.Assert.*;
import org.junit.Test;

public class VerifyPasswordStorage {

    @Test
    public void createAndVerifyHash() throws CannotPerformOperationException, InvalidHashException {
        String password = "alpha";
        String hash = PasswordStorage.createHash(password);
        assertTrue("Password verification failed.", PasswordStorage.verifyPassword(password, hash));
    }

}
