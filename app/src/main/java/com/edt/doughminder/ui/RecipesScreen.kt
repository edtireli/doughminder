package com.edt.doughminder.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.edt.doughminder.data.Recipe
import com.edt.doughminder.data.Recipes
import com.edt.doughminder.ui.theme.Coral
import com.edt.doughminder.ui.theme.CreamDim
import com.edt.doughminder.ui.theme.InkBorder
import com.edt.doughminder.ui.theme.InkRaised

@Composable
fun RecipesScreen(onStartTimer: (stepTitle: String, minutes: Int) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text("Recipes", style = MaterialTheme.typography.displaySmall)
            Spacer(Modifier.height(4.dp))
            Text(
                "Step timers fire a notification when it's time for the next move.",
                style = MaterialTheme.typography.bodyMedium, color = CreamDim,
            )
            Spacer(Modifier.height(12.dp))
        }
        items(Recipes.all) { recipe ->
            RecipeCard(recipe, onStartTimer)
        }
    }
}

@Composable
private fun RecipeCard(recipe: Recipe, onStartTimer: (String, Int) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Column(
        Modifier
            .fillMaxWidth()
            .background(InkRaised, MaterialTheme.shapes.medium)
            .border(1.dp, InkBorder, MaterialTheme.shapes.medium)
            .clickable { expanded = !expanded }
            .padding(20.dp),
    ) {
        Text(recipe.title, style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(4.dp))
        Text(recipe.summary, style = MaterialTheme.typography.bodyMedium, color = CreamDim)
        if (expanded) {
            Spacer(Modifier.height(12.dp))
            recipe.steps.forEachIndexed { i, step ->
                Row(modifier = Modifier.padding(vertical = 6.dp)) {
                    Text("${i + 1}", style = MaterialTheme.typography.titleMedium, color = Coral)
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.fillMaxWidth()) {
                        Text(step.title, style = MaterialTheme.typography.titleMedium)
                        Text(step.detail, style = MaterialTheme.typography.bodyMedium, color = CreamDim)
                        if (step.timerMinutes != null) {
                            TextButton(
                                onClick = { onStartTimer(step.title, step.timerMinutes) },
                                contentPadding = PaddingValues(0.dp),
                            ) {
                                Text("Start ${step.timerMinutes} min timer ⏱", color = Coral)
                            }
                        }
                    }
                }
            }
        }
    }
}
