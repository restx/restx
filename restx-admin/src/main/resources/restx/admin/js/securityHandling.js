// default security handling; redirect to a login.html page
// you can override this by providing a route for /@/ui/js/securityHandling.js at priority < 0

function onSecurityException(baseUri, status, backTo) {
    window.location = baseUri + '/@/ui/login.html?backTo=' + backTo;
}