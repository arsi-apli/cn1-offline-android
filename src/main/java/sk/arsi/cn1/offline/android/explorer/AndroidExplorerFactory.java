/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sk.arsi.cn1.offline.android.explorer;

import java.awt.Image;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Action;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.api.project.Project;
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

/**
 *
 * @author arsi
 */
@NodeFactory.Registration(projectType = "org-netbeans-modules-java-j2seproject", position = 600)
public class AndroidExplorerFactory implements NodeFactory {

    @StaticResource
    private static final String ANDROID_ICON_RES = "sk/arsi/cn1/offline/16_android.gif";
    public static final Image ANDROID_ICON = ImageUtilities.loadImage(ANDROID_ICON_RES);

    public AndroidExplorerFactory() {
        System.out.println("sk.arsi.cn1.offline.android.explorer.AndroidExplorerFactory.<init>()");
    }

    @Override
    public NodeList<?> createNodes(Project p) {
        return new AndroidFolderList(p);
    }

    private class AndroidFolderList implements NodeList<FileObject> {

        private final Project project;

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
                    FileObject androidDir = FileUtil.createFolder(projectDir, "android");
                    fileObjects.add(androidDir);
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            return fileObjects;
        }

        @Override
        public void addChangeListener(ChangeListener l) {
        }

        @Override
        public void removeChangeListener(ChangeListener l) {
        }

        @Override
        public Node node(FileObject key) {
            try {
                DataObject dob = DataObject.find(key);
                return new AndroidNode(dob.getNodeDelegate());
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

    private class AndroidNode extends FilterNode {

        public AndroidNode(Node original) {
            super(original);
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
            List<? extends Action> rootActions = Utilities.actionsForPath("Actions/AndroidOfflineNode");
            return rootActions.toArray(new Action[rootActions.size()]);
        }


    }

}
