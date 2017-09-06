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

package sk.arsi.cn1.offline.android.actions.gui;

import org.netbeans.modules.editor.NbEditorKit;

/**
 *
 * @author arsi
 */
public class EditorKit extends NbEditorKit {

    public static final String MIME_TYPE = "text/x-diff"; // NOI18N

    /**
     * Creates a new instance of ManifestEditorKit
     */
    public EditorKit() {
    }

    /**
     * Create a syntax object suitable for highlighting Manifest file syntax
     */
//    @Override
//    public Syntax createSyntax(Document doc) {
//        return new DiffSyntax();
//    }

    /**
     * Retrieves the content type for this editor kit
     */
    public String getContentType() {
        return MIME_TYPE;
    }
}
