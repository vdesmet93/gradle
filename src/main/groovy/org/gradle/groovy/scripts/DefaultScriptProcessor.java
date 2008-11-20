/*
 * Copyright 2007-2008 the original author or authors.
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
package org.gradle.groovy.scripts;

import groovy.lang.Script;
import org.gradle.CacheUsage;
import org.gradle.api.Project;
import org.gradle.util.GUtil;

import java.io.File;

/**
 * @author Hans Dockter
 */
public class DefaultScriptProcessor implements IScriptProcessor {
    private final IScriptHandler scriptHandler;
    private final CacheUsage cacheUsage;

    public DefaultScriptProcessor(IScriptHandler scriptHandler, CacheUsage cacheUsage) {
        this.scriptHandler = scriptHandler;
        this.cacheUsage = cacheUsage;
    }

    public <T extends ScriptWithSource> T createScript(ScriptSource source, ClassLoader classLoader,
                                                       Class<T> scriptBaseClass) {
        File sourceFile = source.getSourceFile();
        ScriptWithSource script;
        if (isCacheable(sourceFile)) {
            script = (ScriptWithSource) loadViaCache(source, classLoader, scriptBaseClass);
        }
        else {
            script = (ScriptWithSource) loadWithoutCache(source, classLoader, scriptBaseClass);
        }
        script.setSource(source);
        return (T) script;
    }

    private Script loadWithoutCache(ScriptSource source, ClassLoader classLoader, Class<? extends Script> scriptBaseClass) {
        String text = source.getText();
        if (!GUtil.isTrue(text)) {
            return new EmptyScript();
        }

        return scriptHandler.createScript(text, classLoader, source.getClassName(), scriptBaseClass);
    }

    private Script loadViaCache(ScriptSource source, ClassLoader classLoader, Class<? extends Script> scriptBaseClass) {
        File sourceFile = source.getSourceFile();
        File cacheDir = new File(sourceFile.getParentFile(), Project.CACHE_DIR_NAME);
        File scriptCacheDir = new File(cacheDir, sourceFile.getName());
        String scriptClassName = source.getClassName();
        if (cacheUsage == CacheUsage.ON) {
            Script cachedScript = scriptHandler.loadFromCache(sourceFile.lastModified(), classLoader, scriptClassName, scriptCacheDir, scriptBaseClass);
            if (cachedScript != null) {
                return cachedScript;
            }
        }
        return scriptHandler.writeToCache(source.getText(), classLoader, scriptClassName, scriptCacheDir, scriptBaseClass);
    }

    private boolean isCacheable(File sourceFile) {
        return cacheUsage != CacheUsage.OFF && sourceFile != null && sourceFile.isFile();
    }

    public IScriptHandler getScriptHandler() {
        return scriptHandler;
    }
}
