package com.example.strokefree.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
fun CustomIconButton(
    onclickDoSomething: () -> Unit,
    icon: ImageVector,
    contentDescription: String,
    iconTint : Color = Color.Black,
    iconSize : Int = 40
){
    IconButton(
        onClick = onclickDoSomething,
        modifier = Modifier
            .size(iconSize.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = Modifier
                .size(iconSize.dp),
            tint = iconTint
        )
    }

}


// Overloading implementation that accepts a Painter
@Composable
fun CustomIconButton(
    onclickDoSomething: () -> Unit,
    icon: Painter,
    contentDescription: String,
    iconTint: Color = Color.Black,
    iconSize: Int = 40
) {
    IconButton(
        onClick = onclickDoSomething,
        modifier = Modifier.size(iconSize.dp)
    ) {
        Icon(
            painter = icon,
            contentDescription = contentDescription,
            modifier = Modifier.size(iconSize.dp),
            tint = iconTint
        )
    }
}