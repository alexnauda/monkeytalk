
WebBrowser Implementation of MonkeyTalk

Uses Selenium WebDriver to remote Browser.

Some browsers need a "server" to be running as a proxy to browser.
e.g. Chrome proxy (chromedriver) to handle profile and other items needed by chrome to start up.
Servers are specific to OS and are located for now under resources/browser-drivers

For tests to pass the host needs to have Firefox and Chrome installed in their default OS locations.

TODO
o Determine effective way to handle browser and driver proxies.
	o Starting and stoping browser
	o Starting and stoping driver proxy.
	o Chrome driver copied on class path needs executable rights.
o Need additional tests to increase coverage.
o Finish CommandResponse and Verify
o Handle case of not finding WebElement
o Handle error with WebDriver action (e.g. sendKeys, submit, etc.)
o Get maven build in shape including code coverage, is a jar needed?
o Add comments.




