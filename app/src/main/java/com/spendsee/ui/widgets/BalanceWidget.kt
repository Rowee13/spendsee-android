package com.spendsee.ui.widgets

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.*
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.*
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.spendsee.managers.WidgetDataManager

class BalanceWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            BalanceWidgetContent(context)
        }
    }

    @Composable
    private fun BalanceWidgetContent(context: Context) {
        val widgetData = WidgetDataManager.getWidgetData(context)

        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(ColorProvider(Color(0xFF1E1E1E)))
                .padding(16.dp),
            contentAlignment = Alignment.Center
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
                    Spacer(modifier = GlanceModifier.height(4.dp))
                    Text(
                        text = "Unlock widgets",
                        style = TextStyle(
                            fontSize = 12.sp,
                            color = ColorProvider(Color(0xFFAAAAAA))
                        )
                    )
                }
            } else {
                // Widget content
                Column(
                    modifier = GlanceModifier.fillMaxSize(),
                    horizontalAlignment = Alignment.Start,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = "Total Balance",
                        style = TextStyle(
                            fontSize = 12.sp,
                            color = ColorProvider(Color(0xFFAAAAAA))
                        )
                    )
                    Spacer(modifier = GlanceModifier.height(8.dp))
                    Text(
                        text = WidgetDataManager.formatCurrency(
                            widgetData.totalBalance,
                            widgetData.currencySymbol
                        ),
                        style = TextStyle(
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = ColorProvider(Color(0xFFFFFFFF))
                        )
                    )
                    Spacer(modifier = GlanceModifier.defaultWeight())
                    Text(
                        text = "${widgetData.accountsCount} accounts",
                        style = TextStyle(
                            fontSize = 11.sp,
                            color = ColorProvider(Color(0xFF888888))
                        )
                    )
                }
            }
        }
    }
}

class BalanceWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = BalanceWidget()
}
