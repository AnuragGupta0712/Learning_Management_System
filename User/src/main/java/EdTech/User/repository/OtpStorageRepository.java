package EdTech.User.repository;

import EdTech.User.model.OtpStorage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OtpStorageRepository extends JpaRepository<OtpStorage, Long> {
    Optional<OtpStorage> findByEmail(String email);
}

