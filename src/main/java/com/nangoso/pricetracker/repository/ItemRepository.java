package com.nangoso.pricetracker.repository;

import com.nangoso.pricetracker.entity.Item;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {

    Optional<Item> findByItemCode(String itemCode);

    boolean existsByItemCode(String itemCode);

    /**
     * 조회수 기준 상위 아이템 조회
     */
    @Query("SELECT i FROM Item i WHERE i.viewCount > 0 ORDER BY i.viewCount DESC LIMIT :limit")
    List<Item> findAllOrderByViewCountDesc(@Param("limit") int limit);

    /**
     * 아이템 조회수 증가 (원자적 연산)
     */
    @Modifying
    @Query("UPDATE Item i SET i.viewCount = i.viewCount + 1, i.updatedAt = CURRENT_TIMESTAMP WHERE i.itemCode = :itemCode")
    int incrementViewCount(@Param("itemCode") String itemCode);
}
