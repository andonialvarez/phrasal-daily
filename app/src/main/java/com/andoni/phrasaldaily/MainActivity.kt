package com.andoni.phrasaldaily

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.andoni.phrasaldaily.data.ThemeMode
import com.andoni.phrasaldaily.ui.AppViewModel
import com.andoni.phrasaldaily.ui.MainScaffold
import com.andoni.phrasaldaily.ui.screens.AchievementUnlockedDialog
import com.andoni.phrasaldaily.ui.screens.OnboardingScreen
import com.andoni.phrasaldaily.ui.screens.UpdateDialog
import com.andoni.phrasaldaily.ui.theme.PhrasalTheme

class MainActivity : ComponentActivity() {

    private val vm: AppViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val user by vm.user.collectAsStateWithLifecycle()
            val newAchievements by vm.newAchievements.collectAsStateWithLifecycle()
            val update by vm.update.collectAsStateWithLifecycle()
            val updateProgress by vm.updateProgress.collectAsStateWithLifecycle()
            PhrasalTheme(user?.theme ?: ThemeMode.SYSTEM) {
                Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    val u = user
                    when {
                        u == null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("💬", fontSize = 64.sp)
                        }
                        !u.onboarded -> OnboardingScreen { name, mode, hour, minute, notifications ->
                            vm.completeOnboarding(name, mode, hour, minute, notifications)
                        }
                        else -> MainScaffold(vm, u)
                    }
                    update?.let { info ->
                        UpdateDialog(
                            info = info,
                            progress = updateProgress,
                            onUpdate = { vm.startUpdate() },
                            onDismiss = { vm.dismissUpdate() },
                        )
                    }
                    if (update == null && newAchievements.isNotEmpty()) {
                        AchievementUnlockedDialog(newAchievements) { vm.dismissAchievements() }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        vm.onAppOpened()
    }
}
