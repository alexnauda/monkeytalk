monkeytalk-slave
----------------

This module doesn't actually do anything other than touch the local maven repo.
We use it on our Jenkins slave to force the download of certain monkeytalk
artifacts so the rest of the builds on the slave work correctly.
