from scanpointgenerator.compat import np
from scanpointgenerator.core import Generator

def to_list(value):
    if isinstance(value, list):
        return value
    else:
        return [value]

@Generator.register_subclass("scanpointgenerator:generator/ArrayGenerator:1.0")
class ArrayGenerator(Generator):
    """Generate points fron a given list of positions"""

    def __init__(self, axes, units, points, alternate_direction=False):
        self.axes = to_list(axes)
        self.units = {d:u for (d, u) in zip(self.axes, to_list(units))}
        self.alternate_direction = alternate_direction
        self.points = np.array(points, dtype=np.float64)
        if self.points.shape == (len(self.points),):
            self.points = self.points.reshape((len(self.points), 1))
        self.size = len(self.points)
        if len(self.axes) != len(set(self.axes)):
            raise ValueError("Axis names cannot be duplicated; given %s" %
                axes)

        gen_name = "Array"
        for axis_name in self.axes[::-1]:
            gen_name = axis_name + "_" + gen_name
        self.index_names = [gen_name]

    def prepare_arrays(self, index_array):
        points = self.points
        # add linear extension to ends of points, representing t=-1 and t=N+1
        v_left = points[0] - (points[1] - points[0])
        v_right = points[-1] + (points[-1] - points[-2])
        shape = points.shape
        shape = (shape[0] + 2,) + shape[1:]
        extended = np.empty(shape, dtype=points.dtype)
        extended[1:-1] = points
        extended[0] = v_left
        extended[-1] = v_right
        points = extended
        index_floor = np.floor(index_array).astype(np.int32)
        epsilon = index_array - index_floor
        epsilon = epsilon.reshape((-1, 1))

        index_floor += 1

        values = points[index_floor] + epsilon * (points[index_floor+1] - points[index_floor])
        values = values.T
        arrays = {}
        for (i, name) in enumerate(self.axes):
            arrays[name] = values[i]
        return arrays

    def to_dict(self):
        d = {
                "typeid":self.typeid,
                "axes":self.axes,
                "units":[self.units[a] for a in self.axes],
                "points":self.points.ravel().tolist(),
                "alternate_direction":self.alternate_direction,
            }
        return d

    @classmethod
    def from_dict(cls, d):
        axes = d["axes"]
        units = d["units"]
        alternate_direction = d["alternate_direction"]
        flat_points = d["points"]
        arr_shape = (int(len(flat_points) // len(axes)), len(axes))
        points = np.array(flat_points).reshape(arr_shape)
        return cls(axes, units, points, alternate_direction)
