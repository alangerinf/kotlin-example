package com.chatowl.presentation.common.utils

import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.chatowl.R

object PermissionsUtils {


    fun Fragment.hasPermission(permission: String, activityResultLauncher: ActivityResultLauncher<String>): Boolean {
        return when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                permission
            ) == PackageManager.PERMISSION_GRANTED -> {
                true
            }
            shouldShowRequestPermissionRationale(permission) -> {
                showPermissionLoadRecommendationDialog(requireActivity())
                false
            }
            else -> {
                activityResultLauncher.launch(permission)
                false
            }
        }
    }

    fun Fragment.showPermissionLoadRecommendationDialog(activity: Activity) {
        val dialog: AlertDialog.Builder = AlertDialog.Builder(activity)
        dialog.setTitle(getString(R.string.permissions_disabled))
        dialog.setMessage(getString(R.string.accept_permissions_explanation))
        dialog.setPositiveButton(getString(R.string.accept)) { _, _ -> requestPermissionsManually(activity) }
        dialog.show()
    }

    private fun Fragment.requestPermissionsManually(activity: Activity) {
        val options = arrayOf<CharSequence>(getString(R.string.yes), getString(R.string.no))
        val alertDialogBuilder = AlertDialog.Builder(activity)
        alertDialogBuilder.setTitle(getString(R.string.question_accept_permissions))
        alertDialogBuilder.setItems(
            options
        ) { dialog: DialogInterface, i: Int ->
            if (i == 0) {
                val intent = Intent()
                intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                val uri: Uri = Uri.fromParts("package", activity.packageName, null)
                intent.data = uri
                startActivity(intent)
            } else {
                Toast.makeText(activity, getString(R.string.permissions_declined), Toast.LENGTH_LONG)
                    .show()
                dialog.dismiss()
            }
        }
        alertDialogBuilder.show()
    }

}