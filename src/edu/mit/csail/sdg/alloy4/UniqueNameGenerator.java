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

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * This generates unique names based on names provided by the caller.
 *
 * <p><b>Thread Safety:</b>  Safe.
 */

public final class UniqueNameGenerator {

    /** This stores the set of names we've generated so far. */
    private final Set<String> names = new LinkedHashSet<String>();

    /** Construct a UniqueNameGenerator with a blank history. */
    public UniqueNameGenerator() { }

    /**
     * Regard the provided name as "seen".
     * <p> For convenience, it returns the argument as the return value.
     */
    public synchronized String seen(String name) { names.add(name); return name; }

    /**
     * Generate a unique name based on the input name.
     *
     * <p> Specifically: if the name has not been generated/seen already by this generator,
     * then it is returned as is. Otherwise, we append characters to it
     * until the name becomes unique.
     */
    public synchronized String make(String name) {
        if (name.length()==0) name="x";
        while(names.contains(name)) name=name+"'";
        names.add(name);
        return name;
    }
}
