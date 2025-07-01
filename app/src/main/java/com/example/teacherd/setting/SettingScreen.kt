package com.example.teacherd.setting

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.window.Dialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingScreen(
    navController: NavController,
    viewModel: SettingViewModel
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("设置") },
                navigationIcon = {
                    IconButton(
                        onClick = { navController.popBackStack() }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        var showDialog by remember { mutableStateOf(false) }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.elevatedCardColors().copy(containerColor = Color.White)
            ) {
                Row(modifier = Modifier.padding(12.dp)) {
                    Column(modifier = Modifier.weight(8f)) {
                        Text("设置Api key", color = Color.Black, style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "当前Api key：${state.apiKey}",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = Color.LightGray
                        )
                    }
                    Spacer(Modifier.width(6.dp))
                    IconButton(
                        onClick = {
                            showDialog = !showDialog
                        },
                        modifier = Modifier.weight(2f)
                        ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = "返回"
                        )
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.elevatedCardColors().copy(containerColor = Color.White)
            ) {
                val modelName = if (state.model == "deepseek-chat") "DeepSeek-V3模型" else "DeepSeek-R1模型"
                Row(modifier = Modifier.padding(12.dp)) {
                    Column(modifier = Modifier.weight(8f)) {
                        Text(
                            "设置模型",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "当前模型：$modelName",
                            color = Color.LightGray
                        )
                    }
                    Spacer(Modifier.width(50.dp))
                    Box(modifier = Modifier.weight(2f)) {
                        var expanded by remember { mutableStateOf(false) }
                        IconButton(
                            onClick = {
                                expanded = !expanded
                            }
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = "返回"
                            )
                        }
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("DeepSeek-V3模型") },
                                onClick = {
                                    viewModel.selectChatModel()
                                    expanded = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("DeepSeek-R1模型") },
                                onClick = {
                                    viewModel.selectReasonerModel()
                                    expanded = false
                                }
                            )
                        }
                    }
                }

            }
        }
        if (showDialog) {
            Dialog(
                onDismissRequest = { showDialog = false }
            ) {
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.height(200.dp)
                ) {
                    Column(
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        var text by remember { mutableStateOf(state.apiKey) }
                        Text("请输入你的API Key", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                        Spacer(Modifier.height(12.dp))
                        OutlinedTextField(
                            value = text,
                            onValueChange = { text = it },
                            maxLines = 1,
                            modifier = Modifier.weight(1.2f)
                        )
                        Spacer(Modifier.height(12.dp))
                        Row(modifier = Modifier
                            .weight(0.8f)
                            .align(Alignment.End)
                        ) {
                            Button(
                                onClick = {
                                    viewModel.setApiKey(text)
                                }
                            ) {
                                Text("确定")
                            }
                            Spacer(Modifier.width(6.dp))
                            Button(
                                onClick = {
                                    showDialog = false
                                }
                            ) {
                                Text("取消")
                            }
                        }
                    }
                }
            }
        }
    }
}