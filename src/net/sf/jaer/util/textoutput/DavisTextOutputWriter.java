/*
 * Copyright (C) 2018 tobid.
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
package net.sf.jaer.util.textoutput;

import com.jogamp.opengl.GLAutoDrawable;
import eu.seebetter.ini.chips.davis.imu.IMUSample;
import java.awt.Desktop;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;
import net.sf.jaer.Description;
import net.sf.jaer.DevelopmentStatus;
import net.sf.jaer.chip.AEChip;
import net.sf.jaer.event.ApsDvsEvent;
import net.sf.jaer.event.ApsDvsEventPacket;
import net.sf.jaer.event.BasicEvent;
import net.sf.jaer.event.EventPacket;
import net.sf.jaer.event.PolarityEvent;
import net.sf.jaer.eventio.AEInputStream;
import static net.sf.jaer.eventprocessing.EventFilter.log;
import net.sf.jaer.eventprocessing.EventFilter2DMouseAdaptor;
import net.sf.jaer.graphics.FrameAnnotater;

/**
 * Writes out text format DVS, IMU and frame data from DAVIS cameras
 *
 * @author tobid
 */
@Description("Writes out text format files with data from DAVIS cameras")
@DevelopmentStatus(DevelopmentStatus.Status.Experimental)
public class DavisTextOutputWriter extends EventFilter2DMouseAdaptor implements PropertyChangeListener {

    private boolean dvsEvents = getBoolean("dvsEvents", true);
    private boolean apsFrames = getBoolean("apsFrames", false);
    private boolean imuSamples = getBoolean("imuSamples", false);
    protected final int LOG_EVERY_THIS_MANY_EVENTS = 1000; // for logging concole messages
    private ArrayList<FileWriter> fileWriters = new ArrayList();
    private FileWriter dvsWriter = null, imuWriter = null, apsWriter = null, timecodeWriter = null;
    protected static String DEFAULT_FILENAME = "jAER.txt";
    protected String lastFileName = getString("lastFileName", DEFAULT_FILENAME);
    protected File lastFile = null;
    protected int eventsWritten = 0;
    protected static final String TIMECODE_SUFFIX = "-timecode.txt";
    protected File timecodeFile = null;
    protected boolean closeOnRewind = getBoolean("closeOnRewind", true);
    protected boolean rewindBeforeRecording = getBoolean("rewindBeforeRecording", true);
    protected boolean ignoreRewinwdEventFlag = false; // used to signal to igmore first rewind event for closing file on rewind if rewindBeforeRecording=true
    private boolean chipPropertyChangeListenerAdded = false;
    protected int maxEvents = getInt("maxEvents", 0);
    protected ArrayList<String> additionalComments = new ArrayList();
    private boolean writeOnlyWhenMousePressed = getBoolean("writeOnlyWhenMousePressed", false);
    protected volatile boolean writeEnabled = false;

    public DavisTextOutputWriter(AEChip chip) {
        super(chip);
        setPropertyTooltip("startRecordingAndSaveAs", "Opens the output file and starts writing to it. The text file is in format timestamp x y polarity, with polarity ");
        setPropertyTooltip("closeFiles", "Closes the output file if it is open.");
        setPropertyTooltip("closeOnRewind", "closes recording on rewind event, to allow unattended operation");
        setPropertyTooltip("rewindBeforeRecording", "rewinds file before recording");
        setPropertyTooltip("maxEvents", "file is automatically closed after this many events have been written; set to 0 to disable");
        setPropertyTooltip("eventsWritten", "READONLY, shows number of events written");
        setPropertyTooltip("showFolderInDesktop", "Opens the folder containging the last-written file");
        setPropertyTooltip("writeOnlyWhenMousePressed", "If selected, then the events are are saved only when the mouse is pressed in the AEViewer window");
        setPropertyTooltip("writeEnabled", "Selects if writing events is enabled. Use this to temporarily disable output, or in conjunction with writeOnlyWhenMousePressed");
        chip.getSupport().addPropertyChangeListener(this);
        setPropertyTooltip("dvsEvents", "write dvs events as one per line with format timestamp(us) x y polarity(0=off,1=on)");
        setPropertyTooltip("imuSamples", "write IMU samples as one per line with format TBD");
        setPropertyTooltip("apsFrames", "write APS frames with format TBD");

        additionalComments.add("jAER DAVIS/DVS camera text file output");

    }

    /**
     * Processes packet to write output
     *
     * @param in input packet
     * @return input packet
     */
    @Override
    synchronized public EventPacket<?> filterPacket(EventPacket<?> in) {
        if (!chipPropertyChangeListenerAdded) {
            if (chip.getAeViewer() != null) {
                chip.getAeViewer().addPropertyChangeListener(AEInputStream.EVENT_REWOUND, this);
                chipPropertyChangeListenerAdded = true;
            }
        }
        if (!isWriteEnabled() || (!dvsEvents && !imuSamples && !apsFrames)) {
            return in;
        }
        try {
            Iterator itr = null;
            if (in instanceof ApsDvsEventPacket) {
                itr = ((ApsDvsEventPacket) in).fullIterator();
            } else {
                itr = in.inputIterator();
            }
            while (itr.hasNext()) {
                Object o = itr.next();
                if (dvsEvents && dvsWriter != null) {
                    PolarityEvent pe = (PolarityEvent) o;
                    // One event per line (timestamp x y polarity) as in RPG events.txt
                    dvsWriter.write(String.format("%d %d %d %d\n", pe.timestamp, pe.x, pe.y, pe.type));
                    incrementCountAndMaybeCloseOutput();
                }
                if (imuSamples && imuWriter != null && o instanceof ApsDvsEvent) {
                    ApsDvsEvent ae = (ApsDvsEvent) o;
                    if (ae.isImuSample()) {
                        IMUSample i = ae.getImuSample();
                        imuWriter.write(String.format("%d %f %f %f %f %f %f\n", ae.timestamp,
                                i.getAccelX(), i.getAccelY(), i.getAccelZ(),
                                i.getGyroTiltX(), i.getGyroYawY(), i.getGyroRollZ()));
                        incrementCountAndMaybeCloseOutput();
                    }
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(DavisTextOutputWriter.class.getName()).log(Level.SEVERE, null, ex);
            showWarningDialogInSwingThread(ex.toString(), "Error writing");
            doCloseFiles();
        }
        return in;
    }

    /**
     * Opens text output stream and optionally the timecode file, and enable
     * writing to this stream.
     *
     * @param f the file
     * @param additionalComments additional comments to be written to timecode
     * file, Comment header characters are added if not supplied.
     * @return the stream, or null if IOException occurs
     *
     */
    public FileWriter openFileWriter(File f) throws IOException {
        FileWriter fileWriter = new FileWriter(f);
        lastFile = f;
        setEventsWritten(0);
        if (additionalComments != null) {
            for (String s : additionalComments) {
                fileWriter.write("# " + s + "\n");
            }
        }
        fileWriter.write("# created " + new Date().toString() + Character.LINE_SEPARATOR);
        fileWriter.write("# source-file: " + (chip.getAeInputStream() != null ? chip.getAeInputStream().getFile().toString() : "(live input)") + Character.LINE_SEPARATOR);
        log.info("Opened text output file " + f.toString() + " with text format");
        fileWriters.add(fileWriter);
        return fileWriter;
    }

    /**
     * @return the dvsEvents
     */
    public boolean isDvsEvents() {
        return dvsEvents;
    }

    /**
     * @param dvsEvents the dvsEvents to set
     */
    public void setDvsEvents(boolean dvsEvents) {
        this.dvsEvents = dvsEvents;
        putBoolean("dvsEvents", dvsEvents);
    }

    /**
     * @return the apsFrames
     */
    public boolean isApsFrames() {
        return apsFrames;
    }

    /**
     * @param apsFrames the apsFrames to set
     */
    public void setApsFrames(boolean apsFrames) {
        this.apsFrames = apsFrames;
        putBoolean("apsFrames", apsFrames);
    }

    /**
     * @return the imuSamples
     */
    public boolean isImuSamples() {
        return imuSamples;
    }

    /**
     * @param imuSamples the imuSamples to set
     */
    public void setImuSamples(boolean imuSamples) {
        this.imuSamples = imuSamples;
        putBoolean("imuSamples", imuSamples);
    }

    public void doShowFolderInDesktop() {
        if (!Desktop.isDesktopSupported()) {
            log.warning("Sorry, desktop operations are not supported");
            return;
        }
        try {
            Desktop desktop = Desktop.getDesktop();
            File f = lastFile != null ? lastFile : new File(lastFileName);
            if (f.exists()) {
                desktop.open(f.getParentFile());
            }
        } catch (Exception e) {
            log.warning(e.toString());
        }
    }

    synchronized public void doStartRecordingAndSaveAs() {
        if (isFilesOpen()) {
            JOptionPane.showMessageDialog(null, "writers are already opened; close them first");
            return;
        }
        JFileChooser c = new JFileChooser(lastFileName);
        c.setFileFilter(new FileFilter() {

            @Override
            public boolean accept(File f) {
                return f.isDirectory() || f.getName().toLowerCase().endsWith(".txt");
            }

            @Override
            public String getDescription() {
                return "text file";
            }
        });
        c.setSelectedFile(new File(lastFileName));
        int ret = c.showSaveDialog(null);
        if (ret != JFileChooser.APPROVE_OPTION) {
            return;
        }
        String basename = c.getSelectedFile().toString();
        if (basename.toLowerCase().endsWith(".txt")) {
            basename = basename.substring(0, basename.length() - 4);
        }
        lastFileName = basename;
        putString("lastFileName", lastFileName);

        try {
            if (dvsEvents) {
                String fn = basename + "-events.txt";
                if (checkFileExists(fn)) {
                    dvsWriter = openFileWriter(new File(fn));
                    dvsWriter.write("# dvs-events: One event per line:  timestamp(us) x y polarity(0=off,1=on)" + Character.LINE_SEPARATOR);

                }
            }
            if (imuSamples) {
                String fn = basename + "-imu.txt";
                if (checkFileExists(fn)) {
                    imuWriter = openFileWriter(new File(fn));
                    imuWriter.write("# dvs-events: One measurement per line: timestamp(us) ax(g) ay(g) az(g) gx(d/s) gy(d/s) gz(d/s)" + Character.LINE_SEPARATOR);
                }
            }

        } catch (IOException ex) {
            JOptionPane.showMessageDialog(null, ex.toString(), "Couldn't create output file stream", JOptionPane.WARNING_MESSAGE, null);
        }

        if (rewindBeforeRecording) {
            ignoreRewinwdEventFlag = true;
            chip.getAeViewer().getAePlayer().rewind();
        }
        setWriteEnabled(true);
    }

    synchronized public void doCloseFiles() {
        setWriteEnabled(false);
        if (!isFilesOpen()) {
            log.info("No files open, nothing to close");
            setEventsWritten(0);
            return;
        }
        try {
            for (FileWriter f : getFileWriters()) {
                f.close();
            }
            getFileWriters().clear();
            dvsWriter = null;
            imuWriter = null;
            apsWriter = null;
            log.info("total " + eventsWritten + " events were written to files " + lastFileName + "-XXX.txt");
            showPlainMessageDialogInSwingThread("Closed files " + lastFileName + " after " + eventsWritten + " events were written", "Files closed");
            setEventsWritten(0);
        } catch (Exception ex) {
            log.warning(ex.toString());
            ex.printStackTrace();
            getFileWriters().clear();
        }
    }

    /**
     * @return the rewindBeforeRecording
     */
    public boolean isRewindBeforeRecording() {
        return rewindBeforeRecording;
    }

    /**
     * @param rewindBeforeRecording the rewindBeforeRecording to set
     */
    public void setRewindBeforeRecording(boolean rewindBeforeRecording) {
        this.rewindBeforeRecording = rewindBeforeRecording;
        putBoolean("rewindBeforeRecording", rewindBeforeRecording);
    }

//    /**
//     * Turns gl to BufferedImage with fixed format
//     *
//     * @param gl
//     * @param w
//     * @param h
//     * @return
//     */
//    protected BufferedImage toImage(GL2 gl, int w, int h) {
//
//        gl.glReadBuffer(GL.GL_FRONT); // or GL.GL_BACK
//        ByteBuffer glBB = Buffers.newDirectByteBuffer(4 * w * h);
//        gl.glReadPixels(0, 0, w, h, GL2.GL_BGRA, GL.GL_BYTE, glBB);
//
//        BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_BGR);
//        int[] bd = ((DataBufferInt) bi.getRaster().getDataBuffer()).getData();
//
//        for (int y = 0; y < h; y++) {
//            for (int x = 0; x < w; x++) {
//                int b = 2 * glBB.get();
//                int g = 2 * glBB.get();
//                int r = 2 * glBB.get();
//                int a = glBB.get(); // not using
//
//                bd[(h - y - 1) * w + x] = (b << 16) | (g << 8) | r | 0xFF000000;
//            }
//        }
//
//        return bi;
//    }
    protected void writeTimecode(int timestamp) throws IOException {
        if (timecodeWriter != null) {
            timecodeWriter.write(String.format("%d %d\n", eventsWritten, timestamp));
        }
    }

    protected void incrementCountAndMaybeCloseOutput() {
        eventsWritten++;
        if (maxEvents > 0 && eventsWritten >= maxEvents && isFilesOpen()) {
            log.info("wrote maxEvents=" + maxEvents + " events; closing files");
            doCloseFiles();
        }
        if (isFilesOpen() && eventsWritten % LOG_EVERY_THIS_MANY_EVENTS == 0) {
            log.info(String.format("wrote %d events", eventsWritten));
            getSupport().firePropertyChange("eventsWritten", null, eventsWritten);
        }
    }

    private boolean isFilesOpen() {
        return !fileWriters.isEmpty();
    }

    /**
     * @return the maxEvents
     */
    public int getMaxEvents() {
        return maxEvents;
    }

    /**
     * @param maxEvents the maxEvents to set
     */
    public void setMaxEvents(int maxEvents) {
        this.maxEvents = maxEvents;
        putInt("maxEvents", maxEvents);
    }

    /**
     * @return the eventsWritten
     */
    public int getEventsWritten() {
        return eventsWritten;
    }

    /**
     * @return the closeOnRewind
     */
    public boolean isCloseOnRewind() {
        return closeOnRewind;
    }

    /**
     * @param closeOnRewind the closeOnRewind to set
     */
    public void setCloseOnRewind(boolean closeOnRewind) {
        this.closeOnRewind = closeOnRewind;
        putBoolean("closeOnRewind", closeOnRewind);
    }

    /**
     * @param eventsWritten the eventsWritten to set
     */
    public void setEventsWritten(int eventsWritten) {
        int old = this.eventsWritten;
        this.eventsWritten = eventsWritten;
        if (eventsWritten % LOG_EVERY_THIS_MANY_EVENTS == 0) {
            getSupport().firePropertyChange("eventsWritten", old, eventsWritten);
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName() == AEInputStream.EVENT_REWOUND) {
            if (!ignoreRewinwdEventFlag && closeOnRewind && isFilesOpen()) {
                doCloseFiles();
            }
            ignoreRewinwdEventFlag = false;
        }
    }

    /**
     * @return the additionalComments
     */
    public ArrayList<String> getAdditionalComments() {
        return additionalComments;
    }

    /**
     * Sets array of additional comment strings to be written to timecode file.
     *
     * @param additionalComments the additionalComments to set
     */
    public void setAdditionalComments(ArrayList<String> additionalComments) {
        this.additionalComments = additionalComments;
    }

    /**
     * Returns last file written
     *
     * @return the File written
     */
    public File getFile() {
        return lastFile;
    }

    /**
     * @return the writeOnlyWhenMousePressed
     */
    public boolean isWriteOnlyWhenMousePressed() {
        return writeOnlyWhenMousePressed;
    }

    /**
     * @param writeOnlyWhenMousePressed the writeOnlyWhenMousePressed to set
     */
    public void setWriteOnlyWhenMousePressed(boolean writeOnlyWhenMousePressed) {
        this.writeOnlyWhenMousePressed = writeOnlyWhenMousePressed;
        putBoolean("writeOnlyWhenMousePressed", writeOnlyWhenMousePressed);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (writeOnlyWhenMousePressed) {
            setWriteEnabled(false);
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (writeOnlyWhenMousePressed) {
            setWriteEnabled(true);
        }
    }

    public boolean isWriteEnabled() {
        return writeEnabled;
    }

    public void setWriteEnabled(boolean yes) {
        boolean old = this.writeEnabled;
        writeEnabled = yes;
        getSupport().firePropertyChange("writeEnabled", old, yes);
    }

    /**
     * @return the list of fileOutputStream
     */
    public ArrayList<FileWriter> getFileWriters() {
        return fileWriters;
    }

    /**
     * Returns true if the files doesn't exist or it is OK to overwrite it
     *
     * @param fn filename
     * @return true if OK to overwrite
     */
    private boolean checkFileExists(String fn) {
        if (new File(fn).exists()) {
            int r = JOptionPane.showConfirmDialog(null, "File " + fn + " already exists, overwrite it?");
            if (r == JOptionPane.OK_OPTION) {
                return true;
            } else {
                return false;
            }
        } else {
            return true;
        }
    }

    @Override
    public void resetFilter() {
    }

    @Override
    public void initFilter() {
    }

}