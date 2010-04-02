/*
 * BluringFilter2DTracker.java
 */
package ch.unizh.ini.jaer.projects.gesture.virtualdrummer;

import ch.unizh.ini.jaer.projects.gesture.virtualdrummer.BlurringFilter2D.CellGroup;
import com.sun.opengl.util.GLUT;
import net.sf.jaer.aemonitor.AEConstants;
import net.sf.jaer.chip.*;
import net.sf.jaer.eventprocessing.EventFilter2D;
import net.sf.jaer.graphics.*;
import java.awt.*;
import java.awt.geom.*;
import java.util.*;
import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import net.sf.jaer.event.EventPacket;
import net.sf.jaer.eventprocessing.FilterChain;

/**
 * Tracks moving objects. Modified from BluringFilter2DTracker.java
 *
 * @author Jun Haeng Lee, Tobi
 */
public class BluringFilter2DTracker extends EventFilter2D implements FrameAnnotater, Observer /*, PreferenceChangeListener*/ {
    // TODO split out the optical gryo stuff into its own subclass
    // TODO split out the Cluster object as it's own class.

    public static String getDescription() {
        return "Tracks moving hands, which means it tracks two object at most";
    }
    /** The list of clusters. */
    protected java.util.List<Cluster> clusters = new LinkedList<Cluster>();
    protected AEChip chip;
    private AEChipRenderer renderer;
    // Blurring filter to get clusters
    BlurringFilter2D bfilter;
    /** amount each event moves COM of cluster towards itself. */
    private int velocityPoints = getPrefs().getInt("HandTracker.velocityPoints", 10);
    private boolean pathsEnabled = getPrefs().getBoolean("HandTracker.pathsEnabled", true);
    private int pathLength = getPrefs().getInt("HandTracker.pathLength", 100);
    private boolean useVelocity = getPrefs().getBoolean("HandTracker.useVelocity", true); // enabling this enables both computation and rendering of cluster velocities
    private boolean showAllClusters = getPrefs().getBoolean("HandTracker.showAllClusters", false);
    private int predictiveVelocityFactor = 1;// making this M=10, for example, will cause cluster to substantially lead the events, then slow down, speed up, etc.
    private boolean enableClusterExitPurging = getPrefs().getBoolean("HandTracker.enableClusterExitPurging", true);
    private float velAngDiffDegToNotMerge = getPrefs().getFloat("HandTracker.velAngDiffDegToNotMerge", 60);
    private boolean showClusterNumber = getPrefs().getBoolean("HandTracker.showClusterNumber", false);
    private boolean showClusterVelocity = getPrefs().getBoolean("HandTracker.showClusterVelocity", false);
    private float velocityVectorScaling = getPrefs().getFloat("HandTracker.velocityVectorScaling", 1);
    private final float VELOCITY_VECTOR_SCALING = 1e6f; // to scale rendering of cluster velocityPPT vector, velocityPPT is in pixels/tick=pixels/us so this gives 1 screen pixel per 1 pix/s actual vel
    private int loggingIntervalUs = getPrefs().getInt("HandTracker.loggingIntervalUs", 1000);
    private boolean initializeVelocityToAverage = getPrefs().getBoolean("HandTracker.initializeVelocityToAverage", false);
    private Point2D.Float averageVelocityPPT = new Point2D.Float();
    private final float averageVelocityMixingFactor = 0.1f; // average velocities of all clusters mixed with this factor to produce this "prior" on initial cluster velocity
    private boolean showClusterMass = getPrefs().getBoolean("HandTracker.showClusterMass", false);
    private int maximumClusterAgeFrames = getPrefs().getInt("HandTracker.maximumClusterAgeFrames", 20);


    /**
     * Creates a new instance of BluringFilter2DTracker.
     *
     *
     */
    public BluringFilter2DTracker(AEChip chip) {
        super(chip);
        this.chip = chip;
        renderer = (AEChipRenderer) chip.getRenderer();
        initFilter();
        chip.addObserver(this);
        final String sizing = "Sizing", movement = "Movement", lifetime = "Lifetime", disp = "Display", global = "Global", update = "Update", logging = "Logging";
        setPropertyTooltip(lifetime, "enableClusterExitPurging", "enables rapid purging of clusters that hit edge of scene");
        setPropertyTooltip(global, "maximumClusterAgeFrames", "cluster is expired if it's updated during this number of frames");
        setPropertyTooltip(movement, "velocityPoints", "the number of recent path points (one per packet of events) to use for velocity vector regression");
        setPropertyTooltip(disp, "pathsEnabled", "draw paths of clusters over some window");
        setPropertyTooltip(disp, "pathLength", "paths are at most this many packets long");
        setPropertyTooltip(update, "growMergedSizeEnabled", "enabling makes merged clusters take on sum of sizes, otherwise they take on size of older cluster");
        setPropertyTooltip(movement, "useVelocity", "uses measured cluster velocity to predict future position; vectors are scaled " + String.format("%.1f pix/pix/s", VELOCITY_VECTOR_SCALING / AEConstants.TICK_DEFAULT_US * 1e-6));
        setPropertyTooltip(disp, "classifierEnabled", "colors clusters based on single size metric");
        setPropertyTooltip(disp, "classifierThreshold", "the boundary for cluster size classification in fractions of chip max dimension");
        setPropertyTooltip(disp, "showAllClusters", "shows all clusters, not just those with sufficient support");
        setPropertyTooltip(movement, "predictiveVelocityFactor", "how much cluster position leads position based on estimated velocity");
        setPropertyTooltip(update, "velAngDiffDegToNotMerge", "relative angle in degrees of cluster velocity vectors to not merge overlapping clusters");
        setPropertyTooltip(disp, "showClusterVelocity", "annotates velocity in pixels/second");
        setPropertyTooltip(disp, "showClusterNumber", "shows cluster ID number");
        setPropertyTooltip(disp, "showClusterMass", "shows cluster mass");
        setPropertyTooltip(disp, "velocityVectorScaling", "scaling of drawn velocity vectors");
        setPropertyTooltip(logging, "logDataEnabled", "writes a cluster log file called HandTrackerLog.txt in the startup folder host/java");
        setPropertyTooltip(logging, "loggingIntervalUs", "interval in us between logging cluster info to logging file");
        setPropertyTooltip(movement, "initializeVelocityToAverage", "initializes cluster velocity to moving average of cluster velocities; otherwise initialized to zero");

        bfilter = new BlurringFilter2D(chip);
        setEnclosedFilterChain(new FilterChain(chip));
        getEnclosedFilterChain().add(bfilter);
        bfilter.setEnclosed(true, this);
    }


    /**
     * merge clusters that are too close to each other and that have sufficiently similar velocities (if velocityRatioToNotMergeClusters).
    this must be done interatively, because merging 4 or more clusters feedforward can result in more clusters than
    you start with. each time we merge two clusters, we start over, until there are no more merges on iteration.
    for each cluster, if it is close to another cluster then merge them and start over.
     */
    private void mergeClusters() {
        boolean mergePending;
        Cluster c1 = null;
        Cluster c2 = null;
        do {
            mergePending = false;
            int nc = clusters.size();
            outer:
            for (int i = 0; i < nc; i++) {
                c1 = clusters.get(i);
                for (int j = i + 1; j < nc; j++) {
                    c2 = clusters.get(j); // get the other cluster
                    final boolean overlapping = c1.distanceTo(c2) < (c1.getOutterRadius() + c2.getOutterRadius());
                    boolean velSimilar = true; // start assuming velocities are similar
                    if (overlapping && velAngDiffDegToNotMerge > 0 && c1.isVelocityValid() && c2.isVelocityValid() && c1.velocityAngleTo(c2) > velAngDiffDegToNotMerge * Math.PI / 180) {
                        // if velocities valid for both and velocities are sufficiently different
                        velSimilar = false; // then flag them as different velocities
                    }
                    if (overlapping && velSimilar) {
                        // if cluster is close to another cluster, merge them
                        // if distance is less than sum of radii merge them and if velAngle < threshold
                        mergePending = true;
                        break outer; // break out of the outer loop
                    }
                }
            }
            if (mergePending && c1 != null && c2 != null) {
//                System.out.print("Cluster_"+c1.getClusterNumber()+"("+c1.firstEventTimestamp+") and cluster_"+c2.getClusterNumber()+"("+c2.firstEventTimestamp+ ") are merged to ");
                clusters.add(new Cluster(c1, c2));
                clusters.remove(c1);
                clusters.remove(c2);

//                System.out.print("Age of "+clusters.size()+" clusters after merging : ");
//                for (Cluster c : clusters) {
//                    System.out.print("cluster("+c.getClusterNumber()+")-"+c.getAgeFrames()+", ");
//                }
//                System.out.println("");
            }
        } while (mergePending);
        
    }

    /**
     * @return the enableClusterExitPurging
     */
    public boolean isEnableClusterExitPurging() {
        return enableClusterExitPurging;
    }

    /**
    Enables rapid purging of clusters that hit the edge of the scene.

     * @param enableClusterExitPurging the enableClusterExitPurging to set
     */
    public void setEnableClusterExitPurging(boolean enableClusterExitPurging) {
        this.enableClusterExitPurging = enableClusterExitPurging;
        getPrefs().putBoolean("HandTracker.enableClusterExitPurging", enableClusterExitPurging);
    }


    public void initFilter() {
    }

    protected LinkedList<Cluster> pruneList = new LinkedList<Cluster>();

     /** Prunes out old clusters that don't have support or that should be purged for some other reason.
     */
    private void pruneClusters() {
//        System.out.println(pruneList.size()+ " clusters are removed");
        clusters.removeAll(pruneList);
        pruneList.clear();
    }

    /** This method updates the list of clusters, pruning and
     * merging clusters and updating positions based on cluster velocities.
     * It also updates the optical gyro if enabled.
     *
     * @param t the global timestamp of the update.
     */
    private void updateClusterList(int t) {
        mergeClusters();
        pruneClusters();
        updateClusterLocations(t);
        updateClusterPaths(t);
    }

        /** Processes the incoming events to output HandTrackerEvent's.
     *
     * @param in
     * @return packet of HandTrackerEvent.
     */
    public EventPacket<?> filterPacket(EventPacket<?> in) {
//        EventPacket out; // TODO check use of out packet here, doesn't quite make sense
        if (in == null) {
            return null;
        }
        bfilter.filterPacket(in);

        for (Cluster c : clusters)
            c.decreaseAgeFrames();

        int updateTimestamp = 0;
        for (CellGroup cg : bfilter.getCellGroup()){
            track(cg);
            if(cg.getLastEventTimestamp() > updateTimestamp)
                updateTimestamp = cg.getLastEventTimestamp();
        }

        for (Cluster c : clusters) {
            if(c.getAgeFrames() <= 0)
                pruneList.add(c);
        }

        updateClusterList(updateTimestamp);


//        System.out.print("Age of "+clusters.size()+" clusters @" + updateTimestamp + " : ");
//        for (Cluster c : clusters) {
//            System.out.print("cluster("+c.getClusterNumber()+")-"+c.getAgeFrames()+", ");
//        }
//        System.out.println("");

        return in;
    }

    // the method that actually does the tracking
    synchronized void track(CellGroup cellGroup) {
        if (cellGroup.getNumMemberCells() == 0)
            return;

        // record cluster locations before packet is processed
//        for (Cluster c : clusters) {
//            c.getLastPacketLocation().setLocation(c.location);
//        }

        // for input cell group, see which cluster it is closest to and add it to this cluster.
        // if its too far from any cluster, make a new cluster if we can
        Cluster closest = null;
        closest = getNearestCluster(cellGroup); // find cluster that event falls within (or also within surround if scaling enabled)

        if (closest != null) {
            closest.addGroup(cellGroup);
        } else { // start a new cluster
            Cluster newCluster = null;
            newCluster = new Cluster(cellGroup);
            clusters.add(newCluster);
        }
    }

    /** Returns total number of clusters, including those that have been
     * seeded but may not have received sufficient support yet.
     * @return number of Cluster's in clusters list.
     */
    public int getNumClusters() {
        return clusters.size();
    }


    @Override
    public String toString() {
        String s = clusters != null ? Integer.toString(clusters.size()) : null;
        String s2 = "HandTracker with " + s + " clusters ";
        return s2;
    }

    private Cluster getNearestCluster(CellGroup cg) {
        float minDistance = Float.MAX_VALUE;
        ArrayList <Cluster> candidateClusters = new ArrayList();
        ArrayList <Integer> distanceList = new ArrayList();
        Cluster closest = null;
        float currentDistance = 0;

        for (Cluster c : clusters) {
            float dx, dy;
            float radius = c.getMaxRadius()*0.7f;

            if(radius > cg.getOutterRadiusPixels()){ // cg is smaller than cluster
                if ((dx = c.distanceToX(cg)) < radius && (dy = c.distanceToY(cg)) < radius){
                    candidateClusters.add(c);
                    distanceList.add(new Integer((int) (dx+dy)));
                }
            }else{ // cluster is smaller than cg
                if ((dx = c.distanceToX(cg)) < cg.getOutterRadiusPixels() && (dy = c.distanceToY(cg)) < cg.getOutterRadiusPixels()){
                    candidateClusters.add(c);
                    distanceList.add(new Integer((int) (dx+dy)));
                }
            }
        }
        
        if(candidateClusters.size() > 0){
            Iterator cItr = candidateClusters.iterator();
            Iterator dItr = distanceList.iterator();
            
            while(cItr.hasNext()){
                Cluster c = (Cluster) cItr.next();
                int dis = ((Integer) dItr.next()).intValue();
                if (dis < minDistance) {
                        closest = c;
                        minDistance = dis;
                }
            }
            
            closest.increaseAgeFrames();
        }
        
        return closest;
    }
    protected int clusterCounter = 0; // keeps track of absolute cluster number

    /** Updates cluster locations based on cluster velocities, if {@link #useVelocity} is enabled.
     *
     * @param t the global timestamp of the update.
     */
    private void updateClusterLocations(int t) {
        if (!useVelocity) {
            return;
        }
        for (Cluster c : clusters) {
            if (c.isVelocityValid()) {
                int dt = t - c.lastUpdateTime;
                if (dt <= 0) {
                    continue; // bogus timestamp or doesn't need update
                }
                c.location.x += c.velocityPPT.x * dt;
                c.location.y += c.velocityPPT.y * dt;
                if (initializeVelocityToAverage) {
                    averageVelocityPPT.x = (1 - averageVelocityMixingFactor) * averageVelocityPPT.x + averageVelocityMixingFactor * c.velocityPPT.x;
                    averageVelocityPPT.y = (1 - averageVelocityMixingFactor) * averageVelocityPPT.y + averageVelocityMixingFactor * c.velocityPPT.y;
                }
                c.lastUpdateTime = t;
            }
        }
    }

    /** Updates cluster path lists and counts number of visible clusters.
     *
     * @param t the update timestamp
     */
    private void updateClusterPaths(int t) {
        // update paths of clusters
        for (Cluster c : clusters) {
            c.updatePath(t);
        }
    }


    public class Cluster {
        /** location of cluster in pixels */
        public Point2D.Float location = new Point2D.Float(); // location in chip pixels
        private Point2D.Float birthLocation = new Point2D.Float(); // birth location of cluster
        private Point2D.Float lastPacketLocation = new Point2D.Float(); // location at end of last packet, used for movement sample
        /** velocityPPT of cluster in pixels/tick, where tick is timestamp tick (usually microseconds) */
        protected Point2D.Float velocityPPT = new Point2D.Float(); // velocityPPT in chip pixels/tick
        private Point2D.Float velocityPPS = new Point2D.Float(); // cluster velocityPPT in pixels/second
        private boolean velocityValid = false; // used to flag invalid or uncomputable velocityPPT
        final float VELPPS_SCALING = 1e6f / AEConstants.TICK_DEFAULT_US;
        private float innerRadius, outterRadius, maxRadius; // in chip chip pixels
        protected ArrayList<PathPoint> path = new ArrayList<PathPoint>(getPathLength());
        int hitEdgeTime = 0;
        protected boolean hitEdge = false;
        protected int ageFrames = 0;


        /** Returns true if the cluster center is outside the array or if this test is enabled and if the
        cluster has hit the edge of the array and has been there at least the
        minimum time for support.
        @return true if cluster has hit edge for long
        enough (getClusterLifetimeWithoutSupportUs) and test enableClusterExitPurging
         */
        private boolean hasHitEdge() {
            return hitEdge;
        }


        /**
         * Cluster velocity in pixels/timestamp tick as a vector. Velocity values are set during cluster upate.
         *
         * @return the velocityPPT in pixels per timestamp tick.
         * @see #getVelocityPPS()
         */
        public Point2D.Float getVelocityPPT() {
            return velocityPPT;
        }

        /**
         * The location of the cluster at the end of the previous packet.
         * Can be used to measure movement of cluster during this
         * packet.
         * @return the lastPacketLocation.
         */
        public Point2D.Float getLastPacketLocation() {
            return lastPacketLocation;
        }


        /** One point on a Cluster's path */
        public class PathPoint extends Point2D.Float {

            private int t; // timestamp of this point
            private int nEvents; // num events contributed to this point

            /** constructs new PathPoint with given x,y,t and numEvents fields
            @param numEvents the number of events associated with this point; used in velocityPPT estimation
             */
            public PathPoint(float x, float y, int t, int numEvents) {
                this.x = x;
                this.y = y;
                this.t = t;
                this.nEvents = numEvents;
            }

            public int getT() {
                return t;
            }

            public void setT(int t) {
                this.t = t;
            }

            public int getNEvents() {
                return nEvents;
            }

            public void setNEvents(int nEvents) {
                this.nEvents = nEvents;
            }
        }
        private RollingVelocityFitter velocityFitter = new RollingVelocityFitter(path, velocityPoints);
        /** Rendered color of cluster. */
        protected Color color = null;
        /** Number of events collected by this cluster.*/
        protected int numEvents = 0;
        /** Number of cells collected by this cluster.*/
        protected int numCells = 0;
        /** The "mass" of the cluster is the weighted number of events it has collected.
         */
        protected float mass;
        /** First and last timestamp of cluster. <code>firstEventTimestamp</code> is updated when cluster becomes visible.
         * <code>lastEventTimestamp</code> is the last time the cluster was touched either by an event or by
         * some other timestamped update, e.g. {@link #updateClusterList(net.sf.jaer.event.EventPacket, int) }.
         * @see #isVisible()
         */

        protected int lastEventTimestamp, firstEventTimestamp;
        /** This is the last time in timestamp ticks that the cluster was updated, either by an event
         * or by a regular update such as {@link #updateClusterLocations(int)}. This time can be used to
         * compute postion updates given a cluster velocity and time now.
         */
        protected int lastUpdateTime;
        /** assigned to be the absolute number of the cluster that has been created. */
        private int clusterNumber;
        private float[] rgb = new float[4];

        /** Constructs a default cluster. */
        public Cluster() {
            float hue = random.nextFloat();
            Color c = Color.getHSBColor(hue, 1f, 1f);
            setColor(c);
            setClusterNumber(clusterCounter++);
            if (initializeVelocityToAverage) {
                velocityPPT.x = averageVelocityPPT.x;
                velocityPPT.y = averageVelocityPPT.y;
                velocityValid = true;
            }
            maxRadius = 0;
        }

        /** Overrides default hashCode to return {@link #clusterNumber}. This overriding
         * allows for storing clusters in lists and checking for them by their clusterNumber.
         *
         * @return clusterNumber
         */
        @Override
        public int hashCode() {
            return clusterNumber;
        }

        /** Two clusters are equal if their {@link #clusterNumber}'s are equal.
         *
         * @param obj another Cluster.
         * @return true if equal.
         */
        @Override
        public boolean equals(Object obj) { // derived from http://www.geocities.com/technofundo/tech/java/equalhash.html
            if (this == obj) {
                return true;
            }
            if ((obj == null) || (obj.getClass() != this.getClass())) {
                return false;
            }
            // object must be Test at this point
            Cluster test = (Cluster) obj;
            return clusterNumber == test.clusterNumber;
        }

        /** Constructs a cluster 
         * The numEvents, location, birthLocation, first and last timestamps are set.
         * @param cg the cell group.
         */
        public Cluster(CellGroup cg) {
            this();
            location = cg.getLocation();
            birthLocation = cg.getLocation();
            lastPacketLocation =cg.getLocation();
            lastEventTimestamp = cg.getLastEventTimestamp();
            firstEventTimestamp = cg.getFirstEventTimestamp();
            lastUpdateTime = cg.getLastEventTimestamp();
            numEvents = cg.getNumEvents();
            numCells = cg.getNumMemberCells();
            mass = cg.getMass();
            ageFrames++;
            setRadius(cg);
            if((hitEdge = cg.isHitEdge()))
                hitEdgeTime = cg.getLastEventTimestamp();

//            System.out.println("Cluster_"+clusterNumber+" is created @"+firstEventTimestamp);
        }

        /** Constructs a cluster by merging two clusters.
         * All parameters of the resulting cluster should be reasonable combinations of the
         * source cluster parameters.
         * For example, the merged location values are weighted
         * by the mass of events that have supported each
         * source cluster, so that older clusters weigh more heavily
         * in the resulting cluster location. Subtle bugs or poor performance can result
         * from not properly handling the merging of parameters.
         *
         * @param one the first cluster
         * @param two the second cluster
         */
        public Cluster(Cluster one, Cluster two) {
            this();

            Cluster older = one.clusterNumber < two.clusterNumber ? one : two;
            clusterNumber = older.clusterNumber;
            // merge locations by average weighted by mass of events supporting each cluster
            mass = one.mass + two.mass;
            numEvents = one.numEvents + two.numEvents;
            numCells = one.numCells + two.numCells;
            location.x = (one.location.x * one.mass + two.location.x * two.mass) / (mass);
            location.y = (one.location.y * one.mass + two.location.y * two.mass) / (mass);

            lastEventTimestamp = one.lastEventTimestamp > two.lastEventTimestamp ? one.lastEventTimestamp : two.lastEventTimestamp;
            lastUpdateTime = lastEventTimestamp;
            lastPacketLocation.x = older.location.x;
            lastPacketLocation.y = older.location.y;
            firstEventTimestamp = one.firstEventTimestamp < two.firstEventTimestamp ? one.firstEventTimestamp : two.firstEventTimestamp;
            path = older.path;
            birthLocation = older.birthLocation;
            velocityFitter = older.velocityFitter;
            velocityPPT.x = older.velocityPPT.x;
            velocityPPT.y = older.velocityPPT.y;
            velocityPPS.x = older.velocityPPS.x;
            velocityPPS.y = older.velocityPPS.y;
            velocityValid = older.velocityValid;
            ageFrames = older.ageFrames;

            innerRadius = one.mass > two.mass ? one.innerRadius : two.innerRadius;
            outterRadius = one.mass > two.mass ? one.outterRadius : two.outterRadius;
            maxRadius = one.mass > two.mass ? one.maxRadius : two.maxRadius;
            setColor(older.getColor());

            if(one.hasHitEdge() || two.hasHitEdge()){
                hitEdge = true;
                if(one.hasHitEdge() && !two.hasHitEdge())
                    hitEdgeTime = one.hitEdgeTime;
                else if(!one.hasHitEdge() && two.hasHitEdge())
                    hitEdgeTime = two.hitEdgeTime;
                else
                    hitEdgeTime = one.hitEdgeTime < two.hitEdgeTime ? one.hitEdgeTime:two.hitEdgeTime;
            }
//            System.out.println(older.getClusterNumber());
        }

        /** Draws this cluster using OpenGL.
         *
         * @param drawable area to draw this.
         */
        public void draw(GLAutoDrawable drawable) {
            final float BOX_LINE_WIDTH = 2f; // in chip
            final float PATH_POINT_SIZE = 4f;
            final float VEL_LINE_WIDTH = 4f;
            GL gl = drawable.getGL();
            int x = (int) getLocation().x;
            int y = (int) getLocation().y;

            // set color and line width of cluster annotation
            getColor().getRGBComponents(rgb);
            gl.glColor3fv(rgb, 0);
            gl.glLineWidth(BOX_LINE_WIDTH);

            // draw cluster rectangle
            drawBox(gl, x, y, (int) (innerRadius+outterRadius)/2 );

            gl.glPointSize(PATH_POINT_SIZE);
            gl.glBegin(GL.GL_POINTS);
            {
                ArrayList<Cluster.PathPoint> points = getPath();
                for (Point2D.Float p : points) {
                    gl.glVertex2f(p.x, p.y);
                }
            }
            gl.glEnd();

            // now draw velocityPPT vector
            if (showClusterVelocity) {
                gl.glLineWidth(VEL_LINE_WIDTH);
                gl.glBegin(GL.GL_LINES);
                {
                    gl.glVertex2i(x, y);
                    gl.glVertex2f(x + getVelocityPPT().x * VELOCITY_VECTOR_SCALING * velocityVectorScaling, y + getVelocityPPT().y * VELOCITY_VECTOR_SCALING * velocityVectorScaling);
                }
                gl.glEnd();
            }
            // text annoations on clusters, setup
            final int font = GLUT.BITMAP_HELVETICA_18;
            gl.glColor3f(1, 1, 1);
            gl.glRasterPos3f(location.x, location.y, 0);

            // annotate the cluster with hash ID
            if (showClusterNumber) {
                chip.getCanvas().getGlut().glutBitmapString(font, String.format("#%d", hashCode()));
            }

            //annotate the cluster with the velocityPPT in pps
            if (showClusterVelocity) {
                Point2D.Float velpps = getVelocityPPS();
                chip.getCanvas().getGlut().glutBitmapString(font, String.format("%.0f,%.0f pps", velpps.x, velpps.y));
            }
        }

         public int getLastEventTimestamp() {
            return lastEventTimestamp;
        }

         /** updates cluster by one CellGroup
          * 
          * @param inGroup
          */
         private void addGroup(CellGroup cg) {
            location = cg.getLocation();
            lastPacketLocation =cg.getLocation();
            lastEventTimestamp = cg.getLastEventTimestamp();
            lastUpdateTime = cg.getLastEventTimestamp();
            numEvents = cg.getNumEvents();
            numCells = cg.getNumMemberCells();
            mass = cg.getMass();
            ageFrames++;

            if(maxRadius == 0){
                birthLocation = cg.getLocation();
                firstEventTimestamp = cg.getFirstEventTimestamp();
            }

            if(!hitEdge && cg.isHitEdge())
            {
                hitEdge = true;
                hitEdgeTime = cg.getLastEventTimestamp();
            }

            setRadius(cg);
        }

        /** Measures distance from cluster center to a cell group.
         * @return distance of this cluster to the cell group in manhatten (cheap) metric (sum of abs values of x and y distance).
         */
        private float distanceTo(CellGroup cg) {
            final float dx = cg.getLocation().x - location.x;
            final float dy = cg.getLocation().y - location.y;
            return distanceMetric(dx, dy);
        }

        public float distanceMetric(float dx, float dy) {
            return ((dx > 0) ? dx : -dx) + ((dy > 0) ? dy : -dy);
        }

        /** Measures distance in x direction, accounting for
         * instantaneousAngle of cluster and predicted movement of cluster.
         *
         * @return distance in x direction of this cluster to the event,
         * where x is measured along instantaneousAngle=0.
         */
        private float distanceToX(CellGroup cg) {
            int dt = cg.getLastEventTimestamp() - lastUpdateTime;
            float currentLocationX = location.x - velocityPPT.x * (dt);

            if(currentLocationX < 0)
                currentLocationX = 0;
            else if(currentLocationX > chip.getSizeX()-1)
                currentLocationX = chip.getSizeX()-1;
            else {}

            return Math.abs(cg.getLocation().x - currentLocationX);
        }

        /** Measures distance in y direction, accounting for instantaneousAngle of cluster,
         * where y is measured along instantaneousAngle=Pi/2  and predicted movement of cluster
         *
         * @return distance in y direction of this cluster to the event
         */
        private float distanceToY(CellGroup cg) {
            int dt = cg.getLastEventTimestamp() - lastUpdateTime;
            float currentLocationY = location.y - velocityPPT.y * (dt);

            if(currentLocationY < 0)
                currentLocationY = 0;
            else if(currentLocationY > chip.getSizeY()-1)
                currentLocationY = chip.getSizeY()-1;
            else {}

            return Math.abs(cg.getLocation().y - currentLocationY);
        }

        /** Computes and returns distance to another cluster.
         * @return distance of this cluster to the other cluster in pixels.
         */
        protected final float distanceTo(Cluster c) {
            // TODO doesn't use predicted location of clusters, only present locations
            float dx = c.location.x - location.x;
            float dy = c.location.y - location.y;
            return distanceMetric(dx, dy);
        }

        /** Computes and returns the angle of this cluster's velocity vector to another cluster's velocity vector.
         *
         * @param c the other cluster.
         * @return the angle in radians, from 0 to PI in radians. If either cluster has zero velocity, returns 0.
         */
        protected final float velocityAngleTo(Cluster c) {
            float s1 = getSpeedPPS(), s2 = c.getSpeedPPS();
            if (s1 == 0 || s2 == 0) {
                return 0;
            }
            float dot = velocityPPS.x * c.velocityPPS.x + velocityPPS.y * c.velocityPPS.y;
            float angleRad = (float) Math.acos(dot / s1 / s2);
            return angleRad;
        }

        /**
         * Computes and returns the total absolute distance
         * (shortest path) traveled in pixels since the birth of this cluster
         * @return distance in pixels since birth of cluster
         */
        public float getDistanceFromBirth() {
            double dx = location.x - birthLocation.x;
            double dy = location.y - birthLocation.y;
            return (float) Math.sqrt(dx * dx + dy * dy);
        }

        /** @return signed distance in Y from birth. */
        public float getDistanceYFromBirth() {
            return location.y - birthLocation.y;
        }

        /** @return signed distance in X from birth. */
        public float getDistanceXFromBirth() {
            return location.x - birthLocation.x;
        }


        /** The effective radius of the cluster depends on whether highwayPerspectiveEnabled is true or not and also
        on the surround of the cluster. The getRadius value is not used directly since it is a parameter that is combined
        with perspective location and aspect ratio.

        @return the cluster radius.
         */
        public final float getInnerRadius() {
            return innerRadius;
        }

        public final float getOutterRadius() {
            return outterRadius;
        }

        public final float getMaxRadius() {
            return maxRadius;
        }

        /** the radius of a cluster is the distance in pixels from the cluster center
         * that is the putative model size.
         * If highwayPerspectiveEnabled is true, then the radius is set to a fixed size
         * depending on the defaultClusterRadius and the perspective
         * location of the cluster and r is ignored. The aspect ratio parameters
         * radiusX and radiusY of the cluster are also set.
         * @param r the radius in pixels
         */
        private void setRadius(CellGroup cg) {
            innerRadius = cg.getInnerRadiusPixels();
            outterRadius = cg.getOutterRadiusPixels();

            if(maxRadius < outterRadius) {
                maxRadius = outterRadius;
                
                int chipSize = chip.getSizeX()<chip.getSizeY()?chip.getSizeX():chip.getSizeY();
                if(maxRadius > chipSize*0.3f)
                    maxRadius = chipSize*0.3f;
            }
        }

        final public Point2D.Float getLocation() {
            return location;
        }

        public void setLocation(Point2D.Float l) {
            this.location = l;
        }

        /** @return lifetime of cluster in timestamp ticks, measured as lastUpdateTime-firstEventTimestamp. */
        final public int getLifetime() {
            return lastUpdateTime - firstEventTimestamp;
        }

        /** Updates path (historical) information for this cluster,
         * including cluster velocityPPT.
         * @param t current timestamp.
         */
        final public void updatePath(int t) {
            if (!pathsEnabled) {
                return;
            }
            path.add(new PathPoint(location.x, location.y, t, numEvents));
            if (path.size() > getPathLength()) {
                path.remove(path.get(0));
            }
            updateVelocity();
        }

        /** Updates velocity of cluster.
         *
         * @param t current timestamp.
         */
        private void updateVelocity() {
            velocityFitter.update();
            if (velocityFitter.valid) {
                velocityPPT.x = velocityFitter.getXVelocity();
                velocityPPT.y = velocityFitter.getYVelocity();
                velocityPPS.x = velocityPPT.x * VELPPS_SCALING;
                velocityPPS.y = velocityPPT.y * VELPPS_SCALING;
                velocityValid = true;
            } else {
                velocityValid = false;
            }
        }

        @Override
        public String toString() {
            return String.format("Cluster number=#%d numEvents=%d locationX=%d locationY=%d lifetime=%d speedPPS=%.2f",
                    getClusterNumber(), numEvents,
                    (int) location.x,
                    (int) location.y,
                    getLifetime(),
                    getSpeedPPS());
        }

        public ArrayList<PathPoint> getPath() {
            return path;
        }

        public Color getColor() {
            return color;
        }

        public void setColor(Color color) {
            this.color = color;
        }

        /** Returns velocity of cluster in pixels per second.
         *
         * @return averaged velocity of cluster in pixels per second.
         * <p>
         * The method of measuring velocity is based on a linear regression of a number of previous cluter locations.
         * @see #getVelocityPPT()
         *
         */
        public Point2D.Float getVelocityPPS() {
            return velocityPPS;
            /* old method for velocity estimation is as follows
             * The velocity is instantaneously
             * computed from the movement of the cluster caused by the last event, then this velocity is mixed
             * with the the old velocity by the mixing factor. Thus the mixing factor is appplied twice: once for moving
             * the cluster and again for changing the velocity.
             * */
        }

        /** Computes and returns speed of cluster in pixels per second.
         *
         * @return speed in pixels per second.
         */
        public float getSpeedPPS() {
            return (float) Math.sqrt(velocityPPS.x * velocityPPS.x + velocityPPS.y * velocityPPS.y);
        }

        /** Computes and returns speed of cluster in pixels per timestamp tick.
         *
         * @return speed in pixels per timestamp tick.
         */
        public float getSpeedPPT() {
            return (float) Math.sqrt(velocityPPT.x * velocityPPT.x + velocityPPT.y * velocityPPT.y);
        }

        public int getClusterNumber() {
            return clusterNumber;
        }

        public void setClusterNumber(int clusterNumber) {
            this.clusterNumber = clusterNumber;
        }

        public int getAgeFrames() {
            return ageFrames;
        }

        public int increaseAgeFrames() {
            ageFrames+=2;

            if(ageFrames > maximumClusterAgeFrames) ageFrames = maximumClusterAgeFrames;
            
            return ageFrames;
        }

        public int decreaseAgeFrames() {
            ageFrames--;
            return ageFrames;
        }

        /**
         * Does a moving or rolling linear regression (a linear fit) on updated PathPoint data.
         * The new data point replaces the oldest data point. Summary statistics holds the rollling values
         * and are updated by subtracting the oldest point and adding the newest one.
         * From <a href="http://en.wikipedia.org/wiki/Ordinary_least_squares#Summarizing_the_data">Wikipedia article on Ordinary least squares</a>.
         *<p>
        If velocityPPT cannot be estimated (e.g. due to only 2 identical points) it is not updated.
         * @author tobi
         */
        private class RollingVelocityFitter {

            private static final int LENGTH_DEFAULT = 5;
            private int length = LENGTH_DEFAULT;
            private float st = 0, sx = 0, sy = 0, stt = 0, sxt = 0, syt = 0, den = 1; // summary stats
            private ArrayList<PathPoint> points;
            private float xVelocity = 0, yVelocity = 0;
            private boolean valid = false;
            private int nPoints = 0;

            /** Creates a new instance of RollingLinearRegression */
            public RollingVelocityFitter(ArrayList<PathPoint> points, int length) {
                this.points = points;
                this.length = length;
            }

            public String toString() {
                return String.format("RollingVelocityFitter: \n" + "valid=%s nPoints=%d\n" +
                        "xVel=%f, yVel=%f\n" +
                        "st=%f sx=%f sy=%f, sxt=%f syt=%f den=%f",
                        valid, nPoints,
                        xVelocity, yVelocity,
                        st, sx, sy, sxt, syt, den);

            }

            /**
             * Updates estimated velocityPPT based on last point in path. If velocityPPT cannot be estimated
            it is not updated.
             * @param t current timestamp.
             */
            private synchronized void update() {
                int n = points.size();
                if (n < 1) {
                    return;
                }
                PathPoint p = points.get(n - 1); // take last point
                if (p.getNEvents() == 0) {
                    return;
                }
                nPoints++;
                if (n > length) {
                    removeOldestPoint(); // discard data beyond range length
                }
                n = n > length ? length : n;  // n grows to max length
                float dt = p.t - firstEventTimestamp; // t is time since cluster formed, limits absolute t for numerics
                st += dt;
                sx += p.x;
                sy += p.y;
                stt += dt * dt;
                sxt += p.x * dt;
                syt += p.y * dt;
//                if(n<length) return; // don't estimate velocityPPT until we have all necessary points, results very noisy and send cluster off to infinity very often, would give NaN
                den = (n * stt - st * st);
                if (n >= length && den != 0) {
                    valid = true;
                    xVelocity = (n * sxt - st * sx) / den;
                    yVelocity = (n * syt - st * sy) / den;
                } else {
                    valid = false;
                }
//                System.out.println(this.toString());
            }

            private void removeOldestPoint() {
                // takes away from summary states the oldest point
                PathPoint p = points.get(points.size() - length - 1);
                // if points has 5 points (0-4), length=3, then remove points(5-3-1)=points(1) leaving 2-4 which is correct
                float dt = p.t - firstEventTimestamp;
                st -= dt;
                sx -= p.x;
                sy -= p.y;
                stt -= dt * dt;
                sxt -= p.x * dt;
                syt -= p.y * dt;
            }

            int getLength() {
                return length;
            }

            /** Sets the window length.  Clears the accumulated data.
             * @param length the number of points to fit
             * @see #LENGTH_DEFAULT
             */
            synchronized void setLength(int length) {
                this.length = length;
            }

            public float getXVelocity() {
                return xVelocity;
            }

            public float getYVelocity() {
                return yVelocity;
            }

            /** Returns true if the last estimate resulted in a valid measurement
             * (false when e.g. there are only two identical measurements)
             */
            public boolean isValid() {
                return valid;
            }

            public void setValid(boolean valid) {
                this.valid = valid;
            }
        } // rolling velocityPPT fitter

        /** Returns birth location of cluster: initially the first event and later, after cluster
         * becomes visible, it is the location when it becomes visible, which is presumably less noisy.
         *
         * @return x,y location.
         */
        public Point2D.Float getBirthLocation() {
            return birthLocation;
        }

        /** Returns first timestamp of cluster; this time is updated when cluster becomes visible.
         *
         * @return timestamp of birth location.
         */
        public int getBirthTime() {
            return firstEventTimestamp;
        }

        public void setBirthLocation(Point2D.Float birthLocation) {
            this.birthLocation = birthLocation;
        }

        /** This flog is set true after a velocityPPT has been computed for the cluster.
         * This may take several packets.

        @return true if valid.
         */
        public boolean isVelocityValid() {
            return velocityValid;
        }

        public void setVelocityValid(boolean velocityValid) {
            this.velocityValid = velocityValid;
        }
    } // Cluster


    public java.util.List<BluringFilter2DTracker.Cluster> getClusters() {
        return this.clusters;
    }

    private LinkedList<BluringFilter2DTracker.Cluster> getPruneList() {
        return this.pruneList;
    }
    protected static final float fullbrightnessLifetime = 1000000;
    /** Useful for subclasses. */
    protected Random random = new Random();

    private final void drawCluster(final Cluster c, float[][][] fr) {
        int x = (int) c.getLocation().x;
        int y = (int) c.getLocation().y;


        int raidus = (int) c.getInnerRadius();
        int ix, iy;
        int mn, mx;

        Color color = c.getColor();
        if (true) { // draw boxes
            iy = y - raidus;    // line under center
            mn = x - raidus;
            mx = x + raidus;
            for (ix = mn; ix <= mx; ix++) {
                colorPixel(ix, iy, fr, clusterColorChannel, color);
            }
            iy = y + raidus;    // line over center
            for (ix = mn; ix <= mx; ix++) {
                colorPixel(ix, iy, fr, clusterColorChannel, color);
            }
            ix = x - raidus;        // line to left
            mn = y - raidus;
            mx = y + raidus;
            for (iy = mn; iy <= mx; iy++) {
                colorPixel(ix, iy, fr, clusterColorChannel, color);
            }
            ix = x + raidus;    // to right
            for (iy = mn; iy <= mx; iy++) {
                colorPixel(ix, iy, fr, clusterColorChannel, color);
            }
        } else { // draw diamond reflecting manhatten distance measure doesn't look very nice because not antialiased at all
            iy = y - raidus;    // line up right from bot
            ix = x;
            mx = x + raidus;
            while (ix < mx) {
                colorPixel(ix++, iy++, fr, clusterColorChannel, color);
            }
            mx = x + raidus;
            ix = x;
            iy = y + raidus;    // line down right from top
            while (ix < mx) {
                colorPixel(ix++, iy--, fr, clusterColorChannel, color);
            }
            ix = x;        // line from top down left
            iy = y + raidus;
            while (iy >= y) {
                colorPixel(ix--, iy--, fr, clusterColorChannel, color);
            }
            ix = x;
            iy = y - raidus;
            while (iy < y) {
                colorPixel(ix--, iy++, fr, clusterColorChannel, color);
            }
        }

        ArrayList<Cluster.PathPoint> points = c.getPath();
        for (Point2D.Float p : points) {
            colorPixel(Math.round(p.x), Math.round(p.y), fr, clusterColorChannel, color);
        }

    }
    private static final int clusterColorChannel = 2;

    /** @param x x location of pixel
     *@param y y location
     *@param fr the frame data
     *@param channel the RGB channel number 0-2
     *@param brightness the brightness 0-1
     */
    private final void colorPixel(final int x, final int y, final float[][][] fr, int channel, Color color) {
        if (y < 0 || y > fr.length - 1 || x < 0 || x > fr[0].length - 1) {
            return;
        }
        float[] rgb = color.getRGBColorComponents(null);
        float[] f = fr[y][x];
        for (int i = 0; i < 3; i++) {
            f[i] = rgb[i];
        }
    }


    public Object getFilterState() {
        return null;
    }

    private boolean isGeneratingFilter() {
        return false;
    }

    synchronized public void resetFilter() {
        clusters.clear();
        clusterCounter = 0;
        averageVelocityPPT.x = 0;
        averageVelocityPPT.y = 0;
    }



    /** @see #setPathsEnabled
     */
    public boolean isPathsEnabled() {
        return pathsEnabled;
    }

    /**
     * Enable cluster history paths. The path of each cluster is stored as a list of points at the end of each cluster list update.
     * This option is required (and set true) if useVelocity is set true.
     *
     * @param pathsEnabled true to show the history of the cluster locations on each packet.
     */
    public void setPathsEnabled(boolean pathsEnabled) {
        support.firePropertyChange("pathsEnabled", this.pathsEnabled, pathsEnabled);
        this.pathsEnabled = pathsEnabled;
        getPrefs().putBoolean("HandTracker.pathsEnabled", pathsEnabled);
    }


    public void update(Observable o, Object arg) {
        initFilter();
    }


    public void annotate(Graphics2D g) {
    }

    protected void drawBox(GL gl, int x, int y, int radius) {
        final float r2d = (float) (180 / Math.PI);
        gl.glPushMatrix();
        gl.glTranslatef(x, y, 0);
        gl.glBegin(GL.GL_LINE_LOOP);
        {
            gl.glVertex2i(-radius, -radius);
            gl.glVertex2i(+radius, -radius);
            gl.glVertex2i(+radius, +radius);
            gl.glVertex2i(-radius, +radius);
        }
        gl.glEnd();
        gl.glPopMatrix();
    }

    synchronized public void annotate(GLAutoDrawable drawable) {
        if (!isFilterEnabled()) {
            return;
        }
        GL gl = drawable.getGL(); // when we get this we are already set up with scale 1=1 pixel, at LL corner
        if (gl == null) {
            log.warning("null GL in HandTracker.annotate");
            return;
        }
        gl.glPushMatrix();
        try {
            {
                for (Cluster c : clusters) {
                    if (showAllClusters && c.getAgeFrames() > 2) {
                        c.draw(drawable);
                    }
                }
            }
        } catch (java.util.ConcurrentModificationException e) {
            // this is in case cluster list is modified by real time filter during rendering of clusters
            log.warning(e.getMessage());
        }
        gl.glPopMatrix();
    }

//    void drawGLCluster(int x1, int y1, int x2, int y2)
    /** annotate the rendered retina frame to show locations of clusters */
    synchronized public void annotate(float[][][] frame) {
        if (!isFilterEnabled()) {
            return;
        }
        // disable for now TODO
        if (chip.getCanvas().isOpenGLEnabled()) {
            return; // done by open gl annotator
        }
        for (Cluster c : clusters) {
            if (showAllClusters && c.getAgeFrames() > 2)
                drawCluster(c, frame);
        }
    }

    /** Use cluster velocityPPT to give clusters a kind of inertia, so that they
     * are virtually moved by their velocityPPT times the time between the last
     * event and the present one before updating cluster location.
     * Depends on enabling cluster paths. Setting this option true enables cluster paths.
     * @param useVelocity
     * @see #setPathsEnabled(boolean)
     */
    public void setUseVelocity(boolean useVelocity) {
        if (useVelocity) {
            setPathsEnabled(true);
        }
        support.firePropertyChange("useVelocity", this.useVelocity, useVelocity);
        this.useVelocity = useVelocity;
        getPrefs().putBoolean("HandTracker.useVelocity", useVelocity);
    }

    public boolean isUseVelocity() {
        return useVelocity;
    }

    public boolean isShowAllClusters() {
        return showAllClusters;
    }

    /**Sets annotation visibility of clusters that are not "visible"
     * @param showAllClusters true to show all clusters even if there are not "visible"
     */
    public void setShowAllClusters(boolean showAllClusters) {
        this.showAllClusters = showAllClusters;
        getPrefs().putBoolean("HandTracker.showAllClusters", showAllClusters);
    }


    public int getPredictiveVelocityFactor() {
        return predictiveVelocityFactor;
    }

    public void setPredictiveVelocityFactor(int predictiveVelocityFactor) {
        this.predictiveVelocityFactor = predictiveVelocityFactor;
    }

    public int getPathLength() {
        return pathLength;
    }

    /** Sets the maximum number of path points recorded for each cluster. The {@link Cluster#path} list of points is adjusted
     * to be at most <code>pathLength</code> long.
     *
     * @param pathLength the number of recorded path points. If <2, set to 2.
     */
    synchronized public void setPathLength(int pathLength) {
        if (pathLength < 2) {
            pathLength = 2;
        }
        int old = this.pathLength;
        this.pathLength = pathLength;
        getPrefs().putInt("HandTracker.pathLength", pathLength);
        support.firePropertyChange("pathLength", old, pathLength);
        if (velocityPoints > pathLength) {
            setVelocityPoints(pathLength);
        }
    }


    /** @see #setVelocityPoints(int)
     *
     * @return number of points used to estimate velocity.
     */
    public int getVelocityPoints() {
        return velocityPoints;
    }

    /** Sets the number of path points to use to estimate cluster velocity.
     *
     * @param velocityPoints the number of points to use to estimate velocity.
     * Bounded above to number of path points that are stored.
     * @see #setPathLength(int)
     * @see #setPathsEnabled(boolean)
     */
    public void setVelocityPoints(int velocityPoints) {
        if (velocityPoints >= pathLength) {
            velocityPoints = pathLength;
        }
        int old = this.velocityPoints;
        this.velocityPoints = velocityPoints;
        getPrefs().putInt("HandTracker.velocityPoints", velocityPoints);
        support.firePropertyChange("velocityPoints", old, this.velocityPoints);
    }

    /**
     * @return the velAngDiffDegToNotMerge
     */
    public float getVelAngDiffDegToNotMerge() {
        return velAngDiffDegToNotMerge;
    }

    /**
     * @param velAngDiffDegToNotMerge the velAngDiffDegToNotMerge to set
     */
    public void setVelAngDiffDegToNotMerge(float velAngDiffDegToNotMerge) {
        if (velAngDiffDegToNotMerge < 0) {
            velAngDiffDegToNotMerge = 0;
        } else if (velAngDiffDegToNotMerge > 180) {
            velAngDiffDegToNotMerge = 180;
        }
        this.velAngDiffDegToNotMerge = velAngDiffDegToNotMerge;
        getPrefs().putFloat("HandTracker.velAngDiffDegToNotMerge", velAngDiffDegToNotMerge);
    }

    /**
     * @return the showClusterNumber
     */
    public boolean isShowClusterNumber() {
        return showClusterNumber;
    }

    /**
     * @param showClusterNumber the showClusterNumber to set
     */
    public void setShowClusterNumber(boolean showClusterNumber) {
        this.showClusterNumber = showClusterNumber;
        getPrefs().putBoolean("HandTracker.showClusterNumber", showClusterNumber);
    }


    /**
     * @return the showClusterVelocity
     */
    public boolean isShowClusterVelocity() {
        return showClusterVelocity;
    }

    /**
     * @param showClusterVelocity the showClusterVelocity to set
     */
    public void setShowClusterVelocity(boolean showClusterVelocity) {
        this.showClusterVelocity = showClusterVelocity;
        getPrefs().putBoolean("HandTracker.showClusterVelocity", showClusterVelocity);
    }

    /**
     * @return the velocityVectorScaling
     */
    public float getVelocityVectorScaling() {
        return velocityVectorScaling;
    }

    /**
     * @param velocityVectorScaling the velocityVectorScaling to set
     */
    public void setVelocityVectorScaling(float velocityVectorScaling) {
        this.velocityVectorScaling = velocityVectorScaling;
        getPrefs().putFloat("HandTracker.velocityVectorScaling", velocityVectorScaling);
    }

    public int getMaximumClusterAgeFrames() {
        return maximumClusterAgeFrames;
    }

    public void setMaximumClusterAgeFrames(int maximumClusterAgeFrames) {
        this.maximumClusterAgeFrames = maximumClusterAgeFrames;
        getPrefs().putInt("HandTracker.maximumClusterAgeFrames", maximumClusterAgeFrames);
    }


    /**
     * @return the loggingIntervalUs
     */
    public int getLoggingIntervalUs() {
        return loggingIntervalUs;
    }

    /**
     * @param loggingIntervalUs the loggingIntervalUs to set
     */
    public void setLoggingIntervalUs(int loggingIntervalUs) {
        this.loggingIntervalUs = loggingIntervalUs;
        getPrefs().putInt("HandTracker.loggingIntervalUs", loggingIntervalUs);
    }

    /**
     * @return the initializeVelocityToAverage
     */
    public boolean isInitializeVelocityToAverage() {
        return initializeVelocityToAverage;
    }

    /**
     * @param initializeVelocityToAverage the initializeVelocityToAverage to set
     */
    public void setInitializeVelocityToAverage(boolean initializeVelocityToAverage) {
        this.initializeVelocityToAverage = initializeVelocityToAverage;
        getPrefs().putBoolean("HandTracker.initializeVelocityToAverage", initializeVelocityToAverage);
    }

    /**
     * @return the showClusterMass
     */
    public boolean isShowClusterMass() {
        return showClusterMass;
    }

    /**
     * @param showClusterMass the showClusterMass to set
     */
    public void setShowClusterMass(boolean showClusterMass) {
        this.showClusterMass = showClusterMass;
        getPrefs().putBoolean("HandTracker.showClusterMass", showClusterMass);
    }



}
