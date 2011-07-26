package net.sf.jaer.hardwareinterface.serial.edvsviewer;

import java.awt.Color;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.prefs.Preferences;

import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class EP_PigTracker extends EventProcessor {

	private JButton closeGUIButton = new JButton();

	private JFrame frame = new JFrame("Pig Tracker GUI");
	private JCheckBox enablePerspective = new JCheckBox();
	private JCheckBox enableShear = new JCheckBox();
	private JButton learnButton = new JButton();
	private JButton resetButton = new JButton();
	private JSlider distanceSlider = new JSlider(0, 1024, 100);

	private JCheckBox trackSquare = new JCheckBox();
	private JCheckBox trackA = new JCheckBox();
	private JCheckBox trackI = new JCheckBox();
	private JCheckBox trackP56 = new JCheckBox();
	private JCheckBox trackP22 = new JCheckBox();
	private JCheckBox trackCapture = new JCheckBox();

	final int maxNumberOfLines = 500;
	int numberOfLinesInUse = 60;

	double m1, m2, m3, m4, m5, m6, m7, m8, m9;
	int trackingMode = 1; // 1 == any matrix, 2 == no shear, 3 == no perspective

	double la[] = new double[maxNumberOfLines];			// represents lines
	double lb[] = new double[maxNumberOfLines];	
	double lc[] = new double[maxNumberOfLines];	
	double lcx[] = new double[maxNumberOfLines];		// center of line
	double lcy[] = new double[maxNumberOfLines];
	double lch[] = new double[maxNumberOfLines];		// squared half-width of segment
	double lsx[] = new double[maxNumberOfLines];		// "startpoint" of line
	double lsy[] = new double[maxNumberOfLines];
	double lex[] = new double[maxNumberOfLines];		// "endpoint" of line
	double ley[] = new double[maxNumberOfLines];

	double distanceThreshold = 0.04;

	long trackCaptureEventCounter = 0;
	final long numberOfCaptureEvents = 10000;
	boolean captureEventMatrix[][] = new boolean[128][128];
	
	void resetIdentityMatrix() {
		m1 = 1;
		m2 = 0;
		m3 = 0;
		m4 = 0;
		m5 = 1;
		m6 = 0;
		m7 = 0;
		m8 = 0;
		m9 = 1;
	}
	
	void precomputeUsingEndpoints() {
		for (int n=0; n<numberOfLinesInUse; n++) {
			lcx[n] = (lsx[n]+lex[n]) / 2.0;				
			lcy[n] = (lsy[n]+ley[n]) / 2.0;
			lch[n] = ((lsx[n]-lex[n])*(lsx[n]-lex[n]) + (lsy[n]-ley[n])*(lsy[n]-ley[n])) / 4.0;
			
			la[n] = lsy[n]-ley[n];				
			lb[n] = lex[n]-lsx[n];
			lc[n] = lsx[n]*ley[n] - lsy[n]*lex[n];

			double den = Math.sqrt(la[n]*la[n] + lb[n]*lb[n] + lc[n]*lc[n]);
			la[n] = la[n] / den;
			lb[n] = lb[n] / den;
			lc[n] = lc[n] / den;
		}
	}

	public class showGUI extends JApplet {
		private static final long serialVersionUID = 1L;

		showGUI() {
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.getContentPane().add(this);
			frame.setSize(400, 500);
			frame.setResizable(true);				// allow changes in size
			frame.setName("DVSViewerWindow");

			frame.setLocation(1600, 100);

			Container cp = getContentPane();
			cp.setLayout(null);		

			cp.add(closeGUIButton);
			closeGUIButton.setText("closeGUI");
			closeGUIButton.setBounds(280, 10, 100, 20);

			cp.add(enablePerspective);
			enablePerspective.setText("enablePerspective");
			enablePerspective.setBounds(20, 20, 200, 30);

			cp.add(enableShear);
			enableShear.setText("enableShear");
			enableShear.setBounds(20, 50, 200, 30);

			cp.add(learnButton);
			learnButton.setText("learn");
			learnButton.setBounds(20, 100, 120, 30);

			cp.add(resetButton);
			resetButton.setText("init");
			resetButton.setBounds(160, 100, 120, 30);

			cp.add(distanceSlider);
			distanceSlider.setBounds(20, 150, 300, 30);

			cp.add(trackA);
			trackA.setText("track A");
			trackA.setBounds(20, 200, 200, 20);

			cp.add(trackI);
			trackI.setText("track I");
			trackI.setBounds(20, 230, 200, 20);

			cp.add(trackSquare);
			trackSquare.setText("track Square");
			trackSquare.setBounds(20, 260, 200, 20);

			cp.add(trackP56);
			trackP56.setText("track P56");
			trackP56.setBounds(20, 290, 200, 20);

			cp.add(trackP22);
			trackP22.setText("track P22");
			trackP22.setBounds(20, 320, 200, 20);

			cp.add(trackCapture);
			trackCapture.setText("track Capture");
			trackCapture.setBounds(20, 350, 200, 20);


			frame.setVisible(true);

			ActionListener bl = new ButtonListener();
			closeGUIButton.addActionListener(bl);
			learnButton.addActionListener(bl);
			resetButton.addActionListener(bl);
			enablePerspective.addActionListener(bl);
			enableShear.addActionListener(bl);

			trackA.addActionListener(bl);
			trackI.addActionListener(bl);
			trackSquare.addActionListener(bl);
			trackP56.addActionListener(bl);
			trackP22.addActionListener(bl);
			trackCapture.addActionListener(bl);

			ChangeListener cl = new SliderListener();
			distanceSlider.addChangeListener(cl);
			
			loadConfig();
		}
	}

	public void saveConfig() {
		Preferences prefs = Preferences.userNodeForPackage(EmbeddedDVS128Viewer.class);
		prefs.putInt("PigTrackerFrameLocationX", frame.getLocation().x);
		prefs.putInt("PigTrackerFrameLocationY", frame.getLocation().y);

		prefs.putInt("distanceSlider", distanceSlider.getValue());

		prefs.putBoolean("enableSheare", enableShear.isSelected());
		prefs.putBoolean("enablePerspective", enablePerspective.isSelected());

		prefs.putBoolean("trackA", trackA.isSelected());
		prefs.putBoolean("trackI", trackI.isSelected());
		prefs.putBoolean("trackSquare", trackSquare.isSelected());
		prefs.putBoolean("trackP56", trackP56.isSelected());
		prefs.putBoolean("trackP22", trackP22.isSelected());
		prefs.putBoolean("trackCapture", trackCapture.isSelected());
	}
	public void loadConfig() {
		Preferences prefs = Preferences.userNodeForPackage(EmbeddedDVS128Viewer.class);

		frame.setLocation(prefs.getInt("PigTrackerFrameLocationX", 10),
				prefs.getInt("PigTrackerFrameLocationY", 10));

		distanceSlider.setValue(prefs.getInt("distanceSlider", 100));

		enableShear.setSelected(prefs.getBoolean("enableSheare", false));
		enablePerspective.setSelected(prefs.getBoolean("enablePerspective", false));

		trackA.setSelected(prefs.getBoolean("trackA", false));
		trackI.setSelected(prefs.getBoolean("trackI", false));
		trackSquare.setSelected(prefs.getBoolean("trackSquare", false));
		trackP56.setSelected(prefs.getBoolean("trackP56", false));
		trackP22.setSelected(prefs.getBoolean("trackP22", false));
		trackCapture.setSelected(prefs.getBoolean("trackCapture", false));
		
		computeTrackingMode();
	}

	public class SliderListener implements ChangeListener {
		public void stateChanged(ChangeEvent e) {
			distanceThreshold = ((double) distanceSlider.getValue()) / 5120;
 			System.out.println("new value: " + distanceThreshold);
		}
	}

	private void computeTrackingMode() {

		if (enableShear.isSelected()) {
			trackingMode = 1;
		} else {
			if (enablePerspective.isSelected()) {
				trackingMode = 2;
			} else {
				trackingMode = 3;
			}
		}
	}

	public class ButtonListener implements ActionListener {
		private void disableAllTracker() {
			trackA.setSelected(false);
			trackI.setSelected(false);
			trackSquare.setSelected(false);
			trackP56.setSelected(false);
			trackP22.setSelected(false);				
			trackCapture.setSelected(false);
		}
		public void actionPerformed(ActionEvent e) {

			if (e.getSource() == closeGUIButton) {
				frame.dispose();
			}

			if (e.getSource() == learnButton) {
				System.out.println("Learning Button pressed!");
			}
			if (e.getSource() == resetButton) {
				initTemplate();
			}

			if (e.getSource() == trackA) {
				disableAllTracker();
				trackA.setSelected(true);
				initTemplate();
			}
			if (e.getSource() == trackI) {
				disableAllTracker();
				trackI.setSelected(true);
				initTemplate();
			}
			if (e.getSource() == trackSquare) {
				disableAllTracker();
				trackSquare.setSelected(true);
				initTemplate();
			}
			if (e.getSource() == trackP56) {
				disableAllTracker();
				trackP56.setSelected(true);
				initTemplate();
			}
			if (e.getSource() == trackP22) {
				disableAllTracker();
				trackP22.setSelected(true);
				initTemplate();
			}
			if (e.getSource() == trackCapture) {
				disableAllTracker();
				trackCapture.setSelected(true);
				initTemplate();
			}
			
			
			if (e.getSource() == enablePerspective) {
				if (enablePerspective.isSelected()) {
					System.out.println("enablePerspective ON!");
				} else {
					System.out.println("enablePerspective OFF!");
					if (enableShear.isSelected())
						System.out.println("Also turning off enableShear!");
					enableShear.setSelected(false);
				}
			}

			if (e.getSource() == enableShear) {
				if (enableShear.isSelected()) {
					System.out.println("enableShear ON!");
					if (! enablePerspective.isSelected())
						System.out.println("Also turning on enablePerspective!");
					enablePerspective.setSelected(true);
				} else {
					System.out.println("enableShear OFF!");
				}
			}

			computeTrackingMode();
			System.out.println("Tracking Mode = " + trackingMode);

			saveConfig();
		}
	}
	
	private void initPig56() {
		lsx[0] = -0.619048;
		lsy[0] = -0.724868;
		lex[0] = -0.526455;
		ley[0] = -0.597884;

		lsx[1] = -0.835979;
		lsy[1] = -0.708995;
		lex[1] = -0.708995;
		ley[1] = -0.616402;

		lsx[2] = -0.656085;
		lsy[2] = -0.687831;
		lex[2] = -0.621693;
		ley[2] = -0.534392;

		lsx[3] = -0.854497;
		lsy[3] = -0.685185;
		lex[3] = -0.806878;
		ley[3] = -0.534392;

		lsx[4] = 0.177249;
		lsy[4] = -0.632275;
		lex[4] = 0.333333;
		ley[4] = -0.613757;

		lsx[5] = 0.10582;
		lsy[5] = -0.62963;
		lex[5] = -0.0396825;
		ley[5] = -0.568783;

		lsx[6] = -0.542328;
		lsy[6] = -0.626984;
		lex[6] = -0.415344;
		ley[6] = -0.534392;

		lsx[7] = 0.301587;
		lsy[7] = -0.626984;
		lex[7] = 0.452381;
		ley[7] = -0.579365;

		lsx[8] = -0.153439;
		lsy[8] = -0.611111;
		lex[8] = 0.0026455;
		ley[8] = -0.597884;

		lsx[9] = -0.449735;
		lsy[9] = -0.608466;
		lex[9] = -0.312169;
		ley[9] = -0.534392;

		lsx[10] = -0.201058;
		lsy[10] = -0.60582;
		lex[10] = -0.346561;
		ley[10] = -0.547619;

		lsx[11] = 0.425926;
		lsy[11] = -0.597884;
		lex[11] = 0.55291;
		ley[11] = -0.505291;

		lsx[12] = -0.661376;
		lsy[12] = -0.579365;
		lex[12] = -0.78836;
		ley[12] = -0.486772;

		lsx[13] = -0.449735;
		lsy[13] = -0.544974;
		lex[13] = -0.375661;
		ley[13] = -0.407407;

		lsx[14] = 0.526455;
		lsy[14] = -0.52381;
		lex[14] = 0.62963;
		ley[14] = -0.404762;

		lsx[15] = -0.767196;
		lsy[15] = -0.510582;
		lex[15] = -0.84127;
		ley[15] = -0.373016;

		lsx[16] = 0.60582;
		lsy[16] = -0.425926;
		lex[16] = 0.666667;
		ley[16] = -0.280423;

		lsx[17] = 0.780423;
		lsy[17] = -0.420635;
		lex[17] = 0.883598;
		ley[17] = -0.301587;

		lsx[18] = -0.828042;
		lsy[18] = -0.399471;
		lex[18] = -0.857143;
		ley[18] = -0.246032;

		lsx[19] = 0.849206;
		lsy[19] = -0.312169;
		lex[19] = 0.783069;
		ley[19] = -0.169312;

		lsx[20] = -0.859788;
		lsy[20] = -0.275132;
		lex[20] = -1.00529;
		ley[20] = -0.214286;

		lsx[21] = -0.97619;
		lsy[21] = -0.227513;
		lex[21] = -0.957672;
		ley[21] = -0.0714286;

		lsx[22] = 0.780423;
		lsy[22] = -0.206349;
		lex[22] = 0.648148;
		ley[22] = -0.121693;

		lsx[23] = -0.949735;
		lsy[23] = -0.108466;
		lex[23] = -0.920635;
		ley[23] = 0.0449735;

		lsx[24] = 0.679894;
		lsy[24] = -0.108466;
		lex[24] = 0.671958;
		ley[24] = 0.047619;

		lsx[25] = -0.899471;
		lsy[25] = -0.015873;
		lex[25] = -0.78836;
		ley[25] = 0.0952381;

		lsx[26] = 0.669312;
		lsy[26] = 0.0185185;
		lex[26] = 0.640212;
		ley[26] = 0.171958;

		lsx[27] = -0.806878;
		lsy[27] = 0.047619;
		lex[27] = -0.685185;
		ley[27] = 0.148148;

		lsx[28] = -0.402116;
		lsy[28] = 0.0820106;
		lex[28] = -0.505291;
		ley[28] = 0.201058;

		lsx[29] = -0.71164;
		lsy[29] = 0.126984;
		lex[29] = -0.57672;
		ley[29] = 0.206349;

		lsx[30] = 0.640212;
		lsy[30] = 0.142857;
		lex[30] = 0.579365;
		ley[30] = 0.28836;

		lsx[31] = -0.494709;
		lsy[31] = 0.156085;
		lex[31] = -0.640212;
		ley[31] = 0.216931;

		lsx[32] = -0.529101;
		lsy[32] = 0.198413;
		lex[32] = -0.436508;
		ley[32] = 0.325397;

		lsx[33] = -0.201058;
		lsy[33] = 0.21164;
		lex[33] = -0.230159;
		ley[33] = 0.365079;

		lsx[34] = 0.537037;
		lsy[34] = 0.23545;
		lex[34] = 0.589947;
		ley[34] = 0.383598;

		lsx[35] = -0.42328;
		lsy[35] = 0.277778;
		lex[35] = -0.425926;
		ley[35] = 0.433862;

		lsx[36] = -0.227513;
		lsy[36] = 0.335979;
		lex[36] = -0.26455;
		ley[36] = 0.489418;

		lsx[37] = 0.267196;
		lsy[37] = 0.343915;
		lex[37] = 0.338624;
		ley[37] = 0.484127;

		lsx[38] = 0.584656;
		lsy[38] = 0.357143;
		lex[38] = 0.621693;
		ley[38] = 0.510582;

		lsx[39] = -0.208995;
		lsy[39] = 0.365079;
		lex[39] = -0.0846561;
		ley[39] = 0.460317;

		lsx[40] = 0.251323;
		lsy[40] = 0.365079;
		lex[40] = 0.126984;
		ley[40] = 0.460317;

		lsx[41] = -0.428571;
		lsy[41] = 0.417989;
		lex[41] = -0.486772;
		ley[41] = 0.563492;

		lsx[42] = 0.132275;
		lsy[42] = 0.42328;
		lex[42] = -0.015873;
		ley[42] = 0.47619;

		lsx[43] = 0.333333;
		lsy[43] = 0.42328;
		lex[43] = 0.455026;
		ley[43] = 0.52381;

		lsx[44] = -0.103175;
		lsy[44] = 0.425926;
		lex[44] = 0.047619;
		ley[44] = 0.473545;

		lsx[45] = 0.600529;
		lsy[45] = 0.478836;
		lex[45] = 0.597884;
		ley[45] = 0.634921;

		lsx[46] = -0.291005;
		lsy[46] = 0.492063;
		lex[46] = -0.291005;
		ley[46] = 0.650794;

		lsx[47] = 0.330688;
		lsy[47] = 0.5;
		lex[47] = 0.26455;
		ley[47] = 0.642857;

		lsx[48] = 0.420635;
		lsy[48] = 0.510582;
		lex[48] = 0.354497;
		ley[48] = 0.653439;

		lsx[49] = -0.222222;
		lsy[49] = 0.526455;
		lex[49] = -0.251323;
		ley[49] = 0.679894;

		lsx[50] = -0.481481;
		lsy[50] = 0.529101;
		lex[50] = -0.555556;
		ley[50] = 0.666667;

		lsx[51] = 0.571429;
		lsy[51] = 0.592593;
		lex[51] = 0.534392;
		ley[51] = 0.746032;

		lsx[52] = -0.354497;
		lsy[52] = 0.597884;
		lex[52] = -0.396825;
		ley[52] = 0.748677;

		lsx[53] = 0.293651;
		lsy[53] = 0.616402;
		lex[53] = 0.404762;
		ley[53] = 0.727513;

		lsx[54] = -0.547619;
		lsy[54] = 0.640212;
		lex[54] = -0.425926;
		ley[54] = 0.740741;

		lsx[55] = 0.518519;
		lsy[55] = 0.685185;
		lex[55] = 0.378307;
		ley[55] = 0.756614;

		numberOfLinesInUse = 56;
	}
	private void initPig20() {
		lsx[0] = -0.664021;
		lsy[0] = -0.730159;
		lex[0] = -0.626984;
		ley[0] = -0.52381;

		lsx[1] = -0.835979;
		lsy[1] = -0.71164;
		lex[1] = -0.68254;
		ley[1] = -0.568783;

		lsx[2] = -0.584656;
		lsy[2] = -0.666667;
		lex[2] = -0.431217;
		ley[2] = -0.52381;

		lsx[3] = -0.838624;
		lsy[3] = -0.648148;
		lex[3] = -0.801587;
		ley[3] = -0.441799;

		lsx[4] = 0.177249;
		lsy[4] = -0.634921;
		lex[4] = 0.386243;
		ley[4] = -0.603175;

		lsx[5] = 0.116402;
		lsy[5] = -0.632275;
		lex[5] = -0.0899471;
		ley[5] = -0.587302;

		lsx[6] = -0.148148;
		lsy[6] = -0.613757;
		lex[6] = -0.349206;
		ley[6] = -0.550265;

		lsx[7] = 0.449735;
		lsy[7] = -0.587302;
		lex[7] = 0.597884;
		ley[7] = -0.439153;

		lsx[8] = -0.97619;
		lsy[8] = -0.240741;
		lex[8] = -0.944444;
		ley[8] = -0.031746;

		lsx[9] = 0.68254;
		lsy[9] = -0.166667;
		lex[9] = 0.669312;
		ley[9] = 0.042328;

		lsx[10] = -0.928571;
		lsy[10] = -0.0238095;
		lex[10] = -0.759259;
		ley[10] = 0.100529;

		lsx[11] = 0.661376;
		lsy[11] = 0.0793651;
		lex[11] = 0.584656;
		ley[11] = 0.275132;

		lsx[12] = -0.55291;
		lsy[12] = 0.171958;
		lex[12] = -0.420635;
		ley[12] = 0.333333;

		lsx[13] = -0.198413;
		lsy[13] = 0.203704;
		lex[13] = -0.243386;
		ley[13] = 0.410053;

		lsx[14] = 0.531746;
		lsy[14] = 0.224868;
		lex[14] = 0.608466;
		ley[14] = 0.420635;

		lsx[15] = 0.259259;
		lsy[15] = 0.357143;
		lex[15] = 0.417989;
		ley[15] = 0.494709;

		lsx[16] = -0.214286;
		lsy[16] = 0.373016;
		lex[16] = -0.0238095;
		ley[16] = 0.462963;

		lsx[17] = 0.251323;
		lsy[17] = 0.375661;
		lex[17] = 0.0582011;
		ley[17] = 0.457672;

		lsx[18] = -0.431217;
		lsy[18] = 0.428571;
		lex[18] = -0.526455;
		ley[18] = 0.616402;

		lsx[19] = -0.280423;
		lsy[19] = 0.441799;
		lex[19] = -0.306878;
		ley[19] = 0.650794;

		numberOfLinesInUse = 20;
	}
	private void initSquare() {
		lsx[0] = 32;				// top horizontal line
		lsy[0] = 32;
		lex[0] = 96;
		ley[0] = 32;

		lsx[1] = 32;				// left vertical line
		lsy[1] = 32;
		lex[1] = 32;
		ley[1] = 96;

		lsx[2] = 32;				// bottom horizontal line
		lsy[2] = 96;
		lex[2] = 96;
		ley[2] = 96;

		lsx[3] = 96;				// right vertical line
		lsy[3] = 32;
		lex[3] = 96;
		ley[3] = 96;

		numberOfLinesInUse = 4;

		// initialize a[], b[], c[], cx[], cy[]
		for (int n=0; n<numberOfLinesInUse; n++) {
			lsx[n] = (lsx[n]/64.0)-1.0;
			lsy[n] = (lsy[n]/64.0)-1.0;
			lex[n] = (lex[n]/64.0)-1.0;
			ley[n] = (ley[n]/64.0)-1.0;
		}
		
	}
	private void initA() {
		lsx[0] = 32;				// top horizontal line
		lsy[0] = 96;
		lex[0] = 64;
		ley[0] = 16;

		lsx[1] = 64;				// lower horizontal line
		lsy[1] = 16;
		lex[1] = 96;
		ley[1] = 96;
		
		lsx[2] = 48;				// vertical line
		lsy[2] = 64;
		lex[2] = 80;
		ley[2] = 64;
		
		numberOfLinesInUse = 3;

		// initialize a[], b[], c[], cx[], cy[]
		for (int n=0; n<numberOfLinesInUse; n++) {
			lsx[n] = (lsx[n]/64.0)-1.0;
			lsy[n] = (lsy[n]/64.0)-1.0;
			lex[n] = (lex[n]/64.0)-1.0;
			ley[n] = (ley[n]/64.0)-1.0;
		}

	}
	private void initI() {
		lsx[0] = 48;				// top horizontal line
		lsy[0] = 32;
		lex[0] = 80;
		ley[0] = 32;

		lsx[1] = 48;				// lower horizontal line
		lsy[1] = 96;
		lex[1] = 80;
		ley[1] = 96;
		
		lsx[2] = 64;				// vertical line
		lsy[2] = 32;
		lex[2] = 64;
		ley[2] = 96;
		
		numberOfLinesInUse = 3;

		// initialize a[], b[], c[], cx[], cy[]
		for (int n=0; n<numberOfLinesInUse; n++) {
			lsx[n] = (lsx[n]/64.0)-1.0;
			lsy[n] = (lsy[n]/64.0)-1.0;
			lex[n] = (lex[n]/64.0)-1.0;
			ley[n] = (ley[n]/64.0)-1.0;
		}

	}

	private void initCapture() {
		trackCaptureEventCounter = 1;
		// this will start collecting data in processEvent

		for (int x=0; x<127; x++) {
			for (int y=0; y<127; y++) {
				captureEventMatrix[x][y] = false;
			}
		}
System.out.println("InitCapture");
	}

	// ******************************************************************************************************************************************************
	// ******************************************************************************************************************************************************
	
	final int linelength = 20;
	final int linewidth = 1;

	final double fractionOfEventsRequired = 0.8;
	
	private void computeCaptureObject() {
		System.out.println("Capture Done ... now computing object");

		// this prints a rudimentary ascii representation of the date we collected.
//		for (int y=0; y<127; y++) {
//			for (int x=0; x<127; x++) {
//				if (captureEventMatrix[x][y] == true) System.out.print("*"); else System.out.print(" ");
//			}
//			System.out.println("");
//		}

		
		// this is your 128x128 matrix of booleans:
		// captureEventMatrix[x][y]

		// check all line segments of length
		// at least linelength pixels that happen
		// to lie (nearly) completely inside the
		// black area

		int x, y, rot, i;
		int n, w=0;
		int lineX[] = new int[128];
		int lineY[] = new int[128];
		int threshold = (int)(fractionOfEventsRequired * linelength);

		numberOfLinesInUse = 0;

		for (y=linewidth; y<(128-linewidth); y = (y + 106) % 128)
		{
			for (x=linewidth; x<(128-linewidth); x = (x + 62) % 128)
			{
				int bestIndex = -1;
				double bestValue = 0.0;
				for (rot=0; rot < 32; rot++)
				{
					double angle = (Math.PI / 32.0) * rot;
					double c = Math.cos(angle);
					double s = Math.sin(angle);
					double abs_c = Math.abs(c);
					int sign_c = (c < 0.0) ? -1 : +1;
					int xx = x, yy = y;
					double ac = 0.0;
					double as = 0.0;
					int set = 0;
					for (i=0; i<=linelength; i++)
					{
						if (abs_c > s)
						{
							w = (int)(linewidth / abs_c);
							for (n=-w; n<=w; n++) if (captureEventMatrix[xx][yy+n] == true) set++;
						}
						else
						{
							w = (int)(linewidth / s);
							for (n=-w; n<=w; n++) if (captureEventMatrix[xx+n][yy] == true) set++;
						}

						lineX[i] = xx;
						lineY[i] = yy;
						ac += abs_c;
						if (ac >= 1.0)
						{
							ac -= 1.0;
							xx += sign_c;
							if (xx < linewidth || xx >= (128-linewidth)) break;
						};
						as += s;
						if (as >= 1.0)
						{
							as -= 1.0;
							yy++;
							if (yy >= (128-linewidth)) break;
						}
					}

					if (((double)set / (2*w+1) > bestValue) && ((double)set / (2*w+1) > threshold)) {
						bestIndex = rot;
						bestValue = (double)set / (2*w+1);
					}
				}

				// **** here we know which angle is the best for this pixel
				if (bestIndex != -1)
				{
					double angle = (Math.PI / 32.0) * bestIndex;
					double c = Math.cos(angle);
					double s = Math.sin(angle);
					double abs_c = Math.abs(c);
					int sign_c = (c < 0.0) ? -1 : +1;
					int xx = x, yy = y;
					double ac = 0.0;
					double as = 0.0;
					for (i=0; i<=linelength; i++)
					{
						if (abs_c > s)
						{
							w = (int)(linewidth / abs_c);
							for (n=-w; n<=w; n++) captureEventMatrix[xx][yy+n] = false;
						}
						else
						{
							w = (int)(linewidth / s);
							for (n=-w; n<=w; n++) captureEventMatrix[xx+n][yy] = false;
						}

						ac += abs_c;
						if (ac >= 1.0)
						{
							ac -= 1.0;
							xx += sign_c;
							if (xx < linewidth || xx >= (128-linewidth)) break;
						};
						as += s;
						if (as >= 1.0)
						{
							as -= 1.0;
							yy++;
							if (yy >= (128-linewidth)) break;
						}
					}

					lsx[numberOfLinesInUse] = 2.0 * (x / (double)128) - 1.0;
					lsy[numberOfLinesInUse] = 2.0 * (y / (double)128) - 1.0;
					lex[numberOfLinesInUse] = 2.0 * ((x + (int)(c * linelength)) / (double)128) - 1.0;
					ley[numberOfLinesInUse] = 2.0 * ((y + (int)(s * linelength)) / (double)128) - 1.0;
					numberOfLinesInUse++;
				}
			}
		}



		// this is what we need to get filled in range -1 .. +1
//		lsx[0..xx] = yy;
//		lsy[0..xx] = yy;
//		lex[0..xx] = yy;
//		ley[0..xx] = yy;

		// and this shall be the number of lines
//		numberOfLinesInUse = xx+1;

		
// do not change below this line		
		trackCaptureEventCounter = 0;

	}
	// ******************************************************************************************************************************************************
	// ******************************************************************************************************************************************************

	public void initTemplate() {

		if (trackA.isSelected()) initA();
		if (trackI.isSelected()) initI();

		if (trackSquare.isSelected()) initSquare();
		
		if (trackP56.isSelected()) initPig56();
		if (trackP22.isSelected()) initPig20();

		if (trackCapture.isSelected()) initCapture();
		
		precomputeUsingEndpoints();
	}

	public void init() {

		isActive.setText("PigTracker");

		resetIdentityMatrix();

		initTemplate();

	}

	public void paintComponent(Graphics g) {

		double l1, l2, l3, l4, l5, l6, l7, l8, l9;				// this is the inverse of the m matrix

		double den = 1.0 / (m1*m5*m9 + m2*m6*m7 + m3*m4*m8 - m1*m6*m8 - m2*m4*m9 - m3*m5*m7);
		l1 = den * (m5*m9 - m6*m8);
		l2 = den * (m3*m8 - m2*m9);
		l3 = den * (m2*m6 - m3*m5);
		l4 = den * (m6*m7 - m4*m9);
		l5 = den * (m1*m9 - m3*m7);
		l6 = den * (m3*m4 - m1*m6);
		l7 = den * (m4*m8 - m5*m7);
		l8 = den * (m2*m7 - m1*m8);
		l9 = den * (m1*m5 - m2*m4);				// here we have the inverse of m


		g.setColor(Color.CYAN);
		for (int n=0; n<numberOfLinesInUse; n++) {

			double tmpX = (l1*lsx[n]+l2*lsy[n]+l3);
			double tmpY = (l4*lsx[n]+l5*lsy[n]+l6);
			double den2  = (l7*lsx[n]+l8*lsy[n]+l9);
			
			double gsx = tmpX / den2;
			double gsy = tmpY / den2;

			tmpX = (l1*lex[n]+l2*ley[n]+l3);
			tmpY = (l4*lex[n]+l5*ley[n]+l6);
			den2 = (l7*lex[n]+l8*ley[n]+l9);
			
			double gex = tmpX / den2;
			double gey = tmpY / den2;

			g.drawLine((int) (64*(gsx+1)*4), (int) (64*(gsy+1)*4), (int) (64*(gex+1)*4), (int) (64*(gey+1)*4));
			
			// reset template to current state
			lsx[n] = gsx;
			lsy[n] = gsy;
			lex[n] = gex;
			ley[n] = gey;
		}
		precomputeUsingEndpoints();
		resetIdentityMatrix();

	}

	public int processNewEvent(int eventX, int eventY, int eventP) {

		if (trackCaptureEventCounter != 0) {
//System.out.println("Collecting this event " + trackCaptureEventCounter);
			captureEventMatrix[eventX][eventY] = true;
			trackCaptureEventCounter++;
			if (trackCaptureEventCounter == numberOfCaptureEvents) computeCaptureObject();
			return(0);
		}

		double eX = (((double) eventX) / 64.0)-1.0;
		double eY = (((double) eventY) / 64.0)-1.0;

		double minErr = 100000;
		int minDistIndex = -1;
		double bigProduct = 0;

//		System.out.println("--------- Processing Event: (" + eventX + "," + eventY + ") --> (" + eX + "," + eY + ")");
		for (int n=0; n<numberOfLinesInUse; n++) {
			// check that pixel is (1) inside circle and (2) is close to line
			double distX = (lcx[n]-eX);
			double distY = (lcy[n]-eY);
			double distCenter = distX*distX + distY*distY;
			
			if (distCenter > lch[n])
				continue;

			double signedLineError = la[n] * (m1*eX + m2*eY + m3) +
							     lb[n] * (m4*eX + m5*eY + m6) +
							     lc[n] * (m7*eX + m8*eY + m9);
			
			double absLineError = signedLineError < 0 ? -signedLineError : signedLineError;					//  ensure positive

//			System.out.println("Line " + n + ": (" + lsx[n] + "," + lsy[n] + ")-(" + lex[n] + "," + ley[n] + ")"
//							+ " has distance " + distTotal + " (" +distCenter+" / "+distPerp+")");
			if (absLineError < minErr) {
				minErr = absLineError;
				minDistIndex = n;
				bigProduct = signedLineError;
			}
		}

		if (minErr <= distanceThreshold) {

			//System.out.println("This event " + eX + "/" + eY + " goes to line " + minDistIndex + "  with distance " + minDist + " minDistPerp: " + minDistPerp);
//			System.out.println("Using line " + minDistIndex);
//			System.out.println("la: " + la[minDistIndex] + ", lb: " + lb[minDistIndex] + ", lc: " + lc[minDistIndex]);
			
			bigProduct *= 0.1;
			m1 -= bigProduct * la[minDistIndex] * eX;
			m2 -= bigProduct * la[minDistIndex] * eY;
			m3 -= bigProduct * la[minDistIndex];

			m4 -= bigProduct * lb[minDistIndex] * eX;
			m5 -= bigProduct * lb[minDistIndex] * eY;
			m6 -= bigProduct * lb[minDistIndex];

			m7 -= bigProduct * lc[minDistIndex] * eX;
			m8 -= bigProduct * lc[minDistIndex] * eY;
			m9 -= bigProduct * lc[minDistIndex];

			if (trackingMode == 1) {
				// any M is ok
			} else if (trackingMode == 2) {
				// eliminate shear but allow perspective, rotation, and scaling
				double err = m5 * m9 - m6 * m8 - m1 * m9 + m3 * m7;
				err *= 0.5;
				m1 += err * m9;
				m3 -= err * m7;
				m5 -= err * m9;
				m6 += err * m8;
				m7 -= err * m3;
				m8 += err * m6;
				m9 += err * (m1 - m5);
			} else if (trackingMode == 3) {
				// allow only rotation and scaling
				m7 = m8 = 0.0;
				m1 = m5 = (m1 + m5) / 2.0;
				m2 = (m2 - m4) / 2.0;
				m4 = -m2;
			} else {
				System.out.println("Bad trackingMode: " + trackingMode);
			}
//			System.out.println("m: " + m1 + " " + m2 + " " + m3 + " " + m4 + " " + m5 + " " + m6 + " " + m7 + " " + m8 + " " + m9);

		} else {
//			System.out.println("MinDist too big: " + minDist);
			return(1);
		}

		return(2);
	}

	public void processSpecialData(String specialData) {
	}

	public void callBackButtonPressed(ActionEvent e) {
		System.out.println("CallBack!");

		new showGUI();

		initTemplate();
	}
}
