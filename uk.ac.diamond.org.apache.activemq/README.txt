Plugin is build from the active-mq-all.jar.

After doing this the following implementations in active-mq-all is deleted from this bundle. This avoids build conflicts.
The dependency on eachpackage is then added to the bundle so that it comes from somewhere.

org.slf4j
javax.jms