**Content**


# Introduction #

This tutorial will help you to get started with a one-way service that consumes a jms message, transform it and produce an outgoing jms message.

The generated code includes error handling that:
  * Automatically reconnects to the JMS Provider after a network failure or problem with the jms provider itself
  * Perform jms receive and jms send operations in one and the same transaction
  * In case of error during processing (e.g. receiving poisoned messages that can't be transformed)
    * Rollback the transaction
    * Sends a error-log-message to the error-log-queue
    * Removes the poisoned message from the in-queue and puts it on a dead letter queue
> > > (with the same name as the input-queue but prefixed with "DLQ.")

**NOTE**: Prerequisites for this tutorial is that the tutorial [Create a new Integration Component](TutorialCreateIntegrationComponent.md) is completed.

# Create a JMS to JMS service #

  * Select the menu "File --> New --> Other" and expand the wizard "SOI Toolkit Code Generator"
  * Select the code generator "Create a new service"

![http://soi-toolkit.googlecode.com/svn/wiki/Tutorials/TutorialCreateJmsToJmsService/TutorialCreateJmsToJmsService1.png](http://soi-toolkit.googlecode.com/svn/wiki/Tutorials/TutorialCreateJmsToJmsService/TutorialCreateJmsToJmsService1.png)

  * Click on the "Next >" button
  * The wizard "SOI Toolkit - Create a new service" opens up
    * Select the message exchange pattern "One Way"
    * Select the inbound transport "JMS"
    * Select the outbound transport "JMS"
    * Select your service project using the "Browse..." button, e.g. "/mySample-services"
    * Set a proper service-name, e.g. mySampleJmsService

> > NOTE: In real usage, avoid using "`service`" in the name of the service since the actual Mule service will be suffixed with "`-service`", see the file list screenshot below,  `mySampleJmsService-service.xml`

![http://soi-toolkit.googlecode.com/svn/wiki/Tutorials/TutorialCreateJmsToJmsService/TutorialCreateJmsToJmsService2.png](http://soi-toolkit.googlecode.com/svn/wiki/Tutorials/TutorialCreateJmsToJmsService/TutorialCreateJmsToJmsService2.png)

  * Click on the "Finish" button
    * The wizard now starts to generate files and refresh the workspace.

  * When the wizard is done you can find the files for your new service in the Eclipse Package Explorer


> ![http://soi-toolkit.googlecode.com/svn/wiki/Tutorials/TutorialCreateJmsToJmsService/TutorialCreateJmsToJmsService3.png](http://soi-toolkit.googlecode.com/svn/wiki/Tutorials/TutorialCreateJmsToJmsService/TutorialCreateJmsToJmsService3.png)

  * Files of interest:
    * Source folder `src/test/java`
> > A new package is created for the service `org.sample.mysample.mysamplejmsservice` with two Java-classes.
> > `MySampleJmsServiceTransformerTest.java` contains some unit tests for the transformer class that you can use as a start.
> > `MySampleJmsServiceIntegrationTest.java` contains some integrations tests for the whole service that you can use as a start.
> > `MySampleJmsServiceTestReceiver.java` contains a teststub receiver that you can use as a start.
    * Source folder `src/test/resources`
> > The folder `teststub-services` contains the file `MySampleJmsService-teststub-service.xml` that is a teststub service that you can use as a start.
    * Source folder `src/main/java`
> > A new package is created for the service `org.sample.mysample.mysamplejmsservice` with one Java-class.
> > `MySampleJmsServiceTransformer.java` contains a sample transformation that you can use as a start.
    * Source folder `src/main/resources`
> > The folder `services` contains the file `MySampleJmsService-service.xml` that contains the actual definition of the new service.
    * Source folder `src/environment`
> > The configuration file `mySample-config.properties` has got the following properties added:
```
 # Properties for jms-service "mySampleJmsService"
 # TODO: Update to reflect your settings
 MYSAMPLEJMSSERVICE_IN_QUEUE=MYSAMPLE.MYSAMPLEJMSSERVICE.IN.QUEUE
 MYSAMPLEJMSSERVICE_OUT_QUEUE=MYSAMPLE.MYSAMPLEJMSSERVICE.OUT.QUEUE
 MYSAMPLEJMSSERVICE_DL_QUEUE=DLQ.MYSAMPLE.MYSAMPLEJMSSERVICE.IN.QUEUE
```

  * Run unit tests
    * Right-click the test-class `MySampleJmsServiceIntegrationTest.java` and select "Run As" --> "JUnit Test"
> > The tests runs and its success are reported in the JUnit view:


> ![http://soi-toolkit.googlecode.com/svn/wiki/Tutorials/TutorialCreateJmsToJmsService/TutorialCreateJmsToJmsService4.png](http://soi-toolkit.googlecode.com/svn/wiki/Tutorials/TutorialCreateJmsToJmsService/TutorialCreateJmsToJmsService4.png)

> The Console view displays at the same time log-messages from the execution:

> ![http://soi-toolkit.googlecode.com/svn/wiki/Tutorials/TutorialCreateJmsToJmsService/TutorialCreateJmsToJmsService5.png](http://soi-toolkit.googlecode.com/svn/wiki/Tutorials/TutorialCreateJmsToJmsService/TutorialCreateJmsToJmsService5.png)

> NOTE: Take a look at the unit test code and the transformer code for a better understanding on what is going on :-)

  * Run tests manually
> Sometimes just running unit tests are not sufficient so it is a good idea to know how to perform manual tests locally in your own PC.
    * NOTE: Requires that you have ActiveMQ installed separately and that it is started.
> > See [installation guide](InstallationGuide#Installing_Apache_ActiveMQ.md) for instructions.
    * Right-click on the test-server `MySampleMuleServer.java` in package `org/sample/mysample` in source folder `src/test/java` and select "Run As --> Java Application"
    * Go to the url `http://localhost:8161/admin/queues.jsp` in a web browser.
    * Write an ok test-message (e.g. "AAA") to the in-queue (`MYSAMPLE.MYSAMPLEJMSSERVICE.IN.QUEUE`) using the ActiveMQ console and watch the console output and the log + out-queue (`SOITOOLKIT.LOG.INFO` and `MYSAMPLE.MYSAMPLEJMSSERVICE.OUT.QUEUE`)
    * Write a incorrect message (e.g. "ERROR") to the in-queue and review the console + queues.
    * Stop the mule server by giving focus to the console view in Eclipse and pressing the return key.

![http://soi-toolkit.googlecode.com/svn/wiki/Tutorials/TutorialCreateJmsToJmsService/TutorialCreateJmsToJmsService6.png](http://soi-toolkit.googlecode.com/svn/wiki/Tutorials/TutorialCreateJmsToJmsService/TutorialCreateJmsToJmsService6.png)