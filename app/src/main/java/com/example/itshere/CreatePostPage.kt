package com.example.itshere

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import java.text.SimpleDateFormat
import java.util.*

enum class PostType {
    LOST, FOUND
}

// 图片项数据类
data class ImageItem(
    val id: String = UUID.randomUUID().toString(),
    val uri: String,
    val isLocal: Boolean = true
)

// 主函数
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePostPage(
    postType: PostType = PostType.FOUND,
    onBackClick: () -> Unit = {},
    onDraftClick: (CreatePostData) -> Unit = { _ -> }, // 保存草稿时传递数据
    onPostClick: (CreatePostData) -> Unit = { _ -> }   // 发布时传递数据
) {
    // 表单状态
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedPostType by remember { mutableStateOf(postType) }
    var phone by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }

    // 图片状态
    var selectedImages by remember { mutableStateOf<List<ImageItem>>(emptyList()) }

    // 问题状态
    var question1 by remember { mutableStateOf("") }
    var answer1 by remember { mutableStateOf("") }
    var question2 by remember { mutableStateOf("") }
    var answer2 by remember { mutableStateOf("") }
    var question3 by remember { mutableStateOf("") }
    var answer3 by remember { mutableStateOf("") }

    // 图片选择器
    val multiplePhotoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 5),
        onResult = { uris ->
            val newImages = uris.map { uri ->
                ImageItem(uri = uri.toString())
            }
            selectedImages = selectedImages + newImages
        }
    )

    // 创建数据对象
    fun createPostData(): CreatePostData {
        return CreatePostData(
            title = title,
            description = description,
            postType = selectedPostType,
            phone = phone,
            date = date,
            category = selectedCategory,
            images = selectedImages,
            questions = if (selectedPostType == PostType.FOUND) {
                listOf(
                    QuestionAnswer(question1, answer1),
                    QuestionAnswer(question2, answer2),
                    QuestionAnswer(question3, answer3)
                )
            } else {
                emptyList()
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Create Post",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    TextButton(onClick = { onDraftClick(createPostData()) }) {
                        Text(
                            text = "Draft",
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // 图片上传区域
            ImageUploadSection(
                selectedImages = selectedImages,
                onAddImageClick = {
                    multiplePhotoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                onRemoveImage = { imageId ->
                    selectedImages = selectedImages.filter { it.id != imageId }
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 标题区域
            Text(
                text = "Title",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Details for the post items...") },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color.LightGray
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 描述区域（可选添加）
            Text(
                text = "Description",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                placeholder = { Text("Enter description...") },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color.LightGray
                ),
                maxLines = 4
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 帖子类型
            Text(
                text = "Post Type:",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                PostTypeChip(
                    text = "Lost",
                    isSelected = selectedPostType == PostType.LOST,
                    onClick = { selectedPostType = PostType.LOST }
                )
                PostTypeChip(
                    text = "Found",
                    isSelected = selectedPostType == PostType.FOUND,
                    onClick = { selectedPostType = PostType.FOUND }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 问题区域（仅限 Found 类型）
            if (selectedPostType == PostType.FOUND) {
                Text(
                    text = "Questions for verify the item:",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(12.dp))

                // 问题1
                QuestionAnswerField(
                    questionNumber = "1.",
                    question = question1,
                    onQuestionChange = { question1 = it },
                    answer = answer1,
                    onAnswerChange = { answer1 = it }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 问题2
                QuestionAnswerField(
                    questionNumber = "2.",
                    question = question2,
                    onQuestionChange = { question2 = it },
                    answer = answer2,
                    onAnswerChange = { answer2 = it }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 问题3
                QuestionAnswerField(
                    questionNumber = "3.",
                    question = question3,
                    onQuestionChange = { question3 = it },
                    answer = answer3,
                    onAnswerChange = { answer3 = it }
                )

                Spacer(modifier = Modifier.height(24.dp))
            }

            // 电话区域（仅限 Lost 类型）
            if (selectedPostType == PostType.LOST) {
                Text(
                    text = "Phone:",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("60112345678") },
                    shape = RoundedCornerShape(12.dp),
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = Color.Gray
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color.LightGray
                    )
                )

                Spacer(modifier = Modifier.height(24.dp))
            }

            // 日期选择
            Text(
                text = "Date:",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = date,
                onValueChange = {},
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("DD/MM/YYYY") },
                shape = RoundedCornerShape(12.dp),
                readOnly = true,
                trailingIcon = {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = "Select Date",
                            tint = Color.Gray
                        )
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color.LightGray
                )
            )

            // 日期选择器对话框
            if (showDatePicker) {
                val datePickerState = rememberDatePickerState()

                DatePickerDialog(
                    onDismissRequest = { showDatePicker = false },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                datePickerState.selectedDateMillis?.let { millis ->
                                    val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                                    date = formatter.format(Date(millis))
                                }
                                showDatePicker = false
                            }
                        ) {
                            Text("OK")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDatePicker = false }) {
                            Text("Cancel")
                        }
                    }
                ) {
                    DatePicker(state = datePickerState)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 分类选择
            Text(
                text = "Category:",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))

            // 分类芯片 - 第一行
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CategoryChip(
                    text = "Electronic",
                    isSelected = selectedCategory == "Electronic",
                    onClick = { selectedCategory = "Electronic" }
                )
                CategoryChip(
                    text = "Clothes",
                    isSelected = selectedCategory == "Clothes",
                    onClick = { selectedCategory = "Clothes" }
                )
                CategoryChip(
                    text = "Cards",
                    isSelected = selectedCategory == "Cards",
                    onClick = { selectedCategory = "Cards" }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 分类芯片 - 第二行
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CategoryChip(
                    text = "Accessories",
                    isSelected = selectedCategory == "Accessories",
                    onClick = { selectedCategory = "Accessories" }
                )
                CategoryChip(
                    text = "Documents",
                    isSelected = selectedCategory == "Documents",
                    onClick = { selectedCategory = "Documents" }
                )
                CategoryChip(
                    text = "Others",
                    isSelected = selectedCategory == "Others",
                    onClick = { selectedCategory = "Others" }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // 发布按钮
            Button(
                onClick = { onPostClick(createPostData()) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF824DFF)
                ),
                enabled = title.isNotBlank() && date.isNotBlank() && selectedCategory.isNotBlank()
            ) {
                Text(
                    text = "Post",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

// 图片上传区域组件
@Composable
fun ImageUploadSection(
    selectedImages: List<ImageItem>,
    onAddImageClick: () -> Unit,
    onRemoveImage: (String) -> Unit,
    maxImages: Int = 5
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "Upload Images",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(12.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 4.dp)
        ) {
            // 显示已选择的图片
            itemsIndexed(selectedImages) { index, imageItem ->
                ImagePreviewItem(
                    imageItem = imageItem,
                    onRemoveClick = { onRemoveImage(imageItem.id) }
                )
            }

            // 添加图片按钮（如果没有达到最大数量）
            if (selectedImages.size < maxImages) {
                item {
                    AddImageButton(
                        onClick = onAddImageClick,
                        remainingCount = maxImages - selectedImages.size
                    )
                }
            }
        }

        // 图片数量提示
        if (selectedImages.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "${selectedImages.size}/$maxImages images selected",
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray
            )
        }
    }
}

// 图片预览项
@Composable
fun ImagePreviewItem(
    imageItem: ImageItem,
    onRemoveClick: () -> Unit
) {
    Box(
        modifier = Modifier.size(100.dp)
    ) {
        // 图片显示
        Image(
            painter = rememberAsyncImagePainter(model = imageItem.uri),
            contentDescription = "Selected image",
            modifier = Modifier
                .size(100.dp)
                .clip(RoundedCornerShape(12.dp)),
            contentScale = ContentScale.Crop
        )

        // 删除按钮
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(6.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = Color.Black.copy(alpha = 0.7f),
                modifier = Modifier
                    .size(28.dp)
                    .clickable { onRemoveClick() }
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Remove image",
                    tint = Color.White,
                    modifier = Modifier
                        .size(18.dp)
                        .align(Alignment.Center)
                )
            }
        }
    }
}

// 添加图片按钮
@Composable
fun AddImageButton(
    onClick: () -> Unit,
    remainingCount: Int
) {
    Surface(
        modifier = Modifier.size(100.dp),
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFF5F5F5),
        border = BorderStroke(1.dp, Color.LightGray),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.AddPhotoAlternate,
                contentDescription = "Add image",
                tint = Color.Gray,
                modifier = Modifier.size(32.dp)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Add Photo",
                style = MaterialTheme.typography.labelMedium,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = "$remainingCount left",
                style = MaterialTheme.typography.labelSmall,
                color = Color.LightGray
            )
        }
    }
}

// 帖子类型芯片
@Composable
fun PostTypeChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = if (isSelected) Color(0xFFFFCDD2) else Color.Transparent,
        border = if (!isSelected) BorderStroke(1.dp, Color.LightGray) else null,
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = if (isSelected) Color(0xFFC62828) else Color.Gray
        )
    }
}

// 分类芯片
@Composable
fun CategoryChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = if (isSelected) Color(0xFFFFCDD2) else Color(0xFFF5F5F5),
        border = if (!isSelected) BorderStroke(1.dp, Color.LightGray) else null,
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            style = MaterialTheme.typography.bodySmall,
            color = if (isSelected) Color(0xFFC62828) else Color.Gray
        )
    }
}

// 问题和答案字段
@Composable
fun QuestionAnswerField(
    questionNumber: String,
    question: String,
    onQuestionChange: (String) -> Unit,
    answer: String,
    onAnswerChange: (String) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            Text(
                text = questionNumber,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 12.dp, end = 8.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                OutlinedTextField(
                    value = question,
                    onValueChange = onQuestionChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Question...") },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color.LightGray
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = answer,
                    onValueChange = onAnswerChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Answer...") },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color.LightGray
                    )
                )
            }
        }
    }
}

// 数据模型类
data class CreatePostData(
    val title: String = "",
    val description: String = "",
    val postType: PostType = PostType.FOUND,
    val phone: String = "",
    val date: String = "",
    val category: String = "",
    val images: List<ImageItem> = emptyList(),
    val questions: List<QuestionAnswer> = emptyList()
)

data class QuestionAnswer(
    val question: String = "",
    val answer: String = ""
)

// 预览
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun CreatePostFoundPreview() {
    MaterialTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            CreatePostPage(
                postType = PostType.FOUND,
                onDraftClick = { println("Draft saved: $it") },
                onPostClick = { println("Post created: $it") }
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun CreatePostLostPreview() {
    MaterialTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            CreatePostPage(
                postType = PostType.LOST,
                onDraftClick = { println("Draft saved: $it") },
                onPostClick = { println("Post created: $it") }
            )
        }
    }
}

// 如果需要显示示例图片，可以使用这个预览
@Preview(showBackground = true)
@Composable
fun ImagePreviewItemPreview() {
    MaterialTheme {
        Box(modifier = Modifier.background(Color.White)) {
            AddImageButton(
                onClick = {},
                remainingCount = 5
            )
        }
    }
}