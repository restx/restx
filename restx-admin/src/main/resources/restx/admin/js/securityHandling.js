// default security handling; redirect to a login.html page
// you can override this by providing a route for /@/ui/js/securityHandling.js at priority < 0

function onSecurityException(baseUri, response, backTo) {
    if (response.config.headers && response.config.headers.RestxSu && response.status === 403) {
        // do nothing, the forbidden was sent while sudoing, probably in api docs
    } else {
        window.location = baseUri + '/@/ui/login.html?backTo=' + backTo;
    }
}