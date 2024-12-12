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
package com.inilabs.jaer.projects.utils;

import com.inilabs.jaer.projects.environ.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class GsonConfig {
      public static Gson createGson() {
        return new GsonBuilder()
            .addSerializationExclusionStrategy(new NoSuperclassExclusionStrategy(WaypointDrawable.class))
            .addDeserializationExclusionStrategy(new NoSuperclassExclusionStrategy(WaypointDrawable.class))
            .setPrettyPrinting()
            .create();
    }
    
  
}

