package com.pilosa.client;

import com.pilosa.client.internal.ClientProtos;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Scanner;

/**
 * Iterates over a CSV of bitmaps.
 * <p>
 * The CSV file should not have a header and should have the following structure:
 * <pre>
 *     BITMAP_ID,PROFILE_ID[,TIMESTAMP]
 * </pre>
 */
public class CsvFileBitIterator implements IBitIterator {
    private Scanner scanner = null;
    private ClientProtos.Bit nextBit = null;

    private CsvFileBitIterator() {
    }

    /**
     * Creates a bit iterator from the CSV file at the given path.
     * @param path of the CSV file
     * @return bit iterator
     * @throws FileNotFoundException
     */
    public static CsvFileBitIterator fromPath(String path) throws FileNotFoundException {
        return fromStream(new FileInputStream(path));
    }

    /**
     * Creates a bit iterator from an input stream.
     *
     * @param stream CSV stream
     * @return bit iterator
     * @throws FileNotFoundException
     */
    public static CsvFileBitIterator fromStream(InputStream stream) throws FileNotFoundException {
        CsvFileBitIterator iterator = new CsvFileBitIterator();
        iterator.scanner = new Scanner(stream);
        return iterator;
    }

    @Override
    public boolean hasNext() {
        if (this.scanner == null) {
            return false;
        }
        if (this.scanner.hasNextLine()) {
            String line = this.scanner.nextLine();
            if (!line.isEmpty()) {
                String[] fields = line.split(",");
                long bitmapID = Long.parseLong(fields[0]);
                long profileID = Long.parseLong(fields[1]);
                long timestamp = 0;
                if (fields.length > 2) {
                    timestamp = Long.parseLong(fields[2]);
                }
                this.nextBit = ClientProtos.Bit.newBuilder()
                        .setBitmapID(bitmapID)
                        .setProfileID(profileID)
                        .setTimestamp(timestamp)
                        .build();
                return true;
            }
        }
        scanner.close();
        this.scanner = null;
        return false;
    }

    @Override
    public ClientProtos.Bit next() {
        return this.nextBit;
    }

    @Override
    public void remove() {
        // We have this just to avoid compilation problems on JDK 7
    }
}
