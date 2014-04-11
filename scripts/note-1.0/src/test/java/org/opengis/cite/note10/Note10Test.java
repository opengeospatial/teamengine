package org.opengis.cite.note10;

import static org.junit.Assert.*;

import org.junit.Test;

import com.occamlab.te.Engine;
import com.occamlab.te.RuntimeOptions;
import com.occamlab.te.SetupOptions;
import com.occamlab.te.TECore;
import com.occamlab.te.index.Index;
import com.occamlab.te.util.Misc;

public class Note10Test {
	 	static Engine engine;
	    static Index index;
	    static RuntimeOptions runOpts;

	@Test
	public void atest(){
    	String teb =Misc.getResourceAsFile("TE_BASE").getCanonicalPath();
    	System.out.println(teb);
    	System.setProperty("TE_BASE", teb);
	    com.occamlab.te.Test test = new com.occamlab.te.Test();
	    test.execute("note-1.0/src/main/ctl/note.ctl");
	    SetupOptions.getBaseConfigDirectory();
	    assertTrue(true);
	}
	    
	

}
