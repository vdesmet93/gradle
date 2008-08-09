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
package org.gradle.api;

import groovy.lang.Closure;

import java.util.List;
import java.util.Set;

/**
 * <p>A <code>Task</code> represents an single step of a build, such as compiling classes or generating javadoc.</p>
 *
 * <p>A <code>Task</code> is made up of a sequence of {@link org.gradle.api.TaskAction} objects. When the task is
 * executed, each of the actions is executed in turn, by calling {@link TaskAction#execute(Task,
 * org.gradle.execution.Dag)}.  You can add actions to a task by calling {@link #doFirst(TaskAction)} or {@link
 * #doLast(TaskAction)}.</p>
 *
 * <p>A task belongs to a {@link Project}. You can use the various methods on {@link Project} to create and lookup task
 * instances.</p>
 *
 * <h3>Dependencies</h3>
 *
 * <p>A task may have dependencies on other tasks. Gradle ensures that tasks are executed in dependency order, so that
 * the dependencies of a task are executed before the task is executed.  You can add dependencies to a task using {@link
 * #dependsOn(Object[])}.</p>
 *
 * <h3>Using a Task in the Build Script</h3>
 *
 * @author Hans Dockter
 */
public interface Task extends Comparable {
    public static final String TASK_NAME = "name";

    public static final String TASK_TYPE = "type";

    public static final String TASK_DEPENDS_ON = "dependsOn";

    public static final String TASK_OVERWRITE = "overwrite";

    public final static String AUTOSKIP_PROPERTY_PREFIX = "skip.";

    /**
     * </p>Returns the name of this task. The name uniquely identifies the task within its {@link Project}.</p>
     *
     * @return The name of the task. Never returns null.
     */
    String getName();

    /**
     * <p>Returns the {@link Project} which this task belongs to.</p>
     *
     * @return The project this task belongs to. Never returns null.
     */
    Project getProject();

    /**
     * <p>Returns the sequence of {@link TaskAction} objects which will be executed by this task, in the order of
     * execution.</p>
     *
     * @return The task actions in the order they are executed. Returns an empty list if this task has no actions.
     */
    List<TaskAction> getActions();

    /**
     * <p>Returns the paths of the tasks which this task depends on.</p>
     *
     * @return The paths of the tasks this task depends on. Returns an empty set if this task has no dependencies.
     */
    Set getDependsOn();

    /**
     * <p>Sets the paths of the tasks which this task depends on.</p>
     *
     * @param dependsOnTasks The set of task paths.
     */
    void setDependsOn(Set dependsOnTasks);

    /**
     * <p>Returns true if this task has been executed.</p>
     *
     * @return true if this task has been executed already, false otherwise.
     */
    boolean getExecuted();

    /**
     * <p>Returns the path of the task, which is a fully qualified name for the task. The path of a task is the path of
     * its {@link Project} plus the name of the task, separated by <code>:</code>.</p>
     *
     * @return the path of the task, which is equal to the path of the project plus the name of the task.
     */
    String getPath();

    /**
     * <p>Adds the given task paths to the dependsOn task paths of this task. If the task path is a relative path (which
     * means just a name of a task), the path is interpreted as relative to the path of the project belonging to this
     * task. If the given path is absolute, the given path is used as is. That way you can directly refer to tasks of
     * other projects in a multiproject build. Although the reccomended way of establishing cross project dependencies,
     * is via the {@link Project#dependsOn(String)} method of the task's {@link Project}.</p>
     *
     * @param paths The paths of the tasks to add dependencies to.
     * @return the task object this method is applied to
     */
    Task dependsOn(Object... paths);

    /**
     * <p>Adds the given {@link TaskAction} to the beginning of this task's action list.</p>
     *
     * @param action The action to add
     * @return the task object this method is applied to
     */
    Task doFirst(TaskAction action);

    /**
     * <p>Adds the given {@link TaskAction} to the end of this task's action list.</p>
     *
     * @param action The action to add.
     * @return the task object this method is applied to
     */
    Task doLast(TaskAction action);

    /**
     * <p>Removes all the actions of this task.</p>
     *
     * @return the task object this method is applied to
     */
    Task deleteAllActions();

    /**
     * <p>Returns if this task is enabled or not.</p>
     *
     * @see #setEnabled(boolean)
     */
    boolean getEnabled();

    /**
     * <p>Set the enabled state of a task. If a task is disabled none of the its actions are executed. Note that
     * disabling a task does not prevent the execution of the tasks which this task depends on.</p>
     *
     * @param enabled The enabled state of this task (true or false)
     */
    void setEnabled(boolean enabled);

    /**
     * <p>Applies the statements of the closure against this task object. The delegate object for the closure is set to
     * this task.</p>
     *
     * @param configureClosure The closure to be applied (can be null).
     * @return This task
     */
    Task configure(Closure configureClosure);

    /**
     * <p>Returns whether this task is dag neutral or not.</p>
     *
     * @see #setDagNeutral(boolean)
     */
    boolean isDagNeutral();

    /**
     * <p>Set's the dag neutral state of the task. The concept of dag neutrality is important to improve the
     * performance, when two primary tasks are executed as part of one build (e.g. <code>gradle clean install</code>).
     * Gradle guarantees that executing two tasks at once has the same behavior than executing them one after another.
     * If the execution of the first task changes the state of the task execution graph (e.g. if a task action changes a
     * project property), Gradle needs to rebuild the task execution graph before the execution of the second task. If
     * the first task plus all its dependent tasks declare themselves as dag neutral, Gradle does not rebuild the
     * graph.</p>
     */
    void setDagNeutral(boolean dagNeutral);

    /**
     * <p>Returns the list of skip properties. The returned list can be used to add further skip properties. If a system
     * property with the same key as one of the skip properties is set to a value different than <i>false</i>, none of
     * the task actions are executed. It has the same effect as disabling the task. Therefore when starting gradle it is
     * enough to say <code>gradle -Dskip.test</code> to skip a task. You may, but don't need to assign a value.</p>
     *
     * @return List of skip properties. Returns empty list when no skip properties are assigned.
     */
    List<String> getSkipProperties();
}

