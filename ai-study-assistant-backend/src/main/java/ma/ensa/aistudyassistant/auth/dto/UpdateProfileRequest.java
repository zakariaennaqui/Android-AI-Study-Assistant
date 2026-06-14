package ma.ensa.aistudyassistant.auth.dto;

/**
 * Request body for PATCH /api/auth/profile.
 * All fields are optional (null = no change), except currentPassword which is
 * required when newPassword is provided.
 */
public record UpdateProfileRequest(
        String username,
        String email,
        String currentPassword,
        String newPassword
) {}
