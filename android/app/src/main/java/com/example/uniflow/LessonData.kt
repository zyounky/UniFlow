package com.example.uniflow

fun getLessons(): List<LessonInfo> {

    val lessons = listOf (
        LessonInfo(
            time = "12:00",
            subject = "Физика",
            auditorium = "ауд. 214"
        ),

        LessonInfo(
            time = "14:30",
            subject = "Программирование",
            auditorium = "ауд. 405"
        ),

        LessonInfo(
            time = "16:10",
            subject = "Английский язык",
            auditorium = "ауд. 118"
        )
    )
    return lessons
}

fun getMockTasks(): List<TaskInfo> {
    val tasks = listOf(
        TaskInfo(
            subject = "Математический анализ",
            title = "Решить №2816 и №2853",
            deadline = "14 сентября",
            isCompleted = false,
            id = 1
        ),

        TaskInfo(
            subject = "Программирование",
            title = "Дописать лабораторную работу №2",
            deadline = "16 сентября",
            isCompleted = false,
            id = 2
        ),

        TaskInfo(
            subject = "Английский язык",
            title = "Подготовить презентацию",
            deadline = "18 сентября",
            isCompleted = false,
            id = 3
        ),

        TaskInfo(
            subject = "Физика",
            title = "Оформить отчёт по лабораторной",
            deadline = "20 сентября",
            isCompleted = false,
            id = 4
        )
    )
    return tasks
}