/* Copyright (C) 2014,2015  Maximilian Diedrich
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
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Holds metadata for a track file
 * May only be created by TrackDB!
 */
public class TrackDBEntry {
    private final Long identifier;
    private String mapname;
    double minlat, minlon, maxlat, maxlon;

    TrackDBEntry(Long identifier) {
        this.identifier = identifier;
        mapname = "";
    }

    TrackDBEntry(ObjectInputStream ois) throws IOException {
        identifier = ois.readLong();
        mapname = ois.readUTF();
        minlat = ois.readDouble();
        minlon = ois.readDouble();
        maxlat = ois.readDouble();
        minlon = ois.readDouble();
    }

    public void save(ObjectOutputStream oos) throws IOException {
        oos.writeLong(identifier);
        oos.writeUTF(mapname);
        oos.writeDouble(minlat);
        oos.writeDouble(minlon);
        oos.writeDouble(maxlat);
        oos.writeDouble(maxlon);
    }

    public Long getIdentifier() {
        return identifier;
    }

    public String getMapname() {
        return mapname;
    }

    public void setMapname(String mapname) {
        this.mapname = mapname;
        TrackDB.main.save();
    }
}
