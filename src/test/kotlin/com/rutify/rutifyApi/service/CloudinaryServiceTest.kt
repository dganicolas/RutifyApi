package com.rutify.rutifyApi.service

import com.cloudinary.Cloudinary
import com.cloudinary.Uploader
import com.rutify.rutifyApi.utils.AuthUtils
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkStatic
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.springframework.web.multipart.MultipartFile
import kotlin.test.Test

class CloudinaryServiceTest{

    private val cloudinaryMock = mockk<Cloudinary>()
    private val uploaderMock = mockk<Uploader>()
    private val multipartFileMock = mockk<MultipartFile>()

    private val cloudinaryService = CloudinaryService(cloudinaryMock)

    @BeforeEach
    fun setUp() {
    }

    @AfterEach
    fun tearDown() {
    }

    @Test
    fun subirImagen_exito() {
        val fileBytes = byteArrayOf(1, 2, 3)
        val publicId = "sample-image"
        val version = "1234567890"
        val format = "jpg"

        val uploadResult = mapOf(
            "public_id" to publicId,
            "version" to version,
            "format" to format
        )

        every { multipartFileMock.bytes } returns fileBytes
        every { cloudinaryMock.uploader() } returns uploaderMock
        every { uploaderMock.upload(fileBytes, any()) } returns uploadResult

        val result = cloudinaryService.subirImagen(multipartFileMock)

        val expectedUrl = "https://res.cloudinary.com/dhtb7khec/image/upload/v$version/$publicId.$format"
        assertEquals(expectedUrl, result)
    }

}