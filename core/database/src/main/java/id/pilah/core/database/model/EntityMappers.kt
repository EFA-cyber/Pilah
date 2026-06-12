package id.pilah.core.database.model

import id.pilah.core.database.entity.ActionEntity
import id.pilah.core.database.entity.ClassificationEntity
import id.pilah.core.database.entity.FileEntity
import id.pilah.core.database.entity.QuarantineEntity
import id.pilah.core.database.entity.UserCorrectionEntity
import id.pilah.core.model.Classification
import id.pilah.core.model.FileAction
import id.pilah.core.model.FileItem
import id.pilah.core.model.QuarantineEntry
import id.pilah.core.model.UserCorrection

fun FileEntity.toDomain(): FileItem = FileItem(
    id = id,
    path = path,
    name = name,
    type = type,
    sizeBytes = sizeBytes,
    hash = hash,
    lastOpened = lastOpened,
    createdAt = createdAt,
)

fun FileItem.toEntity(): FileEntity = FileEntity(
    id = id,
    path = path,
    name = name,
    type = type,
    sizeBytes = sizeBytes,
    hash = hash,
    lastOpened = lastOpened,
    createdAt = createdAt,
)

fun ClassificationEntity.toDomain(): Classification = Classification(
    id = id,
    fileId = fileId,
    importanceScore = importanceScore,
    category = category,
    reason = reason,
    source = source,
    classifiedAt = classifiedAt,
)

fun Classification.toEntity(): ClassificationEntity = ClassificationEntity(
    id = id,
    fileId = fileId,
    importanceScore = importanceScore,
    category = category,
    reason = reason,
    source = source,
    classifiedAt = classifiedAt,
)

fun ActionEntity.toDomain(): FileAction = FileAction(
    id = id,
    fileId = fileId,
    actionType = actionType,
    fromPath = fromPath,
    toPath = toPath,
    executedAt = executedAt,
)

fun FileAction.toEntity(): ActionEntity = ActionEntity(
    id = id,
    fileId = fileId,
    actionType = actionType,
    fromPath = fromPath,
    toPath = toPath,
    executedAt = executedAt,
)

fun QuarantineEntity.toDomain(): QuarantineEntry = QuarantineEntry(
    id = id,
    fileId = fileId,
    quarantinedAt = quarantinedAt,
    purgeAfter = purgeAfter,
    status = status,
)

fun QuarantineEntry.toEntity(): QuarantineEntity = QuarantineEntity(
    id = id,
    fileId = fileId,
    quarantinedAt = quarantinedAt,
    purgeAfter = purgeAfter,
    status = status,
)

fun UserCorrectionEntity.toDomain(): UserCorrection = UserCorrection(
    id = id,
    fileId = fileId,
    aiCategory = aiCategory,
    userCategory = userCategory,
    correctedAt = correctedAt,
)

fun UserCorrection.toEntity(): UserCorrectionEntity = UserCorrectionEntity(
    id = id,
    fileId = fileId,
    aiCategory = aiCategory,
    userCategory = userCategory,
    correctedAt = correctedAt,
)
