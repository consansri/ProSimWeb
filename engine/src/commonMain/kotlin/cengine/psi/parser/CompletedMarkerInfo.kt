package cengine.psi.parser

import cengine.psi.core.PsiElementTypeDef


// Make CompletedMarkerInfo a top-level (or nested static) data class
data class CompletedMarkerInfo(
    val id: Long,             // Unique ID for this marker
    val start: Int,           // Start token index (inclusive)
    val end: Int,             // End token index (exclusive)
    val elementType: PsiElementTypeDef,
    val precededMarkerId: Long?, // ID of the marker this one precedes/wraps (used by tree builder)
)
