/*
 * Copyright (C) 2024 rjd.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
package com.inilabs.jaer.projects.motor.tests;

import com.inilabs.jaer.projects.cog.tests.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;

public class RawJoystickReader {

    private static final String JOYSTICK_DEVICE = "/dev/input/js0";

    public static void main(String[] args) {
        try (FileInputStream fis = new FileInputStream(JOYSTICK_DEVICE);
             FileChannel channel = fis.getChannel()) {

            ByteBuffer buffer = ByteBuffer.allocate(8); // Joystick events are 8 bytes long
            buffer.order(ByteOrder.LITTLE_ENDIAN);      // Adjust for byte order if necessary

            System.out.println("Listening for raw joystick inputs...");

            while (true) {
                buffer.clear();
                int bytesRead = channel.read(buffer);
                if (bytesRead != 8) {
                    continue; // Skip incomplete reads
                }

                buffer.flip();

                // Decode the packet
                long timestamp = Integer.toUnsignedLong(buffer.getInt()); // First 4 bytes as timestamp
                short signalValue = buffer.getShort();                         // Next 2 bytes as data
                byte signalType = buffer.get();                          // 7th byte as signal type
                byte signalChannel = buffer.get();                             // 8th byte as channel number

                // Print decoded information
                System.out.printf("Timestamp: %d, Value: %d, Signal Type: %d, Channel: %d%n",
                        timestamp, signalValue, signalType, signalChannel);
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Failed to read from joystick device: " + JOYSTICK_DEVICE);
        }
    }
}
