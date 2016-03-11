# This file is executed inline by the by the Jython interpreter (i.e. it's not
# "imported" in the usual Python sense).

from jarray import array as _array


# There are some setter fields which the end user does not set:
setter_blacklist = {
    '_ScanRequest': frozenset(['setAfter',
                               'setAfterResponse',
                               'setBefore',
                               'setBeforeResponse',
                               'setEnd',
                               'setFilePath',
                               'setIgnorePreprocess',
                               'setMonitorNames',
                               'setStart']),

    '_StepModel': frozenset(['setUniqueKey']),
    '_GridModel': frozenset(['setUniqueKey']),
    '_RasterModel': frozenset(['setUniqueKey']),
    '_SinglePointModel': frozenset(['setUniqueKey']),
    '_OneDEqualSpacingModel': frozenset(['setUniqueKey']),
    '_OneDStepModel': frozenset(['setUniqueKey']),

    '_BoundingBox': frozenset([]),
    '_BoundingLine': frozenset([]),

    '_MandelbrotModel': frozenset(['setColumns',
                                   'setEscapeRadius',
                                   'setMaxIterations',
                                   'setMaxX',
                                   'setMaxY',
                                   'setName',
                                   'setPoints',
                                   'setRows',
                                   'setxName',
                                   'setyName']),
}
# TODO: Make some fields optional (e.g. setxName)? setter_graylist?


def bean(java_class, params, setter_blacklist=setter_blacklist):
    """Instantiate a Javabean class from the global scope.

    Each of the bean's setters is called with the corresponding value from
    `params`, a dictionary. For instance, if the bean has a method called
    `setLength`, this function will call it with the value `params['length']`.
    If the key does not exist in `params`, this function will throw an
    exception. Blacklisted setters will not be called.
    """
    # The available model classes have been placed in our global scope by
    # Java. Choose one and instantiate it.
    bean_ = globals()[java_class]()

    # Call each non-blacklisted setter with the corresponding param value.
    setters = frozenset(filter(lambda x: x.startswith('set'), dir(bean_)))
    for setter in (setters - setter_blacklist[java_class]):
        getattr(bean_, setter)(params[setter[3].lower()+setter[4:]])

    return bean_


def model(type, params):  # TODO: Don't shadow `type` builtin.
    """Return an object conforming to IScanPathModel.
    """
    return bean('_'+type[0].upper()+type[1:]+'Model', params)


# TODO: block=False kwarg.
def scan(pm_roi_tuples, det_exp_tuples):
    """Submit a scan request to the parent Java process.
    """
    # We could generate the full ScanModel here, but we'd probably end up doing
    # too much work in Python.
    unzip = lambda l: zip(*l)
    points_models, roi_lists = unzip(pm_roi_tuples)
    detector_names, exposures = unzip(det_exp_tuples)

    # Care is required here. We must pass IROI arrays, not lists. Jython cannot
    # do this coercion for us, because of Java type erasure!
    roi_arrays = map(lambda l: _array(l, _IROI), roi_lists)

    # A ScanRequest expects ROIs to be specified as a map in the following
    # (bizarre?) format:
    roi_map = {model.getUniqueKey(): roi_array
               for (model, roi_array) in zip(points_models, roi_arrays)}

    detector_map = {n: _dmodel(n, exp)
                    for (n, exp) in zip(detector_names, exposures)}

    _output.put(bean('_ScanRequest', {'models': points_models,
                                      'regions': roi_map,
                                      'detectors': detector_map}))


## Points models ##

def step(scannable, start, stop, step):
    roi = None
    return model('step', {'name': scannable,
                          'start': start,
                          'stop': stop,
                          'step': step}), [roi]


def grid(axes=None, div=None, bbox=None, snake=False, roi=None):
    assert None not in (axes, div, bbox)
    (xName, yName) = axes
    (rows, cols) = div
    (xStart, yStart, width, height) = bbox
    # TODO: Should snake be True or False by default?
    # TODO: Allow multiple ROIs.
    return model('grid', {'xName': xName,
                          'yName': yName,
                          'rows': rows,
                          'columns': cols,
                          'snake': snake,
                          'boundingBox': b_box(xStart,
                                               yStart,
                                               width,
                                               height)}), [roi]


def array(scannable, positions):
    # We have to manually call ArrayModel.setPositions,
    # as it takes a (Double... positions) argument.
    # This is not ideal... TODO
    amodel = _ArrayModel()
    amodel.setName(scannable)
    amodel.setPositions(*positions)
    roi = None
    return amodel, [roi]


def raster(axes=None, inc=None, bbox=None, snake=False, roi=None):
    assert None not in (axes, inc, bbox)
    (xName, yName) = axes
    (xStep, yStep) = inc
    (xStart, yStart, width, height) = bbox
    return model('raster', {'xName': xName,
                            'yName': yName,
                            'xStep': xStep,
                            'yStep': yStep,
                            'snake': snake,
                            'boundingBox': b_box(xStart,
                                                 yStart,
                                                 width,
                                                 height)}), [roi]


def point(x, y):
    roi = None
    return model('singlePoint', {'x': x, 'y': y}), [roi]


def line(origin=None, length=None, angle=None, step=None, count=None):
    assert None not in (origin, length, angle)
    (xStart, yStart) = origin
    roi = None
    if step is not None:
        assert count is None
        return model('oneDStep', {'step': step,
                                  'boundingLine': b_line(xStart,
                                                         yStart,
                                                         length,
                                                         angle)}), [roi]
    else:
        assert count is not None
        return model(
            'oneDEqualSpacing', {'points': count,
                                 'boundingLine': b_line(xStart,
                                                        yStart,
                                                        length,
                                                        angle)}), [roi]


## Bounding shapes ##  TODO: Private use only?

def b_box(xStart, yStart, width, height):
    return bean('_BoundingBox', {'xStart': xStart,
                                 'yStart': yStart,
                                 'width': width,
                                 'height': height})


def b_line(xStart, yStart, length, angle):
    return bean('_BoundingLine', {'xStart': xStart,
                                  'yStart': yStart,
                                  'length': length,
                                  'angle': angle})


## ROIs ##

def circ(x, y, radius):
    return _CircularROI(radius, x, y)


def poly(*xy_tuples):
    return _PolygonalROI(_PolylineROI(*map(lambda (x, y): _PointROI(x, y),
                                           xy_tuples)))


def rect(x, y, w, h, angle):
    return _RectangularROI(x, y, w, h, angle)


## Detectors ##

def _dmodel(name, exposure, *args, **kwargs):
    # This should take a string, maybe other params, and return a dmodel.
    return bean('_MandelbrotModel', {'exposure': exposure})
