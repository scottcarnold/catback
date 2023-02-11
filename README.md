#CatBack

CatBack is a simple system back utility.  It allows for selecting included and excluded files and folders and supports incremental backups.  Backed up files and folders are copied as is, without encryption or compression.

At present, CatBack relies on an external Log4j2 configuration file located at the root of the application.  To pick it up, CatBack needs to be run with VM argument: -Dlog4j.configurationFile=-Dlog4j.configurationFile=file:[path-to-CatBack]\Log4j2.xml. Example:

	java -Dlog4j.configurationFile=-Dlog4j.configurationFile=file:\\\C:\CatBack\Log4j2.xml -jar catback.jar