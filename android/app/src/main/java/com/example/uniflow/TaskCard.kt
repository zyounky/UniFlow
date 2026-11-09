package com.example.uniflow

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.uniflow.ui.theme.UniFlowTheme
import androidx.compose.material.icons.filled.Delete

@Composable
fun TaskCard(
    task: TaskInfo,
    onCheckedChange: (Boolean) -> Unit,
    onDelete: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {



    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(12.dp),
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    task.subject,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    task.title,
                    textDecoration = if (task.isCompleted) {
                        TextDecoration.LineThrough
                    }
                    else {
                        TextDecoration.None
                    }
                )
            }

            Text(
                task.deadline,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Checkbox(
                checked = task.isCompleted, // отвечает за текущее отображаемое состояние чекбокса.
                onCheckedChange = onCheckedChange, // onCheckedChange ожидает лямбду типа ((Boolean) -> Unit)
                enabled = enabled
            )

            IconButton(
                onClick = onDelete,
                enabled = enabled
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Удалить задачу",
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TaskCardPreview() {
    UniFlowTheme {
        TaskCard(
            TaskInfo(
                subject = "Математический анализ",
                title = "Решить №2816 и №2853",
                deadline = "14 сентября",
                isCompleted = false,
                id = 1
            ),
            onCheckedChange = {},
            onDelete = {},
            enabled = true
        )
    }
}
