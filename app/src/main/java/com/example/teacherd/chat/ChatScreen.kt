package com.example.teacherd.chat

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    viewModel: ChatViewModel
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val selectedChat by viewModel.selectedChat.collectAsStateWithLifecycle()
    val apiKey by viewModel.apiKey.collectAsStateWithLifecycle()
    val model by viewModel.selectedModel.collectAsStateWithLifecycle()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var question by remember { mutableStateOf("") }
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxHeight()
                ) {
                    Spacer(Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(0.5f)) {
                        Icon(
                            imageVector = Icons.Filled.History,
                            contentDescription = "History",
                            modifier = Modifier.weight(0.3f)
                        )
                        Spacer(Modifier.weight(0.1f))
                        Text(
                            text = "对话历史",
                            modifier = Modifier.weight(2f),
                            style = MaterialTheme.typography.titleLarge
                        )
                        Spacer(Modifier.weight(1.1f))
                    }
                    Spacer(Modifier.height(12.dp))
                    LazyColumn(modifier = Modifier.weight(4f)) {
                        items(state.chats) {
                            NavigationDrawerItem(
                                label = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("对话 ${it.id}", modifier = Modifier.weight(0.8f))
                                        IconButton(onClick = { viewModel.deleteChat(it) }, modifier = Modifier.weight(0.2f)) {
                                            Icon(
                                                imageVector = Icons.Filled.Close,
                                                contentDescription = "Delete"
                                            )
                                        }
                                    }
                                },
                                selected = it.id == selectedChat.id,
                                onClick = { viewModel.selectChat(it.id) }
                            )
                            Spacer(Modifier.height(8.dp))
                        }
                    }
                    Button(
                        onClick = { viewModel.deleteAllChats() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(0.3f)
                    ) { Text("清空对话记录") }
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(selectedChat.title, maxLines = 1) },
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                scope.launch {
                                    drawerState.apply {
                                        if (isClosed) open() else close()
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Menu,
                                contentDescription = "More"
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = { /*TODO*/ }) {
                            Icon(
                                imageVector = Icons.Filled.Settings,
                                contentDescription = "More"
                            )
                        }
                    },
                    modifier = Modifier.height(70.dp)
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp)
            ) {
                var selectedIndex by remember { mutableIntStateOf(if (model == "deepseek-chat") 0 else 1) }
                val options = listOf("DeepSeek-V3模型", "DeepSeek-R1模型")
                Column(modifier = Modifier.fillMaxWidth()) {
                    val lazyListState = rememberLazyListState()
                    LaunchedEffect(viewModel.tempChunks) {
                        snapshotFlow { selectedChat.chunks + viewModel.tempChunks }
                            .collect { items ->
                                snapshotFlow { lazyListState.layoutInfo.totalItemsCount }
                                    .filter { it > 0 }
                                    .first()
                                if (items.isNotEmpty())
                                    lazyListState.animateScrollToItem(items.lastIndex)
                            }
                    }
                    LazyColumn(state = lazyListState) {
                        item { Spacer(Modifier.height(100.dp)) }
                        items(selectedChat.chunks + viewModel.tempChunks) {
                            ChatItem(chat = it)
                            Spacer(Modifier.height(12.dp))
                        }
                        item { Spacer(Modifier.height(100.dp)) }
                    }
                }
                SingleChoiceSegmentedButtonRow(
                    modifier = Modifier.align(Alignment.TopCenter)
                        .offset(y = 30.dp)
                ) {
                    options.forEachIndexed { index, option ->
                        SegmentedButton(
                            selected = index == selectedIndex,
                            onClick = {
                                selectedIndex = index
                                if (index == 0)
                                    viewModel.selectChatModel()
                                else
                                    viewModel.selectReasonerModel()
                            },
                            shape = SegmentedButtonDefaults.itemShape(
                                index = index,
                                count = options.size
                            ),
                            label = { Text(option) },
                            modifier = Modifier.width(150.dp)
                        )
                    }
                }
                Card(
                    modifier = Modifier.fillMaxWidth()
                        .align(Alignment.BottomCenter),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Row(modifier = Modifier.padding(8.dp)) {
                        OutlinedTextField(
                            value = question,
                            shape = RoundedCornerShape(30.dp),
                            onValueChange = { question = it },
                            maxLines = 4,
                            modifier = Modifier.weight(0.8f),
                        )
                        Spacer(Modifier.width(8.dp))
                        FilledTonalButton(
                            onClick = {
                                viewModel.getResponse(question)
                                question = ""

                            },
                            modifier = Modifier.weight(0.2f)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Ask"
                            )
                        }
                    }
                }
            }
        }
    }
    if (apiKey.isEmpty()) {
        Dialog(
            onDismissRequest = { }
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
                    var text by remember { mutableStateOf("") }
                    Text("请输入你的API Key", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = text,
                        onValueChange = { text = it },
                        maxLines = 1,
                        modifier = Modifier.weight(1.2f)
                    )
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = {
                            viewModel.setApiKey(text)
                        },
                        modifier = Modifier
                            .weight(0.8f)
                            .align(Alignment.End)
                    ) {
                        Text("确定")
                    }
                }
            }
        }
    }
}