---
title: Eclipse Setup
layout: docs
---
# Setting up a restx project in Eclipse

## Using Maven Support

If you have chosen to use Maven and have generated a pom for the project (or hand written one following the [provided instructions](manual-app-bootstrap.html)), then you can use Eclipse Maven support (through the m2e plugin) to import the project.

### setup maven annotation processing in preferences

Open eclipse preferences, go to **Maven -> Annotation processing** and select `Automatically configure JDT APT`:

![setup maven annotation processing in preferences](/images/docs/eclipse-maven-preferences-apt.png)

### import the project as a regular maven project

Use the **File -> Import** menu and choose `Existing Maven Projects`:

![import existing maven project into eclipse](/images/docs/eclipse-import-maven-project.png)