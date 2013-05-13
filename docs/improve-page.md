---
filename: improve-page.md
layout: docs
title:  "Improve page"
---
# Improving a page
<STYLE type="text/css">
	.edit { display:block; float:right; }
</STYLE>

<p>You want to improve the page <code id="page-path"> </code>, thanks in advance for your contribution!</p>

Contributions are done via [Github Pull Requests](https://help.github.com/articles/using-pull-requests). If you have never done any pull requests don't worry, we suggest you use [prose.io](http://prose.io/) using the `Edit with prose.io` button below, it's very simple. If you are familiar with github pull requests, you can obviously do the PR with github, use the `Edit on github` button below.

## Using prose.io

<a id="edit-prose" class="edit btn btn-primary" href="#"><i class="icon-desktop"> </i> Edit with prose.io</a>

Editing page with prose.io is as simple as 1 2 3:

1. sign in with github account
2. edit page
3. submit changes

![Editing page with prose](/images/docs/prose.png)


## Using github

<a id="edit-github" class="edit btn btn-primary" href="#"><i class="icon-github"> </i> Edit on github</a>

You can also edit the page using github interface. You will need to fork the repo, make your changes, and submit a pull request. Remember to use the `gh-pages` branch as base branch.



<div class="go-next">
	<ul>
		<li><a href="/community/"><i class="icon-beer"> </i> Community</a></li>
		<li><a href="/docs/"><i class="icon-book"> </i> Documentation</a></li>
		<li><a href="http://github.com/restx/restx"><i class="icon-code"> </i> View on github</a></li>
	</ul>	
</div>

<script>
(function() {
	var filename = window.location.hash.substring(1);
	document.getElementById('page-path').innerHTML=filename;
	document.getElementById('edit-github').setAttribute('href', 'https://github.com/restx/restx/blob/gh-pages/docs/' + filename);
	document.getElementById('edit-prose').setAttribute('href', 'http://prose.io/#restx/restx/edit/gh-pages/docs/' + filename);
})();
</script>