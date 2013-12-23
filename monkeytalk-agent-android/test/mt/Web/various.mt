Browser * Open http://www.google.com
Browser * Verify Google
Browser * VerifyNot Gootle!
Input gbqfq enterText "gorilla logic"
Link "Gorilla Logic | Enterprise Application Consulting Services ..." click
Link "Testing Tools" click
Browser * Verify "Meet the Monkeys! | Gorilla Logic"
Browser * VerifyRegex "Meet the Monkeys!.*"
Browser * VerifyWildcard "Meet ??? Monkeys!*"
Browser * Open https://developer.mozilla.org/en/HTML/Element
Link S click
Link <select> click
Link Examples click
ItemSelector select select "Value 1"
ItemSelector select verify value1
ItemSelector select select "Value 3"
ItemSelector select verify "Value 3" text
ItemSelector select verifyNot "Value 4" text
