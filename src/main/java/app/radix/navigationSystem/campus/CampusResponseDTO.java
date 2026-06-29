package app.radix.navigationSystem.campus;

public record CampusResponseDTO(
        Long id,
        String campusCode,
        String name,
        String address,
        String description,
        Double latitude,
        Double longitude
) {
}
