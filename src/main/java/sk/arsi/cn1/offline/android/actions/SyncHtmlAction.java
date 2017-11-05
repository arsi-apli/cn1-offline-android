/*
 * Copyright 2017 ArSi.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package sk.arsi.cn1.offline.android.actions;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.io.IOUtils;
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
        id = "sk.arsi.cn1.offline.android.actions.SyncHtmlAction"
)
@ActionRegistration(
        displayName = "", lazy = false
)
public class SyncHtmlAction extends NodeAction {

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
                    FileObject build = FileUtil.createFolder(directory.getParent(), "build");
                    FileObject classes = FileUtil.createFolder(build, "classes");
                    FileObject html = FileUtil.createFolder(classes, "html");
                    FileObject src = FileUtil.createFolder(directory, "src");
                    FileObject main = FileUtil.createFolder(src, "main");
                    FileObject resources = FileUtil.createFolder(src, "resources");
                    //for old gradle
                    TarArchiveOutputStream stream = buildHtml(main, html);
                    stream.flush();
                    stream.close();
                    //for new gradle version
                    stream = buildHtml(resources, html);
                    stream.flush();
                    stream.close();
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
    }

    private TarArchiveOutputStream buildHtml(FileObject main, FileObject html) throws FileNotFoundException, IOException {
        TarArchiveOutputStream stream = new TarArchiveOutputStream(new FileOutputStream(main.getPath() + File.separator + "html.tar"));
        FileObject[] children = html.getChildren();
        for (FileObject children1 : children) {
            addFileToTarGz(stream, FileUtil.toFile(children1).getAbsolutePath(), "");
        }
        return stream;
    }

    private void addFileToTarGz(TarArchiveOutputStream tOut, String path, String base)
            throws IOException {
        File f = new File(path);
        System.out.println(f.exists());
        String entryName = base + f.getName();
        TarArchiveEntry tarEntry = new TarArchiveEntry(f, entryName);
        tOut.putArchiveEntry(tarEntry);

        if (f.isFile()) {
            IOUtils.copy(new FileInputStream(f), tOut);
            tOut.closeArchiveEntry();
        } else {
            tOut.closeArchiveEntry();
            File[] children = f.listFiles();
            if (children != null) {
                for (File child : children) {
                    System.out.println(child.getName());
                    addFileToTarGz(tOut, child.getAbsolutePath(), entryName + "/");
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
        return "Sync HTML (First build cn1 project!)";
    }

    @Override
    public HelpCtx getHelpCtx() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
