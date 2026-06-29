package app.radix.navigationSystem.utils;

import java.util.Collection;

import app.radix.navigationSystem.exceptions.BadRequestException;


public final class ValidationUtil {

    // Prevent instantiation
    private ValidationUtil() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Validates that a String is not null or blank.
     */
    public static void requireNonBlank(String value, String error) {
        if (value == null || value.isBlank()) {
            throw new BadRequestException(error);
        }
    }

    /**
     * Validates that an Object is not null.
     */
    public static void requireNonNull(Object value, String error) {
        if (value == null) {
            throw new BadRequestException(error);
        }
    }

    /**
     * Validates that a Collection is not null and not empty.
     */
    public static void requireNotEmpty(Collection<?> collection, String error) {
        if (collection == null || collection.isEmpty()) {
            throw new BadRequestException(error);
        }
    }

    /**
     * Validates that an Array is not null and not empty.
     */
    public static <T> void requireNotEmpty(T[] array, String error) {
        if (array == null || array.length == 0) {
            throw new BadRequestException(error);
        }
    }

    /**
     * Checks if a Collection is empty.
     */
    public static boolean isEmpty(Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }

    /**
     * Checks if an Array is empty.
     */
    public static <T> boolean isEmpty(T[] array) {
        return array == null || array.length == 0;
    }

    /**
     * Ensures a number is greater than zero.
     */
    public static void requirePositive(Number value, String error) {
        if (value == null || value.doubleValue() <= 0) {
            throw new BadRequestException(error);
        }
    }

    /**
     * Ensures a number is zero or greater.
     */
    public static void requireNonNegative(Number value, String error) {
        if (value == null || value.doubleValue() < 0) {
            throw new BadRequestException(error);
        }
    }

    /**
     * Validates a Double value.
     */
    public static void requireValidDouble(Double value, String error) {
        if (value == null
                || Double.isNaN(value)
                || Double.isInfinite(value)) {

            throw new BadRequestException(error);
        }
    }

    /**
     * Validates a value is within range.
     */
    public static void requireRange(
            double value,
            double min,
            double max,
            String error) {

        if (value < min || value > max) {
            throw new BadRequestException(error);
        }
    }

    /**
     * Validates latitude.
     */
    public static void requireValidLatitude(
            Double latitude,
            String error) {

        if (latitude == null
                || latitude < -90
                || latitude > 90) {

            throw new BadRequestException(error);
        }
    }

    /**
     * Validates longitude.
     */
    public static void requireValidLongitude(
            Double longitude,
            String error) {

        if (longitude == null
                || longitude < -180
                || longitude > 180) {

            throw new BadRequestException(error);
        }
    }

    /**
     * Validates email format.
     */
    public static void requireValidEmail(String email, String error) {

        requireNonBlank(email, error);

        if (!email.matches("^[\\w._%+\\-]+@[\\w.\\-]+\\.[a-zA-Z]{2,}$")) {
            throw new BadRequestException(error);
        }
    }

    /**
     * Validates maximum string length.
     */
    public static void requireMaxLength(
            String value,
            int max,
            String error) {

        if (value != null && value.length() > max) {
            throw new BadRequestException(error);
        }
    }

    /**
     * Validates minimum string length.
     */
    public static void requireMinLength(
            String value,
            int min,
            String error) {

        if (value == null || value.length() < min) {
            throw new BadRequestException(error);
        }
    }
}