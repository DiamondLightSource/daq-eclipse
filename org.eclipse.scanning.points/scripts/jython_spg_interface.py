from org.eclipse.scanning.api.points import Point

from scanpointgenerator import LineGenerator
from scanpointgenerator import CompoundGenerator

def create_line(name, units, start, stop, num_points):
    
    line = LineGenerator(name, units, start, stop, num_points)
    
    for point in line.iterator():
        index = point.indexes[0]
        position = point.positions[name]
        java_point = Point(index, position, index, position, False)
        
        yield java_point

def create_2D_line(names, units, start, stop, num_points):
    
    line = LineGenerator(names, units, start, stop, num_points)
    
    for point in line.iterator():
        index = point.indexes[0]
        xPosition = point.positions[names[0]]
        yPosition = point.positions[names[1]]
        java_point = Point(index, xPosition, index, yPosition, False)
        
        yield java_point
