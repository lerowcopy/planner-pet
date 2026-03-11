package com.example.pet.presentation.taskdetail

enum class TaskDayStatus {
    TODAY, TOMORROW, UPCOMING, OVERDUE
}

fun getTaskDayStatus(day: String): TaskDayStatus {
    return try {
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        val taskDate = sdf.parse(day) ?: return TaskDayStatus.UPCOMING

        val today = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, 0)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }

        val tomorrow = (today.clone() as java.util.Calendar).apply {
            add(java.util.Calendar.DAY_OF_MONTH, 1)
        }

        val taskCal = java.util.Calendar.getInstance().apply {
            time = taskDate
            set(java.util.Calendar.HOUR_OF_DAY, 0)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }

        when {
            taskCal.timeInMillis == today.timeInMillis    -> TaskDayStatus.TODAY
            taskCal.timeInMillis == tomorrow.timeInMillis -> TaskDayStatus.TOMORROW
            taskCal.timeInMillis < today.timeInMillis     -> TaskDayStatus.OVERDUE
            else                                          -> TaskDayStatus.UPCOMING
        }
    } catch (e: Exception) {
        TaskDayStatus.UPCOMING
    }
}

fun formatTaskDay(day: String): String {
    return try {
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        val taskDate = sdf.parse(day) ?: return day

        val today = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, 0)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }

        val tomorrow = (today.clone() as java.util.Calendar).apply {
            add(java.util.Calendar.DAY_OF_MONTH, 1)
        }

        val taskCal = java.util.Calendar.getInstance().apply {
            time = taskDate
            set(java.util.Calendar.HOUR_OF_DAY, 0)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }

        when (taskCal.timeInMillis) {
            today.timeInMillis    -> "Сегодня"
            tomorrow.timeInMillis -> "Завтра"
            else -> {
                val dayOfMonth = taskCal.get(java.util.Calendar.DAY_OF_MONTH)
                val dayOfWeek = java.text.SimpleDateFormat("EE", java.util.Locale("ru"))
                    .format(taskDate)
                    .replaceFirstChar { it.uppercaseChar() }
                "$dayOfMonth, $dayOfWeek"
            }
        }
    } catch (e: Exception) {
        day
    }
}