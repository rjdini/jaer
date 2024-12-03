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
package com.inilabs.jaer.projects.environ;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.awt.Color;
import java.io.IOException;

public class ColorAdapter extends TypeAdapter<Color> {

    @Override
    public void write(JsonWriter out, Color color) throws IOException {
        if (color == null) {
            out.nullValue();
            return;
        }
        out.beginObject();
        out.name("r").value(color.getRed());
        out.name("g").value(color.getGreen());
        out.name("b").value(color.getBlue());
        out.name("a").value(color.getAlpha());
        out.endObject();
    }

    @Override
    public Color read(JsonReader in) throws IOException {
        in.beginObject();
        int r = 0, g = 0, b = 0, a = 255; // Default values
        while (in.hasNext()) {
            switch (in.nextName()) {
                case "r":
                    r = in.nextInt();
                    break;
                case "g":
                    g = in.nextInt();
                    break;
                case "b":
                    b = in.nextInt();
                    break;
                case "a":
                    a = in.nextInt();
                    break;
            }
        }
        in.endObject();
        return new Color(r, g, b, a);
    }
}
