To build the swagger-ui.html, I've followed these steps:
- git clone swagger-ui
- do small adjustments to dist/index.html:
  - remove the header,
  - remove <ie8 specific css
  - set the discovery URL to /api/@/api-docs
  - remove the leading slash in handlebar template for api heading h2
- run buildProduction --outroot production --root dist dist/index.html
- manually inline the css and js + replace "</script>" by "<" + "/script"