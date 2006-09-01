package edu.mit.csail.sdg.alloy4.core;

/**
 * This inteface defines what a logger can do.
 *
 * @author Felix Chang
 */

public interface Log {

    /** Writes msg into the log. */
    public void log(String msg);

    /** Writes msg into the log in a bold style (if possible) */
    public void logBold(String msg);

    /** Commits all outstanding writes (if the logger is buffered) */
    public void flush();
}
