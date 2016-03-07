# There are some setter fields which the end user does not set:
setter_blacklists = {
    # (Jython appears not to have set literals...)
    'grid': set(['setUniqueKey', 'setxName', 'setyName']),
    'step': set(['setUniqueKey']),
    # TODO: Make some of these optional (e.g. setxName).
}

def scan(pmodel_type, pmodel_params):
    # The available points model classes have been placed in our global
    # scope by Java. Choose one and instantiate it.
    pmodel = globals()['_'+pmodel_type.capitalize()+'Model']()

    # For each non-blacklisted pmodel setter field, call the setter with the
    # corresponding value from pmodel_params.
    setters = set(filter(lambda x: x.startswith('set'), dir(pmodel)))
    required_setters = setters - setter_blacklists[pmodel_type]
    for setter in required_setters:
        getattr(pmodel, setter)(pmodel_params[setter[3].lower()+setter[4:]])
        # Note that we are forced to call all the setters.

    # Rather than explicitly returning pmodel, set a global variable:
    global _pmodel
    _pmodel = pmodel


# It is now trivial to define convenience functions here. For instance:
def step(start, stop, step):
    return scan('step', {'start': start,
                         'stop': stop,
                         'step': step})


def grid(rows, cols, bbox=(0, 0, 0, 0), snake=False):
    return scan('grid', {'rows': rows,
                         'columns': cols,
                         'snake': snake,
                         'boundingBox': _BoundingBox(*bbox)})
