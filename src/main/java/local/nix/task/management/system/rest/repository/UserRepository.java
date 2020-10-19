package local.nix.task.management.system.rest.repository;

import local.nix.task.management.system.rest.model.user.User;
import local.nix.task.management.system.rest.model.user.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findUserByUsername(String username);
    Optional<User> findUserByName(String name);
    boolean existsByUsername(String username);

    @Query(value = "delete from usr where username = ?1", nativeQuery = true)
    @Modifying
    void purgeByUsername(String username);

    @Query(value = "delete from usr where id = ?1", nativeQuery = true)
    @Modifying
    void purgeById(long id);

    @Query("SELECT name FROM User where name like %:keyword%")
    Page<String> search(@Param("keyword") String keyword, Pageable pageable);

    @Query("update User u set u.status = :status where u.username = :username")
    @Modifying
    void changeStatusByUsername(String username, UserStatus status);


}
