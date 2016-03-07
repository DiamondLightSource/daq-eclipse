# There are some setter fields which the end user does not set:
setter_blacklists = {
    # (Jython appears not to have set literals...)
    'grid': set(['setUniqueKey', 'setxName', 'setyName']),
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


def scan(points_model, detector, exposure):
    # We could generate the full ScanModel here, but we'd probably end up
    # doing too much work in Python.
    global _detector;  _detector = detector
    global _exposure;  _exposure = exposure
    _output.put(points_model)
    # FIXME: Put everything in the queue.
    # TODO: Dynamically enumerate detectors to avoid quote marks?


# It is now trivial to define convenience functions here. For instance:
def step(start, stop, step):
    return create_model('step', {'start': start,
                                 'stop': stop,
                                 'step': step})


def grid(rows, cols, bbox=(0, 0, 0, 0), snake=False):
    return create_model('grid', {'rows': rows,
                                 'columns': cols,
                                 'snake': snake,
                                 'boundingBox': _BoundingBox(*bbox)})
