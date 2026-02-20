package com.example.docbot

import android.app.Activity
import android.app.ComponentCaller
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import com.example.docbot.ui.navigation.Navigation
import com.example.docbot.ui.theme.DocBotTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DocBotTheme {
                Navigation()
            }
        }
    }

//    override fun onActivityResult(
//        requestCode: Int,
//        resultCode: Int,
//        data: Intent?,
//        caller: ComponentCaller
//    ) {
//        super.onActivityResult(requestCode, resultCode, data, caller)
//    }
//
////    fun getDocumentURI() {
////        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
////            addCategory(Intent.CATEGORY_OPENABLE)
////            type = "application/pdf"
////        }
////        startActivityForResult(intent, 2)
////
////        val startForResult = registerForActivityResult(
////            ActivityResultContracts.StartActivityForResult()) { result ->
////                if (result.resultCode == Activity.RESULT_OK) {
////                    val data: Intent? = result.data
////                }
////        }
////    }
//
//    fun openSomeActivityForResult() {
//        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
//            addCategory(Intent.CATEGORY_OPENABLE)
//            type = "application/pdf"
//        }
//        resultLauncher.launch(intent)
//    }
//
//    var resultLauncher = registerForActivityResult(
//        ActivityResultContracts.StartActivityForResult()
//    ) { result ->
//        if (result.resultCode == Activity.RESULT_OK) {
//            // There are no request codes
//            val data: Intent? = result.data
//
//        }
//    }
}