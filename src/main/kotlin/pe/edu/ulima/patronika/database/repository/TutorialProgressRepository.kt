package pe.edu.ulima.patronika.database.repository

import org.springframework.data.jpa.repository.JpaRepository
import pe.edu.ulima.patronika.database.model.TutorialProgress
import java.util.UUID

interface TutorialProgressRepository: JpaRepository<TutorialProgress, UUID>