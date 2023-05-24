package com.fikt.chatly

import android.app.Dialog
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.google.firebase.auth.FirebaseAuth
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter


class QrDialog : DialogFragment() {
    private var link = "https://chatly.com/user/" + FirebaseAuth.getInstance().currentUser!!.uid
    val TAG = "qr_dialog"

    private var toolbar: Toolbar? = null

    fun display(fragmentManager: FragmentManager?): QrDialog? {
        val qrDialog = QrDialog()
        if (fragmentManager != null) {
            qrDialog.show(fragmentManager, TAG)
        }
        return qrDialog
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.AppTheme_FullScreenDialog)
    }

    override fun onStart() {
        super.onStart()
        val dialog: Dialog? = dialog
        if (dialog != null) {
            val width = ViewGroup.LayoutParams.MATCH_PARENT
            val height = ViewGroup.LayoutParams.MATCH_PARENT
            dialog.getWindow()!!.setLayout(width, height)
            dialog.getWindow()!!.setWindowAnimations(R.style.AppTheme_Slide)
        }
    }

    fun getQrCodeBitmap(): Bitmap {
        val size = 1024 //pixels
        var qrCodeContent = this.link

        var blackColor = Color.BLACK
        var whiteColor = Color.WHITE

        if (context != null) {
            var nightModeFlags = Configuration.UI_MODE_NIGHT_NO
            nightModeFlags = requireContext().resources.configuration.uiMode and
                    Configuration.UI_MODE_NIGHT_MASK

            if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES) {
                blackColor = Color.WHITE
                whiteColor = Color.TRANSPARENT
            }
        }

        val hints = hashMapOf<EncodeHintType, Int>().also { it[EncodeHintType.MARGIN] = 1 } // Make the QR code buffer border narrower
        val bits = QRCodeWriter().encode(qrCodeContent, BarcodeFormat.QR_CODE, size, size)
        return Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565).also {
            for (x in 0 until size) {
                for (y in 0 until size) {
                    it.setPixel(x, y, if (bits[x, y]) blackColor else whiteColor)
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val view: View = inflater.inflate(R.layout.qr_dialog, container, false)
        toolbar = view.findViewById(R.id.toolbar)
        val slikaQr = view.findViewById<ImageView>(R.id.qrCodeSlika)

        slikaQr!!.setImageBitmap(getQrCodeBitmap())
//

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        toolbar!!.setNavigationOnClickListener { v -> dismiss() }
//        toolbar!!.setTitle("Some Title")
        toolbar!!.inflateMenu(R.menu.qr_dialog)
//        toolbar!!.setOnMenuItemClickListener { item ->
//            dismiss()
//            true
//        }
    }
}