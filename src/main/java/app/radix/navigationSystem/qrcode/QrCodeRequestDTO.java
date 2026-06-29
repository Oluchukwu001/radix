package app.radix.navigationSystem.qrcode;



public record QrCodeRequestDTO(

        String qrUuid,

        Long locationNodeId,

        String physicalPlacement,

        Boolean active

) {
}