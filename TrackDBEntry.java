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

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * Holds metadata for a track file
 * May only be created by TrackDB!
 */
public class TrackDBEntry {
    private final Long identifier;
    private String mapname;
    int rotation;
    double minlat, minlon, maxlat, maxlon;

    TrackDBEntry(Long identifier) {
        this.identifier = identifier;
        mapname = "";
        rotation = 0;
        minlat = 1000;
        minlon = 1000;
        maxlat = -1000;
        maxlon = -1000;
    }

    TrackDBEntry(DataInput ois) throws IOException {
        identifier = ois.readLong();
        mapname = ois.readUTF();
        rotation = ois.readInt();
        minlat = ois.readDouble();
        minlon = ois.readDouble();
        maxlat = ois.readDouble();
        maxlon = ois.readDouble();
    }

    public void save(DataOutput oos) throws IOException {
        oos.writeLong(identifier);
        oos.writeUTF(mapname);
        oos.writeInt(rotation);
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

    public int getRotation() { return rotation; }
    public void setRotation(int rotation) {
        this.rotation = rotation;
        TrackDB.main.save();
    }

    public void setMinMaxLatLon(double minlat, double minlon, double maxlat, double maxlon) {
        if (minlat == this.minlat && minlon == this.minlon && maxlat == this.maxlat && maxlon == this.maxlon)
            return;
        this.minlat = minlat;
        this.minlon = minlon;
        this.maxlat = maxlat;
        this.maxlon = maxlon;
        TrackDB.main.save();
    }
}
