package com.occamlab.te.web;

import org.junit.Test;

import com.occamlab.te.web.PasswordStorage.CannotPerformOperationException;
import com.occamlab.te.web.PasswordStorage.InvalidHashException;

public class VerifyPasswordStorage {

    @Test
    public void createAndVerifyHash() throws CannotPerformOperationException, InvalidHashException {
        String password = "alpha";
        String hash = PasswordStorage.createHash(password);
        PasswordStorage.verifyPassword(password, hash);
    }

}
