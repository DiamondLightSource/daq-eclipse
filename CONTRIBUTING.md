# Contributing to daq-eclipse

## Basic Ethos
We try to make the daq-eclipse project the heart of GDA9 at Diamond Light Source. Unlike previous versions of GDA we would like to make this system entirely open and very easy to reuse. To this end there are .product files in the repository to make running a dummy acquisition server and a dummy acquisition client very easy. There are also .target files to define the external repositories which provide the required libraries to run the project.

If you would like to contribute, you should provide tests for everything that you add to the system. Do not be surprised if your review asks for more tests to be created. The committers to the project are looking for tests which check for success and attempt to break your system. Your tests should also run fast, not create large files and must be junit tests included in a Suite called 'Suite.java' in order for them to be run in travis.

## Guidlines
(In random order.)

* There is no strict format for code, such as your must indent using a tab rather than four spaces or your imports should be in a specific order. Or a line limit other than the one Java suggests. If a riewer asks for this you should refer to this guideline! 
* Avoid reformatting code just for 'readability' because it creates false diffs. Better to tolerance the original developer's formatting.
* The standard Java class naming (camel case with first upper case letter) and method naming (camel case with a first lower letter) should be followed. 
* Classes should not comnsist of many lines, for instance if your class grows to 1000's of lines, consider delegating parts of it to other classes. 
* Do make sure that classes which are intended to be used outside a package are public and those concerned with implementing the functionality locally are not! 
* Ensure that methods which are not part of the intended external API are private or protected. 
* Consider delegating rather than using inheritance, try to keep inheritance trees short. For instance Interface->Abstract Class->Concrete Class is the maximum.
* There is no rule that interfaces should start with the letter 'I'. Some programmers have chosen to follow this methodology but it is not a requirement of the project.
* Keep interfaces for OSGi services in no (where no means no:) dependency projects. It should be possible to get interfaces from OSGi without making dependencies. Examples of this are that the project ofen uses connector patterns to hide a specific implementation. For instance a connector to scanning devices (EPICSv4) or a JSON API would be created to isolated using services to ensure that the details of the underlying implementation do not leak out. This means that we can swap easily between EPICSv4 and 0MQ or JSON and XML for instance.
* The travis tests must run with junit not junit plugin

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

