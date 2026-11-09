package com.example.uniflow

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp



@Composable
fun ScheduleHeader(day: String) {

    Column(
        modifier = Modifier.padding(bottom = 8.dp)
    ) {
        Text(day)
    }
}
@Composable
fun LessonsList(
    lessons: List<LessonInfo>,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier // сюда передается модифаер который прилетает в функцию LessonsList
    ) {
        items(lessons) { lesson -> // умная отрисовка с помощью items
            Lesson(lesson)
        }
    }
}

@Composable
fun Lesson(lesson: LessonInfo) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(12.dp)
        ) {
            Text(lesson.time)

            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    lesson.subject,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    lesson.auditorium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
