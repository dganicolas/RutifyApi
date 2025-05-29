package com.rutify.rutifyApi.controller

import com.cloudinary.Cloudinary
import com.cloudinary.utils.ObjectUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

@Service
class CloudinaryService() {

    @Autowired
    private lateinit var cloudinary: Cloudinary

    fun subirImagen(file: MultipartFile): String {
        val fileBytes = file.bytes
        val uploadResult = cloudinary.uploader().upload(fileBytes, ObjectUtils.emptyMap())
        val publicId = uploadResult["public_id"] as String
        val version = uploadResult["version"].toString()
        val format = uploadResult["format"] as String  // ej: "jpg"
        return "https://res.cloudinary.com/dhtb7khec/image/upload/v$version/$publicId.$format"
    }
}