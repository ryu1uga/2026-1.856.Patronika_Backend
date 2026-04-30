package pe.edu.ulima.patronika.database.repository

import org.springframework.data.jpa.repository.JpaRepository
import pe.edu.ulima.patronika.database.model.Tutorial
import java.util.UUID

interface TutorialRepository: JpaRepository<Tutorial, UUID>