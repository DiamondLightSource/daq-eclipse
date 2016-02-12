Plugin is build from the active-mq-all.jar.

After doing this the slf4j implementation in active-mq-all is deleted from this bundle. This avoids build conflicts.
The dependency on org.slf4j is then added to the bundle so that it comes from somewhere.