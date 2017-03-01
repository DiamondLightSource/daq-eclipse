###
# Copyright (c) 2016 Diamond Light Source Ltd.
#
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
#
# Contributors:
#    Gary Yendell - initial API and implementation and/or initial documentation
#    Charles Mita - initial API and implementation and/or initial documentation
# 
###

class Mutator(object):
    """Abstract class to apply a mutation to the points of an ND
     ScanPointGenerator"""

    # Lookup table for mutator subclasses
    _mutator_lookup = {}

    def mutate(self, iterator):
        """
        Abstract method to take each point from the given iterator, apply a
        mutation and then yield the new point

        Args:
            iterator(iter): Iterator to mutate

        Yields:
            Point: Mutated points from generator
        """
        raise NotImplementedError

    def to_dict(self):
        """Abstract method to convert object attributes into a dictionary"""
        raise NotImplementedError

    @classmethod
    def from_dict(cls, d):
        """
        Abstract method to create a Mutator instance from a serialised
        dictionary

        Args:
            d(dict): Dictionary of attributes

        Returns:
            Mutator: New Mutator instance
        """

        mutator_type = d["typeid"]
        generator = cls._mutator_lookup[mutator_type]
        assert generator is not cls, \
            "Subclass %s did not redefine from_dict" % mutator_type
        mutator = generator.from_dict(d)

        return mutator

    @classmethod
    def register_subclass(cls, mutator_type):
        """
        Register a subclass so from_dict() works

        Args:
            mutator_type(Mutator): Subclass to register
        """

        def decorator(mutator):

            cls._mutator_lookup[mutator_type] = mutator
            mutator.typeid = mutator_type

            return mutator
        return decorator
