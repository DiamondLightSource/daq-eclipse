# There are some setter fields which the end user does not set:
setter_blacklist = set(['setUniqueKey'])
# TODO: Make some of fields optional (e.g. setxName)?


def model(type, params):
    # Degenerate case of compound model -> one list element.
    return [bean('_'+type[0].upper()+type[1:]+'Model', params)]


def bean(java_class, params):
    # The available model classes have been placed in our global scope by
    # Java. Choose one and instantiate it.
    bean_ = globals()[java_class]()

    # Call each non-blacklisted setter with the corresponding param value.
    setters = set(filter(lambda x: x.startswith('set'), dir(bean_)))
    required_setters = setters - setter_blacklist
    for setter in required_setters:
        getattr(bean_, setter)(params[setter[3].lower()+setter[4:]])

    return bean_


def compound(*points_models):
    # This is just list concatenation.
    return reduce(lambda a, b: a + b, points_models)


def scan(points_models, detector, exposure):
    # We could generate the full ScanModel here, but we'd probably end up
    # doing too much work in Python.
    _output.put(
        _InterpreterResult(_ArrayList(points_models), detector, exposure))
    # TODO: Dynamically enumerate detectors to avoid quote marks?


# It is now trivial to define convenience functions here. For instance:
def step(scannable, start, stop, step):
    return model('step', {'name': scannable,
                          'start': start,
                          'stop': stop,
                          'step': step})


def grid(axes=None, div=None, bbox=None, snake=False):
    assert None not in (axes, div, bbox)
    (xName, yName) = axes
    (rows, cols) = div
    (xStart, yStart, width, height) = bbox
    # TODO: Should snake be True or False by default?
    return model('grid', {'xName': xName,
                          'yName': yName,
                          'rows': rows,
                          'columns': cols,
                          'snake': snake,
                          'boundingBox': b_box(xStart,
                                               yStart,
                                               width,
                                               height)})


def array(scannable, positions):
    # We have to manually call ArrayModel.setPositions,
    # as it takes a (Double... positions) argument.
    amodel = _ArrayModel()
    amodel.setName(scannable)
    amodel.setPositions(*positions)
    return [amodel]


def raster(axes=None, inc=None, bbox=None, snake=False):
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
                                                 height)})


def point(x, y):
    return model('singlePoint', {'x': x, 'y': y})


def line(origin=None, length=None, angle=None, step=None, count=None):
    assert None not in (origin, length, angle)
    (xStart, yStart) = origin
    if step is not None:
        assert count is None
        return model('oneDStep', {'step': step,
                                  'boundingLine': b_line(xStart,
                                                         yStart,
                                                         length,
                                                         angle)})
    else:
        assert count is not None
        return model('oneDEqualSpacing', {'points': count,
                                          'boundingLine': b_line(xStart,
                                                                 yStart,
                                                                 length,
                                                                 angle)})


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
