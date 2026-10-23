package com.example

import com.example.core.QrCodeUtils
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun qrCodeUtils_generatesDeterministicMatrix() {
        val payload = "https://edusync.institute/verify?certId=CERT-2026-8941"
        val matrix1 = QrCodeUtils.generateMatrix(payload)
        val matrix2 = QrCodeUtils.generateMatrix(payload)

        // Matrix size must be exactly 25x25
        assertEquals(QrCodeUtils.MATRIX_SIZE, matrix1.size)
        assertEquals(QrCodeUtils.MATRIX_SIZE, matrix1[0].size)

        // Matrix output must be deterministic
        for (r in 0 until QrCodeUtils.MATRIX_SIZE) {
            for (c in 0 until QrCodeUtils.MATRIX_SIZE) {
                assertEquals(matrix1[r][c], matrix2[r][c])
            }
        }

        // Top-left finder outer ring must be true
        assertTrue(matrix1[0][0])
        assertTrue(matrix1[0][6])
        assertTrue(matrix1[6][0])
        assertTrue(matrix1[6][6])
        // Top-left finder center must be true
        assertTrue(matrix1[3][3])
        // Top-left finder whitespace separator must be false
        assertFalse(matrix1[1][1])
    }

    @Test
    fun qrCodeUtils_differentPayloadsProduceDifferentMatrices() {
        val matrixA = QrCodeUtils.generateMatrix("PAYLOAD_A")
        val matrixB = QrCodeUtils.generateMatrix("PAYLOAD_B")

        var differences = 0
        for (r in 0 until QrCodeUtils.MATRIX_SIZE) {
            for (c in 0 until QrCodeUtils.MATRIX_SIZE) {
                if (matrixA[r][c] != matrixB[r][c]) {
                    differences++
                }
            }
        }
        assertTrue("Different payloads must produce differences in the data matrix", differences > 0)
    }
}
