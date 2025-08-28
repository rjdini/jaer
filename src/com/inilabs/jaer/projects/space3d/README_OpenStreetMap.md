# Space3D GUI — Map-Coherent Version

This build keeps the OpenStreetMap background perfectly in sync with your overlay scale/pan.

Files:
- `Space3DPanel.java` — draws OSM tiles behind the grid; scales tiles so meters-per-pixel == 1/pixelsPerMeter.
- `Space3DGUI.java` — GUI with Scale/Offset sliders, "Show Map" toggle, and Map Zoom slider.

Requirements:
- `Space3D` must expose `getOriginLatDeg()`, `getOriginLonDeg()`, and `getHalfExtentM()`.

Run:
- `Space3DGUI.main(...)` for a quick demo.
