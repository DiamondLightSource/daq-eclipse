package org.eclipse.scanning.test.points;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.scanning.api.points.IPointGenerator;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.Point;
import org.eclipse.scanning.points.JythonPointGeneratorFactory;
import org.eclipse.scanning.points.JythonPointGeneratorFactory.JythonObjectFactory;
import org.junit.Test;

public class JythonPointGeneratorFactoryTest {

    @Test
    public void testNewLineGeneratorFactory() {
        JythonObjectFactory lineGeneratorFactory = JythonPointGeneratorFactory.newLineGeneratorFactory();
        IPointGenerator generator = (IPointGenerator) lineGeneratorFactory.createObject("x", "mm", 1.0, 5.0, 5, false);
        //TODO: check the alternate_direction default=False case
        Iterator iterator = generator.iterator();
        
        List<IPosition> points = new ArrayList<IPosition>();
        while (generator.iterator().hasNext()){
            Object next = generator.iterator().next();
            points.add((IPosition) next);
        }

        assertEquals(new Point(0, 1.0, 0, 1.0, false), points.get(0));
        assertEquals(new Point(1, 2.0, 1, 2.0, false), points.get(1));
        assertEquals(new Point(2, 3.0, 2, 3.0, false), points.get(2));
        assertEquals(new Point(3, 4.0, 3, 4.0, false), points.get(3));
        assertEquals(new Point(4, 5.0, 4, 5.0, false), points.get(4));
    }

}
