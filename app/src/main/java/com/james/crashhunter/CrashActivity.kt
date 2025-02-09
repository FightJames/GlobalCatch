package com.james.crashhunter

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.tooling.preview.Preview
import com.james.crashhunter.ext.logd
import com.james.crashhunter.ui.theme.CrashHunterTheme

class CrashActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        logd("activityOnCreate")
        throw Exception("crash")
        enableEdgeToEdge()
        setContent {
            CrashHunterTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        throw Exception("Crash")
    }
}


@Preview(showBackground = true)
@Composable
fun GreetingPreview2() {
    CrashHunterTheme {
//        Greeting2("Android")
    }
}