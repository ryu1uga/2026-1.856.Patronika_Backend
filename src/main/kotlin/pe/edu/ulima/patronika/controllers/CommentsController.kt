package pe.edu.ulima.patronika.controllers

import io.swagger.v3.oas.annotations.Operation

import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import pe.edu.ulima.patronika.ApiResponse
import pe.edu.ulima.patronika.dto.CommentRequest
import pe.edu.ulima.patronika.dto.CommentResponse
import pe.edu.ulima.patronika.services.CommentsService
import java.util.UUID

@RestController
@RequestMapping("/api/comments")
class CommentsController (
    private val commentsService: CommentsService
) {
    @GetMapping
    @Operation(summary = "List all comments")
    fun loadAllComments(): ResponseEntity<ApiResponse<List<CommentResponse>>> {
        val comments = commentsService.getAll()
        return ResponseEntity.ok(ApiResponse(true, comments))
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get comment by id")
    fun loadComment(@PathVariable id: UUID): ResponseEntity<ApiResponse<CommentResponse>> {
        val comment = commentsService.getComment(id)
        return ResponseEntity.ok(ApiResponse(true, comment))
    }

    @PostMapping
    @Operation(summary = "Create comment")
    fun postComment(
        @RequestHeader("UserId") userId: UUID,
        @Valid @RequestBody commentRequest: CommentRequest
    ): ResponseEntity<ApiResponse<CommentResponse>> {
        val insertedComment = commentsService.insertComment(userId, commentRequest)
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse(true, insertedComment))
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update comment")
    fun putComment(
        @PathVariable id: UUID,
        @Valid @RequestBody commentRequest: CommentRequest
    ) : ResponseEntity<ApiResponse<String>> {
        commentsService.updateComment(id, commentRequest)
        return ResponseEntity.ok(ApiResponse(true, "Comentario modifiado exitosamente"))
    }

    @PostMapping("/{id}/report")
    @Operation(summary = "Report comment")
    fun reportComment(@PathVariable id: UUID): ResponseEntity<ApiResponse<CommentResponse>> {
        val comment = commentsService.reportComment(id)
        return ResponseEntity.ok(ApiResponse(true, comment))
    }

    @PostMapping("/{id}/clear-reports")
    @Operation(summary = "Clear comment reports")
    fun clearCommentReports(@PathVariable id: UUID): ResponseEntity<ApiResponse<CommentResponse>> {
        val comment = commentsService.clearReports(id)
        return ResponseEntity.ok(ApiResponse(true, comment))
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete comment")
    fun deleteComment(@PathVariable id: UUID) : ResponseEntity<ApiResponse<String>> {
        commentsService.deleteComment(id)
        return ResponseEntity.ok(ApiResponse(true, "Comentario eliminado exitosamente"))
    }
}