package org.opengis.cite.note10;

import static org.junit.Assert.*;



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
	public void testExample()  {
	//	TECore teCore = new TECore(engine, index, runOpts);
		TECore teCore = new TECore();
		teCore.execute_test(testName, params, contextNode)
		System.out.println("hello");
		//teCore.getEngine();
		
		
		assertFalse("Should be true",true);
		
	}

}
