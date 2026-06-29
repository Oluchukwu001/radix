package app.radix.navigationSystem.directionHint;



public record DirectionHintRequestDTO(

        Long edgeId,

        DirectionEnum direction,

        String hintText,

        String landmarkReference,

        Integer priority

) {
}