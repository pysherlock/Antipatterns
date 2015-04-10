// $Id: ClassfileImport.java 55 2006-12-10 16:59:35Z tfmorris $
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

package org.argouml.uml.reveng.classfile;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import org.argouml.application.api.ProgressMonitor;
import org.argouml.kernel.Project;
import org.argouml.moduleloader.ModuleInterface;
import org.argouml.uml.reveng.FileImportUtils;
import org.argouml.uml.reveng.ImportInterface;
import org.argouml.uml.reveng.ImportSettings;
import org.argouml.uml.reveng.ImporterManager;
import org.argouml.util.FileFilters;
import org.argouml.util.SuffixFilter;

import antlr.ANTLRException;
import antlr.RecognitionException;
import antlr.TokenStreamException;


/**
 * This is the main class for the classfile import.
 *
 * @author Andreas Rueckert <a_rueckert@gmx.net>
 */
public class ClassfileImport implements ImportInterface, ModuleInterface {

    /////////////////////////////////////////////////////////
    // Instance variables

    /** The files that needs a second RE pass. */
    private Collection secondPassFiles;

    // The current project.
    private Project currentProject = null;
    
    private ImportSettings currentSettings;
    
    private Collection newElements;

    /*
     * @see org.argouml.uml.reveng.ImportInterface#parseFiles(org.argouml.kernel.Project, java.util.Collection, org.argouml.uml.reveng.ImportSettings, org.argouml.application.api.ProgressMonitor)
     */
    public Collection parseFiles(Project p, Collection files,
            ImportSettings settings, ProgressMonitor monitor)
        throws ImportException {

        secondPassFiles = new ArrayList();
        currentProject = p;
        newElements = new HashSet();
        currentSettings = settings;
        
        monitor.setMaximumProgress(countFiles(files));
        int count = 1;
        for (Iterator it = files.iterator(); it.hasNext();) {
            Object file = it.next();
            if (!(file instanceof File)) {
                throw new ImportException("Invalid argument - not a file: " 
                        + file);
            }
            processFile((File) file);
            monitor.updateProgress(count++);
        }
        
        if (count2ndPassFiles(secondPassFiles) > 0) {

            // Process all the files, that need a second pass.
            for (Iterator i = secondPassFiles.iterator(); i.hasNext();) {
                Object next = i.next();

                try {
                    if (next instanceof Collection) {
                        do2ndJarPass((Collection) next);
                    } else {
                        File nextFile = (File) next;
                        String fileName = nextFile.getName();
                        FileInputStream fis;
                        try {
                            fis = new FileInputStream(nextFile);
                        } catch (FileNotFoundException e) {
                            throw new ImportException(e);
                        }
                        parseFile(fis, fileName);

                    }
                } catch (ANTLRException e) {
                    throw new ImportException(e);
                } catch (IOException e) {
                    throw new ImportException(e);
                }
            }
        }
        return newElements;
    }
    

    /**
     * Count all class files, including ones inside JAR files.
     *
     * @return The number of files to process
     */
    private int countFiles(Collection files) {
        
        int total = 0;
        Iterator it = files.iterator();
        while (it.hasNext()) {
            File f = (File) it.next();
            if (f.getName().endsWith(".jar")) {
                try {
                    for (Enumeration e = (new JarFile(f)).entries(); e
                            .hasMoreElements();) {
                        ZipEntry entry = (ZipEntry) e.nextElement();
                        if (!entry.isDirectory()
                                && entry.getName().endsWith(".class")) {
                            total++;
                        }
                    }
                } catch (IOException e) {
                    // Just count it as a normal file
                    total++;
                }
            } else {
                total++;
            }
        }
        return total;
    }


    /**
     * Count the files in the 2nd pass buffer. We can't just use size() because
     * the collection can contain nested collections which need to be counted
     * independently.  In a nested collection, the first entry is the name of
     * the JAR file, so we use size()-1.
     * 
     * @param buffer
     *            The buffer with the files for the 2nd pass.
     */
    private int count2ndPassFiles(Collection buffer) {
	int nfiles = 0;

	for (Iterator i = secondPassFiles.iterator(); i.hasNext();) {
	    Object next = i.next();
	    nfiles += ((next instanceof Collection)
                ? ((Collection) next).size() - 1 : 1);
	}
	return nfiles;
    }

    /**
     * The main method for all parsing actions. It calls the
     * actual parser methods depending on the type of the
     * file.
     *
     * @param f The file or directory, we want to parse.
     * @throws ImportException containing nested exception with original error
     */
    public void processFile(File f) throws ImportException {

        // Is this file a Jarfile?
        if ( f.getName().endsWith(".jar")) { //$NON-NLS-1$
            processJarFile(f);
        } else {
            String fileName = f.getName();
            try {    // Try to parse this file.
                InputStream is;
                try {
                    is = new FileInputStream(f);
                } catch (FileNotFoundException e) {
                    throw new ImportException(e);
                }
                parseFile(is, fileName);
            } catch (ANTLRException e) {
                // TODO: Is this still needed/appropriate? It looks like
                // Modeller has been changed so that it no longer throws
                // exceptions... - tfm
                secondPassFiles.add(f);
            }
        }

    }


    /**
     * Process a Jar file, that contains classfiles.
     *
     * @param f The Jar file.
     */
    private void processJarFile(File f) throws ImportException {
        JarFile jarfile;
        try {
            jarfile = new JarFile(f);
        } catch (IOException e) {
            throw new ImportException("IO exception opening Jar file: " + f, e);
        }
        
	// A second pass buffer just for this jar.
	Collection jarSecondPassFiles = new ArrayList();

	for ( Enumeration e = jarfile.entries(); e.hasMoreElements(); ) {
	    ZipEntry entry = (ZipEntry) e.nextElement();
	    String entryName = entry.getName();
	    if ( !entry.isDirectory() 
                    && entryName.endsWith(".class")) { //$NON-NLS-1$
		try {
                    InputStream is;
                    try {
                        is = jarfile.getInputStream(entry);
                    } catch (IOException e1) {
                        // If this happens, something bad is going on ...
                        throw new ImportException(e1);
                    } 
		    parseFile(is, entryName);
		} catch (ANTLRException e1) {
		    if (jarSecondPassFiles.isEmpty()) {
		        // If there are no files tagged for a second pass,
		        // add the jar file as the 1st element.
			jarSecondPassFiles.add(f);
		    }
		    // Store the entry to be parsed a 2nd time.
		    jarSecondPassFiles.add(entryName);
		} 
            }
	}

	// If there are files to parse again, add the jar to the 2nd pass.
	if ( !jarSecondPassFiles.isEmpty()) {
	    secondPassFiles.add(jarSecondPassFiles);
        }

        try {
            jarfile.close();
        } catch (IOException e) {
            throw new ImportException("IO exception closing Jar file: " + f, e);
        }
    }

    /**
     * Do a 2nd pass on a Jar file.
     *
     * @param secondPassBuffer A buffer, that holds the jarfile and
     *                         the names of the entries to parse again.
     * @throws TokenStreamException 
     * @throws RecognitionException 
     */
    private void do2ndJarPass(Collection secondPassBuffer) throws IOException,
            RecognitionException, TokenStreamException {
        if (!secondPassBuffer.isEmpty()) {
	    Iterator iterator = secondPassBuffer.iterator();
	    JarFile jarfile = new JarFile( (File) iterator.next());

	    while (iterator.hasNext()) {
		String filename = (String) iterator.next();
		parseFile(
		        jarfile.getInputStream(jarfile.getEntry(filename)),
		        filename);
	    }
	    jarfile.close();
	}
    }

    /**
     * This method parses 1 Java classfile.
     *
     * @param is The inputStream for the file to parse.
     * @param fileName the name of the file to parse
     * @throws RecognitionException ANTLR parser error
     * @throws TokenStreamException ANTLR parser error
     */

    public void parseFile(InputStream is, String fileName)
        throws RecognitionException, TokenStreamException {

        int lastSlash = fileName.lastIndexOf('/');
	if (lastSlash != -1) {
	    fileName = fileName.substring(lastSlash + 1);
	}

        ClassfileParser parser =
                new ClassfileParser(new SimpleByteLexer(
                        new BufferedInputStream(is)));

        // start parsing at the classfile rule
        parser.classfile();

        // Create a modeller for the parser
        org.argouml.uml.reveng.java.Modeller modeller =
            new org.argouml.uml.reveng.java.Modeller(
                                                 currentProject.getModel(),
						 currentSettings,
                                                 fileName);


	// do something with the tree
	ClassfileTreeParser tparser = new ClassfileTreeParser();
	tparser.classfile(parser.getAST(), modeller);
        newElements.addAll(modeller.getNewElements());

        // Was there an exception thrown during modelling?
        //Exception e = modeller.getException();
        //if(e != null) {
        //    throw e;
        //}
    }

    /*
     * @see org.argouml.moduleloader.ModuleInterface#enable()
     */
    public boolean enable() {
        ImporterManager.getInstance().addimporter(this);
        return true;
    }

    /*
     * @see org.argouml.moduleloader.ModuleInterface#disable()
     */
    public boolean disable() {
        return true;
    }

    /*
     * @see org.argouml.uml.reveng.FileImportSupport#getName()
     */
    public String getName() {
        return "Java from classes";
    }

    /*
     * @see org.argouml.moduleloader.ModuleInterface#getInfo(int)
     */
    public String getInfo(int type) {
        switch (type) {
        case AUTHOR:
            return "Andreas Rueckert";
        case DESCRIPTION:
            return "Java import from class or jar files";
        case VERSION:
        default:
            return null;
        }
    }

    /*
     * @see org.argouml.uml.reveng.FileImportSupport#getSuffixFilters()
     */
    public SuffixFilter[] getSuffixFilters() {
	SuffixFilter[] result = {
	    FileFilters.JAVA_CLASS_FILTER,
	    FileFilters.JAVA_JAR_FILTER
	};
	return result;
    }

    /*
     * @see org.argouml.uml.reveng.ImportInterface#isParseable(java.io.File)
     */
    public boolean isParseable(File file) {
        return FileImportUtils.matchesSuffix(file, getSuffixFilters());
    }

    /*
     * @see org.argouml.uml.reveng.ImportInterface#getImportSettings()
     */
    public List getImportSettings() {
        return null;
    }

}












