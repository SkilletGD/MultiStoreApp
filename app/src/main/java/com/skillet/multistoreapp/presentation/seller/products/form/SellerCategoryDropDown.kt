package com.skillet.multistoreapp.presentation.seller.products.form

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.skillet.multistoreapp.core.model.Category

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SellerCategoryDropdown(
    categories: List<Category>,
    selectedCategoryId: String,
    enabled: Boolean,
    onSelect: (Category) -> Unit
) {
    var expandeCategory by remember { mutableStateOf(false) }

    val selectedCategoryName : String = categories.firstOrNull{it.id == selectedCategoryId}?.name ?: ""

    ExposedDropdownMenuBox(
        expanded = expandeCategory,
        onExpandedChange = { expandeCategory = !expandeCategory }
    ) {
        OutlinedTextField(
            value = selectedCategoryName,
            onValueChange = {},
            modifier = Modifier.fillMaxWidth().menuAnchor(type = MenuAnchorType.PrimaryEditable),
            enabled = enabled,
            readOnly = true,
            label = {Text(text = "Selecciona una categoria")},
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(
                    expanded = expandeCategory
                )
            }
        )

        ExposedDropdownMenu(
            expanded = expandeCategory,
            onDismissRequest = { expandeCategory = false }
        ) {
            categories.forEach { category ->
                DropdownMenuItem(
                    text = {
                        Text(text = category.name)
                    },
                    onClick = {
                        expandeCategory = false
                        onSelect(category)
                    }
                )
            }
        }
    }

}