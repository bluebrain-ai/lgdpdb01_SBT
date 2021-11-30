package com.bluescript.demo.jpa;

import javax.persistence.QueryHint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;

import com.bluescript.demo.entity.PolicyEntity;

public interface IDeletePolicyJpa extends JpaRepository<PolicyEntity, String> {
    @Transactional
    @Modifying(clearAutomatically = true)
    @Query(value = "DELETE FROM POLICY WHERE ( CUSTOMERNUMBER = :db2CustomernumInt AND POLICYNUMBER = :db2PolicynumInt )", nativeQuery = true)

    void deletePolicyByDb2CustomernumIntAndDb2PolicynumInt(@Param("db2CustomernumInt") int db2CustomernumInt,
            @Param("db2PolicynumInt") int db2PolicynumInt);
}
