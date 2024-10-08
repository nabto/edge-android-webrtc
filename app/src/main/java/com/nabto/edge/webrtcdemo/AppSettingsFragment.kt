package com.nabto.edge.webrtcdemo

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.appcompat.content.res.AppCompatResources
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.preference.EditTextPreference
import androidx.preference.PreferenceFragmentCompat
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class AppSettingsFragment : PreferenceFragmentCompat() {
    private val repo: NabtoRepository by inject()
    private val database: DeviceDatabase by inject()
    private val bookmarks: NabtoBookmarksRepository by inject()
    private val manager: NabtoConnectionManager by inject()
    private val auth: LoginProvider by inject()

    private val displayNameKey = AppConfig.DISPLAY_NAME_PREF
    private val resetDatabaseKey = "preferences_reset_database"
    private val resetPrivateKeyKey = "preferences_reset_private_key"

    private fun getAppVersion(): String {
        val manager = activity?.packageManager
        val name = activity?.packageName
        val versionName = name?.let {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    manager?.getPackageInfo(it, PackageManager.PackageInfoFlags.of(0))?.versionName
                } else {
                    @Suppress("DEPRECATION") manager?.getPackageInfo(it, 0)?.versionName
                }
            } catch (e: PackageManager.NameNotFoundException) {
                null
            }
        }

        return versionName ?: "0.0"
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        val context = preferenceManager.context
        val screen = preferenceManager.createPreferenceScreen(context)
        val getDrawable = { drawable: Int -> AppCompatResources.getDrawable(context, drawable) }

        val prefs = listOf(
            EditTextPreference(context).apply {
                key = displayNameKey
                title = getString(R.string.display_name)
                icon = getDrawable(R.drawable.ic_person)
                summaryProvider = EditTextPreference.SimpleSummaryProvider.getInstance()
                dialogTitle = getString(R.string.display_name)
                dialogMessage = getString(R.string.settings_display_name_dialog_message)
            },

            ConfirmDialogPreference(context).apply {
                key = resetPrivateKeyKey
                title = getString(R.string.settings_reset_pk_title)
                icon = getDrawable(R.drawable.ic_key_reset)
                summary = getString(R.string.settings_reset_pk_summary)
                dialogTitle = getString(R.string.warning)
                dialogMessage = getString(R.string.settings_reset_pk_dialog_message)
                onDialogClosed = { confirmed ->
                    if (confirmed) {
                        repo.resetClientPrivateKey()
                        manager.releaseAll()
                        view?.snack(getString(R.string.settings_reset_pk_snack), Snackbar.LENGTH_SHORT);
                    }
                }
            },

            BasicDialogPreference(context).apply {
                title = getString(R.string.settings_about_title)
                icon = getDrawable(R.drawable.ic_questionmark)
                summary = getString(R.string.settings_about_summary)
                dialogTitle = "About ${context.getString(R.string.app_name)}"
                dialogMessage = """
                    
                    ${context.getString(R.string.app_name)} version ${getAppVersion()}
                    Nabto Client SDK version ${repo.getClientVersion()}
                    Edge-Client-Android version
                    
                """.trimIndent()
            },

            ConfirmDialogPreference(context).apply {
                title = "Log out"
                icon = getDrawable(R.drawable.ic_person)
                summary = "Log out and return to login screen"
                dialogTitle = "Log out"
                dialogMessage = "Are you sure you want to log out?"
                onDialogClosed = { confirmed ->
                    if (confirmed) {
                        lifecycleScope.launch {
                            bookmarks.release()
                            auth.logout()
                            findNavController().navigateAndClearBackStack(AppRoute.login())
                        }
                    }
                }
            }
        )
        prefs.forEach { pref -> screen.addPreference(pref) }
        preferenceScreen = screen
    }

}