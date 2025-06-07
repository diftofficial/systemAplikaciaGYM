package com.example.systemaplikacia100.ui.screens

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.widget.RemoteViews
import com.example.systemaplikacia100.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class TrainingWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        val auth = FirebaseAuth.getInstance()
        val db = FirebaseFirestore.getInstance()

        for (appWidgetId in appWidgetIds) {
            // 1) Loading stav
            val loading = RemoteViews(context.packageName, R.layout.widget_layout)
            loading.setTextViewText(R.id.widget_username, "…")
            loading.setTextViewText(R.id.widget_text, "Body: načítavam…")
            appWidgetManager.updateAppWidget(appWidgetId, loading)

            // 2) Overenie prihlásenia
            val user = auth.currentUser
            if (user == null) {
                val notSigned = RemoteViews(context.packageName, R.layout.widget_layout)
                notSigned.setTextViewText(R.id.widget_username, "")
                notSigned.setTextViewText(R.id.widget_text, "Nie ste prihlásený")
                appWidgetManager.updateAppWidget(appWidgetId, notSigned)
                continue
            }

            // 3) Načítame údaje z Firestore
            db.collection("users")
                .document(user.uid)
                .get()
                .addOnSuccessListener { doc ->
                    // meno – môžeš vziať displayName alebo pole "name" z Firestore
                    val name = doc.getString("name") ?: user.displayName ?: "Používateľ"
                    val pts  = doc.getLong("points") ?: 0L

                    val views = RemoteViews(context.packageName, R.layout.widget_layout)
                    views.setTextViewText(R.id.widget_username,  "Vitaj $name")
                    views.setTextViewText(R.id.widget_text, "Body: $pts")
                    appWidgetManager.updateAppWidget(appWidgetId, views)
                }
                .addOnFailureListener {
                    val error = RemoteViews(context.packageName, R.layout.widget_layout)
                    error.setTextViewText(R.id.widget_username, "")
                    error.setTextViewText(R.id.widget_text, "Chyba načítania")
                    appWidgetManager.updateAppWidget(appWidgetId, error)
                }
        }
    }
}
