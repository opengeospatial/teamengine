/* (c) 2020 Open Geospatial Consortium - All rights reserved
 * This code is licensed under the Apache License, Version 2.0 license, available at https://github.com/opengeospatial/teamengine/blob/master/LICENSE.txt
 *
 */
package com.occamlab.te.util;

import java.io.IOException;
import java.io.Writer;

public class NullWriter extends Writer {

	@Override
	public void write(char[] cbuf, int off, int len) throws IOException {
		// Does nothing
	}

	@Override
	public void flush() throws IOException {
		// Does nothing
	}

	@Override
	public void close() throws IOException {
		// Does nothing
	}

}
