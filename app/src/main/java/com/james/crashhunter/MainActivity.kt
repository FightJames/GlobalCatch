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
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
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
    onClick: () -> Unit
) {
    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .wrapContentHeight()
                .width(IntrinsicSize.Max)
        ) {
            Button(onClick = onClick) {
                Text(
                    text = "go to CrashActivity",
                    modifier = modifier
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