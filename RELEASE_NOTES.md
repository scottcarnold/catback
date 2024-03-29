# CatBack

CatBack is a simple system backup utility.

+ Author: Scott Arnold
+ Contributor: Cari Arnold
+ Icons: Glaze icons by Marco Martin
+ Textures: Genetica Textures by Spiral Graphics
+ Bitwise File Compare Code: Joe Orost
+ Java Version: 1.8

# CatBack 1.6 Release Notes
Released 3/20/2023
## Build
+ Updated CatSwing version to 1.0.6

## CatBack 1.6
+ File trees will now show hidden files
+ Fixed bug where same file or directory could get added to a backup more than once
+ Fixed bug with missing files not preserving the full directory structure of where they are missing from (fixed in CatSwing update)
+ Fixed issue where backing up an enormous number of small files could overload the event dispatch thread with events
+ Summary page will now show warning message if any selected files or folders for the backup are missing
+ System files such as Mac .DS_Store files will now consistently be ignored (fixed in CatSwing update)
+ Directories will no longer appear with file separators in the name (changed with CatSwing update)
+ Added safeguard to ensure user cannot launch backup that includes selections from within its own backup location

# CatBack 1.5 Release Notes
Released 3/12/2023
## Build
+ Updated to use Maven build system
+ Updated Java version from 1.6 to 1.8
+ Removed obsolete Windows installer. Optional executable is now created with Launch4J and all previously external files like the user guide are now packaged inside the archive for easier distribution

## CatBack 1.5
+ Updated appearance and revised counters on the copy process frame
+ Added command line flag for doing backup "dry runs" to be able to simulate backups without actually running them
+ Removed directory size cache debugging window and associated command line flag
+ Fixed issues with backup process not always responding properly to cancel request
+ Fixed resource leak in file copier
+ Fixed bug where file could report as copied if cancelled mid-copy
+ Scroll positions are now retained when switching between panels
+ Refactoring and removal of old code
+ Updated from Log4J to Log4J2
+ Due to the Log4J changes, the behavior of logging had to be revised. If you change the logging configuration with the -l flag, the changes will not take effect until the application is restarted.  If you set logging to "window", the logging window will now be opened via a menu item in the Window drop down menu.
+ About dialog updated to use RELEASE_NOTES.md for content
+ Updated to latest Zenput release

# CatBack 1.4 Release Notes
Released 8/1/2013
## CatBack 1.4
+ Added new preference that allows scan of last backup (backup step 2) to be skipped to make backup process faster.  Even if scan of last backup is turned off, it may still execute if previous backup was interrupted or the previous file list file cannot be loaded.  Turn scan of last backup on if there is any chance the contents of the latest backup folder have been modified since the last backup; otherwise, it is recommended to leave it off.
+ Added new preference to set the number of errors that can occur during the move/copy steps of a backup before the backup is halted.  Default is 10. (Previous versions of CatBack had no limit.)
+ Incremental backup directory sizes are now cached in files named ".cb_size" within the incremental backup directories.  This speeds up incremental backup processing when backups are on a slow network location.  If you manually modify the contents of an incremental backup, delete the ".cb_size" file from the root of the incremental backup directory (it will get recreated on the next backup run).
+ Backup steps 1 and 2 no longer guess on progress when displaying progress bar.  If there is not enough information to show an accurate level of progress, the progress bar appears in the unknown progress state.
+ Added Begin Backup menu item to the File menu.
+ Adjusted the number of fraction digits shown for file sizes (0 for KiB, 1 for MiB, 2 for GiB, 3 for TiB)
+ Mac Only:  Core classes updated to use latest Apple Java extensions.

# CatBack 1.3 Release Notes
Released 5/12/2011
## Build
+ Updated Java version from 1.5 to 1.6

## CatBack 1.3
+ Redesigned user interface.
+ Improved row sorting in tables.
+ Added preference for setting the application Look and Feel.
+ Checkbox file trees now use check boxes and selection highlighting consistent with the active Look and Feel.
+ Default Look and Feel for Windows platform is now the Nimbus Look and Feel.
+ Input processing and validation now handled by XanderCat Zenput framework.
+ Added backup history (on the menu, go to Window -> Backup History).
+ By default, logging output is now sent to catback.log (this can be changed using the "-l" flag).  This file is recreated every time CatBack is started.
+ Multiple miscellaneous bug fixes.

# CatBack 1.2 Release Notes
Released 6/12/2010
## CatBack 1.2
+ When starting the program, the previous state of the main window is restored (size, location, etc).
+ In the Edit menu, a new Backup Settings dialog replaces the Backup Profile Details dialog.
+ New backup settings for limiting the age and size of the incremental backup directories.
+ New backup setting to show what files will be moved/copied before the move/copy takes place.
+ New backup setting to keep the file copy window open after successful backup.
+ Added splash screen on startup.
+ Fixed issue with the log filling with warnings when performing backup to unmapped network drive.
+ Fixed bug where closing a backup from the File menu could result in an error.
+ File popup menus now have a Refresh option.
+ Windows distribution now uses an installer for added convenience.

# CatBack 1.1 Release Notes
Released 10/30/2009
## CatBack 1.1
+ File copying performance improved significantly by utilizing file channels.
+ Added ability to exclude files from a backup set.  This is useful when needing to exclude one or more files from directory being backed up while still automatically backing up any new files added to the directory.
+ File names are rendered in different colors:  blue for included, red for excluded, black if children are included or excluded, and all other files in gray.
+ New popup menu for files with shortcuts for include/exclude and info option for getting file details.
+ When starting the program or loading another backup, the previous state of the file tree is restored.
+ Improved icons.
+ Backup size display improved (calculation bug fixed, sizes now show up to 2 fraction digits, summary size can now show GiB where previously is only showed up to MiB).
+ Added support to enable newer versions of the application to load backup profiles from older versions.
+ Utilize new application framework for more consistent cross platform support and better version history.
+ When cancelling a backup in the middle of copying a large file, it will no longer attempt to finish copying the file (copied portion will be deleted).
+ Application can no longer be closed while a backup is currently in progress (backup must be cancelled or allowed to complete).
+ Fixed bug where message dialog on backup completion could get stuck behind main application window.
+ Profile names are now required (well, ok, more like strongly suggested).
+ Application log by default is now available through the Help menu (assuming you do not change the log target with the "-l" program argument).

# CatBack 1.0 Release Notes

+ Fixed bug where cancelling on step 5/5 would not actually cancel the copying of files.
+ Applied fix to prevent application from stalling out when a directory cannot be accessed (for example, a mapped network drive that is currently unavailable)
+ Various framework improvements (specifically for caching and user input).
+ Minor visual and wording changes.
+ New progress bar implemented to hopefully eliminate intermittent problems with the original progress bar.
+ Copy window status text area is now limited to 500 lines to prevent memory problems on extremely large backups.
+ Fixed issue with Mac verison always reporting it had copied the "/" directory.
+ Tables on the copy window now have a column for parent directory; also, the table columns can be reordered as well as turned on and off.
