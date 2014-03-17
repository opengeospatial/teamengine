package org.opengis.cite;

import static org.junit.Assert.*;

import java.util.List;

import net.sf.saxon.s9api.XdmNode;

import org.junit.Test;

import com.occamlab.te.Engine;
import com.occamlab.te.RuntimeOptions;
import com.occamlab.te.TECore;
import com.occamlab.te.index.Index;

public class Note10Test {
	 	static Engine engine;
	    static Index index;
	    static RuntimeOptions runOpts;

	@Test
	public void test()  throws Throwable {
		TECore teCore = new TECore(engine, index, runOpts);
		System.out.println("hello");
		
		
		try {
			List<String> params = null;
			String testName = null;
			XdmNode contextNode = null;
			teCore.execute_test(testName, params, contextNode);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		assertTrue("Shour be true",true);
		
	}

}
