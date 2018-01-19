package com.occamlab.te.util;

import java.io.IOException;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.FileSystem;
import java.nio.file.LinkOption;
import java.nio.file.WatchService;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.Iterator;
import java.net.URI;

/**
 * This utility was developed to address security vulnerabilities due to manipulation
 * of the file system path.  It restricts file system paths to those which are known 
 * to be safe.  The specific rules applied are:
 *   1) The path is restricted to one of the following root directories
 *        a) TE_BASE - path defined by the TE_BASE environment variable 
 *        b) TE_INSTALL - path defined by the TE_INSTALL environment variable
 *        c) Java temp directory
 *        d) TE_BUILD - An environment variable which contains the path to this source tree  	
 *   2) No navigating up the directory tree
 *   3) These paths are never allowed
 *        a) c:\\Windows 
 *        b) /etc 
 *        c) /bin 
 *        d) /usr/bin  
 * 
 * @version January 18, 2018
 * @author Charles Heazel
 */
public class TEPath implements Path {

    // The following variables define the valid roots
    private static Path tmpdir = null;        // Java temp directory
    private static Path te_install = null;    // TeamEngine installation directory
    private static Path te_base = null;       // TeamEngine base directory
    private static Path te_build = null;      // TeamEngine build directory
    private static Path user_home = null;     // Users home directory

    Path vpath = null;    // The Path object which TEPath encapsulates
        
    private static Logger jlogger = Logger.getLogger("com.occamlab.te.util.TEPath");

    // Constructor - take a string as input

    public TEPath( String arg ) {

        // Make every effort to populate the valid root Paths with something 
        // even if it is made up.
        // Note that these variables are only initialized once.

        String stmp = null;

        // Team Engine Base Directory
        if( te_base == null ) {
            stmp = System.getProperty("TE_BASE");
            if(stmp == null) stmp = System.getenv("TE_BASE");
            if(stmp == null) stmp = "/TE_BASE";
            te_base = Paths.get( stmp );
            }

        //  Java Temp directory
        if( tmpdir == null ) {
            stmp = System.getProperty("java.io.tmpdir");
            if(stmp == null) stmp = te_base.toString();
            tmpdir = Paths.get( stmp );
            }

        // Team Engine Install Directory
        if( te_install == null ) {
            stmp = System.getProperty("TE_INSTALL");
            if(stmp == null) stmp = System.getenv("TE_INSTALL");
            if(stmp == null) stmp = te_base.toString();
            te_install = Paths.get( stmp );
            }

        // Team Engine Build directory (needed for unit testing
        if( te_build == null ) {
            stmp = System.getenv("TE_BUILD");
            if(stmp == null) stmp = te_base.toString();
            te_build = Paths.get( stmp );
            }

        // User Home directory
        if( user_home == null ) {
            stmp = System.getenv("HOME");
            if(stmp == null) stmp = te_base.toString();
            user_home = Paths.get( stmp );
            }

        // Now normalize the path - remove redundant name elements etc.

        Path path1 = Paths.get(arg);
        Path path2 = path1.normalize();

        // Next get the absolute path
        // IOErrors are ignored since we don't need the file to exist at this point.
        // Security exceptions are logged, then we continue with an empty path

        Path path3 = null;
        try { path3 = path2.toAbsolutePath(); }
        catch (SecurityException e) {
            jlogger.log(Level.WARNING, e.getMessage(), e.getCause());
            path3 = Paths.get( new String() );
        }

        // Then get the real path (resolving symbolic links)
        // IOExceptions are ignored since we don't need the file to exist at this point.
        // Security exceptions are logged, then we continue with an empty path

        Path path4 = null;
        try { path4 = path3.toRealPath(); }
        catch (SecurityException e) {
            jlogger.log(Level.WARNING, e.getMessage(), e.getCause());
            path4 = Paths.get( new String() );
        }
        catch (IOException e) {}

        // Now perform validation on the resulting path
        // If the Path object is not valid, then create an empty Path
        if(this.validate(path4)) {
            vpath = path4;
            }
        else {
            jlogger.log(Level.WARNING, "Invalid path name: " + arg + " Empty Path created");
            vpath = Paths.get( new String() );
            }
    }


    public int compareTo( Path arg ){
        int i = vpath.compareTo( arg );
        return i;
        }

    public boolean endsWith( Path arg ){
        boolean i = vpath.endsWith( arg );
        return i;
        }

    public boolean endsWith( String arg ){
        boolean i = vpath.endsWith( arg );
        return i;
        }

    public boolean equals( Object arg ){
        boolean i = vpath.equals( arg );
        return i;
        }

    public Path getFileName( ){
        Path i = vpath.getFileName( );
        return i;
        }

    public FileSystem getFileSystem( ){
        FileSystem i = vpath.getFileSystem( );
        return i;
        }

    public Path getName( int arg ){
        Path i = vpath.getName( arg );
        return i;
        }

    public int getNameCount( ){
        int i = vpath.getNameCount( );
        return i;
        }

    public Path getParent( ){
        Path i = vpath.getParent( );
        return i;
        }

    public Path getRoot( ){
        Path i = vpath.getRoot(  );
        return i;
        }

    public int hashCode( ){
        int i = vpath.hashCode( );
        return i;
        }

    public boolean isAbsolute( ){
        boolean i = vpath.isAbsolute( );
        return i;
        }

    public Iterator<Path> iterator( ){
        Iterator<Path> i = vpath.iterator( );
        return i;
        }

    public Path normalize( ){
        Path i = vpath.normalize( );
        return i;
        }

    // We don't implement register() at this time.  Just a stub to keep the interface happy.
    public WatchKey register (WatchService service, WatchEvent.Kind<?>[] events, WatchEvent.Modifier... modifiers ) throws IOException {
        return null;
        }

    // We don't implement register() at this time.  Just a stub to keep the interface happy.
    public WatchKey register (WatchService service, WatchEvent.Kind<?>... events ) throws IOException {
        return null;
        }

    public Path relativize( Path arg ){
        Path i = vpath.relativize( arg );
        return i;
        }

    public Path resolve( Path arg ){
        Path i = vpath.resolve( arg );
        return i;
        }

    public Path resolve( String arg ){
        Path i = vpath.resolve( arg );
        return i;
        }

    public Path resolveSibling( Path arg ){
        Path i = vpath.resolveSibling( arg );
        return i;
        }

    public Path resolveSibling( String arg ){
        Path i = vpath.resolveSibling( arg );
        return i;
        }

    public boolean startsWith( Path arg ){
        boolean i = vpath.startsWith( arg );
        return i;
        }

    public boolean startsWith( String arg ){
        boolean i = vpath.startsWith( arg );
        return i;
        }

    public Path subpath( int begin, int end ){
        Path i = vpath.subpath( begin, end );
        return i;
        }

    public Path toAbsolutePath( ){
        Path i = vpath.toAbsolutePath( );
        return i;
        }

    public File toFile( ){
        File i = vpath.toFile( );
        return i;
        }

    public Path toRealPath( LinkOption... arg ) throws IOException {
        Path i = vpath.toRealPath( arg );
        return i;
        }

    public String toString( ){
        String i = vpath.toString( );
        return i;
        }

    public URI toUri( ){
        URI i = vpath.toUri(  );
        return i;
        }

    // A useful method.  The Path interface does not allow us to refuse an invalid path.
    // So a TEPath created with an invalid path:
    //    - returns an empty String for all methods which return Strings
    //    - returns false to an isValid() request.
    public boolean isValid() {
    	if(this.validate(vpath)) return(true);
    	return(false);
    }
    
    private boolean validate( Path arg1 ){

    	// Validate checks the path supplied in the argument against a set of rules
    	// for a valid path.  Changes to the source over time may require adjustments
    	// to these rules.

        // a null or empty path is not valid
        if(arg1 == null) return(false);
        if(arg1.toString().isEmpty()) return(false);

        Path tpath = arg1;
        
    	// Restrict the path to one of the valid root directories
    	boolean valid = false;
    	if(tpath.startsWith(te_base)) valid = true; 
    	if(tpath.startsWith(te_install)) valid = true; 
    	if(tpath.startsWith(tmpdir)) valid = true;
    	if(tpath.startsWith(te_build)) valid = true;
    	
    	// These are never allowed
    	if(tpath.startsWith("c:\\Windows")) valid = false; 
    	if(tpath.startsWith("C:\\Windows")) valid = false; 
    	if(tpath.startsWith("/etc")) valid = false; 
    	if(tpath.startsWith("/bin")) valid = false; 
    	if(tpath.startsWith("/usr/bin")) valid = false; 
    	
    	if(valid == false){
           jlogger.warning("TEPATH Invalid Path: " + arg1);
    	}
    	return(valid);
    }
}

