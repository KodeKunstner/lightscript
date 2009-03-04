package murlen.util.fscriptME;

import java.util.Vector;

/**
* <b>LineLoader - used by FScript to load source text</b>
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
 *
 * * 20.08.2001
 *   - getLine added
 *   - setCurLine test was wrong, allowed setting line one too far
*/

final class LineLoader {

    public Vector lines;

    private int curLine;

    /**
           *Constructor */
    public LineLoader() {

        lines=new Vector(20);
        curLine=0;
    }

    /**
      * resets the LineLoader  */
    public final void reset() {
        lines=null;
        lines=new Vector(20);
        curLine=0;
    }

    /**
    * method to incrementally add lines to buffer
    * @param s the line to load */
    public final void addLine(String s) {
        if (!s.trim().equals("")) {
            lines.addElement(s);
        } else {
            //need to add blank lines to keep error msg lines
            //in sync with file lines.
            lines.addElement("");
        }
    }
    
    /**
     *add \n separated lines
     *@param s the lines to add
     */
    public final void addLines(String s){
        int pos;
        
        if (!s.trim().equals("")){
            pos=s.indexOf('\n');
            while (pos>=0){
                addLine(s.substring(0,pos));
                s=s.substring(pos+1,s.length());
                pos=s.indexOf('\n');
            }
            if (!s.trim().equals("")){
                addLine(s);
            }
        }
    }
    
    /**
    * Sets the current execution line
    * @param n the line number */
    public final void setCurLine(int n) {
        if (n>lines.size()) {
            n=lines.size()-1;
        } else if (n<0) {
            n=0;
        }

        curLine=n;
    }

    /**
    * Returns the current execution line */
    public final int getCurLine() {
        return curLine;
    }

    /**
    * Returns the total number of lines in buffer */
    public final int lineCount() {
        return lines.size();
    }

    /**
    * Returns the text of the current line */
    public final String getLine() {
        return (String) lines.elementAt(curLine);
    }
    
    /**
     *Returns the text of the requested line*/
    public final String getLine(int n){
        if(n<0||n>=lines.size()) return "";
        return (String)lines.elementAt(n);
    }
    
    

}
