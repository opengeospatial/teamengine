/*
 * The Open Geospatial Consortium licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 */
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
