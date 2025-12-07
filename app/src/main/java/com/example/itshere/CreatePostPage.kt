package com.example.itshere

import android.content.Context
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
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.itshere.Data.ImageItem
import com.example.itshere.Data.PostType
import com.example.itshere.Data.QuestionAnswer
import com.example.itshere.ViewModel.PostViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePostPage(
    postType: PostType = PostType.FOUND,
    onBackClick: () -> Unit = {},
    onPostSuccess: () -> Unit = {}
) {
    val context = LocalContext.current
    val viewModel: PostViewModel = remember { PostViewModel(context) }

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

    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var showSuccessDialog by remember { mutableStateOf(false) }

    val state by viewModel.state.collectAsState()

    val multiplePhotoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 5),
        onResult = { uris ->
            val newImages = uris.map { uri ->
                ImageItem(uri = uri.toString())
            }
            selectedImages = selectedImages + newImages
        }
    )

    val canPost = if (selectedPostType == PostType.LOST) {
        title.isNotBlank() && date.isNotBlank() && selectedCategory.isNotBlank() &&
                phone.isNotBlank() && selectedImages.isNotEmpty()
    } else {
        title.isNotBlank() && date.isNotBlank() && selectedCategory.isNotBlank() &&
                selectedImages.isNotEmpty() && question1.isNotBlank() && answer1.isNotBlank() &&
                question2.isNotBlank() && answer2.isNotBlank() &&
                question3.isNotBlank() && answer3.isNotBlank()
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
            ) {
                if (state.isLoading && state.uploadProgress > 0f) {
                    LinearProgressIndicator(
                        progress = state.uploadProgress,
                        modifier = Modifier.fillMaxWidth(),
                        color = Color(0xFF824DFF),
                        trackColor = Color(0xFFE0E0E0)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Spacer(modifier = Modifier.height(8.dp))

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

                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
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
                        modifier = Modifier.fillMaxWidth().height(120.dp)
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
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
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
                                    imageVector = Icons.Default.DateRange,
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
                                            val formatter =
                                                SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
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

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        WrapContentRow(
                            horizontalSpacing = 8.dp,
                            verticalSpacing = 8.dp
                        ) {
                            categories.forEach { category ->
                                CategoryChip(
                                    text = category,
                                    isSelected = selectedCategory == category,
                                    onClick = { selectedCategory = category }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Button(
                        onClick = {
                            viewModel.createPost(
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
                                } else emptyList(),
                                onSuccess = {
                                    onPostSuccess()
                                },
                                onError = { error ->
                                    errorMessage = error
                                    showErrorDialog = true
                                }
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF824DFF),
                            disabledContainerColor = Color(0xFFCCCCCC)
                        ),
                        enabled = canPost && !state.isLoading
                    ) {
                        if (state.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = if (canPost) "Post" else "Fill Required Fields",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp,
                                color = if (canPost) Color.White else Color(0xFF666666)
                            )
                        }
                    }
                }
            }

            if (state.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CircularProgressIndicator(color = Color(0xFF824DFF))
                        if (state.uploadProgress > 0f) {
                            Text(
                                text = "Uploading... ${(state.uploadProgress * 100).toInt()}%",
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }

    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = {
                showSuccessDialog = false
                onPostSuccess()
            },
            title = {
                Text(
                    "Success!",
                    color = Color(0xFF824DFF),
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(errorMessage)
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSuccessDialog = false
                        onPostSuccess()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF824DFF)
                    )
                ) {
                    Text("OK")
                }
            }
        )
    }

    if (showErrorDialog) {
        AlertDialog(
            onDismissRequest = { showErrorDialog = false },
            title = {
                Text(
                    "Error",
                    color = Color.Red,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(errorMessage)
            },
            confirmButton = {
                Button(
                    onClick = { showErrorDialog = false },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF824DFF)
                    )
                ) {
                    Text("OK")
                }
            }
        )
    }
}

@Composable
fun WrapContentRow(
    modifier: Modifier = Modifier,
    horizontalSpacing: Dp = 0.dp,
    verticalSpacing: Dp = 0.dp,
    content: @Composable () -> Unit
) {
    Layout(
        content = content,
        modifier = modifier
    ) { measurables, constraints ->
        var currentRowWidth = 0
        var currentRowHeight = 0
        var totalHeight = 0
        var xPositions = mutableListOf<Int>()
        var yPositions = mutableListOf<Int>()

        val placeables = measurables.map { measurable ->
            measurable.measure(constraints.copy(minWidth = 0, minHeight = 0))
        }

        placeables.forEachIndexed { index, placeable ->
            if (currentRowWidth + placeable.width > constraints.maxWidth) {
                totalHeight += currentRowHeight + verticalSpacing.roundToPx()
                currentRowWidth = 0
                currentRowHeight = 0
            }

            xPositions.add(currentRowWidth)
            yPositions.add(totalHeight)

            currentRowWidth += placeable.width + horizontalSpacing.roundToPx()
            currentRowHeight = maxOf(currentRowHeight, placeable.height)
        }

        totalHeight += currentRowHeight

        layout(
            width = constraints.maxWidth,
            height = totalHeight
        ) {
            placeables.forEachIndexed { index, placeable ->
                placeable.place(
                    x = xPositions[index],
                    y = yPositions[index]
                )
            }
        }
    }
}

// ============= UI Components =============

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
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Remove image",
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }
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

@Preview(showBackground = true)
@Composable
fun CreatePostPagePreview() {
    MaterialTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color.White
        ) {
            CreatePostPage()
        }
    }
}