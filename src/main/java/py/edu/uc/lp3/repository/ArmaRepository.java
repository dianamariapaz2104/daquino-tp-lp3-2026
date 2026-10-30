package py.edu.uc.lp3.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import py.edu.uc.lp3.domain.Arma;

@Repository
public interface ArmaRepository extends JpaRepository<Arma, Long> {

    Optional<Arma> findByNombreIgnoreCase(String nombre);

    boolean existsByNombreIgnoreCase(String nombre);

    List<Arma> findByEquipo_NombreIgnoreCase(String nombreEquipo);
}
