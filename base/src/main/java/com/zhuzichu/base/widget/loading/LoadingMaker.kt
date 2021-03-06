package com.zhuzichu.base.widget.loading

import android.content.Context
import java.lang.ref.WeakReference

object LoadingMaker {

    private var sProgressDialogRef: WeakReference<DialogLoading>? = null

    val isShowing: Boolean
        get() {
            val dialog = dialog
            return dialog != null && dialog.isShowing
        }

    private val dialog: DialogLoading?
        get() = if (sProgressDialogRef == null) null else sProgressDialogRef!!.get()


    fun showLoadingDialog(context: Context, canCancelable: Boolean = false): DialogLoading {
        var dialog = dialog
        if (dialog != null && dialog.context !== context) {
            dismissLodingDialog()
            dialog = null
        }
        if (dialog == null) {
            dialog = DialogLoading(context)
            sProgressDialogRef = WeakReference(dialog)
        }
        dialog.setCancelable(canCancelable)
        dialog.show()
        return dialog
    }

    fun dismissLodingDialog() {
        val dialog = dialog ?: return
        sProgressDialogRef!!.clear()
        if (dialog.isShowing) {
            try {
                dialog.dismiss()
            } catch (e: Exception) {
            }
        }
    }
}