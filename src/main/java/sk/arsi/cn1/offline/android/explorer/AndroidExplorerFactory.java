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
package sk.arsi.cn1.offline.android.explorer;

import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.apache.commons.io.IOUtils;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.gradle.project.NbGradleProjectFactory;
import org.netbeans.spi.java.project.support.ui.PackageView;
import org.netbeans.spi.project.ProjectState;
import org.netbeans.spi.project.ui.LogicalViewProvider;
import org.netbeans.spi.project.ui.support.NodeFactory;
import org.netbeans.spi.project.ui.support.NodeList;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.Utilities;
import org.openide.util.WeakListeners;
import org.openide.util.lookup.InstanceContent;

/**
 *
 * @author arsi
 */
@NodeFactory.Registration(projectType = "org-netbeans-modules-java-j2seproject", position = 600)
public class AndroidExplorerFactory implements NodeFactory {

    @StaticResource
    private static final String ANDROID_ICON_RES = "sk/arsi/cn1/offline/16_android.gif";
    public static final Image ANDROID_ICON = ImageUtilities.loadImage(ANDROID_ICON_RES);
    private final InstanceContent instanceContent = new InstanceContent();

    public AndroidExplorerFactory() {
    }

    @Override
    public NodeList<?> createNodes(Project p) {
        return new AndroidFolderList(p);
    }

    private final List<ChangeListener> listeners = new ArrayList<>();

    private class AndroidFolderList implements NodeList<FileObject> {

        private final Project project;
        private FileObject androidDir;
        private AndroidNode node;

        public AndroidFolderList(Project project) {
            this.project = project;
        }

        @Override
        public List<FileObject> keys() {
            List<FileObject> fileObjects = new ArrayList<FileObject>();
            FileObject projectDir = project.getProjectDirectory();
            FileObject cn1PropertiesFile = projectDir.getFileObject("codenameone_settings.properties");
            if (cn1PropertiesFile != null) {
                try {
                    androidDir = FileUtil.createFolder(projectDir, "android");

                    fileObjects.add(androidDir);
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            return fileObjects;
        }

        @Override
        public void addChangeListener(ChangeListener l) {
            listeners.add(l);
        }

        @Override
        public void removeChangeListener(ChangeListener l) {
            listeners.remove(l);
        }

        @Override
        public Node node(FileObject key) {
            try {
                DataObject dob = DataObject.find(key);
                if (node != null) {
                    instanceContent.remove(node);
                }
                node = new AndroidNode(dob.getNodeDelegate(), androidDir, project);
                instanceContent.add(node);
                return node;
            } catch (DataObjectNotFoundException ex) {
                Exceptions.printStackTrace(ex);
            }
            return null;
        }

        @Override
        public void addNotify() {
        }

        @Override
        public void removeNotify() {
        }

    }

    public class AndroidNode extends FilterNode implements ChangeListener, PropertyChangeListener {

        final FileObject directory;
        Project projectGradle;
        final Project projectAndroid;
        LogicalViewProvider viewProvider;
        Node node;

        private final String srcPath;
        private final String nativePath;

        public AndroidNode(Node original, FileObject directory, Project projectAndroid) {
            super(original, new AndroidDirectoryFilter(original));
            this.directory = directory;
            this.projectAndroid = projectAndroid;
            srcPath = directory.getParent().getPath() + File.separator + "src";
            nativePath = directory.getParent().getPath() + File.separator + "native" + File.separator + "android";
            DataObject.Registry registries = DataObject.getRegistry();
            registries.addChangeListener(WeakListeners.change(this, registries));
        }

        @Override
        public Image getIcon(int type) {
            return ANDROID_ICON; //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public String getDisplayName() {
            return "Android offline"; //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public Image getOpenedIcon(int type) {
            return getIcon(type); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public Action[] getActions(boolean context) {
            if (directory.getFileObject("build", "gradle") == null) {
                List<? extends Action> rootActions = Utilities.actionsForPath("Actions/AndroidOfflineNode");
                return rootActions.toArray(new Action[rootActions.size()]);
            } else {
                if (projectGradle == null) {
                    try {
                        projectGradle = ProjectManager.getDefault().findProject(directory);
                        if (projectGradle == null) { //for first sub project open
                            NbGradleProjectFactory factory = new NbGradleProjectFactory();
                            projectGradle = factory.loadProject(directory, new ProjectState() {
                                @Override
                                public void markModified() {
                                }

                                @Override
                                public void notifyDeleted() throws IllegalStateException {
                                }
                            });
                        }
                        viewProvider = projectGradle.getLookup().lookup(LogicalViewProvider.class);
                        node = viewProvider.createLogicalView();
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    } catch (IllegalArgumentException ex) {
                        Exceptions.printStackTrace(ex);
                    }

                }
                List<Action> asList = new ArrayList<>(Arrays.asList(node.getActions(context)));
                asList.addAll(0, Utilities.actionsForPath("Actions/AndroidOffline"));
                return asList.toArray(new Action[asList.size()]);
            }
        }

        private final Map<DataObject, Object> registeredSrc = new WeakHashMap<>();
        private final Map<DataObject, Object> registeredNative = new WeakHashMap<>();

        @Override
        public void stateChanged(ChangeEvent e) {
            Object source = e.getSource();
            if ((source instanceof HashSet)) {
                HashSet<DataObject> dobs = (HashSet<DataObject>) source;
                for (DataObject next : dobs) {
                    if (next.getPrimaryFile().getPath().startsWith(srcPath) && !registeredSrc.containsKey(next)) {
                        next.addPropertyChangeListener(WeakListeners.propertyChange(this, next));
                        registeredSrc.put(next, null);
                    } else if (next.getPrimaryFile().getPath().startsWith(nativePath) && !registeredNative.containsKey(next)) {
                        next.addPropertyChangeListener(WeakListeners.propertyChange(this, next));
                        registeredNative.put(next, null);
                    }
                }
            }
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (evt.getPropertyName() == null ? DataObject.PROP_MODIFIED == null : evt.getPropertyName().equals(DataObject.PROP_MODIFIED) && ((Boolean) evt.getNewValue()) == false) {
                DataObject dob = (DataObject) evt.getSource();
                //sync on save src
                if (registeredSrc.containsKey(dob)) {
                    try {
                        InputStream is = dob.getPrimaryFile().getInputStream();
                        FileObject out = FileUtil.createData(new File(directory.getPath() + File.separator + "src" + File.separator + "main" + File.separator + "java" + File.separator + dob.getPrimaryFile().getPath().replaceFirst(srcPath + File.separator, "")));
                        OutputStream outputStream = out.getOutputStream();
                        IOUtils.copy(is, outputStream);
                        is.close();
                        outputStream.close();
                        FileUtil.refreshFor(new File(srcPath));
                    } catch (FileNotFoundException ex) {
                        Exceptions.printStackTrace(ex);
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                } else if (registeredNative.containsKey(dob)) {
                    //sync on save native
                    try {
                        InputStream is = dob.getPrimaryFile().getInputStream();
                        FileObject out = FileUtil.createData(new File(directory.getPath() + File.separator + "native" + File.separator + "android" + File.separator + dob.getPrimaryFile().getPath().replaceFirst(nativePath + File.separator, "")));
                        OutputStream outputStream = out.getOutputStream();
                        IOUtils.copy(is, outputStream);
                        is.close();
                        outputStream.close();
                        FileUtil.refreshFor(new File(nativePath));
                    } catch (FileNotFoundException ex) {
                        Exceptions.printStackTrace(ex);
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            }
        }

        public Project getProjectAndroid() {
            return projectAndroid;
        }

        public Project getProjectGradle() {
            return projectGradle;
        }

        public FileObject getDirectory() {
            return directory;
        }

    }

    private class AndroidDirectoryFilter extends FilterNode.Children {

        public AndroidDirectoryFilter(Node or) {
            super(or);
        }

        @Override
        protected Node[] createNodes(Node key) {
            FileObject fo = key.getLookup().lookup(FileObject.class);
            if (fo != null && fo.getName().equalsIgnoreCase("src") && fo.isFolder()) {
                return new Node[]{new SrcNode(key)};
            }
            return super.createNodes(key); //To change body of generated methods, choose Tools | Templates.
        }

    }

    private class SrcNode extends FilterNode {

        public SrcNode(Node original) {
            super(original, new SrcFilter(original));
        }

    }

    private class SrcFilter extends FilterNode.Children {

        public SrcFilter(Node or) {
            super(or);
        }

        @Override
        protected Node[] createNodes(Node key) {
            FileObject fo = key.getLookup().lookup(FileObject.class);
            if (fo != null && (fo.getName().equalsIgnoreCase("main") || fo.getName().equalsIgnoreCase("androidTest") || fo.getName().equalsIgnoreCase("test")) && fo.isFolder()) {
                return new Node[]{new MainNode(key)};
            }
            return super.createNodes(key); //To change body of generated methods, choose Tools | Templates.
        }


    }

    private class MainNode extends FilterNode {

        public MainNode(Node original) {
            super(original, new MainFilter(original));
        }

    }

    private class MainFilter extends FilterNode.Children {

        public MainFilter(Node or) {
            super(or);
        }

        @Override
        protected Node[] createNodes(Node key) {
            final FileObject fo = key.getLookup().lookup(FileObject.class);
            if (fo != null && fo.getName().equalsIgnoreCase("java") && fo.isFolder()) {
                return new Node[]{PackageView.createPackageView(new SourceGroup() {
                    @Override
                    public FileObject getRootFolder() {
                        return fo;
                    }

                    @Override
                    public String getName() {
                        return "java";
                    }

                    @Override
                    public String getDisplayName() {
                        return "java";
                    }

                    @Override
                    public Icon getIcon(boolean opened) {
                        return null;
                    }

                    @Override
                    public boolean contains(FileObject file) {
                        return file.getPath().startsWith(fo.getPath());
                    }

                    @Override
                    public void addPropertyChangeListener(PropertyChangeListener listener) {
                    }

                    @Override
                    public void removePropertyChangeListener(PropertyChangeListener listener) {
                    }
                })};
            }
            return super.createNodes(key); //To change body of generated methods, choose Tools | Templates.
        }
    }


}
