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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.api.project.Project;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.actions.NodeAction;
import sk.arsi.cn1.offline.android.actions.gui.AndroidLibSelector;
import sk.arsi.cn1.offline.android.explorer.AndroidExplorerFactory;

@ActionID(
        category = "AndroidOfflineSub/SubActions",
        id = "sk.arsi.cn1.offline.android.actions.UpdateAndroidLibAction"
)
@ActionRegistration(
        displayName = "", lazy = false
)
/**
 *
 * @author arsi
 */
public class UpdateAndroidLibAction extends NodeAction {

    private static final AndroidLibSelector form = new AndroidLibSelector();
    private static final DialogDescriptor desc = new DialogDescriptor(form, "cn1-android.jar update", true, DialogDescriptor.OK_CANCEL_OPTION, DialogDescriptor.OK_OPTION, null);
    private static final int BUFFER_SIZE = 1024;

    static {
        form.setDialogDescriptor(desc);
    }

    @Override
    protected void performAction(Node[] activatedNodes) {
        if ((activatedNodes.length > 0) && (activatedNodes[0] instanceof FilterNode)) {
            Node original = activatedNodes[0].getLookup().lookup(AndroidExplorerFactory.AndroidNode.class);
            if (original instanceof AndroidExplorerFactory.AndroidNode) {
                AndroidExplorerFactory.AndroidNode node = (AndroidExplorerFactory.AndroidNode) original;
                Project project = (Project) node.getProjectAndroid();
                FileObject directory = node.getDirectory();
                Object notify = DialogDisplayer.getDefault().notify(desc);
                String selected = form.getSelected();
                if (DialogDescriptor.OK_OPTION.equals(notify)) {
                    ProgressHandle progress = ProgressHandleFactory.createHandle("Downloading cn1-android.jar");
                    Runnable runnable = new Runnable() {
                        @Override
                        public void run() {
                            progress.start();
                            FileLock lock = null;
                            try {
                                FileObject libsOut = FileUtil.createFolder(directory, "libs");
                                FileObject android = FileUtil.createData(libsOut, "cn1-android.jar");
                                lock = android.lock();
                                URL db = new URL("http://server.arsi.sk/cn1/" + selected + ".jar");
                                URLConnection connection = db.openConnection();
                                connection.setConnectTimeout(3000);
                                connection.connect();
                                OutputStream dest = android.getOutputStream(lock);
                                InputStream in = connection.getInputStream();
                                int count;
                                byte data[] = new byte[BUFFER_SIZE];
                                while ((count = in.read(data, 0, BUFFER_SIZE)) != -1) {
                                    dest.write(data, 0, count);
                                }
                                dest.close();
                                in.close();
                            } catch (IOException iOException) {
                                Exceptions.printStackTrace(iOException);
                            }
                            if (lock != null) {
                                lock.releaseLock();
                            }
                            progress.finish();
                        }
                    };
                    new Thread(runnable).start();
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
        return "Update cn1-android.jar";
    }

    @Override
    public HelpCtx getHelpCtx() {
        return null;
    }

}
