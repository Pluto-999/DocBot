package com.example.docbot.data.sources

import com.example.docbot.data.models.MyObjectBox
import io.objectbox.BoxStore
import org.junit.After
import org.junit.Before
import java.io.File

open class ObjectBoxTest {

    protected lateinit var store: BoxStore

    @Before
    fun setUpObjectBox() {
        val testDirectory = File("test/test-db")

        BoxStore.deleteAllFiles(testDirectory)

        store = MyObjectBox.builder()
            .directory(testDirectory)
            .build()
    }

    @After
    fun tearDownObjectBox() {
        store.close()
        BoxStore.deleteAllFiles(File("test/test-db"))
    }
}