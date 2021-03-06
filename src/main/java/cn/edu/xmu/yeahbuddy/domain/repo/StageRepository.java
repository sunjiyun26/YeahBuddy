package cn.edu.xmu.yeahbuddy.domain.repo;

import cn.edu.xmu.yeahbuddy.domain.Stage;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import javax.persistence.LockModeType;
import java.util.Optional;

public interface StageRepository extends JpaRepository<Stage, Integer> {

    @NotNull
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Stage> queryById(int id);
}
