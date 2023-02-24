# CatBack

CatBack is a simple system back utility.  It allows for selecting included and excluded files and folders and supports incremental backups.  Backed up files and folders are copied as is, without encryption or compression.

At present, CatBack relies on an external Log4j2 configuration file located at the root of the application.  To pick it up, CatBack needs to be run with VM argument: -Dlog4j.configurationFile=file:[path-to-CatBack]\Log4j2.xml. Example:

	java -Dlog4j.configurationFile=file:\\\C:\CatBack\Log4j2.xml -jar catback.jar

CatBack supports the following command line arguments:

* -l [off|console|window|file[:filename]] : changes how log messages are handled, but requires application restart to take effect
    * off : logging is turned off
    * console : log messages are routed to console
    * window : log messages are routed to a dialog window accessible through drop down menu item Window -> Application Log; log messages are lost on exit
    * file : log messages are sent to a log file.  If filename is not provided, a default filename will be used.
* -b [filename] : immediately launches backup of the supplied backup filename