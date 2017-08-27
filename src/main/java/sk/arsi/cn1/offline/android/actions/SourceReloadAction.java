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

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;
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
        id = "sk.arsi.cn1.offline.android.actions.SourceReloadAction"
)
@ActionRegistration(
        displayName = "", lazy = false
)
public class SourceReloadAction extends NodeAction {

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
                    //copy sources
                    FileObject src = FileUtil.createFolder(directory.getParent(), "src");
                    FileObject srcOut = FileUtil.createFolder(FileUtil.createFolder(FileUtil.createFolder(directory, "src"), "main"), "java");
                    FileObject assetsOut = FileUtil.createFolder(FileUtil.createFolder(FileUtil.createFolder(directory, "src"), "main"), "assets");
                    srcOut.delete();
                    srcOut = FileUtil.createFolder(FileUtil.createFolder(FileUtil.createFolder(directory, "src"), "main"), "java");
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
        return "Sync sources";
    }

    @Override
    public HelpCtx getHelpCtx() {
        return null;
    }

}
