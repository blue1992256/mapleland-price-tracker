package com.nangoso.pricetracker.repository;

import com.nangoso.pricetracker.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {

    Optional<Item> findByItemCode(String itemCode);

    boolean existsByItemCode(String itemCode);

    /**
     * 아이템 이름에 검색어가 포함된 아이템 찾기 (자동완성용)
     */
    List<Item> findByNameContaining(String name);
}
