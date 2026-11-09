package com.example.uniflow

import com.example.uniflow.ui.theme.UniFlowTheme

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            UniFlowTheme {
                ScheduleScreen(getLessons())
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
//Scaffold сам знает размеры своих слотов вроде topBar
// и bottomBar и на их основе вычисляет innerPadding.
fun ScheduleScreen(
    lessons: List<LessonInfo>
) {

    var selectedScreen by rememberSaveable {
        mutableStateOf(Screen.Schedule)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    when (selectedScreen) {
                        Screen.Schedule -> {
                            Text(
                                "Расписание",
                                style = MaterialTheme.typography.titleLarge
                            )
                        }

                        Screen.Tasks -> {
                            Text(
                                "Задания",
                                style = MaterialTheme.typography.titleLarge
                            )
                        }
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedScreen == Screen.Schedule,
                    onClick = {
                        selectedScreen = Screen.Schedule
                    },
                    icon = {
                        Icon(Icons.Default.DateRange, contentDescription = null)
                    },
                    label = {
                        Text("Расписание")
                    }
                )

                NavigationBarItem(
                    selected = selectedScreen == Screen.Tasks,
                    onClick = {
                        selectedScreen = Screen.Tasks
                    },
                    icon = {
                        Icon(Icons.AutoMirrored.Filled.List, contentDescription = null)
                    },
                    label = {
                        Text("Задания")
                    }
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            when (selectedScreen) {
                Screen.Schedule -> {
                    ScheduleHeader("Четверг")

                    LessonsList(
                        lessons,
                        modifier = Modifier.weight(1f)
                    )
                }

                Screen.Tasks -> {
                    TasksScreen(
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun ScheduleScreenPreview() {
    UniFlowTheme {
        ScheduleScreen(getLessons())
    }
}

@Preview(showBackground = true)
@Composable
fun TasksScreenPreview() {
    UniFlowTheme {
        TasksList(
            tasks = getMockTasks(),
            onTaskCheckedChange = { _, _ -> },
            onTaskDelete = {},
            enabled = true
        )
    }
}
