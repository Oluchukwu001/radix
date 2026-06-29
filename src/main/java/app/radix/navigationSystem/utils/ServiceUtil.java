package app.radix.navigationSystem.utils;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import app.radix.navigationSystem.exceptions.ConflictException;
import app.radix.navigationSystem.exceptions.NotFoundException;
import app.radix.navigationSystem.exceptions.ValidationException;


public final class ServiceUtil {

    private ServiceUtil() {
    }

    // -------------------------------------------------------------------------
    // LOOKUP
    // -------------------------------------------------------------------------

    /**
     * Finds an entity by ID or throws NotFoundException.
     * Also guards against null or non-positive IDs before hitting the database.
     */
    public static <T, ID extends Number> T findOrThrow(
            JpaRepository<T, ID> repository,
            ID id,
            String entityName) {

        if (id == null || id.longValue() <= 0) {
            throw new ValidationException(entityName + " ID must be a positive value");
        }
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException(entityName + " not found"));
    }

    /**
     * Confirms an entity exists by ID without loading it.
     * Use this when you only need to verify existence, not work with the entity.
     */
    public static <T, ID> void assertExists(
            JpaRepository<T, ID> repository,
            ID id,
            String entityName) {

        if (!repository.existsById(id)) {
            throw new NotFoundException(entityName + " not found");
        }
    }

    /**
     * Fetches multiple entities by ID in one query.
     * Throws if any ID in the list has no matching record.
     */
    public static <T, ID> List<T> findAllOrThrow(
            JpaRepository<T, ID> repository,
            List<ID> ids,
            String entityName) {

        List<T> results = repository.findAllById(ids);
        if (results.size() != ids.size()) {
            throw new NotFoundException(
                    "One or more " + entityName + " records not found");
        }
        return results;
    }

    // -------------------------------------------------------------------------
    // PAGINATION
    // -------------------------------------------------------------------------

    /**
     * Wraps findAll with safe pagination defaults.
     * Page is floored at 0, size is capped at 100 and defaults to 20.
     */
    public static <T> Page<T> findAllPaged(
            JpaRepository<T, ?> repository,
            int page,
            int size,
            String sortBy) {

        int safePage = Math.max(page, 0);
        int safeSize = (size > 0 && size <= 100) ? size : 20;
        Pageable pageable = PageRequest.of(safePage, safeSize, Sort.by(sortBy));
        return repository.findAll(pageable);
    }

    public static <T> Page<T> findAllPaged(
            Function<Pageable, Page<T>> queryFunction,
            int page,
            int size,
            String sortBy,
            String sortDir) {

        int safePage = Math.max(page, 0);
        int safeSize = (size > 0 && size <= 100) ? size : 20;

        Sort.Direction direction = (sortDir != null && sortDir.equalsIgnoreCase("desc"))
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;

        Sort sort = Sort.by(direction, sortBy);

        Pageable pageable = PageRequest.of(safePage, safeSize, sort);

        return queryFunction.apply(pageable);
    }

    // -------------------------------------------------------------------------
    // UNIQUENESS
    // -------------------------------------------------------------------------

    /**
     * Guards against duplicate records on create.
     * Any match in the database is treated as a conflict.
     */
    public static <T> void assertUnique(
            JpaRepository<T, ?> repository,
            Example<T> example,
            String fieldDescription) {

        if (repository.exists(example)) {
            throw new ConflictException(fieldDescription + " already exists");
        }
    }

    /**
     * Guards against duplicate records on update.
     * A match against the entity being updated is allowed — only a match
     * against a different record is treated as a conflict.
     */
    public static <T extends Identifiable<ID>, ID> void assertUniqueOnUpdate(
            JpaRepository<T, ID> repository,
            Example<T> example,
            ID currentId,
            String fieldDescription) {

        repository.findOne(example).ifPresent(existing -> {
            if (!existing.getId().equals(currentId)) {
                throw new ConflictException(fieldDescription + " already exists");
            }
        });
    }

    // -------------------------------------------------------------------------
    // PARTIAL UPDATE
    // -------------------------------------------------------------------------

    /**
     * Sets a field only when the value is non-null.
     * Replaces Optional.ofNullable(value).ifPresent(setter) across update methods.
     */
    public static <T> void setIfPresent(T value, Consumer<T> setter) {
        if (value != null) {
            setter.accept(value);
        }
    }

    /**
     * Sets a field only when the value is non-null, running a validator first.
     * Validation is skipped entirely when the value is absent.
     */
    public static <T> void setIfPresent(
            T value,
            Consumer<T> setter,
            Consumer<T> validator) {

        if (value != null) {
            validator.accept(value);
            setter.accept(value);
        }
    }

    // -------------------------------------------------------------------------
    // SOFT DELETE
    // -------------------------------------------------------------------------

    /**
     * Marks an entity as deleted without removing it from the database.
     * Requires the entity to implement SoftDeletable.
     */
    public static <T extends SoftDeletable, ID extends Number> void softDelete(
            JpaRepository<T, ID> repository,
            ID id,
            String entityName) {

        T entity = findOrThrow(repository, id, entityName);
        entity.setDeleted(true);
        repository.save(entity);
    }
}