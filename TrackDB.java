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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.HashMap;

import de.hu_berlin.informatik.spws2014.mapever.FileUtils;

/**
 * Class for listing and managing meta information about
 * track files in a directory. Saves data in a track.conf file
 * in the passed directory.
 */
public class TrackDB {
    private static final String configFileName = "track.conf";
    private static final int versionNumber = 1;
    private static final int FIRST_IDENTIFIER = 1;

    //Singleton db
    public static TrackDB main = null;

    private final File baseDir;
    private final File dbFile;

    private HashMap<Long, TrackDBEntry> maps;
    private long lastIdentifier;

    /**
     * Tries to load a TrackDB from the baseDirectory
     * If it was already loaded than nothing happens.
     * @return true if load was successful, false if a
     * different db was already loaded before or if
     * IOExceptions happened.
     */
    public static boolean loadDB(File baseDirectory) {
        try {
            System.err.println("Loading DB from:" + baseDirectory);

            if (main != null) {
                System.err.println("Tried to create second TrackDB.");
                return main.baseDir.equals(baseDirectory);
            }
            if (!baseDirectory.isDirectory()) {
                System.err.println("Provide valid directory for TrackDB.");
                return false;
            }

            try {
                main = new TrackDB(baseDirectory);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                return false;
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    @SuppressWarnings("unchecked")
    private boolean versionDependendLoad(ObjectInputStream ois) throws IOException {
        int thisVersion = ois.readInt();
        switch (thisVersion) {
        case 1:
            maps = new HashMap<>();
            long count = ois.readLong();
            for (long i = 0; i < count; i++) {
                TrackDBEntry tmp = new TrackDBEntry(ois);
                maps.put(tmp.getIdentifier(), tmp);
            }
            lastIdentifier = ois.readLong();
            break;
        default:
            System.err.println("Unsupported version number: " + thisVersion + "!");
            return false;
        }
        return true;
    }

    /**
     * loads track.conf file from dbDir
     * @throws IOException
     */
    private TrackDB(File dbDir) throws IOException {
        baseDir = dbDir;
        dbFile = new File(dbDir + File.separator + configFileName);
        File backup = new File(dbDir + File.separator + configFileName + ".bak");
        boolean isFileValid = dbFile.isFile();

        if (isFileValid) {
            System.err.println("All OK.");
            FileInputStream fis = new FileInputStream(dbFile);
            ObjectInputStream ois = new ObjectInputStream(fis);
            isFileValid = versionDependendLoad(ois);
            if (!isFileValid && !backup.exists())
                FileUtils.copyFileToFile(dbFile, backup);
            ois.close();
            fis.close();
        }

        if (!isFileValid) {
            System.err.println("Could not read file. Creating new trackDB in " + dbFile);
            maps = new HashMap<>();
            lastIdentifier = FIRST_IDENTIFIER;
        }

        // Find any "lost" files
        File[] all_files = dbDir.listFiles();
        boolean found_lost = false;
        for (File file : all_files) {
            if (!file.isFile()) continue;
            String name = file.getName();
            if (name.endsWith(".track")) name = name.substring(0, name.length() - ".track".length());
            long number = 0;
            try {
                number = Long.parseLong(name);
            } catch (Exception ignored) {}
            if (number > 0 && maps.get(number) == null) {
                System.err.println("Found lost map " + name);
                TrackDBEntry tmp = new TrackDBEntry(number);
                maps.put(number, tmp);
                if (number >= lastIdentifier) lastIdentifier = number + 1;
                found_lost = true;
            }
        }
        if (found_lost) save();
    }

    /**
     * Tries to delete map
     * @return true if map was deleted. False otherwise.
     */
    public boolean delete(TrackDBEntry map) {
        if (maps.remove(map.getIdentifier()) != null) {
            save();
            //noinspection ResultOfMethodCallIgnored
            new File(baseDir + File.separator + map.getIdentifier() + ".track").delete();
            return true;
        } else {
            return false;
        }
    }

    /**
     * @return The TrackDBEntry with the given identifier.
     */
    public TrackDBEntry getMap(long identifier) {
        return maps.get(identifier);
    }

    /**
     * @return The TrackLDMIO object referenced by map
     * @throws IOException
     */
    public LDMIOTrack getLDMIO(TrackDBEntry map) throws IOException {
        return new LDMIOTrack(baseDir + File.separator + map.getIdentifier() + ".track");
    }

    /**
     * Creates and returns new entry
     */
    public TrackDBEntry createMap() {
        Long currentIdentifier = lastIdentifier++;
        TrackDBEntry tmp = new TrackDBEntry(currentIdentifier);
        maps.put(currentIdentifier, tmp);
        save();
        return tmp;
    }

    /**
     * @return all entries
     */
    public Collection<TrackDBEntry> getAllMaps() {
        return maps.values();
    }

    /**
     * save all entries
     */
    public void save() {
        try {
            FileOutputStream fis = new FileOutputStream(dbFile);
            ObjectOutputStream oos = new ObjectOutputStream(fis);

            oos.writeInt(versionNumber);
            oos.writeLong(maps.size());
            for (TrackDBEntry e : maps.values())
            {
                e.save(oos);
            }
            oos.writeLong(lastIdentifier);

            oos.close();
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
