from scanpointgenerator.compat import np
from scanpointgenerator.core import Generator


@Generator.register_subclass("scanpointgenerator:generator/ArrayGenerator:1.0")
class ArrayGenerator(Generator):
    """Generate points fron a given list of positions"""

    def __init__(self, names, units, points, alternate_direction=False):
        self.names = names
        self.units = units
        self.alternate_direction = alternate_direction
        self.points = np.array(points, dtype=np.float64)
        if self.points.shape == (len(self.points),):
            self.points = self.points.reshape((len(self.points), 1))
        self.size = len(self.points)
        self.position_units = {n:self.units for n in names}
        if len(self.names) != len(set(self.names)):
            raise ValueError("Axis names cannot be duplicated; given %s" %
                names)
        self.axes = self.names

        gen_name = "Array"
        for axis_name in self.names[::-1]:
            gen_name = axis_name + "_" + gen_name
        self.index_names = [gen_name]

    def prepare_arrays(self, index_array):
        points = self.points
        # add linear extension to ends of points, representing t=-1 and t=N+1
        v_left = points[0] - (points[1] - points[0])
        v_right = points[-1] + (points[-1] - points[-2])
        points = np.insert(points, 0, v_left, 0)
        points = np.append(points, [v_right], 0)
        index_floor = np.floor(index_array).astype(np.int32)
        epsilon = index_array - index_floor
        epsilon = epsilon.reshape((-1, 1))

        index_floor += 1

        values = points[index_floor] + epsilon * (points[index_floor+1] - points[index_floor])
        values = values.T
        arrays = {}
        for (i, name) in enumerate(self.names):
            arrays[name] = values[i]
        return arrays

    def to_dict(self):
        d = {
                "typeid":self.typeid,
                "names":self.names,
                "units":self.units,
                "points":self.points.ravel().tolist(),
                "alternate_direction":self.alternate_direction,
            }
        return d

    @classmethod
    def from_dict(cls, d):
        names = d["names"]
        units = d["units"]
        alternate_direction = d["alternate_direction"]
        flat_points = d["points"]
        arr_shape = (int(len(flat_points) // len(names)), len(names))
        points = np.array(flat_points).reshape(arr_shape)
        return cls(names, units, points, alternate_direction)
