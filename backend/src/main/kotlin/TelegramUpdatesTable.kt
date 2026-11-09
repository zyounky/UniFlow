package com.example.uniflow

import org.jetbrains.exposed.v1.core.Table

internal object TelegramUpdatesTable : Table("telegram_updates") {
    val updateId = long("update_id")
    val requestJson = text("request_json")
    val responseJson = text("response_json").nullable()

    override val primaryKey = PrimaryKey(updateId)
}
