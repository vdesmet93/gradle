/*
 * Copyright 2007 the original author or authors.
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

package org.gradle.build.release

import org.gradle.api.GradleException
import org.gradle.api.Project
import org.tigris.subversion.javahl.Status
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory
import org.tmatesoft.svn.core.javahl.SVNClientImpl
import org.tmatesoft.svn.core.wc.SVNClientManager
import org.tmatesoft.svn.core.wc.SVNStatusClient
import org.tmatesoft.svn.core.wc.SVNWCUtil

/**
 * @author Hans Dockter
 */
class Svn {
    Project project
    SVNClientManager clientManager
    SVNStatusClient statusClient
    SVNClientImpl javaHlClient

    Svn(Project project) {
        assert project
        this.project = project
        DAVRepositoryFactory.setup();
        clientManager = SVNClientManager.newInstance(
                SVNWCUtil.createDefaultOptions(true), project.codehausUserName, project.codehausUserPassword)
        statusClient = clientManager.getStatusClient()
        javaHlClient = SVNClientImpl.newInstance()
    }

    def release() {
        checkWorkingCopy()
        if (svn.isTrunk() || version.toString() == branchVersion()) {
            majorOrMinorRelease()
        } else {
            revisionRelease()
        }
    }

    def majorOrMinorRelease() {
        if (isTrunk()) {
            createReleaseBranchDirectory()
        }
        version.storeCurrentVersion()
        commitProperties()
        if (isTrunk()) {
            copyTrunkToReleaseBranch()
        }
        tagReleaseBranch()
    }

    def revisionRelease() {
        version.storeCurrentVersion()
        commitProperties()
        tagReleaseBranch()
    }


    def commitProperties() {
        File props = new File(project.projectDir, 'gradle.properties')
        javaHlClient.add(props.absolutePath, false)
        javaHlClient.commit([props.absolutePath,] as String[], "Incremented version properties", false)
    }

    def createReleaseBranchDirectory() {
        javaHlClient.mkdir([releaseBranchUrl] as String[], "Create new release branch $releaseBranchName")
    }

    def copyTrunkToReleaseBranch() {
        javaHlClient.copy(trunkUrl, releaseBranchUrl, "Copy trunk to release branch $releaseBranchName")
    }

    def tagReleaseBranch() {
        javaHlClient.copy(releaseBranchUrl, createUrl(svnUrl, "REL-$version"), "Tag release $releaseBranchName")
    }

    def getReleaseBranchName() {
        'RB-' + version
    }

    def getReleaseBranchUrl() {
        createUrl(svnUrl, releaseBranchName)
    }

    def getTrunkUrl() {
        createUrl(svnUrl, 'trunk')
    }

    String getSvnUrl() {
        statusClient.doStatus(project.projectDir, false).URL.toString()
    }

    def createUrl(String url, String newSuffix) {
        int pos = url.lastIndexOf('/')
        url.substring(0, pos + 1) + newSuffix
    }

    boolean isTrunk() {
        svnDir == 'trunk'
    }

    String getSvnDir() {
        svnUrl.substring(svnUrl.lastIndexOf('/') + 1)
    }

    String getBranchVersion() {
        svnDir.replaceFirst('RB-', '')
    }

    void checkWorkingCopy() {
        List statuses = javaHlClient.status(project.projectDir.absolutePath, true, true, false)
//        ISVNStatusHandler statusHandler = {SVNStatus status -> statuses << status} as ISVNStatusHandler
//        statusClient.doStatus(project.projectDir, true, true, false, false, statusHandler)
        if (statuses) {
            String result = ''
            statuses.each {Status status -> result += status.path + ' ' + status.url.toString() + '\n'}
            throw new GradleException("The working copy is not in sync with the SVN server. We can't release!\n$result")
        }
    }
}