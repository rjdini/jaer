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
package com.inilabs.jaer.projects.cog;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class JoystickReader {

    private static final String JOYSTICK_DEVICE = "/dev/input/js0";

    public enum Axis {
        ROLL, PITCH, THROTTLE, YAW
    }

    public enum Button {
        BUTTON1, BUTTON2
    }

    public interface JoystickListener {
        void onAxisChange(Axis axis, float value);
        void onButtonPress(Button button, boolean pressed);
    }

    private final JoystickListener listener;

    public JoystickReader(JoystickListener listener) {
        this.listener = listener;
    }

    public void start() {
        new Thread(() -> {
            try (FileInputStream fis = new FileInputStream(JOYSTICK_DEVICE);
                 FileChannel channel = fis.getChannel()) {

                ByteBuffer buffer = ByteBuffer.allocate(8);

                while (true) {
                    buffer.clear();
                    int bytesRead = channel.read(buffer);
                    if (bytesRead != 8) continue;

                    buffer.flip();

                    int time = buffer.getInt();      // Timestamp (4 bytes)
                    short value = buffer.getShort(); // Value (2 bytes)
                    byte type = buffer.get();        // Type (1 byte)
                    byte number = buffer.get();      // Axis or button number (1 byte)

                    if ((type & 0x02) != 0) { // Axis event
                        float normalizedValue = value / 32767f; // Normalize to [-1.0, 1.0]
                        switch (number) {
                            case 0 -> listener.onAxisChange(Axis.ROLL, normalizedValue);
                            case 1 -> listener.onAxisChange(Axis.PITCH, normalizedValue);
                            case 2 -> listener.onAxisChange(Axis.THROTTLE, normalizedValue);
                            case 3 -> listener.onAxisChange(Axis.YAW, normalizedValue);
                            default -> System.err.println("Unknown axis number: " + number);
                        }
                    } else if ((type & 0x01) != 0) { // Button event
                        boolean pressed = value != 0;
                        switch (number) {
                            case 0 -> listener.onButtonPress(Button.BUTTON1, pressed);
                            case 1 -> listener.onButtonPress(Button.BUTTON2, pressed);
                            default -> System.err.println("Unknown button number: " + number);
                        }
                    }
                }
            } catch (IOException e) {
                System.err.println("Error reading joystick device: " + JOYSTICK_DEVICE);
                e.printStackTrace();
            }
        }).start();
    }

    public static void main(String[] args) {
        JoystickReader reader = new JoystickReader(new JoystickListener() {
            @Override
            public void onAxisChange(Axis axis, float value) {
                System.out.printf("Axis %s: %.3f%n", axis, value);
            }

            @Override
            public void onButtonPress(Button button, boolean pressed) {
                System.out.printf("Button %s: %s%n", button, pressed ? "PRESSED" : "RELEASED");
            }
        });

        System.out.println("Listening for joystick inputs...");
        reader.start();

        // Keep the program running
        try {
            Thread.sleep(Long.MAX_VALUE);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

