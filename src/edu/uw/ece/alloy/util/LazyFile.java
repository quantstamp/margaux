/**
 * 
 */
package edu.uw.ece.alloy.util;

import java.io.File;
import java.net.URI;
import java.util.logging.Level;

import edu.mit.csail.sdg.alloy4.Err;
import edu.mit.csail.sdg.alloy4.Util;
import edu.uw.ece.alloy.Compressor;
import edu.uw.ece.alloy.Configuration;

/**
 * @author vajih
 *
 */
public class LazyFile extends File {

	private static final long serialVersionUID = -889771543337404216L;

	public final String content;

	protected LazyFile(String parent, StringBuilder sb) {
		super(parent);
		content = sb.toString();
	}

	protected LazyFile(URI uri, String content) {
		super(uri);
		this.content = content;
	}

	protected LazyFile(String parent, String child, String content) {
		super(parent, child);
		this.content = content;
	}

	protected LazyFile(File parent, String child, String content) {
		super(parent, child);
		this.content = content;
	}

	public LazyFile(String pathname) {
		this(pathname, new StringBuilder(Compressor.EMPTY_STRING));
	}

	public LazyFile(URI uri) {
		this(uri, Compressor.EMPTY_STRING);
	}

	public LazyFile(String parent, String child) {
		this(parent, child, Compressor.EMPTY_STRING);
	}

	public LazyFile(File parent, String child) {
		this(parent, child, Compressor.EMPTY_STRING);
	}

	/**
	 * Load the file mentioned in path to the content
	 * 
	 * @return
	 */
	public LazyFile load() {
		final String content = Utils.readFile(getAbsolutePath());
		return new LazyFile(getAbsolutePath(), new StringBuilder(content));
	}

	/**
	 * Unload the content into a file path.
	 * 
	 * @param parentDirectory
	 * @return
	 */
	public LazyFile unload(File parentDirectory) {
		String pathname = (new File(parentDirectory.getAbsolutePath(), getName()))
				.getAbsolutePath();
		try {
			edu.mit.csail.sdg.alloy4.Util.writeAll(pathname, content);
		} catch (Err e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		return new LazyFile(pathname);
	}

	/**
	 * Dumps the content of this {@link LazyFile} unto the disk using the path represented by the file
	 * @return self
	 */
	public LazyFile dumpFile() {
        
	    this.load();
	    
        try {
            if (!this.getParentFile().exists()) {
                this.getParentFile().mkdirs();
            }
            
            Util.writeAll(this.getAbsolutePath(), this.content);
        } catch (Err e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        
        return this;
    }	
	
	public boolean isLoaded() {
		return !content.equals(Compressor.EMPTY_STRING);
	}

}
