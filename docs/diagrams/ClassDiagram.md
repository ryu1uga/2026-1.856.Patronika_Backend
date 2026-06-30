# Diagrama de Clases — Patronika Backend

```mermaid
classDiagram
direction TB

%% =========================
%% CONTROLLERS
%% =========================

class AuthController {
  -authService: AuthService
  -userService: UsersService
}

class UsersController {
  -usersService: UsersService
}

class PatternsController {
  -patternsService: PatternsService
}

class PublicationsController {
  -publicationsService: PublicationsService
}

class CommentsController {
  -commentsService: CommentsService
}

class TutorialsController {
  -tutorialsService: TutorialsService
}

class TutorialProgressesController {
  -tutorialProgressesService: TutorialProgressesService
}

class PatternLibraryController {
  -patternLibraryService: PatternLibraryService
}

class PublishedPatternsController {
  -publishedPatternRepository: PublishedPatternRepository
}

%% =========================
%% SERVICES
%% =========================

class AuthService {
  -userRepository: UserRepository
  -usersService: UsersService
  -refreshTokenRepository: RefreshTokenRepository
  -emailVerificationCodeRepository: EmailVerificationCodeRepository
  -jwtService: JwtService
  -hashEncoder: HashEncoder
  -emailService: EmailService
}

class UsersService {
  -userRepository: UserRepository
  -hashEncoder: HashEncoder
  -cloudinaryService: CloudinaryService
  -emailService: EmailService
  -emailVerificationCodeRepository: EmailVerificationCodeRepository
}

class PatternsService {
  -patternRepository: PatternRepository
  -userRepository: UserRepository
  -imageConvolutionService: ImageConvolutionService
}

class PublicationsService {
  -publicationRepository: PublicationRepository
  -patternRepository: PatternRepository
  -userRepository: UserRepository
  -cloudinaryService: CloudinaryService
  -publishedPatternRepository: PublishedPatternRepository
  -emailService: EmailService
}

class CommentsService {
  -commentRepository: CommentRepository
  -publicationRepository: PublicationRepository
  -userRepository: UserRepository
}

class TutorialsService {
  -tutorialRepository: TutorialRepository
}

class TutorialProgressesService {
  -tutorialProgressRepository: TutorialProgressRepository
  -tutorialRepository: TutorialRepository
  -userRepository: UserRepository
}

class PatternLibraryService {
  -patternLibraryRepository: PatternLibraryRepository
  -patternRepository: PatternRepository
  -userRepository: UserRepository
}

class JwtService {
  +generateAccessToken()
  +generateRefreshToken()
  +validateToken()
  +getUserIdFromToken()
}

class HashEncoder {
  +encode()
  +matches()
}

class EmailService {
  +sendVerificationCode()
  +sendPublicationDeletedEmail()
  +sendSuspensionEmail()
  +sendEmailChangeCode()
}

class CloudinaryService {
  +uploadImage()
  +deleteImage()
}

class ImageConvolutionService {
  +processImage()
}

%% =========================
%% REPOSITORIES
%% =========================

class UserRepository {
  <<interface>>
  JpaRepository~User, UUID~
}

class PatternRepository {
  <<interface>>
  JpaRepository~Pattern, UUID~
}

class PublicationRepository {
  <<interface>>
  JpaRepository~Publication, UUID~
}

class CommentRepository {
  <<interface>>
  JpaRepository~Comment, UUID~
}

class TutorialRepository {
  <<interface>>
  JpaRepository~Tutorial, UUID~
}

class TutorialProgressRepository {
  <<interface>>
  JpaRepository~TutorialProgress, UUID~
}

class RefreshTokenRepository {
  <<interface>>
  JpaRepository~RefreshTokenEntity, UUID~
}

class EmailVerificationCodeRepository {
  <<interface>>
  JpaRepository~EmailVerificationCodeEntity, UUID~
}

class PatternLibraryRepository {
  <<interface>>
  JpaRepository~PatternLibrary, UUID~
}

class PublishedPatternRepository {
  <<interface>>
  JpaRepository~PublishedPattern, UUID~
}

%% =========================
%% ENTITIES
%% =========================

class User {
  +UUID? id
  +String username
  +String email
  +String hashedPassword
  +String? profileImageUrl
  +Boolean isAdmin
  +Boolean loggedIn
  +Int status
  +LocalDate registeredDate
  +Boolean activateNotification
  +LocalDate? suspensionEndDate
  +String? token
}

class Pattern {
  +UUID? id
  +User user
  +String name
  +String? gridData
  +Int width
  +Int height
  +Boolean isPublic
  +Instant? publishedAt
  +Instant createdAt
}

class Publication {
  +UUID? id
  +User user
  +Pattern pattern
  +String description
  +Int technique
  +String? imageUrl
  +Instant? publishedAt
  +Int reportCount
}

class Comment {
  +UUID? id
  +User user
  +Publication publication
  +String content
  +Int reportCount
  +Instant createdAt
  +Instant? updatedAt
}

class Tutorial {
  +UUID? id
  +String title
  +String description
  +Int difficulty
  +String url
}

class TutorialProgress {
  +UUID? id
  +User user
  +Tutorial tutorial
  +Int status
  +LocalDate? registeredDate
}

class PatternLibrary {
  +UUID? id
  +User user
  +Pattern pattern
  +Instant savedAt
}

class PublishedPattern {
  +UUID? id
  +User user
  +Pattern pattern
  +Instant publishedAt
}

class RefreshTokenEntity {
  +UUID? id
  +UUID userId
  +String token
  +Instant expiresAt
  +Instant createdAt
}

class EmailVerificationCodeEntity {
  +UUID? id
  +String email
  +String hashedCode
  +Instant expiresAt
  +Instant createdAt
}

%% =========================
%% CONTROLLER -> SERVICE
%% =========================

AuthController --> AuthService
AuthController --> UsersService
UsersController --> UsersService
PatternsController --> PatternsService
PublicationsController --> PublicationsService
CommentsController --> CommentsService
TutorialsController --> TutorialsService
TutorialProgressesController --> TutorialProgressesService
PatternLibraryController --> PatternLibraryService
PublishedPatternsController --> PublishedPatternRepository

%% =========================
%% SERVICE -> REPOSITORY / SERVICE
%% =========================

AuthService --> UserRepository
AuthService --> UsersService
AuthService --> RefreshTokenRepository
AuthService --> EmailVerificationCodeRepository
AuthService --> JwtService
AuthService --> HashEncoder
AuthService --> EmailService

UsersService --> UserRepository
UsersService --> CloudinaryService
UsersService --> EmailService
UsersService --> EmailVerificationCodeRepository

PatternsService --> PatternRepository
PatternsService --> UserRepository
PatternsService --> ImageConvolutionService

PublicationsService --> PublicationRepository
PublicationsService --> PatternRepository
PublicationsService --> UserRepository
PublicationsService --> CloudinaryService
PublicationsService --> PublishedPatternRepository
PublicationsService --> EmailService

CommentsService --> CommentRepository
CommentsService --> PublicationRepository
CommentsService --> UserRepository

TutorialsService --> TutorialRepository

TutorialProgressesService --> TutorialProgressRepository
TutorialProgressesService --> TutorialRepository
TutorialProgressesService --> UserRepository

PatternLibraryService --> PatternLibraryRepository
PatternLibraryService --> PatternRepository
PatternLibraryService --> UserRepository

%% =========================
%% REPOSITORY -> ENTITY
%% =========================

UserRepository --> User
PatternRepository --> Pattern
PublicationRepository --> Publication
CommentRepository --> Comment
TutorialRepository --> Tutorial
TutorialProgressRepository --> TutorialProgress
RefreshTokenRepository --> RefreshTokenEntity
EmailVerificationCodeRepository --> EmailVerificationCodeEntity
PatternLibraryRepository --> PatternLibrary
PublishedPatternRepository --> PublishedPattern

%% =========================
%% ENTITY RELATIONSHIPS
%% =========================

User "1" --> "0..*" Pattern : crea
User "1" --> "0..*" Publication : publica
User "1" --> "0..*" Comment : comenta
User "1" --> "0..*" TutorialProgress : registra
User "1" --> "0..*" PatternLibrary : guarda
User "1" --> "0..*" PublishedPattern : registra publicación

Pattern "1" --> "0..*" Publication : se publica como
Pattern "1" --> "0..*" PatternLibrary : guardado en
Pattern "1" --> "0..*" PublishedPattern : publicado en

Publication "1" --> "0..*" Comment : recibe

Comment "*" --> "1" User : autor
Comment "*" --> "1" Publication : pertenece a

Pattern "*" --> "1" User : propietario
Publication "*" --> "1" User : autor
Publication "*" --> "1" Pattern : patrón

TutorialProgress "*" --> "1" User : usuario
TutorialProgress "*" --> "1" Tutorial : tutorial

PatternLibrary "*" --> "1" User : usuario
PatternLibrary "*" --> "1" Pattern : patrón

PublishedPattern "*" --> "1" User : usuario
PublishedPattern "*" --> "1" Pattern : patrón
```
