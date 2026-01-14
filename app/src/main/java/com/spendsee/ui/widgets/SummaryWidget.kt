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
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.spendsee.managers.WidgetDataManager

class SummaryWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            SummaryWidgetContent(context)
        }
    }

    @Composable
    private fun SummaryWidgetContent(context: Context) {
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
                    // Month title
                    Text(
                        text = WidgetDataManager.getMonthYearText(),
                        style = TextStyle(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = ColorProvider(Color(0xFFFFFFFF))
                        )
                    )

                    Spacer(modifier = GlanceModifier.height(16.dp))

                    // Stats Row
                    Row(
                        modifier = GlanceModifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.Start
                    ) {
                        // Expenses
                        Column(
                            modifier = GlanceModifier.defaultWeight(),
                            horizontalAlignment = Alignment.Start
                        ) {
                            Text(
                                text = "Expenses",
                                style = TextStyle(
                                    fontSize = 11.sp,
                                    color = ColorProvider(Color(0xFFAAAAAA))
                                )
                            )
                            Spacer(modifier = GlanceModifier.height(4.dp))
                            Text(
                                text = WidgetDataManager.formatCurrency(
                                    widgetData.monthlyExpenses,
                                    widgetData.currencySymbol
                                ),
                                style = TextStyle(
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ColorProvider(Color(0xFFFF6B6B))
                                )
                            )
                        }

                        Spacer(modifier = GlanceModifier.width(12.dp))

                        // Income
                        Column(
                            modifier = GlanceModifier.defaultWeight(),
                            horizontalAlignment = Alignment.Start
                        ) {
                            Text(
                                text = "Income",
                                style = TextStyle(
                                    fontSize = 11.sp,
                                    color = ColorProvider(Color(0xFFAAAAAA))
                                )
                            )
                            Spacer(modifier = GlanceModifier.height(4.dp))
                            Text(
                                text = WidgetDataManager.formatCurrency(
                                    widgetData.monthlyIncome,
                                    widgetData.currencySymbol
                                ),
                                style = TextStyle(
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ColorProvider(Color(0xFF4ECDC4))
                                )
                            )
                        }

                        Spacer(modifier = GlanceModifier.width(12.dp))

                        // Net
                        Column(
                            modifier = GlanceModifier.defaultWeight(),
                            horizontalAlignment = Alignment.Start
                        ) {
                            Text(
                                text = "Net",
                                style = TextStyle(
                                    fontSize = 11.sp,
                                    color = ColorProvider(Color(0xFFAAAAAA))
                                )
                            )
                            Spacer(modifier = GlanceModifier.height(4.dp))
                            Text(
                                text = WidgetDataManager.formatCurrency(
                                    widgetData.monthlyNet,
                                    widgetData.currencySymbol
                                ),
                                style = TextStyle(
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ColorProvider(
                                        if (widgetData.monthlyNet >= 0) Color(0xFF4ECDC4) else Color(0xFFFF6B6B)
                                    )
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

class SummaryWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = SummaryWidget()
}
