package com.example.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Work
import androidx.compose.ui.graphics.vector.ImageVector

object CategoryIconHelper {
  fun getIcon(iconName: String): ImageVector {
    return when (iconName.lowercase()) {
      "shopping_cart" -> Icons.Default.ShoppingCart
      "restaurant" -> Icons.Default.Restaurant
      "directions_car" -> Icons.Default.DirectionsCar
      "home" -> Icons.Default.Home
      "bolt" -> Icons.Default.Bolt
      "movie" -> Icons.Default.Movie
      "local_hospital" -> Icons.Default.LocalHospital
      "shopping_bag" -> Icons.Default.ShoppingBag
      "payments" -> Icons.Default.Payments
      "work" -> Icons.Default.Work
      "trending_up" -> Icons.AutoMirrored.Filled.TrendingUp
      "school" -> Icons.Default.School
      "flight" -> Icons.Default.Flight
      "account_balance" -> Icons.Default.AccountBalance
      "account_balance_wallet", "savings" -> Icons.Default.AccountBalanceWallet
      else -> Icons.Default.Category
    }
  }
}
