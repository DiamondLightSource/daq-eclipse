# coding=utf-8
# (utf-8 mostly just so we can put ellipses in docstrings.)

"""A friendly interface to mapping scans.

The most important function here is mscan(), which collates information into a
ScanRequest and submits it to a queue for attention of the GDA server. This is
the only function with side effects in this module.

Users who want to create a ScanRequest without submitting it to GDA can use the
pure function scan_request(), whose signature is similar to mscan().

The following pure functions create scan paths which may be passed to mscan():
grid(), step(), line(), array(), point().

There are also some pure functions which may be used to narrow the region of
interest (ROI) when using grid(). They are: circ(), rect(), poly().
"""

from org.eclipse.scanning.api.event.scan import ScanRequest
from org.eclipse.dawnsci.analysis.api.roi import IROI
from org.eclipse.dawnsci.analysis.dataset.roi import (
    CircularROI, RectangularROI, PolygonalROI, PolylineROI, PointROI)
from org.eclipse.scanning.api.points.models import (
    StepModel, GridModel, RasterModel, SinglePointModel,
    OneDEqualSpacingModel, OneDStepModel, ArrayModel,
    BoundingBox, BoundingLine)
from org.eclipse.scanning.server.servlet import Services
from org.eclipse.scanning.api.event.scan import ScanBean


# Grepping for 'mscan' in a GDA workspace shows up nothing, so it seems that
# mscan is a free name.
def mscan(path=None, det=None, now=False, block=False):
    """Create a ScanRequest and submit it to the GDA server.

    A simple usage of this function is as follows:
    >>> mscan(step(my_scannable, 0, 10, 1), det=mandelbrot(0.1))

    The above invokation says "please perform a mapping scan over my scannable
    from 0 to 10 with step size 1, collecting data from the 'Mandelbrot'
    detector with an exposure of 0.1 at each step".

    You can specify multiple detectors with a list (square brackets):
    >>> mscan(…, det=[mandelbrot(0.1), another_detector(0.4)])

    You can embed one scan path inside another to create a compound scan path:
    >>> mscan([step(s, 0, 10, 1), step(f, 1, 5, 1)], …)

    The above invokation says "for each point from 0 to 10 on my slow axis, do
    a scan from 1 to 5 on my fast axis". In fact, for the above case, a grid-
    type scan would be more idiomatic:
    >>> mscan(grid(axes=(f, s), step=(1, 1), origin=(0, 0), size=(10, 4)), …)

    By default, this function will submit the scan request to a queue and
    return immediately. You may override this behaviour with the "now" and
    "block" keywords:
    >>> # Don't return until the scan is complete.
    >>> mscan(…, …, block=True)

    >>> # Skip the queue and run the scan now (but don't wait for completion).
    >>> mscan(…, …, now=True)

    >>> # Skip the queue and return once the scan is complete.
    >>> mscan(…, …, now=True, block=True)
    """
    submit(scan_request(path, det), now, block)


def submit(request, now=False, block=False):
    """Submit an existing ScanRequest to the GDA server.

    See the mscan() docstring for details of `now` and `block`.
    """
    if now or block:
        raise NotImplementedError()  # TODO
    else:
        services.getEventService() \
                .createSubmitter("vm://localhost?persistent=false",
                                 "commandTestQueue") \
                .submit(_instantiate(ScanBean, {'scanRequest': request}))


def scan_request(path=None, det=None):
    """Create a ScanRequest object with the given configuration.

    See the mscan() docstring for usage.
    """
    assert path is not None
    if det is None: det = []

    # The effect of the following two lines is to make square brackets optional
    # when calling this function with length-1 lists. I.e. we can do either
    # scan([grid(…)], …) or scan(grid(…), …).
    scan_paths = _listify(path)
    detectors = _listify(det)

    (scan_path_models, _) = zip(*scan_paths)  # zip(* == unzip(

    # ScanRequest expects ROIs to be specified as a map in the following
    # (bizarre?) format:
    roi_map = {}  # No dict comprehensions in Python 2.5!
    for (model, rois) in scan_paths:
        if len(rois) > 0:
            roi_map[model.getUniqueKey()] = rois
    # Nicer Python 2.7 version for if the Jython interpreter is ever upgraded:
    # roi_map = {model.getUniqueKey(): rois
    #            for (model, rois) in scan_paths if len(rois) > 0}

    detector_map = dict(detectors)
    # Equivalent to (Python 2.7) {name: model for (name, model) in detectors}.

    # TODO: Implement monitors.

    return _instantiate(ScanRequest, {'models': scan_path_models,
                                      'regions': roi_map,
                                      'detectors': detector_map})



# Scan paths
# ----------

def step(axis=None, start=None, stop=None, step=None):
    """Define a step scan path to be passed to mscan().

    Note that this function may be called with or without keyword syntax. That
    is, the following are mutually equivalent:
    >>> step(axis=my_scannable, start=0, stop=10, step=1)
    >>> step(my_scannable, 0, 10, 1)
    """
    assert None not in (axis, start, stop, step)

    # For the first argument, users can pass either a Scannable object
    # or a string. IScanPathModels are only interested in the string (i.e.
    # the Scannable's name).
    axis = _stringify(axis)

    # No such thing as ROIs for StepModels.
    roi = None

    model = _instantiate(
                StepModel,
                {'name': axis,
                 'start': start,
                 'stop': stop,
                 'step': step})

    return model, _listify(roi)


def grid(axes=None, origin=None, size=None, count=None, step=None, snake=True,
         roi=None):
    """Define a grid scan path to be passed to mscan().

    Required keyword arguments:
    * axes: a pair of scannables (x, y) with the former taken as the fast axis
    * origin: a pair of numbers (x0, y0) specifying a grid corner
    * size: a pair of numbers (w, h) specifying the absolute grid dimensions
    - One of:
      * count: a pair of integers (r, c) specifying number of grid divisions
      * step: a pair of numbers (dx, dy) specifying spacing of grid divisions

    Optional keyword arguments:
    * snake: point order should "snake" through the grid? default True
    * roi: use only grid points from given region(s) of interest. default None

    If no ROIs are given, all grid points are used.

    If multiple regions of interest (ROI) are given, the composite ROI is taken
    as the union of the individual ROIs. E.g.:
    >>> grid(…, …, …, …, …, roi=[circ((0, 1), 1.5), rect((-1, -1), (0, 0), 0])
    """
    assert None not in (axes, origin, size)

    # Assert that exactly one of count, step is None.
    assert len(filter(lambda arg: arg is None, (count, step))) == 1

    (xName, yName) = map(_stringify, axes)
    (xStart, yStart) = origin
    (width, height) = size

    if count is not None:
        (rows, cols) = count

        model = _instantiate(
                    GridModel,
                    {'fastAxisName': xName,
                     'slowAxisName': yName,
                     'fastAxisPoints': rows,
                     'slowAxisPoints': cols,
                     'snake': snake,
                     'boundingBox': _bbox(xStart, yStart, width, height)})

    else:
        (xStep, yStep) = step

        model = _instantiate(
                    RasterModel,
                    {'fastAxisName': xName,
                     'slowAxisName': yName,
                     'fastAxisStep': xStep,
                     'slowAxisStep': yStep,
                     'snake': snake,
                     'boundingBox': _bbox(xStart, yStart, width, height)})

    # We _listify() the ROI inputs, so users can type either
    # roi=circ(x, y, r) or roi=[circ(x, y, r), rect(x, y, w, h, angle)].
    return model, _listify(roi)


# TODO: Add axes=None?
def line(origin=None, length=None, angle=None, count=None, step=None):
    """Define a line segment scan path to be passed to mscan().

    Required keyword arguments:
    * origin: origin of line segment
    * length: length of line segment
    * angle: angle of line segment, CCW from vec(1, 0), specified in radians
    - One of:
      * count: a number of points, equally spaced, along the line segment
      * step: a distance between points along the line segment
    """
    assert None not in (origin, length, angle)
    assert len(filter(lambda arg: arg is None, (count, step))) == 1

    (xStart, yStart) = origin
    roi = None

    if step is not None:
        model = _instantiate(
                    OneDStepModel,
                    {'step': step,
                     'boundingLine': _bline(xStart, yStart, length, angle)})

    else:
        model = _instantiate(
                    OneDEqualSpacingModel,
                    {'points': count,
                     'boundingLine': _bline(xStart, yStart, length, angle)})

    return model, _listify(roi)


# TODO: Rename "array" to "values" or "positions"?
def array(axis=None, positions=None):
    """Define an array scan path to be passed to mscan().

    Required keyword arguments:
    * axis: a scannable
    * positions: a list of numerical positions for the scannable to take
    """
    # We have to manually call ArrayModel.setPositions,
    # as it takes a (Double... positions) argument.
    # This is not ideal... TODO

    axis = _stringify(axis)

    roi = None

    amodel = ArrayModel()
    amodel.setName(axis)
    amodel.setPositions(*positions)

    return amodel, _listify(roi)


def point(x, y):
    """Define a point scan path to be passed to mscan().
    """
    roi = None
    return _instantiate(SinglePointModel, {'x': x, 'y': y}), _listify(roi)


# ROIs
# ----

def circ(origin=None, radius=None):
    """Define a circular region of interest (ROI) to be passed to grid().
    """
    assert None not in (origin, radius)

    (x, y) = origin

    return CircularROI(radius, x, y)


def poly(*vertices):
    """Define a polygonal region of interest (ROI) to be passed to grid().

    For instance, a triangle:
    >>> poly((5, 5), (5, 10), (10, 5))

    A square:
    >>> poly((0, 0), (0, 3), (3, 3), (3, 0))
    """
    point_rois = map(lambda (x, y): PointROI(x, y), vertices)

    # PolygonalROI closes the polygon for us.
    return PolygonalROI(PolylineROI(*point_rois))


def rect(origin=None, size=None, angle=0):
    """Define a rectangular region of interest (ROI) to be passed to grid().

    For instance:
    >>> rect((1, 2), (5, 4), 0.1)

    Angles are specified in radians here.
    """
    assert None not in (origin, size, angle)

    (xStart, yStart) = origin
    (width, height) = size

    return RectangularROI(xStart, yStart, width, height, angle)


# Bean construction
# -----------------

# There are some setter fields which the end user does not set:
_setter_blacklist = {
    'ScanRequest': frozenset(['setAfter',
                              'setAfterResponse',
                              'setBefore',
                              'setBeforeResponse',
                              'setEnd',
                              'setFilePath',
                              'setIgnorePreprocess',
                              'setMonitorNames',
                              'setStart']),

    'ScanBean': frozenset(['setFilePath',
                           'setScanNumber',
                           'setDataSetPath',
                           'setBeamline',
                           'setDeviceState',
                           'setMessage',
                           'setPreviousDeviceState',
                           'setPoint',
                           'setSize',
                           'setPosition',
                           'setDeviceName',
                           'setShape',
                           'setShapeEstimationRequired']),

    'StepModel': frozenset(['setUniqueKey']),
    'GridModel': frozenset(['setUniqueKey']),
    'RasterModel': frozenset(['setUniqueKey']),
    'SinglePointModel': frozenset(['setUniqueKey']),
    'OneDEqualSpacingModel': frozenset(['setUniqueKey']),
    'OneDStepModel': frozenset(['setUniqueKey']),

    'BoundingBox': frozenset([]),
    'BoundingLine': frozenset([]),

    'MandelbrotModel': frozenset(['setColumns',
                                  'setEscapeRadius',
                                  'setMaxIterations',
                                  'setMaxRealCoordinate',
                                  'setMaxImaginaryCoordinate',
                                  'setName',
                                  'setPoints',
                                  'setRows',
                                  'setRealAxisName',
                                  'setImaginaryAxisName']),
}
# TODO: Make some fields optional (e.g. setxName)? setter_graylist?


def _instantiate(Bean, params, setter_blacklist=_setter_blacklist):
    """Instantiate a JavaBean class with the given params.

    Each of the bean's setters is called with the corresponding value from
    `params`, a dictionary. For instance, if the bean has a method called
    `setLength`, this function will call it with the value `params['length']`.
    If the key does not exist in `params`, this function will throw an
    exception. Blacklisted setters will not be called.

    This is just a constructor for beans which guarantees all the necessary
    setters are called.
    """
    bean = Bean()

    # Call each non-blacklisted setter with the corresponding param value.
    setters = frozenset(filter(lambda x: x.startswith('set'), dir(bean)))
    for setter in (setters - setter_blacklist[Bean.__name__]):
        getattr(bean, setter)(params[setter[3].lower()+setter[4:]])

    return bean


# Bounding shapes
# ---------------

def _bbox(xStart, yStart, width, height):
    return _instantiate(BoundingBox, {'fastAxisStart': xStart,
                                      'slowAxisStart': yStart,
                                      'fastAxisLength': width,
                                      'slowAxisLength': height})


def _bline(xStart, yStart, length, angle):
    return _instantiate(BoundingLine, {'xStart': xStart,
                                       'yStart': yStart,
                                       'length': length,
                                       'angle': angle})


# Miscellaneous functions
# -----------------------

def _listify(sheep):  # Idempotent.
    # The argument is called "sheep" because it may be either
    # plural ("[a list of] sheep") or singular ("[a] sheep").
    if type(sheep) is list:
        return sheep
    elif sheep is None:
        return []
    else:
        return [sheep]


def _stringify(scannable):
    if isinstance(scannable, basestring):
        return scannable
    else:
        try:
            return scannable.getName()
        except AttributeError:
            raise ValueError(
                str(scannable)+' has no getName() method and is not a string.')
            # TODO: Test for this exception.
