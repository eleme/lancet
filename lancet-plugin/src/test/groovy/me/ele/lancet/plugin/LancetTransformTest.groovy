package me.ele.lancet.plugin

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.junit.Before
import org.junit.Test

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS
import static org.junit.Assert.assertEquals
/**
 * Created by Jude on 2017/5/5.
 */
public class LancetTransformTest extends GroovyTestCase {

    def testsBuildDir = new File('lancet-plugin/build', 'testProject')
    def buildFile // Build file location

    List<File> pluginClasspath


    @Before
    public void setUp(){
        def pluginClasspathResource = getClass().classLoader.findResource("plugin-classpath.txt")
        if (pluginClasspathResource == null) {
            throw new IllegalStateException("Did not find plugin classpath resource, run `testClasses` build task.")
        }

        pluginClasspath = pluginClasspathResource.readLines().collect { new File(it) }
    }

    @Test
    public void testCommon() throws IOException {
        def testBuildDir = new File(testsBuildDir, name) // Defines an unique directory for every test
        testBuildDir.deleteDir() // Cleans the previous state. It is not in a cleanup block because I want to check the generated file
        testBuildDir.mkdirs()

        buildFile = new File(testBuildDir, 'build.gradle') // Build script which will be generated
        buildFile.createNewFile() // Create the file
        buildFile << "buildscript {\n" +
                "    repositories {\n" +
                "        jcenter()\n" +
                "    }\n" +
                "    dependencies {\n" +
                "        classpath \"com.android.tools.build:gradle:2.3.1\"\n" +
                "    }\n" +
                "}\n"


        buildFile << "plugins {\n" +
                "    id 'com.android.application'\n" +
                "    id 'me.ele.lancet'\n" +
                "}\n"
        buildFile << new File('testsample/build.gradle').text.replace("apply plugin: 'com.android.application'","")

        new AntBuilder().copy(todir: testBuildDir) {
            fileset(dir : 'testsample/src') {
                exclude(name:"*.*")
            }
        }

        BuildResult result = GradleRunner.create()
                .withProjectDir(testBuildDir)
                .withPluginClasspath(pluginClasspath)
                .withArguments("--stacktrace ")
                .withArguments("assembleDebug")
                .build();

        result.task()

        assertEquals(result.task(":assembleDebug").getOutcome(), SUCCESS);

    }
}