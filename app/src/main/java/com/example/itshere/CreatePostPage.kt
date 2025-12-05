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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.itshere.Data.ImageItem
import java.text.SimpleDateFormat
import java.util.*

enum class PostType {
    LOST, FOUND
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePostPage(
    postType: PostType = PostType.FOUND,
    onBackClick: () -> Unit = {},
    onDraftClick: (CreatePostData) -> Unit = { _ -> },
    onPostClick: (CreatePostData) -> Unit = { _ -> }
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedPostType by remember { mutableStateOf(postType) }
    var phone by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }

    var selectedImages by remember { mutableStateOf<List<ImageItem>>(emptyList()) }

    var question1 by remember { mutableStateOf("") }
    var answer1 by remember { mutableStateOf("") }
    var question2 by remember { mutableStateOf("") }
    var answer2 by remember { mutableStateOf("") }
    var question3 by remember { mutableStateOf("") }
    var answer3 by remember { mutableStateOf("") }

    val multiplePhotoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 5),
        onResult = { uris ->
            val newImages = uris.map { uri ->
                ImageItem(uri = uri.toString())
            }
            selectedImages = selectedImages + newImages
        }
    )

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

    val canPost = if (selectedPostType == PostType.LOST) {
        title.isNotBlank()
                && date.isNotBlank()
                && selectedCategory.isNotBlank()
                && phone.isNotBlank()
                && selectedImages.isNotEmpty()
    } else {
        title.isNotBlank()
                && date.isNotBlank()
                && selectedCategory.isNotBlank()
                && selectedImages.isNotEmpty()
                && question1.isNotBlank() && answer1.isNotBlank()
                && question2.isNotBlank() && answer2.isNotBlank()
                && question3.isNotBlank() && answer3.isNotBlank()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Create Post",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.Black
                        )
                    }
                },
                actions = {
                    TextButton(onClick = { onDraftClick(createPostData()) }) {
                        Text(
                            text = "Draft",
                            color = Color(0xFF824DFF),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(16.dp))

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

            Column(
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Text(
                    text = "Title",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF666666),
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                CustomTextField(
                    value = title,
                    onValueChange = { title = it },
                    placeholder = "Details for the post items...",
                    modifier = Modifier.fillMaxWidth(),
                    isTitle = true
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Description",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF666666),
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                CustomTextArea(
                    value = description,
                    onValueChange = { description = it },
                    placeholder = "Write a description...",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Post Type:",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF666666),
                    fontSize = 14.sp
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

                if (selectedPostType == PostType.FOUND) {
                    Text(
                        text = "Questions for verify the item:",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF666666),
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    CompactQuestionAnswerField(
                        questionNumber = "1.",
                        question = question1,
                        onQuestionChange = { question1 = it },
                        answer = answer1,
                        onAnswerChange = { answer1 = it }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    CompactQuestionAnswerField(
                        questionNumber = "2.",
                        question = question2,
                        onQuestionChange = { question2 = it },
                        answer = answer2,
                        onAnswerChange = { answer2 = it }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    CompactQuestionAnswerField(
                        questionNumber = "3.",
                        question = question3,
                        onQuestionChange = { question3 = it },
                        answer = answer3,
                        onAnswerChange = { answer3 = it }
                    )

                    Spacer(modifier = Modifier.height(24.dp))
                }

                if (selectedPostType == PostType.LOST) {
                    Text(
                        text = "Phone:",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF666666),
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    CustomTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        placeholder = "60112345678",
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit",
                                tint = Color(0xFF999999),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    )

                    Spacer(modifier = Modifier.height(24.dp))
                }

                Text(
                    text = "Date:",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF666666),
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                CustomTextField(
                    value = date,
                    onValueChange = {},
                    placeholder = "DD/MM/YYYY",
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,
                    trailingIcon = {
                        IconButton(
                            onClick = { showDatePicker = true },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = "Select Date",
                                tint = Color(0xFF999999),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                )

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

                Text(
                    text = "Category:",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF666666),
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                val categories = listOf(
                    "Electronic", "Clothes", "Cards",
                    "Accessories", "Documents", "Others"
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    categories.forEach { category ->
                        CategoryChip(
                            text = category,
                            isSelected = selectedCategory == category,
                            onClick = { selectedCategory = category }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = { onPostClick(createPostData()) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF824DFF),
                        contentColor = Color.White
                    ),
                    enabled = canPost   // 🔥 这里是唯一修改的按钮 enable 条件
                ) {
                    Text(
                        text = "Post",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

// ----------------- 下面全部与原本一样（无改动） -----------------

@Composable
fun CustomTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    isTitle: Boolean = false,
    readOnly: Boolean = false,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    var isFocused by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = if (isFocused) Color(0xFFF8F9FA) else Color(0xFFF5F5F5),
        border = BorderStroke(
            width = 1.dp,
            color = if (isFocused) Color(0xFF824DFF) else Color(0xFFE0E0E0)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier
                    .weight(1f)
                    .onFocusChanged { isFocused = it.isFocused },
                textStyle = TextStyle(
                    color = Color.Black,
                    fontSize = if (isTitle) 16.sp else 14.sp,
                    fontWeight = if (isTitle) FontWeight.Medium else FontWeight.Normal
                ),
                cursorBrush = SolidColor(Color(0xFF824DFF)),
                readOnly = readOnly,
                decorationBox = { innerTextField ->
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
                            color = Color(0xFF999999),
                            fontSize = if (isTitle) 16.sp else 14.sp,
                            fontWeight = if (isTitle) FontWeight.Medium else FontWeight.Normal
                        )
                    }
                    innerTextField()
                }
            )

            trailingIcon?.invoke()
        }
    }
}

@Composable
fun CustomTextArea(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier
) {
    var isFocused by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = if (isFocused) Color(0xFFF8F9FA) else Color(0xFFF5F5F5),
        border = BorderStroke(
            width = 1.dp,
            color = if (isFocused) Color(0xFF824DFF) else Color(0xFFE0E0E0)
        )
    ) {
        Box(
            modifier = Modifier.padding(16.dp)
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier
                    .fillMaxSize()
                    .onFocusChanged { isFocused = it.isFocused },
                textStyle = TextStyle(
                    color = Color.Black,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                ),
                cursorBrush = SolidColor(Color(0xFF824DFF)),
                decorationBox = { innerTextField ->
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
                            color = Color(0xFF999999),
                            fontSize = 14.sp,
                            lineHeight = 20.sp
                        )
                    }
                    innerTextField()
                }
            )
        }
    }
}

@Composable
fun ImageUploadSection(
    selectedImages: List<ImageItem>,
    onAddImageClick: () -> Unit,
    onRemoveImage: (String) -> Unit,
    maxImages: Int = 5
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = "Upload Images",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF666666),
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 4.dp)
        ) {
            itemsIndexed(selectedImages) { index, imageItem ->
                ImagePreviewItem(
                    imageItem = imageItem,
                    onRemoveClick = { onRemoveImage(imageItem.id) }
                )
            }

            if (selectedImages.size < maxImages) {
                item {
                    AddImageButton(
                        onClick = onAddImageClick,
                        remainingCount = maxImages - selectedImages.size
                    )
                }
            }
        }

        if (selectedImages.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "${selectedImages.size}/$maxImages images selected",
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF999999),
                fontSize = 12.sp
            )
        }
    }
}

@Composable
fun ImagePreviewItem(
    imageItem: ImageItem,
    onRemoveClick: () -> Unit
) {
    Box(
        modifier = Modifier.size(80.dp)
    ) {
        Image(
            painter = rememberAsyncImagePainter(model = imageItem.uri),
            contentDescription = "Selected image",
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(12.dp)),
            contentScale = ContentScale.Crop
        )

        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(4.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = Color.Black.copy(alpha = 0.7f),
                modifier = Modifier
                    .size(24.dp)
                    .clickable { onRemoveClick() }
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Remove image",
                    tint = Color.White,
                    modifier = Modifier
                        .size(14.dp)
                        .align(Alignment.Center)
                )
            }
        }
    }
}

@Composable
fun AddImageButton(
    onClick: () -> Unit,
    remainingCount: Int
) {
    Surface(
        modifier = Modifier.size(80.dp),
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFF5F5F5),
        border = BorderStroke(1.dp, Color(0xFFE0E0E0)),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add image",
                tint = Color(0xFF999999),
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Add",
                style = MaterialTheme.typography.labelMedium,
                color = Color(0xFF999999),
                fontSize = 12.sp
            )
        }
    }
}

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
        border = BorderStroke(
            width = 1.dp,
            color = if (isSelected) Color(0xFFF44336) else Color(0xFFE0E0E0)
        ),
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
            style = MaterialTheme.typography.bodyMedium,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = if (isSelected) Color(0xFFC62828) else Color(0xFF666666)
        )
    }
}

@Composable
fun CategoryChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) Color(0xFFFFCDD2) else Color(0xFFF5F5F5),
        border = BorderStroke(
            width = 1.dp,
            color = if (isSelected) Color(0xFFC62828) else Color(0xFFE0E0E0)
        ),
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            style = MaterialTheme.typography.bodySmall,
            fontSize = 13.sp,
            color = if (isSelected) Color(0xFFC62828) else Color(0xFF666666)
        )
    }
}

@Composable
fun CompactQuestionAnswerField(
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
                color = Color(0xFF666666),
                fontSize = 14.sp,
                modifier = Modifier.padding(top = 14.dp, end = 8.dp)
            )
            Column(modifier = Modifier.weight(1f)) {

                CustomTextField(
                    value = question,
                    onValueChange = onQuestionChange,
                    placeholder = "Question...",
                    modifier = Modifier.fillMaxWidth(),
                    isTitle = false
                )
                Spacer(modifier = Modifier.height(8.dp))

                CustomTextField(
                    value = answer,
                    onValueChange = onAnswerChange,
                    placeholder = "Answer...",
                    modifier = Modifier.fillMaxWidth(),
                    isTitle = false
                )
            }
        }
    }
}

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

@Preview(showBackground = true)
@Composable
fun CreatePostPagePreview() {
    MaterialTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color.White
        ) {
            CreatePostPage(
                postType = PostType.FOUND,
                onDraftClick = { println("Draft saved: $it") },
                onPostClick = { println("Post created: $it") }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CreatePostLostPagePreview() {
    MaterialTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color.White
        ) {
            CreatePostPage(
                postType = PostType.LOST,
                onDraftClick = { println("Draft saved: $it") },
                onPostClick = { println("Post created: $it") }
            )
        }
    }
}

