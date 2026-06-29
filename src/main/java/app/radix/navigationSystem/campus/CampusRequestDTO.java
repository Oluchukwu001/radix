package app.radix.navigationSystem.campus;



public record CampusRequestDTO(

                String campusCode,

                String name,

                String address,

                String description,

                Double latitude,

                Double longitude

) {
}