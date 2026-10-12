package br.com.maozinha.admin;

import br.com.maozinha.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ModerationActionRepository
        extends JpaRepository<ModerationAction, Long> {

    Page<ModerationAction> findAllByOrderByActionDateDescIdDesc(
            Pageable pageable
    );

    Page<ModerationAction> findByAdministratorOrderByActionDateDescIdDesc(
            User administrator,
            Pageable pageable
    );

    Page<ModerationAction> findByTargetTypeAndTargetIdOrderByActionDateDescIdDesc(
            String targetType,
            Long targetId,
            Pageable pageable
    );

    Page<ModerationAction> findByActionOrderByActionDateDescIdDesc(
            String action,
            Pageable pageable
    );
}