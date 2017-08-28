/*
 * (C) Copyright 2017 Arsi (http://www.arsi.sk/).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package sk.arsi.cn1.offline.android.actions;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.Project;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.actions.NodeAction;
import static sk.arsi.cn1.offline.android.actions.InitAndroidProjectAction.openProjectBuildProperties;
import sk.arsi.cn1.offline.android.explorer.AndroidExplorerFactory;

/**
 *
 * @author arsi
 */
@ActionID(
        category = "AndroidOfflineSub/SubActions",
        id = "sk.arsi.cn1.offline.android.actions.LibsReloadAction"
)
@ActionRegistration(
        displayName = "", lazy = false
)
public class LibsReloadAction extends NodeAction {

    private static final int BUFFER_SIZE = 1024;

    @Override
    protected void performAction(Node[] activatedNodes) {
        if ((activatedNodes.length > 0) && (activatedNodes[0] instanceof FilterNode)) {
            Node original = activatedNodes[0].getLookup().lookup(AndroidExplorerFactory.AndroidNode.class);
            if (original instanceof AndroidExplorerFactory.AndroidNode) {
                AndroidExplorerFactory.AndroidNode node = (AndroidExplorerFactory.AndroidNode) original;
                Project project = (Project) node.getProjectAndroid();
                FileObject directory = node.getDirectory();
                Properties properties = openProjectBuildProperties(project);
                String mainName = properties.getProperty("codename1.mainName");
                String packageName = properties.getProperty("codename1.packageName");
                String keystorePassword = properties.getProperty("codename1.android.keystorePassword");
                String keystore = properties.getProperty("codename1.android.keystore");
                String keystoreAlias = properties.getProperty("codename1.android.keystoreAlias");
                try {
                    //libs
                    FileObject src = FileUtil.createFolder(directory.getParent(), "src");
                    ClassPath classPath = ClassPath.getClassPath(src, ClassPath.COMPILE);
                    List<FileObject> libs = new ArrayList<>();
                    if (classPath != null) {
                        List<ClassPath.Entry> entries = classPath.entries();
                        for (int i = 0; i < entries.size(); i++) {
                            ClassPath.Entry e = entries.get(i);
                            String url = e.getURL().toExternalForm();
                            if (url.startsWith("jar:")) {
                                String path = url.replace("jar:file:", "").replace("!/", "").replace("!\\", "");
                                FileObject fo = FileUtil.toFileObject(new File(path));
                                if (fo != null) {
                                    switch (fo.getNameExt()) {
                                        case "CodenameOne.jar":
                                        case "CodenameOne_SRC.zip":
                                        case "CLDC11.jar":
                                        case "JavaSE.jar":
                                            break;
                                        default:
                                            libs.add(fo);
                                            break;
                                    }
                                }
                            }
                        }
                    }
                    FileObject libsOut = FileUtil.createFolder(directory, "libs");
                    FileObject[] childrens = libsOut.getChildren();
                    for (FileObject children : childrens) {
                        if (children.isData() && !children.getNameExt().equalsIgnoreCase("cn1-android.jar")) {
                            children.delete();
                        }
                    }
                    for (FileObject lib : libs) {
                        FileUtil.copyFile(lib, libsOut, lib.getName());
                    }
                    //cn1 extensions
                    FileObject libsFolder = FileUtil.createFolder(directory.getParent(), "lib");
                    FileObject[] childs = libsFolder.getChildren();
                    for (FileObject child : childs) {
                        if ("cn1lib".equalsIgnoreCase(child.getExt())) {
                            unpackCn1Lib(child, libsOut);
                        }
                    }
                } catch (Exception e) {
                    Exceptions.printStackTrace(e);
                }
            }
        }
    }

    private void unpackCn1Lib(FileObject lib, FileObject libsOut) {
        try {
            ZipArchiveInputStream in = new ZipArchiveInputStream(lib.getInputStream());
            ZipArchiveEntry entry;

            while ((entry = in.getNextZipEntry()) != null) {
                if (!entry.isDirectory()) {
                    switch (entry.getName()) {
                        case "main.zip":
                            int count;
                            byte data[] = new byte[BUFFER_SIZE];
                            String fileName = lib.getName() + "_main.jar";
                            FileObject file = libsOut.createData(fileName);
                            OutputStream fos = file.getOutputStream();
                            try (BufferedOutputStream dest = new BufferedOutputStream(fos, BUFFER_SIZE)) {
                                while ((count = in.read(data, 0, BUFFER_SIZE)) != -1) {
                                    dest.write(data, 0, count);
                                }
                                dest.close();
                            }
                            break;
                        case "nativeand.zip":
                            unpackNativeCn1Lib(lib, in, entry, libsOut);
                            break;
                    }
                }

            }
        } catch (FileNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private void unpackNativeCn1Lib(FileObject lib, ZipArchiveInputStream masterStream, ZipArchiveEntry masterEntry, FileObject libsOut) {
        try {
            int count;
            FileObject src = FileUtil.createFolder(FileUtil.createFolder(FileUtil.createFolder(libsOut.getParent(), "src"), "main"), "java");
            byte data[] = new byte[BUFFER_SIZE];
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            while ((count = masterStream.read(data, 0, BUFFER_SIZE)) != -1) {
                bos.write(data, 0, count);
            }
            ZipArchiveInputStream in = new ZipArchiveInputStream(new ByteArrayInputStream(bos.toByteArray()));
            bos.close();
            String fileName = lib.getName() + "_native.jar";
            FileObject jarFile = libsOut.createData(fileName);
            ZipArchiveOutputStream out = new ZipArchiveOutputStream(jarFile.getOutputStream());
            ZipArchiveEntry entry;

            while ((entry = in.getNextZipEntry()) != null) {
                if (entry.isDirectory()) {
                    FileUtil.createFolder(src, entry.getName());
                } else if (entry.getName().endsWith("jar")) {
                    FileObject file = libsOut.createData(entry.getName());
                    OutputStream fos = file.getOutputStream();
                    try (BufferedOutputStream dest = new BufferedOutputStream(fos, BUFFER_SIZE)) {
                        while ((count = in.read(data, 0, BUFFER_SIZE)) != -1) {
                            dest.write(data, 0, count);
                        }
                        dest.close();
                    }
                } else {
                    String name = entry.getName();
                    String path = "";
                    if (name.contains("/")) {
                        path = name.substring(0, name.lastIndexOf('/'));
                    }
                    FileObject dir = src;
                    if (!"".equals(path)) {
                        dir = FileUtil.createFolder(src, path);
                    }
                    fileName = name.substring(name.lastIndexOf('/') + 1);
                    FileObject prev = dir.getFileObject(fileName);
                    if (prev != null) {
                        prev.delete();
                    }
                    FileObject file = dir.createData(fileName);
                    OutputStream fos = file.getOutputStream();
                    try (BufferedOutputStream dest = new BufferedOutputStream(fos, BUFFER_SIZE)) {
                        while ((count = in.read(data, 0, BUFFER_SIZE)) != -1) {
                            dest.write(data, 0, count);
                        }
                        dest.close();
                    }
                }
            }
            out.finish();
            out.flush();
            out.close();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    @Override
    protected boolean enable(Node[] activatedNodes) {
        if (activatedNodes.length > 0) {
            FileObject fob = activatedNodes[0].getLookup().lookup(FileObject.class);
            if (fob != null) {
                FileObject fileObject = fob.getFileObject("build", "gradle");
                return fileObject != null;
            }

        }
        return false;
    }

    @Override
    public String getName() {
        return "Sync libs";
    }

    @Override
    public HelpCtx getHelpCtx() {
        return null;
    }

}
