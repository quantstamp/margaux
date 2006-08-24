/*
 * Alloy Analyzer
 * Copyright (c) 2002 Massachusetts Institute of Technology
 *
 * The Alloy Analyzer is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * The Alloy Analyzer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with the Alloy Analyzer; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package kodviz.util;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

/**
 * Represents an enumeration parameter in a parameter block.
 *
 * @see Params
 * @see Parameter
 * @see ParamReader
 * @author Ian Schechter, Ilya Shlyakhter
 */

@SuppressWarnings("unchecked")
public class EnumParameter {
    public String parameter;
    public Vector values;
    public Map v2d;
    
    public EnumParameter (String p) {
	parameter = p;
	values = new Vector();
	v2d = new HashMap();
    }

    public void setParameter (String p) {parameter = p;}
    
    public void addValueDescription(String v, String d) {
	values.add(v);
	v2d.put(v,d);
    }
    public String toString (){
	return "ENUM {"+parameter+v2d+"}";
    }
    

}
  

