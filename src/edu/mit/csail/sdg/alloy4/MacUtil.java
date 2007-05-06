/*
 * Alloy Analyzer
 * Copyright (c) 2007 Massachusetts Institute of Technology
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 */

package edu.mit.csail.sdg.alloy4;

import javax.swing.SwingUtilities;
import com.apple.eawt.Application;
import com.apple.eawt.ApplicationAdapter;
import com.apple.eawt.ApplicationEvent;
import com.apple.eawt.ApplicationListener;
import edu.mit.csail.sdg.alloy4.MultiRunner.MultiRunnable;

/**
 * This class provides useful methods that may be called only on Mac OS X.
 *
 * <p> You must not call any methods here if you're not on a Mac,
 * since that will trigger the loading of com.apple.eawt.* which are not available on other platforms.
 *
 * <p><b>Thread Safety:</b>  Safe.
 */

public final class MacUtil {

    /** Constructor is private, since this class never needs to be instantiated. */
    private MacUtil() { }

    /** The cached Application object. */
    private static Application application=null;

    /** The previous listener (or null if there was none). */
    private static ApplicationListener listener=null;

    /**
     * Register a Mac OS X "ApplicationListener"; if there was a previous listener, it will be removed first.
     * @param handler - the application listener
     * @param reopen - when the user clicks on the Dock icon, we'll call handler.run(reopen)
     * @param about - when the user clicks on About Alloy4, we'll call handler.run(about)
     * @param open - when a file needs to be opened, we'll call handler.run(open,filename)
     * @param quit - when the user clicks on Quit, we'll call handler.run(quit)
     */
    public synchronized static void registerApplicationListener
    (final MultiRunnable handler, final int reopen, final int about, final int open, final int quit) {
        if (application==null) {
            application=new Application();
        }
        if (listener!=null) {
            application.removeApplicationListener(listener);
        }
        listener=new ApplicationAdapter() {
            @Override public void handleReOpenApplication (ApplicationEvent arg) {
                SwingUtilities.invokeLater(new MultiRunner(handler, reopen));
            }
            @Override public void handleAbout (ApplicationEvent arg) {
                arg.setHandled(true);
                SwingUtilities.invokeLater(new MultiRunner(handler, about));
            }
            @Override public void handleOpenFile (ApplicationEvent arg) {
                SwingUtilities.invokeLater(new MultiRunner(handler, open, arg.getFilename()));
            }
            @Override public void handleQuit (ApplicationEvent arg) {
                arg.setHandled(false); // "false" is correct; some documentation on apple.com claimed otherwise.
                SwingUtilities.invokeLater(new MultiRunner(handler, quit));
            }
        };
        application.addApplicationListener(listener);
    }
}
