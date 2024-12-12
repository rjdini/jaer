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

import com.inilabs.jaer.projects.cog.SpatialAttention;
import com.inilabs.jaer.projects.tracker.TrackerAgentDrawable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class SpatialAttentionTest {

    private static final Logger log = Logger.getLogger(SpatialAttentionTest.class.getName());

    public static void main(String[] args) {
        // Initialize SpatialAttention singleton
        
        SpatialAttention spatialAttention = SpatialAttention.getInstance();

        // Create a mock TrackerAgentDrawable
        TrackerAgentDrawable mockTrackerAgent = new TrackerAgentDrawable(0,10,2000);
        mockTrackerAgent.enableSupportQualltyTests(true);
        mockTrackerAgent.setMockSupportQuality(50); // Arbitrary high quality to surpass threshold

        // Set the mock tracker as the best tracker in SpatialAttention
        spatialAttention.setBestTrackerAgent(mockTrackerAgent);

        // Create a scheduler for the sinusoidal azimuth changes
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

        scheduler.scheduleAtFixedRate(new Runnable() {
            private double time = 0.0; // Time counter for the sinusoidal function
            private final double frequency = 0.1; // Frequency of the sinusoidal wave (Hz)
            private final double amplitude = 20.0; // Amplitude of the wave (-20 to 20 degrees)

            @Override
            public void run() {
                // Calculate the new azimuth value using a sinusoidal function
                double newAzimuth = amplitude * Math.sin(2 * Math.PI * frequency * time);
                mockTrackerAgent.setAzimuth((float) newAzimuth);

                // Log the change
                log.info(String.format("Updated azimuth to: %.2f degrees", newAzimuth));

                // Increment the time for the next calculation
                time += 0.1; // Update every 100ms
            }
        }, 0, 100, TimeUnit.MILLISECONDS);

        // Schedule SpatialAttention's update loop
        spatialAttention.startTasks();

        // Add shutdown hook to gracefully stop the scheduler and SpatialAttention
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Shutting down test...");
            scheduler.shutdown();
            spatialAttention.shutdown();
        }));
    }
}
