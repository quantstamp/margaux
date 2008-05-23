/*
 * Alloy Analyzer 4 -- Copyright (c) 2006-2008, Felix Chang
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package edu.mit.csail.sdg.alloy4;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Locale;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FileDialog;
import java.awt.Frame;
import java.awt.GraphicsEnvironment;
import java.awt.event.KeyListener;
import java.awt.event.KeyEvent;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.filechooser.FileFilter;
import static javax.swing.JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED;
import static javax.swing.JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED;

/**
 * Graphical dialog methods for asking the user some questions.
 *
 * <p><b>Thread Safety:</b> Can be called only by the AWT event thread.
 */

public final class OurDialog {

    /** The constructor is private, since this utility class never needs to be instantiated. */
    private OurDialog() { }

    /** Popup the given error message. */
    public static void alert(Frame parentFrame, Object message, String title) {
        JOptionPane.showMessageDialog(parentFrame, message, title, JOptionPane.PLAIN_MESSAGE);
    }

    /** Popup the given error message, then terminate the program. */
    public static void fatal(Frame parentFrame, Object message) {
        JOptionPane.showMessageDialog(parentFrame, message, "Fatal Error", JOptionPane.ERROR_MESSAGE);
        System.exit(1);
    }

    /**
     * Ask if the user wishes to save the file, discard the file, or cancel the entire operation (default==cancel).
     * @return null if cancel, true if save, false if discard
     */
    public static Boolean askSaveDiscardCancel(Frame parentFrame, String description) {
        String save="Save", discard="Don\'t Save", cancel="Cancel";
        int ans=JOptionPane.showOptionDialog(parentFrame,
            new String[]{
                description+" has not been saved. Do you want to",
                "cancel the operation, close the file without saving, or save it and close?"
            },
            "Warning",
            JOptionPane.YES_NO_CANCEL_OPTION,
            JOptionPane.WARNING_MESSAGE,
            null,
            new Object[]{save, discard, cancel},
            cancel);
        if (ans == JOptionPane.YES_OPTION) return Boolean.TRUE;
        if (ans != JOptionPane.NO_OPTION) return null;
        return Boolean.FALSE;
    }

    /** Ask if the user really wishes to overwrite the file (default=no). */
    public static boolean askOverwrite(Frame parentFrame, String filename) {
        String yes="Overwrite", no="Cancel";
        int ans=JOptionPane.showOptionDialog(parentFrame,
            new String[]{"The file \""+filename+"\"", "already exists. Do you wish to overwrite it?"},
            "Warning: file already exists",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE,
            null,
            new Object[]{yes, no},
            no);
        return ans==JOptionPane.YES_OPTION;
    }

    /** This caches the result of the call to get all fonts. */
    private static String[] allFonts = null;

    /** Returns true if a font with that name exists on the system. */
    public static boolean hasFont(String fontname) {
        String[] f;
        synchronized(OurDialog.class) {
            f=allFonts;
            if (f==null) allFonts=(f=GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames());
        }
        for(int i=0; i<f.length; i++) {
           if (fontname.compareToIgnoreCase(f[i])==0) return true;
        }
        return false;
    }

    /** Asks the user to choose a font; returns "" if the user cancels the request. */
    public static String askFont(Frame parentFrame) {
        String[] f;
        synchronized(OurDialog.class) {
            f=allFonts;
            if (f==null) allFonts=(f=GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames());
        }
        JComboBox jcombo = new OurCombobox(f);
        int ans=JOptionPane.showOptionDialog(parentFrame,
            new Object[]{"Please choose the new font:", jcombo},
            "Font",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE,
            null,
            new Object[]{"Ok","Cancel"},
            "Cancel");
        Object value=jcombo.getSelectedItem();
        if (ans!=JOptionPane.YES_OPTION || !(value instanceof String)) return "";
        return ((String)value);
    }

    /** True if we should use AWT (instead of Swing) to display the OPEN and SAVE dialog. */
    private static boolean useAWT = false;

    /**
     * Use the platform's preferred file chooser to ask the user to select a file.
     * <br> Note: if it is a save operation, and the user didn't include an extension, then we'll add the extension.
     * @param parentFrame - the parent frame
     * @param isOpen - true means this is an Open operation; false means this is a Save operation
     * @param dir - the initial directory (or null if we want to use the default)
     * @param ext - the file extension (including "."; using lowercase letters; for example, ".als") or ""
     * @param description - the description for the given extension
     * @return null if the user didn't choose anything, otherwise it returns the selected file
     */
    public static File askFile
    (Frame parentFrame, boolean isOpen, String dir, final String ext, final String description) {
        if (Util.onMac()) useAWT = true;
        if (dir==null) dir=Util.getCurrentDirectory();
        if (!(new File(dir).isDirectory())) dir=System.getProperty("user.home");
        dir=Util.canon(dir);
        String ans;
        if (useAWT) {
        	FileDialog f = new FileDialog(parentFrame, isOpen ? "Open..." : "Save...");
        	f.setMode(isOpen ? FileDialog.LOAD : FileDialog.SAVE);
        	f.setDirectory(dir);
        	if (ext.length()>0) f.setFilenameFilter(new FilenameFilter() {
        		public boolean accept(File dir, String name) { return name.toLowerCase(Locale.US).endsWith(ext); }
        	});
        	f.setVisible(true); // This method blocks until the user either chooses something or cancels the dialog.
        	if (f.getFile()==null) return null;
        	ans = f.getDirectory()+File.separatorChar+f.getFile();
        } else {
            try {
                JFileChooser open = new JFileChooser(dir);
                open.setDialogTitle(isOpen ? "Open..." : "Save...");
                open.setApproveButtonText(isOpen ? "Open" : "Save");
                if (ext.length()>0) open.setFileFilter(new FileFilter() {
                    public boolean accept(File f) { return !f.isFile() || f.getPath().toLowerCase(Locale.US).endsWith(ext); }
                    public String getDescription() { return description; }
                });
                if (open.showOpenDialog(parentFrame)!=JFileChooser.APPROVE_OPTION || open.getSelectedFile()==null) { return null; }
                ans = open.getSelectedFile().getPath();
            } catch(Exception ex) {
                useAWT = true;
                return askFile(parentFrame, isOpen, dir, ext, description);
            }
        }
        if (!isOpen) {
            int lastSlash = ans.lastIndexOf(File.separatorChar);
            int lastDot = (lastSlash>=0) ? ans.indexOf('.', lastSlash) : ans.indexOf('.');
            if (lastDot<0) ans=ans+ext;
        }
        return new File(Util.canon(ans));
    }

    /** Display "msg" in a dialogbox, and ask the user to choose yes versus no (default==no). */
    public static boolean yesno(Frame parentFrame, String msg, String yes, String no) {
        return JOptionPane.showOptionDialog(parentFrame,
           msg,
           "Question",
           JOptionPane.YES_NO_OPTION,
           JOptionPane.WARNING_MESSAGE,
           null,
           new Object[]{yes, no},
           no) == JOptionPane.YES_OPTION;
    }

    /** Display "msg" in a dialogbox, and ask the user to choose "Yes" versus "No" (default==no). */
    public static boolean yesno(Frame parentFrame, String msg) {
        return yesno(parentFrame, msg, "Yes", "No");
    }

    /** Display a modal dialog window containing the "objects"; returns true iff the user clicks Ok. */
    public static boolean getInput(Frame parentFrame, String title, Object... objects) {
        Object main = "Ok";
        for(int i=0; i<objects.length; i++) {
            if (objects[i] instanceof JTextField || objects[i] instanceof JTextArea) {
               main=objects[i];
               break;
            }
        }
        final JOptionPane pane = new JOptionPane(
            objects,
            JOptionPane.QUESTION_MESSAGE,
            JOptionPane.YES_NO_OPTION,
            null,
            new Object[]{"Ok", "Cancel"},
            main);
        final JDialog jd = pane.createDialog(parentFrame,title);
        for (int i=0; i<objects.length; i++) {
            if (!(objects[i] instanceof JTextField || objects[i] instanceof JCheckBox)) { continue; }
            JComponent x = (JComponent)(objects[i]);
            x.addKeyListener(new KeyListener() {
               public void keyPressed(KeyEvent e) {
                  // Make sure "ENTER" will submit the form, as if the user clicked Ok
                  if (e.getKeyCode()==KeyEvent.VK_ENTER) { pane.setValue("Ok"); jd.dispose(); }
               }
               public void keyTyped(KeyEvent e) {}
               public void keyReleased(KeyEvent e) {}
            });
        }
        jd.setVisible(true);
        return pane.getValue() == "Ok";
    }

    /** Display a simple window showing some text. */
    public static JFrame showtext(String title, String text, boolean autoLineWrap) {
        final JFrame window = new JFrame(title);
        final JButton done = new JButton("Close");
        done.addActionListener(Runner.createDispose(window));
        JTextArea textarea = OurUtil.textarea(text,20,60);
        textarea.setBackground(Color.WHITE);
        textarea.setEditable(false);
        textarea.setLineWrap(autoLineWrap);
        textarea.setWrapStyleWord(autoLineWrap);
        JScrollPane scrollPane = new JScrollPane(textarea, VERTICAL_SCROLLBAR_AS_NEEDED, HORIZONTAL_SCROLLBAR_AS_NEEDED);
        window.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        window.getContentPane().setLayout(new BorderLayout());
        window.getContentPane().add(scrollPane, BorderLayout.CENTER);
        window.getContentPane().add(done, BorderLayout.SOUTH);
        window.pack();
        window.setLocation(100,100);
        window.setSize(500,500);
        window.setVisible(true);
        return window;
    }
}
