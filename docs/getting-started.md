---
filename: getting-started.md
layout: docs
title:  "Getting Started"
---
# Getting Started

## Ready

To get ready starting development with RESTX you should first [install RESTX](install.html). This shouldn't take more than a few minutes (could be seconds on Linux/MacOS with good bandwidth!), so you probably already have `restx` ready in your PATH!

Then you will need to install some plugins. Indeed RESTX comes almost entirely nude when you install it, but don't worry installing plugin is super easy. You just need to launch the restx shell (by running the `restx` command), use the `shell install` command to install `restx-core-shell` plugin:
<div class='highlight'><pre><code class='console'><span class='gp'>$</span> <span class='gt'>restx</span>
<span class='go'>===============================================================================</span>
<span class='go'>== WELCOME TO RESTX SHELL - {{ site.restx-version }} - type `help` for help on available commands</span>
<span class='go'>===============================================================================</span>
<span class='gp'>restx&gt;</span> <span class='gt'>shell install</span>
<span class='go'>:: loading settings :: url = jar:file:/Users/xavierhanin/.restx/lib/ivy-2.3.0.jar!/org/apache/ivy/core/settings/ivysettings.xml</span>
<span class='go'>looking for plugins...</span>
<span class='go'>found 3 available plugins</span>
<span class='go'> [  1] io.restx:restx-core-shell:0.2.4</span>
<span class='go'>        core commands: generate new app, ...</span>
<span class='go'> [  2] io.restx:restx-build-shell:0.2.4</span>
<span class='go'>        build commands: generate pom, ivy, ...</span>
<span class='go'> [  3] io.restx:restx-specs-shell:0.2.4</span>
<span class='go'>        specs commands: run a specs server, ...</span>
<span class='go'>Which plugin would you like to install (eg &#39;1 3 5&#39;)?</span>
<span class='gt'>1</span>
<span class='go'>installing io.restx:restx-core-shell:0.2.4...</span>
<span class='go'>:: loading settings :: url = jar:file:/Users/xavierhanin/.restx/lib/ivy-2.3.0.jar!/org/apache/ivy/core/settings/ivysettings.xml</span>
<span class='go'>installed io.restx:restx-core-shell:0.2.4</span>
<span class='go'>installed 1 plugins, restarting shell to take them into account</span>
<span class='go'>RESTARTING SHELL...</span>
<span class='go'>===============================================================================</span>
<span class='go'>== WELCOME TO RESTX SHELL - {{ site.restx-version }} - type `help` for help on available commands</span>
<span class='go'>===============================================================================</span>
<span class='gp'>restx&gt;</span>
</code></pre></div>

the `restx-core-shell` plugin is now ready to use! 

<div class="note">
	<p>As you can see installing plugins in the shell is super easy, remember to check periodically if there are new plugins to try out!</p>
</div>

## Steady

The first step is to bootstrap your first RESTX app. Launch the `restx` command in a directory where you want to create the app (it could be a `workspace` or `projects` directory) and run the `app new` command. It will ask you some questions, you can simply give the app name and use the default answer to all other questions:

<div class='highlight'><pre><code class='console'><span class='gp'>restx&gt;</span> <span class="gt">app new</span>
<span class='go'>Welcome to RESTX APP bootstrap!</span>
<span class='go'>This command will ask you a few questions to generate your brand new RESTX app.</span>
<span class='go'>For any question you can get help by answering &#39;??&#39; (without the quotes).</span>
<span class='gp'>App name? </span><span class="gt">MyApp</span>
<span class='gp'>group id [myapp]?</span>
<span class='gp'>artifact id [myapp]?</span>
<span class='gp'>main package [myapp]?</span>
<span class='gp'>version [0.1-SNAPSHOT]?</span>
<span class='gp'>generate module descriptor (ivy/pom/none/all) [all]?</span>
<span class='gp'>restx version [{{ site.restx-version }}]?</span>
<span class='gp'>signature key (to sign cookies) [4695455274730382645 myapp 45cbde67-4700-485b-b0ce-b1b161c0cf94 MyApp]?</span>
<span class='gp'>default port [8080]?</span>
<span class='gp'>base path [/api]?</span>
<span class='gp'>generate hello resource example [Y/n]?</span>
<span class='go'>scaffolding app to `/Users/xavierhanin/Documents/tmp/myapp` ...</span>
<span class='go'>generating module.ivy ...</span>
<span class='go'>generating pom.xml ...</span>
<span class='go'>generating hello resource ...</span>
<span class='go'>Congratulations! - Your app is now ready in `/Users/xavierhanin/Documents/tmp/myapp`</span>
</code></pre></div>

<div class="note">
<p>If you want to learn more about this app generation, <a href="shell-app-bootstrap.html">check the related doc</a>.</p>
</div>

## Go!

Your app is now ready, you can open it in your favorite IDE, simply follow the instrutions provided here:

- [Intellij IDEA](ide-idea.html)
- [Eclipse](ide-eclipse.html)
- [Netbeans](ide-netbeans.html)

<div class="note">
	<p>Don't want to use an IDE? running the app directly from the shell is planned, watch <a href="https://github.com/restx/restx/issues/4">this issue</a> to show your interest!</p>
</div>

Once your IDE is properly setup, go on and [try out the generated app](try-generated-app.html)
