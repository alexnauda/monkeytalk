-------------------------------------------------------------------------------
MonkeyTalk Javascript Tools
-------------------------------------------------------------------------------

This jar is the MonkeyTalk Javascript tools.  These tools can be used to create
(or re-create) the Javascript libs, including the project-specific JS lib, and
the MonkeyTalkAPI.js lib.  It can also be used to codegen Javascript scripts
from .mt files.

Usage
-----

To generate BOTH the project's JS lib and the main MonkeyTalkAPI.js, run this:

java -classpath monkeytalk-js-tools-X.Y.Z.jar com.gorillalogic.monkeytalk.api.js.tools.JSLibGenerator projdir projdir/libs/proj.js y

where "projdir" is the MonkeyTalk project folder (where your .mt files are
found), and "projdir/libs/proj.js" is the generated project-specific JS lib,
and "y" means to also include MonkeyTalkAPI.js in the "projdir/libs" folder.

More Info
---------

MonkeyTalk:
http://www.gorillalogic.com/monkeytalk

MonkeyTalk Documentation:
http://www.gorillalogic.com/monkeytalk/docs
