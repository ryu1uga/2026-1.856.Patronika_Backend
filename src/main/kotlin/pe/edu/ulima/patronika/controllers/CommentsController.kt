package pe.edu.ulima.patronika.controllers

import jakarta.persistence.Id
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import pe.edu.ulima.patronika.ApiResponse
import pe.edu.ulima.patronika.database.model.Comment
import pe.edu.ulima.patronika.dto.CommentRequest
import pe.edu.ulima.patronika.services.CommentsService
import pe.edu.ulima.patronika.services.TutorialProgressesService
import java.util.UUID

@RestController
@RequestMapping("/api/comments")
class CommentsController (
    private val commentsService: CommentsService
) {
    @GetMapping
    fun loadAllComments(): ResponseEntity<ApiResponse<List<Comment>>> {
        val comments = commentsService.getAll()
        return ResponseEntity.ok(ApiResponse(true, comments))
    }

    @GetMapping("/{id}")
    fun loadComment(@PathVariable id: UUID): ResponseEntity<ApiResponse<Comment>> {
        val comment = commentsService.getComment(id)
        return ResponseEntity.ok(ApiResponse(true, comment))
    }

    @PostMapping
    fun postComment(
        @RequestHeader("UserId") userId: UUID,
        @Valid @RequestBody commentRequest: CommentRequest
    ): ResponseEntity<ApiResponse<Comment>> {
        val insertedComment = commentsService.insertComment(userId, commentRequest)
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse(true, insertedComment))
    }

    @PutMapping("/{id}")
    fun putComment(
        @RequestHeader("Id") id: UUID,
        @Valid @RequestBody commentRequest: CommentRequest
    ) : ResponseEntity<ApiResponse<String>> {
        commentsService.updateComment(id, commentRequest)
        return ResponseEntity.ok(ApiResponse(true, "Comentario modifiado exitosamente"))
    }

    @DeleteMapping("/{id}")
    fun deleteComment(@PathVariable id: UUID) : ResponseEntity<ApiResponse<String>> {
        commentsService.deleteComment(id)
        return ResponseEntity.ok(ApiResponse(true, "Comentario eliminado exitosamente"))
    }
}