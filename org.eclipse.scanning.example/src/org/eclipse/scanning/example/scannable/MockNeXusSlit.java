/*-
 *******************************************************************************
 * Copyright (c) 2011, 2016 Diamond Light Source Ltd.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Matthew Gerring - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.scanning.example.scannable;

import org.eclipse.dawnsci.nexus.NXpositioner;
import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.dawnsci.nexus.NexusScanInfo;
import org.eclipse.dawnsci.nexus.builder.NexusObjectProvider;

/**
 * 
 * See http://confluence.diamond.ac.uk/pages/viewpage.action?pageId=37814632
 * 
 <pre>
 primary_slit:NXslit
    x_gap = {NX_NUMBER}
        @local_name = "s1gapX"
        @units = "mm"
        @controller_record = {NX_CHAR} //EPICS name
    y_gap = {NX_NUMBER}
        @local_name = "s1gapY"
        @units = "mm"
        @controller_record = {NX_CHAR} //EPICS name
    transforms:NXtransformations
        x_centre = {NX_NUMBER}
            @transformation_type = "translation"
            @vector = 1,0,0,
            @depends_on = "y_centre"
            @units = "mm"
            @controller_record = {NX_CHAR} //EPICS name
        y_centre = {NX_NUMBER}
            @transformation_type = "translation"
            @vector = 0,1,0,
            @depends_on = "."
            @offset = 0,0,-14500 //This may change
            @units = "mm"
            @controller_record = {NX_CHAR} //EPICS name
    beam:NXbeam
        //Removed distance from here, since it's in the "y_centre" above
        incident_beam_divergence[2,i] = {NX_FLOAT}
            @units = "radians"
        final_beam_divergence[2,i] = {NX_FLOAT}
            @units = "radians"
    motors:NXcollection
        downstream_x:NXpositioner
            name = "s1dsX"
            description = "Downstream X position"
            value = {NX_NUMBER}
                @units = "mm"
            controller_record = {NX_CHAR} //EPICS name
        downstream_y:NXpositioner
            name = "s1dsY"
            description = "Downstream Y position"
            value = {NX_NUMBER}
                @units = "mm"
            controller_record = {NX_CHAR} //EPICS name
        upstream_x:NXpositioner
            name = "s1usX"
            description = "Upstream X position"
            value = {NX_NUMBER}
                @units = "mm"
            controller_record = {NX_CHAR} //EPICS name
        upstream_y:NXpositioner
            name = "s1usY"
            description = "Upstream Y position"
            value = {NX_NUMBER}
                @units = "mm"
            controller_record = {NX_CHAR} //EPICS name
 </pre>
 
 * 
 * @author Matthew Gerring
 *
 */
public class MockNeXusSlit extends MockNeXusScannable {

	public MockNeXusSlit() {
		super();
	}

	public MockNeXusSlit(String name, double d, int level, String unit) {
		super(name, d, level, unit);
	}

	public MockNeXusSlit(String name, double d, int level) {
		super(name, d, level);
	}

	public NexusObjectProvider<NXpositioner> getNexusProvider(NexusScanInfo info) throws NexusException {
        // TODO FIXME Use NeXus API to create slit information.
		return super.getNexusProvider(info);
	}
}
