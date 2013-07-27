---
filename: ide.md
title: IDE Setup
layout: docs
---
# RESTX IDE Setup

Java has some of the best IDEs, free and open source. Although not necessary, we strongly recommend doing RESTX app development with an IDE.

<div class="note">
	<p>Don't want to use an IDE? running the app directly from the shell is planned, watch <a href="https://github.com/restx/restx/issues/4">this issue</a> to show your interest, or follow the Maven setup instructions below!</p>
</div>

RESTX being pure Java it's very straighforward to use in your IDE of choice. The only thing which require special attention is proper annotation processing setup.

You will find setup instructions for the 3 major Java IDE here:

- [Using RESTX with Intellij IDEA](ide-idea.html)
- [Using RESTX with Eclipse](ide-eclipse.html)
- [Using RESTX with Netbeans](ide-netbeans.html)

## Maven setup
In order to have your Maven build process the annotations, add the following plugins to your pom.xml:
- org.bsc.maven:maven-processor-plugin:2.2.4
- org.codehaus.mojo:build-helper-maven-plugin:1.3
 
### maven-processor-plugin
Use the following configuration to ensure the right annotation processors are being invoked:

    <executions>
      <execution>
        <id>process</id>
        <goals>
          <goal>process</goal>
        </goals>
        <phase>generate-sources</phase>
        <configuration>
          <outputDirectory>target/generated-sources/annotations</outputDirectory>
          <processors>
            <processor>restx.annotations.processor.RestxAnnotationProcessor</processor>
            <processor>restx.exceptions.processor.ErrorAnnotationProcessor</processor>
          </processors>
        </configuration>
      </execution>
    </executions>

### build-helper-maven-plugin
Use the following configuration to ensure the generated sources are added to the build path:

    <executions>                                                                                                                                                                                     
      <execution>                                                                                                                                                                              
        <id>add-source</id>                                                                                                                                                                      
        <phase>generate-sources</phase>                                                                                                                                                          
        <goals>                                                                                                                                                                                  
          <goal>add-source</goal>                                                                                                                                                          
        </goals>                                                                                                                                                                                 
        <configuration>                                                                                                                                                                          
          <sources>
            <source>target/generated-sources/annotations</source>
          </sources>
        </configuration>
      </execution>
    </executions>


<div class="go-next">
	<ul>
		<li><a href="try-generated-app.html"><i class="icon-rocket"> </i> Try the generated app</a></li>
		<li><a href="generated-app-explained.html"><i class="icon-cogs"> </i> Understand generated app</a></li>
		<li><a href="getting-started.html"><i class="icon-play"> </i> Getting started</a></li>
		<li><a href="/community/"><i class="icon-beer"> </i> Community</a></li>
		<li><a href="/docs/"><i class="icon-book"> </i> Documentation</a></li>
	</ul>	
</div>
