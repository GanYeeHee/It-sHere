package com.example.itshere

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController

@Composable
fun ClaimedScreen(navController: NavController, modifier: Modifier = Modifier){
    Box(
        modifier.fillMaxSize()
    ){
        Image(
            painter = painterResource(R.drawable.back_arrow),
            contentDescription = "back",
            modifier = Modifier
                .size(90.dp)
                .padding(top = 55.dp )
                .clickable{navController.navigateUp()}
        )
        Text(
            text = "Claimed",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold ,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 60.dp)
        )
    }
}

@Preview(
    showBackground = true,
    showSystemUi = true
)
@Composable
fun PreClaimedScreen(){
    ClaimedScreen(navController = rememberNavController())
}