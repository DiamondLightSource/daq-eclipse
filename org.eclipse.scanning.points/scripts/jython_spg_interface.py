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

def create_raster(inner_line, outer_line, alternate_direction=False):
    
    inner_name = name = inner_line['name']
    units = inner_line['units']
    start = inner_line['start']
    stop = inner_line['stop']
    num_points = inner_line['num_points']
    inner_line = LineGenerator(name, units, start, stop, num_points, alternate_direction)
    
    outer_name = name = outer_line['name']
    units = outer_line['units']
    start = outer_line['start']
    stop = outer_line['stop']
    num_points = outer_line['num_points']
    outer_line = LineGenerator(name, units, start, stop, num_points)
    
    raster = CompoundGenerator([inner_line, outer_line], [], [])
    
    for point in raster.iterator():
        xIndex = point.indexes[0]
        yIndex = point.indexes[1]
        xPosition = point.positions[inner_name]
        yPosition = point.positions[outer_name]
        java_point = Point(xIndex, xPosition, yIndex, yPosition)
        
        yield java_point
