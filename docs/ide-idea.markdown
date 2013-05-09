---
title: Intellij IDEA Setup
layout: docs
---
# Setting up a restx project in Intellij IDEA

## Using Maven Support

If you have chosen to use Maven and [have generated a pom](getting-started.html) for the project (or hand written one following the [provided instructions](manual-app-bootstrap.html)), then you can use IDEA built in Maven support to import the project.

What you will need to do:

- import the project
- use eclipse java compiler in the settings
- double check the annotation processor settings (optional)

### import the project

Use **File -> Import Project** and selects the pom.xml of the project. Then import the project with your preferred Maven options:

![import existing maven project into Intellij IDEA](/images/docs/idea-import-maven-project.png)

### use eclipse java compiler in the settings

Open the settings dialog in IDEA and go to `Compiler > Java Compiler`. Then select the eclipse compiler:

![selecting the eclipse compiler in IDEA](/images/docs/idea-java-compiler-preferences.png)

The reason to use the eclipse compiler is that incremental compilation with annotation processing currently works better with eclipse compiler.

### double check the annotation processor settings (optional)

You may need to double check the annotation processor settings to make sure it is enabled, use annotation processors from classpath, and preferably generate the sources in a folder relative to the module content root (so that you can easily see the generated sources):

![annotation processor settings in IDEA](/images/docs/idea-annotation-processor-preferences.png)
 