package pe.edu.ulima.patronika.services

import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import org.springframework.mock.web.MockMultipartFile
import pe.edu.ulima.patronika.database.model.User
import pe.edu.ulima.patronika.database.repository.UserRepository
import pe.edu.ulima.patronika.dto.UserRequest
import pe.edu.ulima.patronika.exception.ConflictException
import pe.edu.ulima.patronika.exception.NotFoundException
import pe.edu.ulima.patronika.exception.UnauthorizedException
import pe.edu.ulima.patronika.security.HashEncoder
import java.time.LocalDate
import java.util.Optional
import java.util.UUID

@ExtendWith(MockitoExtension::class)
class UsersServiceTest {

    @Mock
    private lateinit var userRepository: UserRepository

    @Mock
    private lateinit var hashEncoder: HashEncoder

    @Mock
    private lateinit var cloudinaryService: CloudinaryService

    @InjectMocks
    private lateinit var usersService: UsersService

    private fun buildUserRequest(
        username: String = "ana123",
        email: String = "ana@example.com",
        password: String = "Clave123!"
    ) = UserRequest(
        username = username,
        email = email,
        password = password
    )

    private fun buildUser(
        id: UUID = UUID.randomUUID(),
        username: String = "ana123",
        email: String = "ana@example.com",
        isAdmin: Boolean = false,
        status: Int = 0,
        activateNotification: Boolean = true,
        suspensionEndDate: LocalDate? = null
    ) = User(
        id = id,
        username = username,
        email = email,
        hashedPassword = "hash-irrelevante",
        isAdmin = isAdmin,
        status = status,
        activateNotification = activateNotification,
        suspensionEndDate = suspensionEndDate
    )

    //registrar usuario

    @Test
    fun registrarUsuario_flujoCompleto() {
        val request = buildUserRequest()
        whenever(userRepository.findByUsername(request.username)).thenReturn(null)
        whenever(userRepository.findByEmail(request.email)).thenReturn(null)
        whenever(hashEncoder.encode(request.password)).thenReturn("hashed-password")
        whenever(userRepository.save(any())).thenAnswer { it.arguments[0] }

        val result = usersService.insertUser(request, file = null)

        assertEquals(request.username, result.username)
        assertEquals(request.email, result.email)
        assertEquals("hashed-password", result.hashedPassword)
        verifyNoInteractions(cloudinaryService)
        verify(userRepository).save(any())
    }

    @Test
    fun registrarUsuario_usernameDuplicado_lanzaConflictException() {
        val request = buildUserRequest()
        whenever(userRepository.findByUsername(request.username)).thenReturn(buildUser())

        val ex = assertThrows(ConflictException::class.java) {
            usersService.insertUser(request, file = null)
        }

        assertEquals("Usuario ya existe", ex.message)
        verify(userRepository, never()).findByEmail(any())
        verify(userRepository, never()).save(any())
    }

    @Test
    fun registrarUsuario_emailYaRegistrado_lanzaConflictException() {
        val request = buildUserRequest()
        whenever(userRepository.findByUsername(request.username)).thenReturn(null)
        whenever(userRepository.findByEmail(request.email)).thenReturn(buildUser())

        val ex = assertThrows(ConflictException::class.java) {
            usersService.insertUser(request, file = null)
        }

        assertEquals("El correo ya está registrado", ex.message)
        verify(userRepository, never()).save(any())
    }

    @Test
    fun registrarUsuario_conImagenDePerfil_subeImagenYGuardaUrl() {
        val request = buildUserRequest()
        val file = MockMultipartFile("file", "foto.png", "image/png", byteArrayOf(1, 2, 3))
        whenever(userRepository.findByUsername(request.username)).thenReturn(null)
        whenever(userRepository.findByEmail(request.email)).thenReturn(null)
        whenever(hashEncoder.encode(request.password)).thenReturn("hashed")
        whenever(cloudinaryService.uploadImage(file, folder = "users"))
            .thenReturn("https://cloudinary.com/users/foto.png")
        whenever(userRepository.save(any())).thenAnswer { it.arguments[0] }

        val result = usersService.insertUser(request, file)

        assertEquals("https://cloudinary.com/users/foto.png", result.profileImageUrl)
        verify(cloudinaryService).uploadImage(file, folder = "users")
    }

    @Test
    fun registrarUsuario_conArchivoVacio_noSubeNadaACloudinary() {
        val request = buildUserRequest()
        val archivoVacio = MockMultipartFile("file", "vacio.png", "image/png", ByteArray(0))
        whenever(userRepository.findByUsername(request.username)).thenReturn(null)
        whenever(userRepository.findByEmail(request.email)).thenReturn(null)
        whenever(hashEncoder.encode(request.password)).thenReturn("hashed")
        whenever(userRepository.save(any())).thenAnswer { it.arguments[0] }

        val result = usersService.insertUser(request, archivoVacio)

        assertNull(result.profileImageUrl)
        verifyNoInteractions(cloudinaryService)
    }
    @Test
    fun return_userRepository(){
        val request = buildUserRequest()
        whenever(userRepository.findByUsername(request.username)).thenReturn(null)
        whenever(userRepository.findByEmail(request.email)).thenReturn(null)
        whenever(hashEncoder.encode(request.password)).thenReturn("hashed")
        whenever(userRepository.save(any())).thenReturn(null)

        assertThrows(NullPointerException::class.java) {
            usersService.insertUser(request, file = null)
        }
    }

    //Update user
    @Test
    fun updateUsuario_ActualizaUsuario(){
        val TestId = UUID.randomUUID()
        val User = buildUser(id = TestId, email = "test@example.com", username = "User",isAdmin = true, status=1,activateNotification = false,suspensionEndDate =LocalDate.of(2026, 11, 4) )
        val request = buildUserRequest(email = "testexample.com")

        whenever(userRepository.findByEmail(request.email)).thenReturn(null)
        whenever(userRepository.findById(TestId)).thenReturn(Optional.of(User))
        usersService.updateUser(TestId, request)

        assertEquals(request.username, User.username)
        assertEquals(request.email, User.email)
        assertEquals(request.isAdmin, User.isAdmin)
        assertEquals(request.status, User.status)
        assertEquals(request.activateNotification,User.activateNotification)
        assertEquals(request.suspensionEndDate,User.suspensionEndDate)

        verify(userRepository).save(User)
    }

    @Test
    fun updateUsuario_UsuarioconMismoEmail(){
        val TestId = UUID.randomUUID()
        val TestId2 = UUID.randomUUID()

        val ana = buildUser(id =TestId, email = "ana@example.com")
        val juan = buildUser(id =TestId2, email = "compartido@example.com")
        val request = buildUserRequest(email = "compartido@example.com") //el request que manda ana para cambiar a este email

        whenever(userRepository.findById(TestId)).thenReturn(Optional.of(ana))
        whenever(userRepository.findByEmail(request.email)).thenReturn(juan)


        val error = assertThrows(ConflictException::class.java){
            usersService.updateUser(TestId, request)
        }
        assertEquals("El correo ya está registrado", error.message)
        assertEquals("ana@example.com", ana.email)
        verify(userRepository, never()).save(any())

    }

    //actualizar foto de perfil
    @Test
    fun updateProfileImg_flujoCompleto(){
        val id = UUID.randomUUID()
        val user = buildUser(id = id).apply { profileImageUrl = "https://cloudinary.com/old.png" }
        val file = MockMultipartFile("file", "img.png", "image/png", byteArrayOf(9))

        whenever(userRepository.findById(id)).thenReturn(Optional.of(user))
        whenever(cloudinaryService.uploadImage(file, folder = "users")).thenReturn("https://cloudinary.com/new.png")
        whenever(userRepository.save(any())).thenAnswer { it.arguments[0] }

        val result = usersService.updateProfileImage(id,file)

        verify(cloudinaryService).deleteImage("https://cloudinary.com/old.png")
        verify(cloudinaryService).uploadImage(file, folder = "users")
        assertEquals("https://cloudinary.com/new.png", result.profileImageUrl)

    }

    @Test
    fun updateProfileImg_NullPtrException(){
        val id = UUID.randomUUID()
        val user = buildUser(id = id).apply { profileImageUrl = "https://cloudinary.com/old.png" }
        val file = MockMultipartFile("file", "img.png", "image/png", byteArrayOf(9))

        whenever(userRepository.findById(id)).thenReturn(Optional.of(user))
        whenever(cloudinaryService.uploadImage(file, folder = "users")).thenReturn(null)


        assertThrows(NullPointerException::class.java) {
            usersService.updateProfileImage(id, file=file)
        }


    }

    //DeleteUser
    @Test
    fun deleteUser_flujoCompleto(){
        val AdminId = UUID.randomUUID()
        val Admin = buildUser(id = AdminId, username ="user", isAdmin = true)
        val Objetivo = buildUser(username="victima")

        whenever(userRepository.findById(AdminId)).thenReturn(Optional.of(Admin))
        whenever(userRepository.findByUsername("victima")).thenReturn(Objetivo)
        usersService.deleteUser("victima",AdminId)
        verify(userRepository).delete(Objetivo)
    }

    @Test
    fun deleteUser_noEsAdmin(){
        val falseAdminId = UUID.randomUUID()
        val falseAdmin = buildUser(id = falseAdminId, isAdmin = false)

        whenever(userRepository.findById(falseAdminId)).thenReturn(Optional.of(falseAdmin))
        assertThrows(UnauthorizedException::class.java) {
            usersService.deleteUser("victima",falseAdminId)
        }
        verify(userRepository, never()).delete(any())
    }

    @Test
    fun deleteUser_usuarioVictimaNoEncontrado(){
        val AdminId = UUID.randomUUID()
        val Admin = buildUser(id = AdminId, isAdmin = true)

        whenever(userRepository.findById(AdminId)).thenReturn(Optional.of(Admin))
        whenever(userRepository.findByUsername("falseUser")).thenReturn(null)

        assertThrows(NotFoundException::class.java) {
            usersService.deleteUser("falseUser",AdminId)
        }
    }
    //getALL
    @Test
    fun getAll_delegaEnRepositorio() {
        val lista = listOf(buildUser(), buildUser())
        whenever(userRepository.findAll()).thenReturn(lista)

        assertEquals(lista, usersService.getAll())
    }
    @Test
    fun getAll_NullPointerException() {
        doReturn(null).whenever(userRepository).findAll()

        assertThrows(NullPointerException::class.java) {
            usersService.getAll()
        }
    }

    //getUser
    @Test
    fun getUser_idExistente() {
        val id = UUID.randomUUID()
        val user = buildUser(id = id)
        whenever(userRepository.findById(id)).thenReturn(Optional.of(user))

        assertEquals(user, usersService.getUser(id))
    }

    @Test
    fun getUser_idInexistente() {
        val id = UUID.randomUUID()
        whenever(userRepository.findById(id)).thenReturn(Optional.empty())

        val error = assertThrows(NotFoundException::class.java) {
            usersService.getUser(id)
        }
        assertEquals("No encontrado", error.message)
    }

    @Test
    fun getUser_NotFoundException() {
        val id = UUID.randomUUID()
        whenever(userRepository.findById(id)).thenReturn(Optional.empty())

        assertThrows(NotFoundException::class.java) {
            usersService.getUser(id)
        }
    }



}