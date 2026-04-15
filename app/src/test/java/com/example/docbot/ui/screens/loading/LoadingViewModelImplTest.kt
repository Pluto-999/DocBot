package com.example.docbot.ui.screens.loading

import com.example.docbot.data.message.generation.MessageGenerator
import com.example.docbot.ui.screens.loading.initialiser.ModelInitialiser
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.io.File

class LoadingViewModelImplTest {

    private lateinit var messageGeneratorMock: MessageGenerator
    private lateinit var modelInitialiserMock: ModelInitialiser
    private lateinit var viewModel: LoadingViewModel

    @Before
    fun setUp() {
        messageGeneratorMock = mockk<MessageGenerator>()
        modelInitialiserMock = mockk<ModelInitialiser>()

        viewModel = LoadingViewModelImpl(messageGeneratorMock, modelInitialiserMock)
    }


    /****/

    @Test
    fun testInitialiseCallsModelInitialiserAndMessageGenerator() = runTest {
        val fakeFilesDirectory = File("/fake/dir")
        coEvery { modelInitialiserMock.initialiseModel(fakeFilesDirectory) } just runs
        coEvery { messageGeneratorMock.initialiseEngine() } just runs

        viewModel.initialise(fakeFilesDirectory)

        coVerify { modelInitialiserMock.initialiseModel(fakeFilesDirectory) }
        coVerify { messageGeneratorMock.initialiseEngine() }
    }
}