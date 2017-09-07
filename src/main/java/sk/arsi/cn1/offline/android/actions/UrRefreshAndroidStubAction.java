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

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Properties;
import javax.swing.Action;
import javax.swing.JMenuItem;
import org.netbeans.api.project.Project;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.filesystems.FileObject;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.ContextAwareAction;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.WeakListeners;
import org.openide.util.actions.BooleanStateAction;
import org.openide.util.actions.Presenter;
import org.openide.util.actions.SystemAction;
import static sk.arsi.cn1.offline.android.actions.InitAndroidProjectAction.openProjectBuildProperties;
import sk.arsi.cn1.offline.android.explorer.AndroidExplorerFactory;

/**
 *
 * @author arsi
 */
@ActionID(
        category = "AndroidOfflineSub/SubActions",
        id = "sk.arsi.cn1.offline.android.actions.UrRefreshAndroidStubAction"
)
@ActionRegistration(
        displayName = "", lazy = false
)
public class UrRefreshAndroidStubAction extends BooleanStateAction implements ContextAwareAction {
    public static final String ARSIREGENERATESTUB = "arsi.regenerate.stub";
    @Override
    public boolean isEnabled() {
        return false;
    }

    public String getName() {
        return "Generate Android Stub on src sync";
    }

    public HelpCtx getHelpCtx() {
        return null;
    }

    public Action createContextAwareInstance(Lookup actionContext) {
        Node node = actionContext.lookup(Node.class);
        if (node != null) {
            FileObject fob = node.getLookup().lookup(FileObject.class);
            if (fob != null) {
                FileObject fileObject = fob.getFileObject("build", "gradle");
                if (fileObject != null) {
                    RefreshAndroidStubAwareAction a = new RefreshAndroidStubAwareAction(node);
                    return a;
                }
            }
        }
        return this;
    }

    private class RefreshAndroidStubAwareAction implements Action, Presenter.Menu, Presenter.Popup, PropertyChangeListener {

        private final HiddenRefreshAndroidStubAction hba;
        private Properties properties;
        private Project project;

        public RefreshAndroidStubAwareAction(Node node) {
            hba = SystemAction.get(HiddenRefreshAndroidStubAction.class);
            if ((node instanceof FilterNode)) {
                Node original = node.getLookup().lookup(AndroidExplorerFactory.AndroidNode.class);
                if (original instanceof AndroidExplorerFactory.AndroidNode) {
                    AndroidExplorerFactory.AndroidNode nodeAndr = (AndroidExplorerFactory.AndroidNode) original;
                    project = (Project) nodeAndr.getProjectAndroid();
                    properties = openProjectBuildProperties(project);
                    String mainName = properties.getProperty(ARSIREGENERATESTUB, "true");
                    hba.setBooleanState("true".equals(mainName));
                }
            }
            hba.addPropertyChangeListener(WeakListeners.propertyChange(this, hba));
        }


        @Override
        public Object getValue(String key) {
            return hba.getValue(key);
        }

        @Override
        public void putValue(String key, Object value) {
            hba.putValue(key, value);
        }

        public void setEnabled(boolean b) {
            //BreakpointEnableAction.this.setEnabled(b);
        }

        public boolean isEnabled() {
            return true;
        }

        @Override
        public void addPropertyChangeListener(PropertyChangeListener listener) {
            hba.addPropertyChangeListener(listener);
        }

        @Override
        public void removePropertyChangeListener(PropertyChangeListener listener) {
            hba.removePropertyChangeListener(listener);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            //perform();
        }

        @Override
        public JMenuItem getMenuPresenter() {
            return hba.getMenuPresenter();
        }

        @Override
        public JMenuItem getPopupPresenter() {
            return hba.getPopupPresenter();
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if ("booleanState".equals(evt.getPropertyName())) {
                if (properties != null) {
                    properties.setProperty(ARSIREGENERATESTUB, "" + hba.getBooleanState());
                    InitAndroidProjectAction.storeProjectBuildProperties(project, properties);
                }
            }
        }

    }

    private static class HiddenRefreshAndroidStubAction extends BooleanStateAction {

        public HiddenRefreshAndroidStubAction() {
            setEnabled(true);
        }

        @Override
        public String getName() {
            return "Generate Android Stub on src sync";
        }

        @Override
        public HelpCtx getHelpCtx() {
            return null;
        }

        @Override
        public void actionPerformed(ActionEvent ev) {
            super.actionPerformed(ev);
        }

    }

}
