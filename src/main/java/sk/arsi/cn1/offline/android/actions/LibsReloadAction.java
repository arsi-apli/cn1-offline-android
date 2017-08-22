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
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
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

    @Override
    protected void performAction(Node[] activatedNodes) {
        if ((activatedNodes.length > 0) && (activatedNodes[0] instanceof FilterNode)) {
            Node original = null;
            try {
                Field field = FilterNode.class.getDeclaredField("original");
                field.setAccessible(true);
                original = (Node) field.get(activatedNodes[0]);
            } catch (NoSuchFieldException ex) {
                Exceptions.printStackTrace(ex);
            } catch (SecurityException ex) {
                Exceptions.printStackTrace(ex);
            } catch (IllegalArgumentException ex) {
                Exceptions.printStackTrace(ex);
            } catch (IllegalAccessException ex) {
                Exceptions.printStackTrace(ex);
            }
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

                } catch (Exception e) {
                    Exceptions.printStackTrace(e);
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
        return "Sync libs";
    }

    @Override
    public HelpCtx getHelpCtx() {
        return null;
    }
}
