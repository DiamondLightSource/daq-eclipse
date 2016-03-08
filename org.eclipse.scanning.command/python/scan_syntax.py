# There are some setter fields which the end user does not set:
setter_blacklists = {
    # (Jython appears not to have set literals...)
    'grid': set(['setUniqueKey']),
    'step': set(['setUniqueKey']),
    # TODO: Make some of these optional (e.g. setxName).
}


def create_model(type, params):
    # We only use this for points models, but in theory it could be used for
    # any Javabean.

    # The available model classes have been placed in our global scope by
    # Java. Choose one and instantiate it.
    model = globals()['_'+type.capitalize()+'Model']()

    # Call each non-blacklisted setter with the corresponding param value.
    setters = set(filter(lambda x: x.startswith('set'), dir(model)))
    required_setters = setters - setter_blacklists[type]
    for setter in required_setters:
        getattr(model, setter)(params[setter[3].lower()+setter[4:]])

    return model


def compound(*points_models):
    # This is just list concatenation.
    return reduce(lambda a, b: a + b, points_models)


def scan(points_models, detector, exposure):
    # We could generate the full ScanModel here, but we'd probably end up
    # doing too much work in Python.
    _output.put(_InterpreterResult(_ArrayList(points_models), detector, exposure))
    # TODO: Dynamically enumerate detectors to avoid quote marks?


# It is now trivial to define convenience functions here. For instance:
def step(scannable, start, stop, step):
    return [create_model('step', {'name': scannable,
                                  'start': start,
                                  'stop': stop,
                                  'step': step})]


def grid(axes=None, div=None, bbox=None, snake=False):
    (xName, yName) = axes
    (rows, cols) = div
    # TODO: Should snake be True or False by default?
    if bbox is None:
        print "In grid() you must use a BoundingBox like so: bbox=(0, 0, 10, 10)!"
        return
    return [create_model('grid', {'xName': xName,
                                  'yName': yName,
                                  'rows': rows,
                                  'columns': cols,
                                  'snake': snake,
                                  'boundingBox': _BoundingBox(*bbox)})]
