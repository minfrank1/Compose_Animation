package com.android.example.compose_animation

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.android.example.compose_animation.ui.theme.COMPOSE_ANIMATIONTheme
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import kotlin.time.nanoseconds
import androidx.compose.runtime.LaunchedEffect as LaunchedEffect1

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            COMPOSE_ANIMATIONTheme {
//                AnimatedAsStateTest() //animatedAsState
                /**
                 * AnimatedContent vs AnimatedVisibility
                 * AnimatedVisibility 는 targetValue로 boolean 밖에 못보내지만 AnimatedContent는 State를 다 보낼수 있음. State가 바뀌면 AnimatedContent가 트리거 됨.
                 */
//                AnimatedContentTest() //AnimatedContent
//                AnimateContentSizeTest() //AnimateContentSize Composable function의 사이즈가 바뀔때를 감지해서 애니메이션을 뿌려줌
//                CrossFadeTest() //CrossFadeTest 화면 바뀔때 사용하는 애니메이션
//                UpdateTranstionTest() //Transition은 하나 이상의 애니메이션을 하위 요소로 관리하며 여러 상태 간에 동시에 실행합니다.
//                RememberInfiniteTransitionTest() //Transition과 같은 하위 애니메이션이 하나 이상 포함되지만 컴포지션을 시작하는 즉시 애니메이션이 시작되며 삭제되지 않는 한 중단되지 않습니다
//                AnimatableTest() //Animatable은 LaunchedEffect랑같이 사용해야함.
                Gesture()
            }
        }
    }
}

@Composable
fun AnimatedAsStateTest() {
    var enabled by remember { mutableStateOf(true) }
    val alpha: Float by animateFloatAsState(
        targetValue = if (enabled) 1f else 0.5f,
        animationSpec = tween( // tween을 통해 애니메이션의 빠르기를 조정 할 수 있음. easing 파라미터를 통해 점점 빨라지게또는 점점 느려지게 할 수도 있음.
            durationMillis = 500, //애니메이션 동작하는 시간
            delayMillis = 0, //애니메이션이 시작하기전 주는 delay

            /**
             * easing 종류
             * FastOutSlowInEasing 빠르게 시작해서 점차 느려지는 애니메이션
             * LinearOutSlowInEasing 들어오는 요소는 제일 빠른 속도에서 점점 느려짐
             * FastOutLinearInEasing 나가는 요소는 제일 느린 속도에서 점점 빨라지는
             * LinearEasing 수정되지 않은 분수를 반환
             *
             * 원하는 이징이 없는 경우에는 CubicBezierEasing 을 이용하여 직접 이징을 구현할 수 있습니다.
             */
            easing = FastOutSlowInEasing //빠르게 시작해서 점차 느려지는 애니메이션
        )
    )
    Column() {
        Button(onClick = { enabled = !enabled}) {

        }
        Box(
            Modifier
                .fillMaxSize()
                .graphicsLayer(alpha = alpha)
                .background(Color.Red)
        )
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AnimatedContentTest() {
    Row {
        var count by remember { mutableStateOf(0) }
        Button(onClick = { count++ }) {
            Text("Add")
        }

        Button(onClick = { count-- }) {
            Text("minus")
        }
//        AnimatedContent(targetState = count) { targetCount ->
//            // Make sure to use `targetCount`, not `count`.
//            Text(text = "Count: $targetCount")
//        }

        AnimatedContent(
            targetState = count,
            transitionSpec = {
                // Compare the incoming number with the previous number.
                if (targetState > initialState) {
                    // If the target number is larger, it slides up and fades in
                    // while the initial (smaller) number slides up and fades out.
                    slideInVertically { height -> height } + fadeIn() with slideOutVertically { height -> -height } + fadeOut()
                } else {
                    // If the target number is smaller, it slides down and fades in
                    // while the initial number slides down and fades out.
                    slideInVertically { height -> -height } + fadeIn() with slideOutVertically { height -> height } + fadeOut()
                }.using(
                    // Disable clipping since the faded slide-in/out should
                    // be displayed out of bounds.
                    SizeTransform(clip = false)
                )
            }
        ) { targetCount ->
            Text(text = "$targetCount")
        }
    }
}

@Composable
fun AnimateContentSizeTest() {
    var message by remember { mutableStateOf("Hello") }
    Column() {

        Button(onClick = {message += "김성민ㅁㄴㅇㅁㄴㅇㅁㄴㅇㅁㄴㅇㅁㄴㅇㅁㄴㅇㅁ"}) {

        }
        Box(
            modifier = Modifier
                .background(Color.Blue)
                .animateContentSize()
        ) {
            Text(text = message)
        }
    }
}

@Composable
fun CrossFadeTest() {
    var currentPage by remember { mutableStateOf("A") }
    Column {
        Button(onClick = {currentPage = "B"}) {
            
        }
        Crossfade(targetState = currentPage) { screen ->
            when (screen) {
                "A" -> Text("Page A")
                "B" -> Text("Page B")
            }
        }
    }
}

enum class BoxState {
    Collapsed,
    Expanded
}


@Composable
fun UpdateTranstionTest() { //Transition은 하나 이상의 애니메이션을 하위 요소로 관리하며 여러 상태 간에 동시에 실행합니다.
    var currentState = remember { MutableTransitionState(BoxState.Collapsed) } // 초기값 접힘. //MutableTransitionState를 사용하면 처음에 애니메이션이 변경될때만 사용가능
    currentState.targetState = BoxState.Expanded
    val transition = updateTransition(currentState)

    val borderHeight by transition.animateDp(label = "", transitionSpec = {
        when {
            BoxState.Collapsed isTransitioningTo BoxState.Expanded -> tween(durationMillis = 2000)
            else -> tween()
        }

    }) { state ->

        when(state) {
            BoxState.Collapsed -> 0.dp
            BoxState.Expanded -> 100.dp
        }
    }

    val borderWidth by transition.animateDp(label = "", transitionSpec = {
        when {
            BoxState.Collapsed isTransitioningTo BoxState.Expanded -> tween(durationMillis = 2000)
            else -> tween()
        }
    }) { state ->
        when (state) {
            BoxState.Collapsed -> 0.dp
            BoxState.Expanded -> 100.dp
        }
    }

//    LaunchedEffect1(key1 = currentState) {
//        currentState.targetState = if(currentState.currentState == BoxState.Expanded) BoxState.Collapsed else BoxState.Expanded
//        Log.e("minfrank", currentState.targetState.toString())
//        Log.e("minfrank", currentState.currentState.toString())
//    }

    Column() {
        Button(onClick = {
            currentState.targetState = BoxState.Collapsed
        }) {

        }
        Box(modifier = Modifier
            .background(Color.Blue)
            .width(borderWidth)
            .height(borderHeight)
        ) {

        }
    }

}

@Composable
fun RememberInfiniteTransitionTest() {
    val infiniteTransition = rememberInfiniteTransition()
    val color by infiniteTransition.animateColor(
        initialValue = Color.Red,
        targetValue = Color.Green,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Box(
        Modifier
            .fillMaxSize()
            .background(color))
}

@Composable
fun AnimatableTest() {
    val color = remember { Animatable(Color.Gray) }
    var ok by remember {
        mutableStateOf(false)
    }
    LaunchedEffect1(ok) {
        color.animateTo(if (ok) Color.Green else Color.Red)
    }
    Column() {

        Button(onClick = {ok = !ok}) {

        }
        Box(
            Modifier
                .fillMaxSize()
                .background(color.value))
    }
}


@Composable
fun Gesture() {
    val offset = remember { Animatable(Offset(0f, 0f), Offset.VectorConverter) }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                coroutineScope {
                    while (true) {
                        // Detect a tap event and obtain its position.
                        val position = awaitPointerEventScope {
                            awaitFirstDown().position
                        }
                        launch {
                            // Animate to the tap position.
                            offset.animateTo(position)
                        }
                    }
                }
            }
    ) {
        Box(modifier = Modifier.offset { offset.value.toIntOffset() })
    }
}

private fun Offset.toIntOffset() = IntOffset(x.roundToInt(), y.roundToInt())