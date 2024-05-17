package com.grass.android.ui.dasboard

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.grass.android.R
import com.grass.android.advancedShadow
import com.grass.android.ui.theme.DarkBackground
import com.grass.android.ui.theme.LightBackround

@Composable
fun DashboardScreen(modifier: Modifier, viewModel: DashboardViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val item1 = BentoItem("Epoch x Earnings:", null, uiState.epochTotal, R.drawable.grass_icon)
    val item2 = BentoItem(
        "Today's Earnings:",
        Icons.Outlined.Info,
        uiState.todayTotal,
        R.drawable.grass_icon
    )

    Card(
        modifier = Modifier
            .advancedShadow(cornersRadius = 16.dp, offsetY = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = DarkBackground
        ),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Row(
            modifier.padding(8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            BentoBox(modifier.weight(1f), item1)
            BentoBox(modifier.weight(1f), item2)
        }
    }
}


data class BentoItem(
    val title: String,
    val titleIcon: ImageVector?,
    val name: String,
    @DrawableRes val imageResId: Int
)

@Composable
fun BentoBox(modifier: Modifier, item: BentoItem) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = LightBackround, //Card background color
        ),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                item.titleIcon?.let {
                    Icon(
                        it, item.name, Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = item.title, modifier = Modifier.background(
                        DarkBackground
                    ), style = MaterialTheme.typography.titleSmall
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Food items section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = item.imageResId),
                    item.name,
                    modifier = Modifier.size(28.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(text = item.name, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}