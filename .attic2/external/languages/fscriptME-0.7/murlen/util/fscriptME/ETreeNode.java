package murlen.util.fscriptME;

/**
* <b>ETreeNode - an Expression tree node - used by Parser</b>
  <p>
  <I>Copyright (C) 2002 murlen.</I></p>
    <p>
    This program is free software; you can redistribute it and/or
    modify it under the terms of the GNU General Public
    License as published by the Free Software Foundation; either
    version 2 of the License, or (at your option) any later version.</p>
    <p>
    This software is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
    General Public License for more details.</p>
    <p>
    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc.,59 Temple Place, Suite 330, Boston MA 0211-1307 USA
    </p>
* @author murlen
* @version 0.5
*/
class ETreeNode {

  public static int E_OP=0;
  public static int E_VAL=1;

  public int type;

  public Object value;

  public ETreeNode left;
  public ETreeNode right;

  public ETreeNode parent;

  public String toString(){
    return new String("Type=" + type + " Value=" + value);
  }


}
