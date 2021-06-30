/*
 * Copyright (C) 2021 tobid.
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
package net.sf.jaer.eventprocessing.filter;

import ch.unizh.ini.jaer.projects.humanpose.AbstractDavisCNNProcessor;
import ch.unizh.ini.jaer.projects.humanpose.DavisClassifierCNNProcessor;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.util.awt.TextRenderer;
import java.io.File;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import net.sf.jaer.Description;
import net.sf.jaer.DevelopmentStatus;
import net.sf.jaer.chip.AEChip;
import net.sf.jaer.event.BasicEvent;
import net.sf.jaer.event.EventPacket;
import static net.sf.jaer.eventprocessing.EventFilter.log;
import net.sf.jaer.eventprocessing.TimeLimiter;
import net.sf.jaer.graphics.MultilineAnnotationTextRenderer;
import org.tensorflow.Graph;
import org.tensorflow.Operation;
import org.tensorflow.SavedModelBundle;
import org.tensorflow.Session;
import org.tensorflow.Tensor;

/**
 * Noise filter that runs a DNN to denoise events
 *
 * @author Tobi Delbruck and Shasha Guo, 2021
 */
@Description("Denoising noise filter that uses a DNN deep neural network to classify events as signal or noise events")
@DevelopmentStatus(DevelopmentStatus.Status.Experimental)
public class DNNNoiseFilter extends AbstractNoiseFilter {

    private final String KEY_NETWORK_FILENAME = "lastNetworkFilename";
    private String lastManuallyLoadedNetwork = getString("lastManuallyLoadedNetwork", ""); // stores filename and path to last successfully loaded network that user loaded via doLoadNetwork
    private TextRenderer textRenderer = null;
    private boolean measurePerformance = getBoolean("measurePerformance", true);

    private String performanceString = null; // holds string representation of processing time
    private TimeLimiter timeLimiter = new TimeLimiter(); // private instance used to accumulate events to slices even if packet has timed out
    private int processingTimeLimitMs = getInt("processingTimeLimitMs", 100); // time limit for processing packet in ms to process OF events (events still accumulate). Overrides the system EventPacket timelimiter, which cannot be used here because we still need to accumulate and render the events.
    private String lastPerformanceString = null;

    private int sxm1; // size of chip minus 1
    private int sym1;
    private int ssx; // size of subsampled timestamp map
    private int ssy;

    private SavedModelBundle tfSavedModelBundle = null;
    private Graph tfExecutionGraph = null;
    private Session tfSession = null;
    private int tfBatchSizeEvents = getInt("tfBatchSizeEvents", 128);
    private int tfNumInBatchSoFar = 0;
    private FloatBuffer tfInputFloatBuffer = null;
    private ArrayList<BasicEvent> eventList = new ArrayList(tfBatchSizeEvents);
    protected float signalClassifierThreshold = getFloat("signalClassifierThreshold", 0.5f);

    public enum TIPatchMethod {
        ExponentialDecay, LinearDecay
    };
    private TIPatchMethod tiPatchMethod =  TIPatchMethod.valueOf(getString("tiPatchMethod", TIPatchMethod.ExponentialDecay.toString()));

    private int patchWidthAndHeightPixels = getInt("patchWidthAndHeightPixels", 11);
    private int[][] timestampImage; // timestamp image

    public DNNNoiseFilter(AEChip chip) {
        super(chip);
        String deb = "5. Debug", disp = "2. Display", anal = "4. Analysis", tf = "0. Tensorflow", input = "1. Input";
        setPropertyTooltip(tf, "loadNetwork", "Load a protobuf .pb file containing the network or select a folder holding SavedModelBundle");
        setPropertyTooltip(tf, "lastManuallyLoadedNetwork", "last network we manually loaded");
        setPropertyTooltip(disp, "measurePerformance", "Measures and logs time in ms to process each frame along with estimated operations count (MAC=2OPS)");
        setPropertyTooltip(tf, "inputLayerName", "(TensorFlow only) Input layer; parse it from loading the network and examining console output for layers for lines starting with ****");
        setPropertyTooltip(tf, "outputLayerName", "(TensorFlow only) Output layer; parse it from loading the network and examining console output for layers for lines starting with ****");
        setPropertyTooltip(tf, "tfBatchSizeEvents", "Number of events to process in parallel for inference");
        setPropertyTooltip(tf, "patchWidthAndHeightPixels", "Dimension (width and height in pixels) of the timestamp image input to DNN around each event (default 11)"); // TODO fix default to match training
        setPropertyTooltip(tf, "signalClassifierThreshold", "threshold for clasifying event as signal"); // TODO fix default to match training
        setPropertyTooltip(tf, "tiPatchMethod", "method used to compute the value of the timestamp image patch values"); // TODO fix default to match training
    }

    @Override
    synchronized public EventPacket<? extends BasicEvent> filterPacket(EventPacket<? extends BasicEvent> in) {
        super.filterPacket(in);
        if (timestampImage == null) {
            allocateMaps(chip);
        }
        int tauUs = (int) Math.round(getCorrelationTimeS() * 1e6f);
        ssx = sxm1 >> subsampleBy;
        ssy = sym1 >> subsampleBy;

        for (BasicEvent e : in) {
            if (e == null) {
                continue;
            }
            if (e.isSpecial()) {
                continue;
            }
            totalEventCount++;
            final int ts = e.timestamp;
            final int x = (e.x >> subsampleBy), y = (e.y >> subsampleBy); // subsampling address
            if ((x < 0) || (x > ssx) || (y < 0) || (y > ssy)) { // out of bounds, discard (maybe bad USB or something)
                filterOut(e);
                continue;
            }
            if (timestampImage[x][y] == DEFAULT_TIMESTAMP) { // if it is the first event at this pixel
                timestampImage[x][y] = ts; // save timestamp
                if (letFirstEventThrough) {
                    filterIn(e); // if we let through every first event at each pixel, filter it in
                    continue;
                } else {
                    filterOut(e); // otherwise filter out
                    continue;
                }
            }

            // make timestamp image patch to classify
            int radius = (patchWidthAndHeightPixels - 1) / 2;
            tfNumInBatchSoFar++;
            eventList.add(e); // add the event object to list so we can later filter it in or not, in the original order
            for (int indx = x - radius; indx <= x + radius; indx++) {
                // iterate over NNb, computing the TI patch value
                for (int indy = y - radius; indy <= y + radius; indy++) {
                    if (indx < 0 || indx >= ssx || indy < 0 || indy > ssy) {
                        tfInputFloatBuffer.put(0); // For NNbs that are outside chip address space, set the TI patch input to zero
                        continue;
                    }
                    int nnbTs = timestampImage[indx][indy]; // NNb timestamp 
                    if (nnbTs == DEFAULT_TIMESTAMP) {
                        tfInputFloatBuffer.put(0); // if the NNb pixel had no event, then just write 0 to TI patch
                    } else {
                        float dt = nnbTs - ts; // dt is negative delta time, i.e. the time in us of NNb event relative to us.  When NNb ts is older, dt is more negative
                        switch (tiPatchMethod) {
                            case ExponentialDecay:
                                float expDt = (float) Math.exp(dt / tauUs);  // Compute exp(-dt/tau) that decays to zero for very old events in NNb
                                tfInputFloatBuffer.put(expDt);
                                break;
                            case LinearDecay:
                                float linearDt;
                                if (-dt > tauUs) {
                                    linearDt = 0;
                                } else {
                                    linearDt = 1 - (float) (-dt / tauUs);  // if dt is 0, then linearDt is 1, if dt=-tauUs, then linearDt=0
                                }
                                tfInputFloatBuffer.put(linearDt);
                        }
                    }
                }
            }
            if (tfNumInBatchSoFar >= tfBatchSizeEvents) { // if we have a full batch, classify the events in it
                classifyEvents();
            }
            // write TI *after* we classify S vs N
            timestampImage[x][y] = ts;

        } // event packet loop

        // classify remaining events as final batch for this packet, so that we process all events in packet even if only a few
        if (tfNumInBatchSoFar > 0) {
            classifyEvents();
        }

        getNoiseFilterControl().maybePerformControl(in);
        return in;
    }

    /**
     * Classifies events in the assembled batch
     *
     */
    private void classifyEvents() {
        tfInputFloatBuffer.flip();
        // Create input tensor with channel first. Each event's input TI patch is a vector arranged according to for loop order above,
        // i.e. y last order, with y index changing fastest.
        Tensor<Float> tfInputTensor = Tensor.create(new long[]{tfNumInBatchSoFar, patchWidthAndHeightPixels * patchWidthAndHeightPixels}, tfInputFloatBuffer);
        try {
            if (tfSession == null) {
                tfSession = new Session(tfExecutionGraph);
            }
            List<Tensor<?>> tfOutputs = tfSession.runner().feed("input", tfInputTensor).fetch("output/Sigmoid").run();
            Tensor<Float> tfOutput = tfOutputs.get(0).expect(Float.class);
            final long[] rshape = tfOutput.shape();
            if (tfOutput.numDimensions() != 2 || rshape[0] != tfNumInBatchSoFar || rshape[1] != 1) {
                throw new RuntimeException(
                        String.format(
                                "Expected model to produce a [N 1] shaped tensor where N is the tfBatchSizeEvents, instead it produced one with shape %s",
                                Arrays.toString(rshape)));
            }
            int nevents = (int) rshape[0];
            if (nevents != tfNumInBatchSoFar) {
                throw new RuntimeException("got " + nevents + " outputs from network; expected " + tfNumInBatchSoFar);
            }
            float[][] output2d = new float[nevents][1];
            tfOutput.copyTo(output2d);
            tfOutput.close();
            float[] outputVector = new float[nevents];
            for (int i = 0; i < nevents; i++) {
                // TODO ugly
                outputVector[i] = output2d[i][0];
            }

            int idx = 0;
            for (BasicEvent ev : eventList) {
                float scalarClassification = outputVector[idx];
                if (scalarClassification > signalClassifierThreshold) {
                    filterIn(ev);
                } else {
                    filterOut(ev);
                }
                idx++;
            }
            tfInputFloatBuffer.clear();
            eventList.clear();
            tfNumInBatchSoFar = 0;
        } catch (Exception ex) {
            log.log(Level.SEVERE, "Exception running network: " + ex.toString(), ex.getCause());
            if (tfSession != null) {
                tfSession.close();
                tfSession = null;
                setFilterEnabled(false);
                resetFilter();
            }
        }
    }

    @Override
    public String infoString() {
        String s = getClass().getSimpleName();
        s = s.replaceAll("[a-z]", "");
        s = s + String.format(": tau=%ss subSamp=%d TI dim=%dx%d threshold=%.2f",
                eng.format(getCorrelationTimeS()),
                getSubsampleBy(),
                patchWidthAndHeightPixels, patchWidthAndHeightPixels,
                getSignalClassifierThreshold()
        );
        return s;
    }

    @Override
    public void initFilter() {
        // if dnn was loaded before, load it now
        if (preferenceExists(KEY_NETWORK_FILENAME) && tfExecutionGraph == null) {
            File f = new File(getString(KEY_NETWORK_FILENAME, ""));
            if (f.exists() && f.isFile()) {
                try {
                    loadNetwork(f);
                } catch (Exception ex) {
                    Logger.getLogger(AbstractDavisCNNProcessor.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        sxm1 = chip.getSizeX() - 1;
        sym1 = chip.getSizeY() - 1;
        ssx = sxm1 >> subsampleBy;
        ssy = sym1 >> subsampleBy;
        int ninputsTotal = tfBatchSizeEvents * patchWidthAndHeightPixels * patchWidthAndHeightPixels;
        tfInputFloatBuffer = FloatBuffer.allocate(ninputsTotal);
        allocateMaps(chip);
        resetFilter();

    }

    @Override
    public synchronized final void resetFilter() {
        super.resetFilter();
        if (timestampImage == null) {
            log.warning("tried to clear lastTimesMap but it is null");
            return;
        }
        for (int[] arrayRow : timestampImage) {
            Arrays.fill(arrayRow, DEFAULT_TIMESTAMP);
        }
        tfNumInBatchSoFar = 0;
        if (tfInputFloatBuffer != null) {
            tfInputFloatBuffer.clear();
        }
        eventList.clear();

    }

    private void allocateMaps(AEChip chip) {
        if ((chip != null) && (chip.getNumCells() > 0) && (timestampImage == null || timestampImage.length != chip.getSizeX() >> subsampleBy)) {
            timestampImage = new int[chip.getSizeX()][chip.getSizeY()]; // TODO handle subsampling to save memory (but check in filterPacket for range check optomization)
        }
    }

    @Override
    public void annotate(GLAutoDrawable drawable) {
        super.annotate(drawable);
        GL2 gl = drawable.getGL().getGL2();
        if (tfExecutionGraph != null) {
            MultilineAnnotationTextRenderer.resetToYPositionPixels(chip.getSizeY() * 1f);
            MultilineAnnotationTextRenderer.setScale(.3f);
            MultilineAnnotationTextRenderer.renderMultilineString(this.getClass().getSimpleName());
            if ((measurePerformance == true) && (performanceString != null) /*&& !performanceString.equals(lastPerformanceString)*/) {
                MultilineAnnotationTextRenderer.renderMultilineString(performanceString);
                lastPerformanceString = performanceString;
            }
        }
    }

    /**
     * Fills timestampImage with waiting times drawn from Poisson process with
     * rate noiseRateHz
     *
     * @param noiseRateHz rate in Hz
     * @param lastTimestampUs the last timestamp; waiting times are created
     * before this time
     */
    @Override
    public void initializeLastTimesMapForNoiseRate(float noiseRateHz, int lastTimestampUs) {
        Random random = new Random();
        for (final int[] arrayRow : timestampImage) {
            for (int i = 0; i < arrayRow.length; i++) {
                final double p = random.nextDouble();
                final double t = -noiseRateHz * Math.log(1 - p);
                final int tUs = (int) (1000000 * t);
                arrayRow[i] = lastTimestampUs - tUs;
            }
        }
    }

    /**
     * Loads a convolutional neural network (CNN) trained using DeapLearnToolbox
     * for Matlab (https://github.com/rasmusbergpalm/DeepLearnToolbox) that was
     * exported using Danny Neil's XML Matlab script cnntoxml.m.
     *
     */
    public synchronized void doLoadNetwork() {
        File file = null;
        file = openFileDialogAndGetFile("Choose a network, either tensorflow protobuf binary (pb),  or folder holding tensorflow SavedModelBundle",
                KEY_NETWORK_FILENAME, "",
                "CNN file", "pb");
        if (file == null) {
            return;
        }
        try {
            loadNetwork(file);
            setLastManuallyLoadedNetwork(file.toString()); // store the last manually loaded network as the 

        } catch (Exception ex) {
            Logger.getLogger(DavisClassifierCNNProcessor.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(chip.getAeViewer().getFilterFrame(), "Couldn't load net from this file, caught exception " + ex + ". See console for logging.", "Bad network file", JOptionPane.WARNING_MESSAGE);
        }
    }

    public void loadNetwork(File f) throws IOException {
        if (f == null) {
            throw new IOException("null file");
        }
        ArrayList<String> ioLayers = new ArrayList();

        try {
            if (f.isDirectory()) {
                log.info("loading \"serve\" graph from tensorflow SavedModelBundle folder " + f);
                tfSavedModelBundle = SavedModelBundle.load(f.getCanonicalPath(), "serve");
                tfExecutionGraph = tfSavedModelBundle.graph();
            } else {
                log.info("loading network from file " + f);
                byte[] graphDef = Files.readAllBytes(Paths.get(f.getAbsolutePath())); // "tensorflow_inception_graph.pb"
                tfExecutionGraph = new Graph();
                tfExecutionGraph.importGraphDef(graphDef);
            }

            Iterator<Operation> itr = tfExecutionGraph.operations();
            StringBuilder b = new StringBuilder("TensorFlow Graph: \n");
            int opnum = 0;
            ioLayers.clear();
            while (itr.hasNext()) {
                Operation o = itr.next();
                final String s = o.toString().toLowerCase();
//                if(s.contains("input") || s.contains("output") || s.contains("placeholder")){
                if (s.contains("input")
                        || s.contains("placeholder")
                        || s.contains("output")
                        || s.contains("prediction")) {  // find input placeholder & output
//                    int numOutputs = o.numOutputs();
//                    if(! s.contains("output_shape") && !s.contains("conv2d_transpos")){
                    b.append("********** ");
                    ioLayers.add(s);
//                    for (int onum = 0; onum < numOutputs; onum++) {
//                        Output output = o.output(onum);
//                        Shape shape = output.shape();
//                        int numDimensions = shape.numDimensions();
//                        for (int dimidx = 0; dimidx < numDimensions; dimidx++) {
//                            long dim = shape.size(dimidx);
//                        }
//                    }
//                    int inputLength=o.inputListLength("");
                    b.append(opnum++ + ": " + o.toString() + "\n");
//                    }
                }
            }
            log.info(b.toString());
        } catch (Exception e) {
            log.warning(e.toString());
            e.printStackTrace();
        }
    }

    private String getExtension(File f) {
        String ext = null;
        String s = f.getName();
        int i = s.lastIndexOf('.');

        if (i > 0 && i < s.length() - 1) {
            ext = s.substring(i + 1).toLowerCase();
        }
        return ext;
    }

    /**
     * Opens a file (do not accept directory) using defaults and previous stored
     * preference values
     *
     * @param tip the tooltip shown
     * @param key a string key to store preference value
     * @param defaultFile the default filename
     * @param type The file type string, e.g. "labels"
     * @param ext the allowed extensions as an array of strings
     * @return the file, or null if selection was canceled
     */
    protected File openFileDialogAndGetFile(String tip, String key, String defaultFile, String type, String... ext) {
        String name = getString(key, defaultFile);
        JFileChooser c = new JFileChooser(name);
        File f = new File(name);
        c.setCurrentDirectory(new File(getString(key, "")));
        c.setToolTipText(tip);
        FileFilter filt = new FileNameExtensionFilter(type, ext);
        c.setFileSelectionMode(JFileChooser.FILES_ONLY);
        c.addChoosableFileFilter(filt);
        c.setFileFilter(filt);
        c.setSelectedFile(f);
        int ret = c.showOpenDialog(chip.getAeViewer());
        if (ret != JFileChooser.APPROVE_OPTION) {
            return null;
        }
        name = c.getSelectedFile().toString();
        putString(key, name);
        File file = c.getSelectedFile();
        return file;
    }

    /**
     * @return the lastManuallyLoadedNetwork
     */
    public String getLastManuallyLoadedNetwork() {
        return lastManuallyLoadedNetwork;
    }

    /**
     * @param lastManuallyLoadedNetwork the lastManuallyLoadedNetwork to set
     */
    public void setLastManuallyLoadedNetwork(String lastManuallyLoadedNetwork) {
        this.lastManuallyLoadedNetwork = lastManuallyLoadedNetwork;
        putString("lastManuallyLoadedNetwork", lastManuallyLoadedNetwork);
    }

    /**
     * @return the measurePerformance
     */
    public boolean isMeasurePerformance() {
        return measurePerformance;
    }

    /**
     * @param measurePerformance the measurePerformance to set
     */
    public void setMeasurePerformance(boolean measurePerformance) {
        this.measurePerformance = measurePerformance;
        putBoolean("measurePerformance", measurePerformance);
    }

    /**
     * @return the signalClassifierThreshold
     */
    public float getSignalClassifierThreshold() {
        return signalClassifierThreshold;
    }

    /**
     * @param signalClassifierThreshold the signalClassifierThreshold to set
     */
    public void setSignalClassifierThreshold(float signalClassifierThreshold) {
        this.signalClassifierThreshold = signalClassifierThreshold;
        putFloat("signalClassifierThreshold", signalClassifierThreshold);
    }

    /**
     * @return the tfBatchSizeEvents
     */
    public int getTfBatchSizeEvents() {
        return tfBatchSizeEvents;
    }

    /**
     * @param tfBatchSizeEvents the tfBatchSizeEvents to set
     */
    synchronized public void setTfBatchSizeEvents(int tfBatchSizeEvents) {
        this.tfBatchSizeEvents = tfBatchSizeEvents;
        putInt("tfBatchSizeEvents", tfBatchSizeEvents);
        tfInputFloatBuffer.capacity();
        int ninputsTotal = tfBatchSizeEvents * patchWidthAndHeightPixels * patchWidthAndHeightPixels;
        tfInputFloatBuffer = FloatBuffer.allocate(ninputsTotal);
    }

    /**
     * @return the tiPatchMethod
     */
    public TIPatchMethod getTiPatchMethod() {
        return tiPatchMethod;
    }

    /**
     * @param tiPatchMethod the tiPatchMethod to set
     */
    public void setTiPatchMethod(TIPatchMethod tiPatchMethod) {
        this.tiPatchMethod = tiPatchMethod;
        putString("tiPatchMethod", tiPatchMethod.toString());
    }

}
