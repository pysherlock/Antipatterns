// $Id: AbstractFilePersister.java 11580 2006-12-10 17:57:23Z tfmorris $
// Copyright (c) 1996-2006 The Regents of the University of California. All
// Rights Reserved. Permission to use, copy, modify, and distribute this
// software and its documentation without fee, and without a written
// agreement is hereby granted, provided that the above copyright notice
// and this paragraph appear in all copies.  This software program and
// documentation are copyrighted by The Regents of the University of
// California. The software program and documentation are supplied "AS
// IS", without any accompanying services from The Regents. The Regents
// does not warrant that the operation of the program will be
// uninterrupted or error-free. The end-user understands that the program
// was developed for research purposes and is advised not to rely
// exclusively on the program for any reason.  IN NO EVENT SHALL THE
// UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY FOR DIRECT, INDIRECT,
// SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES, INCLUDING LOST PROFITS,
// ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
// THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
// SUCH DAMAGE. THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY
// WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
// MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
// PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
// CALIFORNIA HAS NO OBLIGATIONS TO PROVIDE MAINTENANCE, SUPPORT,
// UPDATES, ENHANCEMENTS, OR MODIFICATIONS.

package org.argouml.persistence;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.swing.event.EventListenerList;
import javax.swing.filechooser.FileFilter;

import org.argouml.kernel.Project;
import org.argouml.util.ThreadUtils;

/**
 * To persist to and from zargo (zipped file) storage.
 *
 * @author Bob Tarling
 */
public abstract class AbstractFilePersister extends FileFilter
        implements ProjectFilePersister {
    private EventListenerList listenerList = new EventListenerList();

    /**
     * Create a temporary copy of the existing file.
     *
     * @param file the file to copy.
     * @return the temp file or null if none copied.
     * @throws FileNotFoundException if file not found
     * @throws IOException if error reading or writing
     */
    protected File createTempFile(File file)
        throws FileNotFoundException, IOException {
        File tempFile = new File(file.getAbsolutePath() + "#");

        if (tempFile.exists()) {
            tempFile.delete();
        }

        if (file.exists()) {
            copyFile(file, tempFile);
        }

        return tempFile;
    }

    /**
     * Copies one file src to another, raising file exceptions
     * if there are some problems.
     *
     * @param dest The destination file.
     * @param src The source file.
     * @return The destination file after successful copying.
     * @throws IOException if there is some problems with the files.
     * @throws FileNotFoundException if any of the files cannot be found.
     */
    protected File copyFile(File src, File dest)
        throws FileNotFoundException, IOException {

        FileInputStream fis  = new FileInputStream(src);
        FileOutputStream fos = new FileOutputStream(dest);
        byte[] buf = new byte[1024];
        int i = 0;
        while ((i = fis.read(buf)) != -1) {
            fos.write(buf, 0, i);
        }
        fis.close();
        fos.close();

        dest.setLastModified(src.lastModified());

        return dest;
    }



    ////////////////////////////////////////////////////////////////
    // FileFilter API

    /*
     * @see javax.swing.filechooser.FileFilter#accept(java.io.File)
     */
    public boolean accept(File f) {
        if (f == null) {
	    return false;
	}
        if (f.isDirectory()) {
	    return true;
	}
        String s = getExtension(f);
        if (s != null) {
	    // this check for files without extension...
            if (s.equalsIgnoreCase(getExtension())) {
		return true;
	    }
	}
        return false;
    }

    /**
     * The extension valid for this type of file.
     * (Just the chars, not the dot: e.g. "zargo".)
     *
     * @return the extension valid for this type of file
     */
    public abstract String getExtension();

    /**
     * Just the description, not the extension between "()".
     *
     * @return the description valid for this type of file
     */
    protected abstract String getDesc();

    private static String getExtension(File f) {
        if (f == null) {
	    return null;
	}
        return getExtension(f.getName());
    }

    private static String getExtension(String filename) {
        int i = filename.lastIndexOf('.');
        if (i > 0 && i < filename.length() - 1) {
            return filename.substring(i + 1).toLowerCase();
        }
        return null;
    }

    /**
     * Given the full filename this returns true if that filename contains the
     * expected extension for the is persister.
     *
     * @param filename The filename to test.
     * @return true if the filename is valid for this persister
     */
    public boolean isFileExtensionApplicable(String filename) {
        return filename.toLowerCase().endsWith("." + getExtension());
    }

    /*
     * @see javax.swing.filechooser.FileFilter#getDescription()
     */
    public String getDescription() {
        return getDesc() + " (*." + getExtension() + ")";
    }

    /**
     * Save a project to file.<p>
     * This first archives the existing file, then calls
     * doSave(...) to do the actual saving.<p>
     * Should doSave(...) throw an exception then it is
     * caught here and any rollback handled before rethrowing
     * the exception.
     *
     * @param project The project being saved.
     * @param file The file to which the save is taking place.
     * @throws SaveException when anything goes wrong
     * @throws InterruptedException     if the thread is interrupted
     *
     * @see org.argouml.persistence.ProjectFilePersister#save(
     * org.argouml.kernel.Project, java.io.File)
     */
    public final void save(Project project, File file) throws SaveException, 
    InterruptedException {
        preSave(project, file);
        doSave(project, file);
        postSave(project, file);
    }

    /**
     * Handle archiving of previous file or any other common
     * requirements before saving a model to a file.
     *
     * @param project The project being saved.
     * @param file The file to which the save is taking place.
     * @throws SaveException when anything goes wrong
     */
    private void preSave(Project project, File file) throws SaveException {
        if (project == null && file == null) {
            throw new SaveException("No project nor file given");
        }
    }

    /**
     * Handle archiving on completion of a save such as renaming
     * the temporary save file to the real filename.
     *
     * @param project The project being saved.
     * @param file The file to which the save is taking place.
     * @throws SaveException when anything goes wrong
     */
    private void postSave(Project project, File file) throws SaveException {
        if (project == null && file == null) {
            throw new SaveException("No project nor file given");
        }
    }

    /**
     * Implement in your concrete class to save a project to a
     * file.<p>
     * There is no need to worry about archiving or restoring
     * archive on failure, that is handled by the rest of the
     * framework.<p>
     *
     * @param project the project to save
     * @param file The file to write.
     * @throws SaveException when anything goes wrong
     * @throws InterruptedException     if the thread is interrupted
     *
     * @see org.argouml.persistence.AbstractFilePersister#save(
     * org.argouml.kernel.Project, java.io.File)
     */
    protected abstract void doSave(Project project, File file)
        throws SaveException, InterruptedException;

    /**
     * Some persisters only provide load functionality for discontinued formats
     * but no save.
     * This method returns true by default. Those Peristers that do not provide
     * save must override this.
     * @return true if this persister is able to save
     */
    public boolean isSaveEnabled() {
        return true;
    }

    /**
     * Some persisters only provide save functionality for deprecated formats.
     * Other persisters with the same extension will manage loading.
     * This method returns true by default. Those Peristers that do not provide
     * load must override this.
     * @return true if this persister is able to load
     */
    public boolean isLoadEnabled() {
        return true;
    }

    /*
     * @see org.argouml.persistence.ProjectFilePersister#doLoad(java.io.File)
     */
    public abstract Project doLoad(File file)
    	throws OpenException, InterruptedException;



    /**
     * Add any object interested in listening to persistence progress.
     *
     * @param listener the interested listener.
     */
    public void addProgressListener(ProgressListener listener) {
        listenerList.add(ProgressListener.class, listener);
    }

    /**
     * Remove any object no longer interested in listening to persistence
     * progress.
     *
     * @param listener the listener to remove.
     */
    public void removeProgressListener(ProgressListener listener) {
        listenerList.remove(ProgressListener.class, listener);
    }

    /**
     * Returns true if a FileChooser should visualize an icon for the
     * persister.
     * 
     * @return true if the persister is associated to an icon 
     */
    public abstract boolean hasAnIcon();
    
    // TODO: Document 
    class ProgressMgr implements ProgressListener {
        
        /**
         * The percentage completeness of phases complete.
         * Does not include part-completed phases.
         */
        private int percentPhasesComplete;

        /**
         * The sections complete of a load or save.
         */
        private int phasesCompleted;

        /**
         * The number of equals phases the progress will measure.
         * It is assumed each phase will be of equal time.
         * There is one phase for each upgrade from a previous
         * version and one pahse for the final load.
         */
        private int numberOfPhases;
        
        public void setPercentPhasesComplete(int aPercentPhasesComplete) {
            this.percentPhasesComplete = aPercentPhasesComplete;
        }

        public void setPhasesCompleted(int aPhasesCompleted) {
            this.phasesCompleted = aPhasesCompleted;
        }

        public void setNumberOfPhases(int aNumberOfPhases) {
            this.numberOfPhases = aNumberOfPhases;
        }

        public int getNumberOfPhases() {
            return this.numberOfPhases;
        }
        
        protected void nextPhase() throws InterruptedException {
            ThreadUtils.checkIfInterrupted();
            ++phasesCompleted;
            percentPhasesComplete =
                (phasesCompleted * 100) / numberOfPhases;
            fireProgressEvent(percentPhasesComplete);
        }
        
        /**
         * Called when a ProgressEvent is fired.
         *  
         * @see org.argouml.persistence.ProgressListener#progress(org.argouml.persistence.ProgressEvent)
         * @throws InterruptedException     if thread is interrupted
         */
        public void progress(ProgressEvent event) throws InterruptedException {
            ThreadUtils.checkIfInterrupted();
            int percentPhasesLeft = 100 - percentPhasesComplete;
            long position = event.getPosition();
            long length = event.getLength();
            long proportion = (position * percentPhasesLeft) / length;
            fireProgressEvent(percentPhasesComplete + proportion);
        }
        
        /**
         * Inform listeners of any progress notifications.
         * @param percent the current percentage progress.
         * @throws InterruptedException     if thread is interrupted
         */
        protected void fireProgressEvent(long percent) 
            throws InterruptedException {
            ProgressEvent event = null;
            // Guaranteed to return a non-null array
            Object[] listeners = listenerList.getListenerList();
            // Process the listeners last to first, notifying
            // those that are interested in this event
            for (int i = listeners.length - 2; i >= 0; i -= 2) {
                if (listeners[i] == ProgressListener.class) {
                    // Lazily create the event:
                    if (event == null) {
                        event = new ProgressEvent(this, percent, 100);
                    }
                    ((ProgressListener) listeners[i + 1]).progress(event);
                }
            }
        }        
    }
}
