from org.eclipse.scanning.api.points import Point
from org.eclipse.scanning.api.points import Scalar
from org.eclipse.scanning.api.points import MapPosition
from org.eclipse.scanning.points import SerializableIterator
from java.util import ArrayList

from scanpointgenerator import LineGenerator
from scanpointgenerator import ArrayGenerator
from scanpointgenerator import SpiralGenerator
from scanpointgenerator import LissajousGenerator
from scanpointgenerator import CompoundGenerator
from scanpointgenerator import RandomOffsetMutator

# logging
import logging
# logging.basicConfig(level=logging.DEBUG)


class JavaIteratorWrapper(SerializableIterator):
    """
    A wrapper class to give a python iterator the while(hasNext() {next()})
    operation required of Java Iterators
    """
    
    def __init__(self):
        self._iterator = iter(self._iterator())
        self._has_next = None
        self._next = None
    
    def iterator(self):
        return self
    
    def _iterator(self):
        raise NotImplementedError("Must be implemented in child class")

    def next(self):
        
        if self._has_next:
            result = self._next
        else:
            result = next(self._iterator)
            
        self._has_next = None
        
        return result
    
    def hasNext(self):
        
        if self._has_next is None:
            
            try:
                self._next = next(self._iterator)
            except StopIteration:
                self._has_next = False
            else:
                self._has_next = True
            
        return self._has_next
    
    def toDict(self):
        return self.generator.to_dict()
    
    def size(self):
        return self.generator.num


class JLineGenerator1D(JavaIteratorWrapper):
    """
    Create a 1D LineGenerator and wrap the points into java Scalar objects
    """
    
    def __init__(self, name, units, start, stop, num_points, alternate_direction=False):
        super(JLineGenerator1D, self).__init__()
        
        self.name = name
        self.generator = LineGenerator(name, units, start, stop, num_points, alternate_direction)
        logging.debug(self.generator.to_dict())
    
    def _iterator(self):
        
        for point in self.generator.iterator():
            index = point.indexes[0]
            position = point.positions[self.name]
            java_point = Scalar(self.name, index, position)
            
            yield java_point
            

class JLineGenerator2D(JavaIteratorWrapper):
    """
    Create a 2D LineGenerator and wrap the points into java Point objects
    """
    
    def __init__(self, names, units, start, stop, num_points, alternate_direction=False):
        super(JLineGenerator2D, self).__init__()
        
        start = start.tolist()  # Convert from array to list
        stop = stop.tolist()
        
        self.names = names
        self.generator = LineGenerator(names, units, start, stop, num_points, alternate_direction)
        logging.debug(self.generator.to_dict())
    
    def _iterator(self):
        
        for point in self.generator.iterator():
            index = point.indexes[0]
            xName = self.names[0]
            yName = self.names[1]
            xPosition = point.positions[xName]
            yPosition = point.positions[yName]
            java_point = Point(xName, index, xPosition, 
                               yName, index, yPosition, False)
            # Set is2D=False
            
            yield java_point
            

class JArrayGenerator(JavaIteratorWrapper):
    """
    Create an ArrayGenerator and wrap the points into java Scalar objects
    """

    def __init__(self, name, units, points):
        super(JArrayGenerator, self).__init__()
        
        points = points.tolist()  # Convert from array to list
        
        self.name = name
        self.generator = ArrayGenerator(name, units, points)
        logging.debug(self.generator.to_dict())
    
    def _iterator(self):
        
        for point in self.generator.iterator():
            name = self.name[0]
            index = point.indexes[0]
            position = point.positions[name]
            java_point = Scalar(name, index, position)
            
            yield java_point
            

class JSpiralGenerator(JavaIteratorWrapper):
    """
    Create a SpiralGenerator and wrap the points into java Point objects
    """

    def __init__(self, names, units, centre, radius, scale=1.0, alternate_direction=False):
        super(JSpiralGenerator, self).__init__()
        
        self.names = names
        self.generator = SpiralGenerator(names, units, centre, radius, scale, alternate_direction)
        logging.debug(self.generator.to_dict())
        
    
    def _iterator(self):
        
        xName = self.names[0]
        yName = self.names[1]
        
        for point in self.generator.iterator():
            index = point.indexes[0]
            xPosition = point.positions[xName]
            yPosition = point.positions[yName]
            java_point = Point(xName, index, xPosition, 
                               yName, index, yPosition, False)
            # Set is2D=False
            
            yield java_point
            

class JLissajousGenerator(JavaIteratorWrapper):
    """
    Create a LissajousGenerator and wrap the points into java Point objects
    """

    def __init__(self, names, units, box, num_lobes, num_points):
        super(JLissajousGenerator, self).__init__()
        
        self.names = names
        self.generator = LissajousGenerator(names, units, box, num_lobes, num_points)
        logging.debug(self.generator.to_dict())
    
    def _iterator(self):
        
        xName = self.names[0]
        yName = self.names[1]
        
        for point in self.generator.iterator():
            index = point.indexes[0]
            xPosition = point.positions[xName]
            yPosition = point.positions[yName]
            java_point = Point(xName, index, xPosition, 
                               yName, index, yPosition, False)
            # Set is2D=False
            
            yield java_point
            

class JCompoundGenerator(JavaIteratorWrapper):
    """
    Create a CompoundGenerator and wrap the points into java Point objects
    """

    def __init__(self, iterators, excluders, mutators):
        super(JCompoundGenerator, self).__init__()
        
        try: # If JavaIteratorWrapper
            generators = [iterator.generator for iterator in iterators]
        except AttributeError: # Else call get*() of Java iterator
            generators = [iterator.getPyIterator().generator for iterator in iterators]
        logging.debug("Generators passed to JCompoundGenerator:")
        logging.debug([generator.to_dict() for generator in generators])
        
        excluders = [excluder.py_excluder for excluder in excluders]
        mutators = [mutator.py_mutator for mutator in mutators]
        
        extracted_generators = []
        for generator in generators:
            if generator.__class__.__name__ == "CompoundGenerator":
                extracted_generators.extend(generator.generators)
            else:
                extracted_generators.append(generator)
        generators = extracted_generators
        
        self.index_locations = {}
        self.scan_names = ArrayList()
        for index, generator in enumerate(generators):
            
            for axis in generator.axes:
                self.index_locations[axis] = index
                
                scan_name = ArrayList()
                for axis in generator.axes:
                    scan_name.add(axis)
                
            self.scan_names.add(scan_name)
        
        logging.debug("Index Locations:")
        logging.debug(self.index_locations)
        
        self.generator = CompoundGenerator(generators, excluders, mutators)
        
        logging.debug("CompoundGenerator:")
        logging.debug(self.generator.to_dict())
    
    def _iterator(self):
        
        for point in self.generator.iterator():
            
            if len(point.positions.keys()) == 1:
                for point in self.generator.iterator():
                    name = point.positions.keys()[0]
                    index = point.indexes[0]
                    position = point.positions[name]
                    java_point = Scalar(name, index, position)
                
            elif len(point.positions.keys()) == 2:
                    xName = self.generator.axes[1]
                    yName = self.generator.axes[0]
                    xIndex = point.indexes[1]
                    yIndex = point.indexes[0]
                    xPosition = point.positions[xName]
                    yPosition = point.positions[yName]
                    java_point = Point(xName, xIndex, xPosition, 
                                       yName, yIndex, yPosition)
            else:
                java_point = MapPosition()
                
                for axis, value in point.positions.items():
                    index = self.index_locations[axis]
                    logging.debug([index, point.indexes, point.positions])
                    java_point.put(axis, value)
                    java_point.putIndex(axis, point.indexes[index])
                
                java_point.setDimensionNames(self.scan_names)
                            
            yield java_point


class JRandomOffsetMutator(object):
    
    def __init__(self, seed, max_offset):
        self.py_mutator = RandomOffsetMutator(seed, max_offset)
        logging.debug(self.py_mutator.to_dict())
