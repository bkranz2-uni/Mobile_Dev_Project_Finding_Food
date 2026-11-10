package com.example.mobile_dev_project_finding_food.ui.debug

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

/** Temporary screen to test the Supabase connection. Replace with the real map screen later. */
@Composable
fun SupabaseDebugScreen(
    modifier: Modifier = Modifier,
    viewModel: SupabaseDebugViewModel = viewModel(),
) {
    val state by viewModel.state.collectAsState()
    val signedInEmail by viewModel.signedInEmail.collectAsState()
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }

    Column(
        modifier = modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("Supabase debug", style = MaterialTheme.typography.headlineSmall)
        Text("Signed in as: ${signedInEmail ?: "nobody"}")

        if (signedInEmail == null) {
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Test user email") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth(),
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { viewModel.runTest(email, password) }, enabled = !state.loading) {
                Text("Load vegan products in test-1")
            }
            if (signedInEmail != null) {
                OutlinedButton(onClick = { viewModel.signOut() }) { Text("Sign out") }
            }
        }

        if (state.loading) CircularProgressIndicator()
        Text(state.status)
        HorizontalDivider()

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(state.products, key = { it.reportNumber }) { product ->
                Column {
                    Text(product.productName, style = MaterialTheme.typography.titleMedium)
                    Text(
                        "${product.reportNumber} · vegan=${product.isVegan} " +
                            "vegetarian=${product.isVegetarian} glutenFree=${product.isGlutenFree}",
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        }
    }
}
