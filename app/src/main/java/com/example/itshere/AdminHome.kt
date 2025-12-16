package com.example.itshere



import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.itshere.ui.theme.ItsHereTheme

@Composable
fun Greeting(
    modifier: Modifier = Modifier,
    name: String
) {
    Row{
        Text(
            text = "What'sUP? ",
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.SansSerif,
            modifier = modifier.padding(top = 12.dp)
        )
        Text(
            text = name,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.SansSerif,
            modifier = modifier.align(Alignment.Bottom)
        )
    }
}

@Composable
fun AdminHome(
    navController: NavController,
    modifier: Modifier = Modifier,
    onOpenDrawer: () -> Unit
) {

    Column(modifier
        .padding(top = 60.dp),
    ) {
        Row {
            IconButton(
                modifier = modifier.padding(end = 16.dp, bottom = 16.dp),
                onClick = onOpenDrawer //demo logout first
            ) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Menu",
                    tint = Color.Black,
                    modifier = modifier.size(32.dp)
                )
            }

            Greeting(
                name = "Admin1010",
                modifier = modifier.align(Alignment.Bottom)
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .background(
                    color = Color(0xFFBFC6FF),
                    shape = RoundedCornerShape(20.dp)
                )
                .clickable {navController.navigate("item_list")}
        ) {
            Text(
                text = "Item list",
                modifier = Modifier.padding(10.dp),
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )

            Image(
                painter = painterResource(R.drawable.box),
                contentDescription = null,
                modifier = Modifier
                    .size(120.dp)
                    .alpha(0.3f)
                    .align(Alignment.TopEnd)
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .background(
                    color = Color(0xFFBFC6FF),
                    shape = RoundedCornerShape(20.dp)
                )
                .clickable {navController.navigate("new_request")}
        ) {
            Text(
                text = "New Request",
                modifier = Modifier.padding(10.dp),
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )

            Image(
                painter = painterResource(R.drawable.bell),
                contentDescription = null,
                modifier = Modifier
                    .size(100.dp)
                    .alpha(0.3f)
                    .align(Alignment.TopEnd)

            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .background(
                    color = Color(0xFFe0e0e0),
                    shape = RoundedCornerShape(20.dp)
                )
                .clickable {navController.navigate("claimed")}
        ) {
            Text(
                text = "Claimed",
                modifier = Modifier.padding(10.dp),
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )

            Image(
                painter = painterResource(R.drawable.take),
                contentDescription = null,
                modifier = Modifier
                    .size(120.dp)
                    .alpha(0.3f)
                    .align(Alignment.TopEnd)
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .background(
                    color = Color(0xFFfacdcd),
                    shape = RoundedCornerShape(20.dp)
                )
                .clickable {navController.navigate("rejected")}
        ) {
            Text(
                text = "Rejected Claims",
                modifier = Modifier.padding(10.dp),
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )

            Image(
                painter = painterResource(R.drawable.stop),
                contentDescription = null,
                modifier = Modifier
                    .size(120.dp)
                    .alpha(0.3f)
                    .align(Alignment.TopEnd)
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreAdminHome() {
    ItsHereTheme {
        AdminHome(rememberNavController(), onOpenDrawer = {})
    }
}

