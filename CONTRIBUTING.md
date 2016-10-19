# Contributing to daq-eclipse

## Basic Ethos
We try to make the daq-eclipse project the heart of GDA9 at Diamond Light Source. Unlike previous versions of GDA we would like to make this system entirely open and very easy to reuse. To this end there are .product files in the repository to make running a dummy acquisition server and a dummy acquisition client very easy. There are also .target files to define the external repositories which provide the required libraries to run the project.

If you would like to contribute you should provide tests for everything that you add to the system. Do not be surprised if your review asks for more tests to be created. The committers to the project are looking for tests which check for success and attempt to break your system. Your tests should also run fast, not create large files and must be junit tests included in a Suite called 'Suite.java' in order for them to be run in travis.

## Route 1 - Become a Committer
* 1 Ask to be a member of the project, you can send a message to the project leader to do this.
* 2 When the project is in eclipse, vote will be required (through the eclipse foundation web pages). Currently a conversation with Matthew Webber and Matthew Gerring is required.
* 3 You then will be able to push your local changes to a branch on github
* 4 When ready create a pull request from your branch, the travis tests will be run automatically for your pull request
* 5 You may update your branch by pushing to it and the tests will be automatically rerun.
* 6 You should then invite other committers to review the change on github and respond to their comments.
* 7 Merge up to your branch the items to address their feedback, the tests will be automatically rerun.

## Reoute 2 - Non-Committer Pull Request
* Fork the repository on github and push your changes there.
* Create a pull request from your fork to daq-eclipse
* Follow steps 4-7 above from here.
* A committer will merge your request once the test passes and the review items are addressed.

