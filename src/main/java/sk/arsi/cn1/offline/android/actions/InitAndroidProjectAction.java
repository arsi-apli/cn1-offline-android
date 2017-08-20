/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package sk.arsi.cn1.offline.android.actions;

import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.actions.NodeAction;

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


    @Override
    protected void performAction(Node[] activatedNodes) {
    }

    @Override
    protected boolean enable(Node[] activatedNodes) {
        if (activatedNodes.length > 0) {
            FileObject fob = activatedNodes[0].getLookup().lookup(FileObject.class);
            if (fob != null) {
                FileObject fileObject = fob.getFileObject("android", "off");
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

}
