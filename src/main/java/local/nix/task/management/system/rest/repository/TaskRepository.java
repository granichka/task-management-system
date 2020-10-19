package local.nix.task.management.system.rest.repository;

import local.nix.task.management.system.rest.model.task.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    @Query(value = "delete from task where id = ?1", nativeQuery = true)
    @Modifying
    void purgeById(long id);

    @Query(value = "SELECT t FROM Task t INNER JOIN User u ON t.executor = u.id WHERE t.text LIKE '%' || :keyword || '%'"
            + " OR u.name LIKE '%' || :keyword || '%' ")
    Page<Task> search(@Param("keyword") String keyword, Pageable pageable);

}
