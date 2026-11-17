package com.example.proteinexplorer.scanner

import android.content.Context
import android.widget.Toast
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning

class QRScanner(private val context: Context) {

    fun scanQRCode(
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit = {}
    ) {
        val options = GmsBarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
            .build()

        val scanner = GmsBarcodeScanning.getClient(context, options)

        scanner.startScan()
            .addOnSuccessListener { barcode ->
                val rawValue = barcode.rawValue
                if (!rawValue.isNullOrEmpty()) {
                    // Extract accession if URL or raw ID (e.g., https://www.uniprot.org/uniprotkb/P69905 -> P69905)
                    val accession = extractAccession(rawValue)
                    onSuccess(accession)
                } else {
                    onError("No valid accession found in QR code.")
                    Toast.makeText(context, "Scanned empty QR code", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnCanceledListener {
                onError("Scan cancelled.")
                Toast.makeText(context, "Scan cancelled", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                val errorMsg = e.localizedMessage ?: "Failed to scan QR code."
                onError(errorMsg)
                Toast.makeText(context, "Scanner error: $errorMsg", Toast.LENGTH_LONG).show()
            }
    }

    private fun extractAccession(input: String): String {
        val trimmed = input.trim()
        if (trimmed.contains("/")) {
            val lastSegment = trimmed.substringAfterLast("/").trim()
            if (lastSegment.isNotEmpty()) return lastSegment.uppercase()
        }
        return trimmed.uppercase()
    }
}
