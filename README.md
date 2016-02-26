# [daq-eclipse](http://diamondlightsource.github.io/daq-eclipse)  


[![Build Status](https://api.travis-ci.org/DiamondLightSource/daq-eclipse.png)](https://travis-ci.org/DiamondLightSource/daq-eclipse)


The purpose of this repository is to hold open source code before it is submitted into an Eclipse Project.

This repository is checked by a Travis CI build which looks at the last commit message.

It is compulsory to reference a jira commit or merge from a commit that does or the build will fail.

# Adding Tests
Instead of having to name your class *Test as in other projects, in this project you should add a Suite to each package of tests. This defines the tests that are run automatically in the build. This procedure allows for long running or CPU/threading tests to be ommited from a build to keep the run time down while waiting for the merge.

Example: [org.eclipse.scanning.test.points.Suite](https://github.com/DiamondLightSource/daq-eclipse/blob/master/org.eclipse.scanning.test/src/org/eclipse/scanning/test/points/Suite.java)
