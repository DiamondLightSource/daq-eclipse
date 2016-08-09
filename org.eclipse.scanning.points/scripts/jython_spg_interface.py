from java.util import Iterator;
from org.eclipse.scanning.api.points import Point
from org.eclipse.scanning.api.points import Scalar
from org.eclipse.scanning.api.points import MapPosition
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


class JavaIteratorWrapper(Iterator):
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
    
    def __init__(self, name, units, start, stop, num_points, alternate_direction=False):
        super(JLineGenerator2D, self).__init__()
        
        start = start.tolist()  # Convert from array to list
        stop = stop.tolist()
        
        self.name = name
        self.generator = LineGenerator(name, units, start, stop, num_points, alternate_direction)
        logging.debug(self.generator.to_dict())
    
    def _iterator(self):
        
        for point in self.generator.iterator():
            index = point.indexes[0]
            xName = self.name + "_X"
            yName = self.name + "_Y"
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
            

# class JRasterGenerator(JavaIteratorWrapper):
#     """
#     Create a CompoundGenerator with two LineGenerators and wrap the points
#     into java Point objects
#     """
# 
#     def __init__(self, outer_line, inner_line, alternate_direction=False):
#         super(JRasterGenerator, self).__init__()
#         
#         self.inner_name = inner_line['name']
#         name = [inner_line['name']]
#         units = inner_line['units']
#         start = [inner_line['start']]
#         stop = [inner_line['stop']]
#         num_points = inner_line['num_points']
#         inner_line = LineGenerator(name, units, start, stop, num_points, alternate_direction)
#         
#         self.outer_name = outer_line['name']
#         name = [outer_line['name']]
#         units = outer_line['units']
#         start = [outer_line['start']]
#         stop = [outer_line['stop']]
#         num_points = outer_line['num_points']
#         outer_line = LineGenerator(name, units, start, stop, num_points)
#         
#         self.generator = CompoundGenerator([outer_line, inner_line], [], [])
#         logging.debug(self.generator.to_dict())
#     
#     def _iterator(self):
#         
#         for point in self.generator.iterator():
#             xIndex = point.indexes[0]
#             yIndex = point.indexes[1]
#             xName = self.inner_name
#             yName = self.outer_name
#             xPosition = point.positions[xName]
#             yPosition = point.positions[yName]
#             java_point = Point(xName, xIndex, xPosition, 
#                                yName, yIndex, yPosition)
#             # Set is2D=False
#             
#             yield java_point
            

class JSpiralGenerator(JavaIteratorWrapper):
    """
    Create a SpiralGenerator and wrap the points into java Point objects
    """

    def __init__(self, name, units, centre, radius, scale=1.0, alternate_direction=False):
        super(JSpiralGenerator, self).__init__()
        
        self.name = name
        self.generator = SpiralGenerator(name, units, centre, radius, scale, alternate_direction)
        logging.debug(self.generator.to_dict())
        
    
    def _iterator(self):
        
        xName = self.name + "_X"
        yName = self.name + "_Y"
        
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

    def __init__(self, name, units, box, num_lobes, num_points):
        super(JLissajousGenerator, self).__init__()
        
        self.name = name
        self.generator = LissajousGenerator(name, units, box, num_lobes, num_points)
        logging.debug(self.generator.to_dict())
    
    def _iterator(self):
        
        xName = self.name + "_X"
        yName = self.name + "_Y"
        
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
        
        try:
            generators = [iterator.generator for iterator in iterators]
        except AttributeError:
            generators = [iterator.getPyIterator().generator for iterator in iterators]
        logging.debug("Generators passed to JCompoundGenerator:")
        logging.debug([generator.to_dict() for generator in generators])
        
        excluders = [excluder.py_excluder for excluder in excluders]
        mutators = [mutator.py_mutator for mutator in mutators]
        
        self.names = [generator.name for generator in generators]
        
        self.generator = CompoundGenerator(generators, excluders, mutators)
        logging.debug("CompoundGenerator:")
        logging.debug(self.generator.to_dict())
    
    def _iterator(self):
        
        xName = self.names[1]
        yName = self.names[0]
        
        for point in self.generator.iterator():
            if len(point.positions.keys()) > 2:
                java_point = MapPosition()
                
                names = ArrayList()
                for index, (axis, value) in enumerate(point.positions.items()):
                    logging.debug([index, point.indexes, point.positions])
                    java_point.put(axis, value)
                    java_point.putIndex(axis, point.indexes[index])
                    name = ArrayList()
                    name.add(axis)
                    names.add(name)
                    
                java_point.setDimensionNames(names)
            else:
                xIndex = point.indexes[1]
                yIndex = point.indexes[0]
                xPosition = point.positions[xName]
                yPosition = point.positions[yName]
                java_point = Point(xName, xIndex, xPosition, 
                                   yName, yIndex, yPosition)
            
            yield java_point


class JRandomOffsetMutator(object):
    
    def __init__(self, seed, max_offset):
        self.py_mutator = RandomOffsetMutator(seed, max_offset)
        logging.debug(self.py_mutator.to_dict())
