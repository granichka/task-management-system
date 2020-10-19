package local.nix.task.management.system.rest.repository;

import local.nix.task.management.system.rest.model.user.KnownAuthority;
import local.nix.task.management.system.rest.model.user.UserAuthority;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface UserAuthorityRepository extends JpaRepository<UserAuthority, Integer> {

    Set<KnownAuthority> ADMIN_AUTHORITIES = EnumSet.of(KnownAuthority.ROLE_USER, KnownAuthority.ROLE_ADMIN);

    Optional<UserAuthority> findByValue(KnownAuthority value);
    List<UserAuthority> findByValueIn(Set<KnownAuthority> values);
}
