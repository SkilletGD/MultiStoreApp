package com.skillet.multistoreapp.presentation.seller.order

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.contextmenu.modifier.appendTextContextMenuComponents
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.skillet.multistoreapp.core.model.OrderStatus
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SellerOrderDetailScreen(
    viewModel: SellerOrderDetailViewModel,
    onGoToBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.effect.collect{ effect ->
            when(effect){
                is SellerOrderDetailEffect.ShowMessage -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                }
                is SellerOrderDetailEffect.OrderStatusUpdated -> {
                    onGoToBack()
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "Detalle del pedido")
                }
            )
        }
    ) {paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ){
            when{
                state.isLoading -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "Cargando...")
                    }
                }
                state.errorMessage != null -> {

                        Text(
                            text = state.errorMessage.orEmpty(),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center
                        )
                }
                state.order != null -> {
                    val order = state.order!!

                    val formatter = SimpleDateFormat(
                        "dd/MM/yyyy - hh:mm a",
                        Locale.getDefault()
                    )
                    val formattedDate = formatter.format(Date(order.created))

                    val statusText = when(order.status){
                        OrderStatus.PENDIENTE -> "Pendiente"
                        OrderStatus.CONFIRMADO -> "Confirmado"
                        OrderStatus.EN_CAMINO -> "En camino"
                        OrderStatus.ENTREGADO -> "Entregado"
                        OrderStatus.CANCELADO -> "Cancelado"
                    }

                    val avaliableStatus = when(order.status){
                        OrderStatus.PENDIENTE -> listOf(
                            OrderStatus.CONFIRMADO,
                            OrderStatus.CANCELADO
                        )
                        OrderStatus.CONFIRMADO -> listOf(
                            OrderStatus.EN_CAMINO,
                            OrderStatus.CANCELADO
                        )
                        OrderStatus.EN_CAMINO -> listOf(
                            OrderStatus.ENTREGADO
                        )

                        OrderStatus.ENTREGADO -> emptyList()

                        OrderStatus.CANCELADO -> emptyList()
                    }

                    var expanded by remember (order.status){
                        mutableStateOf(false)
                    }

                    val totalUnits = order.items.sumOf { orderItem ->
                        orderItem.quantity
                    }

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize(),
                        contentPadding = PaddingValues(
                            horizontal = 12.dp,
                            vertical = 12.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxSize(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text(
                                                text = "Pedido:",
                                                style = MaterialTheme.typography.titleMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Text(
                                                text = "#${order.id}",
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.SemiBold,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }

                                        Surface(
                                            shape = RoundedCornerShape(50.dp),
                                            color = MaterialTheme.colorScheme.secondaryContainer
                                        ) {
                                            Text(
                                                text = statusText,
                                                modifier = Modifier.padding(
                                                    horizontal = 8.dp,
                                                    vertical = 4.dp
                                                ),
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold,
                                            )
                                        }
                                    }

                                    HorizontalDivider(
                                        thickness = 0.5.dp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "Cliente ${order.customerName}",
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Text(
                                        text = "Fecha del pedido: $formattedDate",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        text = "Productos diferentes: ${order.items.size}",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        text = "Unidades solicitadas: $totalUnits",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }

                        if(avaliableStatus.isNotEmpty()){
                            item {
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = "Actualizar estado del pedido:",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    ExposedDropdownMenuBox(
                                        expanded = expanded,
                                        onExpandedChange = {
                                            if(!state.isUpdatingStatus){
                                                expanded = !expanded
                                            }
                                        }
                                    ) {
                                        OutlinedTextField(
                                            value = if(state.isUpdatingStatus) "Actualizando..." else statusText,
                                            onValueChange = {},
                                            readOnly = true,
                                            label = {
                                                Text(text = "Estado actual")
                                            },
                                            trailingIcon = {
                                                if(state.isUpdatingStatus){
                                                    CircularProgressIndicator(
                                                        modifier = Modifier.size(24.dp),
                                                        strokeWidth = 2.dp
                                                    )
                                                }else{
                                                    ExposedDropdownMenuDefaults.TrailingIcon(
                                                        expanded = expanded
                                                    )
                                                }


                                            },
                                            enabled = !state.isUpdatingStatus,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .menuAnchor(
                                                    type = MenuAnchorType.PrimaryNotEditable
                                            )
                                        )
                                        ExposedDropdownMenu(
                                            expanded = expanded,
                                            onDismissRequest = { expanded = false }
                                        ) {
                                            avaliableStatus.forEach { newStatus ->
                                                val newStatusText = when(newStatus){
                                                    OrderStatus.PENDIENTE -> "Pendiente"
                                                    OrderStatus.CONFIRMADO -> "Confirmado"
                                                    OrderStatus.EN_CAMINO -> "En camino"
                                                    OrderStatus.ENTREGADO -> "Entregado"
                                                    OrderStatus.CANCELADO -> "Cancelado"
                                                }
                                                DropdownMenuItem(
                                                    text = {
                                                        Text(text = newStatusText)
                                                    },
                                                    onClick = {
                                                        expanded = false
                                                        viewModel.onEvent(
                                                            SellerOrderDetailEvent.UpdateOrderStatus(
                                                                newStatus
                                                            )
                                                        )
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        item{
                            Text(
                                text = "Productos",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        items(
                            items = order.items,
                            key = { orderItem ->
                                orderItem.productId
                            }
                        ) { orderItem ->
                            Card(
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxSize(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Text(
                                            text = orderItem.productName,
                                            modifier = Modifier.weight(1f),
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.weight(1f))
                                        Text(
                                            text = String.format(
                                                Locale.getDefault(),
                                                    "%.2f",
                                                    orderItem.subtotal
                                            ),
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )

                                    }

                                    if(orderItem.productDescription.isNotBlank()){
                                        Text(
                                            text = orderItem.productDescription,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    Text(
                                        text = "${orderItem.quantity} * ${
                                            String.format(
                                                Locale.getDefault(),
                                                "%.2f USD",
                                                orderItem.productPrice
                                            )
                                        }",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth()
                            ){
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp)

                                    ,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Total del pedido:",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )

                                    Text(
                                        text = String.format(
                                            Locale.getDefault(),
                                            "%.2f USD",
                                            order.total
                                        ),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }

                else -> {
                    Text(
                        text = "No se encontro el pedido",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}