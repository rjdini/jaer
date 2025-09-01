package com.inilabs.jaer.projects.exec;

import com.inilabs.jaer.projects.space3d.AbstractAgent3D;
import com.inilabs.jaer.projects.space3d.Agent3D;
import com.inilabs.jaer.projects.space3d.Space3D;
import com.inilabs.jaer.projects.space3d.TargetAgent;
import com.inilabs.jaer.projects.space3d.TargetShape;

public final class WorldPresets {
    private WorldPresets(){}
    public enum Preset { EXAMPLE }
    public static WorldInitializer preset(Preset p){ return WorldPresets::buildExampleWorld; }
    private static void buildExampleWorld(Space3D world) {
        world.setHalfExtentM(300);
        AbstractAgent3D cam = new AbstractAgent3D("dvx-0", Agent3D.ObjectType.DVXPLORER) {};
        cam.setPosition3D(new Space3D.Vec3(0, 0, 0));
        world.addAgent(cam);
        
        TargetAgent t1 = new TargetAgent("tgt-circle", new Space3D.Vec3(-15,0,120), new Space3D.Vec3(+15,0,40), 10.0);
        t1.setPhysicalDiameterM(1.0f); t1.setShape(TargetShape.CIRCLE); t1.setDensityScale(1.0f); world.addAgent(t1);
        
        TargetAgent t2 = new TargetAgent("tgt-square", new Space3D.Vec3(+20,0,150), new Space3D.Vec3(-20,0,60), 8.0);
        t2.setPhysicalDiameterM(1.5f); t2.setShape(TargetShape.SQUARE); t2.setDensityScale(1.2f); world.addAgent(t2);
        
        TargetAgent t3 = new TargetAgent("tgt-triangle", new Space3D.Vec3(-10,5,130), new Space3D.Vec3(+10,-5,50), 12.0);
        t3.setPhysicalDiameterM(0.8f); t3.setShape(TargetShape.TRIANGLE); t3.setDensityScale(0.9f); world.addAgent(t3);
        
        TargetAgent t4 = new TargetAgent("tgt-cross", new Space3D.Vec3(+5,0,110), new Space3D.Vec3(-5,0,30), 9.0);
        t4.setPhysicalDiameterM(1.2f); t4.setShape(TargetShape.CROSS); t4.setDensityScale(1.0f); world.addAgent(t4);
        t1.start(); t2.start(); t3.start(); t4.start();
    }
}