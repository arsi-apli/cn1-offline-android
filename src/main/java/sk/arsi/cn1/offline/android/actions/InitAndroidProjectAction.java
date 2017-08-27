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
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.Project;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.filesystems.FileChooserBuilder;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.NbPreferences;
import org.openide.util.actions.NodeAction;
import sk.arsi.cn1.offline.android.explorer.AndroidExplorerFactory.AndroidNode;

/**
 *
 * @author arsi
 */
@ActionID(
        category = "AndroidOfflineNode",
        id = "sk.arsi.cn1.offline.android.actions.InitAndroidProjectAction"
)
@ActionRegistration(
        displayName = "", lazy = false
)
public class InitAndroidProjectAction extends NodeAction {

    @StaticResource
    private static final String PROJECT_TAR = "sk/arsi/cn1/offline/android.tar.gz";
    private static final int BUFFER_SIZE = 1024;
    private Properties properties = new Properties();

    @Override
    protected void performAction(Node[] activatedNodes) {
        if ((activatedNodes.length > 0) && (activatedNodes[0] instanceof FilterNode)) {
            Node original = activatedNodes[0].getLookup().lookup(AndroidNode.class);
            if (original instanceof AndroidNode) {
                FileObject fob = activatedNodes[0].getLookup().lookup(FileObject.class);
                InputStream resourceAsStream = getClass().getClassLoader().getResourceAsStream(PROJECT_TAR);
                extractTarGZ(resourceAsStream, fob);
                AndroidNode node = (AndroidNode) original;
                Project project = node.getProjectAndroid();
                FileObject directory = node.getDirectory();
                properties = openProjectBuildProperties(project);
                String mainName = properties.getProperty("codename1.mainName");
                String packageName = properties.getProperty("codename1.packageName");
                String keystorePassword = properties.getProperty("codename1.android.keystorePassword");
                String keystore = properties.getProperty("codename1.android.keystore");
                String keystoreAlias = properties.getProperty("codename1.android.keystoreAlias");
                //gradle build
                FileObject gradle = directory.getFileObject("build", "gradle");
                try {
                    String script = gradle.asText("UTF-8");
                    script = script.replace("com.codename1.apps.devicetester", packageName);
                    OutputStream os = gradle.getOutputStream();
                    os.write(script.getBytes("UTF-8"));
                    os.close();
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
                try {
                    FileObject manifest = FileUtil.createFolder(FileUtil.createFolder(directory, "src"), "main").getFileObject("AndroidManifest", "xml");
                    String script = manifest.asText("UTF-8");
                    script = script.replace("package=\"com.codename1.apps.devicetester\"", "package=\"" + packageName + "\"");
                    script = script.replace("android:label=\"DeviceTester\"", "android:label=\"" + mainName + "\"");
                    script = script.replace("android:name=\"com.codename1.apps.devicetester.DeviceTesterStub\"", "android:name=\"" + packageName + "." + mainName + "Stub\"");
                    script = script.replace("android:authorities=\"com.codename1.apps.devicetester.google_measurement_service\"", "android:authorities=\"" + packageName + ".google_measurement_service\"");
                    OutputStream os = manifest.getOutputStream();
                    os.write(script.getBytes("UTF-8"));
                    os.close();
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
                //setup SDK
                String sdkPath = NbPreferences.forModule(InitAndroidProjectAction.class).get("sdk", null);
                if (sdkPath == null) {
                    FileChooserBuilder fcho = new FileChooserBuilder(InitAndroidProjectAction.class);
                    fcho.setDirectoriesOnly(true);
                    fcho.setTitle("Select Android SDK location");
                    File sdk = fcho.showOpenDialog();
                    if (sdk != null) {
                        sdkPath = sdk.getAbsolutePath();
                        NbPreferences.forModule(InitAndroidProjectAction.class).put("sdk", sdkPath);
                    }
                }
                if (sdkPath != null) {
                    try {
                        FileObject local = directory.getFileObject("local", "properties");
                        OutputStream os = local.getOutputStream();
                        String prop = "sdk.dir=" + sdkPath + "\n";
                        os.write(prop.getBytes("UTF-8"));
                        os.close();
                    } catch (Exception e) {
                    }
                }
                try {
                    //copy sources
                    FileObject src = FileUtil.createFolder(directory.getParent(), "src");
                    FileObject srcOut = FileUtil.createFolder(FileUtil.createFolder(FileUtil.createFolder(directory, "src"), "main"), "java");
                    FileObject assetsOut = FileUtil.createFolder(FileUtil.createFolder(FileUtil.createFolder(directory, "src"), "main"), "assets");
                    FileObject[] children = src.getChildren();
                    for (FileObject fo : children) {
                        if (fo.isFolder()) {
                            copyFolder(fo, FileUtil.createFolder(srcOut, fo.getNameExt()), null);
                        } else {
                            FileObject previous = assetsOut.getFileObject(fo.getName(), fo.getExt());
                            if (previous != null) {
                                previous.delete();
                            }
                            FileUtil.copyFile(fo, assetsOut, fo.getName(), fo.getExt());
                        }
                    }

                    //natives
                    List<FileObject> natives = new ArrayList<>();
                    src = FileUtil.createFolder(FileUtil.createFolder(directory.getParent(), "native"), "android");
                    children = src.getChildren();
                    for (FileObject fo : children) {
                        if (fo.isFolder()) {
                            copyFolder(fo, FileUtil.createFolder(srcOut, fo.getNameExt()), natives);
                        } else {
                            FileObject previous = srcOut.getFileObject(fo.getName(), fo.getExt());
                            if (previous != null) {
                                previous.delete();
                            }
                            FileObject copyFile = FileUtil.copyFile(fo, srcOut, fo.getName(), fo.getExt());
                            natives.add(copyFile);
                        }
                    }
                    String outPatch = srcOut.getPath();
                    List<String> classes = new ArrayList<>();
                    for (FileObject fon : natives) {
                        if (fon.getPath().contains(".java")) {
                            classes.add(fon.getPath().replace(outPatch + File.separator, "").replace("/", ".").replace("\\", ".").replace(".java", ""));
                        }
                    }
                    //stub
                    StringTokenizer tok = new StringTokenizer(packageName, ".", false);
                    FileObject current = srcOut;
                    while (tok.hasMoreElements()) {
                        String dirName = tok.nextToken();
                        current = FileUtil.createFolder(current, dirName);
                    }
                    FileObject stub = FileUtil.createData(current, mainName + "Stub.java");
                    String source = Stub.STUB;
                    source = source.replaceAll("#package", packageName);
                    source = source.replaceAll("#classname", mainName + "Stub");
                    source = source.replaceAll("#origname", mainName);
                    String nativeStub = "\n";
                    for (String classe : classes) {
                        nativeStub += "com.codename1.system.NativeLookup.register(" + classe.replace("Impl", "") + ".class," + classe + ".class);\n";
                    }
                    nativeStub += "\n";
                    source = source.replaceAll("#native", nativeStub);
                    OutputStream outputStream = stub.getOutputStream();
                    outputStream.write(source.getBytes("UTF-8"));
                    outputStream.close();
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
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
                    for (FileObject lib : libs) {
                        FileUtil.copyFile(lib, libsOut, lib.getName());
                    }

                } catch (Exception e) {
                    Exceptions.printStackTrace(e);
                }
            }
        }
    }

    private void copyFolder(FileObject fo, FileObject srcOut, List<FileObject> writen) {
        FileObject[] childrens = fo.getChildren();
        for (FileObject children : childrens) {
            if (children.isFolder()) {
                try {
                    copyFolder(children, FileUtil.createFolder(srcOut, children.getNameExt()), writen);
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            } else {
                try {
                    FileObject copyFile = FileUtil.copyFile(children, srcOut, children.getName());
                    if (writen != null) {
                        writen.add(copyFile);
                    }
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
    }

    public void extractTarGZ(InputStream in, FileObject fob) {
        GzipCompressorInputStream gzipIn = null;
        try {
            gzipIn = new GzipCompressorInputStream(in);
            try (TarArchiveInputStream tarIn = new TarArchiveInputStream(gzipIn)) {
                TarArchiveEntry entry;

                while ((entry = (TarArchiveEntry) tarIn.getNextEntry()) != null) {
                    /**
                     * If the entry is a directory, create the directory. *
                     */
                    if (entry.isDirectory()) {
                        FileUtil.createFolder(fob, entry.getName());
                    } else {
                        int count;
                        byte data[] = new byte[BUFFER_SIZE];
                        String name = entry.getName();
                        String path = "";
                        if (name.contains("/")) {
                            path = name.substring(0, name.lastIndexOf('/'));
                        }
                        FileObject dir = fob;
                        if (!"".equals(path)) {
                            dir = FileUtil.createFolder(fob, path);
                        }
                        String fileName = name.substring(name.lastIndexOf('/') + 1);
                        FileObject file = dir.createData(fileName);
                        OutputStream fos = file.getOutputStream();
                        try (BufferedOutputStream dest = new BufferedOutputStream(fos, BUFFER_SIZE)) {
                            while ((count = tarIn.read(data, 0, BUFFER_SIZE)) != -1) {
                                dest.write(data, 0, count);
                            }
                            dest.close();
                        }
                    }
                }

                tarIn.close();
                System.out.println("Untar completed successfully!");
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        } finally {
            try {
                gzipIn.close();
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    @Override
    protected boolean enable(Node[] activatedNodes) {
        if (activatedNodes.length > 0) {
            FileObject fob = activatedNodes[0].getLookup().lookup(FileObject.class);
            if (fob != null) {
                FileObject fileObject = fob.getFileObject("build", "gradle");
                return fileObject == null;
            }

        }
        return false;
    }

    @Override
    public String getName() {
        return "Create Android offline project";
    }

    @Override
    public HelpCtx getHelpCtx() {
        return null;
    }

    public static Properties openProjectBuildProperties(final Project prj) {
        final FileObject projectDir = prj.getProjectDirectory();
        final Properties prop = new Properties();
        try {
            final FileInputStream in = new FileInputStream(projectDir.getPath() + "/codenameone_settings.properties");
            prop.load(in);
            in.close();
        } catch (IOException ex) {
            Exceptions.printStackTrace((Throwable) ex);
        }
        return prop;
    }

}
