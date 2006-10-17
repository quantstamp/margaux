package edu.mit.csail.sdg.alloy4;

import java.awt.Component;
import javax.swing.Icon;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

/**
 * Graphical menu that extends JMenu.
 *
 * <p/><b>Thread Safety:</b> Can be called only by the AWT thread.
 *
 * @author Felix Chang
 */

public final class OurMenu extends JMenu {

    /** This silences javac's warning about missing serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /**
     * Construct a new menu and add it to an existing JMenuBar.
     *
     * <p/> Note: every time the user expands then collapses this menu,
     * it will automatically enable all JMenu and JMenuItem objects inside it.
     *
     * @param parent - the MenuBar to add this Menu into (or null if we don't want to add it to a JMenuBar yet)
     * @param label - the label to show on screen
     * @param mnemonic - the mnemonic (eg. KeyEvent.VK_F), or -1 if you don't want mnemonic
     * @param func - the function to call if the user expands this menu (or null if there is no function to call)
     */
    public OurMenu(JMenuBar parent, String label, int mnemonic, final OurFunc0 func) {
        super(label,false);
        if (mnemonic!=-1 && !Util.onMac()) setMnemonic(mnemonic);
        if (func!=null) addMenuListener(new MenuListener() {
            public final void menuSelected(MenuEvent e) { func.run(); }
            public final void menuDeselected(MenuEvent e) { enableChildren(OurMenu.this); }
            public final void menuCanceled(MenuEvent e) { enableChildren(OurMenu.this); }
        });
        if (parent!=null) parent.add(this);
    }

    /**
     * Convenience method that creates a new MenuItem and add it to this Menu.
     * @param icon - the icon to show on the left of the label (or null if you don't want an icon)
     * @param label - the label for the new MenuItem
     * @param enabled - whether the new MenuItem should be initially enabled or disabled
     * @param key - the mnemonic (eg. KeyEvent.VK_F), or -1 if you don't want mnemonic
     * @param accel - the accelerator (eg. KeyEvent.VK_F), or -1 if you don't want accelerator
     * @param func - the function to call if the user clicks this item (or null if there is no function to call)
     * @return the newly constructed OurMenuItem object
     */
    public OurMenuItem addMenuItem(Icon icon, String label, boolean enabled, int key, int accel, OurFunc0 func) {
        // OurMenuItem's constructor will add the new item into the list, so we don't have to call add() here.
        OurMenuItem ans = new OurMenuItem(this,label,key,accel,func);
        ans.setEnabled(enabled);
        if (icon!=null) ans.setIcon(icon);
        return ans;
    }

    /**
     * Convenience method that recursively enable every Menu and MenuItem inside "menu".
     * @param menu - the menu to start the recursive search
     */
    private static void enableChildren(JMenu menu) {
        for(int i=0; i<menu.getMenuComponentCount(); i++) {
            Component obj=menu.getMenuComponent(i);
            if (obj instanceof JMenuItem) ((JMenuItem)obj).setEnabled(true);
            else if (obj instanceof JMenu) enableChildren((JMenu)obj);
        }
    }
}
