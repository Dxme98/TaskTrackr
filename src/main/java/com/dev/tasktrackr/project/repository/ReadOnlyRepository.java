package com.dev.tasktrackr.project.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.Repository;

import java.util.List;
import java.util.Optional;

@NoRepositoryBean
public interface ReadOnlyRepository<T, ID> extends JpaRepository<T, ID> {
    // Alle Schreibmethoden blockieren
    @Override @Deprecated default <S extends T> S save(S entity) { throw readOnlyException(); }
    @Override @Deprecated default <S extends T> List<S> saveAll(Iterable<S> entities) { throw readOnlyException(); }
    @Override @Deprecated default void delete(T entity) { throw readOnlyException(); }
    @Override @Deprecated default void deleteAll() { throw readOnlyException(); }
    @Override @Deprecated default void deleteAll(Iterable<? extends T> entities) { throw readOnlyException(); }
    @Override @Deprecated default void deleteById(ID id) { throw readOnlyException(); }
    @Override @Deprecated default void deleteAllInBatch() { throw readOnlyException(); }
    @Override @Deprecated default void deleteAllInBatch(Iterable<T> entities) { throw readOnlyException(); }

    private static UnsupportedOperationException readOnlyException() {
        return new UnsupportedOperationException("This is a read-only repository");
    }
}
