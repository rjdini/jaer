/*
 * Copyright (C) 2025 rjd.
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

/**
 *
 * @author rjd
 */

package com.inilabs.jaer.projects.space3d;

import java.util.Objects;

/** Process-local registry so filters/tools can share the same Space3D instance. */
public final class Space3DRegistry {
    private static volatile Space3D space;

    private Space3DRegistry(){}

    public static void set(Space3D s){ space = Objects.requireNonNull(s); }
    public static Space3D get(){ return space; }
}

