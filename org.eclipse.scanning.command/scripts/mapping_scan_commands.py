"""A friendly interface to mapping scans.

The most important function here is mscan(), which collates information into a
ScanRequest and submits it to a queue for attention of the GDA server.

Users who want to create a ScanRequest without submitting it to GDA can use the
pure function scan_request(), whose signature is similar to mscan(). The scan
request object can later be submitted to the GDA server with submit().

The following pure functions create scan paths which may be passed to mscan():
grid(), step(), line(), array(), point(), val().

There are also some pure functions which may be used to narrow the region of
interest (ROI) when using grid(). They are: circ(), rect(), poly().
"""

# To use these commands in the GDA Jython REPL:
#
# - To server/main/_common/jython_server_facade.xml in your <beamline>-config,
#   add a gda.jython.ScriptProject with path:
#   ${gda.install.git.loc}/daq-eclipse.git/org.eclipse.scanning.command/scripts
#
# - To localStation.py in your <beamline>-config, add the following line:
#   from mapping_scan_commands import *
#
# - For each detector, write a function which fetches the currently active
#   detector model, updates it with any given arguments, and returns it as the
#   second element of a tuple whose first element is the detector name. E.g:
#
#   def mandelbrot(exposure_time):
#       """Create mandelbrot detector settings to be passed to mscan().
#       """
#       model = _fetch_model_for_detector('mandelbrot_detector')
#       model.setExposureTime(exposure_time)
#       return ('mandelbrot_detector', model)
#
#   (_fetch_model_for_detector can be imported from the present module.)
#
#   Import this detector function into localStation.py. Then your users can do
#   >>> mscan(step(x, 0, 10, 1), det=mandelbrot(0.1))
#
#   mscan() will send your updated detector model as part of the ScanRequest.

from java.net import URI
from org.eclipse.dawnsci.analysis.dataset.roi import (
    CircularROI, RectangularROI, PolygonalROI, PolylineROI, PointROI)
from org.eclipse.scanning.api.points.models import (
    StepModel, GridModel, RasterModel, SinglePointModel,
    OneDEqualSpacingModel, OneDStepModel, ArrayModel,
    BoundingBox, BoundingLine)
from org.eclipse.scanning.server.servlet.Services import (
    getEventService, getScanService)
from org.eclipse.scanning.api.event.scan import (ScanBean, ScanRequest)
from org.eclipse.scanning.api.event.IEventService import SUBMISSION_QUEUE


# Grepping for 'mscan' in a GDA workspace shows up nothing, so it seems that
# mscan is a free name.
def mscan(path=None, mon=None, det=None, now=False, block=False,
          broker_uri="tcp://localhost:61616"):
    """Create a ScanRequest and submit it to the GDA server.

    A simple usage of this function is as follows:
    >>> mscan(step(my_scannable, 0, 10, 1), det=mandelbrot(0.1))

    The above invokation says "please perform a mapping scan over my scannable
    from 0 to 10 with step size 1, collecting data from the 'Mandelbrot'
    detector with an exposure time of 0.1 seconds at each step".

    You can specify multiple detectors with a list (square brackets):
    >>> mscan(..., det=[mandelbrot(0.1), another_detector(0.4)])

    You can specify a scannable or list of scannables to monitor:
    >>> mscan(..., mon=my_scannable, ...)  # or:
    >>> mscan(..., mon=[my_scannable, another_scannable], ...)

    You can embed one scan path inside another to create a compound scan path:
    >>> mscan([step(s, 0, 10, 1), step(f, 1, 5, 1)], ...)

    The above invokation says "for each point from 0 to 10 on my slow axis, do
    a scan from 1 to 5 on my fast axis". In fact, for the above case, a grid-
    type scan would be more idiomatic:
    >>> mscan(grid(axes=(f, s), step=(1, 1), origin=(0, 0), size=(10, 4)), ...)

    By default, this function will submit the scan request to a queue and
    return immediately. You may override this behaviour with the "now" and
    "block" keywords:
    >>> # Don't return until the scan is complete.
    >>> mscan(..., ..., block=True)

    >>> # Skip the queue and run the scan now (but don't wait for completion).
    >>> mscan(..., ..., now=True)

    >>> # Skip the queue and return once the scan is complete.
    >>> mscan(..., ..., now=True, block=True)
    """
    submit(scan_request(path, mon, det), now, block, broker_uri)


def submit(request, now=False, block=False,
           broker_uri="tcp://localhost:61616"):
    """Submit an existing ScanRequest to the GDA server.

    See the mscan() docstring for details of `now` and `block`.
    """
    if now or block:
        raise NotImplementedError()  # TODO
    else:
        getEventService() \
            .createSubmitter(URI(broker_uri), SUBMISSION_QUEUE) \
            .submit(_instantiate(ScanBean, {'scanRequest': request}))


def scan_request(path=None, mon=None, det=None):
    """Create a ScanRequest object with the given configuration.

    See the mscan() docstring for usage.
    """
    try:
        assert path is not None
    except AssertionError:
        raise ValueError('Scan request must have a scan path.')

    # The effect of the following three lines is to make square brackets
    # optional when calling this function with length-1 lists. I.e. we can do
    # either scan([grid(...)], ...) or scan(grid(...), ...). Also _stringify
    # the monitors so users can pass either a monitor name in quotes or a
    # scannable object from the Jython namespace.
    scan_paths = _listify(path)
    monitors = map(_stringify, _listify(mon))
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

    return _instantiate(ScanRequest, {'models': scan_path_models,
                                      'regions': roi_map,
                                      'monitorNames': monitors,
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
    try:
        assert None not in (axis, start, stop, step)
    except (TypeError, ValueError):
        raise ValueError(
            '`axis`, `start`, `stop` and `step` must be provided.')

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
    >>> grid(..., roi=[circ((0, 1), 1.5), rect((-1, -1), (0, 0), 0)])
    """
    try:
        assert None not in (axes, origin, size)
    except AssertionError:
        raise ValueError(
            '`axes`, `origin` and `size` must be provided to grid().')

    try:
        assert len(filter(lambda arg: arg is None, (count, step))) == 1
    except AssertionError:
        raise ValueError(
            'Either `count` or `step` must be provided to to grid().')

    try:
        (xName, yName) = map(_stringify, axes)
    except (TypeError, ValueError):
        raise ValueError('`axes` must be a pair of scannables (x, y).')

    try:
        (xStart, yStart) = origin
    except (TypeError, ValueError):
        raise ValueError('`origin` must be a pair of values (x0, y0).')

    try:
        (width, height) = size
    except (TypeError, ValueError):
        raise ValueError('`size` must be a pair of values (w, h).')

    bbox = _instantiate(BoundingBox,
                        {'fastAxisStart': xStart,
                         'slowAxisStart': yStart,
                         'fastAxisLength': width,
                         'slowAxisLength': height})

    if count is not None:
        try:
            (rows, cols) = count
        except (TypeError, ValueError):
            raise ValueError('`count` must be a pair of integers (r, c).')

        model = _instantiate(
                    GridModel,
                    {'fastAxisName': xName,
                     'slowAxisName': yName,
                     'fastAxisPoints': rows,
                     'slowAxisPoints': cols,
                     'snake': snake,
                     'boundingBox': bbox})

    else:
        try:
            (xStep, yStep) = step
        except (TypeError, ValueError):
            raise ValueError('`step` must be a pair of numbers (dx, dy).')

        model = _instantiate(
                    RasterModel,
                    {'fastAxisName': xName,
                     'slowAxisName': yName,
                     'fastAxisStep': xStep,
                     'slowAxisStep': yStep,
                     'snake': snake,
                     'boundingBox': bbox})

    # We _listify() the ROI inputs, so users can type either
    # roi=circ(x, y, r) or roi=[circ(x, y, r), rect(x, y, w, h, angle)].
    return model, _listify(roi)


# TODO: Add axes=None?
def line(origin=None, length=None, angle=None, count=None, step=None):
    """Define a line segment scan path to be passed to mscan().

    Required keyword arguments:
    * origin: origin of line segment (x0, y0)
    * length: length of line segment
    * angle: angle of line segment, CCW from vec(1, 0), specified in radians
    - One of:
      * count: a number of points, equally spaced, along the line segment
      * step: a distance between points along the line segment
    """
    try:
        assert None not in (origin, length, angle)
    except (TypeError, ValueError):
        raise ValueError(
            '`origin`, `length` and `angle` must be provided to line().')
    try:
        assert len(filter(lambda arg: arg is None, (count, step))) == 1
    except AssertionError:
        raise ValueError(
            'Either `count` or `step` must be provided to line().')

    try:
        (xStart, yStart) = origin
    except (TypeError, ValueError):
        raise ValueError('`origin` must be a pair of values (x0, y0).')

    roi = None

    bline = _instantiate(BoundingLine,
                         {'xStart': xStart,
                          'yStart': yStart,
                          'length': length,
                          'angle': angle})

    if step is not None:
        model = _instantiate(
                    OneDStepModel,
                    {'step': step,
                     'boundingLine': _instantiate(BoundingLine,
                                                  {'xStart': xStart,
                                                   'yStart': yStart,
                                                   'length': length,
                                                   'angle': angle})})

    else:
        model = _instantiate(
                    OneDEqualSpacingModel,
                    {'points': count,
                     'boundingLine': _instantiate(BoundingLine,
                                                  {'xStart': xStart,
                                                   'yStart': yStart,
                                                   'length': length,
                                                   'angle': angle})})

    return model, _listify(roi)


def array(axis=None, values=None):
    """Define an array scan path to be passed to mscan().

    Required keyword arguments:
    * axis: a scannable
    * values: a list of numerical values for the scannable to take
    """
    try:
        assert None not in (axis, values)
    except AssertionError:
        raise ValueError('`axis` and `values` must be provided to array().')

    axis = _stringify(axis)

    roi = None

    # We have to manually call ArrayModel.setPositions,
    # as it takes a (Double... positions) argument.
    amodel = ArrayModel()
    amodel.setName(axis)
    amodel.setPositions(*values)

    return amodel, _listify(roi)


def val(axis=None, value=None):
    """Define a single axis position to be passed to mscan().

    This single-point scan "path" can be used as the innermost scan path in a
    compound scan to ensure a particular scannable is always set to a given
    value when exposures are taken. (A.k.a. "move to keep still".)

    For instance:
    >>> # Step x from 0 to 10, moving y to 5 after each x movement.
    >>> mscan([step(x, 0, 10, 1), val(y, 5)], ...)
    """
    try:
        assert None not in (axis, value)
    except AssertionError:
        raise ValueError('`axis` and `value` must be provided to val().')

    return array(axis, [value])


def point(x, y):
    """Define a point scan path to be passed to mscan().
    """
    roi = None
    return _instantiate(SinglePointModel, {'x': x, 'y': y}), _listify(roi)


# ROIs
# ----

def circ(origin=None, radius=None):
    """Define a circular region of interest (ROI) to be passed to grid().

    For instance:
    >>> circ(origin=(0, 1), radius=3)
    """
    try:
        assert None not in (origin, radius)
    except AssertionError:
        raise ValueError(
            '`origin` and `radius` must be provided to circ().')

    try:
        (x, y) = origin
    except (TypeError, ValueError):
        raise ValueError('`origin` must be a pair of values (x0, y0).')

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

    Angles are specified in radians here. If no angle is passed, the angle is
    taken as 0.
    """
    try:
        assert None not in (origin, size)
    except AssertionError:
        raise ValueError(
            '`origin` and `size` must be provided to rect().')

    try:
        (xStart, yStart) = origin
    except (TypeError, ValueError):
        raise ValueError('`origin` must be a pair of values (x0, y0).')

    try:
        (width, height) = size
    except (TypeError, ValueError):
        raise ValueError('`size` must be a pair of values (w, h).')

    return RectangularROI(xStart, yStart, width, height, angle)


# Bean construction
# -----------------

def _instantiate(Bean, params):
    """Instantiate a JavaBean class with the given params.

    `params` is a dictionary containing attributes to call bean setters with.
    For instance, if params contains {'length': 10}, the instantiated bean will
    have its setLength method called with the value 10. If no such method
    exists, a ValueError is thrown.
    """
    bean = Bean()
    setters = filter(lambda x: x.startswith('set'), dir(bean))

    # For each param, call one of the setters.
    for p in params.keys():
        try:
            [setter] = filter(lambda s: p == s[3].lower()+s[4:], setters)
            getattr(bean, setter)(params[p])
        except ValueError:
            raise ValueError(
                "No setter for param '"+p+"' in "+Bean.__name__+".")

    return bean


def _fetch_model_for_detector(detector_name):

    detector = getScanService().getRunnableDevice(detector_name)

    try:
        assert detector is not None
    except AssertionError:
        raise ValueError("Detector '"+detector_name+"' not found.")

    return detector.getDeviceInformation().getModel()


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
