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
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
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
public class PopupAction extends AbstractAction implements ActionListener, Presenter.Popup, ContextAwareAction {

    private final Lookup actionContext;

    public PopupAction() {
        actionContext = null;
    }

    public PopupAction(Lookup actionContext) {
        this.actionContext = actionContext;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        //NOP
    }

    @Override
    public JMenuItem getPopupPresenter() {
        JMenu main = new JMenu(Bundle.CTL_PopupAction());
        List<? extends Action> actionsForPath = Utilities.actionsForPath("Actions/AndroidOfflineSub/SubActions");
        for (Action action : actionsForPath) {
            if (action instanceof ContextAwareAction) {
                action = ((ContextAwareAction) action).createContextAwareInstance(actionContext);
            }
            if (action instanceof Presenter.Popup) {
                main.add(((Popup) action).getPopupPresenter());
            } else {
                main.add(action);
            }
        }
        return main;
    }

    @Override
    public Action createContextAwareInstance(Lookup actionContext) {
        return new PopupAction(actionContext);
    }
}
