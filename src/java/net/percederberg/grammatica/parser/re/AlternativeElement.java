/*
 * AlternativeElement.java
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free
 * Software Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 * MA 02111-1307, USA.
 *
 * Copyright (c) 2003-2005 Per Cederberg. All rights reserved.
 */

package net.percederberg.grammatica.parser.re;

import java.io.IOException;
import java.io.PrintWriter;

import net.percederberg.grammatica.parser.LookAheadReader;

/**
 * A regular expression alternative element. This element matches the
 * longest alternative element.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.5
 */
class AlternativeElement extends Element {

    /**
     * The first alternative element.
     */
    private Element elem1;

    /**
     * The second alternative element.
     */
    private Element elem2;

    /**
     * Creates a new alternative element.
     *
     * @param first          the first alternative
     * @param second         the second alternative
     */
    public AlternativeElement(Element first, Element second) {
        elem1 = first;
        elem2 = second;
    }

    /**
     * Creates a copy of this element. The copy will be an instance
     * of the same class matching the same strings. Copies of elements
     * are necessary to allow elements to cache intermediate results
     * while matching strings without interfering with other threads.
     *
     * @return a copy of this element
     */
    public Object clone() {
        return new AlternativeElement(elem1, elem2);
    }

    /**
     * Returns the length of a matching string starting at the
     * specified position. The number of matches to skip can also be
     * specified, but numbers higher than zero (0) cause a failed
     * match for any element that doesn't attempt to combine other
     * elements.
     *
     * @param m              the matcher being used
     * @param input          the input character stream to match
     * @param start          the starting position
     * @param skip           the number of matches to skip
     *
     * @return the length of the longest matching string, or
     *         -1 if no match was found
     *
     * @throws IOException if a I/O error occurred
     */
    public int match(Matcher m, LookAheadReader input, int start, int skip)
        throws IOException {

        int  length = 0;
        int  length1 = -1;
        int  length2 = -1;
        int  skip1 = 0;
        int  skip2 = 0;

        while (length >= 0 && skip1 + skip2 <= skip) {
            length1 = elem1.match(m, input, start, skip1);
            length2 = elem2.match(m, input, start, skip2);
            if (length1 >= length2) {
                length = length1;
                skip1++;
            } else {
                length = length2;
                skip2++;
            }
        }
        return length;
    }

    /**
     * Prints this element to the specified output stream.
     *
     * @param output         the output stream to use
     * @param indent         the current indentation
     */
    public void printTo(PrintWriter output, String indent) {
        output.println(indent + "Alternative 1");
        elem1.printTo(output, indent + "  ");
        output.println(indent + "Alternative 2");
        elem2.printTo(output, indent + "  ");
    }

}
