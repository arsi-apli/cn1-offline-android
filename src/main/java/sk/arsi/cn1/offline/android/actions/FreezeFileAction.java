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

import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import java.util.List;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.ContextAwareAction;
import org.openide.util.HelpCtx;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.actions.BooleanStateAction;
import org.openide.util.actions.Presenter;
import sk.arsi.cn1.offline.android.explorer.FreezeProjectHook;

/**
 *
 * @author arsi
 */
public class FreezeFileAction extends BooleanStateAction implements ContextAwareAction {

    private static final String ANDROID_ICON_RES = "sk/arsi/cn1/offline/16_android.gif";
    public static final Image ANDROID_ICON = ImageUtilities.loadImage(ANDROID_ICON_RES);

    @Override
    public boolean isEnabled() {
        return false;
    }

    @Override
    public String getName() {
        return "Exclude from src sync";
    }

    @Override
    public HelpCtx getHelpCtx() {
        return null;
    }

    @Override
    public Action createContextAwareInstance(Lookup actionContext) {
        Node node = actionContext.lookup(Node.class);
        if (node != null) {
            FreezeFileAwareAction a = new FreezeFileAwareAction(node);
            return a;
        }
        return this;
    }

    private class FreezeFileAwareAction implements Action, Presenter.Menu, Presenter.Popup, ActionListener {

        private final Project project;
        private final FileObject fob;
        private final List<String> freezeList;
        private final JCheckBoxMenuItem menuItem = new JCheckBoxMenuItem("Exclude from sync", new ImageIcon(ANDROID_ICON));

        public FreezeFileAwareAction(Node node) {
            fob = node.getLookup().lookup(DataObject.class).getPrimaryFile();
            Project gradle = FileOwnerQuery.getOwner(fob);
            project = FileOwnerQuery.getOwner(gradle.getProjectDirectory().getParent());
            freezeList = FreezeProjectHook.freezeMap.get(project);
            menuItem.setState(freezeList.contains(fob.getPath()));

            FileObject parent = fob.getParent();
            menuItem.setEnabled(true);
            while (parent != null) {
                if (freezeList.contains(parent.getPath())) {
                    menuItem.setEnabled(false);
                    menuItem.setState(true);
                    break;
                }
                parent = parent.getParent();
            }
            menuItem.addActionListener(this);

        }

        @Override
        public Object getValue(String key) {
            return menuItem.getState();
        }

        @Override
        public void putValue(String key, Object value) {
        }

        public void setEnabled(boolean b) {
            //BreakpointEnableAction.this.setEnabled(b);
        }

        public boolean isEnabled() {
            return menuItem.isEnabled();
        }

        @Override
        public void addPropertyChangeListener(PropertyChangeListener listener) {
        }

        @Override
        public void removePropertyChangeListener(PropertyChangeListener listener) {
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (menuItem.getState()) {
                if (!freezeList.contains(fob.getPath())) {
                    FileObject parent = fob.getParent();
                    menuItem.setEnabled(true);
                    while (parent != null) {
                        if (freezeList.contains(parent.getPath())) {
                            return;
                        }
                        parent = parent.getParent();
                    }
                    freezeList.add(fob.getPath());
                }
            } else {
                freezeList.remove(fob.getPath());
            }

        }

        @Override
        public JMenuItem getMenuPresenter() {
            return menuItem;
        }

        @Override
        public JMenuItem getPopupPresenter() {
            return menuItem;
        }

    }

}
