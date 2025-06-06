package com.rutify.rutifyApi.service

import com.cloudinary.Cloudinary
import com.cloudinary.Uploader
import com.rutify.rutifyApi.domain.Comentario
import com.rutify.rutifyApi.domain.Usuario
import com.rutify.rutifyApi.dto.ComentarioDto
import com.rutify.rutifyApi.exception.exceptions.NotFoundException
import com.rutify.rutifyApi.exception.exceptions.ValidationException
import com.rutify.rutifyApi.repository.ComentarioRepository
import com.rutify.rutifyApi.repository.IUsuarioRepository
import com.rutify.rutifyApi.utils.AuthUtils
import com.rutify.rutifyApi.utils.DTOMapper
import io.mockk.*
import org.bson.types.ObjectId
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.assertThrows
import org.springframework.http.HttpStatus
import org.springframework.security.core.Authentication
import org.springframework.web.multipart.MultipartFile
import java.net.URI
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*
import kotlin.test.Test

class ComentarioServiceTest{

    // Mocks de dependencias necesarias
    private val comentarioRepository = mockk<ComentarioRepository>(relaxed = true)
    private val cloudinaryService = mockk<CloudinaryService>(relaxed = true)
    private val cloudinary = mockk<Cloudinary>(relaxed = true)
    private val mensajesService = mockk<MensajesService>(relaxed = true)
    private val usuarioRepository = mockk<IUsuarioRepository>(relaxed = true)
    private val authentication = mockk<Authentication>(relaxed = true)
    // Instancia de la clase a probar
    private val comentarioService = ComentarioService(
        comentarioRepository = comentarioRepository,
        cloudinaryService = cloudinaryService,
        cloudinary = cloudinary,
        mensajesService = mensajesService,
        usuariosRepository = usuarioRepository
    )


    @BeforeEach
    fun setUp() {
    }

    @AfterEach
    fun tearDown() {
        unmockkStatic(AuthUtils::class)
    }

    @Test
    fun crearComentariodebeguardarcomentarioconimagensubida() {
        // Arrange
        val mockMultipartFile = mockk<MultipartFile>()
        val comentarioDto = ComentarioDto(
            idFirebase = "abc123",
            nombreUsuario = "Usuario",
            avatarUrl = "url-avatar",
            fechaPublicacion = LocalDate.now(),
            imagenUrl = null,
            estadoAnimo = "feliz",
            texto = "Hola mundo"
        )

        every { mockMultipartFile.isEmpty } returns false
        every { cloudinaryService.subirImagen(mockMultipartFile) } returns "https://imagen.subida.com/img.png"

        val comentarioCapturado = slot<Comentario>()

        every { comentarioRepository.save(capture(comentarioCapturado)) } answers { comentarioCapturado.captured }

        // Act
        val resultado = comentarioService.crearComentario(comentarioDto, mockMultipartFile)

        // Assert
        assertEquals("https://imagen.subida.com/img.png", resultado.imagenUrl)
        assertEquals("Hola mundo", resultado.texto)
        assertEquals(false, comentarioCapturado.captured.estado)
    }

    @Test
    fun crearComentariodebeguardarcomentariosinimagensiMultipartFileesvacio() {
        val comentarioDto = ComentarioDto(
            idFirebase = "abc123",
            nombreUsuario = "Usuario",
            avatarUrl = "url-avatar",
            fechaPublicacion = LocalDate.now(),
            imagenUrl = null,
            estadoAnimo = "feliz",
            texto = "Sin imagen"
        )

        val emptyMultipartFile = mockk<MultipartFile> {
            every { isEmpty } returns true
        }

        val comentarioCapturado = slot<Comentario>()
        every { comentarioRepository.save(capture(comentarioCapturado)) } answers { comentarioCapturado.captured }

        val resultado = comentarioService.crearComentario(comentarioDto, emptyMultipartFile)

        assertNull(resultado.imagenUrl)
        assertNull(comentarioCapturado.captured.estado)
    }

    @Test
    fun `obtenerComentarios devuelve lista de ComentarioDto en orden inverso`() {
        // Arrange
        val comentario1 = Comentario(_id = "1", texto = "Primero", estado = null, idComentarioPadre = null, imagenUrl = "", avatarUrl = "", estadoAnimo = "", fechaPublicacion = LocalDate.now(), idFirebase = "", nombreUsuario = "")
        val comentario2 = Comentario(_id = "2", texto = "Segundo", estado = null, idComentarioPadre = null, imagenUrl = "", avatarUrl = "", estadoAnimo = "", fechaPublicacion = LocalDate.now(), idFirebase = "", nombreUsuario = "")

        every { comentarioRepository.findByIdComentarioPadreIsNullAndEstadoIsNull() } returns listOf(comentario1, comentario2)

        // Para mapear, asumiendo que DTOMapper.ComentarioToComentarioDto es estático o está disponible
        mockkObject(DTOMapper)
        every { DTOMapper.ComentarioToComentarioDto(comentario1) } returns ComentarioDto(_id = "1", texto = "Primero", idComentarioPadre = null, imagenUrl = "", avatarUrl = "", estadoAnimo = "", fechaPublicacion = LocalDate.now(), idFirebase = "", nombreUsuario = "")
        every { DTOMapper.ComentarioToComentarioDto(comentario2) } returns ComentarioDto(_id = "1", texto = "Segundo", idComentarioPadre = null, imagenUrl = "", avatarUrl = "", estadoAnimo = "", fechaPublicacion = LocalDate.now(), idFirebase = "", nombreUsuario = "")

        // Act
        val resultado = comentarioService.obtenerComentarios()

        // Assert
        assertEquals(2, resultado.size)
        // Como se hace reversed, el primero debe ser "Segundo"
        assertEquals("Segundo", resultado[0].texto)
        assertEquals("Primero", resultado[1].texto)
    }

    @Test
    fun `obtenerRespuestas devuelve comentario padre y sus respuestas mapeadas a ComentarioDto`() {
        // Arrange
        val idPadre = "idPadre123"
        val comentarioPadre = Comentario(_id = idPadre, texto = "Comentario Padre", estado = null, idComentarioPadre = null, imagenUrl = "", avatarUrl = "", estadoAnimo = "", fechaPublicacion = LocalDate.now(), idFirebase = "", nombreUsuario = "")
        val respuesta1 = Comentario(_id = "1", texto = "Primero", estado = null, idComentarioPadre = idPadre, imagenUrl = "", avatarUrl = "", estadoAnimo = "", fechaPublicacion = LocalDate.now(), idFirebase = "", nombreUsuario = "")
        val respuesta2 = Comentario(_id = "2", texto = "Segundo", estado = null, idComentarioPadre = idPadre, imagenUrl = "", avatarUrl = "", estadoAnimo = "", fechaPublicacion = LocalDate.now(), idFirebase = "", nombreUsuario = "")

        every { comentarioRepository.findById(idPadre) } returns Optional.of(comentarioPadre)
        every { comentarioRepository.findByIdComentarioPadre(idPadre) } returns listOf(respuesta1, respuesta2)

        mockkObject(DTOMapper)
        every { DTOMapper.ComentarioToComentarioDto(comentarioPadre) } returns ComentarioDto(_id = idPadre, texto = "Comentario Padre", idComentarioPadre = null, imagenUrl = "", avatarUrl = "", estadoAnimo = "", fechaPublicacion = LocalDate.now(), idFirebase = "", nombreUsuario = "")
        every { DTOMapper.ComentarioToComentarioDto(respuesta1) } returns ComentarioDto(_id = "1", texto = "Respuesta 1", idComentarioPadre = idPadre, imagenUrl = "", avatarUrl = "", estadoAnimo = "", fechaPublicacion = LocalDate.now(), idFirebase = "", nombreUsuario = "")
        every { DTOMapper.ComentarioToComentarioDto(respuesta2) } returns ComentarioDto(_id = "2", texto = "Respuesta 2", idComentarioPadre = idPadre, imagenUrl = "", avatarUrl = "", estadoAnimo = "", fechaPublicacion = LocalDate.now(), idFirebase = "", nombreUsuario = "")

        // Act
        val resultado = comentarioService.obtenerRespuestas(idPadre)

        // Assert
        assertEquals(3, resultado.size)
        assertEquals("Comentario Padre", resultado[0].texto)
        assertEquals("Respuesta 1", resultado[1].texto)
        assertEquals("Respuesta 2", resultado[2].texto)

        unmockkObject(DTOMapper)
    }

    val comentarioNromal = Comentario(_id = "12", texto = "Comentario Padre", estado = null, idComentarioPadre = null, imagenUrl = "", avatarUrl = "", estadoAnimo = "", fechaPublicacion = LocalDate.now(), idFirebase = "", nombreUsuario = "")
    val comentarioDto = ComentarioDto(_id = "1", texto = "Primero", idComentarioPadre = "12", imagenUrl = "", avatarUrl = "", estadoAnimo = "", fechaPublicacion = LocalDate.now(), idFirebase = "4343", nombreUsuario = "")
    @Test
    fun `responderComentario guarda y retorna ComentarioDto correctamente`() {
        // Arrange
        val comentarioEntity = Comentario(_id = "1", texto = "Primero", estado = null, idComentarioPadre = "12", imagenUrl = "", avatarUrl = "", estadoAnimo = "", fechaPublicacion = LocalDate.now(), idFirebase = "", nombreUsuario = "")
        val comentarioGuardado = ComentarioDto(_id = "1", texto = "Primero", idComentarioPadre = "12", imagenUrl = "", avatarUrl = "", estadoAnimo = "", fechaPublicacion = LocalDate.now(), idFirebase = "", nombreUsuario = "")

        mockkObject(DTOMapper)
        every { DTOMapper.ComnetarioDtoToComentario(any()) } returns comentarioEntity
        every { comentarioRepository.save(any()) } returns comentarioEntity
        every { DTOMapper.ComentarioToComentarioDto(any()) } returns comentarioGuardado.copy(_id = "nuevoId")

        // Act
        val resultado = comentarioService.responderComentario(comentarioGuardado)

        // Assert
        assertEquals("nuevoId", resultado._id)
        assertEquals("Primero", resultado.texto)

        unmockkObject(DTOMapper)
    }

    @Test
    fun `extraerPublicId devuelve null si la url es null`() {
        val resultado = comentarioService.extraerPublicId(null)
        assertNull(resultado)
    }

    @Test
    fun `extraerPublicId extrae correctamente el publicId de la url`() {
        val url = "https://res.cloudinary.com/dhtb7khec/image/upload/v1234567890/folder1/publicId123.jpg"
        val expected = "folder1/publicId123"
        val resultado = comentarioService.extraerPublicId(url)
        assertEquals(expected, resultado)
    }

    @Test
    fun `extraerPublicId maneja url sin carpeta`() {
        val url = "https://res.cloudinary.com/dhtb7khec/image/upload/v1234567890/publicId123.jpg"
        val expected = "publicId123"
        val resultado = comentarioService.extraerPublicId(url)
        assertEquals(expected, resultado)
    }

    val ususario = Usuario(idFirebase = "user-firebase-id", "user",
        LocalDate.now(),"","",
        LocalDate.now(),
        ObjectId(),"",false,false,1,"",)

    @Test
    fun crearComentario() {
        val comentarioExistente = comentarioNromal.copy(
            _id = "comentario-id",
            idFirebase = "autor-firebase-id",
            idComentarioPadre = "padre-id",
            imagenUrl = "https://res.cloudinary.com/tu_cuenta/image/upload/v1234/folder/publicId.jpg"
        )

        // Mock del comentario padre
        val comentarioPadre = comentarioNromal.copy(
            _id = "padre-id",
            idFirebase = "user-firebase-id", // mismo que authentication.name para simular autor del padre
            idComentarioPadre = null,
            imagenUrl = null
        )

        every { comentarioRepository.findById("comentario-id") } returns Optional.of(comentarioExistente)
        every { comentarioRepository.findById("padre-id") } returns Optional.of(comentarioPadre)
        every { comentarioRepository.deleteByIdComentarioPadreEquals("comentario-id") } just Awaits
        every { comentarioRepository.delete(comentarioExistente) } just Runs
        every { usuarioRepository.findByIdFirebase("user-firebase-id") } returns ususario

        // Mock para cloudinary uploader
        val uploader = mockk<Uploader>(relaxed = true)
        every { cloudinary.uploader() } returns uploader
        every { uploader.destroy(any(), any()) } returns mapOf<String, Any>()
    }

    @Test
    fun `eliminarComentario elimina correctamente comentario si autorizado`() {

        val comentarioExistente = comentarioNromal.copy(
            _id = "comentario-id",
            idFirebase = "autor-firebase-id",
            idComentarioPadre = "padre-id",
            imagenUrl = "https://res.cloudinary.com/tu_cuenta/image/upload/v1234/folder/publicId.jpg"
        )

        every { authentication.name } returns "user-firebase-id"
        every { comentarioRepository.findById(any()) } returns Optional.of(comentarioExistente)
        every { comentarioRepository.deleteByIdComentarioPadreEquals("comentario-id") } returns 1L
        every { comentarioRepository.delete(comentarioExistente) } just Runs
        every { cloudinary.uploader().destroy(any(), any()) } returns mapOf("result" to "ok")

        comentarioService.eliminarComentario("comentario-id", authentication)
    }

    @Test
    fun `aprobarComentario permite aprobar si el usuario es admin`() {
        val comentarioDto = ComentarioDto(
            _id = "comentario-id",
            idFirebase = "uid-usuario",
            nombreUsuario = "Test",
            avatarUrl = "avatar.png",
            fechaPublicacion = LocalDate.now(),
            imagenUrl = null,
            estadoAnimo = "feliz",
            texto = "Texto de prueba"
        )

        val comentarioExistente = Comentario(
            _id = "comentario-id",
            idFirebase = "uid-usuario",
            nombreUsuario = "Test",
            avatarUrl = "avatar.png",
            fechaPublicacion = LocalDate.now(),
            imagenUrl = null,
            estadoAnimo = "feliz",
            texto = "Texto de prueba",
            estado = false
        )

        val admin = ususario.copy(
            idFirebase = "admin-id",
            rol = "admin",
            nombre = "Admin",
            correo = "admin@mail.com",
            sexo = "H",
            fechaNacimiento = LocalDate.now().minusYears(30)
        )

        every { authentication.name } returns "admin-id"
        every { usuarioRepository.findByIdFirebase("admin-id") } returns admin
        every { comentarioRepository.findById("comentario-id") } returns Optional.of(comentarioExistente)
        every { comentarioRepository.save(any()) } returns comentarioExistente
        comentarioService.aprobarComentario(comentarioDto, authentication)

        verify { comentarioRepository.save(match { it.estado == null }) }
    }

    @Test
    fun `obtenerComentarioPorId retorna comentario si existe`() {
        val comentario = comentarioNromal.copy(
            _id = "comentario-id",
            idFirebase = "user-firebase-id",
            texto = "Contenido de prueba",
            idComentarioPadre = null
        )

        every { comentarioRepository.findById("comentario-id") } returns Optional.of(comentario)

        val resultado = comentarioService.obtenerComentarioPorId("comentario-id")

        assertEquals(comentario, resultado)
    }

    @Test
    fun `obtenerComentarioPorId lanza NotFoundException si no existe`() {
        every { comentarioRepository.findById("comentario-id") } returns Optional.empty()
        every { mensajesService.obtenerMensaje("comentarioNoEncontrado") } returns "Comentario no encontrado"

        val exception = assertThrows<NotFoundException> {
            comentarioService.obtenerComentarioPorId("comentario-id")
        }

        assertEquals("Not found exception (404). Comentario no encontrado", exception.message)
    }

    @Test
    fun `obtenerComentariosPorAutor retorna lista de comentarios si idFirebase es válido`() {
        val idFirebase = "user-firebase-id"
        val comentarios = listOf(
            comentarioNromal.copy(_id = "1", idFirebase = idFirebase, texto = "Comentario 1"),
            comentarioNromal.copy(_id = "2", idFirebase = idFirebase, texto = "Comentario 2")
        )
        mockkObject(DTOMapper)
        every { comentarioRepository.findAllByIdFirebaseAndIdComentarioPadreIsNull(any()) } returns comentarios
        every { DTOMapper.ComentarioToComentarioDto(any()) }  answers {
            val comentario = firstArg<Comentario>()
            ComentarioDto(
                _id = comentario._id ?: "",
                texto = comentario.texto ?: "",
                idComentarioPadre = comentario.idComentarioPadre ?: "",
                imagenUrl = comentario.imagenUrl ?: "",
                avatarUrl = comentario.avatarUrl ?: "",
                estadoAnimo = comentario.estadoAnimo ?: "",
                fechaPublicacion = comentario.fechaPublicacion ?: LocalDate.now(),
                idFirebase = comentario.idFirebase ?: "",  // aquí evitar null
                nombreUsuario = comentario.nombreUsuario ?: ""
            )
        }


        val response = comentarioService.obtenerComentariosPorAutor(idFirebase)

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(2, response.body?.size)
        assertEquals("Comentario 1", response.body?.get(0)?.texto)
    }

    @Test
    fun `obtenerComentariosPorAutor lanza ValidationException si idFirebase es vacío`() {
        val exception = assertThrows<ValidationException> {
            comentarioService.obtenerComentariosPorAutor("")
        }

        assertEquals("Error en la validacion (400). El ID del creador no puede estar vacío", exception.message)
    }

    @Test
    fun `countByIdFirebaseAndIdComentarioPadreIsNull retorna el conteo correcto`() {
        val idFirebase = "user-firebase-id"
        every { comentarioRepository.countByIdFirebaseAndIdComentarioPadreIsNull(idFirebase) } returns 5L
        val resultado = comentarioService.countByIdFirebaseAndIdComentarioPadreIsNull(idFirebase)
        assertEquals(5L, resultado)
    }

    @Test
    fun `eliminarComentariosDeUnUsuario llama eliminarComentario para cada comentario`() {
        val idFirebase = "user-firebase-id"
        val authentication = mockk<Authentication>()
        every { authentication.name } returns "admin-uid" // o el id del usuario autenticado

        val comentarios = listOf(
            comentarioNromal.copy(_id = "1", idFirebase = idFirebase),
            comentarioNromal.copy(_id = "2", idFirebase = idFirebase)
        )
        val comentarioServiceSpy = spyk(comentarioService)
        every { comentarioServiceSpy.eliminarComentario(any(), authentication) } just Runs
        comentarioServiceSpy.eliminarComentariosDeUnUsuario(idFirebase, authentication)

    }
}