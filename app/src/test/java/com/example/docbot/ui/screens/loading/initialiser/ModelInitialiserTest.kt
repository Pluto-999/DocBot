package com.example.docbot.ui.screens.loading.initialiser

import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.io.File
import kotlin.io.path.createTempDirectory

class ModelInitialiserTest {

    private val fileName = "gemma-3n-E2B-it-int4.litertlm"

    private lateinit var sourceDirectory: File
    private lateinit var filesDirectory: File
    private lateinit var initialiser: ModelInitialiser

    @Before
    fun setUp() {
        sourceDirectory = createTempDirectory("source").toFile()
        filesDirectory = createTempDirectory("destination").toFile()
        initialiser = ModelInitialiser(sourceDirectory)
    }

    @After
    fun tearDown() {
        sourceDirectory.deleteRecursively()
        filesDirectory.deleteRecursively()
    }


    /****/

    @Test
    fun testModelDirectoryIsCreatedWhenTheModelIsNotInitialised() {
        initialiser.initialiseModel(filesDirectory)

        val modelDirectoryExists = File(filesDirectory, "slm").exists()
        assertTrue(modelDirectoryExists)
    }


    /****/

    @Test
    fun testSourceFileContentsAreCopiedToDestinationFileWhenDestinationDoesNotExist() {
        File(sourceDirectory, fileName).writeText("test")

        initialiser.initialiseModel(filesDirectory)

        val destinationContents = File(filesDirectory, "slm/$fileName").readText()
        assertEquals("test", destinationContents)
    }

    @Test
    fun testSourceFileContentsAreCopiedToDestinationFileWhenNotTheSameSize() {
        File(sourceDirectory, fileName).writeText("test test")

        val destinationFile = File(filesDirectory, "slm/$fileName")
        destinationFile.parentFile.mkdirs()
        destinationFile.writeText("test")

        initialiser.initialiseModel(filesDirectory)

        val destinationContents = File(filesDirectory, "slm/$fileName").readText()
        assertEquals("test test", destinationContents)
    }


    /****/

    @Test
    fun testSourceFileContentsAreNotCopiedWhenDestinationAlreadyMatches() {
        File(sourceDirectory, fileName).writeText("test")

        val destinationFile = File(filesDirectory, "slm/$fileName")
        destinationFile.parentFile.mkdirs()
        destinationFile.writeText("test")

        val lastModifiedBefore = destinationFile.lastModified()

        initialiser.initialiseModel(filesDirectory)

        val lastModifiedAfter = destinationFile.lastModified()

        assertEquals(lastModifiedBefore, lastModifiedAfter)
    }


    /****/

    @Test
    fun testIntialiseModelOnlyRunsOnce() {
        File(sourceDirectory, fileName).writeText("test 1")

        initialiser.initialiseModel(filesDirectory)

        File(sourceDirectory, fileName).writeText("test 2")

        initialiser.initialiseModel(filesDirectory)

        val destinationContents = File(filesDirectory, "slm/$fileName").readText()

        assertEquals("test 1", destinationContents)
    }
}