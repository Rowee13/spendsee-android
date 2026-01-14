package com.spendsee.ui.widgets

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.*
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.*
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.spendsee.managers.BudgetProgressData
import com.spendsee.managers.WidgetDataManager

class BudgetWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            BudgetWidgetContent(context)
        }
    }

    @Composable
    private fun BudgetWidgetContent(context: Context) {
        val widgetData = WidgetDataManager.getWidgetData(context)

        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(ColorProvider(Color(0xFF1E1E1E)))
                .padding(16.dp)
        ) {
            if (widgetData == null || !widgetData.isPremium) {
                // Premium required message
                Column(
                    modifier = GlanceModifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "\uD83D\uDD12",
                        style = TextStyle(
                            fontSize = 32.sp,
                            color = ColorProvider(Color(0xFFFFFFFF))
                        )
                    )
                    Spacer(modifier = GlanceModifier.height(8.dp))
                    Text(
                        text = "Premium Required",
                        style = TextStyle(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = ColorProvider(Color(0xFFFFFFFF))
                        )
                    )
                }
            } else {
                // Widget content
                Column(
                    modifier = GlanceModifier.fillMaxSize(),
                    horizontalAlignment = Alignment.Start
                ) {
                    // Header
                    Row(
                        modifier = GlanceModifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Budget Progress",
                            style = TextStyle(
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = ColorProvider(Color(0xFFFFFFFF))
                            )
                        )
                        Spacer(modifier = GlanceModifier.defaultWeight())
                        Text(
                            text = WidgetDataManager.getMonthYearText(),
                            style = TextStyle(
                                fontSize = 11.sp,
                                color = ColorProvider(Color(0xFF888888))
                            )
                        )
                    }

                    Spacer(modifier = GlanceModifier.height(16.dp))

                    // Budget list
                    if (widgetData.budgets.isEmpty()) {
                        // Empty state
                        Column(
                            modifier = GlanceModifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "No budgets",
                                style = TextStyle(
                                    fontSize = 12.sp,
                                    color = ColorProvider(Color(0xFF888888))
                                )
                            )
                            Text(
                                text = "Create budgets in the app",
                                style = TextStyle(
                                    fontSize = 11.sp,
                                    color = ColorProvider(Color(0xFF666666))
                                )
                            )
                        }
                    } else {
                        Column(
                            modifier = GlanceModifier.fillMaxWidth()
                        ) {
                            widgetData.budgets.take(5).forEach { budget ->
                                BudgetProgressItem(budget, widgetData.currencySymbol)
                                Spacer(modifier = GlanceModifier.height(12.dp))
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun BudgetProgressItem(budget: BudgetProgressData, currencySymbol: String) {
        Column(
            modifier = GlanceModifier.fillMaxWidth()
        ) {
            // Budget name and amounts
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = budget.name,
                    style = TextStyle(
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = ColorProvider(Color(0xFFFFFFFF))
                    ),
                    modifier = GlanceModifier.defaultWeight()
                )
                Text(
                    text = "${WidgetDataManager.formatCurrency(budget.spent, currencySymbol)} / ${WidgetDataManager.formatCurrency(budget.planned, currencySymbol)}",
                    style = TextStyle(
                        fontSize = 10.sp,
                        color = ColorProvider(Color(0xFF888888))
                    )
                )
            }

            Spacer(modifier = GlanceModifier.height(6.dp))

            // Progress bar - showing percentage
            val progressWidth = (budget.percentage / 100f).coerceIn(0f, 1f)
            val isOverBudget = budget.spent > budget.planned

            Row(
                modifier = GlanceModifier.fillMaxWidth()
            ) {
                // Filled portion
                if (progressWidth > 0) {
                    Box(
                        modifier = GlanceModifier
                            .height(8.dp)
                            .defaultWeight()
                            .background(
                                ColorProvider(
                                    if (isOverBudget) Color(0xFFFF6B6B) else Color(0xFF007AFF)
                                )
                            ),
                        content = {}
                    )
                }
                // Unfilled portion
                if (progressWidth < 1.0f) {
                    Box(
                        modifier = GlanceModifier
                            .height(8.dp)
                            .defaultWeight()
                            .background(ColorProvider(Color(0xFF333333))),
                        content = {}
                    )
                }
            }

            Spacer(modifier = GlanceModifier.height(4.dp))

            // Percentage
            Text(
                text = "${budget.percentage.toInt()}%",
                style = TextStyle(
                    fontSize = 10.sp,
                    color = ColorProvider(
                        if (budget.spent > budget.planned) Color(0xFFFF6B6B) else Color(0xFF888888)
                    )
                )
            )
        }
    }
}

class BudgetWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = BudgetWidget()
}
