/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ch.unizh.ini.caviar.hardwareinterface;

/**
 * Interface has firmware that can be updated from the host, e.g. via USB. A hardware interface that implements this interface
 * can eaily be updated.
 * 
 * @author tobi
 */
public interface HasUpdatableFirmware {
        
    /** Updates the firmware. Implementing classes must define how this is achieved, e.g. by 
     * downloading firmware from the project resources stored in the classpath as part of the project build.
     * 
     * @throws ch.unizh.ini.caviar.hardwareinterface.HardwareInterfaceException if there is any error, including lack of support
     * or missing firmware file. The exception should detail the error as much as possible.
     */
    public void updateFirmware() throws HardwareInterfaceException;
        
}
