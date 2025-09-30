package com.example.prottect_yourself.logic.email

import android.content.Context
import java.io.File

object EmailService {

    private const val TEMPLATE_FILE_NAME = "email_template.txt"
    private const val RECIPIENTS_FILE_NAME = "email_recipients.txt"

    fun saveTemplate(context: Context, template: String) {
        val file = File(context.filesDir, TEMPLATE_FILE_NAME)
        file.writeText(template)
    }

    fun loadTemplate(context: Context): String {
        val file = File(context.filesDir, TEMPLATE_FILE_NAME)
        return if (file.exists()) file.readText() else ""
    }

    fun saveEmails(context: Context, emails: List<String>) {
        val file = File(context.filesDir, RECIPIENTS_FILE_NAME)
        file.printWriter().use { out ->
            emails.forEach { email ->
                out.println(email)
            }
        }
    }

    fun loadEmails(context: Context): MutableList<String> {
        val file = File(context.filesDir, RECIPIENTS_FILE_NAME)
        return if (file.exists()) {
            file.readLines().toMutableList()
        } else {
            mutableListOf()
        }
    }
}