/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package sk.arsi.cn1.offline.android.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;
import org.openide.util.Utilities;
import org.openide.util.actions.Presenter;

/**
 *
 * @author arsi
 */
@ActionID(
        category = "AndroidOffline",
        id = "sk.arsi.cn1.offline.android.actions.PopupAction"
)
@ActionRegistration(
        displayName = "#CTL_PopupAction", lazy = false
)
@ActionReferences({
    @ActionReference(path = "AndroidOffline")
})
@Messages("CTL_PopupAction=Android offline")
public class PopupAction extends AbstractAction implements ActionListener, Presenter.Popup {

    @Override
    public void actionPerformed(ActionEvent e) {
        //NOP
    }

    @Override
    public JMenuItem getPopupPresenter() {
        JMenu main = new JMenu(Bundle.CTL_PopupAction());
        List<? extends Action> actionsForPath = Utilities.actionsForPath("Actions/AndroidOfflineSub/SubActions");
        for (Action action : actionsForPath) {
            main.add(action);
        }
        return main;
    }
}
