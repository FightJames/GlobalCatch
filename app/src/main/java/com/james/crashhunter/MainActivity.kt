package com.james.crashhunter

import android.content.Intent
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
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.james.crashhunter.service.CrashService
import com.james.crashhunter.ui.theme.CrashHunterTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CrashHunterTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting2(
                        modifier = Modifier.padding(innerPadding),
                        {
                            Intent(this, CrashActivity::class.java).also {
                                startActivity(it)
                            }
                        },
                        {
                            Intent(this, ClickCrashActivity::class.java).also {
                                startActivity(it)
                            }
                        },
                        {
                            Intent(this, CrashService::class.java).also {
                                startService(it)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun Greeting2(
    modifier: Modifier = Modifier,
    gotoCrashActivity: () -> Unit,
    gotoClickCrashActivity: () -> Unit,
    gotoServiceCrashActivity: () -> Unit
) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier
                .wrapContentHeight()
                .width(IntrinsicSize.Max)
        ) {
            Button(onClick = gotoCrashActivity) {
                Text(
                    text = "go to CrashActivity",
                    modifier = Modifier.wrapContentSize()
                )
            }

            Button(onClick = gotoClickCrashActivity) {
                Text(
                    text = "go to ClickCrashActivity",
                    modifier = Modifier.wrapContentSize()
                )
            }

            Button(onClick = gotoServiceCrashActivity) {
                Text(
                    text = "start crash service",
                    modifier = Modifier.wrapContentSize()
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    CrashHunterTheme {
//        Greeting("Android")
    }
}