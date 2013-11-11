---
filename: ref-settings.md
layout: docs
title:  "Settings"
---
# RESTX Settings support

Restx provides a simple yet powerful way to handle your application settings.

## Defining a settings interface

First you need to define an interface which will be used to access your application or module settings:

{% highlight java %}
@Settings
public interface MySettings {
    @SettingsKey(key = "example.key1")
    String key1();
    @SettingsKey(key = "example.key2", defaultValue = "MyValue2")
    String key2();
    @SettingsKey(key = "example.key3", defaultValue = "MyValue3",
            doc = "This is an example key 3")
    String key3();
}
{% endhighlight %}

Each method defines a settings key, i.e. a particular setting. You can provide a default value for each setting. The doc parameter allow to add some basic documentation for the setting, which will be displayed in the admin page for the settings.

## Using the settings

Then you can simply get an instance of this interface injected in any of your components:

{% highlight java %}
@Component
public class MyComponent {
    public MyComponent(MySettings settings) {
        System.out.println(String.format("I got these settings:\n" +
                " key1: %s ; key2: %s ; key3: %s",
                settings.key1(),
                settings.key2(),
                settings.key3()));
    }
}
{% endhighlight %}

Note that all settings are also made available as Named strings in the factory, so you can get a single setting injected:
{% highlight java %}
@Component
public class MyComponent {
    public MyComponent(@Named("example.key1") key1) {
    }
}
{% endhighlight %}


## Providing the settings

You have several way to define the value of the settings:

### Using default value

By giving a default value in the settings key, you can define the value the setting will take by default

### Using properties file

You can easily load settings values from a properties file. Restx provide a ConfigLoader component which can be used to load such properties from classpath:

{% highlight java %}
@Module
public class MyModule {
  public ConfigSupplier myConfigSupplier(ConfigLoader configLoader) {
        return configLoader.fromResource("myapp/myConfig");
    }
}
{% endhighlight %}

This will try to load settings from to classpath resources:

- myapp.myConfig.[env].properties
- myapp.myConfig.properties

where [env] is the value of a string named 'env' (which can be set using system property).

The properties file are not true properties file, they have the following differences:

- use UTF-8 encoding
- lines beginning with `#` are considered to be the documentation of the key following the comment

Here is an example file:

{% highlight console %}
# this is the doc for key1
# which can be on multiple lines
example.key1=value1
example.key2=value2
{% endhighlight %}


If you want to take advantage of this properties format but don't want to load it from the classpath, you can also use the parser on its own:
{% highlight java %}
    @Provides
    public ConfigSupplier myConfigSupplier() {
        return new ConfigSupplier() {
            @Override
            public RestxConfig get() {
                try {
                    return StdRestxConfig.parse("myfile.properties", 
                            Files.newReaderSupplier(new File("myfile.properties"), Charsets.UTF_8));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }
{% endhighlight %}


### Using named strings

You can provide a named string in the factory to set the value of a setting.

For instance:
{% highlight java %}
@Module
public class MyModule {
  @Provides @Named("example.key1") public String thisKey() { return "myvalue"; }
}
{% endhighlight %}

This is useful if you want to do programmatic settings.

### Using system properties

System properties set using -Dexample.key1=value will override the value of the setting.

## Web Console

You can access all the settings values in the admin console, on /@/ui/config/.

![Settings console](/images/docs/admin-config.png)

As you can see the console provides:

- the origin of each value (in the screenshot, key2 was set with system property, key1 in a properties file in classpath, and key3 used the default value from the rxinvoice.MySettings interface)
- the documentation for the key, coming either from the settings interface or from the properties file.


<div class="go-next">
	<ul>
		<li><a href="ref-core.html"><i class="icon-cloud"> </i> REST support reference</a></li>
		<li><a href="ref-factory.html"><i class="icon-cogs"> </i> RESTX Factory reference</a></li>
		<li><a href="/community/"><i class="icon-beer"> </i> Community</a></li>
		<li><a href="/docs/"><i class="icon-book"> </i> Documentation</a></li>
	</ul>	
</div>


 
