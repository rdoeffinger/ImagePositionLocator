/* Copyright (C) 2016 Reimar Doeffinger
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package de.hu_berlin.informatik.spws2014.ImagePositionLocator;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;

/**
 * Fix up the object names when serializing in from old version.
 *
 * Note: Using Java serialization is something you will ALWAYS end up regretting,
 * please NEVER use it.
 */
public class TranslateObjectInputStream extends ObjectInputStream {
    TranslateObjectInputStream(InputStream in) throws IOException {
        super(in);
    }

    @Override
    protected ObjectStreamClass readClassDescriptor() throws IOException, ClassNotFoundException {
        ObjectStreamClass res = super.readClassDescriptor();
        String name = res.getName();
        String newName = null;

        if (name.startsWith("de.hu_berlin.informatik.spws2014.imagePositionLocator")) {
            newName = name;
            newName.replace("de.hu_berlin.informatik.spws2014.imagePositionLocator", "de.hu_berlin.informatik.spws2014.ImagePositionLocator");
        }
        if (name.equals("de.hu_berlin.informatik.spws2014.a.p")) newName = "de.hu_berlin.informatik.spws2014.ImagePositionLocator.TrackDBEntry";
        if (name.equals("de.hu_berlin.informatik.spws2014.a.b")) newName = "de.hu_berlin.informatik.spws2014.ImagePositionLocator.GpsPoint";
        if (name.equals("de.hu_berlin.informatik.spws2014.a.j")) newName = "de.hu_berlin.informatik.spws2014.ImagePositionLocator.Marker";
        if (name.equals("de.hu_berlin.informatik.spws2014.a.l")) newName = "de.hu_berlin.informatik.spws2014.ImagePositionLocator.Point2D";

        if (newName != null)
            return ObjectStreamClass.lookup(Class.forName(newName));
        return res;
    }

}
