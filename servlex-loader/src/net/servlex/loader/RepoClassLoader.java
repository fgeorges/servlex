/****************************************************************************/
/*  File:       RepoClassLoader.java                                        */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2015-12-06                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2015 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package net.servlex.loader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import org.apache.catalina.loader.WebappClassLoader;

/**
 * Tomcat {@link WebappClassLoader} that looks into an EXPath repository.
 * 
 * Saxon and Calabash allow packages to contain extensions written in Java.
 * These packages contain JAR files, which are deployed with the rest of the
 * package in the repository.
 * 
 * This loader scans the repository when it is started, and add the JAR files
 * from Saxon and Calabash extensions to the classpath.
 * 
 * One way of plugging it into Tomcat is to add the following `Loader` element
 * within the `Context` element in `conf/catalina/localhost/servlex.xml`:
 * 
 *     &lt;Loader loaderClass="net.servlex.loader.RepoClassLoader"/>
 * 
 * The JAR file containing this class must be put in Tomcat's own `lib/`
 * directory.
 * 
 * @author Florent Georges
 */
public class RepoClassLoader
        extends WebappClassLoader
{
    public RepoClassLoader()
    {
        super();
        initRepo();
    }

    public RepoClassLoader(ClassLoader loader)
    {
        super(loader);
        initRepo();
    }

    private void ensureDir(Path dir, String name)
    {
        if ( ! Files.exists(dir) ) {
            error(name + " does not exist: " + dir);
        }
        if ( ! Files.isDirectory(dir) ) {
            error(name + " is not a directory: " + dir);
        }
    }

    private void ensureFile(Path file, String name)
    {
        if ( ! Files.exists(file) ) {
            error(name + " does not exist: " + file);
        }
        if ( ! Files.isRegularFile(file) ) {
            error(name + " is not a regular file: " + file);
        }
        if ( ! Files.isReadable(file) ) {
            error(name + " is not readable: " + file);
        }
    }

    private Path getRepoDir()
    {
        // get the repo dir path
        String prop = System.getProperty(REPO_DIR_PROPERTY);
        if ( prop == null ) {
            error("Repo dir property is not set: " + REPO_DIR_PROPERTY);
        }
        // get the repo dir
        Path dir = Paths.get(prop);
        ensureDir(dir, "Repo dir");
        return dir;
    }

    private Set<Path> getPackageDirs()
    {
        // the repo
        Path repo = getRepoDir();
        // the private repo dir
        Path cellar = repo.resolve(".expath-pkg");
        ensureDir(cellar, "Private EXPath dir");
        // the repo package list
        Path pkgs = cellar.resolve("packages.txt");
        ensureFile(pkgs, "Repo package list");
        // the result set
        Set<Path> result = new HashSet<>();
        // each line is a package, with the format: "[dir] [name URI] [version]"
        // where [dir] is the package directory under [repo]
        for ( String line : readLines(pkgs) ) {
            int space = line.indexOf(' ');
            if ( space < 0 ) {
                error("Repo package list ill-formatted, no space in line: " + line);
            }
            String dir = line.substring(0, space);
            // the package dir
            Path pkg = repo.resolve(dir);
            ensureDir(pkg, "Package dir");
            result.add(pkg);
        }
        return result;
    }

    /**
     * Initialize the class loader, by loading the `.saxon/classpath.txt` files.
     * 
     * I would have expected overriding init() the right solution, but it is
     * never called.  So I call initRepo() explicitly from each constructor
     * instead.  That does not respect the lifecycle of the object.
     */
    private void initRepo()
    {
        // browse the children of the repo dir
        for ( Path pkg : getPackageDirs() ) {
            // [repo]/[pkg]/.saxon
            Path saxon = pkg.resolve(".saxon");
            if ( Files.exists(saxon) ) {
                if ( ! Files.isDirectory(saxon) ) {
                    error(".saxon package dir is not a directory: " + saxon);
                }
                // [repo]/[pkg]/.saxon/classpath.txt
                Path cp = saxon.resolve("classpath.txt");
                if ( Files.exists(cp) ) {
                    // TODO: Still support old style content dir name?
                    // [repo]/[pkg]/content
                    Path content = pkg.resolve("content");
                    ensureDir(content, "Package content dir");
                    // do it!
                    addClasspathFile(cp, content);
                }
            }
        }
    }

    private Set<String> readLines(Path file)
    {
        File f = file.toFile();
        BufferedReader in = null;
        try {
            Reader r = new FileReader(f);
            in = new BufferedReader(r);
        }
        catch ( FileNotFoundException ex ) {
            error("File does not exist to read lines: " + file, ex);
        }
        Set<String> lines = new HashSet<>();
        try {
            String line;
            while ( (line = in.readLine()) != null ) {
                lines.add(line);
            }
        }
        catch ( IOException ex ) {
            error("Error reading lines: " + file, ex);
        }
        return lines;
    }

    private void addClasspathFile(Path cp, Path content)
    {
        // content dir as a URL, absolute
        Path abs  = content.toAbsolutePath();
        URI  root = abs.toUri();
        // read lines
        for ( String line : readLines(cp) ) {
            // should be relative to package content dir
            // but allows it to be absolute as well
            URI uri = root.resolve(line);
            addClasspathLine(uri);
        }
    }

    private void addClasspathLine(URI uri)
    {
        // Use proper logging here? Beware we are in Tomcat top-level
        // classloader context, here.
        System.err.println("RepoClassLoader: Add JAR file to classpath: " + uri);
        URL u = null;
        try {
            u = uri.toURL();
        }
        catch ( MalformedURLException ex ) {
            error("Invalid URL: " + uri, ex);
        }
        super.addURL(u);
    }

    private void error(String msg)
    {
        // TODO: ...
        throw new RuntimeException(msg);
    }

    private void error(String msg, Throwable ex)
    {
        // TODO: ...
        throw new RuntimeException(msg, ex);
    }

    /**
     * The system property name for the repo directory.
     * 
     * Duplicated from {@code ServlexConstants}, has that class cannot be used
     * from here.
     */
    public static final String REPO_DIR_PROPERTY = "org.expath.servlex.repo.dir";
}


/* ------------------------------------------------------------------------ */
/*  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS COMMENT.               */
/*                                                                          */
/*  The contents of this file are subject to the Mozilla Public License     */
/*  Version 1.0 (the "License"); you may not use this file except in        */
/*  compliance with the License. You may obtain a copy of the License at    */
/*  http://www.mozilla.org/MPL/.                                            */
/*                                                                          */
/*  Software distributed under the License is distributed on an "AS IS"     */
/*  basis, WITHOUT WARRANTY OF ANY KIND, either express or implied.  See    */
/*  the License for the specific language governing rights and limitations  */
/*  under the License.                                                      */
/*                                                                          */
/*  The Original Code is: all this file.                                    */
/*                                                                          */
/*  The Initial Developer of the Original Code is Florent Georges.          */
/*                                                                          */
/*  Contributor(s): none.                                                   */
/* ------------------------------------------------------------------------ */
