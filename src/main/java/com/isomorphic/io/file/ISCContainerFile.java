/*-=-
    Isomorphic SmartClient web presentation layer
    Copyright 2000-2007 Isomorphic Software, Inc.

    OWNERSHIP NOTICE
    Isomorphic Software owns and reserves all rights not expressly granted in this source code,
    including all intellectual property rights to the structure, sequence, and format of this code
    and to all designs, interfaces, algorithms, schema, protocols, and inventions expressed herein.

    CONFIDENTIALITY NOTICE
    The contents of this file are confidential and protected by non-disclosure agreement:
      * You may not expose this file to any person who is not bound by the same obligations.
      * You may not expose or send this file unencrypted on a public network.

    SUPPORTED INTERFACES
    Most interfaces expressed in this source code are internal and unsupported. Isomorphic supports
    only the documented behaviors of properties and methods that are marked "@visibility external"
    in this code. All other interfaces may be changed or removed without notice. The implementation
    of any supported interface may also be changed without notice.

    If you have any questions, please email <sourcecode@isomorphic.com>.

    This entire comment must accompany any portion of Isomorphic Software source code that is
    copied or moved from this file.
*/

package com.isomorphic.io.file;

import com.isomorphic.io.ISCFile;
import com.isomorphic.log.Logger;
import com.isomorphic.datasource.DSRequest;
import com.isomorphic.criteria.Evaluator;

import static com.isomorphic.datasource.DataSource.FS_FILE_NAME;
import static com.isomorphic.datasource.DataSource.FS_FILE_TYPE;
import static com.isomorphic.datasource.DataSource.FS_FILE_FORMAT;
import static com.isomorphic.datasource.DataSource.FS_PRIMARY_KEYS;

import java.io.*;
import java.util.*;
import java.net.URL;


public class ISCContainerFile extends ISCUrlFile {
    private static Logger log = new Logger(ISCContainerFile.class.getName());


    protected String strippedFilename;

    // Note that ISCFile.newInstance will NOT strip the __USE_CONTAINER__ from the
    // filename before calling this.
    public ISCContainerFile (String filename, DSRequest request) throws IOException {
        super(filename, request, false);

        // ISCFile.newInstance() checks for this as well, and constructs an
        // ISCNativeFile instead if fallBackRootDir is available
        if (servletContext == null) {
            throw new IOException(
                "Configured for containerIO, but servletContext not " +
                "available!  You need to install the Init servlet"
            );
        }

        // strip containerIO token and canonicalize
        strippedFilename = stripContainerIOPrefix(filename);
        if (strippedFilename.startsWith("/")) {
            strippedFilename = strippedFilename.substring(1, strippedFilename.length());
        }
        this.url = servletContext.getClassLoader().getResource(strippedFilename);
    }

    public ISCContainerFile (String path) throws IOException {
        this(path, null);
    }

    // We could optimize the case where it is simple criteria and
    // fileName, fileType and fileFormat are all provided, because
    // in that case we could look up a single path.
    @Override
    public List<Map<String, Object>> findChildAttributes (Object criteria, DSRequest request) throws Exception {
        if (request == null) request = requestContext;

        Evaluator eval = new Evaluator(criteria, "exact");

        // Make sure the filename is interpreted as a directory
        final String path = strippedFilename.endsWith("/") ? strippedFilename : strippedFilename + "/";
        Set<String> allChildren = servletContext.getResourcePaths(path);
        ArrayList<Map<String, Object>> result = new ArrayList<Map<String, Object>>(allChildren.size());
        for (String eachChild : allChildren) {
            if (!eachChild.endsWith("/")) {
                ISCContainerFile child = new ISCContainerFile(eachChild, request);
                Map<String, Object> attributes = child.readAttributes();
                if (eval.valuesMatchCriteria(attributes)) {
                    result.add(attributes);
                }
            }
        }

        return result;
    }

    // We could optimize the case where it is simple criteria and
    // fileName, fileType and fileFormat are all provided, because
    // in that case we could look up a single path.
    @Override
    public List<ISCFile> findChildren (Object criteria, DSRequest request) throws Exception {
        if (request == null) request = requestContext;

        Evaluator eval = new Evaluator(criteria, "exact");

        // Make sure the filename is interpreted as a directory
        final String path = strippedFilename.endsWith("/") ? strippedFilename : strippedFilename + "/";
        Set<String> allChildren = servletContext.getResourcePaths(path);
        ArrayList<ISCFile> result = new ArrayList<ISCFile>(allChildren.size());
        for (String eachChild : allChildren) {
            if (!eachChild.endsWith("/")) {
                ISCContainerFile child = new ISCContainerFile(eachChild, request);
                Map<String, Object> attributes = child.readAttributes();
                if (eval.valuesMatchCriteria(attributes)) {
                    result.add(child);
                }
            }
        }

        return result;
    }

    @Override
    public ISCFile getChild (Map<String, Object> primaryKeys, DSRequest request) throws IOException {
        if (request == null) request = requestContext;

        Set<String> keys = primaryKeys.keySet();
        if (keys.containsAll(FS_PRIMARY_KEYS)) {
            // If all primary keys are supplied, then we can optimize by
            // getting the known path.
            return getChild(expandFileExtensions(primaryKeys), request);
        } else {
            // If we're missing some keys, then use the keys as arguments to findChildren,
            // so that missing keys will be treated as wildcards
            try {
                List<ISCFile> children = findChildren(primaryKeys, request);
                if (children.size() == 0) {
                    // If there were no children, then return a non-existing file
                    return getChild(expandFileExtensions(primaryKeys), request);
                } else {
                    if (children.size() > 1) {
                        log.warn("getChild found more than one file -- returning first");
                    }
                    return children.get(0);
                }
            }
            catch (Exception ex) {
                log.warn(ex.toString());
                return getChild(expandFileExtensions(primaryKeys), request);
            }
        }
    }

    @Override
    public ISCFile getChild (String path, DSRequest request) throws IOException {
        if (request == null) request = requestContext;

        final String fullPath = filename +
                                (filename.endsWith("/") ? "" : "/") +
                                path;

        return new ISCContainerFile(fullPath, request);
    }
}
