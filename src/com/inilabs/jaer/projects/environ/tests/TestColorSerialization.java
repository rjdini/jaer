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

package com.inilabs.jaer.projects.environ.tests;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.inilabs.jaer.projects.environ.ColorAdapter;
import java.awt.Color;

public class TestColorSerialization {
    public static void main(String[] args) {

        // Create Gson instance with ColorAdapter
        Gson gson = new GsonBuilder()
            .registerTypeAdapter(Color.class, new ColorAdapter())
            .setPrettyPrinting()
            .create();

        // Serialize
        TestObject testObject = new TestObject(new Color(0, 128, 255));
        String json = gson.toJson(testObject);
        System.out.println("Serialized JSON: " + json);

        // Deserialize
        TestObject deserialized = gson.fromJson(json, TestObject.class);
        System.out.println("Deserialized Color: " + deserialized.color);
    }

    // Helper class for testing
    static class TestObject {
        public Color color;

        public TestObject() {} // Default constructor for deserialization

        public TestObject(Color color) {
            this.color = color;
        }
    }
}
