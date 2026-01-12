package com.study.grpc.user.repository;

import com.study.grpc.user.domain.User;
import com.study.grpc.user.domain.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * User Repository
 *
 * Spring Data JPA를 사용한 데이터 액세스 계층
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * 이메일로 회원 조회
     */
    Optional<User> findByEmail(String email);

    /**
     * 이메일 존재 여부 확인
     */
    boolean existsByEmail(String email);

    /**
     * 상태별 회원 목록 조회
     */
    List<User> findByStatus(UserStatus status);

    /**
     * 이름으로 회원 검색 (LIKE 검색)
     */
    List<User> findByNameContaining(String name);

    /**
     * 활성 회원 목록 조회 (커스텀 쿼리)
     */
    @Query("SELECT u FROM User u WHERE u.status = 'ACTIVE'")
    List<User> findAllActiveUsers();

    /**
     * 이메일과 상태로 회원 조회
     */
    @Query("SELECT u FROM User u WHERE u.email = :email AND u.status = :status")
    Optional<User> findByEmailAndStatus(
        @Param("email") String email,
        @Param("status") UserStatus status
    );

    /**
     * 삭제되지 않은 회원 수 조회
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.status != 'DELETED'")
    long countActiveUsers();
}

