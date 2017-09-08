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
package sk.arsi.cn1.offline.android.explorer;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.netbeans.spi.project.ui.ProjectOpenedHook;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;

/**
 *
 * @author arsi
 */
@ProjectServiceProvider(service = ProjectOpenedHook.class, projectType = {"org-netbeans-modules-java-j2seproject"})
public class FreezeProjectHook extends ProjectOpenedHook {

    private final Project project;
    public static final Map<Project, List<String>> freezeMap = new ConcurrentHashMap<>();

    public FreezeProjectHook(Project project) {
        this.project = project;
    }

    @Override
    protected void projectOpened() {
        final FileObject projectDir = project.getProjectDirectory();
        FileObject aofo = projectDir.getFileObject("android-offline.excludes");
        FileObject cn1fo = projectDir.getFileObject("codenameone_settings.properties");
        if (cn1fo != null) {
            List<String> fos = new ArrayList<>();
            if (aofo != null) {
                try {

                    List<String> asLines = aofo.asLines();
                    for (String line : asLines) {
                        FileObject fo = projectDir.getFileObject(line);
                        if (fo != null) {
                            fos.add(fo.getPath());
                        }
                    }
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }

            }
            freezeMap.put(project, fos);
        }

    }

    @Override
    protected void projectClosed() {
        try {
            final FileObject projectDir = project.getProjectDirectory();
            FileObject aofo = FileUtil.createData(projectDir, "android-offline.excludes");
            if (freezeMap.containsKey(project)) {
                List<String> freezeList = freezeMap.get(project);
                List<String> lines = new ArrayList<>();
                for (String foff : freezeList) {
                    FileObject fo = FileUtil.toFileObject(new File(foff));
                    if (fo.isValid()) {
                        lines.add(fo.getPath().replace(projectDir.getPath(), "").replace("\\", "/"));
                    }
                }
                OutputStream outputStream = aofo.getOutputStream();
                PrintWriter writer = new PrintWriter(outputStream);
                for (String line : lines) {
                    writer.println(line);
                }
                writer.close();
                outputStream.close();
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
}
